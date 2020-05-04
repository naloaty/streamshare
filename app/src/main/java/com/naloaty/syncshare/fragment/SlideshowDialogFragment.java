package com.naloaty.syncshare.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.naloaty.syncshare.R;
import com.naloaty.syncshare.communication.CommunicationHelper;
import com.naloaty.syncshare.database.device.NetworkDevice;
import com.naloaty.syncshare.media.ListHolder;
import com.naloaty.syncshare.media.Media;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SlideshowDialogFragment extends DialogFragment {

    private static final String TAG = "SlideshowDialogFragment";

    public static final String EXTRA_LIST_HOLDER = "listHolder";
    public static final String EXTRA_POSITION = "position";

    private List<Media> mList;
    private ViewPager mViewPager;
    private ViewPagerAdapter mViewPagerAdapter;

    private TextView mMediaCount;
    private ImageView mBackButton;
    //private TextView mDateTaken;

    private int mSelectedPosition = 0;

    private NetworkDevice mNetworkDevice;

    static SlideshowDialogFragment newInstance() {
        SlideshowDialogFragment fragment = new SlideshowDialogFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.layout_image_slider_fragment, container, false);
        mViewPager = view.findViewById(R.id.viewpager);

        mMediaCount = view.findViewById(R.id.slider_media_count);
        mBackButton = view.findViewById(R.id.back_button);

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        Bundle bundle = getArguments();

        if (bundle != null) {

            ListHolder listHolder = (ListHolder) bundle.getSerializable(EXTRA_LIST_HOLDER);
            mList = listHolder.getMediaList();
            mNetworkDevice = listHolder.getNetworkDevice();
            mSelectedPosition = bundle.getInt(EXTRA_POSITION);

            Log.d(TAG, String.format("Position: %s, Items count: %s", mSelectedPosition, mList.size()));
        }
        else
        {
            onInternalError("NULL_BUNDLE");
            return null;
        }



        mViewPagerAdapter = new ViewPagerAdapter();
        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        setCurrentItem(mSelectedPosition);

        return view;
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
        mMediaCount.setText(String.format("%s of %s", position + 1, mList.size()));

        Media media = mList.get(position);

        /*Date date = new Date(media.getDateTaken());
        TODO: make date format with month name
        DateFormat dateFormat = new SimpleDateFormat("dd.mm.yyyy", Locale.US);

        mDateTaken.setText(dateFormat.format(date));*/
    }

    private void onInternalError(String errorCode) {

        Log.d(TAG, "onInternalError(): " + errorCode);

        new AlertDialog.Builder(getContext())
                .setTitle(R.string.title_error)
                .setCancelable(false)
                .setMessage(String.format(getString(R.string.text_internalAppError), errorCode))
                .setPositiveButton(R.string.btn_close, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        getActivity().onBackPressed();
                    }
                }).show();
    }

    public class ViewPagerAdapter extends PagerAdapter {

        private LayoutInflater layoutInflater;

        public ViewPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            layoutInflater = LayoutInflater.from(getContext());
            View view = layoutInflater.inflate(R.layout.image_fullscreen, container, false);

            ImageView image = view.findViewById(R.id.image);
            LinearLayout playBtn = view.findViewById(R.id.play_btn);


            Media media = mList.get(position);

            CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(getContext());
            circularProgressDrawable.setTint(getResources().getColor(R.color.colorAccent));
            circularProgressDrawable.setStrokeWidth(5f);
            circularProgressDrawable.setCenterRadius(30f);
            circularProgressDrawable.start();


            RequestOptions options = new RequestOptions()
                    .format(DecodeFormat.PREFER_ARGB_8888)
                    .placeholder(circularProgressDrawable)
                    .error(R.drawable.ic_warning_24dp)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE);

            if (media.getMediaType() == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO){
                playBtn.setVisibility(View.VISIBLE);
                playBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String URL = CommunicationHelper.getServeRequestURL(mNetworkDevice) + media.getFilename();

                        Log.d(TAG, "Intent for video by URL -> " + URL);

                        Intent intent = new Intent(Intent.ACTION_VIEW).setDataAndType(Uri.parse(URL), media.getMimeType());
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(intent);
                    }
                });
            }
            else
                playBtn.setVisibility(View.INVISIBLE);

            try
            {
                String URL = CommunicationHelper.getFullsizeImageRequestURL(mNetworkDevice) + media.getFilename();

                Log.d(TAG, "Media url: " + URL);

                Glide.with(getContext())
                        .load(URL)
                        .apply(options)
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
