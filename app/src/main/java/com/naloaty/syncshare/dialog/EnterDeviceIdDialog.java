package com.naloaty.syncshare.dialog;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;

import com.naloaty.syncshare.R;

/**
 * This class represents a dialog for manually entering a device identifier.
 * @see com.naloaty.syncshare.activity.AddDeviceActivity
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