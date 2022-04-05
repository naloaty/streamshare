package com.naloaty.streamshare.adapter;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.naloaty.streamshare.R;
import com.naloaty.streamshare.database.entity.Album;
import com.naloaty.streamshare.media.MediaObject;
import com.naloaty.streamshare.media.MediaProvider;

import java.util.ArrayList;
import java.util.List;

import static com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions.withCrossFade;

/**
 * Adapter class for RecyclerView that displays a list of albums on the local device
 * @see com.naloaty.streamshare.fragment.LocalAlbumsFragment
 */
public class LocalAlbumsAdapter extends RecyclerView.Adapter<LocalAlbumsAdapter.ViewHolder>{

    private static final String TAG = "LocalAlbumsAdapter";

    private List<Album> mList = new ArrayList<>();
    private OnRVClickListener mClickListener;

    public LocalAlbumsAdapter(OnRVClickListener clickListener){
        mClickListener = clickListener;
    }

    /**
     * Updates the current list of albums
     * @param albumsList New or updated albums list
     */
    public void setAlbumsList(@NonNull List<Album> albumsList) {
        /*
         * TODO: When adding item to RV it flashes during ViewMessage animation
         */
        if (albumsList.size() == 1 && mList.size() < 2){
            mList = albumsList;
            notifyDataSetChanged();
            return;
        }

        if (mList == null) {
            mList = albumsList;
            notifyItemRangeInserted(0, albumsList.size());
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
                    return albumsList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return mList.get(oldItemPosition).getId() ==
                            albumsList.get(newItemPosition).getId();
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    Album newAlbum = albumsList.get(newItemPosition);
                    Album oldAlbum = mList.get(oldItemPosition);

                    return newAlbum.getId() == oldAlbum.getId()
                            && newAlbum.getAlbumId() == oldAlbum.getAlbumId()
                            && TextUtils.equals(newAlbum.getName(), oldAlbum.getName())
                            && TextUtils.equals(newAlbum.getPath(), oldAlbum.getPath())
                            && TextUtils.equals(newAlbum.getLastItemFilename(), oldAlbum.getLastItemFilename())
                            && newAlbum.getItemsCount() == oldAlbum.getItemsCount();

                }
            });

            mList = albumsList;
            result.dispatchUpdatesTo(this);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.local_album, parent, false);
        return new ViewHolder(view, mClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Album album = mList.get(position);

        holder.albumName.setText(album.getName());
        holder.itemsCount.setText(String.valueOf(album.getItemsCount()));
        holder.accessAllowed.setChecked(album.isAccessAllowed());
        holder.album = album;

        RequestOptions options = new RequestOptions()
                .format(DecodeFormat.PREFER_ARGB_8888)
                .centerCrop()
                .placeholder(R.color.colorEmptyThumbnail)
                .error(R.drawable.ic_warning_24dp)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE);

        /* Catches exceptions that may be caused by missing album cover */
        try {
            DrawableCrossFadeFactory factory = new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build();

            MediaObject cover = MediaProvider.getMediaObjectById(holder.albumPreview.getContext(), album.getLastItemFilename());
            Log.d(TAG, "Album lastItemFilename: " + album.getLastItemFilename());

            Glide.with(holder.albumPreview.getContext())
                    .asBitmap()
                    .load(cover.getPath())
                    .apply(options)
                    .transition(withCrossFade(factory))
                    .into(holder.albumPreview);
        }
        catch (Exception e) {
            Log.d(TAG, "Cannot load album cover: " + e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ImageView albumPreview;
        TextView albumName;
        TextView itemsCount;
        Switch accessAllowed;

        OnRVClickListener clickListener;
        Album album;

        ViewHolder(@NonNull View itemView, OnRVClickListener clickListener) {
            super(itemView);
            albumPreview = itemView.findViewById(R.id.local_album_image);
            albumName = itemView.findViewById(R.id.local_album_name);
            itemsCount = itemView.findViewById(R.id.local_album_count);
            accessAllowed = itemView.findViewById(R.id.local_album_switch);

            this.clickListener = clickListener;
            accessAllowed.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            album.setAccessAllowed(accessAllowed.isChecked());
            clickListener.onClick(getAdapterPosition());
        }
    }
}
