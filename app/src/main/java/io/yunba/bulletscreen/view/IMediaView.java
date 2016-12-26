package io.yunba.bulletscreen.view;

import android.view.SurfaceHolder;

import java.util.HashMap;

/**
 * Created by longmiao on 16-12-25.
 */
public interface IMediaView {
    SurfaceHolder getSurfaceHolder();
    void addCallback(SurfaceHolder.Callback callback);
    void onInfoChange(int mediaCode, HashMap<String, String> metaData);
    void onMediaError(int errCode);
    void onMediaComplete();
}
