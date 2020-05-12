package com.naloaty.syncshare.util;

import android.content.res.Configuration;
import android.content.res.Resources;

import androidx.annotation.NonNull;

import com.naloaty.syncshare.R;

public class DeviceUtils {

    public static boolean isLandscape(@NonNull Resources resources) {
        return resources.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    public static boolean isPortrait(@NonNull Resources resources) {
        return resources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    public static int getOptimalColumsCount(@NonNull Resources resources) {
        final float width = resources.getDisplayMetrics().widthPixels;
        //final float density = resources.getDisplayMetrics().density;

        //final int dpSize = (int) Math.ceil(width / density);

        return (int) Math.ceil(width / resources.getDimension(R.dimen.media_column_size));
    }
}
