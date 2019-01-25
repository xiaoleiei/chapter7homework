package com.bytedance.camera.demo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.bytedance.camera.demo.utils.Utils;

import java.io.File;

import static com.bytedance.camera.demo.utils.Utils.rotateImage;


public class TakePictureActivity extends AppCompatActivity {

    private ImageView imageView;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private static final int REQUEST_EXTERNAL_STORAGE = 101;
    private File imgFile;  //自己新增


    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_picture);
        imageView = findViewById(R.id.img);
        findViewById(R.id.btn_picture).setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(TakePictureActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(TakePictureActivity.this,
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                //todo 在这里申请相机、存储的权限
            } else {

                takePicture();
            }
        });

    }

    private void takePicture() {
        //todo 打开相机
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
      //  imgFile = Utils.getOutputMediaFile(MEDIA_TYPE_IMAGE);
            startActivityForResult(takePictureIntent,REQUEST_IMAGE_CAPTURE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            imgFile = new File (Utils.saveCameraImage(data));
//            Uri fileUri = FileProvider.getUriForFile(this,"com.bytedance.camera.demo",imgFile);
//            .putExtra(MediaStore.EXTRA_OUTPUT,fileUri);
            setPic();
        }
    }


    private void setPic() {
        int targetW = imageView.getWidth(); //获取宽和高；
        int targetH = imageView.getHeight();
        //todo 根据imageView裁剪
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        //值设为true那么将不返回实际的bitmap，也不给其分配内存空间这样就避免内存溢出了。但是允许我们查询图片的信息这其中就包括图片大小信息
        BitmapFactory.decodeFile(imgFile.getAbsolutePath(),bmOptions);
         int photoW = bmOptions.outWidth;
         int photoH = bmOptions.outHeight;

        //todo 根据缩放比例读取文件，生成Bitmap
        int scaleFactor = Math.min(photoW / targetW,photoH / targetH);
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true ;

        Bitmap bmp = BitmapFactory.decodeFile(imgFile.getAbsolutePath(),bmOptions);
        imageView.setImageBitmap(bmp);
        //todo 如果存在预览方向改变，进行图片旋转
        imageView.setImageBitmap(rotateImage(bmp,imgFile.getAbsolutePath()));

        //todo 如果存在预览方向改变，进行图片旋转



        //先将图片放进去
       // Bitmap bmp = BitmapFactory.decodeFile(imgFile.getAbsolutePath);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE: {
                //todo 判断权限是否已经授予
                break;
            }
        }
    }
}
