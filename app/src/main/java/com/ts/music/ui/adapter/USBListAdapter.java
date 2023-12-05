package com.ts.music.ui.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.ts.music.R;
import com.ts.music.base.BaseRecyclerAdapter;
import com.ts.music.base.BaseRecyclerHolder;
import com.ts.music.entity.RadioBean;
import com.ts.music.utils.LogUtils;
import com.ts.sdk.media.bean.AudioInfoBean;

import java.util.ArrayList;
import java.util.List;

public class USBListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<AudioInfoBean> mUsbBeanList = new ArrayList<>();
    private static final String TAG = "USBListAdapter";
    private int selectedItem = -1;
    public void setDataList(List<AudioInfoBean> list) {
        mUsbBeanList.clear();
        if (list != null) {
            mUsbBeanList.addAll(list);
        }
        notifyDataSetChanged();
    }
    public void clearData() {
        mUsbBeanList.clear();
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_usb_list, parent, false);
        return new USBListAdapter.RadioViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((USBListAdapter.RadioViewHolder) holder).bind(mUsbBeanList.get(position),holder.getAdapterPosition() );
        holder.itemView.setSelected(selectedItem == position);
//        Log.e(TAG, "onBindViewHolder: "+(selectedItem == position));
        ((USBListAdapter.RadioViewHolder) holder).radio_list_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notifyItemChanged(selectedItem); // 取消上一次选中项的背景
                selectedItem = position; // 设置当前选中项
                notifyItemChanged(selectedItem); // 设置当前选中项的背景
                if (mOnClickListener != null) {
                    mOnClickListener.play(holder,mUsbBeanList.get(holder.getAdapterPosition()),holder.getAdapterPosition());
                }
            }
        });
        if (((USBListAdapter.RadioViewHolder) holder).itemView.isSelected()){
            ((USBListAdapter.RadioViewHolder) holder).tv_num.setVisibility(View.INVISIBLE);
        }else {
            ((USBListAdapter.RadioViewHolder) holder).tv_num.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mUsbBeanList.size();
    }

    public static class RadioViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName , tvTitle,tvTime,tv_num;
        private ConstraintLayout radio_list_item;

        public RadioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvTitle = itemView.findViewById(R.id.tv_title);
            radio_list_item = itemView.findViewById(R.id.radio_list_item);
            tvTime = itemView.findViewById(R.id.tv_time);
            tv_num = itemView.findViewById(R.id.tv_num);
        }

        public void bind(AudioInfoBean radioBean,int po) {
            LogUtils.logD(TAG, "USBListAdapter :: bind");

            tvTitle.setText(radioBean.getAudioName());
            tvName.setText(radioBean.getAudioArtistName());

            long audioDuration = radioBean.getAddTime();
            int durationInSeconds = (int) (audioDuration / 1000); // 将毫秒转换为秒
            int minutes = durationInSeconds / 60; // 计算分钟
            int seconds = durationInSeconds % 60; // 计算剩余的秒数
            String formattedDuration = String.format("%02d:%02d", minutes, seconds); // 格式化为分钟:秒的形式
            LogUtils.logD(TAG, "USBListAdapter :: bind:formattedDuration" + formattedDuration);

            tvTime.setText(formattedDuration);
            if (getAbsoluteAdapterPosition() + 1 < 10) {
                tv_num.setText("0" + (getAbsoluteAdapterPosition() + 1));
            } else {
                tv_num.setText(getAbsoluteAdapterPosition() + 1 +"");
            }
        }
    }

    public interface OnClickListener {
        void play( RecyclerView.ViewHolder holder, AudioInfoBean resultBean, int position);
    }

    private USBListAdapter.OnClickListener mOnClickListener;

    public void setOnItemClickListener(USBListAdapter.OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }
}