package com.weiba.commonhybridapp.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;
import com.tandong.bottomview.view.BottomView;
import com.weiba.commonhybridapp.R;
import com.weiba.web.sharelibrary.adapter.BVAdapter;
import com.weiba.web.sharelibrary.bean.WchatPayEntity;
import com.weiba.web.sharelibrary.bean.WebShareBean;
import com.weiba.web.sharelibrary.util.Constants;
import com.weiba.web.sharelibrary.util.Tools;
import com.weiba.web.sharelibrary.util.WebToolUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;

public class MainActivity extends AppCompatActivity implements WebView.OnLongClickListener {
    private final static String TAG = MainActivity.class.getSimpleName();

    private ImageView mTestImgView;
    private Toolbar mToolbar;
    private WebView mWebView;
    private ProgressBar progressBar;
    private Handler mHandler = new Handler();

    private WebShareBean shareBean;
    private int SHARE_TYPE = 0;
    private long mExitTime;
    private ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> mUploadCallbackAboveL;
    private String WebUrl = "https://shenma.nz/";
    private Runnable mUpdateResults = new Runnable() {
        public void run() {
            shareByType(SHARE_TYPE);
        }
    };
    private boolean isNeedReload;
    private Uri imageUri = Uri.EMPTY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        shareBean = new WebShareBean();
        //WebUrl = "https://seller.daodian100.com/";
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!TextUtils.isEmpty(Constants.WXPAY_RESULT_URL)) {
            mWebView.loadUrl(Constants.WXPAY_RESULT_URL);
            Constants.WXPAY_RESULT_URL = "";
        }

        // 解决跳转到支付宝APP再回来后不能自动刷新界面获取支付结果的bug
        if (isNeedReload && mWebView != null) {
            mWebView.reload();
            isNeedReload = false;
        }
    }

    @Override
    public void onBackPressed() {
        if (mWebView != null && mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            } else {
                Intent home = new Intent(Intent.ACTION_MAIN);
                home.addCategory(Intent.CATEGORY_HOME);
                startActivity(home);
            }
        }
    }

    //文件上传
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != 1) {
            return;
        }

        if (null == mUploadMessage && null == mUploadCallbackAboveL) return;

        Uri result = (data == null || resultCode != RESULT_OK) ? null : data.getData();
        if (mUploadCallbackAboveL != null) {
            onActivityResultAboveL(requestCode, resultCode, data);
        } else if (mUploadMessage != null) {
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                mWebView.reload();
                break;
            case R.id.action_home:
                mWebView.loadUrl(WebUrl);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 显示溢出菜单图标
     *
     * @param view
     * @param menu
     * @return
     */
    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu) {
        if (menu != null) {
            if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
                try {
                    Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (Exception e) {
                    Log.e(getClass().getSimpleName(), "onMenuOpened...unable to set icons for overflow menu", e);
                }
            }
        }

        return super.onPrepareOptionsPanel(view, menu);
    }

    ////////////////// WebView LongClickListener ////////////////////
    @Override
    public boolean onLongClick(View v) {
        if (v == null || !(v instanceof WebView)) {
            return false;
        }

        WebView.HitTestResult testResult = ((WebView)v).getHitTestResult();
        if (testResult == null) {
            return false;
        }

        int type = testResult.getType();
        if (type == WebView.HitTestResult.UNKNOWN_TYPE) {
            return false;
        }

        if (type == WebView.HitTestResult.EDIT_TEXT_TYPE) {
            return true;
        }

        if (type == WebView.HitTestResult.SRC_ANCHOR_TYPE) {
            Log.d(TAG, "hit test result 超链接 [" + testResult.getExtra() + "]");
            shareBean.setLink(testResult.getExtra());
        }

        if (type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE || type == WebView.HitTestResult.IMAGE_TYPE) {
            Log.d(TAG, "hit test result 图片 [" + testResult.getExtra() + "]");
            shareBean.setImgUrl(testResult.getExtra());

            if (shareBean.getImgBitmap() != null) {
                initBottomView();
            }
        }

        return true;
    }

    ///////////////// Customize Method /////////////////
    private void initView() {
        mToolbar = (Toolbar) this.findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mTestImgView = (ImageView) findViewById(R.id.test_imgview);

        progressBar = (ProgressBar) findViewById(R.id.pb_progress);

        mWebView = (WebView) this.findViewById(R.id.webview);
        initWebView();
    }

    private void initWebView() {
        mWebView.setHorizontalScrollBarEnabled(false);//水平不显示
        mWebView.setVerticalScrollBarEnabled(false); //垂直不显示
        //mWebView.setInitialScale(100);
        WebSettings webSettings = mWebView.getSettings();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        // 修改ua使得web端正确判断
        String ua = webSettings.getUserAgentString();
        webSettings.setUserAgentString(ua + "; davidWebView");
        webSettings.setGeolocationEnabled(true);
        webSettings.setGeolocationDatabasePath(getFilesDir().getPath());
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        // 开启 DOM storage API 功能
        webSettings.setDomStorageEnabled(true);
        //设置WebView属性，能够执行Javascript脚本
        webSettings.setJavaScriptEnabled(true);
        //设置可以访问文件
        webSettings.setAllowFileAccess(true);
        //设置支持缩放
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(false);

        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.addJavascriptInterface(new JsInteraction(), "androidShare");
        mWebView.setWebChromeClient(new MyWebChromeClient());
        mWebView.setWebViewClient(new MyWebViewClient());
        mWebView.setOnLongClickListener(this);
        mWebView.loadUrl(WebUrl);
    }

    protected void NetworkOperation() {
        mHandler.post(mUpdateResults);
    }

    private void onActivityResultAboveL(int requestCode, int resultCode, Intent data) {
        if (requestCode != 1 || mUploadCallbackAboveL == null) {
            return;
        }
        Uri[] results = null;
        if (resultCode == Activity.RESULT_OK) {
            if (data == null) {
                results = new Uri[]{imageUri};
            } else {
                String dataString = data.getDataString();
                ClipData clipData = data.getClipData();

                if (clipData != null) {
                    results = new Uri[clipData.getItemCount()];
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        results[i] = item.getUri();
                    }
                }

                if (dataString != null)
                    results = new Uri[]{Uri.parse(dataString)};
            }
        }
        if (results != null) {
            mUploadCallbackAboveL.onReceiveValue(results);
            mUploadCallbackAboveL = null;
        } else {
            results = new Uri[]{imageUri};
            mUploadCallbackAboveL.onReceiveValue(results);
            mUploadCallbackAboveL = null;
        }
    }

    private void initBottomView() {
        if (shareBean == null) {
            Toast.makeText(this, "无分享内容.", Toast.LENGTH_SHORT).show();
            return;
        }

        final GridView lv_menu_list;
        final ArrayList<String> menus = new ArrayList<>();
        menus.add("微信");
        menus.add("朋友圈");
        if (shareBean.getImgBitmap() != null) {
            menus.add("保存图片");
        }

        final BottomView bv = new BottomView(this,
                R.style.BottomViewTheme_Default, R.layout.bottom_view);
        bv.setAnimation(R.style.BottomToTopAnim);//设置动画，可选
        bv.showBottomView(true);
        lv_menu_list = (GridView) bv.getView().findViewById(R.id.bv_gridview);
        lv_menu_list.setAdapter(new BVAdapter(this, menus));
        lv_menu_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (menus.get(position)) {
                    case "朋友圈":

                        SHARE_TYPE = Constants.WEIXIN_CIRCLE;
                        break;
                    case "微信":

                        SHARE_TYPE = Constants.WEIXIN;
                        break;
                    case "QQ空间":

                        SHARE_TYPE = Constants.QZONE;
                        break;
                    case "QQ":

                        SHARE_TYPE = Constants.QQ;
                        break;
                    case "新浪微博":

                        SHARE_TYPE = Constants.SINA;
                        break;
                    case "复制链接":

                        SHARE_TYPE = Constants.CLONE_LINK;
                        break;
                    case "二维码":

                        SHARE_TYPE = Constants.QRCODE;
                        break;

                    case "保存图片":

                        SHARE_TYPE = Constants.SAVE_PHOTO;
                        break;
                }
                NetworkOperation();
                bv.dismissBottomView();
            }
        });
    }

    private void shareByType(int shareType) {

        if (Tools.saveImg2CacheDir(MainActivity.this, shareBean.getImgBitmap())) {
            shareBean.setImgDataStr(getCacheDir() + "/tmp.jpg");
        } else {
            shareBean.setImgDataStr(null);
        }

        Platform.ShareParams sp = new Platform.ShareParams();
        if (!TextUtils.isEmpty(shareBean.getImgDataStr())) {
            sp.setImagePath(shareBean.getImgDataStr());
            sp.setShareType(Platform.SHARE_IMAGE);
        } else {
            sp.setImagePath(shareBean.getImgUrl());
            sp.setShareType(Platform.SHARE_WEBPAGE);
        }
        sp.setTitle(shareBean.getTitle());
        sp.setUrl(shareBean.getImgUrl());

        switch (shareType) {
            case Constants.WEIXIN_CIRCLE:
                Platform wechatMoments = ShareSDK.getPlatform(WechatMoments.NAME);
                wechatMoments.setPlatformActionListener(new PlatformActionListener() {
                    @Override
                    public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
                        Log.d(TAG, "wechatMoments onComplete start ...");
                        Toast.makeText(MainActivity.this,"分享成功.",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Platform platform, int i, Throwable throwable) {
                        Log.d(TAG, "wechatMoments onError start ...");
                        Toast.makeText(MainActivity.this,"分享失败.",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancel(Platform platform, int i) {
                        Log.d(TAG, "wechatMoments onCancel start ...");
                    }
                });
                wechatMoments.share(sp);

                break;
            case Constants.WEIXIN:
                Platform wechat = ShareSDK.getPlatform(Wechat.NAME);
                wechat.setPlatformActionListener(new PlatformActionListener() {
                    @Override
                    public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
                        Log.d(TAG, "wechat onComplete start ...");
                        Toast.makeText(MainActivity.this,"分享成功.",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Platform platform, int i, Throwable throwable) {
                        Log.d(TAG, "wechat onError start ...");
                        Toast.makeText(MainActivity.this,"分享失败.",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancel(Platform platform, int i) {
                        Log.d(TAG, "wechat onCancel start ...");
                    }
                });
                wechat.share(sp);

                break;
            case Constants.SAVE_PHOTO:
                String url = Tools.saveImageToGallery(MainActivity.this, shareBean.getImgBitmap());
                if (!TextUtils.isEmpty(url)) {
                    Toast.makeText(MainActivity.this, "保存图片成功.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "保存图片失败.", Toast.LENGTH_SHORT).show();
                }

                break;
            case Constants.CLONE_LINK:
                ClipboardManager cmb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                cmb.setText(shareBean.getImgUrl());
                Toast.makeText(this,"复制成功.",Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }


    }

    /**
     * 调起微信支付
     */
//    private void onWXPay(WchatPayEntity wchatPayEntity) {
//        api.registerApp(Constants.WX_APP_ID);
//        if (!api.isWXAppInstalled()) {
//            Toast.makeText(MainActivity.this, "没有安装微信", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        if (!api.isWXAppSupportAPI()) {
//            Toast.makeText(MainActivity.this, "当前版本不支持支付功能", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        PayReq req = new PayReq();
//        //req.appId = "wxf8b4f85f3a794e77";  // 测试用appId
//        //req.appId			= wchatPayEntity.getAppid();
//        req.appId = wchatPayEntity.getAppid();
//        req.partnerId = wchatPayEntity.getMch_id();
//        req.prepayId = wchatPayEntity.getPrepay_id();
//        req.nonceStr = wchatPayEntity.getNonce_str();
//        req.timeStamp = String.valueOf(wchatPayEntity.getTimestamp());
//        req.packageValue = "Sign=WXPay";
//        req.sign = wchatPayEntity.getSign();
//        req.extData = wchatPayEntity.getTrade_type(); // optional
//        // 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
//        api.sendReq(req);
//    }


    public class JsInteraction {

        @JavascriptInterface
        public void getData(String result) {
            //{"title":"商城主页","desc":"","link":"http://shop13287532.wxrrd.com/","imgUrl":"http://ms.wrcdn.com/2015/11/23/Fph0NnAo5WZAXInSSMgq0stHXUoL.png?imageMogr2/thumbnail/60x60!"}
            if (result != null) {
                shareBean = new Gson().fromJson(result, WebShareBean.class);
                NetworkOperation();
            }
        }

        @JavascriptInterface
        public void getWXPayParams(String result) {
            Log.i("获取微信支付参数", result);
            if (result != null) {
                WchatPayEntity wchatPayEntity = new Gson().fromJson(result, WchatPayEntity.class);
                Constants.WXPAY_RESULT_URL = wchatPayEntity.getReturn_url();
                //onWXPay(wchatPayEntity);
            }
        }

    }

    private class MyWebChromeClient extends WebChromeClient {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress == 100) {
                progressBar.setVisibility(View.GONE);
            } else {
                if (progressBar.getVisibility() == View.GONE) {
                    progressBar.setVisibility(View.VISIBLE);
                }

                progressBar.setProgress(newProgress);
            }

            super.onProgressChanged(view, newProgress);
        }


        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            mUploadCallbackAboveL = filePathCallback;
            WebToolUtil.TakePhoto(MainActivity.this, imageUri);
            return true;
        }

        // For Android 4.1
        public void openFileChooser(ValueCallback<Uri> uploadMsg) {
            mUploadMessage = uploadMsg;
            WebToolUtil.TakePhoto(MainActivity.this, imageUri);
        }

        // For Android 3.0+
        public void openFileChooser(ValueCallback uploadMsg, String acceptType) {
            mUploadMessage = uploadMsg;
            WebToolUtil.TakePhoto(MainActivity.this, imageUri);
        }

        // For Android 3.0-
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
            mUploadMessage = uploadMsg;
            WebToolUtil.TakePhoto(MainActivity.this, imageUri);
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            mToolbar.setTitle(title);
            if (shareBean != null) {
                shareBean.setTitle(title);
                shareBean.setDesc(title);
            }
        }

        @Override
        public void onGeolocationPermissionsHidePrompt() {
            super.onGeolocationPermissionsHidePrompt();
        }

        @Override
        public void onGeolocationPermissionsShowPrompt(final String origin, final GeolocationPermissions.Callback callback) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("允许访问位置信息吗？");
            DialogInterface.OnClickListener dialogButtonOnClickListener = new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int clickedButton) {
                    if (DialogInterface.BUTTON_POSITIVE == clickedButton) {
                        callback.invoke(origin, true, true);
                    } else if (DialogInterface.BUTTON_NEGATIVE == clickedButton) {
                        callback.invoke(origin, false, false);
                    }
                }
            };
            builder.setPositiveButton("允许", dialogButtonOnClickListener);
            builder.setNegativeButton("拒绝", dialogButtonOnClickListener);
            builder.show();
            super.onGeolocationPermissionsShowPrompt(origin, callback);
        }
    }

    private class MyWebViewClient extends WebViewClient {

        @Override
        @TargetApi(21)
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            Log.d(TAG, "shouldOverrideUrlLoading start URL[" + request.getUrl().toString() + "]");
            return super.shouldInterceptRequest(view, request);
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            Log.d(TAG, "onLoadResource start URL[" + url + "]");
            super.onLoadResource(view, url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(final WebView view, String url) {
            Log.d(TAG, "shouldOverrideUrlLoading start URL[" + url + "]");
            if (url.startsWith("intent://")) {
                startIntent(url);
            } else if (url.startsWith("alipays://platformapi")) {
                launchAliPay(url);
            } else {
                if (url.contains("http:") || url.contains("https:")) {
                    view.loadUrl(url);
                }
            }

            return true;
        }

        private void launchAliPay(String alipayUri) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(alipayUri));
                startActivity(intent);
                isNeedReload = true;
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "未安装支付宝应用", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }

        private void startIntent(String url) {
            try {
                Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setComponent(null);
                startActivity(intent);
            } catch (Exception e) {
                if (url.contains("com.eg.android.AlipayGphone")) {
                    Toast.makeText(MainActivity.this, "未安装支付宝应用", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "应用未安装", Toast.LENGTH_SHORT).show();
                }
                e.printStackTrace();
            }
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }
    }
}
