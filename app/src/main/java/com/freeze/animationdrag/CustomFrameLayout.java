package com.freeze.animationdrag;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/**
 * Created by WangQi on 2017/3/21.
 */

public class CustomFrameLayout extends FrameLayout implements AnimationDragHandler{

    private AnimationDragHelper animationDragHelper;

    public CustomFrameLayout(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public CustomFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CustomFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        animationDragHelper = new AnimationDragHelper(context);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return animationDragHelper.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return animationDragHelper.onTouchEvent(ev);
    }

    @Override
    public void addAnimSet(AnimSet set, @AnimationDragHelper.DragDirection int dragDirection) {
        animationDragHelper.addAnimSet(set, dragDirection);
    }
}
