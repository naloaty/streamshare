package com.naloaty.syncshare.adapter.base;

import com.naloaty.syncshare.adapter.CategoryAdapter;

public abstract class BodyItem extends ListItem {

    private Category category;
    private OnItemClickListener onItemClickListener;

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    @Override
    public int getType() {
        return TYPE_BODYITEM;
    }

    public interface OnItemClickListener {
        void onItemClick(BodyItem item);
    }
}
