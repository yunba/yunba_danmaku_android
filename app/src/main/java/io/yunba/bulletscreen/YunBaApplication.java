package io.yunba.bulletscreen;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.beardedhen.androidbootstrap.TypefaceProvider;

import io.yunba.android.manager.YunBaManager;

/**
 * Created by miao on 2016/12/21.
 */

public class YunBaApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        TypefaceProvider.registerDefaultIconSets();
        startYunBaService();
    }

    private void startYunBaService() {
        YunBaManager.setThirdPartyEnable(getApplicationContext(), true);
        YunBaManager.start(getApplicationContext());
    }
}
