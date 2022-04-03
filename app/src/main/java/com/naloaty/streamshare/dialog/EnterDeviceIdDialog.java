package com.naloaty.streamshare.dialog;

import android.content.Context;

import com.naloaty.streamshare.R;

/**
 * This class represents a dialog for manually entering a device identifier.
 * @see com.naloaty.streamshare.activity.AddDeviceActivity
 */
public class EnterDeviceIdDialog extends SingleTextInputDialog {

    public EnterDeviceIdDialog(Context context, OnEnteredListener listener) {
        super(context, listener);

        setTitle(R.string.title_enterDeviceId);

        setPositiveButtonS(R.string.btn_add, dialog -> {
            /*
             * Checks if the entered device identifier matches the pattern (should be nine groups)
             */
            String deviceID = getEditText().getText().toString();
            int count = deviceID.split("-").length;

            if (count == 9) {
                getOnEnteredListener().onEntered(deviceID);
                return true;
            }
            else {
                getEditText().setError(context.getString(R.string.msg_wrongDeviceId));
                return false;
            }
        });
    }
}