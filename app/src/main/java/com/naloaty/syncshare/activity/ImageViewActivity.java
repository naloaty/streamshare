package com.naloaty.syncshare.activity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.naloaty.syncshare.R;
import com.naloaty.syncshare.app.GlideApp;
import com.naloaty.syncshare.app.MediaActivity;
import com.naloaty.syncshare.communication.CommunicationHelper;
import com.naloaty.syncshare.database.device.NetworkDevice;
import com.naloaty.syncshare.media.ListHolder;
import com.naloaty.syncshare.media.Media;
import com.ortiz.touchview.TouchImageView;

import java.util.ArrayList;
import java.util.List;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/**
 * This activity loads and displays the selected media file in full-resolution.
 *
 * Related fragments:
 * @see com.naloaty.syncshare.fragment.RemoteMediaFragment
 */
public class ImageViewActivity extends MediaActivity {

    private static final String TAG = "ImageViewActivity";

    public static final String EXTRA_LIST_HOLDER = "listHolder";
    public static final String EXTRA_POSITION = "position";

    private List<Media> mList = new ArrayList<>();
    private int mSelectedPosition = 0;
    private NetworkDevice mNetworkDevice;
    private ViewPagerAdapter mViewPagerAdapter;


    /* UI elements */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        //TODO: rid of MediaActivity

        //Important to call this BEFORE super.onCreate();
        setContentView(R.layout.activity_image_view);

        super.onCreate(savedInstanceState);
        mViewPager = findViewById(R.id.viewpager);

        //TODO: need to pass arguments by other way
        if (getIntent() != null
                && getIntent().hasExtra(EXTRA_LIST_HOLDER)
                && getIntent().hasExtra(EXTRA_POSITION)) {

            ListHolder listHolder = (ListHolder) getIntent().getSerializableExtra(EXTRA_LIST_HOLDER);

            if (listHolder == null) {
                onSourceError();
                return;
            }

            mList = listHolder.getMediaList();
            mNetworkDevice = listHolder.getNetworkDevice();
            mSelectedPosition = getIntent().getIntExtra(EXTRA_POSITION, 0);
        }
        else
        {
            onSourceError();
            return;
        }

        mViewPagerAdapter = new ViewPagerAdapter();
        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        setCurrentItem(mSelectedPosition);
    }

    /**
     * When there is no image source, this method is used to display an error message.
     */
    private void onSourceError() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.title_noImageSource)
                .setMessage(R.string.text_noImageSource)
                .setPositiveButton(R.string.btn_close, (dialog, which) -> onBackPressed())
                .show();
    }

    /**
     * Sets the index of the image to be displayed.
     */
    private void setCurrentItem(int position) {
        mViewPager.setCurrentItem(position, false);
        displayMediaInfo(mSelectedPosition);
    }

    /**
     * Page change listener
     */
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            displayMediaInfo(position);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    /**
     * TODO: add additional information about image such as date, name, etc
     * Displays the position of the image in the media list.
     */
    private void displayMediaInfo(int position) {
        setTitle(String.format(getString(R.string.text_imagePosition), position + 1, mList.size()));
    }


    /**
     * This class controls the viewpager.
     * Its objective is to request and display the required image.
     */
    public class ViewPagerAdapter extends PagerAdapter {

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            LayoutInflater layoutInflater = LayoutInflater.from(ImageViewActivity.this);

            View view = layoutInflater.inflate(R.layout.image_fullscreen, container, false);
            LinearLayout playBtn = view.findViewById(R.id.play_btn);

            TouchImageView image = view.findViewById(R.id.image);
            image.setOnClickListener(v -> toggleSystemUI());
            image.setZoomEnabled(false);

            Media media = mList.get(position);

            if (media.getMediaType() == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO){
                playBtn.setVisibility(View.VISIBLE);
                playBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //TODO: refactor NetworkDevice system
                        Intent intent = new Intent(ImageViewActivity.this, VideoPlayerActivity.class);
                        intent.putExtra(VideoPlayerActivity.EXTRA_REMOTE_URL, CommunicationHelper.getServeRequestURL(mNetworkDevice));
                        intent.putExtra(VideoPlayerActivity.EXTRA_VIDEO_INFO, media);
                        startActivity(intent);
                    }
                });
            }
            else
            {
                image.setZoomEnabled(true);
                playBtn.setVisibility(View.INVISIBLE);
            }

            //TODO: CircularProgress size should be dynamically calculated
            CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(ImageViewActivity.this);
            circularProgressDrawable.setColorSchemeColors(ContextCompat.getColor(ImageViewActivity.this, R.color.colorAccent));
            circularProgressDrawable.setStrokeWidth(12f);
            circularProgressDrawable.setCenterRadius(90f);
            circularProgressDrawable.start();

            RequestOptions options = new RequestOptions()
                    .format(DecodeFormat.PREFER_ARGB_8888)
                    .placeholder(circularProgressDrawable)
                    .error(R.drawable.ic_warning_24dp)
                    .diskCacheStrategy(DiskCacheStrategy.NONE);

            try
            {
                String URL = CommunicationHelper.getFullsizeImageRequestURL(mNetworkDevice) + media.getFilename();
                DrawableCrossFadeFactory factory = new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build();

                GlideApp.with(ImageViewActivity.this)
                        .load(URL)
                        .apply(options)
                        .transition(withCrossFade(factory))
                        .into(image);

            }
            catch (Exception e) {
                Log.d(TAG, "Cannot load image: " + e.getMessage());
            }

            container.addView(view);
            return view;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object obj) {
            return view == ((View) obj);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }
}
