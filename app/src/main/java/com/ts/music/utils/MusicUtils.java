package com.ts.music.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.ts.sdk.media.bean.AudioInfoBean;
import com.ts.music.R;
import com.ts.music.base.BaseApplication;
import com.ts.music.constants.MusicConstants;
import com.ts.music.entity.RecordAudioInfo;
import com.ts.music.entity.UsbMusicListBean;
import com.ts.music.preferences.SharedPreferencesHelps;
import com.ts.music.ui.adapter.SongListAdapter;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MusicUtils.
 */
public class MusicUtils {
    private static final String TIME_FORMAT_HOUR_MINUTE_SECOND = "%d:%02d:%02d";
    private static final String TIME_FORMAT_HOUR_MINUTE = "%d:%02d";
    private static final String INITIAL_TIME = "0:00";
    private static volatile MusicUtils sInstance;
    private static final String PATH_SEPARATOR = "/";
    private static final String TAG = "MusicUtils";
    private static final int USB_MIN_DEPTH = 3;

    /**
     * Initialization.
     */
    public static MusicUtils getInstance() {
        if (null == sInstance) {
            synchronized (MusicUtils.class) {
                if (null == sInstance) {
                    sInstance = new MusicUtils();
                }
            }
        }
        return sInstance;
    }

    /**
     * Set split line.
     *
     * @param recyclerView RecyclerView
     */
    public void setRecyclerViewDivider(RecyclerView recyclerView) {
        try {
            DividerItemDecoration divider = new DividerItemDecoration(
                    BaseApplication.getApplication(), DividerItemDecoration.VERTICAL);
            divider.setDrawable(BaseApplication.getApplication()
                    .getResources().getDrawable(R.drawable.shape_recycler_view_line));
            recyclerView.addItemDecoration(divider);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Duration format.
     *
     * @param timeMs Unit millisecond
     * @return Formatted string time
     */
    public String stringForAudioTime(long timeMs) {
        if (timeMs <= 0 || timeMs >= MusicConstants.FORMATTER_HOUR
                * MusicConstants.FORMATTER_MINUTE
                * MusicConstants.FORMATTER_MINUTE
                * MusicConstants.FORMATTER_SECOND) {
            return INITIAL_TIME;
        }
        long totalSeconds = timeMs / MusicConstants.FORMATTER_SECOND;
        int seconds = (int) (totalSeconds % MusicConstants.FORMATTER_MINUTE);
        int minutes = (int) ((totalSeconds / MusicConstants.FORMATTER_MINUTE)
                % MusicConstants.FORMATTER_MINUTE);
        int hours = (int) (totalSeconds / MusicConstants.FORMATTER_MILLISECOND);
        StringBuilder stringBuilder = new StringBuilder();
        Formatter formatter = new Formatter(stringBuilder, Locale.getDefault());
        if (hours > 0) {
            return formatter.format(TIME_FORMAT_HOUR_MINUTE_SECOND, hours, minutes, seconds)
                    .toString();
        } else {
            return formatter.format(TIME_FORMAT_HOUR_MINUTE, minutes, seconds).toString();
        }
    }

    /**
     * Return resources according to playback modeID.
     *
     * @param playerModel Playback mode
     * @return Resources　ICON
     */
    public int getPlayerModelToWhiteRes(int playerModel) {
        if (playerModel == MusicConstants.MUSIC_MODEL_LOOP) {
            return R.drawable.list_loop;
        } else if (playerModel == MusicConstants.MUSIC_MODEL_SINGLE) {
            return R.drawable.music_ic_model_signle;
        } else if (playerModel == MusicConstants.MUSIC_MODEL_RANDOM) {
            return R.drawable.music_ic_random_player;
        }
        return R.drawable.list_loop;
    }

    /**
     * Return text according to playback mode.
     *
     * @param context     Context
     * @param playerModel Play mode
     * @return Schema description.
     */
    public String getPlayerModelToString(Context context, int playerModel) {
        if (playerModel == MusicConstants.MUSIC_MODEL_LOOP) {
            return context.getResources().getString(R.string.text_play_mode_list_loop);
        } else if (playerModel == MusicConstants.MUSIC_MODEL_SINGLE) {
            return context.getResources().getString(R.string.text_play_mode_single_loop);
        } else if (playerModel == MusicConstants.MUSIC_MODEL_RANDOM) {
            return context.getResources().getString(R.string.text_play_mode_random);
        }
        return context.getResources().getString(R.string.text_play_mode_list_loop);
    }

    /**
     * search keyword matching highlights .
     *
     * @param text    text
     * @param keyword keyword
     * @return spannableString
     */
    public SpannableString matcherSearchText(String text, String keyword) {
        if (null == text || text.isEmpty()) {
            text = BaseApplication.getApplication().getResources().getString(
                    R.string.audio_name_default);
        }
        SpannableString spannableString = new SpannableString(text);
        if (null != keyword && !keyword.isEmpty()) {
            Pattern pattern = Pattern.compile(keyword.toLowerCase());
            Matcher matcher = pattern.matcher(new SpannableString(text.toLowerCase()));
            while (matcher.find()) {
                int star = matcher.start();
                int end = matcher.end();
                spannableString.setSpan(new ForegroundColorSpan(
                                BaseApplication.getApplication().getResources()
                                        .getColor(R.color.top_tab_bar_light)),
                        star, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return spannableString;
    }

    /**
     * Highlight the current folder path.
     */
    public SpannableString match(String text) {
        int start = text.lastIndexOf("/");
        int end = text.length();
        SpannableString spannableString = new SpannableString(text);
        spannableString.setSpan(new ForegroundColorSpan(
                        BaseApplication.getApplication().getResources()
                                .getColor(R.color.top_tab_bar_light)), start, end,
                 Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

    /**
     * Return to the playing position.
     *
     * @param audioInfo Data set
     * @param musicId   Audio ID
     * @return index
     */
    public int getCurrentPlayIndex(List<AudioInfoBean> audioInfo, long musicId, String path) {
        if (!TextUtils.isEmpty(path) && null != audioInfo && audioInfo.size() > 0) {
            for (int i = 0; i < audioInfo.size(); i++) {
                if (musicId == audioInfo.get(i).getAudioId()
                        || path.equals(audioInfo.get(i).getAudioPath())) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Get music cover.
     *
     * @param url                Audio path
     * @param isListDefaultCover true:List default cover ; false：Player default cover
     * @return Cover bitmap
     */
    public Bitmap getAlbumArt(String url, boolean isListDefaultCover) {
        if (TextUtils.isEmpty(url)) {
            return getDefBitmap(isListDefaultCover);
        }
        Bitmap bitmap = null;
        MediaMetadataRetriever  metadataRetriever = new MediaMetadataRetriever();
        try {
            Uri selectedAudio = Uri.fromFile(new File(url));
            if (null != selectedAudio) {
                metadataRetriever.setDataSource(BaseApplication.getApplication(), selectedAudio);
                byte[] embedded = metadataRetriever.getEmbeddedPicture();
                if (embedded != null) {
                    bitmap = BitmapFactory.decodeByteArray(embedded, 0, embedded.length);
                    if (null == bitmap) {
                        bitmap = getDefBitmap(isListDefaultCover);
                    }
                } else {
                    bitmap = getDefBitmap(isListDefaultCover);
                }
                return bitmap;
            }
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            bitmap = getDefBitmap(isListDefaultCover);
            LogUtils.logD(TAG, "Path to the file:" + ex.getMessage());
        } finally {
            try {
                metadataRetriever.release();
            } catch (RuntimeException | IOException ex) {
                ex.printStackTrace();
            }
        }
        return bitmap;
    }

    private Bitmap getDefBitmap(boolean isListDefaultCover) {
        Bitmap bitmap;
        if (isListDefaultCover) {
            bitmap = BitmapFactory.decodeResource(BaseApplication.getApplication()
                    .getResources(), R.drawable.icon_usb_item_def);
        } else {
            bitmap = BitmapFactory.decodeResource(BaseApplication.getApplication()
                    .getResources(), R.drawable.icon_usb_item_def);
        }
        return bitmap;
    }

    /**
     * Set recycler view offset.
     *
     * @param rlv      RecyclerView
     * @param adapter  SongListAdapter
     * @param position index
     */
    public int setRecyclerViewPositionOffset(RecyclerView rlv, SongListAdapter adapter,
                                             int position) {
        int offsetPosition;
        int firstPosition = 0;
        int lastPosition = 0;
        //Get firstPosition,lastPosition
        LinearLayoutManager manager = (LinearLayoutManager) rlv.getLayoutManager();
        if (null != manager) {
            firstPosition = manager.findFirstVisibleItemPosition();
            lastPosition = manager.findLastVisibleItemPosition();
        }
        int maxPosition = adapter.getItemCount() - 1;
        //position offset
        int offset = (lastPosition - firstPosition) / MusicConstants.RECYCLE_VIEW_OFFSET;
        if (firstPosition > position) {
            offsetPosition = position - offset;
        } else if (lastPosition < position) {
            offsetPosition = position + offset;
        } else {
            if (lastPosition - position > position - firstPosition) {
                offsetPosition = position - offset;
            } else {
                offsetPosition = position + offset;
            }
        }
        if (offsetPosition < 0) {
            offsetPosition = 0;
        } else if (offsetPosition > maxPosition) {
            offsetPosition = maxPosition;
        }
        return offsetPosition;
    }

    /**
     * Filter audio files.
     *
     * @param audioInfoBeans Audio list
     * @param targetDir      Filter condition
     * @return Filter result
     */
    public UsbMusicListBean filterAudio(List<AudioInfoBean> audioInfoBeans, String targetDir) {
        if (targetDir.isEmpty() || null == audioInfoBeans || audioInfoBeans.size() == 0) {
            return null;
        }
        // Add corresponding data according to USB road strength
        List<AudioInfoBean> usbSourceData = new ArrayList<>();
        for (int i = 0; i < audioInfoBeans.size(); i++) {
            if (audioInfoBeans.get(i).getAudioPath().contains(targetDir)) {
                if (null == audioInfoBeans.get(i).getAudioName()
                        || TextUtils.isEmpty(audioInfoBeans.get(i).getAudioName())) {
                    audioInfoBeans.get(i).setAudioName(BaseApplication.getApplication()
                            .getResources().getString(R.string.audio_name_default));
                }
                usbSourceData.add(audioInfoBeans.get(i));
            }
        }
        SortUtils.sortAudioListData(usbSourceData);
        UsbMusicListBean usbMusicListBean = new UsbMusicListBean();
        usbMusicListBean.setCurrentPath(targetDir);
        usbMusicListBean.setAudioList(usbSourceData);
        return usbMusicListBean;
    }

    /**
     * Filter audio files.
     *
     * @param audioInfoBeans Audio list
     * @param targetDir      Filter condition
     * @return Filter result
     */
    public UsbMusicListBean filterFolderAudio(List<AudioInfoBean> audioInfoBeans,
                                              String targetDir) {
        if (targetDir.isEmpty() || null == audioInfoBeans || audioInfoBeans.size() == 0) {
            return null;
        }
        LogUtils.logD(TAG, "filterAudio targetDir: " + targetDir + ", count: "
                + audioInfoBeans.size());
        int targetDirDepth = targetDir.split(PATH_SEPARATOR).length;
        LogUtils.logD(TAG, "filterAudio targetDir depth: " + targetDirDepth);
        if (targetDirDepth < USB_MIN_DEPTH) {
            return null;
        }
        Iterator<AudioInfoBean> iterator = audioInfoBeans.iterator();
        List<AudioInfoBean> resultAudios = new ArrayList<>();
        List<AudioInfoBean> resultFolders = new ArrayList<>();
        List<String> folderNames = new ArrayList<>();
        UsbMusicListBean usbMusicListBean = new UsbMusicListBean();
        AudioInfoBean bean;
        while (iterator.hasNext()) {
            bean = iterator.next();
            String path = bean.getAudioPath();
            if (path.contains(targetDir)) {
                String[] tmp = path.split(PATH_SEPARATOR);
                if (tmp.length < USB_MIN_DEPTH) {
                    continue;
                }
                StringBuffer pathBuffer = new StringBuffer();
                boolean isContain = false;
                for (int index = 0; index < tmp.length - 1; index++) {
                    if (index != 0) {
                        pathBuffer.append(PATH_SEPARATOR);
                    }
                    pathBuffer.append(tmp[index]);
                    if (pathBuffer.toString().equals(targetDir) || isContain) {
                        isContain = true;
                        String[] strings = path.split(PATH_SEPARATOR);
                        int depth = strings.length;
                        if (depth == targetDirDepth + 1) {  // music
                            LogUtils.logD(TAG, "filterAudio add music: " + path);
                            resultAudios.add(bean);
                        }
                        if (depth > targetDirDepth + 1) {  // folder
                            AudioInfoBean dir = new AudioInfoBean();
                            String dirName = strings[targetDirDepth];
                            String dirPath = targetDir + PATH_SEPARATOR + dirName;
                            dir.setAudioName(dirName);
                            dir.setAudioPath(dirPath);
                            dir.setAudioId(MusicConstants.AUDIO_FOLDER_ID);
                            if (!folderNames.contains(dirPath)) {
                                LogUtils.logD(TAG, "filterAudio add folder: " + dirPath);
                                folderNames.add(dirPath);
                                resultFolders.add(dir);
                            }
                        }
                    }
                }
            }
        }
        SortUtils.sortAudioListData(resultFolders);
        usbMusicListBean.setCurrentPath(targetDir);
        usbMusicListBean.setFolderList(resultFolders);
        SortUtils.sortAudioListData(resultAudios);
        usbMusicListBean.setAudioList(resultAudios);
        return usbMusicListBean;
    }

    /**
     * Record playback progress.
     *
     * @param recordAudioInfo RecordInfo
     */
    public void saveAudioPlaybackInfo(RecordAudioInfo recordAudioInfo) {
        LogUtils.logD(TAG, "saveAudioPlaybackInfo :: invoke ");
        Gson gson = new Gson();
        RecordAudioInfo usbFirst = gson.fromJson(SharedPreferencesHelps
                        .getObjectData(MusicConstants.USB_FIRST_UUID),
                RecordAudioInfo.class);
        RecordAudioInfo usbSecond = gson.fromJson(SharedPreferencesHelps
                        .getObjectData(MusicConstants.USB_SECOND_UUID),
                RecordAudioInfo.class);
        LogUtils.logD(TAG, "saveAudioPlaybackInfo :: usbFirst :"
                + usbFirst + " : usbSecond : " + usbSecond);
        if (null == usbSecond) {
            LogUtils.logD(TAG, "saveAudioPlaybackInfo :: usbSecond  isEmpty");
            if (null == usbFirst) {
                LogUtils.logD(TAG, "saveAudioPlaybackInfo :: usbFirst isEmpty ");
                SharedPreferencesHelps
                        .setObjectData(MusicConstants.USB_FIRST_UUID, recordAudioInfo);
            } else if (usbFirst.getUuid().equals(recordAudioInfo.getUuid())) {
                LogUtils.logD(TAG, "saveAudioPlaybackInfo :: usbFirst same ");
                SharedPreferencesHelps
                        .setObjectData(MusicConstants.USB_FIRST_UUID, recordAudioInfo);
            } else {
                LogUtils.logD(TAG, "saveAudioPlaybackInfo :: usbFirst diff ");
                SharedPreferencesHelps
                        .setObjectData(MusicConstants.USB_SECOND_UUID, recordAudioInfo);
            }
        } else if (usbSecond.getUuid().equals(recordAudioInfo.getUuid())) {
            LogUtils.logD(TAG, "saveAudioPlaybackInfo :: usbSecond same");
            SharedPreferencesHelps.setObjectData(MusicConstants.USB_SECOND_UUID, recordAudioInfo);
        } else {
            LogUtils.logD(TAG, "saveAudioPlaybackInfo :: usbFirst same :: change ");
            SharedPreferencesHelps.setObjectData(MusicConstants.USB_FIRST_UUID, usbSecond);
            SharedPreferencesHelps.setObjectData(MusicConstants.USB_SECOND_UUID, recordAudioInfo);
        }
    }

    public String getUuidFromPath(String path) {
        String[] strings = path.split(MusicConstants.FILE_SIGN);
        return strings[MusicConstants.UUID_INDEX];
    }

    /**
     * Get the total number of current U disk songs.
     *
     * @param audioInfoBeans Scan results
     * @return Total
     */
    public int getAudioSum(List<AudioInfoBean> audioInfoBeans) {
        int sum = 0;
        if (null == audioInfoBeans || audioInfoBeans.size() == 0) {
            return sum;
        }
        // Add corresponding data according to USB road strength
        for (int i = 0; i < audioInfoBeans.size(); i++) {
            if (audioInfoBeans.get(i).getAudioId() == MusicConstants.AUDIO_FOLDER_ID) {
                sum++;
            }
        }
        return sum;
    }

    /**
     * Get all audio information of U disk.
     *
     * @param audioInfoBeans Audio list
     * @param targetDir      Filter condition
     * @return Filter result
     */
    public List<AudioInfoBean> getAllAudio(List<AudioInfoBean> audioInfoBeans, String targetDir) {
        if (targetDir.isEmpty() || null == audioInfoBeans || audioInfoBeans.size() == 0) {
            return null;
        }
        // Add corresponding data according to USB road strength
        List<AudioInfoBean> usbSourceData = new ArrayList<>();
        for (int i = 0; i < audioInfoBeans.size(); i++) {
            if (audioInfoBeans.get(i).getAudioPath().contains(targetDir)) {
                usbSourceData.add(audioInfoBeans.get(i));
            }
        }
        SortUtils.sortAudioListData(usbSourceData);
        return usbSourceData;
    }

    /**
     * Check file exist or not.
     */
    public boolean checkFileExist(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

    /**
     * Get previous level path.
     */
    public String getPreviousLevelPath(String path) {
        LogUtils.logD(TAG, "getPreviousLevelPath :: invoke :: path" + path);
        if (path.split(PATH_SEPARATOR).length > 2) {
            path = new StringBuffer(path).substring(0, path.lastIndexOf(PATH_SEPARATOR));
        }
        return path;
    }
}
