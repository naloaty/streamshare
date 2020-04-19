package com.naloaty.syncshare.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.naloaty.syncshare.R;
import com.naloaty.syncshare.adapter.CategoryAdapter;
import com.naloaty.syncshare.adapter.base.Category;
import com.naloaty.syncshare.adapter.custom.DiscoveredDevice;
import com.naloaty.syncshare.database.DeviceConnection;
import com.naloaty.syncshare.database.DeviceConnectionViewModel;
import com.naloaty.syncshare.util.NsdHelper;
import com.naloaty.syncshare.widget.RecyclerViewEmptySupport;

import java.util.ArrayList;
import java.util.List;

public class NearbyDiscoveryFragment extends Fragment {

    private ArrayList<Category> mList;
    private RecyclerViewEmptySupport mRecyclerView;
    private CategoryAdapter mCategoryAdapter;
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
        final View view = inflater.inflate(R.layout.layout_nearby_discovery_fragment, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mCategoryAdapter = new CategoryAdapter();

        mRecyclerView = view.findViewById(R.id.nearby_discovery_recycler_view);
        mRecyclerView.setEmptyView(view.findViewById(R.id.nearby_discovery_empty_placeholder));
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mCategoryAdapter);

        deviceConnectionViewModel.getAllConnections().observe(getViewLifecycleOwner(), new Observer<List<DeviceConnection>>() {
            @Override
            public void onChanged(List<DeviceConnection> deviceConnections) {
                mList = new ArrayList<>();

                //Category category = new Category(new DefaultHeader(R.string.text_pairWithDiscoveredDevices));
                Category category = new Category(null);

                for(DeviceConnection connection: deviceConnections) {
                    if (connection.isLocalDevice() || connection.getDeviceId().contentEquals("-"))
                        continue;

                    category.addItem(new DiscoveredDevice(connection.getDeviceId(), "1.0", R.drawable.ic_phone_android_24dp));
                }

                mList.add(category);
                mCategoryAdapter.setItems(mList);
            }
        });
    }
}
