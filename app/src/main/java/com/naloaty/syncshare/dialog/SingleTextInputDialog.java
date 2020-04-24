package com.naloaty.syncshare.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.EditText;

import com.naloaty.syncshare.R;

public abstract class SingleTextInputDialog extends ManualDismissDialog {

    private EditText mEditText;
    private ViewGroup mView;
    private OnEnteredListener mListener;

    public SingleTextInputDialog(Context context,OnEnteredListener listener)
    {
        super(context);

        mView = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.layout_dialog_single_text_input, null);
        mEditText = mView.findViewById(R.id.dialog_single_text_input_edit_text);
        mListener = listener;

        setView(mView);
        setTitle(R.string.text_defaultValue);
        setNegativeButton(R.string.btn_close, null);

        mEditText.requestFocus();
    }

    protected EditText getEditText()
    {
        return mEditText;
    }

    protected OnEnteredListener getOnEnteredListener() {
        return mListener;
    }

    public interface OnEnteredListener {
        void onEntered(String text);
    }
}
