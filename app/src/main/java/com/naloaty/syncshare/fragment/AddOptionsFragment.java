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
import com.naloaty.syncshare.activity.AddDeviceActivity;

public class AddOptionsFragment extends Fragment {

    private static final String TAG = "AddOptionsFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.layout_add_options_fragment, container, false);
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
            }
        };

        view.findViewById(R.id.add_option_connection_info).setOnClickListener(listener);
        view.findViewById(R.id.add_option_scan_qr).setOnClickListener(listener);
        view.findViewById(R.id.add_option_enter_ip).setOnClickListener(listener);
    }

    private void changeFragment(OptionFragment targetFragment) {

        Log.d(TAG, "Sending broadcast");
        getContext().sendBroadcast(new Intent(AddDeviceActivity.ACTION_CHANGE_FRAGMENT)
                .putExtra(AddDeviceActivity.EXTRA_TARGET_FRAGMENT, targetFragment.toString()));
    }
}
