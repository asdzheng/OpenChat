package com.asdzheng.openchat;

import android.app.Application;
import android.content.Context;

import com.asdzheng.openchat.net.OpenClient;
import com.asdzheng.openchat.util.PreferencesManager;
import com.bluewhaleyt.common.DynamicColorsUtil;

public class App extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        DynamicColorsUtil.setDynamicColorsIfAvailable(this);
        if(!PreferencesManager.getOpenAIAPIKey().isEmpty()) {
            OpenClient.INSTANCE.build(PreferencesManager.getOpenAIAPIKey());
        }
    }

    public static Context getContext() {
        return context;
    }

}