package com.ts.music.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.ts.music.R;
import com.ts.music.base.BaseApplication;
import com.ts.music.utils.LogUtils;
import com.ts.sdk.media.bean.AudioInfoBean;

import java.util.ArrayList;
import java.util.List;

public class UsbCurrentlyListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<AudioInfoBean> mUsbBeanList = new ArrayList<>();
    private static final String TAG = "RadioListAdapter";
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

    public List<AudioInfoBean> getData(){
        return mUsbBeanList;
    }

    public void setSelect(int position){
        selectedItem = position;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_currently_radio_list, parent, false);
        return new RadioViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((RadioViewHolder) holder).bind(mUsbBeanList.get(position));
        holder.itemView.setSelected(selectedItem == position);
//        Log.e(TAG, "onBindViewHolder: "+(selectedItem == position));
        ((RadioViewHolder) holder).radioListItem.setOnClickListener(new View.OnClickListener() {
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
        if (((RadioViewHolder) holder).itemView.isSelected()){
            ((RadioViewHolder) holder).tvNum.setVisibility(View.INVISIBLE);
        }else {
            ((RadioViewHolder) holder).tvNum.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mUsbBeanList.size();
    }

    public static class RadioViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName , tvNum , tvTime;
        private ConstraintLayout radioListItem;
        private ImageView imageView;

        public RadioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvNum = itemView.findViewById(R.id.tv_num);
            tvTime = itemView.findViewById(R.id.tv_time);
            imageView = itemView.findViewById(R.id.imageView);
            radioListItem = itemView.findViewById(R.id.radio_list_item);
        }

        public void bind(AudioInfoBean radioBean) {
            tvName.setText(radioBean.getAudioName());

            long audioDuration = radioBean.getAddTime();
            int durationInSeconds = (int) (audioDuration / 1000); // 将毫秒转换为秒
            int minutes = durationInSeconds / 60; // 计算分钟
            int seconds = durationInSeconds % 60; // 计算剩余的秒数
            String formattedDuration = String.format("%02d:%02d", minutes, seconds); // 格式化为分钟:秒的形式
            LogUtils.logD(TAG, "USBListAdapter :: bind:formattedDuration" + formattedDuration);
            Glide.with(BaseApplication.getApplication())
                    .load(radioBean.getAudioPath())
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                    .placeholder(R.drawable.icon_usb_item_def)
                    .centerCrop()
                    .into(imageView);
            tvTime.setText(formattedDuration);
            if (getAbsoluteAdapterPosition() + 1 < 10) {
                tvNum.setText("0" + (getAbsoluteAdapterPosition() + 1));
            } else {
                tvNum.setText(getAbsoluteAdapterPosition() + 1 +"");
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