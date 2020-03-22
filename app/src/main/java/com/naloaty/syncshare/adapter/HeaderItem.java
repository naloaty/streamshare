package com.naloaty.syncshare.adapter;

public class HeaderItem extends ListItem {

    private int captionResource;

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