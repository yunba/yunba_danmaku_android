package io.yunba.bulletscreen.presenter;

/**
 * Created by longmiao on 16-12-22.
 */
public interface ILiveVideoPresenter {
    void onCreate();
    void onResume();
    void onPause();
    void onDestroy();
    void publishDanmaku(String text);
    void agree();
}
