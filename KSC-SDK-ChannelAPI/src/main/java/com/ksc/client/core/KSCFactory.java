package com.ksc.client.core;

import android.content.Context;

import com.ksc.client.core.config.KSCSDKConstant;
import com.ksc.client.core.config.KSCSDKInfo;
import com.ksc.client.core.inner.ChannelBase;
import com.ksc.client.util.KSCHelpUtils;
import com.ksc.client.util.KSCLog;
import com.ksc.client.util.KSCStorageUtils;

import java.io.File;

/**
 * Created by Alamusi on 2016/6/22.
 */
public class KSCFactory {

    private static ChannelBase mChannel = null;

    protected static ChannelBase getChannel() {
        return mChannel;
    }

    public static void load(Context context) {
        KSCSDKInfo.loadLocalConfig(context);

        // 覆盖安装情况下，version比本地的version大，重新加载
        if (KSCHelpUtils.compare(KSCSDKInfo.getBuildVersionFromAssetsConfig(context), KSCSDKInfo.getBuildVersion()) > 0) {
            File file = KSCStorageUtils.getFile(context, KSCSDKConstant.SDK_DIR, KSCSDKConstant.CONFIG_FILE_NAME);
            KSCStorageUtils.deleteFile(file);
            if (!file.exists()) {
                KSCStorageUtils.copyFile(context, KSCSDKConstant.CONFIG_FILE_NAME, file);
            }
            KSCSDKInfo.loadAssetsConfig(context);
        }

        mChannel = loadClass(KSCSDKConstant.SDK_CHANNEL_CLASS_NAME);
    }

    private static <T> T loadClass(String className) {
        try {
            Class<?> clz = Class.forName(className);
            if (clz == null) {
                KSCLog.e("can not find class " + className);
                return null;
            }
            KSCLog.i("loaded : " + clz);
            return (T) clz.newInstance();
        } catch (ClassNotFoundException e) {
            KSCLog.w("can not find class " + className);
        } catch (Exception e) {
            KSCLog.e("can not create instance for class " + className, e);
        }
        return null;
    }
}
