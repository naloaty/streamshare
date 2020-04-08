package com.naloaty.syncshare.util;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.naloaty.syncshare.config.AppConfig;

public class NsdHelper {
    private static final String TAG = "NsdHelper";
    public static final String NSD_ENABLED = "nsd_enabled";

    private Context mContext;
    private NsdManager mNsdManager;
    private NsdManager.RegistrationListener mRegistrationListener;
    private NsdManager.DiscoveryListener mDiscoveryListener;
    private String mServiceName;

    public NsdHelper(Context context) {
        this.mContext = context;
    }

    public void registerService()
    {
        //TODO: NSD problem
        //Nsd discovery works only on SDK 16 and above
        //So we need to think another method for device discovery (bruteforce through all ip?)
        //Or just set minimum SDK version to 16 ¯\_(ツ)_/¯
        if (isNsdDiscoveryEnabled() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {

            final NsdServiceInfo serviceInfo = new NsdServiceInfo();

            serviceInfo.setServiceName(AppConfig.NSD_SERVICE_NAME + "_" + AppUtils.getUniqueNumber());
            serviceInfo.setServiceType(AppConfig.NSD_SERVICE_TYPE);
            serviceInfo.setPort(AppConfig.SERVER_PORT);

            try {
                getNsdManager().registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, getRegistrationListener());
            } catch (Exception e) {
                e.printStackTrace();
            }

            Log.i(TAG, "NSD registered");
        }
        else
        {
            Log.i(TAG, "registerService();");
        }
    }

    public NsdManager.RegistrationListener getRegistrationListener()
    {
        if (isNsdDiscoveryEnabled()
                && mRegistrationListener == null
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            mRegistrationListener = new NsdManager.RegistrationListener()
            {
                @Override
                public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode)
                {
                    Log.e(TAG, "NDS registration failed with error code " + errorCode);
                }

                @Override
                public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode)
                {
                    Log.e(TAG, "NDS failed to unregister with error code " + errorCode);
                }

                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onServiceRegistered(NsdServiceInfo serviceInfo)
                {
                    mServiceName = serviceInfo.getServiceName();
                    Log.v(TAG, "NDS registered with success " + serviceInfo.getServiceName());
                }

                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onServiceUnregistered(NsdServiceInfo serviceInfo)
                {
                    Log.i(TAG, "NDS unregistered with success " + serviceInfo.getServiceName());
                }
            };

        return mRegistrationListener;
    }

    public boolean isNsdDiscoveryEnabled() {
        return AppUtils.getDefaultSharedPreferences(mContext).getBoolean(NSD_ENABLED, true);
    }

    public NsdManager getNsdManager()
    {
        if (mNsdManager == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            mNsdManager = (NsdManager) mContext.getSystemService(Context.NSD_SERVICE);
        else
            Log.i(TAG, "getNsdManager()");

        return mNsdManager;
    }

    public NsdManager.DiscoveryListener getDiscoveryListener()
    {
        if (mDiscoveryListener == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mDiscoveryListener = new NsdManager.DiscoveryListener()
            {
                @Override
                public void onStartDiscoveryFailed(String serviceType, int errorCode)
                {
                    Log.e(TAG, "NSD discovery failed to start with error code " + errorCode);
                }

                @Override
                public void onStopDiscoveryFailed(String serviceType, int errorCode)
                {
                    Log.e(TAG, "NSD discovery failed to stop with error code " + errorCode);
                }

                @Override
                public void onDiscoveryStarted(String serviceType)
                {
                    Log.v(TAG, "NSD discovery started");
                }

                @Override
                public void onDiscoveryStopped(String serviceType)
                {
                    Log.v(TAG, "NSD discovery stopped");
                }

                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onServiceFound(NsdServiceInfo service) {
                    Log.i(TAG, "Service discovery found something: " + service.getServiceName());
                    if (!service.getServiceType().equals(AppConfig.NSD_SERVICE_TYPE)) {
                        Log.i(TAG, "Unknown Service Type: " + service.getServiceType());
                    }
                    else if (service.getServiceName().startsWith(AppConfig.NSD_SERVICE_NAME))
                    {
                        mNsdManager.resolveService(service, new NsdManager.ResolveListener() {
                            @Override
                            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                                Log.i(TAG, "Resolve failed for " + serviceInfo.getServiceName());
                            }

                            @Override
                            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                                Log.i(TAG, "Resolved with success " + serviceInfo.getServiceName()
                                        + " with IP address of " + serviceInfo.getHost().getHostAddress());
                                NetworkDeviceManager.manageDevice(mContext, serviceInfo.getHost().getHostAddress(), serviceInfo.getServiceName());
                            }
                        });
                    }
                }

                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onServiceLost(NsdServiceInfo serviceInfo)
                {
                    Log.i(TAG, "NSD service is now lost which is of " + serviceInfo.getServiceName());
                    NetworkDeviceManager.manageLostDevice(mContext, serviceInfo.getServiceName());
                }
            };
        }

        return mDiscoveryListener;
    }

    public void startDiscovering()
    {
        if (isNsdDiscoveryEnabled() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            try {
                getNsdManager().discoverServices(AppConfig.NSD_SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, getDiscoveryListener());
            } catch (Exception e) {
                e.printStackTrace();
            }

            Log.i(TAG, "NSD Started");
        }
    }

    public void stopDiscovering()
    {
        if (isNsdDiscoveryEnabled() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            try {
                getNsdManager().stopServiceDiscovery(getDiscoveryListener());
            } catch (Exception e) {
                e.printStackTrace();
            }

            Log.i(TAG, "NSD Stopped");
        }
    }

    public void unregisterService()
    {
        if (isNsdDiscoveryEnabled() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            try {
                getNsdManager().unregisterService(getRegistrationListener());
            } catch (Exception e) {
                e.printStackTrace();
            }
    }


}
