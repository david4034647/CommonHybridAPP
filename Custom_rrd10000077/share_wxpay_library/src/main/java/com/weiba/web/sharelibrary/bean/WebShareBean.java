package com.weiba.web.sharelibrary.bean;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Base64;

import java.io.UnsupportedEncodingException;

/**
 * Created by Gyueqi on 16/7/14.
 */
public class WebShareBean {

    /**
     * title : 商城主页
     * desc :
     * link : http://shop13287532.wxrrd.com/
     * imgUrl : http://ms.wrcdn.com/2015/11/23/Fph0NnAo5WZAXInSSMgq0stHXUoL.png?imageMogr2/thumbnail/60x60!
     */

    private String title;
    private String desc;
    private String link;
    private String imgUrl;

    private String imgDataStr;
    private Bitmap imgBitmap;

    public String getImgDataStr() {
        return imgDataStr;
    }

    public void setImgDataStr(String imgDataStr) {
        this.imgDataStr = imgDataStr;
    }

    public Bitmap getImgBitmap() {
        return imgBitmap;
    }

    public void setImgBitmap(Bitmap imgBitmap) {
        this.imgBitmap = imgBitmap;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        if (TextUtils.isEmpty(imgUrl)) {
            return;
        }

        String tmpImgStr;
        if (!imgUrl.startsWith("data:image")) {
            this.imgUrl = imgUrl;
            return;

        }

        int index = imgUrl.indexOf("data:image/png;base64,");
        tmpImgStr = imgUrl.substring(22);
        if (TextUtils.isEmpty(tmpImgStr)) {
            return;
        }

        this.imgDataStr = imgUrl;
        byte[] imgArray = new byte[0];
        try {
            imgArray = Base64.decode(tmpImgStr.getBytes("utf-8"), Base64.DEFAULT);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Bitmap imgBitmap = BitmapFactory.decodeByteArray(imgArray, 0, imgArray.length);
        this.imgBitmap = imgBitmap;
    }
}
