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
import androidx.recyclerview.widget.RecyclerView;

import com.naloaty.syncshare.R;
import com.naloaty.syncshare.adapter.CategoryAdapter;
import com.naloaty.syncshare.adapter.base.Category;
import com.naloaty.syncshare.adapter.custom.DefaultHeader;
import com.naloaty.syncshare.adapter.custom.DiscoveredDevice;
import com.naloaty.syncshare.database.NetworkDevice;
import com.naloaty.syncshare.database.NetworkDeviceViewModel;
import com.naloaty.syncshare.util.NsdHelper;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment {

    private ArrayList<Category> mList;
    private RecyclerView mRecyclerView;
    private CategoryAdapter mCategoryAdapter;
    private NsdHelper mNsdHelper;
    private NetworkDeviceViewModel networkDeviceViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNsdHelper = new NsdHelper(getContext());

        networkDeviceViewModel = new ViewModelProvider(this).get(NetworkDeviceViewModel.class);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        //mNsdHelper.startDiscovering();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        //mNsdHelper.stopDiscovering();
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

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mCategoryAdapter = new CategoryAdapter();

        mRecyclerView = view.findViewById(R.id.main_fragment_devices_online);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mCategoryAdapter);

        /*networkDeviceViewModel.getAllDevices().observe(getViewLifecycleOwner(), new Observer<List<NetworkDevice>>() {
            @Override
            public void onChanged(List<NetworkDevice> networkDevices) {
                mList = new ArrayList<>();

                Category category = new Category(new DefaultHeader(R.string.text_nearbyArea));

                for(NetworkDevice connection: networkDevices) {
                    category.addItem(new DiscoveredDevice(connection.getDeviceId(), "1.0", R.drawable.ic_phone_android_24dp));
                }

                mList.add(category);
                mCategoryAdapter.setItems(mList);
            }
        });*/
    }
}
