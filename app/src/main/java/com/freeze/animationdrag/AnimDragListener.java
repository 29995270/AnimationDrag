package com.freeze.animationdrag;

/**
 * Created by WangQi on 2017/4/3.
 */

public interface AnimDragListener {
    void onProcess(float percent, int distance);
    void onRelease(boolean forwardOrBackward);
    void onStartDrag();
    void onDragEnd(boolean startOrEnd);
}
