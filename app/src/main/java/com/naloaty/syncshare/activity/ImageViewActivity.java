package com.naloaty.syncshare.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.naloaty.syncshare.R;
import com.naloaty.syncshare.app.GlideApp;
import com.naloaty.syncshare.app.MediaActivity;
import com.naloaty.syncshare.communication.CommunicationHelper;
import com.naloaty.syncshare.database.device.NetworkDevice;
import com.naloaty.syncshare.media.ListHolder;
import com.naloaty.syncshare.media.Media;
import com.ortiz.touchview.TouchImageView;

import java.util.List;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class ImageViewActivity extends MediaActivity {

    private static final String TAG = "ImageViewActivity";

    public static final String EXTRA_LIST_HOLDER = "listHolder";
    public static final String EXTRA_POSITION = "position";

    private List<Media> mList;
    private ViewPager mViewPager;
    private ViewPagerAdapter mViewPagerAdapter;

    //private TextView mDateTaken;

    private int mSelectedPosition = 0;
    private NetworkDevice mNetworkDevice;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.activity_image_view);
        super.onCreate(savedInstanceState);

        mViewPager = findViewById(R.id.viewpager);

        if (getIntent() != null && getIntent().hasExtra(EXTRA_LIST_HOLDER) && getIntent().hasExtra(EXTRA_POSITION)) {

            ListHolder listHolder = (ListHolder) getIntent().getSerializableExtra(EXTRA_LIST_HOLDER);
            mList = listHolder.getMediaList();
            mNetworkDevice = listHolder.getNetworkDevice();
            mSelectedPosition = getIntent().getIntExtra(EXTRA_POSITION, 0);

            Log.d(TAG, String.format("Position: %s, Items count: %s", mSelectedPosition, mList.size()));
        }
        else
        {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.title_noImageSource)
                    .setMessage(R.string.text_noImageSource)
                    .setPositiveButton(R.string.btn_close, (dialog, which) -> onBackPressed())
                    .show();

            return;
        }

        mViewPagerAdapter = new ViewPagerAdapter();
        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        setCurrentItem(mSelectedPosition);
    }


    private void setCurrentItem(int position) {
        mViewPager.setCurrentItem(position, false);
        displayMediaInfo(mSelectedPosition);
    }

    //page change listener
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

    private void displayMediaInfo(int position) {
        setTitle(String.format("%s of %s", position + 1, mList.size()));
        Media media = mList.get(position);

        /*Date date = new Date(media.getDateTaken());
        TODO: make date format with month name
        DateFormat dateFormat = new SimpleDateFormat("dd.mm.yyyy", Locale.US);

        mDateTaken.setText(dateFormat.format(date));*/
    }



public class ViewPagerAdapter extends PagerAdapter {


    public ViewPagerAdapter() {
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        LayoutInflater layoutInflater = LayoutInflater.from(ImageViewActivity.this);
        View view = layoutInflater.inflate(R.layout.image_fullscreen, container, false);

        TouchImageView image = view.findViewById(R.id.image);
        image.setOnClickListener(v -> toggleSystemUI());

        LinearLayout playBtn = view.findViewById(R.id.play_btn);

        Media media = mList.get(position);

        if (media.getMediaType() == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO){
            image.setZoomEnabled(false);
            playBtn.setVisibility(View.VISIBLE);
            playBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String URL = CommunicationHelper.getServeRequestURL(mNetworkDevice) + media.getFilename();

                    Log.d(TAG, "Intent for video by URL -> " + URL);

                        /*Intent intent = new Intent(Intent.ACTION_VIEW).setDataAndType(Uri.parse(URL), media.getMimeType());
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(intent);*/

                    Intent intent = new Intent(ImageViewActivity.this, VideoPlayerActivity.class);
                    intent.putExtra(VideoPlayerActivity.EXTRA_VIDEO_SOURCE, URL);
                    startActivity(intent);
                }
            });
        }
        else
        {
            image.setZoomEnabled(true);
            playBtn.setVisibility(View.INVISIBLE);
        }


        CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(ImageViewActivity.this);
        circularProgressDrawable.setColorSchemeColors(ContextCompat.getColor(ImageViewActivity.this, R.color.colorAccent));
        circularProgressDrawable.setStrokeWidth(12f);
        circularProgressDrawable.setCenterRadius(90f);
        circularProgressDrawable.start();

        //TODO: detail lose while zooming
        /*
         * Try to use https://github.com/davemorrissey/subsampling-scale-image-view
         * But in that case file should be saved on disk.
         */

        RequestOptions options = new RequestOptions()
                .format(DecodeFormat.PREFER_ARGB_8888)
                .placeholder(circularProgressDrawable)
                .error(R.drawable.ic_warning_24dp)
                .diskCacheStrategy(DiskCacheStrategy.NONE);

        try
        {
            String URL = CommunicationHelper.getFullsizeImageRequestURL(mNetworkDevice) + media.getFilename();

            Log.d(TAG, "Media url: " + URL);

            DrawableCrossFadeFactory factory =
                    new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build();

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
    public boolean isViewFromObject(View view, Object obj) {
        return view == ((View) obj);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}
}
