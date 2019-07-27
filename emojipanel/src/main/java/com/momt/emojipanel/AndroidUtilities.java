package com.momt.emojipanel;

public class AndroidUtilities {
    public static float density;

    public static int dp(float value) {
        return (int) Math.ceil(density * value);
    }
}
