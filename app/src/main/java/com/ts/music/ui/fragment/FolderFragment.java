package com.ts.music.ui.fragment;

import static android.view.View.SCROLLBARS_OUTSIDE_OVERLAY;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ts.music.R;
import com.ts.music.entity.Au;
import com.ts.music.ui.adapter.USBFolderAdapter;

import java.util.ArrayList;
import java.util.List;

public class FolderFragment extends Fragment {
    public static FolderFragment newInstance() {
        return new FolderFragment();
    }
    private USBFolderAdapter usbFolderAdapter;
    private List<Au> list;
    private RecyclerView mRecyclerView;
    private View view;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_allchannel, container, false);
        initView();
        initLayout();
        return view;
    }

    private void initView(){
        mRecyclerView = view.findViewById(R.id.mRecyclerView);
    }

    public void initLayout() {
        list = new ArrayList<>();
        usbFolderAdapter = new USBFolderAdapter();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        list.add(new Au("01",R.drawable.icon_usb_list_item,
                "joe hisaishi","Collection of Mo Wenwei","526 pieces of music"));
        list.add(new Au("02",R.drawable.icon_usb_list_item,
                "Jake Miller","Collection of Jay Chou","289 pieces of music"));
        list.add(new Au("03",R.drawable.icon_usb_list_item,
                "Jake Miller","Collection of Chinese music lists","260 pieces of music"));
        list.add(new Au("04",R.drawable.icon_usb_list_item,
                "Grapefruit Moon","Eason Chan Cantonese Collection","260 pieces of music"));
        list.add(new Au("05",R.drawable.icon_usb_list_item,
                "Oasis","Folder 1","260 pieces of music"));

        mRecyclerView.setScrollBarStyle(SCROLLBARS_OUTSIDE_OVERLAY);
        usbFolderAdapter.setDataList(list);
        mRecyclerView.setAdapter(usbFolderAdapter);
    }
}
