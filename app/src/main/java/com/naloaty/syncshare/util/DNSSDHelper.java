package com.naloaty.syncshare.util;

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
import com.naloaty.syncshare.config.AppConfig;
import com.naloaty.syncshare.database.device.NetworkDevice;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

/*
 * Copied from library example app
 * https://github.com/andriydruk/RxDNSSD/
 */

public class DNSSDHelper {

    private static final String TAG = "DNSSDHelper";

    private static final String
                    TXT_RECORD_DEVICE_ID = "deviceId",
                    TXT_RECORD_DEVICE_NICKNAME = "deviceNickname",
                    TXT_RECORD_APP_VERSION = "appVersion";

    private DNSSD mDNSSD;
    private Handler mHandler;

    private String mServiceName;
    private Context mContext;

    private DNSSDService mBrowseService;
    private DNSSDService mRegisterService;

    public DNSSDHelper(Context context) {
        this.mDNSSD = new DNSSDBindable(context);
        this.mHandler = new Handler(Looper.getMainLooper());
        this.mContext = context;
    }

    public void startBrowse() {
        Log.d(TAG, "Start browse");
        try {
            mBrowseService = mDNSSD.browse(AppConfig.DNSSD_SERVICE_TYPE, new BrowseListener() {
                @Override
                public void serviceFound(DNSSDService browser, int flags, int ifIndex, final String serviceName, String regType, String domain) {
                    Log.d(TAG, "Found " + serviceName);

                    startResolve(flags, ifIndex, serviceName, regType, domain);
                }

                @Override
                public void serviceLost(DNSSDService browser, int flags, int ifIndex, String serviceName, String regType, String domain) {
                    NetworkDeviceManager.manageLostDevice(mContext, serviceName);
                }

                @Override
                public void operationFailed(DNSSDService service, int errorCode) {
                    Log.e("TAG", "error: " + errorCode);
                }
            });
        } catch (DNSSDException e) {
            e.printStackTrace();
            Log.e(TAG, "Error", e);
        }
    }

    private void startResolve(int flags, int ifIndex, final String serviceName, final String regType, final String domain) {
        try {
            mDNSSD.resolve(flags, ifIndex, serviceName, regType, domain, new ResolveListener() {
                @Override
                public void serviceResolved(DNSSDService resolver, int flags, int ifIndex, String fullName, String hostName, int port, Map<String, String> txtRecord) {
                    Log.d("TAG", "Resolved " + hostName);
                    startQueryRecords(flags, ifIndex, serviceName, regType, domain, hostName, port, txtRecord);
                }

                @Override
                public void operationFailed(DNSSDService service, int errorCode) {

                }
            });
        } catch (DNSSDException e) {
            e.printStackTrace();
        }
    }

    private void startQueryRecords(int flags, int ifIndex, final String serviceName, final String regType, final String domain, final String hostName, final int port, final Map<String, String> txtRecord) {
        try {
            QueryListener listener = new QueryListener() {
                @Override
                public void queryAnswered(DNSSDService query, int flags, int ifIndex, String fullName, int rrtype, int rrclass, byte[] rdata, int ttl) {
                    Log.d(TAG, "Query address " + fullName);
                    mHandler.post(() -> {
                        //BonjourService.Builder builder = new BonjourService.Builder(flags, ifIndex, serviceName, regType, domain).dnsRecords(txtRecord).port(port).hostname(hostName);
                        try {
                            InetAddress address = InetAddress.getByAddress(rdata);

                            NetworkDevice device = new NetworkDevice(address.getHostAddress(), serviceName);
                            device.setDeviceName(txtRecord.get(TXT_RECORD_DEVICE_NICKNAME));
                            device.setDeviceId(txtRecord.get(TXT_RECORD_DEVICE_ID));
                            device.setAppVersion(txtRecord.get(TXT_RECORD_APP_VERSION));
                            NetworkDeviceManager.manageDevice(mContext, device);

                        } catch (UnknownHostException e) {
                            e.printStackTrace();
                        }

                    });
                }

                @Override
                public void operationFailed(DNSSDService service, int errorCode) {

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

    public void stopBrowse() {
        Log.d("TAG", "Stop browsing");
        mBrowseService.stop();
        mBrowseService = null;
    }

    public void register() {
        Log.d(TAG, "register");
        try {
            mServiceName = AppConfig.DNSSD_SERVICE_NAME + "_" + AppUtils.getUniqueNumber();

            TXTRecord extraData = new TXTRecord();
            extraData.set(TXT_RECORD_DEVICE_ID, AppUtils.getDeviceId(mContext));
            extraData.set(TXT_RECORD_DEVICE_NICKNAME, AppUtils.getLocalDeviceName());
            extraData.set(TXT_RECORD_APP_VERSION, AppConfig.APP_VERSION);

            //mServiceName, AppConfig.DNSSD_SERVICE_TYPE, AppConfig.DNSSD_SERVER_PORT
            mRegisterService = mDNSSD.register(0, 0, mServiceName, AppConfig.DNSSD_SERVICE_TYPE, null, null, AppConfig.DNSSD_SERVER_PORT, extraData,  new RegisterListener() {
                @Override
                public void serviceRegistered(DNSSDRegistration registration, int flags, String serviceName, String regType, String domain) {
                    Log.d(TAG, "Register successfully " + serviceName);
                }

                @Override
                public void operationFailed(DNSSDService service, int errorCode) {
                    Log.e(TAG, "Error " + errorCode);
                }
            });
        } catch (DNSSDException e) {
            e.printStackTrace();
        }
    }

    public void unregister() {
        Log.d(TAG, "Unregister");
        mRegisterService.stop();
        mRegisterService = null;
    }


}
