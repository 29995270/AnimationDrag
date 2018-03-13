package com.freeze.animationdrag;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "AAA";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View view = findViewById(R.id.view);
        View list = findViewById(R.id.list);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ChromeActivity.class));
            }
        });

        int maxDragDistance = Utils.getScreenHeight(this) - Utils.dp2px(this, 200 / 2 + 100 / 2 + 56) - Utils.getStatusBarHeight(this);
        int listMaxDragDistance = Utils.getScreenHeight(this) - Utils.dp2px(this, 56) - Utils.getStatusBarHeight(this);

        AnimSet animSet = new AnimSet(maxDragDistance, this);
        animSet.setActiveHandler(new AnimSet.DefaultHandle(view));

        ObjectAnimator animator1 = ObjectAnimator.ofFloat(view, "translationY", 0, maxDragDistance/2);
        ObjectAnimator animator2 = ObjectAnimator.ofFloat(view, "translationX", 0, Utils.getScreenWidth(this)/4);
        ObjectAnimator animator3 = ObjectAnimator.ofFloat(list, "translationY", 0, listMaxDragDistance/2);

        animSet.startAnimators(200, animator1, animator2, animator3);

        ObjectAnimator animator4 = ObjectAnimator.ofFloat(view, "translationY", maxDragDistance/2, maxDragDistance);
        ObjectAnimator animator5 = ObjectAnimator.ofFloat(list, "translationY", listMaxDragDistance/2, listMaxDragDistance);
        ObjectAnimator animator6 = ObjectAnimator.ofFloat(view, "scaleX", 1, 0.5f);
        ObjectAnimator animator7 = ObjectAnimator.ofFloat(view, "scaleY", 1, 0.5f);

        animSet.afterAnimators(true, 200, animator4, animator5, animator6, animator7);

        ObjectAnimator animator8 = ObjectAnimator.ofFloat(view, "rotationX", 0, (float) (-Math.PI * 3));
        animSet.afterAnimators(false, 100, animator8);

        animSet.setDragListener(new AnimDragListener() {
            @Override
            public void onProcess(float percent, int distance) {
                Log.d(TAG, "onProcess: percent = " + percent + "  distance = " + distance);
            }

            @Override
            public void onRelease(boolean forwardOrBackward) {
                Log.d(TAG, "onRelease: forwardOrBackward " + forwardOrBackward);
            }

            @Override
            public void onStartDrag() {
                Log.d(TAG, "onStartDrag:");
            }

            @Override
            public void onDragEnd(boolean startOrEnd) {
                Log.d(TAG, "onDragEnd: startOrEnd " + startOrEnd);
            }
        });

        CustomFrameLayout root = findViewById(R.id.root);
        root.addAnimSet(animSet, AnimationDragHelper.DRAG_VERTICAL_T2B);

        findViewById(R.id.qq).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, QQSlideActivity.class));
            }
        });
    }
}
