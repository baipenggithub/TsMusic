package com.ts.music.ui.adapter;

import android.graphics.drawable.AnimationDrawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.ts.sdk.media.bean.AudioInfoBean;
import com.ts.music.R;
import com.ts.music.base.BaseApplication;
import com.ts.music.base.BaseRecyclerAdapter;
import com.ts.music.base.BaseRecyclerHolder;
import com.ts.music.constants.MusicConstants;
import com.ts.music.customview.SongListMarquee;
import com.ts.music.utils.LogUtils;
import com.ts.music.utils.MusicUtils;


import java.util.List;

/**
 * SongList Adapter.
 */
public class SongListAdapter extends BaseRecyclerAdapter<AudioInfoBean, BaseRecyclerHolder> {
    private static final String TAG = SongListAdapter.class.getSimpleName();
    private int mSelected = 0;
    private boolean mIsPlay;
    private long mCurrentAudioId = -1;
    private int mLastSelected = -1;

    @Override
    public void onViewRecycled(@NonNull BaseRecyclerHolder holder) {
        super.onViewRecycled(holder);
        LogUtils.logD(TAG, "onViewRecycled ::" + holder.getAdapterPosition());
        setPlayingVisiable(holder, false);
        SongListMarquee audioName = holder.getView(R.id.audioName);
        if (null != audioName) {
            audioName.clear();
        }
        SongListMarquee nickName = holder.getView(R.id.nickName);
        if (null != nickName) {
            nickName.clear();
        }
    }

    public SongListAdapter(int layoutResId, @Nullable List<AudioInfoBean> data) {
        super(layoutResId, data);
    }

    @Override
    public void convert(BaseRecyclerHolder holder, AudioInfoBean item) {
        LogUtils.logD(TAG, "AudioId:" + item.getAudioId() + ", name:" + item.getAudioName());
        holder.getView(R.id.nickName).setSelected(true);
        holder.getView(R.id.audioName).setSelected(true);
        // Folder
        if (item.getAudioId() == MusicConstants.AUDIO_FOLDER_ID) {
            holder.setGone(R.id.songListRightIcon, false);
            holder.setGone(R.id.songListSum, false);
            holder.setGone(R.id.nickName, false);
            holder.setText(R.id.audioName, TextUtils.isEmpty(item.getAudioName())
                    ? BaseApplication.getApplication().getResources()
                    .getString(R.string.audio_name_default) : item.getAudioName());
            Glide.with(holder.itemView.getContext()).load(R.drawable.usb_ic_item_dragon)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .centerCrop()
                    .into((ImageView) holder.itemView.findViewById(R.id.songListLeftIcon));
            holder.itemView.setBackgroundColor(BaseApplication.getApplication().getResources()
                    .getColor(R.color.color_transparent));
            // Music files
        } else {
            int folderSum = MusicUtils.getInstance().getAudioSum(getData());
            int listIndex = holder.getAdapterPosition() + 1 - folderSum;
            holder.setGone(R.id.songListSum, true);
            holder.setGone(R.id.nickName, true);
            holder.setText(R.id.songListSum, String.valueOf(listIndex));
            holder.setText(R.id.audioName, TextUtils.isEmpty(item.getAudioName())
                    ? BaseApplication.getApplication().getResources()
                    .getString(R.string.audio_name_default) : item.getAudioName());
            holder.setText(R.id.nickName, TextUtils.isEmpty(item.getAudioArtistName())
                    ? BaseApplication.getApplication().getResources()
                    .getString(R.string.audio_name_default) : item.getAudioArtistName());
            SongListMarquee audioName = holder.getView(R.id.audioName);
            if (null != audioName) {
                audioName.start();
            }
            SongListMarquee nickName = holder.getView(R.id.nickName);
            if (null != nickName) {
                nickName.start();
            }
            LogUtils.logD(TAG, "mSelected:" + mSelected + " ,position:"
                    + holder.getAdapterPosition() + " ,listIndex:" + listIndex);
            if (mSelected == MusicConstants.DEFAULT_PLAYER_INDEX) {
                mSelected = mSelected + folderSum;
            }
            if (holder.getLayoutPosition() == mSelected && mCurrentAudioId == item.getAudioId()) {
                holder.itemView.setBackgroundColor(BaseApplication.getApplication()
                        .getResources().getColor(R.color.light_black));
                if (mIsPlay) {
                    setPlayingVisiable(holder, true);
                } else {
                    setPlayingVisiable(holder, false);
                }
            } else {
                setPlayingVisiable(holder, false);
                holder.itemView.setBackgroundColor(BaseApplication.getApplication().getResources()
                        .getColor(R.color.color_transparent));
            }
            Glide.with(holder.itemView.getContext()).load(
                    item.getAudioPath())
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                    .placeholder(R.drawable.usb_music_ic_item_no_album)
                    .centerCrop()
                    .into((ImageView) holder.itemView.findViewById(R.id.songListLeftIcon));
        }
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
    protected void convert(BaseRecyclerHolder holder, AudioInfoBean item, List<Object> payloads) {
        LogUtils.logD(TAG, "AudioId:" + item.getAudioId() + ", name:"
                + item.getAudioName() + ":: payloads");
        if (null != payloads && payloads.size() > 0) {
            for (Object payload : payloads) {
                String data = String.valueOf(payload);
                if (String.valueOf(R.id.songListRightIcon).equals(data)) {
                    LogUtils.logD(TAG, "convert :: update :: playing");
                    int folderSum = MusicUtils.getInstance().getAudioSum(getData());
                    int listIndex = holder.getAdapterPosition() + 1 - folderSum;
                    ImageView imageView = (ImageView) holder.getView(R.id.songListRightIcon);
                    LogUtils.logD(TAG, "mSelected:" + mSelected + " ,position:"
                            + holder.getAdapterPosition() + " ,listIndex:" + listIndex);
                    if (mSelected == MusicConstants.DEFAULT_PLAYER_INDEX) {
                        mSelected = mSelected + folderSum;
                    }
                    if (holder.getLayoutPosition() == mSelected
                            && mCurrentAudioId == item.getAudioId()) {
                        holder.itemView.setBackgroundColor(BaseApplication.getApplication()
                                .getResources().getColor(R.color.light_black));
                        if (mIsPlay) {
                            setPlayingVisiable(holder, true);
                        } else {
                            setPlayingVisiable(holder, false);
                        }
                    } else {
                        setPlayingVisiable(holder, false);
                        holder.itemView.setBackgroundColor(BaseApplication.getApplication()
                                .getResources().getColor(R.color.color_transparent));
                    }
                }
            }
        }
    }

    /**
     * Set Music player Manager and current playing index.
     *
     * @param isPlay   UsbMusicManager
     * @param position current playing index
     */
    public void setMusicPlayerManager(boolean isPlay, int position, long audioId) {
        LogUtils.logD(TAG, "setMusicPlayerManager :: invoke :: isPlay :"
                + isPlay + " : position :: " + position + " : audioId :" + audioId
                + " mLastSelected :" + mLastSelected
                + " mCurrentAudioId : " + mCurrentAudioId);
        mSelected = position;
        if (mLastSelected > -1 && mLastSelected != mSelected) {
            notifyItemChanged(mLastSelected, R.id.songListRightIcon);
        }
        if (mCurrentAudioId != audioId || mIsPlay != isPlay) {
            notifyItemChanged(mSelected, R.id.songListRightIcon);
        }
        mIsPlay = isPlay;
        mCurrentAudioId = audioId;
        mLastSelected = mSelected;
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
