package com.hiroshi.cimoc.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Environment;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.azhon.appupdate.config.UpdateConfiguration;
import com.azhon.appupdate.manager.DownloadManager;
import com.hiroshi.cimoc.App;
import com.hiroshi.cimoc.Constants;
import com.hiroshi.cimoc.R;


import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by Hiroshi on 2016/8/24.
 */
public class Update {

    private static final String NAME = "name";
    private static final String TAG_NAME = "tag_name";
    private static final String ASSETS = "assets";
    private static String updateJson = "";

    public static Observable<String> check() {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                OkHttpClient client = App.getHttpClient();
                Request request = new Request.Builder().url(App.getUpdateCurrentUrl()).build();
                Response response = null;
                try {
                    response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        String json = response.body().string();
                        updateJson = json;
                        JSONObject object = JSON.parseObject(json);
                        String version = object.getString(TAG_NAME).toLowerCase();
                        subscriber.onNext(version);
                        subscriber.onCompleted();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (response != null) {
                        response.close();
                    }
                }
                subscriber.onError(new Exception());
            }
        }).subscribeOn(Schedulers.io());
    }

    @SuppressLint("DefaultLocale")
    public static boolean update(Context context) {
        try {
//                DownloadManager.getInstance().release();
            JSONObject updateObject = JSON.parseObject(Update.getUpdateJson());
            JSONObject updateAssetsObject = updateObject.getJSONArray(ASSETS).getJSONObject(0);

            UpdateConfiguration configuration = new UpdateConfiguration()
                    //输出错误日志
                    .setEnableLog(true)
                    //设置自定义的下载
                    //.setHttpManager()
                    //下载完成自动跳动安装页面
                    .setJumpInstallPage(true)
                    //设置对话框背景图片 (图片规范参照demo中的示例图)
                    .setDialogImage(R.drawable.ic_dialog_download_top_3)
                    //设置按钮的颜色
                    .setDialogButtonColor(Color.parseColor("#39c1e9"))
                    //设置按钮的文字颜色
                    .setDialogButtonTextColor(Color.WHITE)
                    //支持断点下载
                    .setBreakpointDownload(true)
                    //设置是否显示通知栏进度
                    .setShowNotification(true)
                    //设置强制更新
                    .setForcedUpgrade(false);

            DownloadManager manager = DownloadManager.getInstance(context);
            manager.setApkName("Comic." + updateObject.getString(NAME) + ".release.apk")
                    .setApkUrl(updateAssetsObject.getString("browser_download_url"))
                    .setDownloadPath(Environment.getExternalStorageDirectory() + "/Download")
                    .setApkDescription(updateObject.getString("body"))
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setShowNewerToast(true)
                    .setConfiguration(configuration)
                    .setApkVersionCode(2)
                    .setApkVersionName(updateObject.getString(TAG_NAME).substring(1));

            if (App.getUpdateCurrentUrl().equals(Constants.UPDATE_GITEE_URL)) {
                manager.download();
            } else {
                manager.setApkSize(String.format("%.2f", updateAssetsObject.getDouble("size") / (1024 * 1024)))
                        .download();
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getUpdateJson() {
        return updateJson;
    }
}
