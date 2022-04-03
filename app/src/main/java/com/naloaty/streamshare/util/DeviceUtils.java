package com.naloaty.streamshare.util;

import android.content.res.Configuration;
import android.content.res.Resources;

import androidx.annotation.NonNull;

import com.naloaty.streamshare.R;

/**
 * This class contains useful utilities related to the device.
 */
public class DeviceUtils {

    /**
     * @param resources Resources.
     * @return True if the device is in landscape orientation.
     */
    public static boolean isLandscape(@NonNull Resources resources) {
        return resources.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    /**
     * @param resources Resources.
     * @return True if the device is in portrait orientation.
     */
    public static boolean isPortrait(@NonNull Resources resources) {
        return resources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    /**
     * Returns the optimal number of columns for the grid layout depending on the screen size.
     * @return The optimal number of columns.
     */
    public static int getOptimalColumnsCount(@NonNull Resources resources) {
        final float width = resources.getDisplayMetrics().widthPixels;
        return (int) Math.ceil(width / resources.getDimension(R.dimen.media_column_size));
    }
}
