package com.naloaty.syncshare.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.naloaty.syncshare.R;

/**
 * This class represents a dialog with progressbar and description.
 * Used when a long operation is performed.
 * @see com.naloaty.syncshare.app.SSActivity
 * @see com.naloaty.syncshare.util.AddDeviceHelper
 */
public class SSProgressDialog extends AlertDialog.Builder {

    /* UI elements */
    private AlertDialog mDialog;
    private ViewGroup mView;
    private TextView mTextView;

    public SSProgressDialog(@NonNull Context context) {
        super(context);

        mView = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.layout_dialog_ss_progress, null);
        mTextView = mView.findViewById(R.id.ss_progress_message);

        setCancelable(false);
        setView(mView);
    }

    /**
     * Sets the description of long operation.
     * @param messageId Description text resource.
     * @return This instance of {@link SSProgressDialog}.
     */
    @Override
    public SSProgressDialog setMessage(int messageId) {
        mTextView.setText(messageId);
        return this;
    }

    /**
     * Shows the dialog.
     * @return Showed dialog.
     */
    @Override
    public AlertDialog show() {
        mDialog = super.show();
        return mDialog;
    }

    /**
     * Returns an instance of the dialog.
     * @return Showing dialog.
     */
    public AlertDialog getDialog () {
        return mDialog;
    }

    /**
     * Returns true if the dialog is showing.
     * @return State of the dialog (showing or not).
     */
    public boolean isShowing() {
        if (mDialog != null)
            return mDialog.isShowing();
        else
            return false;
    }

    /**
     * Dismisses the showing dialog.
     */
    public void dismiss() {
        if (mDialog != null)
            mDialog.dismiss();
    }
}
