package com.naloaty.streamshare.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
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
import com.naloaty.streamshare.R;
import com.naloaty.streamshare.activity.RemoteViewActivity;
import com.naloaty.streamshare.adapter.OnRVClickListener;
import com.naloaty.streamshare.adapter.RemoteAlbumsAdapter;
import com.naloaty.streamshare.communication.CommunicationHelper;
import com.naloaty.streamshare.database.entity.NetworkDevice;
import com.naloaty.streamshare.database.viewmodel.NetworkDeviceViewModel;
import com.naloaty.streamshare.database.entity.SSDevice;
import com.naloaty.streamshare.database.viewmodel.SSDeviceViewModel;
import com.naloaty.streamshare.database.entity.Album;

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

/**
 * This fragment displays a list of albums on the remote device.
 * @see RemoteViewActivity
 */
public class RemoteAlbumsFragment extends Fragment {

    private List<Album> mList;
    private RemoteAlbumsAdapter mRVadapter;
    private SSDeviceViewModel mDeviceViewModel;
    private NetworkDeviceViewModel mNetworkDeviceViewModel;
    private CompositeDisposable mDisposables;
    private NetworkDevice mNetworkDevice;
    private SSDevice mSSDevice;
    private String mDeviceId;

    /* UI elements*/
    private RecyclerView mRecyclerView;
    private TextView mHelpText;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDisposables = new CompositeDisposable();
        mDeviceViewModel = new ViewModelProvider(this).get(SSDeviceViewModel.class);
        mNetworkDeviceViewModel = new ViewModelProvider(this).get(NetworkDeviceViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_remote_albums_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mHelpText = view.findViewById(R.id.remote_albums_help);
        mRecyclerView = view.findViewById(R.id.remote_albums_recycler_view);

        Bundle bundle = getArguments();

        if (bundle != null) {
            String deviceNickname = bundle.getString(RemoteViewActivity.EXTRA_DEVICE_NICKNAME);
            mDeviceId = bundle.getString(RemoteViewActivity.EXTRA_DEVICE_ID);
            mHelpText.setText(String.format(getString(R.string.text_remoteAlbumsHelp), deviceNickname));
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

        mDisposables.clear();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mDisposables.dispose();
    }

    /**
     * Initializes a list of albums on remote device
     */
    private void setupRecyclerView() {
        OnRVClickListener clickListener = itemIndex -> {
            Album album = mList.get(itemIndex);

            Gson gson = new Gson();
            Intent intent = new Intent(RemoteViewActivity.ACTION_CHANGE_FRAGMENT);
            intent.putExtra(RemoteViewActivity.EXTRA_TARGET_FRAGMENT, RemoteViewFragment.MediaGridView.toString());
            intent.putExtra(RemoteViewActivity.EXTRA_ALBUM_NAME, album.getName());
            intent.putExtra(RemoteViewActivity.EXTRA_ALBUM, gson.toJson(album));

            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent);
        };

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRVadapter = new RemoteAlbumsAdapter(clickListener, mNetworkDevice);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mRVadapter);
    }

    /**
     * Loads network information about a remote device from a database
     * @see NetworkDeviceViewModel#findDevice(String, String, String) 
     * TODO: replace by class-helper
     */
    private void initDevice() {
        setUIState(UIState.LoadingAlbums);
        Single<NetworkDevice> netDeviceObs = mNetworkDeviceViewModel.findDevice(null, mDeviceId, null);

        mDisposables.add(netDeviceObs
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
                        setUIState(UIState.CannotLoadAlbums);

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
     * Loads general information about a remote device from a database
     * @see SSDeviceViewModel#findDevice(String) 
     * TODO: replace by class-helper
     */
    private void initSSDevice() {
        Single<SSDevice> ssDeviceObs = mDeviceViewModel.findDevice(mDeviceId);

        mDisposables.add(ssDeviceObs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(new DisposableSingleObserver<SSDevice>() {
                    @Override
                    public void onSuccess(SSDevice ssDevice) {
                        mSSDevice = ssDevice;
                        requestAlbumsList();
                    }

                    @Override
                    public void onError(Throwable e) {
                        setUIState(UIState.CannotLoadAlbums);
                        onInternalError("DEVICE_NOT_FOUND");
                    }
                }));
    }

    /**
     * Loads a list of albums on remote device
     */
    private void requestAlbumsList() {
        Call<List<Album>> request = CommunicationHelper.requestAlbumsList(getContext(), mNetworkDevice);

        if (request == null) {
            onInternalError("NULL_REQUEST");
            return;
        }

        request.enqueue(new Callback<List<Album>>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<List<Album>> call, Response<List<Album>> response) {
                if (response.body() != null && response.body().size() > 0) {
                    mList = response.body();
                    mRVadapter.setAlbumsList(response.body());
                    setUIState(UIState.AlbumsShown);
                }
                else
                    setUIState(UIState.NoAlbumsFound);
            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<List<Album>> call, Throwable t) {
                setUIState(UIState.CannotLoadAlbums);

                if (t instanceof SSLHandshakeException) {
                    new AlertDialog.Builder(requireContext())
                            .setTitle(R.string.title_securityException)
                            .setCancelable(false)
                            .setMessage(String.format(getString(R.string.text_securityException), mSSDevice.getNickname()))
                            .setPositiveButton(R.string.btn_close, (dialog, which) -> requireActivity().onBackPressed())
                            .show();
                }
                else
                    onInternalError("ALBUMS_REQUEST_FAILURE");
            }
        });
    }

    /**
     * Displays an error code alert dialog.
     * @param errorCode Error code
     */
    private void onInternalError(String errorCode) {
        setUIState(UIState.CannotLoadAlbums);

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.title_error)
                .setCancelable(false)
                .setMessage(String.format(getString(R.string.text_internalAppError), errorCode))
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
        AlbumsShown,
        LoadingAlbums,
        NoAlbumsFound,
        CannotLoadAlbums,
    }

    /**
     * Sets the state of the UI
     * @param state Required UI state
     */
    private void setUIState(UIState state) {
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