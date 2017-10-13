package com.openproject.xiaojie.android_nfc;

import android.app.Application;

import com.openproject.xiaojie.android_nfc.nfc.Utils;

/**
 * Created by lenovo on 10/13.
 */

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);
    }
}
