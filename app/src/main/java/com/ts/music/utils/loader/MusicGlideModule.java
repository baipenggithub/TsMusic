package com.ts.music.utils.loader;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;

import java.nio.ByteBuffer;

@GlideModule
public class MusicGlideModule extends AppGlideModule {

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide,
                                   @NonNull Registry registry) {
        registry.prepend(String.class, ByteBuffer.class, new AlbumModelLoaderFactory());
    }
}
