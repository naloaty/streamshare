package com.naloaty.syncshare.adapter.custom;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.naloaty.syncshare.R;
import com.naloaty.syncshare.adapter.base.HeaderItem;

public class DefaultHeader extends HeaderItem {

    public DefaultHeader(int captionResource) {
        super(captionResource);
    }

    @Override
    public RecyclerView.ViewHolder getViewHolder(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.default_header, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void bindViewHolder(RecyclerView.ViewHolder viewHolder) {

        if (viewHolder instanceof DefaultHeader.ViewHolder) {
            DefaultHeader.ViewHolder holder = (DefaultHeader.ViewHolder) viewHolder;

            holder.headerCaption.setText(getCaptionResource());
        }

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView headerCaption;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            headerCaption = itemView.findViewById(R.id.default_header_text);
        }
    }
}
