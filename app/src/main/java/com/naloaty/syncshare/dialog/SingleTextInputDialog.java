package com.naloaty.syncshare.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import com.naloaty.syncshare.R;

public abstract class SingleTextInputDialog extends AlertDialog.Builder {

    private EditText mEditText;
    private ViewGroup mView;

    public SingleTextInputDialog(Context context)
    {
        super(context);

        mView = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.layout_dialog_single_text_input, null);
        mEditText = mView.findViewById(R.id.dialog_single_text_input_edit_text);

        setView(mView);
        setTitle(R.string.text_defaultValue);
        setNegativeButton(R.string.btn_close, null);

        mEditText.requestFocus();
    }

    public EditText getEditText()
    {
        return mEditText;
    }
}
