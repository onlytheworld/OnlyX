package com.OnlyX;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;
import androidx.recyclerview.widget.RecyclerView;

import com.OnlyX.component.AppGetter;
import com.OnlyX.core.Storage;
import com.OnlyX.fresco.ControllerBuilderProvider;
import com.OnlyX.helper.DBOpenHelper;
import com.OnlyX.helper.UpdateHelper;
import com.OnlyX.manager.PreferenceManager;
import com.OnlyX.manager.SourceManager;
import com.OnlyX.misc.ActivityLifecycle;
import com.OnlyX.model.DaoMaster;
import com.OnlyX.model.DaoSession;
import com.OnlyX.saf.DocumentFile;
import com.OnlyX.ui.adapter.GridAdapter;
import com.OnlyX.utils.DocumentUtils;
import com.OnlyX.utils.StringUtils;
import com.facebook.drawee.backends.pipeline.Fresco;

import org.greenrobot.greendao.identityscope.IdentityScopeType;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

/**
 * Created by Hiroshi on 2016/7/5.
 */
public class App extends MultiDexApplication implements AppGetter, Thread.UncaughtExceptionHandler {

    public static int mWidthPixels;
    public static int mHeightPixels;
    public static int mCoverWidthPixels;
    public static int mCoverHeightPixels;
    public static int mLargePixels;

    private static OkHttpClient mHttpClient;

    private DocumentFile mDocumentFile;
    private static PreferenceManager mPreferenceManager;
    private ControllerBuilderProvider mBuilderProvider;
    private RecyclerView.RecycledViewPool mRecycledPool;
    private DaoSession mDaoSession;
    private ActivityLifecycle mActivityLifecycle;


    private static WifiManager manager_wifi;
    @SuppressLint("StaticFieldLeak")
    private static Activity sActivity;

    // 默认Github源
    private static String UPDATE_CURRENT_URL = "https://api.github.com/repos/onlytheworld/OnlyX/releases/latest";

    @Override
    public void onCreate() {
        super.onCreate();
        DBOpenHelper helper = new DBOpenHelper(this, "onlyx.db");
        mDaoSession = new DaoMaster(helper.getWritableDatabase()).newSession(IdentityScopeType.None);
        Thread.setDefaultUncaughtExceptionHandler(this);
        mActivityLifecycle = new ActivityLifecycle();
        registerActivityLifecycleCallbacks(mActivityLifecycle);
        mPreferenceManager = new PreferenceManager(this);
        UpdateHelper.update(this, mPreferenceManager, getDaoSession());
        Fresco.initialize(this);
        initPixels();

        manager_wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        //获取栈顶Activity以及当前App上下文
        this.registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                Log.d("ActivityLifecycle:", activity + "onActivityCreated");
            }

            @Override
            public void onActivityStarted(Activity activity) {
                Log.d("ActivityLifecycle:", activity + "onActivityStarted");
                sActivity = activity;

            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }

    @Override
    public void uncaughtException(@NonNull Thread t, Throwable e) {
        StringBuilder sb = new StringBuilder();
        sb.append("MODEL: ").append(Build.MODEL).append('\n');
        sb.append("SDK: ").append(Build.VERSION.SDK_INT).append('\n');
        sb.append("RELEASE: ").append(Build.VERSION.RELEASE).append('\n');
        sb.append('\n').append(e.getLocalizedMessage()).append('\n');
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append('\n');
            sb.append(element.toString());
        }
        try {
            DocumentFile doc = getDocumentFile();
            DocumentFile dir = DocumentUtils.getOrCreateSubDirectory(doc, "log");
            assert dir != null;
            DocumentFile file = DocumentUtils.getOrCreateFile(dir, StringUtils.getDateStringWithSuffix("log"));
            assert file != null;
            DocumentUtils.writeStringToFile(getContentResolver(), file, sb.toString());
        } catch (Exception ignored) {
        }
        mActivityLifecycle.clear();
        System.exit(1);
    }

    @Override
    public App getAppInstance() {
        return this;
    }

    public static Activity getActivity() {
        return sActivity;
    }

    public static WifiManager getManager_wifi() {
        return manager_wifi;
    }

    private void initPixels() {
        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
        mWidthPixels = metrics.widthPixels;
        mHeightPixels = metrics.heightPixels;
        mCoverWidthPixels = mWidthPixels / 3;
        mCoverHeightPixels = mHeightPixels * mCoverWidthPixels / mWidthPixels;
        mLargePixels = 3 * metrics.widthPixels * metrics.heightPixels;
    }

    public void initRootDocumentFile() {
        String uri = mPreferenceManager.getString(PreferenceManager.PREF_OTHER_STORAGE);
        if (uri == null) {
            uri = getFilesDir().toString();
        }
        mDocumentFile = Storage.initRoot(this, uri);
    }

    public DocumentFile getDocumentFile() {
        if (mDocumentFile == null) {
            initRootDocumentFile();
        }
        return mDocumentFile;
    }

    public DaoSession getDaoSession() {
        return mDaoSession;
    }

    public static PreferenceManager getPreferenceManager() {
        return mPreferenceManager;
    }

    public RecyclerView.RecycledViewPool getGridRecycledPool() {
        if (mRecycledPool == null) {
            mRecycledPool = new RecyclerView.RecycledViewPool();
            mRecycledPool.setMaxRecycledViews(GridAdapter.TYPE_GRID, 20);
        }
        return mRecycledPool;
    }

    public ControllerBuilderProvider getBuilderProvider() {
        if (mBuilderProvider == null) {
            mBuilderProvider = new ControllerBuilderProvider(getApplicationContext(),
                    SourceManager.getInstance(this).new SMGetter(), true);
        }
        return mBuilderProvider;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static void setUpdateCurrentUrl(String updateCurrentUrl) {
        UPDATE_CURRENT_URL = updateCurrentUrl;
    }

    public static String getUpdateCurrentUrl() {
        return UPDATE_CURRENT_URL;
    }

    public static OkHttpClient getHttpClient() {

        //OkHttpClient返回null实现"仅WiFi联网"，后面要注意空指针处理
        if (!manager_wifi.isWifiEnabled() && mPreferenceManager.getBoolean(PreferenceManager.PREF_OTHER_CONNECT_ONLY_WIFI, false)) {
            return null;
        }

        if (mHttpClient == null) {
//            Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("localhost", 1080));

            // 3.OkHttp访问https的Client实例
            OkHttpClient.Builder clientBuilder = new OkHttpClient().newBuilder();
            clientBuilder.cookieJar(new CookieJar() {
                private final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();

                @Override
                public void saveFromResponse(@NonNull HttpUrl url, @NonNull List<Cookie> cookies) {
                    List<Cookie> cookie = loadForRequest(url);
                    cookie.addAll(cookies);
                    for (Cookie item : cookies) {
                        boolean flag = false;
                        for (int i = 0; i < cookie.size(); i++) {
                            if (item.name().equals(cookie.get(i).name())) {
                                cookie.set(i, item);
                                flag = true;
                                break;
                            }
                        }
                        if (!flag) {
                            cookie.add(item);
                        }
                    }
                    cookieStore.put(url.host(), cookie);
                }

                @NonNull
                @Override
                public List<Cookie> loadForRequest(@NonNull HttpUrl url) {
                    List<Cookie> cookies = cookieStore.get(url.host());
                    return cookies != null ? cookies : new ArrayList<>();
                }
            });
            mHttpClient = clientBuilder.sslSocketFactory(createSSLSocketFactory())
                    .hostnameVerifier(new TrustAllHostnameVerifier())
//                    .proxy(proxy)
                    .build();

        }

        return mHttpClient;
    }

    // 1.实现X509TrustManager接口
    @SuppressLint("CustomX509TrustManager")
    private static class TrustAllCerts implements X509TrustManager {
        @SuppressLint("TrustAllX509TrustManager")
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }

        @SuppressLint("TrustAllX509TrustManager")
        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    // 2.实现HostnameVerifier接口
    private static class TrustAllHostnameVerifier implements HostnameVerifier {
        @SuppressLint("BadHostnameVerifier")
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    private static SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory ssfFactory = null;

        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new TrustAllCerts()}, new SecureRandom());

            ssfFactory = sc.getSocketFactory();
        } catch (Exception ignored) {
        }

        return ssfFactory;
    }


}
