package com.ts.music.utils.loader;

import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;
import com.ts.music.base.BaseApplication;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class AlbumDataFetcher implements DataFetcher<ByteBuffer> {
    private final String mModel;

    AlbumDataFetcher(String model) {
        mModel = model;
    }

    @Override
    public void loadData(@NonNull Priority priority,
                         @NonNull DataCallback<? super ByteBuffer> callback) {
        if (TextUtils.isEmpty(mModel)) {
            return;
        }
        MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
        try {
            Uri selectedAudio = Uri.fromFile(new File(mModel));
            byte[] embedded = null;
            if (null != selectedAudio) {
                metadataRetriever.setDataSource(BaseApplication.getApplication(), selectedAudio);
                embedded = metadataRetriever.getEmbeddedPicture();
                if (embedded != null) {
                    ByteBuffer byteBuffer = ByteBuffer.wrap(embedded);
                    if (byteBuffer != null) {
                        callback.onDataReady(byteBuffer);
                    }
                }
            }
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            callback.onLoadFailed(ex);
        } finally {
            try {
                metadataRetriever.release();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    @Override
    public void cleanup() {
        // Do nothing
    }

    @Override
    public void cancel() {
        // Do nothing
    }

    @NonNull
    @Override
    public Class<ByteBuffer> getDataClass() {
        return ByteBuffer.class;
    }

    @NonNull
    @Override
    public DataSource getDataSource() {
        return DataSource.LOCAL;
    }
}
