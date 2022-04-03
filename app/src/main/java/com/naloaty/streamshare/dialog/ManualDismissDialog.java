package com.naloaty.streamshare.dialog;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

/**
 * This class represents a dialog that can only be dismissed by code.
 * Dialogs by default automatically dismisses when you click action buttons.
 * @see MyDeviceDetailsDialog
 * @see SingleTextInputDialog
 */
public abstract class ManualDismissDialog extends AlertDialog.Builder {

    private OnClickListener mPositiveListener;
    private OnClickListener mNeutralListener;
    private OnClickListener mNegativeListener;

    public ManualDismissDialog(@NonNull Context context) {
        super(context);
    }

    /**
     * Shows the dialog.
     * NOTE: Control over dialog dismissing is in hands of ManualDismissDialog.OnClickListener();
     * @return Showed dialog.
     */
    @Override
    public AlertDialog show()
    {
        AlertDialog dialog = super.show();

        if (mPositiveListener != null)
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                if (mPositiveListener.onClick(dialog))
                    dialog.dismiss();
            });

        if (mNeutralListener != null)
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> {
                if (mNeutralListener.onClick(dialog))
                    dialog.dismiss();
            });

        if (mNegativeListener != null)
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> {
                if (mNegativeListener.onClick(dialog))
                    dialog.dismiss();
            });

        return dialog;
    }

    /**
     * Setups positive action button.
     * @param textId Button text resource.
     * @param listener Button click listener. Instance of {@link ManualDismissDialog.OnClickListener}.
     */
    public ManualDismissDialog setPositiveButtonS(int textId, ManualDismissDialog.OnClickListener listener) {

        mPositiveListener = listener;

        //We set click listener in show();
        super.setPositiveButton(textId, null);

        return this;
    }

    /**
     * Setups neutral action button.
     * @param textId Button text resource.
     * @param listener Button click listener. Instance of {@link ManualDismissDialog.OnClickListener}.
     */
    public ManualDismissDialog setNeutralButtonS(int textId, ManualDismissDialog.OnClickListener listener) {
        mNeutralListener = listener;

        //We set click listener in show();
        super.setPositiveButton(textId, null);

        return this;
    }

    /**
     * Setups negative action button.
     * @param textId Button text resource.
     * @param listener Button click listener. Instance of {@link ManualDismissDialog.OnClickListener}.
     */
    public ManualDismissDialog setNegativeButtonS(int textId, ManualDismissDialog.OnClickListener listener) {
        mNegativeListener = listener;

        //We set click listener in show();
        super.setNegativeButton(textId, null);

        return this;
    }

    /**
     * Action button click listener.
     */
    public interface OnClickListener {
        boolean onClick(AlertDialog dialog);
    }
}
