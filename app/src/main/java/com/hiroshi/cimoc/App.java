package com.hiroshi.cimoc;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.support.multidex.MultiDex;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.hiroshi.cimoc.component.AppGetter;
import com.hiroshi.cimoc.core.Storage;
import com.hiroshi.cimoc.fresco.ControllerBuilderProvider;
import com.hiroshi.cimoc.helper.DBOpenHelper;
import com.hiroshi.cimoc.helper.UpdateHelper;
import com.hiroshi.cimoc.manager.PreferenceManager;
import com.hiroshi.cimoc.manager.SourceManager;
import com.hiroshi.cimoc.misc.ActivityLifecycle;
import com.hiroshi.cimoc.model.DaoMaster;
import com.hiroshi.cimoc.model.DaoSession;
import com.hiroshi.cimoc.network.HttpDns;
import com.hiroshi.cimoc.saf.DocumentFile;
import com.hiroshi.cimoc.ui.adapter.GridAdapter;
import com.hiroshi.cimoc.utils.DocumentUtils;
import com.hiroshi.cimoc.utils.StringUtils;

import org.greenrobot.greendao.identityscope.IdentityScopeType;

import java.net.InetSocketAddress;
import java.net.Proxy;

import okhttp3.OkHttpClient;

/**
 * Created by Hiroshi on 2016/7/5.
 */
public class App extends Application implements AppGetter, Thread.UncaughtExceptionHandler {

    public static int mWidthPixels;
    public static int mHeightPixels;
    public static int mCoverWidthPixels;
    public static int mCoverHeightPixels;
    public static int mLargePixels;

    private static OkHttpClient mHttpClient;

    private DocumentFile mDocumentFile;
    private PreferenceManager mPreferenceManager;
    private ControllerBuilderProvider mBuilderProvider;
    private RecyclerView.RecycledViewPool mRecycledPool;
    private DaoSession mDaoSession;
    private ActivityLifecycle mActivityLifecycle;

    public static OkHttpClient getHttpClient() {
        if (mHttpClient == null) {
            if (false) {
                mHttpClient = new OkHttpClient.Builder()
                        .dns(new HttpDns())
//                https://t.me/proxy?server=sean.taipei&port=9487&secret=7c8b14f05c262263d245109fac1e38c8
                        .proxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("sean.taipei", 9487)))
                        .build();
            } else {
                mHttpClient = new OkHttpClient.Builder()
//                        .dns(new HttpDns())
//                        .proxy(new Proxy(Proxy.Type.HTTP,new InetSocketAddress("proxy", proxyPort)))
//                        .protocols(Collections.unmodifiableList(Arrays.asList(Protocol.HTTP_1_1)))//disable http2
                        .build();
            }
        }
        return mHttpClient;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(this);
        mActivityLifecycle = new ActivityLifecycle();
        registerActivityLifecycleCallbacks(mActivityLifecycle);
        mPreferenceManager = new PreferenceManager(this);
        DBOpenHelper helper = new DBOpenHelper(this, "cimoc.db");
        mDaoSession = new DaoMaster(helper.getWritableDatabase()).newSession(IdentityScopeType.None);
        UpdateHelper.update(mPreferenceManager, getDaoSession());
        Fresco.initialize(this);
        initPixels();
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
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
            DocumentFile file = DocumentUtils.getOrCreateFile(dir, StringUtils.getDateStringWithSuffix("log"));
            DocumentUtils.writeStringToFile(getContentResolver(), file, sb.toString());
        } catch (Exception ex) {
        }
        mActivityLifecycle.clear();
        System.exit(1);
    }

    @Override
    public App getAppInstance() {
        return this;
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

    public PreferenceManager getPreferenceManager() {
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
                    SourceManager.getInstance(this).new HeaderGetter(), true);
        }
        return mBuilderProvider;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

}
