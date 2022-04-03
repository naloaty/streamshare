package com.naloaty.streamshare.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.github.druk.dnssd.BrowseListener;
import com.github.druk.dnssd.DNSSD;
import com.github.druk.dnssd.DNSSDBindable;
import com.github.druk.dnssd.DNSSDException;
import com.github.druk.dnssd.DNSSDRegistration;
import com.github.druk.dnssd.DNSSDService;
import com.github.druk.dnssd.QueryListener;
import com.github.druk.dnssd.RegisterListener;
import com.github.druk.dnssd.ResolveListener;
import com.github.druk.dnssd.TXTRecord;
import com.naloaty.streamshare.config.AppConfig;
import com.naloaty.streamshare.database.device.NetworkDevice;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * This class helps to manage the discovery service.
 * @see com.naloaty.streamshare.service.CommunicationService
 */
public class DNSSDHelper {

    private static final String TAG = "DNSSDHelper";

    private static final String TXT_RECORD_DEVICE_ID       = "deviceId";
    private static final String TXT_RECORD_DEVICE_NICKNAME = "deviceNickname";
    private static final String TXT_RECORD_APP_VERSION     = "appVersion";

    private DNSSD mDNSSD;
    private Handler mHandler;

    private String mServiceName;
    private Context mContext;

    private DNSSDService mBrowseService;
    private DNSSDService mRegisterService;

    /**
     * @param context The Context in which this request should be executed.
     */
    public DNSSDHelper(Context context) {
        this.mDNSSD = new DNSSDBindable(context);
        this.mHandler = new Handler(Looper.getMainLooper());
        this.mContext = context;
    }

    /**
     * Starts a search for nearby devices
     */
    public void startBrowse() {
        Log.i(TAG, "Starting to browse nearby devices");

        try {
            mBrowseService = mDNSSD.browse(AppConfig.DNSSD_SERVICE_TYPE, new BrowseListener() {
                @Override
                public void serviceFound(DNSSDService browser, int flags, int ifIndex, final String serviceName, String regType, String domain) {
                    Log.i(TAG, String.format("Found device. Service name is %s", serviceName));
                    startResolve(flags, ifIndex, serviceName, regType, domain);
                }

                @Override
                public void serviceLost(DNSSDService browser, int flags, int ifIndex, String serviceName, String regType, String domain) {
                    Log.i(TAG, String.format("Lost device. Service name is %s", serviceName));
                    NetworkDeviceManager.manageLostDevice(mContext, serviceName);
                }

                @Override
                public void operationFailed(DNSSDService service, int errorCode) {
                    Log.e(TAG, String.format("Error: %d ", errorCode));
                }
            });
        } catch (DNSSDException e) {
            e.printStackTrace();
            Log.e(TAG, String.format("Error: %s ", e.toString()));
        }
    }

    /**
     * Stops a search for nearby devices
     */
    public void stopBrowse() {
        Log.d("TAG", "Stopping to browse nearby devices");
        mBrowseService.stop();
        mBrowseService = null;
    }

    /**
     * Registers the local device service on the current network.
     */
    public void register() {
        Log.i(TAG, "Registering service");

        try {
            mServiceName = AppConfig.DNSSD_SERVICE_NAME + "_" + AppUtils.getUniqueNumber();

            TXTRecord extraData = new TXTRecord();
            extraData.set(TXT_RECORD_DEVICE_ID, AppUtils.getDeviceId(mContext));
            extraData.set(TXT_RECORD_DEVICE_NICKNAME, AppUtils.getLocalDeviceName());
            extraData.set(TXT_RECORD_APP_VERSION, AppConfig.APP_VERSION);

            mRegisterService = mDNSSD.register(0, 0, mServiceName, AppConfig.DNSSD_SERVICE_TYPE, null, null, AppConfig.DNSSD_SERVER_PORT, extraData,  new RegisterListener() {
                @Override
                public void serviceRegistered(DNSSDRegistration registration, int flags, String serviceName, String regType, String domain) {
                    Log.i(TAG, String.format("Service registered successfully. Service name is %s", serviceName));
                }

                @Override
                public void operationFailed(DNSSDService service, int errorCode) {
                    Log.e(TAG, String.format("Cannot register service. Error code: %d", errorCode));
                }
            });
        } catch (DNSSDException e) {
            Log.e(TAG, String.format("Error during registering service: %s", e.toString()));
        }
    }

    /**
     * Unregisters the local device service on the current network.
     */
    public void unregister() {
        Log.i(TAG, "Unregistering service");

        mRegisterService.stop();
        mRegisterService = null;
    }

    /**
     * Resolves the found service.
     */
    private void startResolve(int flags, int ifIndex, final String serviceName, final String regType, final String domain) {

        Log.i(TAG, "Resolving the found service");

        try {
            mDNSSD.resolve(flags, ifIndex, serviceName, regType, domain, new ResolveListener() {
                @Override
                public void serviceResolved(DNSSDService resolver, int flags, int ifIndex, String fullName, String hostName, int port, Map<String, String> txtRecord) {
                    Log.i(TAG, String.format("Resolved service with hostname %s", hostName));
                    startQueryRecords(flags, ifIndex, serviceName, regType, domain, hostName, port, txtRecord);
                }

                @Override
                public void operationFailed(DNSSDService service, int errorCode) {
                    Log.e(TAG, String.format("Cannot resolve service. Error code: %d", errorCode));
                }
            });
        } catch (DNSSDException e) {
            e.printStackTrace();
        }
    }

    /**
     * Queries TXT records from the found service.
     */
    private void startQueryRecords(int flags, int ifIndex, final String serviceName, final String regType, final String domain, final String hostName, final int port, final Map<String, String> txtRecord) {

        Log.i(TAG, "Querying TXT Records of the found service");

        try {
            QueryListener listener = new QueryListener() {
                @Override
                public void queryAnswered(DNSSDService query, int flags, int ifIndex, String fullName, int rrtype, int rrclass, byte[] rdata, int ttl) {
                    mHandler.post(() -> {
                        //BonjourService.Builder builder = new BonjourService.Builder(flags, ifIndex, serviceName, regType, domain).dnsRecords(txtRecord).port(port).hostname(hostName);
                        try {
                            InetAddress address = InetAddress.getByAddress(rdata);

                            NetworkDevice device = new NetworkDevice(address.getHostAddress(), serviceName);
                            device.setDeviceName(txtRecord.get(TXT_RECORD_DEVICE_NICKNAME));
                            device.setDeviceId(txtRecord.get(TXT_RECORD_DEVICE_ID));
                            device.setAppVersion(txtRecord.get(TXT_RECORD_APP_VERSION));
                            NetworkDeviceManager.manageDevice(mContext, device);

                            Log.i(TAG, String.format("Recognized the found service: SN: %s;  IP: %s; Name: %s; ID: %s; App: %s;", serviceName, device.getIpAddress(),
                                    device.getDeviceName(), device.getDeviceId(), device.getAppVersion()));

                        } catch (UnknownHostException e) {
                            Log.e(TAG, String.format("Cannot resolve ipv4 of the found service. Reason: %s", e.toString()));
                        }

                    });
                }

                @Override
                public void operationFailed(DNSSDService service, int errorCode) {
                    Log.e(TAG, String.format("Cannot resolve TXT Records of the found service. Error code: %s", errorCode));
                }
            };
            //ip v4 query
            mDNSSD.queryRecord(0, ifIndex, hostName, 1, 1, listener);

            //ip v6 query
            //mDNSSD.queryRecord(0, ifIndex, hostName, 28, 1, listener);
        } catch (DNSSDException e) {
            e.printStackTrace();
        }
    }

}
