package com.ksc.client.core.update;


import android.content.Context;

/**
 * Created by Alamusi on 2016/7/1.
 */
public class PatchClient {

    public static native int applyPatchToOldApk(String oldApkPath, String newApkPath, String patchFilePath);

    public static void loadLib() {
        System.loadLibrary("PatchClient");
    }

    public static int applyPatch(String oldApkPath, String newApkPath, String patchFilePath) {
        return applyPatchToOldApk(oldApkPath, newApkPath, patchFilePath);
    }

    public static int applyPatch(Context context, String newApkPath, String patchFilePath) {
        String oldApkPath = context.getApplicationInfo().sourceDir;
        return applyPatchToOldApk(oldApkPath, newApkPath, patchFilePath);
    }

}
