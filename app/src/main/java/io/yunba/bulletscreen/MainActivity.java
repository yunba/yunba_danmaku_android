package io.yunba.bulletscreen;

import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.AwesomeTextView;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.beardedhen.androidbootstrap.BootstrapLabel;
import com.beardedhen.androidbootstrap.BootstrapText;
import com.pili.pldroid.player.AVOptions;
import com.pili.pldroid.player.PLMediaPlayer;

import java.util.HashMap;

import io.yunba.bulletscreen.message.DanmakuReceiver;
import io.yunba.bulletscreen.presenter.ILiveVideoPresenter;
import io.yunba.bulletscreen.presenter.IMediaPresenter;
import io.yunba.bulletscreen.presenter.LiveVideoPresenter;
import io.yunba.bulletscreen.presenter.MediaPresenter;
import io.yunba.bulletscreen.ui.CustomResizeSurfaceView;
import io.yunba.bulletscreen.ui.FlowLikeView;
import io.yunba.bulletscreen.view.ILiveVideoView;
import io.yunba.bulletscreen.view.IMediaView;
import master.flame.danmaku.controller.IDanmakuView;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;

public class MainActivity extends AppCompatActivity implements IMediaView, ILiveVideoView {
    public static final String TAG = "iyb.MainActivity";

    private IMediaPresenter mMediaPresenter;
    private ILiveVideoPresenter mLiveVideoPresenter;
    private IDanmakuView mDanmakuView;
    private BootstrapButton mSendCommentBtn;
    private BootstrapButton mAgreeBtn;
    private BootstrapEditText mInputCommentEt;
    private BootstrapLabel mConnStat;
    private BootstrapButton mReconnBtn;
    private AwesomeTextView mOnlineTv;
    private AwesomeTextView mAgreeTv;
    private ImageView mConnIv;
    private TextView mAgreeAnimTv;
    private int mNumberOfOnline = 0;
    private int mNumberOfAgree = 0;
    private View mLoadingView;
    private CustomResizeSurfaceView mSurfaceView;
    private Toast mToast = null;
    private boolean mIsActivityPaused = true;
    private TextView mPauseTv;
    private boolean isPause = false;
    private BaseDanmakuParser mDanmakuParser;
    private DanmakuContext mDanmakuContext;
    private FlowLikeView mFlowLikeView;
    private RelativeLayout mConnStatLayout;
    private RelativeLayout mConnSucLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        mMediaPresenter = new MediaPresenter(this, this);
        mLiveVideoPresenter = new LiveVideoPresenter(this, this);
        DanmakuReceiver.mLiveVideoPresenter = (LiveVideoPresenter) mLiveVideoPresenter;
        mLiveVideoPresenter.onCreate();
        mMediaPresenter.onCreate(getIntent().getIntExtra("liveStreaming", 1),
                getIntent().getIntExtra("mediaCodec", AVOptions.MEDIA_CODEC_HW_DECODE));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLiveVideoPresenter.onResume();
        mMediaPresenter.onResume();
        mIsActivityPaused = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLiveVideoPresenter.onPause();
        mMediaPresenter.onPause();
        mIsActivityPaused = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMediaPresenter.onDestroy();
        mLiveVideoPresenter.onDestroy();
    }

    @Override
    public SurfaceHolder getSurfaceHolder() {
        return mSurfaceView.getHolder();
    }

    @Override
    public void addCallback(SurfaceHolder.Callback callback) {
        mSurfaceView.getHolder().addCallback(callback);
    }

    @Override
    public void onInfoChange(int mediaCode, HashMap<String, String> meta) {
        switch (mediaCode) {
            case PLMediaPlayer.MEDIA_INFO_BUFFERING_START:
                mLoadingView.setVisibility(View.VISIBLE);
                break;
            case PLMediaPlayer.MEDIA_INFO_BUFFERING_END:
            case PLMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                mLoadingView.setVisibility(View.GONE);
                Log.i(TAG, "meta: " + meta.toString());
                break;
            case PLMediaPlayer.MEDIA_INFO_SWITCHING_SW_DECODE:
                Log.i(TAG, "Hardware decoding failure, switching software decoding!");
                break;
            default:
                break;
        }
    }

    @Override
    public void onMediaError(int errCode) {
        Log.e(TAG, "Error happened, errorCode = " + errCode);
        switch (errCode) {
            case PLMediaPlayer.ERROR_CODE_INVALID_URI:
                showToastTips("Invalid URL !");
                break;
            case PLMediaPlayer.ERROR_CODE_404_NOT_FOUND:
                showToastTips("404 resource not found !");
                break;
            case PLMediaPlayer.ERROR_CODE_CONNECTION_REFUSED:
                showToastTips("Connection refused !");
                break;
            case PLMediaPlayer.ERROR_CODE_CONNECTION_TIMEOUT:
                showToastTips("Connection timeout !");
                break;
            case PLMediaPlayer.ERROR_CODE_EMPTY_PLAYLIST:
                showToastTips("Empty playlist !");
                break;
            case PLMediaPlayer.ERROR_CODE_STREAM_DISCONNECTED:
                showToastTips("Stream disconnected !");
                break;
            case PLMediaPlayer.ERROR_CODE_IO_ERROR:
                showToastTips("Network IO Error !");
                break;
            case PLMediaPlayer.ERROR_CODE_UNAUTHORIZED:
                showToastTips("Unauthorized Error !");
                break;
            case PLMediaPlayer.ERROR_CODE_PREPARE_TIMEOUT:
                showToastTips("Prepare timeout !");
                break;
            case PLMediaPlayer.ERROR_CODE_READ_FRAME_TIMEOUT:
                showToastTips("Read frame timeout !");
                break;
            case PLMediaPlayer.ERROR_CODE_HW_DECODE_FAILURE:
            case PLMediaPlayer.MEDIA_ERROR_UNKNOWN:
                break;
            default:
                showToastTips("unknown error !");
                break;
        }
    }

    @Override
    public void onMediaComplete() {
        Log.d(TAG, "Play Completed !");
        showToastTips("Play Completed !");
        optionEnable(false);
    }

    private void showToastTips(final String tips) {
        if (mIsActivityPaused) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mToast != null) {
                    mToast.cancel();
                }
                mToast = Toast.makeText(MainActivity.this, tips, Toast.LENGTH_SHORT);
                mToast.show();
            }
        });
    }

    private void optionEnable(final boolean isEnable) {
        mSendCommentBtn.setEnabled(isEnable);
    }


    @Override
    public void setOnlineNum(int onlineNum) {
        mNumberOfOnline = onlineNum;
        notifyPresenceChanged();
    }

    @Override
    public void setAgreeNum(int num) {
        mNumberOfAgree = num;
        BootstrapText text = new BootstrapText.Builder(getApplicationContext()).addText(getString(R.string.agree_number, String.valueOf(mNumberOfAgree)))
                .build();
        mAgreeTv.setBootstrapText(text);
    }

    @Override
    public void onAgreeNumInc() {
        mAgreeAnimTv.setVisibility(View.GONE);
        mFlowLikeView.addLikeView();
        setAgreeNum(mNumberOfAgree + 1);
    }

    @Override
    public void onOnlineChanged(String alias, String action) {
        if ("join".equals(action)) {
            if (!Constant.ALIAS.equals(alias)) {
                mNumberOfOnline++;
                notifyPresenceChanged();
            }
        } else if ("leave".equals(action)) {
            mNumberOfOnline--;
            notifyPresenceChanged();
        }
    }

    private void init() {
        mLoadingView = findViewById(R.id.LoadingView);
        mSurfaceView = (CustomResizeSurfaceView) findViewById(R.id.SurfaceView);
        mDanmakuView = (IDanmakuView) findViewById(R.id.bulletscreen_view);
        mSendCommentBtn = (BootstrapButton) findViewById(R.id.commentSend_btn);
        mAgreeBtn = (BootstrapButton) findViewById(R.id.agree_btn);
        mInputCommentEt = (BootstrapEditText) findViewById(R.id.commentInput_et);
        mAgreeTv = (AwesomeTextView) findViewById(R.id.agree_tv);
        mAgreeAnimTv = (TextView) findViewById(R.id.animation_tv);
        mOnlineTv = (AwesomeTextView) findViewById(R.id.online_tv);
        mFlowLikeView = (FlowLikeView) findViewById(R.id.flowLikeView);
        mConnStat = (BootstrapLabel) findViewById(R.id.connstat_label);
        mReconnBtn = (BootstrapButton) findViewById(R.id.reconn_btn);
        mConnStatLayout = (RelativeLayout) findViewById(R.id.connStat_layout);
        mConnSucLayout = (RelativeLayout) findViewById(R.id.connSuc_layout);
        mConnIv = (ImageView) findViewById(R.id.loading_icon);
        AnimationDrawable loadingAd = (AnimationDrawable) mConnIv.getDrawable();
        loadingAd.start();
        mSendCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String comment = mInputCommentEt.getText().toString();
                mLiveVideoPresenter.publishDanmaku(comment);
            }
        });
        mAgreeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLiveVideoPresenter.agree();
            }
        });
        mPauseTv = (TextView) findViewById(R.id.videoCtlTv);
        mPauseTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPause) {
                    mPauseTv.setText("暂停");
                    mMediaPresenter.resumeDisplay();
                    isPause = false;
                } else {
                    mPauseTv.setText("开始");
                    mMediaPresenter.pauseDisplay();
                    isPause = true;
                }
            }
        });
    }

    public void addDanmaku(String text, boolean islive) {
        BaseDanmaku danmaku = mDanmakuContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
        if (null == danmaku || null == mDanmakuView) {
            return;
        }
        danmaku.text = text;
        danmaku.padding = 5;
        danmaku.priority = 0;
        danmaku.isLive = islive;
        danmaku.setTime(mDanmakuView.getCurrentTime() + 1200);
        danmaku.textSize = 25f * (mDanmakuParser.getDisplayer().getDensity() - 0.6f);
        danmaku.textColor = Color.RED;
        mDanmakuView.addDanmaku(danmaku);
    }

    @Override
    public void addDanmaku(String text, int color, int mode, int dur, boolean isLive) {
        BaseDanmaku danmaku = mDanmakuContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
        if (null == danmaku || null == mDanmakuView) {
            return;
        }
        danmaku.text = text;
        danmaku.padding = 5;
        danmaku.priority = 0;
        danmaku.isLive = isLive;
        danmaku.setTime(mDanmakuView.getCurrentTime() + dur);
        danmaku.textSize = 25f * (mDanmakuParser.getDisplayer().getDensity() - 0.6f);
        danmaku.textColor = color;
        danmaku.textShadowColor = Color.WHITE;
        mDanmakuView.addDanmaku(danmaku);
    }

    @Override
    public void prepareDanmaku(BaseDanmakuParser parser, DanmakuContext danmakuContext) {
        mDanmakuView.setCallback(new master.flame.danmaku.controller.DrawHandler.Callback() {
            @Override
            public void updateTimer(DanmakuTimer timer) {
            }

            @Override
            public void drawingFinished() {

            }

            @Override
            public void danmakuShown(BaseDanmaku danmaku) {
            }

            @Override
            public void prepared() {
                mDanmakuView.start();
            }
        });
        mDanmakuContext = danmakuContext;
        mDanmakuParser = parser;
        mDanmakuView.prepare(mDanmakuParser, mDanmakuContext);
        mDanmakuView.showFPS(false);
        mDanmakuView.enableDanmakuDrawingCache(true);
    }

    @Override
    public void onEnterLiveVideoError() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                BootstrapText text = new BootstrapText.Builder(getApplicationContext()).addText("连接云巴服务器失败，请刷新重试")
                        .build();
                mConnStat.setBootstrapText(text);
                mReconnBtn.setVisibility(View.VISIBLE);
                mConnIv.setVisibility(View.GONE);
                mReconnBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BootstrapText text = new BootstrapText.Builder(getApplicationContext()).addText("正在连接云巴服务器...")
                                .build();
                        mConnStat.setBootstrapText(text);
                        mConnIv.setVisibility(View.VISIBLE);
                        mReconnBtn.setVisibility(View.INVISIBLE);
                        mLiveVideoPresenter.reconnect();
                    }
                });
            }
        });
    }

    @Override
    public void onEnterLiveVideoSuc() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnStatLayout.setVisibility(View.GONE);
                mConnSucLayout.setVisibility(View.VISIBLE);
                mInputCommentEt.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        if (editable.toString().length() == 0) {
                            mSendCommentBtn.setEnabled(false);
                        } else {
                            mSendCommentBtn.setEnabled(true);
                        }
                    }
                });
                mAgreeBtn.setEnabled(true);
            }
        });
    }

    @Override
    public void onPubDanmakuError() {
//        Toast.makeText(this, "发送弹幕失败，请重新发送", Toast.LENGTH_SHORT).show();
        optionEnable(true);
    }

    @Override
    public void onPubDanmakuSuc() {
        optionEnable(true);
//        mInputCommentEt.setText("");
    }

    @Override
    public void onAgreeFail() {
//        Toast.makeText(this, "点赞失败，请重试", Toast.LENGTH_SHORT).show();
    }

    private void notifyPresenceChanged() {
        BootstrapText text = new BootstrapText.Builder(getApplicationContext()).addText(getString(R.string.online_number, String.valueOf(mNumberOfOnline)))
                .build();
        mOnlineTv.setBootstrapText(text);
        mOnlineTv.setText(getString(R.string.online_number, String.valueOf(mNumberOfOnline)));
    }
}
