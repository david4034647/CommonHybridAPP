package com.weiba.commonhybridapp.wxapi;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.weiba.commonhybridapp.R;
import com.weiba.web.sharelibrary.util.Constants;

public class WXPayEntryActivity extends AppCompatActivity implements IWXAPIEventHandler {
    private IWXAPI api;
    private Toolbar mToolbar;
    private WebView mWebView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pay_result);

        api = WXAPIFactory.createWXAPI(this, Constants.WX_APP_ID);
        api.handleIntent(getIntent(), this);

        mToolbar = (Toolbar) this.findViewById(R.id.wxpay_toolbar);
        mToolbar.setTitle("显示支付结果页");
        mToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mWebView = (WebView) this.findViewById(R.id.wxpay_webview);
        initWebViewSettings();
//        mWebView.loadUrl(Constants.WXPAY_RESULT_URL);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
        });
    }

    private void initWebViewSettings() {
        WebSettings webSettings = mWebView.getSettings();
//        myWebView.getSettings().setSupportZoom(true);
//        myWebView.getSettings().setBuiltInZoomControls(true);
//        myWebView.getSettings().setDefaultFontSize(12);
//        myWebView.getSettings().setLoadWithOverviewMode(true);
        // 设置可以访问文件
        webSettings.setAllowFileAccess(true);
        //如果访问的页面中有Javascript，则webview必须设置支持Javascript
        webSettings.setJavaScriptEnabled(true);
        // 修改ua使得web端正确判断
        String ua = webSettings.getUserAgentString();
        webSettings.setUserAgentString(ua + "; rrdDZ");
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setAllowFileAccess(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq req) {
    }

    @Override
    public void onResp(BaseResp resp) {
        if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
            String message = "";
            switch (resp.errCode) {
                case 0://成功
                    message = "成功!";
                    break;
                case -1://错误
                    message = "错误";
                    break;
                case -2://用户取消
                    Constants.WXPAY_RESULT_URL = "";
                    message = "用户取消";
                    break;
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}