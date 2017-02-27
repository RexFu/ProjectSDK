package com.cysion.tdframework.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by cysion.liu on 2016/6/21.
 * 图像工具类，目前包括:
 * 1--将bitmap转为byte数组
 * 2--将图片bitmap保存为文件
 * 3--二次采样从文件中获得小图片
 * 4--获得图片的旋转角度
 */
public class TdImgUtils {

    /**
     * Bitmap → byte[]
     * @param bm
     * @return byte数组
     */
    public static byte[] convertBitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    /**
     * @Description 将图片存入到文件
     * @param imagePath 目标文件的路径（包含名称）
     * @param bitmap 图片数据
     */
    public static void saveScaleBitmap(String imagePath, Bitmap bitmap) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imagePath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @Description 读取图片属性：旋转的角度
     * @param path	图片绝对路径
     * @return int	degree旋转的角度
     */
    public static int readPictureDegree(String path) {
        int degree  = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * @Description  根据路径获得图片并压缩，返回bitmap用于显示
     * @param filePath 图片路径
     * @param targetWidth 目标宽度
     * @param targetHeight 目标高度
     * @return 所需要大小的bitmap
     */
    public static Bitmap getSmallBitmap(String filePath, int targetWidth, int targetHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, targetWidth, targetHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);//真正去读取图片

        return BitmapFactory.decodeFile(filePath, options);
    }

    //计算图片的缩放值
    private static int calculateInSampleSize(BitmapFactory.Options options,int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height/ (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }
}
