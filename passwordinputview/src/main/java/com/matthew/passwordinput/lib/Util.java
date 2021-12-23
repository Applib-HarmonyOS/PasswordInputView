package com.matthew.passwordinput.lib;

import ohos.app.Context;

/**
 * Utils for Attributes.
 *
 * @author matheew
 * @date 2019/5/21
 */
class Util {
    private Util() {

    }

    static float dp2px(Context context, float dp) {
        return (int) (context.getResourceManager().getDeviceCapability().screenDensity / 160 * dp);
    }
}