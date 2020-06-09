package com.naloaty.syncshare.fragment;

import androidx.fragment.app.Fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.naloaty.syncshare.R;
import com.naloaty.syncshare.activity.ImageViewActivity;
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
import com.naloaty.syncshare.util.DeviceUtils;

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
import retrofit2.internal.EverythingIsNonNull;

public class RemoteMediaFragment extends Fragment {

    private static final String TAG = "RemoteMediaFragment";

    private List<Media> mList;
    private RemoteMediaAdapter mRVAdapter;
    private SSDeviceViewModel ssDeviceVM;
    private NetworkDeviceViewModel netDeviceVM;
    private CompositeDisposable disposables;
    private NetworkDevice mNetworkDevice;
    private SSDevice mSSDevice;
    private String deviceId;
    private Album mAlbum;

    /* UI elements */
    private RecyclerView mRecyclerView;

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
        return inflater.inflate(R.layout.layout_remote_media_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView = view.findViewById(R.id.remote_media_recycler_view);
        Bundle bundle = getArguments();

        if (bundle != null){
            Gson gson = new Gson();
            deviceId = bundle.getString(RemoteViewActivity.EXTRA_DEVICE_ID);
            mAlbum = gson.fromJson(bundle.getString(RemoteViewActivity.EXTRA_ALBUM), Album.class);
        }
        else
        {
            onInternalError("EMPTY_BUNDLE");
            return;
        }

        initMessage(view.findViewById(R.id.message_placeholder));
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

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        setUpColumns();
    }

    /**
     * Initializes a grid list of media-files on remote devices.
     */
    private void setupRecyclerView() {
        OnRVClickListener clickListener = itemIndex -> {
            ListHolder listHolder = new ListHolder(mList, mNetworkDevice);

            Intent intent = new Intent(getActivity(), ImageViewActivity.class);
            intent.putExtra(ImageViewActivity.EXTRA_POSITION, itemIndex);
            intent.putExtra(ImageViewActivity.EXTRA_LIST_HOLDER, listHolder);

            startActivity(intent);
        };

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), getColumnsCount());
        mRVAdapter = new RemoteMediaAdapter(clickListener, mNetworkDevice);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mRVAdapter);

    }

    /**
     * Sets the optimal number of columns for the grid layout depending on the screen size.
     */
    private void setUpColumns() {
        if (mRecyclerView == null)
            return;

        if (mRecyclerView.getLayoutManager() == null) {
            Log.e(TAG, "RecyclerView is not properly initialized: LayoutManager is missing");
            return;
        }

        int columnsCount = getColumnsCount();

        if (columnsCount != ((GridLayoutManager) mRecyclerView.getLayoutManager()).getSpanCount()) {
            mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), columnsCount));
        }
    }

    /**
     * TODO: add ability to change in settings
     * Returns the optimal number of columns for the grid layout depending on the screen size.
     */
    private int getColumnsCount() {
       return DeviceUtils.getOptimalColumsCount(getResources());
    }

    /**
     * Loads general information about a remote device from a database
     * TODO: replace by class-helper
     */
    private void initDevice() {
        setUIState(UIState.LoadingMedia);
        Single<NetworkDevice> netDeviceObs = netDeviceVM.findDevice(null, deviceId, null);

        disposables.add(netDeviceObs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(new DisposableSingleObserver<NetworkDevice>() {
                    @Override
                    public void onSuccess(NetworkDevice networkDevice) {
                        mNetworkDevice = networkDevice;
                        setupRecyclerView();
                        initSSDevice();
                    }

                    @Override
                    public void onError(Throwable e) {
                        setUIState(UIState.CannotLoadMedia);
                        new AlertDialog.Builder(requireContext())
                                .setTitle(R.string.title_deviceOffline)
                                .setMessage(R.string.text_deviceOffline)
                                .setCancelable(false)
                                .setPositiveButton(R.string.btn_close, (dialog, which) -> requireActivity().onBackPressed())
                                .show();
                    }
                }));


    }

    /**
     * Loads network information about a remote device from a database
     * TODO: replace by class-helper
     */
    private void initSSDevice() {
        Single<SSDevice> ssDeviceObs = ssDeviceVM.findDevice(deviceId);

        disposables.add(ssDeviceObs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(new DisposableSingleObserver<SSDevice>() {
                    @Override
                    public void onSuccess(SSDevice ssDevice) {
                        mSSDevice = ssDevice;
                        requestMediaList();
                    }

                    @Override
                    public void onError(Throwable e) {
                        setUIState(UIState.CannotLoadMedia);
                        onInternalError("DEVICE_NOT_FOUND");
                    }
                }));
    }

    /**
     * Loads a media list from a specific album on a remote device.
     * @see CommunicationHelper#requestMediaList(Context, NetworkDevice, Album)
     */
    private void requestMediaList() {
        Call<List<Media>> request = CommunicationHelper.requestMediaList(getContext(), mNetworkDevice, mAlbum);

        if (request == null) {
            onInternalError("NULL_REQUEST");
            return;
        }

        request.enqueue(new Callback<List<Media>>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<List<Media>> call, Response<List<Media>> response) {

                if (response.body() != null && response.body().size() > 0) {
                    mList = response.body();
                    mRVAdapter.setMediaList(response.body());
                    setUIState(UIState.MediaShown);
                }
                else
                    setUIState(UIState.NoMediaFound);
            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<List<Media>> call, Throwable t) {
                setUIState(UIState.CannotLoadMedia);

                if (t instanceof SSLHandshakeException) {
                    new AlertDialog.Builder(requireContext())
                            .setTitle(R.string.title_securityException)
                            .setMessage(String.format(getString(R.string.text_securityException), mSSDevice.getNickname()))
                            .setCancelable(false)
                            .setPositiveButton(R.string.btn_close, (dialog, which) -> requireActivity().onBackPressed())
                            .show();
                }
                else
                    onInternalError("MEDIA_REQUEST_FAILURE");
            }
        });
    }

    /**
     * Displays an error code alert dialog.
     * @param errorCode Error code
     */
    private void onInternalError(String errorCode) {
        setUIState(UIState.CannotLoadMedia);

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.title_error)
                .setMessage(String.format(getString(R.string.text_internalAppError), errorCode))
                .setCancelable(false)
                .setPositiveButton(R.string.btn_close, (dialog, which) -> requireActivity().onBackPressed())
                .show();
    }

    /*
     * Following section represents the UI state machine
     * TODO: Wrap ViewMessage into widget
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

    /**
     * Sets the state of the UI
     * @param state Required UI state
     */
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

    /**
     * Replaces one message with another (progressbar instead of icon)
     * @param textResource Message text resource
     */
    private void replaceMessage(int textResource) {
        replaceMessage(0, textResource);
    }

    /**
     * Replaces one message with another
     * @param iconResource Message icon resource
     * @param textResource Message text resource
     */
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

    /**
     * Sets the message
     * @param iconResource Message icon resource
     * @param textResource Message text resource
     */
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

    /**
     * Toggles message visibility
     * @param isVisible Required message visibility
     */
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