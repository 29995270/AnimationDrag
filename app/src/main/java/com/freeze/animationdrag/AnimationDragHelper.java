package com.freeze.animationdrag;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.VelocityTrackerCompat;
import android.util.Pair;
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
    public static final int DRAG_HORIZONTAL = 0;
    public static final int DRAG_VERTICAL = 1;

    private int dragDirection = DRAG_INVALID;
    private Context context;

    @IntDef({DRAG_HORIZONTAL, DRAG_VERTICAL})
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

    private SparseArray<AnimSet> animSetArray = new SparseArray<>();;

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

                AnimSet hAnimSet = animSetArray.get(DRAG_HORIZONTAL);
                AnimSet vAnimSet = animSetArray.get(DRAG_VERTICAL);
                if (
                        (hAnimSet != null
                                && hAnimSet.getActiveHandle() != null
                                && hAnimSet.getActiveHandle().active(Pair.create(x, y), hAnimSet.getCurrentPlayTime() == 0 ? STATE_START: STATE_END)) ||
                                (vAnimSet != null
                                        && vAnimSet.getActiveHandle() != null
                                        && vAnimSet.getActiveHandle().active(Pair.create(x, y), vAnimSet.getCurrentPlayTime() == 0 ? STATE_START: STATE_END))
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
                    lastMotionY = y;
                    dragDirection = DRAG_VERTICAL;
                } else if (yDiff < touchSlop/2 && xDiff > touchSlop) {
                    isBeingDragged = true;
                    lastMotionX = x;
                    dragDirection = DRAG_HORIZONTAL;
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

                AnimSet hAnimSet = animSetArray.get(DRAG_HORIZONTAL);
                AnimSet vAnimSet = animSetArray.get(DRAG_VERTICAL);
                if (
                        (hAnimSet != null
                                && hAnimSet.getActiveHandle() != null
                                && hAnimSet.getActiveHandle().active(Pair.create(x, y), hAnimSet.getCurrentPlayTime() == 0 ? STATE_START: STATE_END)) ||
                                (vAnimSet != null
                                        && vAnimSet.getActiveHandle() != null
                                        && vAnimSet.getActiveHandle().active(Pair.create(x, y), vAnimSet.getCurrentPlayTime() == 0 ? STATE_START: STATE_END))
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
                    dragDirection = DRAG_VERTICAL;
                    isBeingDragged = true;
                    if (dy > 0) {
                        dy -= touchSlop;
                    } else {
                        dy += touchSlop;
                    }
                } else if (!isBeingDragged && Math.abs(dx) > touchSlop && Math.abs(dy) < touchSlop/2) {
                    dragDirection = DRAG_HORIZONTAL;
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
                    if (dragDirection == DRAG_HORIZONTAL) {
                        vel = VelocityTrackerCompat.getXVelocity(velocityTracker,
                                activePointerId);
                    } else if (dragDirection == DRAG_VERTICAL){
                        vel = VelocityTrackerCompat.getYVelocity(velocityTracker,
                                activePointerId);
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
        if (dragDirection == DRAG_HORIZONTAL) {
            animSet.setCurrentPlayTime(animSet.getCurrentPlayTime() + animSet.distanceToTime(-dx));
        } else if (dragDirection == DRAG_VERTICAL) {
            animSet.setCurrentPlayTime(animSet.getCurrentPlayTime() + animSet.distanceToTime(-dy));
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
