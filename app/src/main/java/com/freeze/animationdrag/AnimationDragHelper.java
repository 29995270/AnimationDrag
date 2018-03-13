package com.freeze.animationdrag;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.VelocityTrackerCompat;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by WangQi on 2017/3/21.
 */

public class AnimationDragHelper implements AnimationDragHandler{
    public static final int DRAG_INVALID = -1;
    public static final int DRAG_HORIZONTAL_L2R = 0;
    public static final int DRAG_HORIZONTAL_R2L = 1;
    public static final int DRAG_VERTICAL_T2B = 2;
    public static final int DRAG_VERTICAL_B2T = 3;

    private int dragDirection = DRAG_INVALID;
    private Context context;

    @IntDef({DRAG_HORIZONTAL_L2R, DRAG_HORIZONTAL_R2L, DRAG_VERTICAL_T2B, DRAG_VERTICAL_B2T})
    @Retention(RetentionPolicy.SOURCE)
    public @interface DragDirection {

    }

    public static final int STATE_START = 0;
    public static final int STATE_RUNNING_DRAG = 1;
    public static final int STATE_RUNNING_SETTLING = 2;
    public static final int STATE_END = 3;

    @IntDef({STATE_START, STATE_RUNNING_DRAG, STATE_RUNNING_SETTLING, STATE_END})
    @Retention(RetentionPolicy.SOURCE)
    public @interface AnimationState {

    }

    private SparseArray<AnimSet> animSetArray = new SparseArray<>();

    private static final int INVALID_POINTER = -1;

    private int touchSlop = -1;
    private boolean isBeingDragged;
    private VelocityTracker velocityTracker;
    private int activePointerId = INVALID_POINTER;
    private int lastMotionX;
    private int lastMotionY;

    public AnimationDragHelper(Context context) {
        this.context = context;
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (touchSlop < 0) {
            touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        }

        final int action = ev.getAction();

        // Shortcut since we're being dragged
        if (action == MotionEvent.ACTION_MOVE && isBeingDragged) {
            return true;
        }

        switch (MotionEventCompat.getActionMasked(ev)) {
            case MotionEvent.ACTION_DOWN: {
                isBeingDragged = false;
                dragDirection = DRAG_INVALID;
                final int x = (int) ev.getX();
                final int y = (int) ev.getY();

                AnimSet r2lAnimSet = animSetArray.get(DRAG_HORIZONTAL_R2L);
                AnimSet l2rAnimSet = animSetArray.get(DRAG_HORIZONTAL_L2R);
                AnimSet t2bAnimSet = animSetArray.get(DRAG_VERTICAL_T2B);
                AnimSet b2tAnimSet = animSetArray.get(DRAG_VERTICAL_B2T);
                if (
                        (r2lAnimSet != null && r2lAnimSet.care(x, y)) ||
                        (l2rAnimSet != null && l2rAnimSet.care(x, y)) ||
                        (t2bAnimSet != null && t2bAnimSet.care(x, y)) ||
                        (b2tAnimSet != null && b2tAnimSet.care(x, y))
                        ) {
                    lastMotionY = y;
                    lastMotionX = x;
                    activePointerId = ev.getPointerId(0);
                    ensureVelocityTracker();
                }
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                final int activePointerId = this.activePointerId;
                if (activePointerId == INVALID_POINTER) {
                    // If we don't have a valid id, the touch down wasn't on content.
                    break;
                }
                final int pointerIndex = ev.findPointerIndex(activePointerId);
                if (pointerIndex == -1) {
                    break;
                }

                final int x = (int) ev.getX(pointerIndex);
                final int y = (int) ev.getY(pointerIndex);

                final int xDiff = Math.abs(x - lastMotionX);
                final int yDiff = Math.abs(y - lastMotionY);

                if (yDiff > touchSlop && xDiff < touchSlop/2) {
                    isBeingDragged = true;
                    if (dragDirection == DRAG_INVALID) {
                        if (y - lastMotionY > 0) {
                            AnimSet endedSet = animSetArray.get(DRAG_VERTICAL_B2T);
                            dragDirection = endedSet != null && endedSet.isEnd() ? DRAG_VERTICAL_B2T : DRAG_VERTICAL_T2B;
                        } else {
                            AnimSet endedSet = animSetArray.get(DRAG_VERTICAL_T2B);
                            dragDirection = endedSet != null && endedSet.isEnd() ? DRAG_VERTICAL_T2B : DRAG_VERTICAL_B2T;
                        }
                    }
                    lastMotionY = y;
                } else if (yDiff < touchSlop/2 && xDiff > touchSlop) {
                    isBeingDragged = true;
                    if (dragDirection == DRAG_INVALID) {
                        if (x - lastMotionX > 0) {
                            AnimSet endedSet = animSetArray.get(DRAG_HORIZONTAL_R2L);
                            dragDirection = endedSet != null && endedSet.isEnd() ? DRAG_HORIZONTAL_R2L : DRAG_HORIZONTAL_L2R;
                        } else {
                            AnimSet endedSet = animSetArray.get(DRAG_HORIZONTAL_L2R);
                            dragDirection = endedSet != null && endedSet.isEnd() ? DRAG_HORIZONTAL_L2R : DRAG_HORIZONTAL_R2L;
                        }
                    }
                    lastMotionX = x;
                } else {
                    isBeingDragged = false;
                    dragDirection = DRAG_INVALID;
                }
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                isBeingDragged = false;
                activePointerId = INVALID_POINTER;
                dragDirection = DRAG_INVALID;
                if (velocityTracker != null) {
                    velocityTracker.recycle();
                    velocityTracker = null;
                }
                break;
            }
        }

        if (velocityTracker != null) {
            velocityTracker.addMovement(ev);
        }
        return isBeingDragged;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        if (touchSlop < 0) {
            touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        }

        switch (MotionEventCompat.getActionMasked(ev)) {
            case MotionEvent.ACTION_DOWN: {
                final int x = (int) ev.getX();
                final int y = (int) ev.getY();
                lastMotionX = x;
                lastMotionY = y;
                activePointerId = ev.getPointerId(0);
                ensureVelocityTracker();

                AnimSet r2lAnimSet = animSetArray.get(DRAG_HORIZONTAL_R2L);
                AnimSet l2rAnimSet = animSetArray.get(DRAG_HORIZONTAL_L2R);
                AnimSet t2bAnimSet = animSetArray.get(DRAG_VERTICAL_T2B);
                AnimSet b2tAnimSet = animSetArray.get(DRAG_VERTICAL_B2T);
                if (
                        (r2lAnimSet != null && r2lAnimSet.care(x, y)) ||
                                (l2rAnimSet != null && l2rAnimSet.care(x, y)) ||
                                (t2bAnimSet != null && t2bAnimSet.care(x, y)) ||
                                (b2tAnimSet != null && b2tAnimSet.care(x, y))
                        ) {
                    lastMotionX = x;
                    lastMotionY = y;
                    activePointerId = ev.getPointerId(0);
                    ensureVelocityTracker();
                } else {
                    return false;
                }
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                final int activePointerIndex = ev.findPointerIndex(activePointerId);
                if (activePointerIndex == -1) {
                    return false;
                }

                final int x = (int) ev.getX(activePointerIndex);
                final int y = (int) ev.getY(activePointerIndex);
                int dx = lastMotionX - x;
                int dy = lastMotionY - y;

                if (!isBeingDragged && Math.abs(dy) > touchSlop && Math.abs(dx) < touchSlop/2) {
                    if (dragDirection == DRAG_INVALID) {
                        if (dy > 0) {
                            AnimSet endedSet = animSetArray.get(DRAG_VERTICAL_T2B);
                            dragDirection = endedSet != null && endedSet.isEnd() ? DRAG_VERTICAL_T2B : DRAG_VERTICAL_B2T;
                        } else {
                            AnimSet endedSet = animSetArray.get(DRAG_VERTICAL_B2T);
                            dragDirection = endedSet != null && endedSet.isEnd() ? DRAG_VERTICAL_B2T : DRAG_VERTICAL_T2B;
                        }
                    }
                    isBeingDragged = true;
                    if (dy > 0) {
                        dy -= touchSlop;
                    } else {
                        dy += touchSlop;
                    }
                } else if (!isBeingDragged && Math.abs(dx) > touchSlop && Math.abs(dy) < touchSlop/2) {
                    if (dragDirection == DRAG_INVALID) {
                        if (dx > 0) {
                            AnimSet endedSet = animSetArray.get(DRAG_HORIZONTAL_L2R);
                            dragDirection = endedSet != null && endedSet.isEnd() ? DRAG_HORIZONTAL_L2R : DRAG_HORIZONTAL_R2L;
                        } else {
                            AnimSet endedSet = animSetArray.get(DRAG_HORIZONTAL_R2L);
                            dragDirection = endedSet != null && endedSet.isEnd() ? DRAG_HORIZONTAL_R2L : DRAG_HORIZONTAL_L2R;
                        }
                    }
                    isBeingDragged = true;
                    if (dx > 0) {
                        dx -= touchSlop;
                    } else {
                        dx += touchSlop;
                    }
                }

                if (isBeingDragged) {
                    lastMotionX = x;
                    lastMotionY = y;
                    // We're being dragged so scroll the ABL
                    scroll(dx, dy, dragDirection);
                }
                break;
            }

            case MotionEvent.ACTION_UP:
                if (velocityTracker != null) {
                    velocityTracker.addMovement(ev);
                    velocityTracker.computeCurrentVelocity(1000);
                    float vel = 0;
                    if (dragDirection == DRAG_HORIZONTAL_L2R || dragDirection == DRAG_HORIZONTAL_R2L) {
                        vel = velocityTracker.getXVelocity(activePointerId);
                    } else if (dragDirection == DRAG_VERTICAL_T2B || dragDirection == DRAG_VERTICAL_B2T){
                        vel = velocityTracker.getYVelocity(activePointerId);
                    }
                    fling(dragDirection, vel);
                }
                // $FALLTHROUGH
            case MotionEvent.ACTION_CANCEL: {
                isBeingDragged = false;
                dragDirection = DRAG_INVALID;
                activePointerId = INVALID_POINTER;
                if (velocityTracker != null) {
                    velocityTracker.recycle();
                    velocityTracker = null;
                }
                break;
            }
        }

        if (velocityTracker != null) {
            velocityTracker.addMovement(ev);
        }

        return true;
    }
    private void fling(int dragDirection, float vel) {
        AnimSet animSet = animSetArray.get(dragDirection);
        if (animSet != null) {
            animSet.release(vel);
        }
    }

    private void scroll(int dx, int dy, int dragDirection) {
        AnimSet animSet = animSetArray.get(dragDirection);
        if (animSet == null) return;
        switch (dragDirection) {
            case DRAG_HORIZONTAL_L2R:
                animSet.setCurrentPlayTime(animSet.getCurrentPlayTime() + animSet.distanceToTime(-dx));
                break;
            case DRAG_HORIZONTAL_R2L:
                animSet.setCurrentPlayTime(animSet.getCurrentPlayTime() + animSet.distanceToTime(dx));
                break;
            case DRAG_VERTICAL_T2B:
                animSet.setCurrentPlayTime(animSet.getCurrentPlayTime() + animSet.distanceToTime(-dy));
                break;
            case DRAG_VERTICAL_B2T:
                animSet.setCurrentPlayTime(animSet.getCurrentPlayTime() + animSet.distanceToTime(dy));
                break;
            default:
        }
    }

    private void ensureVelocityTracker() {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
    }

    @Override
    public void addAnimSet(AnimSet set, @DragDirection int dragDirection) {
        animSetArray.append(dragDirection, set);
    }

}
