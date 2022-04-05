package com.naloaty.streamshare.util;

import android.content.Context;
import android.util.Log;


import com.naloaty.streamshare.database.entity.NetworkDevice;
import com.naloaty.streamshare.database.repository.NetworkDeviceRepository;

import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.internal.EverythingIsNonNull;

/**
 * This class manages all found nearby devices.
 */
public class NetworkDeviceManager {

    //TODO: add composite disposable

    private static final String TAG = "NetworkDeviceManager";

    /**
     * Adds the found device to the database.
     * @param context The Context in which this operation should be executed.
     * @param networkDevice Network information about found device.
     */
    @EverythingIsNonNull
    public static void manageDevice(Context context, NetworkDevice networkDevice) {
        String myId = AppUtils.getDeviceId(context);

        if (networkDevice.getDeviceId().equals(myId))
            return;

        processDevice(context, networkDevice);
    }

    /**
     * Adds the found device to the database.
     * @param context The Context in which this operation should be executed.
     * @param networkDevice Network information about found device.
     */
    @EverythingIsNonNull
    private static void processDevice(Context context, NetworkDevice networkDevice) {
        NetworkDeviceRepository repository = new NetworkDeviceRepository(context);

        Single<NetworkDevice> netDeviceSingle = repository.findDevice(networkDevice.getIpAddress(), networkDevice.getDeviceId(), networkDevice.getServiceName());

        Disposable disposable = netDeviceSingle
                .subscribeOn(Schedulers.io())
                .subscribeWith(new DisposableSingleObserver<NetworkDevice>() {
                    @Override
                    public void onSuccess(NetworkDevice networkDevice) {
                        networkDevice.setId(networkDevice.getId());
                        repository.update(networkDevice);

                        Log.i(TAG, String.format("Network device SN: %s updated in the database", networkDevice.getServiceName()));
                    }

                    @Override
                    public void onError(Throwable e) {
                        repository.insert(networkDevice);

                        Log.i(TAG, String.format("Network device SN: %s inserted into the database", networkDevice.getServiceName()));
                    }
                });

    }

    /**
     * Remove the lost device from the database.
     * @param context The Context in which this operation should be executed.
     * @param serviceName Service name of the found device.
     * @see DNSSDHelper
     */
    @EverythingIsNonNull
    public static void manageLostDevice(Context context, String serviceName) {

        NetworkDeviceRepository repository = new NetworkDeviceRepository(context);

        Single<NetworkDevice> netDeviceSingle = repository.findDevice(null, null, serviceName);

        Disposable disposable = netDeviceSingle
                .subscribeOn(Schedulers.io())
                .subscribeWith(new DisposableSingleObserver<NetworkDevice>() {
                    @Override
                    public void onSuccess(NetworkDevice networkDevice) {
                        repository.delete(networkDevice);

                        Log.i(TAG, String.format("Network device SN: %s removed from the database", serviceName));
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.i(TAG, String.format("Network device SN: %s is not found in database", serviceName));
                    }
                });
    }
}
