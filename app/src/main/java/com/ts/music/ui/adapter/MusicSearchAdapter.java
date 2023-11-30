package com.ts.music.ui.adapter;

import android.view.View;

import androidx.annotation.Nullable;


import com.ts.sdk.media.bean.AudioInfoBean;
import com.ts.sdk.media.bean.UsbDevicesInfoBean;
import com.ts.music.R;
import com.ts.music.base.BaseRecyclerAdapter;
import com.ts.music.base.BaseRecyclerHolder;
import com.ts.music.constants.MusicConstants;
import com.ts.music.utils.MusicUtils;

import java.util.List;

/**
 * Music search adapter.
 */
public class MusicSearchAdapter extends BaseRecyclerAdapter<AudioInfoBean, BaseRecyclerHolder> {
    private String mSearchKey;
    private List<UsbDevicesInfoBean> mUsbDevicesInfoBeans;
    private static final int LABEL_INDEX = 9;

    public MusicSearchAdapter(int layoutResId, @Nullable List<AudioInfoBean> data) {
        super(layoutResId, data);
    }

    public void setSearchKey(String searchKey) {
        mSearchKey = searchKey;
        notifyDataSetChanged();
    }

    public void setUsbDevicesInfoBeans(List<UsbDevicesInfoBean> usbDevicesInfoBeans) {
        mUsbDevicesInfoBeans = usbDevicesInfoBeans;
    }

    /**
     * Reset song path display .
     */
    public String resetPath(String path) {
        StringBuffer newPath = new StringBuffer("/storage/");
        if (null != mUsbDevicesInfoBeans && mUsbDevicesInfoBeans.size() > 0) {
            if (path != null) {
                String uuid = path.substring(LABEL_INDEX, path.indexOf("/", LABEL_INDEX));
                if (uuid != null && uuid.length() > 0) {
                    for (UsbDevicesInfoBean infoBean : mUsbDevicesInfoBeans) {
                        if (infoBean.getUuid().equals(uuid)) {
                            if (infoBean.getLabel() == null) {
                                if (infoBean.getPort().equals(MusicConstants.USB_1_PORT)) {
                                    newPath.append("disk1");
                                } else {
                                    newPath.append("disk2");
                                }
                            } else {
                                newPath.append(infoBean.getLabel());
                            }
                        }
                    }
                }
                newPath.append(path.substring(path.indexOf("/", LABEL_INDEX)));
            }
        }
        return newPath.toString();
    }

    @Override
    public void convert(BaseRecyclerHolder holder, AudioInfoBean item) {
        holder.getView(R.id.audioName).setSelected(true);
        holder.getView(R.id.nickName).setSelected(true);
        holder.getView(R.id.usbPath).setSelected(true);

        holder.setText(R.id.audioName, MusicUtils.getInstance().matcherSearchText(
                item.getAudioName(), mSearchKey));
        holder.setText(R.id.nickName, MusicUtils.getInstance().matcherSearchText(
                (item.getAudioArtistName() + " - " + item.getAudioAlbumName()), mSearchKey));
        String newPath = resetPath(item.getAudioPath());
        holder.setText(R.id.usbPath, newPath);
        holder.setImageBitmap(R.id.songListLeftIcon, MusicUtils.getInstance()
                .getAlbumArt(item.getAudioPath(), true));
        holder.getView(R.id.linearLayout).setOnClickListener(new ItemClick(
                holder, item, holder.getAdapterPosition()));
    }

    @Override
    protected void convert(BaseRecyclerHolder helper, AudioInfoBean item, List<Object> payloads) {
        // Nothing to do
    }

    public class ItemClick implements View.OnClickListener {
        private AudioInfoBean mBaseAudioInfo;
        private int mPosition;
        private BaseRecyclerHolder mHelper;

        ItemClick(BaseRecyclerHolder helper, AudioInfoBean baseAudioInfo, int position) {
            mBaseAudioInfo = baseAudioInfo;
            mPosition = position;
            mHelper = helper;
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.linearLayout) {
                if (null != mOnClickListener) {
                    mOnClickListener.play(mHelper, mBaseAudioInfo, mPosition);
                }
            }
        }
    }

    public interface OnClickListener {
        void play(BaseRecyclerHolder helper, AudioInfoBean resultBean, int position);
    }

    private OnClickListener mOnClickListener;

    public void setOnItemClickListener(OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }
}
