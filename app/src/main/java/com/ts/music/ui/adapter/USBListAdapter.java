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

public class USBListAdapter extends BaseRecyclerAdapter<AudioInfoBean, BaseRecyclerHolder> {
    private final List<RadioBean> mRadioBeanList = new ArrayList<>();
    private static final String TAG = "RadioListAdapter";
    private int selectedItem = -1;

    /**
     * Initialization data.
     *
     * @param layoutResId
     * @param data
     */
    public USBListAdapter(int layoutResId, @Nullable List<AudioInfoBean> data) {
        super(layoutResId, data);
    }

    public void setDataList(List<RadioBean> list) {
        mRadioBeanList.clear();
        if (list != null) {
            mRadioBeanList.addAll(list);
        }
        notifyDataSetChanged();
    }


    @Override
    protected void convert(BaseRecyclerHolder holder, AudioInfoBean item) {
        LogUtils.logD(TAG, "AudioId:" + item.getAudioId() + ", name:" + item.getAudioName());
        holder.itemView.setSelected(selectedItem == holder.getLayoutPosition());
        holder.getView(R.id.radio_list_item).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notifyItemChanged(selectedItem);
                selectedItem = holder.getLayoutPosition();
                notifyItemChanged(selectedItem);
            }
        });
        if (holder.itemView.isSelected()) {
            holder.getView(R.id.tv_num).setVisibility(View.INVISIBLE);
        } else {
            holder.getView(R.id.tv_num).setVisibility(View.VISIBLE);
        }

        holder.setText(R.id.tv_num, String.valueOf(holder.getLayoutPosition() + 1));
        holder.setText(R.id.tv_name, item.getAudioName());
        holder.setText(R.id.tv_title, item.getAudioArtistName());
        holder.setText(R.id.tv_time, item.getAddTime()+"");
        Glide.with(holder.itemView.getContext())
                .load(item.getAudioPath())
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                .placeholder(R.drawable.usb_music_ic_item_no_album)
                .centerCrop()
                .into((ImageView) holder.itemView.findViewById(R.id.imageView));

    }

    @Override
    protected void convert(BaseRecyclerHolder holder, AudioInfoBean item, List<Object> payloads) {

    }


    @Override
    public int getItemCount() {
        return mRadioBeanList.size();
    }

    public static class RadioViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName, tvNum, tv_title, tv_time;
        private ImageView imageView;
        private ConstraintLayout radio_list_item;

        public RadioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvNum = itemView.findViewById(R.id.tv_num);
            tv_title = itemView.findViewById(R.id.tv_title);
            imageView = itemView.findViewById(R.id.imageView);
            tv_time = itemView.findViewById(R.id.tv_time);
            radio_list_item = itemView.findViewById(R.id.radio_list_item);
        }

        public void bind(RadioBean radioBean) {
            tv_title.setText(radioBean.getTitle());
            tvNum.setText(radioBean.getNum());
            tvName.setText(radioBean.getName());
            imageView.setImageResource(radioBean.getIcon());
            tv_time.setText(radioBean.getTime());
        }
    }
}