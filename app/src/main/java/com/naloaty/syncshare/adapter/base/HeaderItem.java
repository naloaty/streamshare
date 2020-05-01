package com.naloaty.syncshare.adapter.base;

import androidx.recyclerview.widget.RecyclerView;

public abstract class HeaderItem extends ListItem {

    private int captionResource;

    private boolean applySpacing;

    public void setApplySpacing(boolean applySpacing) {
        this.applySpacing = applySpacing;
    }

    public boolean isApplySpacing() {
        return applySpacing;
    }

    public HeaderItem(int captionRes) {
        this.captionResource = captionRes;
    }

    public int getCaptionResource() {
        return captionResource;
    }

    @Override
    public int getType() {
        return TYPE_HEADER;
    }
}