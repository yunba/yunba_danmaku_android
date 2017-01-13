package io.yunba.bulletscreen.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;

/**
 * Created by miao on 2017/1/3.
 */

public class CustomResizeSurfaceView extends SurfaceView {
    private int mL, mT, mR, mB;
    private boolean isFist = true;

    public CustomResizeSurfaceView(Context context) {
        super(context);
    }

    public CustomResizeSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomResizeSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (isFist) {
            mL = l;
            mT = t;
            mR = r;
            mB = b;
            isFist = false;
        }
        super.onLayout(changed, l, t, r, b);
    }

    public void forceLayoutAsFirst() {
        layout(mL, mT, mR, mB);
    }
}
