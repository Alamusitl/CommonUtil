package com.ksc.client.core.api;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.webkit.MimeTypeMap;

/**
 * Created by Alamusi on 2016/7/4.
 */
public class KSCDownloadService {

    private static DownloadManager mDownloadManager;

    public static void startDownload(Context context, String url, String destPath) {
        if (mDownloadManager == null) {
            mDownloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        }
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        request.setMimeType(MimeTypeMap.getFileExtensionFromUrl(url));
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "");
        request.setVisibleInDownloadsUi(true);
        long id = mDownloadManager.enqueue(request);

    }
}
