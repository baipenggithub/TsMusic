package com.ts.music.ui.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ts.music.databinding.ItemFavoriteradioListBinding;
import com.ts.music.databinding.ItemRadioListBinding;
import com.ts.music.entity.RadioBean;

import java.util.ArrayList;
import java.util.List;

public class RadioFavoriteListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<RadioBean> mRadioBeanList = new ArrayList<>();
    private static final String TAG = "RadioListAdapter";
    private int selectedItem = -1;
    public void setDataList(List<RadioBean> list) {
        mRadioBeanList.clear();
        if (list != null) {
            mRadioBeanList.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFavoriteradioListBinding binding = ItemFavoriteradioListBinding
                .inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new RadioViewHolder(binding);

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((RadioViewHolder) holder).bind(mRadioBeanList.get(position));
        holder.itemView.setSelected(selectedItem == position);
        Log.e(TAG, "onBindViewHolder: "+(selectedItem == position));
        ((RadioViewHolder) holder).mBinding.radioListItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notifyItemChanged(selectedItem); // 取消上一次选中项的背景
                selectedItem = position; // 设置当前选中项
                notifyItemChanged(selectedItem); // 设置当前选中项的背景
            }
        });
        if (((RadioViewHolder) holder).itemView.isSelected()){
            ((RadioViewHolder) holder).mBinding.tvNum.setVisibility(View.INVISIBLE);
        }else {
            ((RadioViewHolder) holder).mBinding.tvNum.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mRadioBeanList.size();
    }

    public static class RadioViewHolder extends RecyclerView.ViewHolder {
        private ItemFavoriteradioListBinding mBinding;

        public RadioViewHolder(@NonNull ItemFavoriteradioListBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }
        public void bind(RadioBean radioBean) {
            mBinding.setRadioInfo(radioBean);
            Context context = mBinding.getRoot().getContext();
            mBinding.tvName.setText(radioBean.getName());
            mBinding.tvNum.setText(radioBean.getNum());

            mBinding.executePendingBindings();
        }
    }
}