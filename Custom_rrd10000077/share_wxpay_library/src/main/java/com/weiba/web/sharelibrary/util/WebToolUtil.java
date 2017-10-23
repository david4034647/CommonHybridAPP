package com.weiba.web.sharelibrary.util;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.widget.Toast;

import com.umeng.socialize.PlatformConfig;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.weiba.web.sharelibrary.Twodimnsion;
import com.weiba.web.sharelibrary.bean.WebShareBean;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Gyueqi on 16/7/14.
 */
public class WebToolUtil {
    private final static int FILECHOOSER_RESULTCODE = 1;

    public static void ShareByType(Activity ac, int type, WebShareBean shareBean) {
        if (shareBean.getDesc().equals("")) {
            shareBean.setDesc("暂无描述");
        }
        if (!shareBean.getImgUrl().contains(".jpg") && !shareBean.getImgUrl().contains(".png")) {
            shareBean.setImgUrl("");
        }
        ShareAction shareAction = new ShareAction(ac);
        switch (type) {
            case Constants.WEIXIN:
                shareAction
                        .setPlatform(SHARE_MEDIA.WEIXIN)
                        .setCallback(new MyUmShareListener(ac))
                        .withTitle(shareBean.getTitle())
                        .withText(shareBean.getDesc())
                        .withTargetUrl(shareBean.getLink())
                        .withMedia(new UMImage(ac, shareBean.getImgUrl()))
                        .share();
                break;
            case Constants.WEIXIN_CIRCLE:
                shareAction
                        .setPlatform(SHARE_MEDIA.WEIXIN_CIRCLE)
                        .setCallback(new MyUmShareListener(ac))
                        .withTitle(shareBean.getTitle())
                        .withText(shareBean.getDesc())
                        .withTargetUrl(shareBean.getLink())
                        .withMedia(new UMImage(ac, shareBean.getImgUrl()))
                        .share();
                break;
            case Constants.QQ:
                shareAction
                        .setPlatform(SHARE_MEDIA.QQ)
                        .setCallback(new MyUmShareListener(ac))
                        .withTitle(shareBean.getTitle())
                        .withText(shareBean.getDesc())
                        .withTargetUrl(shareBean.getLink());
                if (!shareBean.getImgUrl().equals("")) {
                    shareAction.withMedia(new UMImage(ac, shareBean.getImgUrl()));
                }
                shareAction.share();
                break;
            case Constants.QZONE:
                shareAction
                        .setPlatform(SHARE_MEDIA.QZONE)
                        .setCallback(new MyUmShareListener(ac))
                        .withTitle(shareBean.getTitle())
                        .withText(shareBean.getDesc())
                        .withTargetUrl(shareBean.getLink())
                        .withMedia(new UMImage(ac, shareBean.getImgUrl()))
                        .share();
                break;
            case Constants.SINA:
                shareAction.setPlatform(SHARE_MEDIA.SINA)
                        .setCallback(new MyUmShareListener(ac))
                        .withTitle(shareBean.getTitle())
                        .withText(shareBean.getDesc())
                        .withTargetUrl(shareBean.getLink());
                if (!shareBean.getImgUrl().equals("")) {
                    shareAction.withMedia(new UMImage(ac, shareBean.getImgUrl()));
                }
                shareAction.share();
                break;
            case Constants.QRCODE:
                Twodimnsion.a = 1;
                Intent intent = new Intent(ac, Twodimnsion.class);
                intent.putExtra("url", shareBean.getLink());
                intent.putExtra("name", shareBean.getTitle());
                ac.startActivity(intent);
                break;
            case Constants.CLONE_LINK:
                // 得到剪贴板管理器
                ClipboardManager cmb = (ClipboardManager) ac.getSystemService(Context.CLIPBOARD_SERVICE);
                cmb.setText(shareBean.getLink());
                Toast.makeText(ac,"复制成功!",Toast.LENGTH_SHORT).show();
                break;
        }
        //详细的更新


    }

    static class MyUmShareListener implements UMShareListener {
        private Activity ac;

        public MyUmShareListener(Activity activity) {
            ac = activity;
        }

        @Override
        public void onResult(SHARE_MEDIA platform) {
            Toast.makeText(ac, platform + " 分享成功啦", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(SHARE_MEDIA platform, Throwable t) {
            Toast.makeText(ac, platform + " 分享失败啦", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCancel(SHARE_MEDIA platform) {
            Toast.makeText(ac, platform + " 分享取消了", Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean showShare(SHARE_MEDIA shareMedia){
        PlatformConfig.Platform platform = PlatformConfig.getPlatform(shareMedia);
        if(platform.isConfigured()){
            return true;
        }
        return false;
    }

    public static void setJurisdiction() {
        PlatformConfig.setWeixin("wxfaf66627a01482c5", "f2c79daba953999bd08dac5698594905");
        //微信 appid appsecret
//        PlatformConfig.setSinaWeibo("1194704336", "1ae30c8e32da79a1f4f4f0e9e39cc8a2");
//        PlatformConfig.setSinaWeibo("3921700954", "04b48b094faeb16683c32669824ebdad");
        //新浪微博 appkey appsecret
//        PlatformConfig.setQQZone("1105509869", "DoX835itDabA1suQ");
        // QQ和Qzone appid appkey
    }

    /**
     * 调用选择手机相册和摄像头界面
     */
    public static void TakePhoto(Activity ac, Uri imageUri) {
        File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyApp");
        // Create the storage directory if it does not exist
        if (!imageStorageDir.exists()) {
            imageStorageDir.mkdirs();
        }
        File file = new File(imageStorageDir + File.separator + "IMG_" + String.valueOf(System.currentTimeMillis()) + ".jpg");
        imageUri = Uri.fromFile(file);

        final List<Intent> cameraIntents = new ArrayList<Intent>();
        final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = ac.getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent i = new Intent(captureIntent);
            i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            i.setPackage(packageName);
            i.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            cameraIntents.add(i);

        }
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        Intent chooserIntent = Intent.createChooser(i, "Image Chooser");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[]{}));
        ac.startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);
    }

}
