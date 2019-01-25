package com.bytedance.camera.demo.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.ContentValues.TAG;

public class Utils {

    public static String convertUriToPath(Context context, Uri uri) {
        if (uri == null) {
            return null;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            String imgPathSel = UriUtils.formatUri(context, uri);
            if (!TextUtils.isEmpty(imgPathSel)) {
                return imgPathSel;
            }
        }
        String schema = uri.getScheme();
        if (TextUtils.isEmpty(schema) || ContentResolver.SCHEME_FILE.equals(schema)) {
            return uri.getPath();
        }
        if ("http".equals(schema)) {
            return uri.toString();
        }
        if (ContentResolver.SCHEME_CONTENT.equals(schema)) {
            String[] projection = new String[]{MediaStore.MediaColumns.DATA};
            Cursor cursor = null;
            String filePath = "";
            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                if (cursor.moveToFirst()) {
                    filePath = cursor.getString(0);
                }
                if (null != cursor) {
                    cursor.close();
                }
            } catch (Exception e) {
                // do nothing
            } finally {
                try {
                    if (null != cursor) {
                        cursor.close();
                    }
                } catch (Exception e2) {
                    // do nothing
                }
            }
            if (TextUtils.isEmpty(filePath)) {
                try {
                    ContentResolver contentResolver = context.getContentResolver();
                    String selection = MediaStore.Images.Media._ID + "= ?";
                    String id = uri.getLastPathSegment();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && !TextUtils.isEmpty(id) && id.contains(":")) {
                        id = id.split(":")[1];
                    }
                    String[] selectionArgs = new String[]{id};
                    cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, null);
                    if (cursor.moveToFirst()) {
                        filePath = cursor.getString(0);
                    }
                    if (null != cursor) {
                        cursor.close();
                    }

                } catch (Exception e) {
                    // do nothing
                } finally {
                    try {
                        if (cursor != null) {
                            cursor.close();
                        }
                    } catch (Exception e) {
                        // do nothing
                    }
                }
            }
            return filePath;
        }
        return null;
    }


    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    /**
     * Create a File for saving an image or video
     */
    public static String getFileName(){
       return  new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    }
    public static File getOutputMediaFile(int type) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "CameraDemo");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        // Create a media file name  //创建文件已经写好
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

        //自己写保存相机图片
    public static String saveCameraImage(Intent data){
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)){
            Log.i(TAG,"sd card is not avaiable/writeable right now.");
            return "";
        }
        //为图片命名
        Bitmap bmp = (Bitmap) data.getExtras().get("data"); //


        FileOutputStream fos = null;
        File file = new File(Environment.getExternalStorageDirectory().getPath()+"/mnt/sdcard/test/");
        file.mkdirs();  //创建文件夹
        String fileName =Environment.getExternalStorageDirectory().getPath()+ "/mnt/sdcard/test/" + getFileName()+".jpg";
        File file2=new File(fileName);

        try{//写入sd card
            try {
                file2.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            fos = new FileOutputStream(fileName);
            bmp.compress(Bitmap.CompressFormat.JPEG,100,fos);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }finally {
            try{
                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return fileName;


    }

    private static void getDir(){
       File file=new File(Environment.getExternalStorageDirectory().getPath()+ "/mnt/sdcard/test/") ;
       file.getAbsoluteFile().listFiles();//获取xxxxx.jpg
       file.getAbsolutePath();//获取路径
      //  BitmapFactory.decodeFile();//把bitmap放到内存里面，然后再剪辑，最后放进去，或者覆盖或者加（1），（2）.
    }


    private static final int NUM_90 = 90;
    private static final int NUM_180 = 180;
    private static final int NUM_270 = 270;

    public static  Bitmap rotateImage(Bitmap bitmap, String path) {
        ExifInterface srcExif = null;
        try {
            srcExif = new ExifInterface(path);
        } catch (IOException e) {
            e.printStackTrace();
            return bitmap;
        }
        Matrix matrix = new Matrix();
        int angle = 0;
        int orientation = srcExif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                angle = NUM_90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                angle = NUM_180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                angle = NUM_270;
                break;
            default:
                break;
        }
        matrix.postRotate(angle);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
}
