package io.yunba.bulletscreen.presenter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.HashMap;

import io.yunba.android.manager.YunBaManager;
import io.yunba.bulletscreen.Constant;
import io.yunba.bulletscreen.view.ILiveVideoView;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.IDisplayer;
import master.flame.danmaku.danmaku.model.android.BaseCacheStuffer;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.model.android.SpannedCacheStuffer;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;

/**
 * Created by longmiao on 16-12-25.
 */
public class LiveVideoPresenter implements ILiveVideoPresenter {
    private Context mContext;
    private ILiveVideoView mLiveVideoView;
    private BaseDanmakuParser mDanmakuParser;
    private DanmakuContext mDanmakuContext;
    private YunBaMessageReceiver mMessageReceiver;
    private static final int HANDLER_TEST = 0x500;
    private static final int HANDLER_SUB_SUC = 0x100;
    private static final int HANDLER_SUB_ERR = 0x101;
    private static final int HANDLER_PUB_SUC = 0x102;
    private static final int HANDLER_PUB_ERR = 0x103;
    private static final int HANDLER_ONLINE_NUM = 0x104;
    private static final int HANDLER_AGREE_NUM = 0x105;
    private static final int HANDLER_AGREE_FAIL = 0x106;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case HANDLER_TEST:
                    Toast.makeText(mContext, "success", Toast.LENGTH_SHORT).show();
                    break;
                case HANDLER_SUB_SUC:
                    mLiveVideoView.onEnterLiveVideoSuc();
//                    getNumberOfOnline();
//                    getAgreeCount();
                    break;
                case HANDLER_SUB_ERR:
                    mLiveVideoView.onEnterLiveVideoError();
                    break;
                case HANDLER_PUB_SUC:
                    mLiveVideoView.onPubDanmakuSuc();
                    break;
                case HANDLER_PUB_ERR:
                    mLiveVideoView.onPubDanmakuError();
                    break;
                case HANDLER_AGREE_FAIL:
                    mLiveVideoView.onAgreeFail();
                    break;
                case HANDLER_ONLINE_NUM:
                    mLiveVideoView.setOnlineNum(msg.arg1);
                    break;
                case HANDLER_AGREE_NUM:
                    mLiveVideoView.setAgreeNum(msg.arg1);
                    break;
                default:
            }
        }
    };

    private BaseCacheStuffer.Proxy mCacheStufferAdapter = new BaseCacheStuffer.Proxy() {

        @Override
        public void prepareDrawing(BaseDanmaku danmaku, boolean fromWorkerThread) {

        }

        @Override
        public void releaseResource(BaseDanmaku danmaku) {

        }
    };

    public LiveVideoPresenter(Context context, ILiveVideoView view) {
        this.mContext = context;
        this.mLiveVideoView = view;
    }

    @Override
    public void onCreate() {
        // 设置最大显示行数
        HashMap<Integer, Integer> maxLinesPair = new HashMap<Integer, Integer>();
        maxLinesPair.put(BaseDanmaku.TYPE_SCROLL_RL, 5); // 滚动弹幕最大显示5行
        // 设置是否禁止重叠
        HashMap<Integer, Boolean> overlappingEnablePair = new HashMap<Integer, Boolean>();
        overlappingEnablePair.put(BaseDanmaku.TYPE_SCROLL_RL, true);
        overlappingEnablePair.put(BaseDanmaku.TYPE_FIX_TOP, true);
        mDanmakuContext = DanmakuContext.create();
        mDanmakuContext.setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN, 3).setDuplicateMergingEnabled(false).setScrollSpeedFactor(1.2f).setScaleTextSize(1.2f)
                .setCacheStuffer(new SpannedCacheStuffer(), mCacheStufferAdapter) // 图文混排使用SpannedCacheStuffer
                .setMaximumLines(maxLinesPair)
                .preventOverlapping(overlappingEnablePair);
        mDanmakuParser = createParser(null);
        mLiveVideoView.prepareDanmaku(mDanmakuParser, mDanmakuContext);
        Constant.ALIAS = String.valueOf(System.currentTimeMillis());
        subscribe();
//        registerMessageReceiver();
    }

    @Override
    public void reconnect() {
        subscribe();
    }

    private void subscribe() {
        YunBaManager.subscribe(mContext, Constant.DANMAKU_STAT_TOPIC, null);
        YunBaManager.setAlias(mContext, Constant.ALIAS, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {
                YunBaManager.subscribe(mContext, Constant.DANMAKU_TOPIC, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken iMqttToken) {
                        YunBaManager.subscribe(mContext, Constant.DANMAKU_AGREE_TOPIC, new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken iMqttToken) {
                                YunBaManager.subscribe(mContext, Constant.DANMAKU_STAT_TOPIC, new IMqttActionListener() {
                                    @Override
                                    public void onSuccess(IMqttToken iMqttToken) {
                                        handler.sendEmptyMessage(HANDLER_SUB_SUC);
//                                        handler.sendEmptyMessage(HANDLER_TEST);
                                    }

                                    @Override
                                    public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                                        handler.sendEmptyMessage(HANDLER_SUB_ERR);
                                    }
                                });
                            }

                            @Override
                            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                                handler.sendEmptyMessage(HANDLER_SUB_ERR);
                            }
                        });
                    }

                    @Override
                    public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                        handler.sendEmptyMessage(HANDLER_SUB_ERR);
                    }
                });
            }

            @Override
            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                handler.sendEmptyMessage(HANDLER_SUB_ERR);
            }
        });
    }

    @Override
    public void onResume() {
    }

    @Override
    public void onPause() {
    }

    @Override
    public void onDestroy() {
        YunBaManager.unsubscribe(mContext, Constant.DANMAKU_TOPIC, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {
            }

            @Override
            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
            }
        });
//        unregisterMessageReceiver();
    }

    @Override
    public void publishDanmaku(String text) {
        JSONObject toSend = new JSONObject();
        try {
            toSend.put("mode", 2);
            toSend.put("text", text);
            toSend.put("color", 0xFFFF0000);
            toSend.put("dur", 4000);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        YunBaManager.publish(mContext, Constant.DANMAKU_TOPIC, toSend.toString(), new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {
                handler.sendEmptyMessage(HANDLER_PUB_SUC);
            }

            @Override
            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                handler.sendEmptyMessage(HANDLER_PUB_ERR);
            }
        });
    }

    @Override
    public void agree() {
        YunBaManager.publish(mContext, Constant.DANMAKU_AGREE_TOPIC, "like", new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {

            }

            @Override
            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                handler.sendEmptyMessage(HANDLER_AGREE_FAIL);
            }
        });
    }

    private BaseDanmakuParser createParser(InputStream stream) {
        return new BaseDanmakuParser() {
            @Override
            protected IDanmakus parse() {
                return new Danmakus();
            }
        };
    }

    public void processCustomMessage(Context context, Intent yunbaIntent) {
        if (YunBaManager.MESSAGE_RECEIVED_ACTION.equals(yunbaIntent.getAction())) {
            String topic = yunbaIntent.getStringExtra(YunBaManager.MQTT_TOPIC);
            String msg = yunbaIntent.getStringExtra(YunBaManager.MQTT_MSG);
//            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            if (Constant.DANMAKU_TOPIC.equals(topic)) {
                try {
                    JSONObject json = new JSONObject(msg);
                    String entity = json.getString("text");
                    int mode = json.getInt("mode");
                    int color = json.getInt("color");
                    int dur = json.getInt("dur");
                    mLiveVideoView.addDanmaku(entity, color, mode, dur, false);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (Constant.DANMAKU_AGREE_TOPIC.equals(topic)) {
                mLiveVideoView.onAgreeNumInc();
            } else if (Constant.ALIAS.equals(topic)) {
                try {
                    JSONObject json = new JSONObject(msg);
                    int likeCount = json.getInt("like");
                    int presence = json.getInt("presence");
                    Message msgPresence = Message.obtain();
                    msgPresence.what = HANDLER_ONLINE_NUM;
                    msgPresence.arg1 = presence;
                    handler.sendMessage(msgPresence);

                    Message msgLike = Message.obtain();
                    msgLike.what = HANDLER_AGREE_NUM;
                    msgLike.arg1 = likeCount;
                    handler.sendMessage(msgLike);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class YunBaMessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constant.MESSAGE_RECEIVED_ACTION)) {
                String topic = intent.getStringExtra(Constant.PARAMS_MESSAGE_TOPIC);
                if (Constant.DANMAKU_TOPIC.equals(topic)) {
                    String showTxt = intent.getStringExtra(Constant.PARAMS_MESSAGE_BODY);
                    try {
                        JSONObject json = new JSONObject(showTxt);
                        String entity = json.getString("text");
                        int mode = json.getInt("mode");
                        int color = json.getInt("color");
                        int dur = json.getInt("dur");
//                        mLiveVideoView.addDanmaku(json.getString("text"), false);
                        mLiveVideoView.addDanmaku(entity, color, mode, dur, false);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (Constant.DANMAKU_AGREE_TOPIC.equals(topic)) {
                    mLiveVideoView.onAgreeNumInc();
                } else if (Constant.ALIAS.equals(topic)) {
                    String showTxt = intent.getStringExtra(Constant.PARAMS_MESSAGE_BODY);
                    try {
                        JSONObject json = new JSONObject(showTxt);
                        int likeCount = json.getInt("like");
                        int presence = json.getInt("presence");
                        Message msg = Message.obtain();
                        msg.what = HANDLER_ONLINE_NUM;
                        msg.arg1 = presence;
                        handler.sendMessage(msg);

                        Message msgLike = Message.obtain();
                        msgLike.what = HANDLER_AGREE_NUM;
                        msgLike.arg1 = likeCount;
                        handler.sendMessage(msg);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            } else if (intent.getAction().equals(Constant.PRESENCE_RECIVED_ACTION)) {
                String topic = intent.getStringExtra(Constant.PARAMS_PRESENCE_TOPIC);
                String action = intent.getStringExtra(Constant.PARAMS_PRESENCE_ACTION);
                String alias = intent.getStringExtra(Constant.PARAMS_PRESENCE_ALIAS);
                if (topic.equals(Constant.DANMAKU_TOPIC)) {
                    mLiveVideoView.onOnlineChanged(alias, action);
                }
            }
        }
    }

    private void registerMessageReceiver() {
        mMessageReceiver = new YunBaMessageReceiver();
        IntentFilter msgfilter = new IntentFilter();
        msgfilter.addAction(Constant.MESSAGE_RECEIVED_ACTION);

        IntentFilter presenceFilter = new IntentFilter();
        presenceFilter.addAction(Constant.PRESENCE_RECIVED_ACTION);
        mContext.registerReceiver(mMessageReceiver, msgfilter);
        mContext.registerReceiver(mMessageReceiver, presenceFilter);
    }

    private void unregisterMessageReceiver() {
        mContext.unregisterReceiver(mMessageReceiver);
    }
}
