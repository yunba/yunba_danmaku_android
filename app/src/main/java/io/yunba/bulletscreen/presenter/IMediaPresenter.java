package io.yunba.bulletscreen.presenter;

/**
 * Created by longmiao on 16-12-25.
 */
public interface IMediaPresenter {

    void onCreate(int isLiveStreaming, int iCodec);
    void onResume();
    void onPause();
    void onDestroy();
    void pauseDisplay();
}
