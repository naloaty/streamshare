package com.naloaty.syncshare.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.DialogInterface;
import android.content.Intent;
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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.naloaty.syncshare.R;
import com.naloaty.syncshare.activity.AddDeviceActivity;
import com.naloaty.syncshare.activity.RemoteViewActivity;
import com.naloaty.syncshare.adapter.OnRVClickListener;
import com.naloaty.syncshare.adapter.RemoteAlbumsAdapter;
import com.naloaty.syncshare.communication.CommunicationHelper;
import com.naloaty.syncshare.database.device.NetworkDevice;
import com.naloaty.syncshare.database.device.NetworkDeviceViewModel;
import com.naloaty.syncshare.database.device.SSDevice;
import com.naloaty.syncshare.database.device.SSDeviceViewModel;
import com.naloaty.syncshare.database.media.Album;
import com.naloaty.syncshare.dialog.SSProgressDialog;

import java.util.ArrayList;
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

public class RemoteAlbumsFragment extends Fragment {

    private static final String TAG = "RemoteAlbumsFragment";

    private List<Album> mList;
    private RecyclerView mRecyclerView;
    private RemoteAlbumsAdapter mRVadapter;

    private TextView mHelpText;

    private SSDeviceViewModel ssDeviceVM;
    private NetworkDeviceViewModel netDeviceVM;

    private CompositeDisposable disposables;

    private NetworkDevice mNetworkDevice;
    private SSDevice mSSDevice;

    private String deviceId;

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
        final View view = inflater.inflate(R.layout.layout_remote_albums_fragment, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mHelpText = view.findViewById(R.id.remote_albums_help);
        mRecyclerView = view.findViewById(R.id.remote_albums_recycler_view);
        initMessage(view.findViewById(R.id.message_placeholder));

        Bundle bundle = getArguments();
        String deviceNickname;

        if (bundle != null){
            deviceNickname = bundle.getString(RemoteViewActivity.EXTRA_DEVICE_NICKNAME);
            deviceId = bundle.getString(RemoteViewActivity.EXTRA_DEVICE_ID);
        }
        else
        {
            onInternalError("EMPTY_BUNDLE");
            return;
        }

        mHelpText.setText(String.format(getString(R.string.text_remoteAlbumsHelp), deviceNickname));

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
                Album album = mList.get(itemIndex);

                Intent intent = new Intent(RemoteViewActivity.ACTION_CHANGE_FRAGMENT);
                intent.putExtra(RemoteViewActivity.EXTRA_TARGET_FRAGMENT, RemoteViewFragment.MediaView.toString());
                intent.putExtra(RemoteViewActivity.EXTRA_ALBUM_NAME, album.getName());
                Gson gson = new Gson();
                intent.putExtra(RemoteViewActivity.EXTRA_ALBUM, gson.toJson(album));
                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
            }
        };

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRVadapter = new RemoteAlbumsAdapter(clickListener, mNetworkDevice);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mRVadapter);
    }


    private void initDevice() {
        Log.d(TAG, "initDevice()");
        /*mProgressDialog.setMessage(R.string.progress_checkingOnline);

        if (!mProgressDialog.isShowing()){
            mProgressDialog.show();
        }*/

        setUIState(UIState.LoadingAlbums);

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
                        setUIState(UIState.CannotLoadAlbums);
                        new AlertDialog.Builder(getContext())
                                .setTitle(R.string.title_deviceOffline)
                                .setMessage(R.string.text_deviceOffline)
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
                        requestAlbumsList();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "initSSDevice(): error -> " + e);
                        setUIState(UIState.CannotLoadAlbums);
                        onInternalError("DEVICE_NOT_FOUND");
                    }
                }));
    }

    public void requestAlbumsList() {
        //setUIState(UIState.LoadingMedia);

        Log.d(TAG, "requestAlbumsList()");

        Call<List<Album>> request = CommunicationHelper.requestAlbumsList(getContext(), mNetworkDevice);
        request.enqueue(new Callback<List<Album>>() {
            @Override
            public void onResponse(Call<List<Album>> call, Response<List<Album>> response) {
                //setUIState(UIState.MediaShown);

                Log.d(TAG, "requestAlbumsList(): received response");
                if (response.body() != null && response.body().size() > 0) {
                    mList = response.body();
                    mRVadapter.setAlbumsList(response.body());
                    setUIState(UIState.AlbumsShown);
                    Log.d(TAG, "requestAlbumsList(): body has items. Message -> " + response.message());
                }
                else
                {
                    Log.d(TAG, "requestAlbumsList(): body null or do not have items. Message -> " + response.message());
                    setUIState(UIState.NoAlbumsFound);
                }
            }

            @Override
            public void onFailure(Call<List<Album>> call, Throwable t) {

                Log.d(TAG, "requestAlbumsList(): failure ->  " + t);
                setUIState(UIState.CannotLoadAlbums);

                if (t instanceof SSLHandshakeException) {
                    new AlertDialog.Builder(getContext())
                            .setTitle(R.string.title_securityException)
                            .setMessage(String.format(getString(R.string.text_securityException), mSSDevice.getNickname()))
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
                    onInternalError("ALBUMS_REQUEST_FAILURE");
            }
        });
    }

    private void onInternalError(String errorCode) {

        Log.d(TAG, "onInternalError(): " + errorCode);
        setUIState(UIState.CannotLoadAlbums);

        new AlertDialog.Builder(getContext())
                .setTitle(R.string.title_error)
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
        AlbumsShown,
        LoadingAlbums,
        NoAlbumsFound,
        CannotLoadAlbums,
    }

    private void setUIState(UIState state)
    {
        if (currentUIState == state)
            return;

        switch (state) {
            case AlbumsShown:
                toggleMessage(false);
                break;

            case LoadingAlbums:
                replaceMessage(R.string.text_loadingAlbums);
                toggleMessage(true);
                break;

            case NoAlbumsFound:
                setMessage(R.drawable.ic_image_24dp, R.string.text_noAlbumsFound);
                toggleMessage(true);
                break;

            case CannotLoadAlbums:
                setMessage(R.drawable.ic_warning_24dp, R.string.text_cannotLoadAlbums);
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
