package com.OnlyX.core;

//import com.alibaba.fastjson.JSONObject;
import com.OnlyX.App;

import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.schedulers.Schedulers;

//import com.azhon.appupdate.config.UpdateConfiguration;
//import com.azhon.appupdate.manager.DownloadManager;

/**
 * Created by Hiroshi on 2016/8/24.
 */
public class Update {

    private static final String UPDATE_URL = "https://api.github.com/repos/onlytheworld/onlyx/releases/latest";
    private static final String SERVER_FILENAME = "tag_name";
//    private static final String LIST = "list";

    public static Observable<String> check() {
        return Observable.create((Observable.OnSubscribe<String>) subscriber -> {
            OkHttpClient client = App.getHttpClient();
            Request request = new Request.Builder().url(UPDATE_URL).build();
            try {
                assert client != null;
                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        assert response.body() != null;
                        String json = response.body().string();
    //                        JSONObject object = new JSONObject(json).getJSONArray(LIST).getJSONObject(0);
                        String version = new JSONObject(json).getString(SERVER_FILENAME);
                        subscriber.onNext(version);
                        subscriber.onCompleted();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            subscriber.onError(new Exception());
        }).subscribeOn(Schedulers.io());
    }


//    @SuppressLint("DefaultLocale")
//    public static boolean update(Context context) {
//        try {
////                DownloadManager.getInstance().release();
//            JSONObject updateObject = JSON.parseObject(Update.getUpdateJson());
//            JSONObject updateAssetsObject = updateObject.getJSONArray(ASSETS).getJSONObject(0);
//
//            UpdateConfiguration configuration = new UpdateConfiguration()
//                    //输出错误日志
//                    .setEnableLog(true)
//                    //设置自定义的下载
//                    //.setHttpManager()
//                    //下载完成自动跳动安装页面
//                    .setJumpInstallPage(true)
//                    //设置对话框背景图片 (图片规范参照demo中的示例图)
//                    .setDialogImage(R.drawable.ic_dialog_download_top_3)
//                    //设置按钮的颜色
//                    .setDialogButtonColor(Color.parseColor("#39c1e9"))
//                    //设置按钮的文字颜色
//                    .setDialogButtonTextColor(Color.WHITE)
//                    //支持断点下载
//                    .setBreakpointDownload(true)
//                    //设置是否显示通知栏进度
//                    .setShowNotification(true)
//                    //设置强制更新
//                    .setForcedUpgrade(false);
//
//            DownloadManager manager = DownloadManager.getInstance(context);
//            manager.setApkName("Comic." + updateObject.getString(NAME) + ".release.apk")
//                    .setApkUrl(updateAssetsObject.getString("browser_download_url"))
//                    .setDownloadPath(Environment.getExternalStorageDirectory() + "/Download")
//                    .setApkDescription(updateObject.getString("body"))
//                    .setSmallIcon(R.mipmap.ic_launcher_blue_foreground)
//                    .setShowNewerToast(true)
//                    .setConfiguration(configuration)
//                    .setApkVersionCode(2)
//                    .setApkVersionName(updateObject.getString(TAG_NAME).substring(1));
//
//            if (App.getUpdateCurrentUrl().equals(Constants.UPDATE_GITEE_URL)) {
//                manager.download();
//            } else {
//                manager.setApkSize(String.format("%.2f", updateAssetsObject.getDouble("size") / (1024 * 1024)))
//                        .download();
//            }
//
//            return true;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    public static String getUpdateJson() {
//        return updateJson;
//    }
}
