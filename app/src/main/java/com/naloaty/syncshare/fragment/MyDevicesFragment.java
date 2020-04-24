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
import com.naloaty.syncshare.adapter.custom.DefaultHeader;
import com.naloaty.syncshare.adapter.custom.MyDevice;
import com.naloaty.syncshare.database.NetworkDevice;
import com.naloaty.syncshare.database.NetworkDeviceViewModel;
import com.naloaty.syncshare.widget.RecyclerViewEmptySupport;

import java.util.ArrayList;
import java.util.List;

public class MyDevicesFragment extends Fragment {

    private ArrayList<Category> mList;
    private RecyclerViewEmptySupport mRecyclerView;
    private CategoryAdapter mCategoryAdapter;
    private NetworkDeviceViewModel networkDeviceViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        networkDeviceViewModel = new ViewModelProvider(this).get(NetworkDeviceViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.layout_my_devices_fragment, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mCategoryAdapter = new CategoryAdapter();

        mRecyclerView = view.findViewById(R.id.my_devices_recycler_view);
        mRecyclerView.setEmptyView(view.findViewById(R.id.my_devices_empty_placeholder));
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mCategoryAdapter);

        networkDeviceViewModel.getAllDevices().observe(getViewLifecycleOwner(), new Observer<List<NetworkDevice>>() {
            @Override
            public void onChanged(List<NetworkDevice> networkDevices) {

                mList = new ArrayList<>();
                Category myDevices = new Category(new DefaultHeader(R.string.text_myDevices));

                for(NetworkDevice connection: networkDevices) {
                    if (connection.isLocalDevice() || connection.getDeviceId().contentEquals("-"))
                        continue;

                    myDevices.addItem(new MyDevice(connection.getDeviceId(), connection.getIpAddress(), R.drawable.ic_phone_android_24dp));
                }

                if (myDevices.getItemsCount() > 0) {
                    mList.add(myDevices);
                    mCategoryAdapter.setItems(mList);
                }
            }
        });
    }
}
