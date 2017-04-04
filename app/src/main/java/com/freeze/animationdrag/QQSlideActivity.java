package com.freeze.animationdrag;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by WangQi on 2017/4/4.
 */

public class QQSlideActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qq);

        View view = findViewById(R.id.view);
        CustomFrameLayout root = (CustomFrameLayout) findViewById(R.id.root);

        AnimSet animSet = new AnimSet(Utils.getScreenWidth(this) / 4, this);
        animSet.setActiveHandler(new AnimSet.DefaultHandle(view));

        ObjectAnimator animator1 = ObjectAnimator.ofFloat(view, "translationX", 0, Utils.getScreenWidth(this) / 4);
        ObjectAnimator animator2 = ObjectAnimator.ofFloat(view, "scaleX", 1, 0.7f);
        ObjectAnimator animator3 = ObjectAnimator.ofFloat(view, "scaleY", 1, 0.7f);

        animSet.oneStepAnimators(300, animator1, animator2, animator3);

        AnimSet animSet2 = new AnimSet(-Utils.getScreenWidth(this) / 4, this);
        animSet2.setActiveHandler(new AnimSet.DefaultHandle(view));

        ObjectAnimator animator4 = ObjectAnimator.ofFloat(view, "translationX", 0, -Utils.getScreenWidth(this) / 4);
        ObjectAnimator animator5 = ObjectAnimator.ofFloat(view, "scaleX", 1, 0.7f);
        ObjectAnimator animator6 = ObjectAnimator.ofFloat(view, "scaleY", 1, 0.7f);

        animSet2.oneStepAnimators(300, animator4, animator5, animator6);

        AnimSet animSet3 = new AnimSet(Utils.getScreenHeight(this) / 4, this);
        animSet3.setActiveHandler(new AnimSet.DefaultHandle(view));

        ObjectAnimator animator7 = ObjectAnimator.ofFloat(view, "translationY", 0, Utils.getScreenHeight(this) / 4);
        ObjectAnimator animator8 = ObjectAnimator.ofFloat(view, "scaleX", 1, 0.7f);
        ObjectAnimator animator9 = ObjectAnimator.ofFloat(view, "scaleY", 1, 0.7f);

        animSet3.oneStepAnimators(300, animator7, animator8, animator9);

        AnimSet animSet4 = new AnimSet(-Utils.getScreenHeight(this) / 4, this);
        animSet4.setActiveHandler(new AnimSet.DefaultHandle(view));

        ObjectAnimator animator10 = ObjectAnimator.ofFloat(view, "translationY", 0, -Utils.getScreenHeight(this) / 4);
        ObjectAnimator animator11 = ObjectAnimator.ofFloat(view, "scaleX", 1, 0.7f);
        ObjectAnimator animator12 = ObjectAnimator.ofFloat(view, "scaleY", 1, 0.7f);

        animSet4.oneStepAnimators(300, animator10, animator11, animator12);

        root.addAnimSet(animSet, AnimationDragHelper.DRAG_HORIZONTAL_L2R);
        root.addAnimSet(animSet2, AnimationDragHelper.DRAG_HORIZONTAL_R2L);
        root.addAnimSet(animSet3, AnimationDragHelper.DRAG_VERTICAL_T2B);
        root.addAnimSet(animSet4, AnimationDragHelper.DRAG_VERTICAL_B2T);
    }
}
