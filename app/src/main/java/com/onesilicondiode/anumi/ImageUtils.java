package com.onesilicondiode.anumi;

public class ImageUtils {
    public static int getImageResourceId(int day) {
        int[] imageResources = {
                R.drawable.day17_image,
                R.drawable.day18_image,
                R.drawable.day19_image,
                R.drawable.day20_image,
                R.drawable.day21_image,
                R.drawable.day22_image,
                R.drawable.day23_image,
                R.drawable.day24_image,
                R.drawable.day25_image,
                R.drawable.day26_image,
                R.drawable.day27_image,
                R.drawable.day28_image,
                R.drawable.day29_image,
        };

        if (day >= 1 && day <= imageResources.length) {
            return imageResources[day - 1]; // Adjust for 0-based index
        } else {
            return R.drawable.day17_image; // Use a default image resource ID
        }
    }
}