package com.naloaty.syncshare.util;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import com.naloaty.syncshare.R;
import com.naloaty.syncshare.communication.CommunicationHelper;
import com.naloaty.syncshare.communication.SimpleServerResponse;
import com.naloaty.syncshare.database.device.NetworkDevice;
import com.naloaty.syncshare.database.device.NetworkDeviceRepository;
import com.naloaty.syncshare.database.device.SSDevice;
import com.naloaty.syncshare.database.device.SSDeviceRepository;
import com.naloaty.syncshare.dialog.SSProgressDialog;

import javax.net.ssl.SSLHandshakeException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddDeviceHelper {

    private static final String TAG = "AddDeviceHelper";
    public static final int ERROR_ALREADY_ADDED = 0;
    public static final int ERROR_UNTRUSTED_DEVICE = 1;
    public static final int ERROR_DEVICE_OFFLINE = 2;
    public static final int ERROR_BAD_RESPONSE = 3;
    public static final int ERROR_HANDSHAKE_EXCEPTION = 4;
    public static final int ERROR_REQUEST_FAILED = 5;
    public static final int ERROR_SENDING_FAILED = 6;

    private final Context mContext;
    private final SSDevice mSSDevice;
    private final AddDeviceCallback mCallback;
    private final SSProgressDialog mProgressDialog;

    public AddDeviceHelper(final Context context, final SSDevice device, final AddDeviceCallback callback) {
        mContext = context;
        mSSDevice = device;
        mCallback = callback;
        mProgressDialog = new SSProgressDialog(context);
    }

    private void addDevice() {
        final SSDeviceRepository ssDeviceRepo = new SSDeviceRepository(mContext);

        if (mSSDevice.isTrusted())
            ssDeviceRepo.publish(mSSDevice);

        final NetworkDeviceRepository netDeviceRepo = new NetworkDeviceRepository(mContext);
        NetworkDevice onlineDevice = netDeviceRepo.findDeviceDep(null, mSSDevice.getDeviceId(), null);

        if (onlineDevice != null) {
            Log.d(TAG, "Device online");
            Call<SSDevice> request = CommunicationHelper.requestDeviceInformation(mContext, onlineDevice);

            mProgressDialog.setMessage(R.string.text_onRequestDeviceInfo);

            if (!mProgressDialog.isShowing())
                mProgressDialog.show();

            request.enqueue(new Callback<SSDevice>() {
                @Override
                public void onResponse(Call<SSDevice> call, Response<SSDevice> response) {
                    SSDevice remoteDevice = response.body();

                    if (remoteDevice != null) {
                        Log.d(TAG, "Received remote device");
                        ssDeviceRepo.publish(remoteDevice);

                        sendDeviceInfo(onlineDevice);
                        return;
                    }

                    if (mProgressDialog.isShowing())
                        mProgressDialog.dismiss();

                    mCallback.onException(ERROR_BAD_RESPONSE);
                }

                @Override
                public void onFailure(Call<SSDevice> call, Throwable t) {
                    if (mProgressDialog.isShowing())
                        mProgressDialog.dismiss();

                    Log.w(TAG, "Device information request is failed. Reason: " + t.getMessage());

                    if (t instanceof SSLHandshakeException)
                        mCallback.onException(ERROR_HANDSHAKE_EXCEPTION);
                    else
                        mCallback.onException(ERROR_REQUEST_FAILED);
                }
            });

            return;
        }

        mCallback.onException(ERROR_DEVICE_OFFLINE);
    }

    private void sendDeviceInfo(final NetworkDevice onlineDevice) {
        Log.d(TAG, "Sending this device information");

        Call<SimpleServerResponse> request = CommunicationHelper.sendDeviceInformation(mContext, onlineDevice, AppUtils.getLocalDevice(mContext));

        mProgressDialog.setMessage(R.string.text_onSendingDeviceInfo);

        if (!mProgressDialog.isShowing())
            mProgressDialog.show();


        request.enqueue(new Callback<SimpleServerResponse>() {
            @Override
            public void onResponse(Call<SimpleServerResponse> call, Response<SimpleServerResponse> response) {
                if (mProgressDialog.isShowing())
                    mProgressDialog.dismiss();

                if (response.isSuccessful())
                    mCallback.onSuccessfullyAdded();
                else
                    mCallback.onException(ERROR_SENDING_FAILED);
            }

            @Override
            public void onFailure(Call<SimpleServerResponse> call, Throwable t) {
                if (mProgressDialog.isShowing())
                    mProgressDialog.dismiss();

                Log.w(TAG, "Sending device information is failed. Reason: " + t.getMessage());
                mCallback.onException(ERROR_SENDING_FAILED);
            }
        });
    }

    public void processDevice() {
        final SSDeviceRepository ssDeviceRepo = new SSDeviceRepository(mContext);
        SSDevice existentDevice = ssDeviceRepo.findDeviceDep(mSSDevice.getDeviceId());

        if (existentDevice != null) {
            Log.d(TAG, "Already added");
            mCallback.onException(ERROR_ALREADY_ADDED);
            return;
        }

        askLocalApproval();
    }

    private void askLocalApproval() {
        DialogInterface.OnClickListener btnUntrusted = new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                mCallback.onException(ERROR_UNTRUSTED_DEVICE);
            }
        };

        DialogInterface.OnClickListener btnTrusted = new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                mSSDevice.setTrusted(true);
                mSSDevice.setAccessAllowed(true);
                addDevice();
            }
        };

        new AlertDialog.Builder(mContext)
                .setTitle(R.string.title_trustQuestion)
                .setMessage(String.format(mContext.getString(R.string.text_trustQuestionHelp), mSSDevice.getDeviceId()))
                .setNegativeButton(R.string.btn_no, btnUntrusted)
                .setPositiveButton(R.string.btn_yes, btnTrusted)
                .show();
    }

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

    public interface AddDeviceCallback {
        void onSuccessfullyAdded();
        void onException(int errorCode);
    }

}
