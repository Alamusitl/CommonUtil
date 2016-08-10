package com.ksc.client.update;


import android.content.Context;

/**
 * Created by Alamusi on 2016/7/1.
 */
public class PatchClient {

    public static native int applyPatchToOldApk(String oldApkPath, String newApkPath, String patchFilePath);

    /**
     * 客户端合并老的APK和差异化文件
     *
     * @param oldApkPath    老的APK路径
     * @param newApkPath    新的APK路径
     * @param patchFilePath 差异化文件的路径
     * @return 返回0表示合并文件，生成新的APK成功
     */
    public static int applyPatch(String oldApkPath, String newApkPath, String patchFilePath) {
        return applyPatchToOldApk(oldApkPath, newApkPath, patchFilePath);
    }

    /**
     * 客户端合并老的APK和差异化文件
     *
     * @param context       当前上下文
     * @param newApkPath    生成的新的APK路径
     * @param patchFilePath 差异化文件的路径
     * @return 返回0表示合并文件，生成新的APK成功
     */
    public static int applyPatch(Context context, String newApkPath, String patchFilePath) {
        String oldApkPath = context.getApplicationInfo().sourceDir;
        return applyPatchToOldApk(oldApkPath, newApkPath, patchFilePath);
    }

}
