package com.naloaty.syncshare.dialog;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

public abstract class ManualDismissDialog extends AlertDialog.Builder {

    private OnClickListener mPositiveListener;
    private OnClickListener mNeutralListener;
    private OnClickListener mNegativeListener;

    public ManualDismissDialog(@NonNull Context context) {
        super(context);
    }

    //Control over dialog dismissing is in hands of ManualDismissDialog.OnClickListener();
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

        if (mNeutralListener != null)
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (mNeutralListener.onClick(dialog))
                        dialog.dismiss();
                }
            });

        if (mNegativeListener != null)
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (mNegativeListener.onClick(dialog))
                        dialog.dismiss();
                }
            });

        return dialog;
    }

    public ManualDismissDialog setPositiveButtonS(int textId, ManualDismissDialog.OnClickListener listener) {

        mPositiveListener = listener;

        //We set click listener in show();
        super.setPositiveButton(textId, null);

        return this;
    }

    public ManualDismissDialog setNeutralButtonS(int textId, ManualDismissDialog.OnClickListener listener) {
        mNeutralListener = listener;

        //We set click listener in show();
        super.setPositiveButton(textId, null);

        return this;
    }

    public ManualDismissDialog setNegativeButtonS(int textId, ManualDismissDialog.OnClickListener listener) {
        mNegativeListener = listener;

        //We set click listener in show();
        super.setNegativeButton(textId, null);

        return this;
    }

    public interface OnClickListener {
        boolean onClick(AlertDialog dialog);
    }
}
