package com.onesilicondiode.anumi;

public class ImageUtils {
    public static int getImageResourceId(int day) {
        int[] imageResources = {
                R.drawable.wall_17,
                R.drawable.wall_18,
                R.drawable.wall_19,
                R.drawable.wall_20,
                R.drawable.wall_21,
                R.drawable.wall_22,
                R.drawable.wall_23,
                R.drawable.wall_24,
                R.drawable.wall_25,
                R.drawable.wall_26,
                R.drawable.wall_27,
                R.drawable.wall_28,
        };

        if (day >= 1 && day <= imageResources.length) {
            return imageResources[day - 1]; // Adjust for 0-based index
        } else {
            return R.drawable.wall_28;
        }
    }
}