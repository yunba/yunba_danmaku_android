package io.yunba.bulletscreen.message;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import io.yunba.android.manager.YunBaManager;
import io.yunba.bulletscreen.Constant;
import io.yunba.bulletscreen.presenter.LiveVideoPresenter;

/**
 * Created by miao on 2016/12/21.
 */

public class DanmakuReceiver extends BroadcastReceiver {
    public static LiveVideoPresenter mLiveVideoPresenter;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (YunBaManager.MESSAGE_RECEIVED_ACTION.equals(intent.getAction())) {
            if (null != mLiveVideoPresenter) {
                mLiveVideoPresenter.processCustomMessage(context, intent);
            }
        } else if (YunBaManager.PRESENCE_RECEIVED_ACTION.equals(intent.getAction())) {
            //msg from presence.
        }
    }
}
