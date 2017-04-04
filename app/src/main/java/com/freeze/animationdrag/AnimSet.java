package com.freeze.animationdrag;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by WangQi on 2017/3/21.
 */

public class AnimSet {
    private List<List<ValueAnimator>> animatorLists;
    private int endIndex = 0;  //结束动画的 index
    private int endTime; //index 对应的动画结束后的时间
    private int currentPlayTime;

    private int totalDuration;
    private SparseIntArray durations;
    private final int scaledMinimumFlingVelocity;

    private boolean isOneStep = false;

    private int maxDragDistance;
    private ActiveHandle activeHandle = new ActiveHandle() {
        @Override
        public boolean active(Pair<Integer, Integer> startXY, int state) {
            return true;
        }
    };
    private AnimDragListener dragListener;

    public AnimSet(int maxDragDistance, Context context) {
        animatorLists = new ArrayList<>();
        durations = new SparseIntArray();
        this.maxDragDistance = maxDragDistance;
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        scaledMinimumFlingVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
    }

    public AnimSet oneStepAnimators(long duration, ValueAnimator... animators) {
        if (animatorLists.size() != 0) {
            throw new UnsupportedOperationException("oneStepAnimators can not invoke after other add animator function");
        }
        List<ValueAnimator> valueAnimators = new ArrayList<>(animators.length);
        animatorLists.add(valueAnimators);
        durations.append(0, (int) duration);
        totalDuration += duration;
        endTime = (int) duration;
        endIndex = 0;
        for (ValueAnimator animator : animators) {
            animator.setInterpolator(new DragLinearInterpolator());
            animator.setDuration(duration);
        }
        valueAnimators.addAll(Arrays.asList(animators));
        isOneStep = true;
        return this;
    }

    public AnimSet startAnimators(long duration, ValueAnimator... animators) {
        List<ValueAnimator> valueAnimators;
        if (animatorLists.size() == 0) {
            valueAnimators = new ArrayList<>();
            animatorLists.add(0, valueAnimators);
            durations.append(0, (int) duration);
            totalDuration += duration;
            endTime = (int) duration;
            endIndex = 0;
        } else {
            valueAnimators = animatorLists.get(0);
        }
        for (ValueAnimator animator : animators) {
            animator.setInterpolator(new LinearInterpolator());
            animator.setDuration(duration);
        }
        valueAnimators.addAll(Arrays.asList(animators));
        isOneStep = false;
        return this;
    }

    public AnimSet afterAnimators(boolean isEndPoint, long duration, ValueAnimator... animators) {
        int size = animatorLists.size();
        if (isEndPoint) {
            if (endIndex != 0) {
                throw new UnsupportedOperationException("Unsupported change end point");
            }
            endIndex = size;
            endTime = (int) (totalDuration + duration);
        }
        for (ValueAnimator animator : animators) {
            animator.setInterpolator(new LinearInterpolator());
            animator.setDuration(duration);
        }
        durations.append(size, (int) duration);
        animatorLists.add(Arrays.asList(animators));
        totalDuration += duration;
        return this;
    }

    public AnimSet setActiveHandler(ActiveHandle handler) {
        this.activeHandle = handler;
        return this;
    }

    public ActiveHandle getActiveHandle() {
        return activeHandle;
    }

    public boolean isEnd() {
        return getCurrentPlayTime() == endTime;
    }

    public boolean isStart() {
        return getCurrentPlayTime() == 0;
    }

    void setCurrentPlayTime(int currentPlayTime) {
        if (currentPlayTime < 0) {
            currentPlayTime = 0;
        } else if (currentPlayTime > totalDuration) {
            currentPlayTime = totalDuration;
        }
        int prePlayTime = this.currentPlayTime;
        this.currentPlayTime = currentPlayTime;
        int timeLeft = 0;
        int currentStepIndex = -1;
        int currentStepTime = 0;
        int passStepStartToEnd = 0; // 0 没有穿过段落 1 start to end 穿过段落 -1 end to start 穿过段落
        for (int i = 0; i < durations.size(); i++) {
            int duration = durations.get(i);
            if (currentPlayTime > timeLeft && currentPlayTime <= (timeLeft + duration)) {
                currentStepIndex = i;
                currentStepTime = currentPlayTime - timeLeft;
                if (prePlayTime <= timeLeft) {
                    passStepStartToEnd = 1;
                } else if (prePlayTime > (timeLeft + duration)) {
                    passStepStartToEnd = -1;
                }
                break;
            }
            timeLeft += duration;
        }

        // fix currentPlayTime == timeLeft
        if (currentPlayTime == 0) {
            currentStepIndex = 0;
        } else if (currentPlayTime == totalDuration) {
            currentStepIndex = durations.size() - 1;
        }

        Log.v("AAA", currentPlayTime + ": currentPlayTime  " + currentStepIndex + ": currentStepIndex");

        for (int i = 0; i < animatorLists.size(); i++) {
            List<ValueAnimator> list = animatorLists.get(i);

            if (i == currentStepIndex) {
                if (passStepStartToEnd == 1) {
                    if (i - 1 >= 0) {
                        List<ValueAnimator> valueAnimators = animatorLists.get(i - 1);
                        for (ValueAnimator animator : valueAnimators) {
                            animator.setCurrentPlayTime(animator.getDuration());
                        }
                    }
                } else if (passStepStartToEnd == -1) {
                    if (i + 1 < animatorLists.size()) {
                        List<ValueAnimator> valueAnimators = animatorLists.get(i + 1);
                        for (ValueAnimator animator : valueAnimators) {
                            animator.setCurrentPlayTime(0);
                        }
                    }
                }
                for (ValueAnimator animator : list) {
                    animator.setCurrentPlayTime(currentStepTime);
                }
            }
        }
    }

    int getCurrentPlayTime() {
        return currentPlayTime;
    }

    int distanceToTime(int distance) {
        return (int) (totalDuration / ((float) Math.abs(maxDragDistance)) * distance);
    }

    void release(float vel) {
        if (currentPlayTime == 0 || currentPlayTime == endTime) {
            if (dragListener != null) dragListener.onRelease(currentPlayTime == endTime);
            return;
        }
        if (maxDragDistance > 0) {
            boolean b = vel > scaledMinimumFlingVelocity * 15 || (getCurrentPlayTime() > totalDuration * 0.5f && vel > 0);
            startOrReverseAnimators(b);
            if (dragListener != null) dragListener.onRelease(b);
        } else {
            boolean b = (vel < -scaledMinimumFlingVelocity * 15) || (getCurrentPlayTime() > totalDuration * 0.5f && vel < 0);
            startOrReverseAnimators(b);
            if (dragListener != null) dragListener.onRelease(b);
        }
    }

    private void startOrReverseAnimators(boolean startOrReverse) {
        int timeLeft = 0;
        int currentStepIndex = -1;
        for (int i = 0; i < durations.size(); i++) {
            int duration = durations.get(i);
            if (currentPlayTime > timeLeft && currentPlayTime <= (timeLeft + duration)) {
                currentStepIndex = i;
                break;
            }
            timeLeft += duration;
        }

        if (currentStepIndex <= endIndex) {
            if (startOrReverse) {
                //正向行进到endIndex对应的动画结束
                List<List<ValueAnimator>> subAnimationLists = animatorLists.subList(currentStepIndex, endIndex + 1);
                start(subAnimationLists, endTime);
            } else {
                //逆向到开始
                List<List<ValueAnimator>> subAnimationLists = animatorLists.subList(0, currentStepIndex + 1);
                reverse(subAnimationLists, 0);
            }
        } else {
            //逆向回到 endIndex对应的动画结束
            List<List<ValueAnimator>> subAnimationLists = animatorLists.subList(endIndex + 1, currentStepIndex + 1);
            reverse(subAnimationLists, endTime);
            Log.v("AAA", "逆向回到 endIndex对应的动画结束 : " + endTime + "____" + subAnimationLists);
        }

    }

    private void start(final List<List<ValueAnimator>> animLists, final int endTime) {
        List<ValueAnimator> valueAnimators = animLists.get(0);
        for (int i = 0; i < valueAnimators.size(); i++) {
            ValueAnimator animator = valueAnimators.get(i);
            animator.start();
            animator.setCurrentPlayTime(currentPlayTime);
            if (isOneStep) {
                ((DragLinearInterpolator) animator.getInterpolator()).setReleaseVel(1);
            }

            if (i == 0) {
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        animation.removeListener(this);
                        if (isOneStep) {
                            ((DragLinearInterpolator) ((ValueAnimator) (animation)).getInterpolator()).setReleaseVel(0);
                        }
                        if (animLists.size() <= 1) {
                            currentPlayTime = endTime;
                            return;
                        }
                        start(animLists.subList(1, animLists.size()), endTime);
                    }
                });
            } else {
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        animation.removeListener(this);
                        if (isOneStep) {
                            ((DragLinearInterpolator) ((ValueAnimator) (animation)).getInterpolator()).setReleaseVel(0);
                        }
                        if (animLists.size() <= 1) {
                            currentPlayTime = endTime;
                        }
                    }
                });
            }
        }
    }

    private void reverse(final List<List<ValueAnimator>> animLists, final int endTime) {
        List<ValueAnimator> valueAnimators = animLists.get(animLists.size() - 1);
        for (int i = 0; i < valueAnimators.size(); i++) {
            ValueAnimator animator = valueAnimators.get(i);
            if (Build.VERSION.SDK_INT <= 21) {
                // TODO: 2017/3/24  api 21 reverse 动画不能在断点处返回，会先 end 再 reverse
//                    try {
//                        Field mStarted = ValueAnimator.class.getDeclaredField("mStarted");
//                        mStarted.setAccessible(true);
//                        Log.d("AAA", "startOrReverseAnimators: " + mStarted.get(animator));
//                    } catch (NoSuchFieldException e) {
//                        e.printStackTrace();
//                    } catch (IllegalAccessException e) {
//                        e.printStackTrace();
//                    }
            }
            animator.reverse();
            if (isOneStep) {
                ((DragLinearInterpolator) animator.getInterpolator()).setReleaseVel(-1);
            }

            if (i == 0) {
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        animation.removeListener(this);
                        if (isOneStep) {
                            ((DragLinearInterpolator) ((ValueAnimator) (animation)).getInterpolator()).setReleaseVel(0);
                        }
                        if (animLists.size() <= 1) {
                            currentPlayTime = endTime;
                            return;
                        }
                        reverse(animLists.subList(0, animLists.size() - 1), endTime);
                    }
                });
            } else {
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        animation.removeListener(this);
                        if (isOneStep) {
                            ((DragLinearInterpolator) ((ValueAnimator) (animation)).getInterpolator()).setReleaseVel(0);
                        }
                        if (animLists.size() <= 1) {
                            currentPlayTime = endTime;
                        }
                    }
                });
            }
        }
    }

    void setDragListener(@Nullable AnimDragListener dragListener) {
        this.dragListener = dragListener;
    }

    boolean care(int x, int y) {
        return getActiveHandle() != null
                && getActiveHandle().active(Pair.create(x, y), getCurrentPlayTime() == 0 ? AnimationDragHelper.STATE_START: AnimationDragHelper.STATE_END);
    }

    public interface ActiveHandle {
        boolean active(Pair<Integer, Integer> startXY, @AnimationDragHelper.AnimationState int state);
    }

    public static class DefaultHandle implements ActiveHandle {

        private View activeView;
        private Rect rect = new Rect();

        public DefaultHandle(View activeView) {
            this.activeView = activeView;
        }

        @Override
        public boolean active(Pair<Integer, Integer> startXY, @AnimationDragHelper.AnimationState int state) {
            activeView  .getHitRect(rect);
            return rect.contains(startXY.first, startXY.second);
        }
    }

    private static class DragLinearInterpolator extends LinearInterpolator {
        private float releaseVel = 0;

        private float releaseRemain = -1;

        public float getReleaseVel() {
            return releaseVel;
        }

        public void setReleaseVel(float releaseVel) {
            this.releaseVel = releaseVel;
            if (releaseVel == 0) {
                releaseRemain = -1;
            }
        }

        @Override
        public float getInterpolation(float input) {
            if (releaseVel != 0) {
                if (releaseVel > 0) {
                    if (releaseRemain == -1) {
                        releaseRemain = 1 - input;
                    }
                    return (float) (releaseRemain * Math.sin((input - (1 - releaseRemain)) / releaseRemain * Math.PI / 2)) + (1 - releaseRemain);
                } else {
                    if (releaseRemain == -1) {
                        releaseRemain = input;
                    }
                    return (float) (-(releaseRemain * Math.cos(input / releaseRemain * Math.PI / 2)) + releaseRemain);
                }
            } else {
                return input;
            }
        }
    }
}
