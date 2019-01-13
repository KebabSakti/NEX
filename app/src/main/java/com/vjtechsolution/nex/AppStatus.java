package com.vjtechsolution.nex;

import android.app.Application;

/**
 * Created by Aryo on 3/23/2018.
 */

public class AppStatus extends Application {
    private static boolean status = true;

    public static boolean isStatus() {
        return status;
    }

    public static void setStatus(boolean status) {
        AppStatus.status = status;
    }
}
