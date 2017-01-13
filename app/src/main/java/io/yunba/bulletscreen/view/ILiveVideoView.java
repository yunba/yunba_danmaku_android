package io.yunba.bulletscreen.view;

import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;

/**
 * Created by longmiao on 16-12-24.
 */
public interface ILiveVideoView {
    void addDanmaku(String text, boolean islive);
    void addDanmaku(String text, int color, int mode, int dur, boolean isLive);
    void prepareDanmaku(BaseDanmakuParser parser, DanmakuContext danmakuContext);
    void setOnlineNum(int onlineNum);
    void onEnterLiveVideoError();
    void onEnterLiveVideoSuc();
    void onPubDanmakuError();
    void onPubDanmakuSuc();
    void setAgreeNum(int num);
    void onAgreeNumInc();
    void onOnlineChanged(String alias, String action);
    void onAgreeFail();
}
