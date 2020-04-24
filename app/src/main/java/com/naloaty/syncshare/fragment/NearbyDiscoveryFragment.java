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
import com.naloaty.syncshare.adapter.base.BodyItem;
import com.naloaty.syncshare.adapter.base.Category;
import com.naloaty.syncshare.adapter.custom.DiscoveredDevice;
import com.naloaty.syncshare.database.NetworkDevice;
import com.naloaty.syncshare.database.NetworkDeviceRepository;
import com.naloaty.syncshare.database.NetworkDeviceViewModel;
import com.naloaty.syncshare.util.AddDeviceHelper;
import com.naloaty.syncshare.util.AppUtils;
import com.naloaty.syncshare.util.DNSSDHelper;
import com.naloaty.syncshare.widget.RecyclerViewEmptySupport;

import java.util.ArrayList;
import java.util.List;

public class NearbyDiscoveryFragment extends Fragment {

    private ArrayList<Category> mList;
    private RecyclerViewEmptySupport mRecyclerView;
    private CategoryAdapter mCategoryAdapter;
   // private NsdHelper mNsdHelper;
    private DNSSDHelper mDNSSDHelper;
    private NetworkDeviceViewModel networkDeviceViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //mNsdHelper = new NsdHelper(getContext());

        this.mDNSSDHelper = AppUtils.getDNSSDHelper(getActivity().getApplicationContext());
        this.networkDeviceViewModel = new ViewModelProvider(this).get(NetworkDeviceViewModel.class);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        //mNsdHelper.startDiscovering();
        NetworkDeviceRepository repository = new NetworkDeviceRepository(getContext());
        repository.deleteAllConnections();

        mDNSSDHelper.startBrowse();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        //mNsdHelper.stopDiscovering();
        mDNSSDHelper.stopBrowse();
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

        BodyItem.OnItemClickListener clickListener = new BodyItem.OnItemClickListener() {
            @Override
            public void onItemClick(BodyItem item) {
                DiscoveredDevice device = (DiscoveredDevice)item;
                AddDeviceHelper.proccessDevice(device.getIpAddress());
            }
        };

        networkDeviceViewModel.getAllDevices().observe(getViewLifecycleOwner(), new Observer<List<NetworkDevice>>() {
            @Override
            public void onChanged(List<NetworkDevice> networkDevices) {
                mList = new ArrayList<>();

                //Category category = new Category(new DefaultHeader(R.string.text_pairWithDiscoveredDevices));
                Category category = new Category(null);

                for(NetworkDevice networkDevice: networkDevices) {
                    if (networkDevice.isLocalDevice() || networkDevice.getDeviceId().contentEquals("-"))
                        continue;

                    DiscoveredDevice device = new DiscoveredDevice(networkDevice.getDeviceName(), networkDevice.getIpAddress(), R.drawable.ic_phone_android_24dp);
                    device.setOnItemClickListener(clickListener);
                    category.addItem(device);
                }

                mList.add(category);
                mCategoryAdapter.setItems(mList);
            }
        });
    }
}
