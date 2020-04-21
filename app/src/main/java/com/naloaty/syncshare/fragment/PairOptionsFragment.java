package com.naloaty.syncshare.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.naloaty.syncshare.R;
import com.naloaty.syncshare.activity.PairDeviceActivity;

public class PairOptionsFragment extends Fragment {

    private static final String TAG = "PairOptionsFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.layout_pair_options_fragment, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                switch (v.getId()) {
                    case R.id.pair_option_connection_info:
                        changeFragment(PairFragment.ConnectionInfo);
                        break;
                    case R.id.pair_option_scan_qr:
                        changeFragment(PairFragment.ScanQR);
                        break;
                    case R.id.pair_option_enter_ip:
                        changeFragment(PairFragment.EnterIP);
                        break;
                }
            }
        };

        view.findViewById(R.id.pair_option_connection_info).setOnClickListener(listener);
        view.findViewById(R.id.pair_option_scan_qr).setOnClickListener(listener);
        view.findViewById(R.id.pair_option_enter_ip).setOnClickListener(listener);
    }

    private void changeFragment(PairFragment targetFragment) {

        Log.d(TAG, "Sending broadcast");
        getContext().sendBroadcast(new Intent(PairDeviceActivity.ACTION_CHANGE_FRAGMENT)
                .putExtra(PairDeviceActivity.EXTRA_TARGET_FRAGMENT, targetFragment.toString()));
    }
}
