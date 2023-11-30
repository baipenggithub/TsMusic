package com.ts.music.utils.loader;


import android.media.MediaFile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.signature.ObjectKey;

import java.nio.ByteBuffer;

public class AlbumModeLoader implements ModelLoader<String, ByteBuffer> {
    @Nullable
    @Override
    public LoadData buildLoadData(@NonNull String path, int width,
                                  int height, @NonNull Options options) {
        return new LoadData<>(new ObjectKey(path), new AlbumDataFetcher(path));
    }

    @Override
    public boolean handles(@NonNull String path) {
        int fileType = 0;
        String mimeType = MediaFile.getMimeTypeForFile(path);
        if (mimeType != null) {
            fileType = MediaFile.getFileTypeForMimeType(mimeType);
        }
        if (fileType == 0) {
            MediaFile.MediaFileType mediaFileType = MediaFile.getFileType(path);
            if (mediaFileType != null) {
                fileType = mediaFileType.fileType;
            }
        }
        return MediaFile.isAudioFileType(fileType);
    }
}
