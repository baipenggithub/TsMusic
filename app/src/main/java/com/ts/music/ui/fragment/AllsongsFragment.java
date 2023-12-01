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
import com.ts.music.entity.RadioBean;
import com.ts.music.ui.adapter.USBListAdapter;

import java.util.ArrayList;
import java.util.List;

public class AllsongsFragment extends Fragment {
    public static AllsongsFragment newInstance() {
        return new AllsongsFragment();
    }
    private USBListAdapter usbListAdapter;
    private List<RadioBean> list;
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
        usbListAdapter = new USBListAdapter();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        list.add(new RadioBean("01", R.drawable.icon_item1,
                "joe hisaishi","City of sky","1:53"));
        list.add(new RadioBean("02",R.drawable.icon_item2,
                "Jake Miller","Parties","1:53"));
        list.add(new RadioBean("03",R.drawable.icon_item3,
                "Jake Miller","Mad at Disney","2:50"));
        list.add(new RadioBean("04",R.drawable.icon_item4,
                "Grapefruit Moon","New Coat Of Paint","1:53"));
        list.add(new RadioBean("05",R.drawable.icon_item5,
                "Oasis","Stop Crying Your Heart Out","4:16"));

        mRecyclerView.setScrollBarStyle(SCROLLBARS_OUTSIDE_OVERLAY);
        usbListAdapter.setDataList(list);
        mRecyclerView.setAdapter(usbListAdapter);
    }
}
