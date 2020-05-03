package com.naloaty.syncshare.adapter;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.naloaty.syncshare.R;
import com.naloaty.syncshare.communication.CommunicationHelper;
import com.naloaty.syncshare.database.device.NetworkDevice;
import com.naloaty.syncshare.media.Media;

import java.util.ArrayList;
import java.util.List;

public class RemoteMediaAdapter extends RecyclerView.Adapter<RemoteMediaAdapter.ViewHolder> {

    private static final String TAG = "RemoteMediaAdapter";

    private List<Media> mList = new ArrayList<>();
    private OnRVClickListener mClickListener;
    private NetworkDevice mNetworkDevice;

    public RemoteMediaAdapter(OnRVClickListener clickListener, NetworkDevice networkDevice) {
        mClickListener = clickListener;
        mNetworkDevice = networkDevice;
    }

    public void setMediaList(List<Media> mediaList) {

        /*
         * TODO: when adding item to RV it flashes during message animation
         */
        if (mediaList.size() == 1 && mList.size() < 2){
            mList = mediaList;
            notifyDataSetChanged();
            return;
        }

        if (mList == null) {
            mList = mediaList;
            notifyItemRangeInserted(0, mediaList.size());
        }
        else
        {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return mList.size();
                }

                @Override
                public int getNewListSize() {
                    return mediaList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return mList.get(oldItemPosition) == mediaList.get(newItemPosition);
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    Media newMedia = mediaList.get(newItemPosition);
                    Media oldMedia = mList.get(oldItemPosition);

                    return TextUtils.equals(newMedia.getFilename(), oldMedia.getFilename());

                }
            });

            mList = mediaList;
            result.dispatchUpdatesTo(this);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_thumbnail, parent, false);

        return new ViewHolder(itemView, mClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Media media = mList.get(position);

        RequestOptions options = new RequestOptions()
                .format(DecodeFormat.PREFER_ARGB_8888)
                .centerCrop()
                .placeholder(R.color.colorBlueLight)
                .error(R.color.colorOffline)
                //.animate(R.anim.fade_in)//TODO:DONT WORK WELL
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE);

        try
        {
            String URL = CommunicationHelper.getThumbnailRequestURL(mNetworkDevice) + media.getFilename();

            Log.d(TAG, "Media url: " + URL);

            Glide.with(holder.thumbnail.getContext())
                    .asBitmap()
                    .load(URL)
                    .apply(options)
                    .into(holder.thumbnail);

        }
        catch (Exception e) {
            Log.d(TAG, "Cannot load thumbnail: " + e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ImageView thumbnail;
        OnRVClickListener clickListener;

        public ViewHolder(@NonNull View itemView, OnRVClickListener clickListener) {
            super(itemView);

            this.thumbnail = itemView.findViewById(R.id.thumbnail);
            this.clickListener = clickListener;

            thumbnail.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            clickListener.onClick(getAdapterPosition());
        }
    }

}
