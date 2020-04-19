package com.naloaty.syncshare.adapter.base;

public abstract class BodyItem extends ListItem {

    private Category category;

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    @Override
    public int getType() {
        return TYPE_BODYITEM;
    }
}
