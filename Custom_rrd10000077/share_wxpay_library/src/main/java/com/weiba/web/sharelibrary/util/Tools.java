package com.weiba.web.sharelibrary.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Created by lidong on 16/9/21.
 */

public class Tools {
    private final static String TAG = Tools.class.getSimpleName();

    private static SharedPreferences pref = null;

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
            Log.d(TAG, url);

            CookieSyncManager.createInstance(context);

            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            cookieManager.removeSessionCookie();// 移除
            cookieManager.removeAllCookie();
            String oldCookie = cookieManager.getCookie(url);
            if(oldCookie != null){
                Log.d(TAG, "Nat: webView.syncCookieOutter.oldCookie" + oldCookie);
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
                Log.d(TAG, "Nat: webView.syncCookie.newCookie" + newCookie);
            }
        }catch(Exception e){
            Log.e(TAG , "Nat: webView.syncCookie failed " + e.toString());
        }
    }

    //图片插入到系统图库
    public static String saveImageToGallery(Context context, Bitmap bitmap) {
        String returnUrl = null;

        if (bitmap == null) {
            return returnUrl;
        }

        //解决安卓4.4的保存图片bug
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            if (pref == null) {
                pref = context.getSharedPreferences("fixMediaDir", context.MODE_PRIVATE);
            }

            boolean created = pref.getBoolean("created", false);
            //没有创建，就手动创建相册
            if (!created) {
                File sdcard = Environment.getExternalStorageDirectory();
                if (sdcard == null) {
                    return returnUrl;
                }

                File mediaDir = new File(sdcard, "DCIM/Camera");
                if (!mediaDir.exists()) {
                    mediaDir.mkdirs();
                }

                //修复完成设置标志。
                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean("created", true);
                editor.commit();
            }
        }

        //把图片插入到系统图库
        return MediaStore.Images.Media.insertImage(context.getContentResolver(),
                bitmap, null, null);

        //通知图库更新
        //sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + file.getAbsolutePath())));
    }

    public static boolean saveImg2CacheDir(Context ctx, Bitmap imgBitmap) {
        boolean result = false;

        if (ctx == null || imgBitmap == null) {
            return result;
        }

        File cacheDir = ctx.getCacheDir();
        if (cacheDir == null) {
            return result;
        }

        String tmpImgPath = cacheDir.getAbsolutePath() + "/tmp.jpg";

        Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
        int quality = 100;
        OutputStream stream = null;
        try {
            stream = new FileOutputStream(tmpImgPath);
            result = imgBitmap.compress(format, quality, stream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return result;
    }
}
