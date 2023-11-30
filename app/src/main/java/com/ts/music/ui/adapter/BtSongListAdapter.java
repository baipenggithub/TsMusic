package com.ts.music.ui.adapter;

import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.ts.sdk.media.bean.AudioInfoBean;
import com.ts.music.R;
import com.ts.music.base.BaseApplication;
import com.ts.music.base.BaseRecyclerAdapter;
import com.ts.music.base.BaseRecyclerHolder;
import com.ts.music.constants.MusicConstants;
import com.ts.music.utils.LogUtils;
import com.ts.music.utils.MusicUtils;

import java.util.List;

/**
 * SongList Adapter.
 */
public class BtSongListAdapter extends BaseRecyclerAdapter<AudioInfoBean, BaseRecyclerHolder> {
    private static final String TAG = BtSongListAdapter.class.getSimpleName();
    public int mSelected = 0;
    private boolean mIsPlay;
    private Resources mResources;

    public BtSongListAdapter(int layoutResId, @Nullable List<AudioInfoBean> data) {
        super(layoutResId, data);
        mResources = BaseApplication.getApplication().getResources();
    }

    @Override
    public void convert(BaseRecyclerHolder holder, AudioInfoBean item) {
        LogUtils.logD(TAG, "AudioId:" + item.getAudioId() + ", name:" + item.getAudioName());
        holder.getView(R.id.audioName).setSelected(true);

        int folderSum = MusicUtils.getInstance().getAudioSum(getData());
        int listIndex = holder.getAdapterPosition() + 1 - folderSum;
        holder.setGone(R.id.songListSum, true);
        holder.setText(R.id.songListSum, String.valueOf(listIndex));
        holder.setText(R.id.audioName, item.getAudioName());
        ImageView imageView = (ImageView) holder.getView(R.id.songListRightIcon);
        LogUtils.logD(TAG, "mSelected:" + mSelected + " ,position:"
                + holder.getAdapterPosition() + " ,listIndex:" + listIndex);
        if (mSelected == MusicConstants.DEFAULT_PLAYER_INDEX) {
            mSelected = mSelected + folderSum;
        }
        if (holder.getLayoutPosition() == mSelected) {
            holder.itemView.setBackgroundColor(mResources.getColor(R.color.light_black));
            if (mIsPlay) {
                setPlayingVisiable(holder, true);
            } else {
                setPlayingVisiable(holder, false);
            }
        } else {
            setPlayingVisiable(holder, false);
            holder.itemView.setBackgroundColor(mResources.getColor(R.color.color_transparent));
        }
        holder.setImageBitmap(R.id.songListLeftIcon, MusicUtils.getInstance().getAlbumArt(
                item.getAudioPath(), true));
        holder.getView(R.id.linearLayout).setOnClickListener(new ItemClick(holder, item,
                holder.getAdapterPosition()));
    }

    private void setPlayingVisiable(BaseRecyclerHolder holder, boolean show) {
        ImageView imageView = (ImageView) holder.getView(R.id.songListRightIcon);
        if (imageView != null) {
            if (show) {
                imageView.setVisibility(View.VISIBLE);
                ((AnimationDrawable) imageView.getDrawable()).start();
            } else {
                ((AnimationDrawable) imageView.getDrawable()).stop();
                imageView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void convert(BaseRecyclerHolder helper, AudioInfoBean item, List<Object> payloads) {
        // Nothing to do
    }

    /**
     * Set Music player Manager and current playing index.
     *
     * @param isPlay   UsbMusicManager
     * @param position current playing index
     */
    public void setMusicPlayerManager(boolean isPlay, int position, String artistName) {
        mSelected = position;
        mIsPlay = isPlay;
        notifyDataSetChanged();
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
