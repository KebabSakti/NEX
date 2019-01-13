package com.vjtechsolution.nex;

import android.app.Application;

/**
 * Created by Aryo on 3/25/2018.
 */

public class GlobalApiAddress extends Application {
    private static final String domain = "https://nexaice.com/devnex";

    public GlobalApiAddress() {
    }

    public static String getDomain() {
        return domain;
    }
}
