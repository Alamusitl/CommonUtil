package com.ksc.client.util;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Created by Alamusi on 2016/6/22.
 */
public class KSCStorageUtils {

    /**
     * 递归删除文件夹及文件夹下的所有文件
     *
     * @param dir 删除的文件夹
     */
    public static void deleteDir(File dir) {
        for (File file : dir.listFiles()) {
            if (file.getName().equals(".") || file.getName().equals("..")) {
                continue;
            }
            if (file.isFile()) {
                file.delete();
            }
            if (file.isDirectory()) {
                deleteDir(file);
            }
        }
        dir.delete();
    }

    /**
     * 判断文件夹下是否存在文件
     *
     * @param context 当前context
     * @param path    判断的路径
     * @return
     */
    public static boolean isContainFile(Context context, String path) {
        boolean isExist = false;
        File file = getDir(context, path);
        if (file != null && file.listFiles().length > 0) {
            isExist = true;
        }
        return isExist;
    }

    /**
     * 获得/data/data/package/path文件夹
     *
     * @param context 当前context
     * @return
     */
    public static File getDir(Context context, String path) {
        if (context == null || path == null) {
            return null;
        }
        File file = context.getDir(path, Context.MODE_PRIVATE);
        return file;
    }

    /**
     * 获得/data/data/package/path/fileName文件
     *
     * @param context  当前的Context
     * @param path     文件路径
     * @param fileName 文件名字
     * @return
     */
    public static File getFile(Context context, String path, String fileName) {
        if (context == null || path == null || fileName == null) {
            return null;
        }
        File file = new File(context.getDir(path, Context.MODE_PRIVATE), fileName);
        return file;
    }

    /**
     * 从assets目录下拷贝jar文件到
     *
     * @param context  当前Context
     * @param fileName 拷贝的源文件
     * @param dest     目标文件
     * @return
     */
    public static boolean copyFile(Context context, String fileName, File dest) {
        OutputStream output = null;
        BufferedInputStream bis = null;
        try {
            int BUF_SIZE = 8 * 1024;
            bis = new BufferedInputStream(context.getAssets().open(fileName));
            output = new BufferedOutputStream(new FileOutputStream(dest));
            byte[] buf = new byte[BUF_SIZE];
            int len = 0;
            while ((len = bis.read(buf, 0, BUF_SIZE)) > 0) {
                output.write(buf, 0, len);
            }
            output.close();
            bis.close();
            return true;
        } catch (IOException e) {
            KSCLog.e("can not find " + fileName + " from assets");
            if (output != null) {
                try {
                    output.close();
                } catch (IOException ioe) {
                    KSCLog.e("error in close stream", ioe);
                }
            }
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException ioe) {
                    KSCLog.e("error in close stream", ioe);
                }
            }
        }
        return false;
    }

    /**
     * 从文件获得输入流
     *
     * @param file 获得输入流的文件
     * @return
     */
    public static InputStream getInputStream(File file) {
        if (file == null) {
            return null;
        }
        InputStream fis = null;
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            KSCLog.e("file not found ", e);
        }
        return fis;
    }

    /**
     * 删除指定的文件
     *
     * @param file
     */
    public static void deleteFile(File file) {
        if (file.exists() && file.isFile()) {
            file.delete();
        }
    }

    /**
     * 获得SD卡总大小
     *
     * @param context
     * @return
     */
    public static String getSDCardTotalSize(Context context) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            KSCLog.e("sd card is not available, please check!");
            return null;
        }
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize;
        long blockCount;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            blockSize = stat.getBlockSizeLong();
            blockCount = stat.getBlockCountLong();
        } else {
            blockSize = stat.getBlockSize();
            blockCount = stat.getBlockCount();
        }
        return Formatter.formatFileSize(context, blockSize * blockCount);
    }

    /**
     * 获得SD卡可用的大小
     *
     * @param context
     * @return
     */
    public static String getSDCardAvailableSize(Context context) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            KSCLog.e("sd card is not available, please check!");
            return null;
        }
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize;
        long availableBlockCount;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            blockSize = stat.getBlockSizeLong();
            availableBlockCount = stat.getAvailableBlocksLong();
        } else {
            blockSize = stat.getBlockSize();
            availableBlockCount = stat.getAvailableBlocks();
        }
        return Formatter.formatFileSize(context, blockSize * availableBlockCount);
    }

    /**
     * 获得内部存储总大小
     *
     * @param context
     * @return
     */
    public static String getRomTotalSize(Context context) {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize;
        long blockCount;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            blockSize = stat.getBlockSizeLong();
            blockCount = stat.getBlockCountLong();
        } else {
            blockSize = stat.getBlockSize();
            blockCount = stat.getBlockCount();
        }
        return Formatter.formatFileSize(context, blockSize * blockCount);
    }

    /**
     * 获得内部存储可用大小
     *
     * @param context
     * @return
     */
    public static String getRomAvailableSize(Context context) {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize;
        long availableBlockCount;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            blockSize = stat.getBlockSizeLong();
            availableBlockCount = stat.getAvailableBlocksLong();
        } else {
            blockSize = stat.getBlockSize();
            availableBlockCount = stat.getAvailableBlocks();
        }
        return Formatter.formatFileSize(context, blockSize * availableBlockCount);
    }
}
