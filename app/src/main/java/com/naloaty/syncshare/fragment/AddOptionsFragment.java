package com.naloaty.syncshare.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.naloaty.syncshare.R;
import com.naloaty.syncshare.activity.AddDeviceActivity;
import com.naloaty.syncshare.service.CommunicationService;
import com.naloaty.syncshare.util.AppUtils;

/*
 * This fragment displays a list of device adding options.
 */
public class AddOptionsFragment extends Fragment {

    private static final String TAG = "AddOptionsFragment";
    private ProgressBar mProgressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_add_options_fragment, container, false);
    }

    /**
     * Sets the state of UI depending on the state of CommunicationService
     * @param serviceRunning CommunicationService state (running or not)
     */
    public void setServiceState(boolean serviceRunning) {
        if (mProgressBar != null)
            mProgressBar.setVisibility(serviceRunning ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();

        setServiceState(AppUtils.isServiceRunning(requireContext(), CommunicationService.class));
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mProgressBar = view.findViewById(R.id.progressBar);

        View.OnClickListener listener = v -> {
            switch (v.getId()) {
                case R.id.add_option_connection_info:
                    changeFragment(OptionFragment.DeviceInfo);
                    break;
                case R.id.add_option_scan_qr:
                    changeFragment(OptionFragment.ScanQR);
                    break;
                case R.id.add_option_enter_ip:
                    changeFragment(OptionFragment.EnterDeviceId);
                    break;
            }
        };

        view.findViewById(R.id.add_option_connection_info).setOnClickListener(listener);
        view.findViewById(R.id.add_option_scan_qr).setOnClickListener(listener);
        view.findViewById(R.id.add_option_enter_ip).setOnClickListener(listener);
    }

    /**
     * Sends a request to change a fragment
     * @param targetFragment Required fragment
     */
    private void changeFragment(@NonNull OptionFragment targetFragment) {
        Log.d(TAG, String.format("Requesting %s fragment", targetFragment.toString()));


        LocalBroadcastManager.getInstance(requireContext())
                .sendBroadcast(new Intent(AddDeviceActivity.ACTION_CHANGE_FRAGMENT)
                .putExtra(AddDeviceActivity.EXTRA_TARGET_FRAGMENT, targetFragment.toString()));
    }
}
