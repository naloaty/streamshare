package com.naloaty.syncshare.adapter.custom;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.naloaty.syncshare.R;
import com.naloaty.syncshare.adapter.base.BodyItem;

public class ActionMessage extends BodyItem {

    private int iconResource;
    private int messageResource;
    private int buttonResource;
    private View.OnClickListener actionClickListener;

    public ActionMessage(int iconResource, int messageResource, int btnResource, View.OnClickListener buttonListener) {
        this.iconResource = iconResource;
        this.messageResource = messageResource;
        this.actionClickListener = buttonListener;
        this.buttonResource = btnResource;
    }


    public int getIconResource() {
        return iconResource;
    }

    public int getMessageResource() {
        return messageResource;
    }

    public View.OnClickListener getActionClickListener() {
        return actionClickListener;
    }

    public int getButtonResource() {
        return buttonResource;
    }

    @Override
    public RecyclerView.ViewHolder getViewHolder(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.action_message, parent, false);

        return new ActionMessage.ViewHolder(view);
    }

    @Override
    public void bindViewHolder(RecyclerView.ViewHolder viewHolder) {
        if (viewHolder instanceof ActionMessage.ViewHolder) {
            ActionMessage.ViewHolder holder = (ActionMessage.ViewHolder) viewHolder;

            holder.messageIcon.setImageResource(getIconResource());
            holder.messageText.setText(getMessageResource());
            holder.actionButton.setText(getButtonResource());
            holder.actionButton.setOnClickListener(getActionClickListener());
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView messageIcon;
        TextView messageText;
        AppCompatButton actionButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            messageIcon = itemView.findViewById(R.id.action_message_icon);
            messageText = itemView.findViewById(R.id.action_message_text);
            actionButton = itemView.findViewById(R.id.action_message_btn);
        }

    }
}
