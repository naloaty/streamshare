package com.naloaty.syncshare.app;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.module.LibraryGlideModule;
import com.naloaty.syncshare.communication.SSOkHttpClient;
import com.naloaty.syncshare.security.SecurityUtils;

import java.io.InputStream;
import java.lang.annotation.Annotation;

import okhttp3.OkHttpClient;

@GlideModule
public class SSOkHttpGlideModule extends AppGlideModule {

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, Registry registry) {
        OkHttpClient client = SSOkHttpClient.getOkHttpClient(context);
        registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(client));
    }
}
