
package com.umeng.android.util;

import android.content.Context;

public class PullToRefreshUtils {

    /**
     * 将dip转换为px
     * 
     * @param dimen
     * @return px
     */
    public static int convertDimenToPix(Context context, float dimen) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (density * dimen + 0.5f);
    }

}
