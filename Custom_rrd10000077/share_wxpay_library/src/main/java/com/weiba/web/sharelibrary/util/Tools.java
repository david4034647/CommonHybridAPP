package com.weiba.web.sharelibrary.util;

import android.content.Context;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.umeng.socialize.utils.Log;

/**
 * Created by lidong on 16/9/21.
 */

public class Tools {
     /**
     * 同步一下cookie
     */
    public static void synCookies(Context context, String url) {
        CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.removeSessionCookie();//移除
        cookieManager.setCookie(url, Constants.OLD_COOKIE);//cookies是在HttpClient中获得的cookie
        CookieSyncManager.getInstance().sync();
    }
    /**
     * Sync Cookie
     */
    public static void syncCookie(Context context, String url){
        try{
            Log.d("Nat: webView.syncCookie.url", url);

            CookieSyncManager.createInstance(context);

            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            cookieManager.removeSessionCookie();// 移除
            cookieManager.removeAllCookie();
            String oldCookie = cookieManager.getCookie(url);
            if(oldCookie != null){
                Log.d("Nat: webView.syncCookieOutter.oldCookie", oldCookie);
            }

            StringBuilder sbCookie = new StringBuilder();
            sbCookie.append(String.format("JSESSIONID=%s","INPUT YOUR JSESSIONID STRING"));
            sbCookie.append(String.format(";domain=%s", "INPUT YOUR DOMAIN STRING"));
            sbCookie.append(String.format(";path=%s","INPUT YOUR PATH STRING"));

            String cookieValue = sbCookie.toString();
            cookieManager.setCookie(url, cookieValue);
            CookieSyncManager.getInstance().sync();

            String newCookie = cookieManager.getCookie(url);
            if(newCookie != null){
                Log.d("Nat: webView.syncCookie.newCookie", newCookie);
            }
        }catch(Exception e){
            Log.e("Nat: webView.syncCookie failed", e.toString());
        }
    }
}
