package com.naloaty.syncshare.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.naloaty.syncshare.R;
import com.naloaty.syncshare.adapter.DeviceItem;
import com.naloaty.syncshare.adapter.DevicesOnlineAdapter;
import com.naloaty.syncshare.adapter.HeaderItem;
import com.naloaty.syncshare.adapter.ListItem;

import java.util.ArrayList;

public class MainFragment extends Fragment {

    private ArrayList<ListItem> mList;
    private RecyclerView mRecyclerView;
    private DevicesOnlineAdapter mDevicesOnlineAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        mList = new ArrayList<>();

        mList.add(new HeaderItem(R.string.text_localArea));
        mList.add(new DeviceItem("This Device", R.drawable.ic_home_24dp));

        mList.add(new HeaderItem(R.string.text_nearbyArea));
        mList.add(new DeviceItem("Test device #1", R.drawable.ic_phone_android_24dp));
        mList.add(new DeviceItem("Test device #2", R.drawable.ic_phone_android_24dp));
        mList.add(new DeviceItem("Test device #3", R.drawable.ic_phone_android_24dp));

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mDevicesOnlineAdapter = new DevicesOnlineAdapter(mList);

        mRecyclerView = view.findViewById(R.id.main_fragment_devices_online);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mDevicesOnlineAdapter);
    }
}
