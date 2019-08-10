package com.momt.emojipanel;

import android.content.Context;

public class ApplicationLoader {
    public static Context applicationContext;

    public static void setContext(Context context) {
        applicationContext = context.getApplicationContext();
    }
}
