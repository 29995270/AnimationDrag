package com.freeze.animationdrag;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        view = findViewById(R.id.view);
        View list = findViewById(R.id.list);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ChromeActivity.class));
            }
        });

        AnimSet animSet = new AnimSet(1000, this);
        animSet.setActiveHandler(new AnimSet.ActiveHandle() {
            Rect rect = new Rect();
            @Override
            public boolean active(Pair<Integer, Integer> startXY, @AnimationDragHelper.AnimationState int state) {
                view.getHitRect(rect);
                return rect.contains(startXY.first, startXY.second);
            }
        });

        ObjectAnimator animator1 = ObjectAnimator.ofFloat(view, "translationY", 0, 500);
        ObjectAnimator animator2 = ObjectAnimator.ofFloat(view, "translationX", 0, 200);
        ObjectAnimator animator3 = ObjectAnimator.ofFloat(list, "translationY", 0, 960);

        animSet.startAnimators(200, animator1, animator2, animator3);

        ObjectAnimator animator4 = ObjectAnimator.ofFloat(view, "translationY", 500, 1000);
        ObjectAnimator animator5 = ObjectAnimator.ofFloat(list, "translationY", 960, 1920);
        ObjectAnimator animator6 = ObjectAnimator.ofFloat(view, "scaleX", 1, 0.5f);
        ObjectAnimator animator7 = ObjectAnimator.ofFloat(view, "scaleY", 1, 0.5f);

        animSet.afterAnimators(true, 200, animator4, animator5, animator6, animator7);

        ObjectAnimator animator8 = ObjectAnimator.ofFloat(view, "rotationX", 0, (float) (-Math.PI * 3));
        animSet.afterAnimators(false, 100, animator8);

        CustomFrameLayout root = (CustomFrameLayout) findViewById(R.id.root);
        root.addAnimSet(animSet, AnimationDragHelper.DRAG_VERTICAL);
    }
}
