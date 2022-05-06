package com.mobile.security;

import android.content.Context;
import android.net.ConnectivityManager;

public class TheNetworkState {

    public static boolean connectionAvailable(Context context){

        ConnectivityManager connectivityManager;
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getActiveNetworkInfo() != null) return true;
        else return false;
    }
}