package com.naloaty.streamshare.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.EditText;

import com.naloaty.streamshare.R;

/**
 * This class represents a dialog with a single text input field.
 * @see EnterDeviceIdDialog
 */
public abstract class SingleTextInputDialog extends ManualDismissDialog {

    private OnEnteredListener mListener;

    /* UI elements */
    private EditText mEditText;
    private ViewGroup mView;

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

    /**
     * Returns text input field.
     * @return Input field.
     */
    protected EditText getEditText() {
        return mEditText;
    }

    /**
     * Returns input listener.
     * @return Input listener.
     */
    protected OnEnteredListener getOnEnteredListener() {
        return mListener;
    }

    /**
     * Listener for input. Called when the action button is clicked.
     */
    public interface OnEnteredListener {
        void onEntered(String text);
    }
}
