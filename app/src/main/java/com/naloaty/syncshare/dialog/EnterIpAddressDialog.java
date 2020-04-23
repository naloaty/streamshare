package com.naloaty.syncshare.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import com.naloaty.syncshare.R;

public class EnterIpAddressDialog extends SingleTextInputDialog{

    public EnterIpAddressDialog(Context context) {
        super(context);

        setTitle(R.string.title_enterIpAdress);

        setPositiveButton(R.string.btn_connect, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onConnectClick(context);
            }
        });
    }

    private void onConnectClick(Context context) {
        final String ipAddress = getEditText().getText().toString();

        if (ipAddress.matches("([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3})")) {
            Toast.makeText(context, "IP Address Correct!", Toast.LENGTH_SHORT).show();
        } else
            getEditText().setError(context.getString(R.string.msg_wrongIpAddressFormat));
    }


}
