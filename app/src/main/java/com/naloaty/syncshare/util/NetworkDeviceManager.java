package com.naloaty.syncshare.util;

import android.content.Context;
import android.util.Log;


import com.naloaty.syncshare.database.device.NetworkDevice;
import com.naloaty.syncshare.database.device.NetworkDeviceRepository;

import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class NetworkDeviceManager {

    private static final String TAG = NetworkDeviceManager.class.getSimpleName();

    public static final int JOB_DETECTIVE_ID = 43123;

    public static void manageDevice(Context context, NetworkDevice networkDevice) {
        String myId = AppUtils.getDeviceId(context);

        if (networkDevice.getDeviceId().equals(myId))
            return;

        processDevice(context, networkDevice);
    }

    public static void processDevice(final Context context, final NetworkDevice netDevice)
    {
        NetworkDeviceRepository repository = new NetworkDeviceRepository(context);
        //NetworkDevice entry = repository.findDeviceDep(networkDevice.getIpAddress(), networkDevice.getDeviceId(), networkDevice.getServiceName());

        Single<NetworkDevice> netDeviceSingle = repository.findDevice(netDevice.getIpAddress(), netDevice.getDeviceId(), netDevice.getServiceName());

        Disposable disposable = netDeviceSingle
                .subscribeOn(Schedulers.io())
                .subscribeWith(new DisposableSingleObserver<NetworkDevice>() {
                    @Override
                    public void onSuccess(NetworkDevice networkDevice) {
                        netDevice.setId(networkDevice.getId());
                        repository.update(netDevice);
                    }

                    @Override
                    public void onError(Throwable e) {
                        repository.insert(netDevice);
                    }
                });

        //disposable.dispose();

    }

    public static void manageLostDevice(Context context, String serviceName) {

        NetworkDeviceRepository repository = new NetworkDeviceRepository(context);

        Single<NetworkDevice> netDeviceSingle = repository.findDevice(null, null, serviceName);

        Disposable disposable = netDeviceSingle
                .subscribeOn(Schedulers.io())
                .subscribeWith(new DisposableSingleObserver<NetworkDevice>() {
                    @Override
                    public void onSuccess(NetworkDevice networkDevice) {
                        repository.delete(networkDevice);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, String.format("Network device SN: %s not found in database", serviceName));
                    }
                });
    }
}
