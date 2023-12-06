package com.ts.music.ui.adapter;

import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.ts.music.R;
import com.ts.music.entity.Au;

import java.util.ArrayList;
import java.util.List;

public class RadioListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<Au> mRadioBeanList = new ArrayList<>();
    private static final String TAG = "RadioListAdapter";
    private int selectedItem = -1;
    public void setDataList(List<Au> list) {
        mRadioBeanList.clear();
        if (list != null) {
            mRadioBeanList.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_radio_list, parent, false);
        return new RadioViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((RadioViewHolder) holder).bind(mRadioBeanList.get(position));
        holder.itemView.setSelected(selectedItem == position);
//        Log.e(TAG, "onBindViewHolder: "+(selectedItem == position));
        ((RadioViewHolder) holder).radio_list_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notifyItemChanged(selectedItem); // 取消上一次选中项的背景
                selectedItem = position; // 设置当前选中项
                notifyItemChanged(selectedItem); // 设置当前选中项的背景
            }
        });

        AnimationDrawable animationDrawable =
                (AnimationDrawable) ((RadioViewHolder) holder).lottieAnimationView.getBackground();


        if (((RadioViewHolder) holder).itemView.isSelected()){
            ((RadioViewHolder) holder).tvNum.setVisibility(View.INVISIBLE);
            ((RadioViewHolder) holder).lottieAnimationView.setVisibility(View.VISIBLE);
            animationDrawable.setOneShot(false);
            animationDrawable.start();

        }else {
            ((RadioViewHolder) holder).tvNum.setVisibility(View.VISIBLE);
            ((RadioViewHolder) holder).lottieAnimationView.setVisibility(View.INVISIBLE);
            animationDrawable.stop();
        }
    }

    @Override
    public int getItemCount() {
        return mRadioBeanList.size();
    }

    public static class RadioViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName , tvNum;
        private ConstraintLayout radio_list_item;
        private ImageView lottieAnimationView;

        public RadioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_num);
            tvNum = itemView.findViewById(R.id.tv_num);
            radio_list_item = itemView.findViewById(R.id.radio_list_item);
            lottieAnimationView = itemView.findViewById(R.id.lottieAnimationView);
        }

        public void bind(Au radioBean) {
            tvName.setText(radioBean.getName());
            tvNum.setText(radioBean.getNum());
        }
    }
}