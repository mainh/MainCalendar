package com.jjforever.wgj.maincalendar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by Wgj on 2016/10/7.
 * 文字渐变动画
 */
public class GradientShaderTextView extends TextView {
    private LinearGradient mLinearGradient;
    private Matrix mGradientMatrix;
    private int mViewWidth = 0;
    private int mTranslate = 0;
    private float mTextWidth = 0;

    private int delta = 15;
    public GradientShaderTextView(Context ctx)
    {
        this(ctx,null);
    }

    public GradientShaderTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        if (mViewWidth == 0) {
            mViewWidth = getMeasuredWidth();
            if (mViewWidth > 0) {
                Paint paint = getPaint();
                String text = getText().toString();
                mTextWidth = paint.measureText(text);
                int size = (int)paint.measureText(text);
                int startX = (mViewWidth - size) / 2;
                mLinearGradient = new LinearGradient(startX + 1, 0, 0, 0,
                        new int[] { 0x90ffffff, 0xffffffff, 0x90ffffff },
                        new float[] { 0, 0.5f, 1 }, Shader.TileMode.CLAMP); //边缘融合
                paint.setShader(mLinearGradient);
                mGradientMatrix = new Matrix();
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mGradientMatrix != null && mTextWidth > 0) {
            mTranslate += delta;
            if (mTranslate > mTextWidth + 1 || mTranslate < 1) {
                delta = -delta;
            }
            mGradientMatrix.setTranslate(mTranslate, 0);
            mLinearGradient.setLocalMatrix(mGradientMatrix);
            postInvalidateDelayed(30);
        }
    }
}
