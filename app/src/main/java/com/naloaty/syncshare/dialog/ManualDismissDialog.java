package com.naloaty.syncshare.dialog;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

public abstract class ManualDismissDialog extends AlertDialog.Builder {

    private OnClickListener mPositiveListener;

    public ManualDismissDialog(@NonNull Context context) {
        super(context);
    }

    //All control over dialog dismiss in hands of ManualDismissDialog.OnClickListener();
    @Override
    public AlertDialog show()
    {
        AlertDialog dialog = super.show();

        if (mPositiveListener != null)
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (mPositiveListener.onClick(dialog))
                        dialog.dismiss();
                }
            });

        return dialog;
    }

    public ManualDismissDialog setPositiveButton(int textId, ManualDismissDialog.OnClickListener listener) {

        mPositiveListener = listener;

        //We set click listener in show();
        super.setPositiveButton(textId, null);

        return this;
    }

    public interface OnClickListener {
        boolean onClick(AlertDialog dialog);
    }
}
