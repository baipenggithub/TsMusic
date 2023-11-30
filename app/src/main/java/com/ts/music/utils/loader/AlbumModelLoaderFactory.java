package com.ts.music.utils.loader;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;

import java.nio.ByteBuffer;

public class AlbumModelLoaderFactory implements ModelLoaderFactory<String, ByteBuffer> {
    @NonNull
    @Override
    public ModelLoader<String, ByteBuffer> build(@NonNull MultiModelLoaderFactory multiFactory) {
        return new AlbumModeLoader();
    }

    @Override
    public void teardown() {
        //Do nothing
    }
}
