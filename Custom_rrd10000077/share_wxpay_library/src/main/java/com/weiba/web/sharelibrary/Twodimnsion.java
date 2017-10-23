package com.weiba.web.sharelibrary;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.weiba.web.sharelibrary.fragment.SuccessDialogFragment;
import com.weiba.web.sharelibrary.util.Constants;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;


/**
 * Created by Administrator on 2015/12/14 0014.
 */
public class Twodimnsion extends AppCompatActivity {
    protected int mScreenWidth;
    private ImageView iv_qr_image;
    private TextView tv_shopname;
    private ImageButton bun_fin;
    private String url;
    public static int a=1;
    private Button preservation;
    private static SharedPreferences pref = null;
    // Constants
    // ===========================================================
    /**
     * 生成二维码图片大小
     */
    private static final int QRCODE_SIZE = 900;
    /**
     * 头像图片大小
     */
    private static final int PORTRAIT_SIZE = 165;

    // ===========================================================
    // Fields
    // ===========================================================
    /**
     * 头像图片
     */
    private Bitmap portrait;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.twodimension);

        iv_qr_image = (ImageView) findViewById(R.id.qr_show);
        tv_shopname = (TextView) findViewById(R.id.shopname);
        bun_fin = (ImageButton) findViewById(R.id.main_title_catalogBtn);
        bun_fin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        preservation = (Button) findViewById(R.id.main_title_searchBtn2);
        Intent intent = getIntent();
        if (intent.getStringExtra("url") == null) {
            // Toast.makeText(Twodimension.this, "店铺链接失效，生成二维码错误", Toast.LENGTH_SHORT).show();
        } else if (intent.getStringExtra("url").contains("https:")) {

            tv_shopname.setText(intent.getStringExtra("name"));
            // 初始化头像
            portrait = initProtrait("playstore_icon.png");
            // 建立二维码
            Bitmap qr = createQRCodeBitmap(intent.getStringExtra("url"));
            createQRCodeBitmapWithPortrait(qr, portrait);
            iv_qr_image.setDrawingCacheEnabled(true);
            iv_qr_image.setImageBitmap(qr);
            preservation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (a== Constants.state){
                        Bitmap obmp = Bitmap.createBitmap(iv_qr_image.getDrawingCache());
                        if (obmp != null) {
                            iv_qr_image.setDrawingCacheEnabled(false);
                            saveImageToGallery(Twodimnsion.this, obmp);
                            showDialog();
                        } else {
                            Toast.makeText(Twodimnsion.this, "保存失败!", Toast.LENGTH_SHORT).show();
                        }
                        a++;
                    }else{
                        Toast.makeText(Twodimnsion.this, "您已经保存过了，请不要重复保存！", Toast.LENGTH_SHORT).show();

                    }

                }
            });


        } else {
            Toast.makeText(Twodimnsion.this, "二维码生成错误!", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * 初始化头像图片
     */
    private Bitmap initProtrait(String url) {
        try {
            // 这里采用从asset中加载图片abc.jpg
            Bitmap portrait = BitmapFactory.decodeStream(getAssets().open(url));

            // 对原有图片压缩显示大小
            Matrix mMatrix = new Matrix();
            float width = portrait.getWidth();
            float height = portrait.getHeight();
            mMatrix.setScale(PORTRAIT_SIZE / width, PORTRAIT_SIZE / height);
            return Bitmap.createBitmap(portrait, 0, 0, (int) width,
                    (int) height, mMatrix, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 创建QR二维码图片
     */
    private Bitmap createQRCodeBitmap(String url) {
        // 用于设置QR二维码参数
        Hashtable<EncodeHintType, Object> qrParam = new Hashtable<EncodeHintType, Object>();
        // 设置QR二维码的纠错级别——这里选择最高H级别
        qrParam.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        // 设置编码方式
        qrParam.put(EncodeHintType.CHARACTER_SET, "UTF-8");


        // 生成QR二维码数据——这里只是得到一个由true和false组成的数组
        // 参数顺序分别为：编码内容，编码类型，生成图片宽度，生成图片高度，设置参数
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(url,
                    BarcodeFormat.QR_CODE, QRCODE_SIZE, QRCODE_SIZE, qrParam);

            // 开始利用二维码数据创建Bitmap图片，分别设为黑白两色
            int w = bitMatrix.getWidth();
            int h = bitMatrix.getHeight();
            int[] data = new int[w * h];

            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    if (bitMatrix.get(x, y))
                        data[y * w + x] = 0xff000000;// 黑色
                    else
                        data[y * w + x] = -1;// -1 相当于0xffffffff 白色
                }
            }

            // 创建一张bitmap图片，采用最高的图片效果ARGB_8888
            Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            // 将上面的二维码颜色数组传入，生成图片颜色
            bitmap.setPixels(data, 0, w, 0, 0, w, h);
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 在二维码上绘制头像
     */
    private void createQRCodeBitmapWithPortrait(Bitmap qr, Bitmap portrait) {
        // 头像图片的大小
        int portrait_W = portrait.getWidth();
        int portrait_H = portrait.getHeight();

        // 设置头像要显示的位置，即居中显示
        int left = (QRCODE_SIZE - portrait_W) / 2;
        int top = (QRCODE_SIZE - portrait_H) / 2;
        int right = left + portrait_W;
        int bottom = top + portrait_H;
        Rect rect1 = new Rect(left, top, right, bottom);

        // 取得qr二维码图片上的画笔，即要在二维码图片上绘制我们的头像
        Canvas canvas = new Canvas(qr);

        // 设置我们要绘制的范围大小，也就是头像的大小范围
        Rect rect2 = new Rect(0, 0, portrait_W, portrait_H);
        // 开始绘制
        canvas.drawBitmap(portrait, rect2, rect1, null);
    }
    // 插入成功状态
    public void showDialog() {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        SuccessDialogFragment dialogFragment = (SuccessDialogFragment) fm.findFragmentByTag("tag");

        if (dialogFragment == null) {
            dialogFragment = SuccessDialogFragment.newInstance("成功保存到相册");
        }
        dialogFragment.show(getSupportFragmentManager(), "Dialog");

    }
    //图片插入到系统图库
    public static void saveImageToGallery(Context context, Bitmap bitmap) {
        if (bitmap == null) {
            return;
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
                if (sdcard != null) {
                    File mediaDir = new File(sdcard, "DCIM/Camera");
                    if (!mediaDir.exists()) {
                        mediaDir.mkdirs();
                    }
                }
                //修复完成设置标志。
                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean("created", true);
                editor.commit();
            }
        }
        //把图片插入到系统图库
        MediaStore.Images.Media.insertImage(context.getContentResolver(),
                bitmap, null, null);
        //通知图库更新
        //sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + file.getAbsolutePath())));
    }

}
