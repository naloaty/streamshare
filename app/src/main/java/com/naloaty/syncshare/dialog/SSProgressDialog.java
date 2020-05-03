package com.naloaty.syncshare.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.naloaty.syncshare.R;

public class SSProgressDialog extends AlertDialog.Builder {

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

    @Override
    public SSProgressDialog setMessage(int messageId) {

        mTextView.setText(messageId);
        return this;
    }

    @Override
    public AlertDialog show() {
        mDialog = super.show();
        return mDialog;
    }

    public AlertDialog getDialog () {
        return mDialog;
    }

    public boolean isShowing() {
        if (mDialog != null)
            return mDialog.isShowing();
        else
            return false;
    }

    public void dismiss() {
        if (mDialog != null)
            mDialog.dismiss();
    }
}
