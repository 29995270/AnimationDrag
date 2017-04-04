package com.freeze.animationdrag;

import android.animation.ObjectAnimator;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by WangQi on 2017/3/22.
 */

public class ChromeActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private View view;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chrome);

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        WebView webView = (WebView) findViewById(R.id.web_view);
        view = findViewById(R.id.view);

        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("https://baidu.com");

        CustomFrameLayout root = (CustomFrameLayout) findViewById(R.id.root);
        AnimSet animSet = new AnimSet(300, this);

        ObjectAnimator animator1 = ObjectAnimator.ofFloat(view, "scaleX", 1, 0.8f);
        ObjectAnimator animator2 = ObjectAnimator.ofFloat(view, "scaleY", 1, 0.8f);

        animSet.oneStepAnimators(300, animator1, animator2)
                .setActiveHandler(new AnimSet.ActiveHandle() {
                    Rect rect = new Rect();

                    @Override
                    public boolean active(Pair<Integer, Integer> startXY, @AnimationDragHelper.AnimationState int state) {
                        if (state == AnimationDragHelper.STATE_START) {
                            toolbar.getHitRect(rect);
                            return rect.contains(startXY.first, startXY.second);
                        } else {
                            view.getHitRect(rect);
                            return rect.contains(startXY.first, startXY.second);
                        }
                    }
                });
        root.addAnimSet(animSet, AnimationDragHelper.DRAG_VERTICAL_T2B);

    }
/*
    @SuppressWarnings("unchecked")
    protected static class AnimationHandler implements Runnable {
        // The per-thread list of all active animations
        *//** @hide *//*
        protected final ArrayList<ValueAnimator> mAnimations = new ArrayList<ValueAnimator>();

        // Used in doAnimationFrame() to avoid concurrent modifications of mAnimations
        private final ArrayList<ValueAnimator> mTmpAnimations = new ArrayList<ValueAnimator>();

        // The per-thread set of animations to be started on the next animation frame
        *//** @hide *//*
        protected final ArrayList<ValueAnimator> mPendingAnimations = new ArrayList<ValueAnimator>();

        *//**
     * Internal per-thread collections used to avoid set collisions as animations start and end
     * while being processed.
     * @hide
     *//*
        protected final ArrayList<ValueAnimator> mDelayedAnims = new ArrayList<ValueAnimator>();
        private final ArrayList<ValueAnimator> mEndingAnims = new ArrayList<ValueAnimator>();
        private final ArrayList<ValueAnimator> mReadyAnims = new ArrayList<ValueAnimator>();

        private final Choreographer mChoreographer;
        private boolean mAnimationScheduled;

        private AnimationHandler() {
            mChoreographer = Choreographer.getInstance();
        }

        *//**
     * Start animating on the next frame.
     *//*
        public void start() {
            scheduleAnimation();
        }

        private void doAnimationFrame(long frameTime) {
            // mPendingAnimations holds any animations that have requested to be started
            // We're going to clear mPendingAnimations, but starting animation may
            // cause more to be added to the pending list (for example, if one animation
            // starting triggers another starting). So we loop until mPendingAnimations
            // is empty.
            while (mPendingAnimations.size() > 0) {
                ArrayList<ValueAnimator> pendingCopy =
                        (ArrayList<ValueAnimator>) mPendingAnimations.clone();
                mPendingAnimations.clear();
                int count = pendingCopy.size();
                for (int i = 0; i < count; ++i) {
                    ValueAnimator anim = pendingCopy.get(i);
                    // If the animation has a startDelay, place it on the delayed list
                    if (anim.mStartDelay == 0) {
                        anim.startAnimation(this);
                    } else {
                        mDelayedAnims.add(anim);
                    }
                }
            }
            // Next, process animations currently sitting on the delayed queue, adding
            // them to the active animations if they are ready
            int numDelayedAnims = mDelayedAnims.size();
            for (int i = 0; i < numDelayedAnims; ++i) {
                ValueAnimator anim = mDelayedAnims.get(i);
                if (anim.delayedAnimationFrame(frameTime)) {
                    mReadyAnims.add(anim);
                }
            }
            int numReadyAnims = mReadyAnims.size();
            if (numReadyAnims > 0) {
                for (int i = 0; i < numReadyAnims; ++i) {
                    ValueAnimator anim = mReadyAnims.get(i);
                    anim.startAnimation(this);
                    anim.mRunning = true;
                    mDelayedAnims.remove(anim);
                }
                mReadyAnims.clear();
            }

            // Now process all active animations. The return value from animationFrame()
            // tells the handler whether it should now be ended
            int numAnims = mAnimations.size();
            for (int i = 0; i < numAnims; ++i) {
                mTmpAnimations.add(mAnimations.get(i));
            }
            for (int i = 0; i < numAnims; ++i) {
                ValueAnimator anim = mTmpAnimations.get(i);
                if (mAnimations.contains(anim) && anim.doAnimationFrame(frameTime)) {
                    mEndingAnims.add(anim);
                }
            }
            mTmpAnimations.clear();
            if (mEndingAnims.size() > 0) {
                for (int i = 0; i < mEndingAnims.size(); ++i) {
                    mEndingAnims.get(i).endAnimation(this);
                }
                mEndingAnims.clear();
            }

            // If there are still active or delayed animations, schedule a future call to
            // onAnimate to process the next frame of the animations.
            if (!mAnimations.isEmpty() || !mDelayedAnims.isEmpty()) {
                scheduleAnimation();
            }
        }

        // Called by the Choreographer.
        @Override
        public void run() {
            mAnimationScheduled = false;
            doAnimationFrame(mChoreographer.getFrameTime());
        }

        private void scheduleAnimation() {
            if (!mAnimationScheduled) {
                mChoreographer.postCallback(Choreographer.CALLBACK_ANIMATION, this, null);
                mAnimationScheduled = true;
            }
        }
    }*/
}
