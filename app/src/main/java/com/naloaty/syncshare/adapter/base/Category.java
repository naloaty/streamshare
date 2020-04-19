package com.naloaty.syncshare.adapter.base;

import java.util.ArrayList;
import java.util.List;

public class Category {

    private HeaderItem mHeader;
    private List<BodyItem> mItems = new ArrayList<>();

    public Category(HeaderItem header) {
        this.mHeader = header;
    }

    public void addItem(BodyItem item) {
        if (item != null){
            item.setCategory(this);
            mItems.add(item);
        }
    }

    public ListItem getHeader() {
        return mHeader;
    }

    public List<BodyItem> getItems() {
        return mItems;
    }

    public int getItemsCount() {
        return mItems.size();
    }
}
