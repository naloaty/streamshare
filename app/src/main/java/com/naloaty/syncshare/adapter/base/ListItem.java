package com.naloaty.syncshare.adapter.base;

import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

public abstract class ListItem {

    public static final int TYPE_HEADER = 0;
    public static final int TYPE_BODYITEM = 1;

    protected boolean firstItem = false;

    public boolean isFirstItem() {
        return firstItem;
    }

    public void setFirstItem(Boolean isFirstItem) {
        this.firstItem = isFirstItem;
    }

    abstract public RecyclerView.ViewHolder getViewHolder(ViewGroup parent);

    abstract public void bindViewHolder(RecyclerView.ViewHolder viewHolder);

    abstract public int getType();
}
