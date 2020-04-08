package com.naloaty.syncshare.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.naloaty.syncshare.R;
import com.naloaty.syncshare.activity.MainActivity;
import com.naloaty.syncshare.adapter.DeviceItem;
import com.naloaty.syncshare.adapter.DevicesOnlineAdapter;
import com.naloaty.syncshare.adapter.HeaderItem;
import com.naloaty.syncshare.adapter.ListItem;
import com.naloaty.syncshare.database.DeviceConnection;
import com.naloaty.syncshare.database.DeviceConnectionViewModel;
import com.naloaty.syncshare.util.AppUtils;
import com.naloaty.syncshare.util.NsdHelper;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment {

    private ArrayList<ListItem> mList;
    private RecyclerView mRecyclerView;
    private DevicesOnlineAdapter mDevicesOnlineAdapter;
    private NsdHelper mNsdHelper;
    private DeviceConnectionViewModel deviceConnectionViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNsdHelper = new NsdHelper(getContext());

        deviceConnectionViewModel = new ViewModelProvider(this).get(DeviceConnectionViewModel.class);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        mNsdHelper.startDiscovering();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        mNsdHelper.stopDiscovering();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.layout_main_fragment, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toast.makeText(getContext(), "Fragment created.", Toast.LENGTH_SHORT).show();

        //mList = new ArrayList<>();

        //mList.add(new HeaderItem(R.string.text_localArea));
        //mList.add(new DeviceItem("This Device", R.drawable.ic_home_24dp));

        //mList.add(new HeaderItem(R.string.text_nearbyArea));
        //mList.add(new DeviceItem("Test device #1", R.drawable.ic_phone_android_24dp));
        //mList.add(new DeviceItem("Test device #2", R.drawable.ic_phone_android_24dp));
        //mList.add(new DeviceItem("Test device #3", R.drawable.ic_phone_android_24dp));

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mDevicesOnlineAdapter = new DevicesOnlineAdapter();

        mRecyclerView = view.findViewById(R.id.main_fragment_devices_online);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mDevicesOnlineAdapter);

        deviceConnectionViewModel.getAllConnections().observe(getViewLifecycleOwner(), new Observer<List<DeviceConnection>>() {
            @Override
            public void onChanged(List<DeviceConnection> deviceConnections) {
                mList = new ArrayList<>();
                mList.add(new HeaderItem(R.string.text_nearbyArea));

                for(DeviceConnection connection: deviceConnections) {
                    mList.add(new DeviceItem(connection.getDeviceId(), R.drawable.ic_phone_android_24dp));
                }

                mDevicesOnlineAdapter.setItems(mList);
                Toast.makeText(getContext(), "New connection", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
