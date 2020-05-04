package com.naloaty.syncshare.fragment;

import androidx.fragment.app.Fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.naloaty.syncshare.R;
import com.naloaty.syncshare.activity.RemoteViewActivity;
import com.naloaty.syncshare.adapter.OnRVClickListener;
import com.naloaty.syncshare.adapter.RemoteMediaAdapter;
import com.naloaty.syncshare.communication.CommunicationHelper;
import com.naloaty.syncshare.database.device.NetworkDevice;
import com.naloaty.syncshare.database.device.NetworkDeviceViewModel;
import com.naloaty.syncshare.database.device.SSDevice;
import com.naloaty.syncshare.database.device.SSDeviceViewModel;
import com.naloaty.syncshare.database.media.Album;
import com.naloaty.syncshare.media.ListHolder;
import com.naloaty.syncshare.media.Media;

import java.util.List;

import javax.net.ssl.SSLHandshakeException;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RemoteMediaFragment extends Fragment {

    private static final String TAG = "RemoteMediaFragment";

    private List<Media> mList;
    private RecyclerView mRecyclerView;
    private RemoteMediaAdapter mRVAdapter;

    private SSDeviceViewModel ssDeviceVM;
    private NetworkDeviceViewModel netDeviceVM;

    private CompositeDisposable disposables;

    private NetworkDevice mNetworkDevice;
    private SSDevice mSSDevice;

    private String deviceId;
    private Album mAlbum;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        disposables = new CompositeDisposable();

        ssDeviceVM = new ViewModelProvider(this).get(SSDeviceViewModel.class);
        netDeviceVM = new ViewModelProvider(this).get(NetworkDeviceViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.layout_remote_media_fragment, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView = view.findViewById(R.id.remote_media_recycler_view);
        initMessage(view.findViewById(R.id.message_placeholder));

        Bundle bundle = getArguments();

        if (bundle != null){
            deviceId = bundle.getString(RemoteViewActivity.EXTRA_DEVICE_ID);

            Gson gson = new Gson();
            mAlbum = gson.fromJson(bundle.getString(RemoteViewActivity.EXTRA_ALBUM), Album.class);
        }
        else
        {
            onInternalError("EMPTY_BUNDLE");
            return;
        }

        initDevice();
    }

    @Override
    public void onStop() {
        super.onStop();
        disposables.clear();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposables.dispose();
    }

    private void setupRecyclerView() {

        Log.d(TAG, "setupRecyclerView()");

        OnRVClickListener clickListener = new OnRVClickListener() {
            @Override
            public void onClick(int itemIndex) {
                Bundle bundle = new Bundle();
                bundle.putInt(SlideshowDialogFragment.EXTRA_POSITION, itemIndex);

                ListHolder listHolder = new ListHolder(mList, mNetworkDevice);
                bundle.putSerializable(SlideshowDialogFragment.EXTRA_LIST_HOLDER, listHolder);

                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                SlideshowDialogFragment newFragment = SlideshowDialogFragment.newInstance();

                newFragment.setArguments(bundle);
                newFragment.show(ft, "slideshow");
            }
        };

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
        mRVAdapter = new RemoteMediaAdapter(clickListener, mNetworkDevice);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mRVAdapter);
    }


    private void initDevice() {
        Log.d(TAG, "initDevice()");

        setUIState(UIState.LoadingMedia);

        Single<NetworkDevice> netDeviceObs = netDeviceVM.findDevice(null, deviceId, null);

        disposables.add(netDeviceObs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(new DisposableSingleObserver<NetworkDevice>() {
                    @Override
                    public void onSuccess(NetworkDevice networkDevice) {
                        Log.d(TAG, "initDevice(): success");
                        mNetworkDevice = networkDevice;
                        setupRecyclerView();
                        initSSDevice();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "initDevice(): error -> " + e);
                        setUIState(UIState.CannotLoadMedia);
                        new AlertDialog.Builder(getContext())
                                .setTitle(R.string.title_deviceOffline)
                                .setMessage(R.string.text_deviceOffline)
                                .setCancelable(false)
                                .setPositiveButton(R.string.btn_close, new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        getActivity().onBackPressed();
                                    }
                                }).show();
                    }
                }));


    }

    private void initSSDevice() {

        Log.d(TAG, "initSSDevice()");
        /*mProgressDialog.setMessage(R.string.progress_loadingDevice);

        if (!mProgressDialog.isShowing()){
            mProgressDialog.show();
        }*/

        Single<SSDevice> ssDeviceObs = ssDeviceVM.findDevice(deviceId);

        disposables.add(ssDeviceObs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(new DisposableSingleObserver<SSDevice>() {
                    @Override
                    public void onSuccess(SSDevice ssDevice) {
                        Log.d(TAG, "initSSDevice(): success");
                        mSSDevice = ssDevice;
                        requestMediaList();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "initSSDevice(): error -> " + e);
                        setUIState(UIState.CannotLoadMedia);
                        onInternalError("DEVICE_NOT_FOUND");
                    }
                }));
    }

    public void requestMediaList() {
        Log.d(TAG, "requestMediaList()");

        Call<List<Media>> request = CommunicationHelper.requestMediaList(getContext(), mNetworkDevice, mAlbum);
        request.enqueue(new Callback<List<Media>>() {
            @Override
            public void onResponse(Call<List<Media>> call, Response<List<Media>> response) {
                //setUIState(UIState.MediaShown);

                Log.d(TAG, "requestMediaList(): received response");
                if (response.body() != null && response.body().size() > 0) {
                    mList = response.body();
                    mRVAdapter.setMediaList(response.body());
                    setUIState(UIState.MediaShown);
                    Log.d(TAG, "requestMediaList(): body has items. Message -> " + response.message());
                }
                else
                {
                    Log.d(TAG, "requestMediaList(): body null or do not have items. Message -> " + response.message());
                    setUIState(UIState.NoMediaFound);
                }
            }

            @Override
            public void onFailure(Call<List<Media>> call, Throwable t) {

                Log.d(TAG, "requestMediaList(): failure ->  " + t);
                setUIState(UIState.CannotLoadMedia);

                if (t instanceof SSLHandshakeException) {
                    new AlertDialog.Builder(getContext())
                            .setTitle(R.string.title_securityException)
                            .setMessage(String.format(getString(R.string.text_securityException), mSSDevice.getNickname()))
                            .setCancelable(false)
                            .setPositiveButton(R.string.btn_close, new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    getActivity().onBackPressed();
                                }
                            }).show();
                }
                else
                    onInternalError("MEDIA_REQUEST_FAILURE");
            }
        });
    }

    private void onInternalError(String errorCode) {

        Log.d(TAG, "onInternalError(): " + errorCode);
        setUIState(UIState.CannotLoadMedia);

        new AlertDialog.Builder(getContext())
                .setTitle(R.string.title_error)
                .setMessage(String.format(getString(R.string.text_internalAppError), errorCode))
                .setCancelable(false)
                .setPositiveButton(R.string.btn_close, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        getActivity().onBackPressed();
                    }
                }).show();
    }

    /*
     * UI State Machine
     */

    /* Message view */
    private ImageView mMessageIcon;
    private TextView mMessageText;
    private ProgressBar mMessageProgressBar;
    private ViewGroup mMessageHolder;

    private UIState currentUIState;

    private void initMessage(ViewGroup messageHolder) {
        mMessageIcon = messageHolder.findViewById(R.id.message_icon);
        mMessageText = messageHolder.findViewById(R.id.message_text);
        mMessageProgressBar = messageHolder.findViewById(R.id.message_progress);
        mMessageHolder = messageHolder;
    }

    private enum UIState {
        MediaShown,
        LoadingMedia,
        NoMediaFound,
        CannotLoadMedia,
    }

    private void setUIState(UIState state)
    {
        if (currentUIState == state)
            return;

        switch (state) {
            case MediaShown:
                toggleMessage(false);
                break;

            case LoadingMedia:
                replaceMessage(R.string.text_loadingMedia);
                toggleMessage(true);
                break;

            case NoMediaFound:
                setMessage(R.drawable.ic_image_24dp, R.string.text_noMediaFound);
                toggleMessage(true);
                break;

            case CannotLoadMedia:
                setMessage(R.drawable.ic_warning_24dp, R.string.text_cannotLoadMedia);
                toggleMessage(true);
                break;
        }

        currentUIState = state;
    }

    private void replaceMessage(int textResource) {
        replaceMessage(0, textResource);
    }

    private void replaceMessage(int iconResource, int textResource) {

        int currState = mMessageHolder.getVisibility();

        if (currState == View.VISIBLE) {
            mMessageHolder
                    .animate()
                    .setDuration(300)
                    .alpha(0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            setMessage(iconResource, textResource);
                        }
                    });
        }
        else
        {
            setMessage(iconResource, textResource);
        }
    }

    private void setMessage(int iconResource, int textResource) {
        mMessageIcon.setImageResource(iconResource);
        mMessageText.setText(textResource);

        if (iconResource != 0)
            mMessageProgressBar.setVisibility(View.GONE);
        else
            mMessageProgressBar.setVisibility(View.VISIBLE);

        int currSate = mMessageHolder.getVisibility();

        if (mMessageHolder.getAlpha() < 1 && currSate == View.VISIBLE) {
            mMessageHolder
                    .animate()
                    .setDuration(300)
                    .alpha(1)
                    .setListener(null);
        }
    }

    private void toggleMessage(boolean isVisible) {

        int currentState = mMessageHolder.getVisibility();

        if (isVisible) {
            if (currentState == View.VISIBLE)
                return;

            mRecyclerView.setVisibility(View.GONE);
            mMessageHolder.setAlpha(0);
            mMessageHolder.setVisibility(View.VISIBLE);
            mMessageHolder.animate()
                    .alpha(1)
                    .setDuration(150)
                    .setListener(null);
        }
        else
        {
            if (currentState == View.GONE)
                return;

            mMessageHolder.setVisibility(View.VISIBLE);
            mMessageHolder.setAlpha(1);
            mMessageHolder.animate()
                    .alpha(0)
                    .setDuration(150)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            mMessageHolder.setVisibility(View.GONE);
                            mRecyclerView.setVisibility(View.VISIBLE);
                        }
                    });
        }
    }
}