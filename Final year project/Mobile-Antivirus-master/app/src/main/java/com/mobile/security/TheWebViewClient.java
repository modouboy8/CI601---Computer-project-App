package com.mobile.security;

import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

class TheWebViewClient extends WebViewClient {

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        view.loadUrl(url);
        CookieManager.getInstance().setAcceptCookie(true);
        return true;
    }
}