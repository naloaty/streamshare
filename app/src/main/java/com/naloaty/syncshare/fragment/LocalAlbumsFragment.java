package com.naloaty.syncshare.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.naloaty.syncshare.R;
import com.naloaty.syncshare.adapter.LocalAlbumsAdapter;
import com.naloaty.syncshare.adapter.OnRVClickListener;
import com.naloaty.syncshare.app.SSActivity;
import com.naloaty.syncshare.database.media.Album;
import com.naloaty.syncshare.database.media.AlbumViewModel;
import com.naloaty.syncshare.media.MediaProvider;
import com.naloaty.syncshare.security.SecurityUtils;
import com.naloaty.syncshare.service.CommunicationService;
import com.naloaty.syncshare.util.DeviceUtils;
import com.naloaty.syncshare.util.PermissionHelper;

import java.util.ArrayList;
import java.util.List;

public class LocalAlbumsFragment extends Fragment {

    private static final String TAG = "LocalAlbumsFragment";

    private ArrayList<Album> mList = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private LocalAlbumsAdapter mRVadapter;
    private AlbumViewModel mAlbumViewModel;

    private boolean isLoadingAlbums = false;
    private boolean isLoadingFailed = false;

    private final IntentFilter mFilter = new IntentFilter();

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d(TAG, "rec brod " + intent);

            if (getContext() == null)
                return;

            boolean securityStuffOk = SecurityUtils.checkSecurityStuff(getContext().getFilesDir(), false);
            boolean permissionsGranted = PermissionHelper.checkRequiredPermissions(getContext());

            if (securityStuffOk && permissionsGranted)
                setupRecyclerView();
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAlbumViewModel = new ViewModelProvider(this).get(AlbumViewModel.class);

        mFilter.addAction(SSActivity.PERMISSION_REQUEST_RESULT);
        mFilter.addAction(SSActivity.SECURITY_STUFF_GENERATION_RESULT);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_local_albums_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initMessage(view.findViewById(R.id.message_placeholder));

        mRecyclerView = view.findViewById(R.id.local_device_recycler_view);

        boolean securityStuffOk = SecurityUtils.checkSecurityStuff(getContext().getFilesDir(), false);
        boolean permissionsGranted = PermissionHelper.checkRequiredPermissions(getContext());

        if (securityStuffOk && permissionsGranted)
            setupRecyclerView();
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mReceiver, mFilter);
    }

    @Override
    public void onPause() {
        super.onPause();

        boolean securityStuffOk = SecurityUtils.checkSecurityStuff(getContext().getFilesDir(), false);
        boolean permissionsGranted = PermissionHelper.checkRequiredPermissions(getContext());

        if (!permissionsGranted || !securityStuffOk)
            return;

        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mReceiver);
    }

    private void setupRecyclerView() {

        OnRVClickListener clickListener = new OnRVClickListener() {
            @Override
            public void onClick(int itemIndex) {
                Album album = mList.get(itemIndex);
                mAlbumViewModel.publish(album);
            }
        };

        Fragment mainFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.fragment_main);

        RecyclerView.LayoutManager layoutManager;

        if (DeviceUtils.isPortrait(getResources()) || mainFragment != null)
            layoutManager = new LinearLayoutManager(getContext());
        else
            layoutManager = new GridLayoutManager(getContext(), 2);

        mRVadapter = new LocalAlbumsAdapter(clickListener);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mRVadapter);

        AlbumsLoaderCallback callback = new AlbumsLoaderCallback() {
            @Override
            public void onStart() {
                isLoadingAlbums = true;
                updateUIState();
            }

            @Override
            public void onFinish(List<Album> albums) {
                isLoadingAlbums = false;

                mList.clear();

                if (albums != null)
                    mList.addAll(albums);

                mRVadapter.setAlbumsList(mList);
                updateUIState();

            }

            @Override
            public void onFail() {
                isLoadingFailed = true;
                updateUIState();
            }
        };

        new LoadAlbumsAT(getContext(), callback).execute(mAlbumViewModel);
    }

    public interface AlbumsLoaderCallback {
        void onStart();
        void onFinish(List<Album> albums);
        void onFail();
    }

    private static class LoadAlbumsAT extends AsyncTask<AlbumViewModel, Void, List<Album>> {

        private final Context context;
        private final AlbumsLoaderCallback callback;
        
        public LoadAlbumsAT(final Context context, AlbumsLoaderCallback callback) {
            this.context = context;
            this.callback = callback;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            callback.onStart();
        }

        @Override
        protected List<Album> doInBackground(AlbumViewModel... viewModels) {

            try 
            {
                if (viewModels[0] == null)
                    cancel(true);

                List<Album> albums = MediaProvider.getAlbums(context);

                Log.w(TAG, "Count: " + albums.size());

                if (albums == null)
                    cancel(true);

                ArrayList<Album> resultAlbums = new ArrayList<>();
                List<Album> databaseAlbums = viewModels[0].getAllAlbumsList();

                for (Album album: albums) {

                    for(Album dbAlbum: databaseAlbums){
                        if (dbAlbum.getName().equals(album.getName())
                                && dbAlbum.getPath().equals(album.getPath())){
                            album.setAccessAllowed(dbAlbum.isAccessAllowed());
                            break;
                        }
                    }

                    resultAlbums.add(album);
                }

                return resultAlbums;
            }
            catch (Exception e) {
                Log.d(TAG, "Cannot load albums: " + e.getMessage());
                callback.onFail();
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<Album> albums) {
            super.onPostExecute(albums);
            callback.onFinish(albums);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            callback.onFail();
        }
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

    private void updateUIState() {
        setUIState(getRequiredState());
    }

    private UIState getRequiredState() {

        if (isLoadingAlbums)
            return UIState.LoadingAlbums;

        if (isLoadingFailed)
            return UIState.CannotLoadAlbums;

        boolean hasAlbums = mList.size() > 0;

        if (hasAlbums)
            return UIState.AlbumsShown;
        else
            return UIState.NoAlbumsFound;

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
