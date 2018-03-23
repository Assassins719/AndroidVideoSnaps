package com.example.abc.myapplication;

import android.app.Application;
import android.content.Context;

import com.danikula.videocache.HttpProxyCacheServer;

/**
 * Created by ABC on 3/11/2018.
 */
public class App extends Application {

        private HttpProxyCacheServer proxy;

    public static HttpProxyCacheServer getProxy(Context context) {
        Application app = (Application) context.getApplicationContext();
        return newProxy(context);
//        return app.proxy == null ? (app.proxy = app.newProxy()) : app.proxy;
    }

    private static HttpProxyCacheServer newProxy(Context context) {
        return new HttpProxyCacheServer(context);
    }
//    private HttpProxyCacheServer proxy;
//
//    public static HttpProxyCacheServer getProxy(Context context) {
//        App app = (App) context.getApplicationContext();
//        return app.proxy == null ? (app.proxy = app.newProxy()) : app.proxy;
//    }
//
//    private HttpProxyCacheServer newProxy() {
//        return new HttpProxyCacheServer.Builder(this)
//                .cacheDirectory(Utils.getVideoCacheDir(this))
//                .build();
//    }
}
