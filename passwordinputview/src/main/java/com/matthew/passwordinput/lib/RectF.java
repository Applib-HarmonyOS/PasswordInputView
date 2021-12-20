package com.matthew.passwordinput.lib;

import ohos.agp.utils.RectFloat;

/**
 * RectF class.
 */
public class RectF extends RectFloat {
    @Override
    public void modify(float left, float top, float right, float bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        super.modify(left, top, right, bottom);
    }

    /**
     * Set method.
     *
     * @param left left
     * @param top top
     * @param right right
     * @param bottom bottom
     */
    public void set(float left, float top, float right, float bottom) {
        modify(left, top, right, bottom);
    }
}