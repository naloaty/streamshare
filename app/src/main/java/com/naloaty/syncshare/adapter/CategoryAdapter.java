package com.naloaty.syncshare.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.naloaty.syncshare.adapter.base.BodyItem;
import com.naloaty.syncshare.adapter.base.Category;
import com.naloaty.syncshare.adapter.base.ListItem;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private List<ListItem> mItems = new ArrayList<>();

    public void setItems(List<Category> items) {
        mItems.clear();

        for (Category category: items) {
            if (category.getHeader() != null)
                mItems.add(category.getHeader());

            mItems.addAll(category.getItems());
        }

        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return mItems.get(viewType).getViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        mItems.get(position).bindViewHolder(viewHolder);
    }

    @Override
    public int getItemViewType(int position) {

        //Every item can be unique, so it is itself view type
        return position;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }


}
