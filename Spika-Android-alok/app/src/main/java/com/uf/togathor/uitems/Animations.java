package com.uf.togathor.uitems;

import android.app.Activity;
import android.view.View;

import com.facebook.rebound.BaseSpringSystem;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringSystem;
import com.facebook.rebound.SpringUtil;
import com.uf.togathor.model.OnAnimationFinishListener;

/**
 * Created by alok on 12/30/14.
 */
public class Animations {

    private final BaseSpringSystem mSpringSystem = SpringSystem.create();
    private SpringListener mSpringListener;
    private Spring mScaleSpring;
    private static View viewParent;
    private float lastUsedScale;
    private Activity activity;
    private OnAnimationFinishListener animationFinishListener;

    public Animations() {
        super();
    }

    public Animations(Activity activity, OnAnimationFinishListener animationFinishListener)   {
        this.activity = activity;
        this.animationFinishListener = animationFinishListener;
    }

    public void setupScene(View parent) {
        this.viewParent = parent;
        mScaleSpring = mSpringSystem.createSpring();
    }

    public void springExpand() {
        lastUsedScale = (float) 3;
        mSpringListener = new SpringListener((float) 3, 0);
        mScaleSpring.addListener(mSpringListener);
        mScaleSpring.setEndValue(1);
    }

    public void springClose() {
        mSpringListener = new SpringListener(1 / lastUsedScale, 0);
        mScaleSpring.addListener(mSpringListener);
        mScaleSpring.setEndValue(1);
    }

    private class SpringListener extends SimpleSpringListener {

        float scale;
        float originalMeasure;
        int animateCode;
        float oldValue = 0.0f;

        SpringListener(float scale, int animateCode) {
            this.scale = scale;
            this.animateCode = animateCode;

            switch (animateCode)    {
                case 0:
                    originalMeasure = viewParent.getMeasuredHeight();
                    break;
                case 1:
                    originalMeasure = viewParent.getElevation();
                    break;
            }
        }

        @Override
        public void onSpringUpdate(Spring spring) {
            float mappedValue, finalMeasure;

            switch (animateCode) {
                case 0:
                    mappedValue = (float) SpringUtil.mapValueFromRangeToRange(spring.getCurrentValue(), 0, 1, 1, scale);
                    finalMeasure = (originalMeasure * (mappedValue));
                    viewParent.getLayoutParams().height = (int) finalMeasure;
                    viewParent.requestLayout();
                    break;

                case 1:
                    mappedValue = (float) SpringUtil.mapValueFromRangeToRange(spring.getCurrentValue(), 0, 1, 0, scale);
                    if(oldValue > mappedValue)  {
                        spring.setAtRest();
                    }
                    viewParent.setElevation(mappedValue);
                    oldValue = mappedValue;
                    break;
            }
        }

        @Override
        public void onSpringAtRest(Spring spring) {
            spring.removeAllListeners();
            if(animationFinishListener != null)
                animationFinishListener.animationFinished();
            super.onSpringAtRest(spring);
        }
    }

    public void fadeIn() {

    }

    public void fadeOut() {

    }

    public void raiseView(float scale) {
        mSpringListener = new SpringListener(scale, 1);
        mScaleSpring.addListener(mSpringListener);
        mScaleSpring.setEndValue(1);
    }

    public void lowerView() {

    }

}
