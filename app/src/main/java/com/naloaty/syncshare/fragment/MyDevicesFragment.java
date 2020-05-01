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
import com.naloaty.syncshare.adapter.custom.DefaultHeader;
import com.naloaty.syncshare.adapter.custom.MyDevice;
import com.naloaty.syncshare.config.AppConfig;
import com.naloaty.syncshare.database.NetworkDevice;
import com.naloaty.syncshare.database.NetworkDeviceRepository;
import com.naloaty.syncshare.database.SSDevice;
import com.naloaty.syncshare.database.SSDeviceRepository;
import com.naloaty.syncshare.database.SSDeviceViewModel;
import com.naloaty.syncshare.dialog.MyDeviceDetailsDialog;
import com.naloaty.syncshare.widget.RecyclerViewEmptySupport;

import java.util.ArrayList;
import java.util.List;

public class MyDevicesFragment extends Fragment {

    private ArrayList<Category> mList;
    private RecyclerViewEmptySupport mRecyclerView;
    private CategoryAdapter mCategoryAdapter;
    private SSDeviceViewModel ssDeviceViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ssDeviceViewModel = new ViewModelProvider(this).get(SSDeviceViewModel.class);
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

        BodyItem.OnItemClickListener clickListener = new BodyItem.OnItemClickListener() {
            @Override
            public void onItemClick(BodyItem item) {
                SSDeviceRepository repository = new SSDeviceRepository(getContext());

                MyDevice device = (MyDevice)item;
                SSDevice ssDevice = repository.findDevice(device.getDeviceId());

                MyDeviceDetailsDialog details = new MyDeviceDetailsDialog(getContext());
                details.setSSDevice(ssDevice);
                details.show();
            }
        };

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mCategoryAdapter = new CategoryAdapter();

        mRecyclerView = view.findViewById(R.id.my_devices_recycler_view);
        mRecyclerView.setEmptyView(view.findViewById(R.id.my_devices_empty_placeholder));
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mCategoryAdapter);

        ssDeviceViewModel.getAllDevices().observe(getViewLifecycleOwner(), new Observer<List<SSDevice>>() {
            @Override
            public void onChanged(List<SSDevice> ssDevices) {
                NetworkDeviceRepository repository = new NetworkDeviceRepository(getContext());

                mList = new ArrayList<>();
                Category myDevices = new Category(new DefaultHeader(R.string.text_myDevices));

                for(SSDevice device: ssDevices) {
                    NetworkDevice networkDevice = repository.findDevice(null, device.getDeviceId(), null);

                    int imageResource;
                    int colorResource;
                    String generalInfo;
                    String extraInfo;

                    String platform = device.getAppVersion().split("::")[1];
                    switch (platform) {

                        case AppConfig.PLATFORM_MOBILE:
                            imageResource = R.drawable.ic_phone_android_24dp;
                            break;

                        case AppConfig.PLATFORM_DESKTOP:
                            imageResource = R.drawable.ic_desktop_windows_24dp;
                            break;

                        default:
                            imageResource = R.drawable.ic_warning_24dp;
                            break;
                    }

                    generalInfo = device.getNickname();

                    if (networkDevice != null){
                        colorResource = R.color.colorOnline;
                        extraInfo = networkDevice.getIpAddress();
                    }
                    else
                    {
                        colorResource = R.color.colorOffline;
                        extraInfo = getString(R.string.text_unknownAddress);
                    }

                    MyDevice myDevice = new MyDevice(device.getDeviceId(), generalInfo, extraInfo, imageResource);
                    myDevice.setImageTint(getContext(), colorResource);
                    myDevice.setOnItemClickListener(clickListener);
                    myDevices.addItem(myDevice);
                }

                if (myDevices.getItemsCount() > 0)
                    mList.add(myDevices);

                mCategoryAdapter.setItems(mList);
            }
        });
    }
}
