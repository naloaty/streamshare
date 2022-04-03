package com.naloaty.streamshare.util;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.naloaty.streamshare.R;
import com.naloaty.streamshare.communication.CommunicationHelper;
import com.naloaty.streamshare.communication.SimpleServerResponse;
import com.naloaty.streamshare.database.device.NetworkDevice;
import com.naloaty.streamshare.database.device.NetworkDeviceRepository;
import com.naloaty.streamshare.database.device.SSDevice;
import com.naloaty.streamshare.database.device.SSDeviceRepository;
import com.naloaty.streamshare.dialog.SSProgressDialog;

import javax.net.ssl.SSLProtocolException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

/**
 * This class helps to add trusted devices.
 * @see com.naloaty.streamshare.activity.AddDeviceActivity
 * @see com.naloaty.streamshare.fragment.NearbyDiscoveryFragment
 */
public class AddDeviceHelper {

    private static final String TAG = "AddDeviceHelper";

    public static final int ERROR_ALREADY_ADDED       = 0;
    public static final int ERROR_UNTRUSTED_DEVICE    = 1;
    public static final int ERROR_DEVICE_OFFLINE      = 2;
    public static final int ERROR_BAD_RESPONSE        = 3;
    public static final int ERROR_SSL_PROTOCOL_EXCEPTION = 4;
    public static final int ERROR_REQUEST_FAILED      = 5;
    public static final int ERROR_SENDING_FAILED      = 6;

    private final Context mContext;
    private final SSDevice mSSDevice;
    private final AddDeviceCallback mCallback;
    private final SSProgressDialog mProgressDialog;

    public interface AddDeviceCallback {
        void onSuccessfullyAdded();
        void onException(int errorCode);
    }

    /**
     * @param context The Context in which this instance should be created.
     * @param device Information about local device. See {@link AppUtils#getLocalDevice(Context)}.
     * @param callback The callback that will be called when device adding is complete.
     */
    public AddDeviceHelper(@NonNull Context context, @NonNull SSDevice device, AddDeviceCallback callback) {
        mContext = context;
        mSSDevice = device;
        mCallback = callback;
        mProgressDialog = new SSProgressDialog(context);
    }

    /**
     * Communicates with potential trusted device and, if successful, adds it to the database.
     */
    private void addDevice() {
        SSDeviceRepository ssDeviceRepo = new SSDeviceRepository(mContext);

        if (mSSDevice.isTrusted())
            ssDeviceRepo.publish(mSSDevice);

        final NetworkDeviceRepository netDeviceRepo = new NetworkDeviceRepository(mContext);
        NetworkDevice onlineDevice = netDeviceRepo.findDeviceDep(null, mSSDevice.getDeviceId(), null);

        if (onlineDevice != null) {
            Call<SSDevice> request = CommunicationHelper.requestDeviceInformation(mContext, onlineDevice);

            mProgressDialog.setMessage(R.string.text_onRequestDeviceInfo);

            if (!mProgressDialog.isShowing())
                mProgressDialog.show();

            request.enqueue(new Callback<SSDevice>() {
                @Override
                @EverythingIsNonNull
                public void onResponse(Call<SSDevice> call, Response<SSDevice> response) {
                    SSDevice remoteDevice = response.body();

                    if (remoteDevice != null) {
                        ssDeviceRepo.publish(remoteDevice);

                        sendDeviceInfo(onlineDevice);
                        return;
                    }

                    if (mProgressDialog.isShowing())
                        mProgressDialog.dismiss();

                    mCallback.onException(ERROR_BAD_RESPONSE);
                }

                @Override
                @EverythingIsNonNull
                public void onFailure(Call<SSDevice> call, Throwable t) {
                    if (mProgressDialog.isShowing())
                        mProgressDialog.dismiss();

                    Log.e(TAG, String.format("Device information request is failed. Reason: %s", t.getMessage()));

                    if (t instanceof SSLProtocolException)
                        mCallback.onException(ERROR_SSL_PROTOCOL_EXCEPTION);
                    else
                        mCallback.onException(ERROR_REQUEST_FAILED);
                }
            });

            return;
        }

        mCallback.onException(ERROR_DEVICE_OFFLINE);
    }

    /**
     * Sends general information about a local device to a remote device.
     * @param networkDevice Network information about remote device. See {@link com.naloaty.streamshare.util.DNSSDHelper}.
     */
    private void sendDeviceInfo(NetworkDevice networkDevice) {
        Call<SimpleServerResponse> request = CommunicationHelper.sendDeviceInformation(mContext, networkDevice, AppUtils.getLocalDevice(mContext));

        mProgressDialog.setMessage(R.string.text_onSendingDeviceInfo);

        if (!mProgressDialog.isShowing())
            mProgressDialog.show();

        request.enqueue(new Callback<SimpleServerResponse>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<SimpleServerResponse> call, Response<SimpleServerResponse> response) {
                if (mProgressDialog.isShowing())
                    mProgressDialog.dismiss();

                if (response.isSuccessful())
                    mCallback.onSuccessfullyAdded();
                else
                    mCallback.onException(ERROR_SENDING_FAILED);
            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<SimpleServerResponse> call, Throwable t) {
                if (mProgressDialog.isShowing())
                    mProgressDialog.dismiss();

                Log.w(TAG, String.format("Sending device information is failed. Reason: %s", t.getMessage()));
                mCallback.onException(ERROR_SENDING_FAILED);
            }
        });
    }

    /**
     * Initiates the process of adding a device.
     */
    public void processDevice() {
        SSDeviceRepository ssDeviceRepo = new SSDeviceRepository(mContext);
        SSDevice existentDevice = ssDeviceRepo.findDeviceDep(mSSDevice.getDeviceId());

        if (existentDevice != null) {
            mCallback.onException(ERROR_ALREADY_ADDED);
            return;
        }

        askLocalApproval();
    }

    /**
     * Asks the user if the device he wants to add is trusted.
     */
    private void askLocalApproval() {
        new AlertDialog.Builder(mContext)
                .setTitle(R.string.title_trustQuestion)
                .setMessage(String.format(mContext.getString(R.string.text_trustQuestionHelp), mSSDevice.getDeviceId()))
                .setNegativeButton(R.string.btn_no, (dialog, which) -> mCallback.onException(ERROR_UNTRUSTED_DEVICE))
                .setPositiveButton(R.string.btn_yes, ((dialog, which) -> {
                    mSSDevice.setTrusted(true);
                    mSSDevice.setAccessAllowed(true);
                    addDevice();
                }))
                .show();
    }

    /**
     * @return Empty instance of {@link SSDevice}.
     */
    public static SSDevice getEmptyDevice() {
        SSDevice device = new SSDevice("-", "-");
        device.setBrand("-");
        device.setModel("-");
        device.setAppVersion("-::-");
        device.setLastUsageTime(0);
        device.setAccessAllowed(false);
        device.setTrusted(false);
        device.setNickname("-");

        return device;
    }
}
