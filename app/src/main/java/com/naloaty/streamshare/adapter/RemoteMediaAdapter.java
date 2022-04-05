package com.naloaty.streamshare.adapter;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.naloaty.streamshare.R;
import com.naloaty.streamshare.app.GlideApp;
import com.naloaty.streamshare.communication.CommunicationHelper;
import com.naloaty.streamshare.database.entity.NetworkDevice;
import com.naloaty.streamshare.media.Media;

import java.util.ArrayList;
import java.util.List;

import static com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions.withCrossFade;

/**
 * Adapter class for RecyclerView that displays a grid-list of media-files on the remote device.
 * @see com.naloaty.streamshare.fragment.RemoteMediaFragment
 */
public class RemoteMediaAdapter extends RecyclerView.Adapter<RemoteMediaAdapter.ViewHolder> {

    private static final String TAG = "RemoteMediaAdapter";

    private List<Media> mList = new ArrayList<>();
    private OnRVClickListener mClickListener;
    private NetworkDevice mNetworkDevice;

    public RemoteMediaAdapter(OnRVClickListener clickListener, NetworkDevice networkDevice) {
        mClickListener = clickListener;
        mNetworkDevice = networkDevice;
    }

    /**
     * Updates the current list of media-files
     * @param mediaList New or updated media-files list
     */
    public void setMediaList(@NonNull List<Media> mediaList) {
        /*
         * TODO: When adding item to RV it flashes during ViewMessage animation
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

                    return TextUtils.equals(newMedia.getFilename(), oldMedia.getFilename())
                            && TextUtils.equals(newMedia.getMimeType(), oldMedia.getMimeType())
                            && newMedia.getDateTaken() == oldMedia.getDateTaken()
                            && newMedia.getOrientation() == oldMedia.getOrientation()
                            && newMedia.getSize() == oldMedia.getSize();

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
                .placeholder(R.color.colorEmptyThumbnail)
                .error(R.drawable.ic_warning_24dp)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE);

        if (media.getMediaType() == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)
            holder.videoText.setVisibility(View.VISIBLE);
        else
            holder.videoText.setVisibility(View.INVISIBLE);

        String URL = CommunicationHelper.getThumbnailRequestURL(mNetworkDevice) + media.getFilename();
        DrawableCrossFadeFactory factory = new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build();

        Log.d(TAG, "Media url: " + URL);

        GlideApp.with(holder.thumbnail.getContext())
                .asBitmap()
                .load(URL)
                .apply(options)
                .transition(withCrossFade(factory))
                .into(holder.thumbnail);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ImageView thumbnail;
        CardView videoText;
        OnRVClickListener clickListener;

        ViewHolder(@NonNull View itemView, OnRVClickListener clickListener) {
            super(itemView);

            this.videoText = itemView.findViewById(R.id.text_video);
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
