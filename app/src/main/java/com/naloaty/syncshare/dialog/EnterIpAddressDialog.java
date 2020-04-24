package com.naloaty.syncshare.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.naloaty.syncshare.R;

public class EnterIpAddressDialog extends SingleTextInputDialog{

    public EnterIpAddressDialog(Context context, OnEnteredListener listener) {
        super(context, listener);

        setTitle(R.string.title_enterIpAdress);

        setPositiveButton(R.string.btn_connect, new ManualDismissDialog.OnClickListener() {

            @Override
            public boolean onClick(AlertDialog dialog) {
                String ipAddress = getEditText().getText().toString();

                if (ipAddress.matches("([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3})")) {
                    getOnEnteredListener().onEntered(ipAddress);
                    return true;
                }
                else {
                    getEditText().setError(context.getString(R.string.msg_wrongIpAddressFormat));
                    return false;
                }

            }
        });
    }




}
