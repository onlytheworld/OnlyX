package com.OnlyX.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.OnlyX.App;
import com.OnlyX.R;
import com.OnlyX.component.ThemeResponsive;
import com.OnlyX.fresco.ControllerBuilderProvider;
import com.OnlyX.global.Extra;
import com.OnlyX.manager.PreferenceManager;
import com.OnlyX.manager.SourceManager;
import com.OnlyX.presenter.BasePresenter;
import com.OnlyX.presenter.MainPresenter;
import com.OnlyX.ui.fragment.BaseFragment;
import com.OnlyX.ui.fragment.ComicFragment;
import com.OnlyX.ui.fragment.dialog.MessageDialogFragment;
import com.OnlyX.ui.fragment.recyclerview.SourceFragment;
import com.OnlyX.ui.view.MainView;
import com.OnlyX.utils.HintUtils;
import com.OnlyX.utils.PermissionUtils;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import butterknife.BindView;

//auth0

/**
 * Created by Hiroshi on 2016/7/1.
 */
public class MainActivity extends BaseActivity implements MainView, NavigationView.OnNavigationItemSelectedListener {

    private static final int DIALOG_REQUEST_NOTICE = 0;
    private static final int DIALOG_REQUEST_PERMISSION = 1;
    private static final int DIALOG_REQUEST_LOGOUT = 2;

    private static final int REQUEST_ACTIVITY_SETTINGS = 0;

    private static final int FRAGMENT_NUM = 3;

    @BindView(R.id.main_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.main_navigation_view)
    NavigationView mNavigationView;
    @BindView(R.id.main_fragment_container)
    FrameLayout mFrameLayout;

    private TextView mLastText;
    private SimpleDraweeView mDraweeView;
    private ControllerBuilderProvider mControllerBuilderProvider;

    private MainPresenter mPresenter;
    private ActionBarDrawerToggle mDrawerToggle;
    private long mExitTime = 0;
    private long mLastId = -1;
    private int mLastSource = -1;
    private String mLastCid;

    private int mCheckItem;
    private SparseArray<BaseFragment> mFragmentArray;
    private BaseFragment mCurrentFragment;
    private boolean night;

    //auth0
//    private Auth0 auth0;

    @Override
    protected BasePresenter<MainView> initPresenter() {
        mPresenter = new MainPresenter();
        mPresenter.attachView(this);
        return mPresenter;
    }

    @Override
    protected void initView() {
        initDrawerToggle();
        initNavigation();
        initFragment();
        mPresenter.loadLast();

        //检查App更新
        String updateUrl;
        if (mPreference.getBoolean(PreferenceManager.PREF_UPDATE_APP_AUTO, true)) {
            if ((updateUrl = App.getPreferenceManager().getString(PreferenceManager.PREF_UPDATE_CURRENT_URL)) != null) {
                App.setUpdateCurrentUrl(updateUrl);
            }

            checkUpdate();
        }
    }

//    private void login() {
//        HintUtils.showToast(MainActivity.this, R.string.user_login_tips);
//        WebAuthProvider.init(auth0)
//            .withScheme("demo")
//            .withScope("openid profile email")
//            .withAudience(String.format("https://%s/userinfo", getString(R.string.com_auth0_domain)))
//            .start(MainActivity.this, new AuthCallback() {
//                @Override
//                public void onFailure(@NonNull final Dialog dialog) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            dialog.show();
//                        }
//                    });
//                }
//
//                @Override
//                public void onFailure(final AuthenticationException exception) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
////                            Toast.makeText(MainActivity.this, "Error: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
//                            HintUtils.showToast(MainActivity.this, R.string.user_login_failed);
//                        }
//                    });
//                }
//
//                @Override
//                public void onSuccess(@NonNull final Credentials credentials) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
////                            Toast.makeText(MainActivity.this, "Logged in: " + credentials.getAccessToken(), Toast.LENGTH_LONG).show();
//                            HintUtils.showToast(MainActivity.this, R.string.user_login_sucess);
//                            mPreference.putString(PreferenceManager.PREFERENCES_USER_TOCKEN, credentials.getAccessToken());
//                            getUesrInfo();
//                        }
//                    });
//                }
//            });
//    }
//
//    private void logoutShowDialog(){
//        MessageDialogFragment fragment = MessageDialogFragment.newInstance(R.string.user_login_logout,
//            R.string.user_login_logout_tips, true, DIALOG_REQUEST_LOGOUT);
//        fragment.show(getFragmentManager(), null);
//    }
//
//    private void logout() {
//        HintUtils.showToast(MainActivity.this, R.string.user_login_logout_sucess);
//        mPreference.putString(PreferenceManager.PREFERENCES_USER_EMAIL, "");
//        mPreference.putString(PreferenceManager.PREFERENCES_USER_TOCKEN, "");
//        mPreference.putString(PreferenceManager.PREFERENCES_USER_NAME, "");
//        mPreference.putString(PreferenceManager.PREFERENCES_USER_ID, "");
//    }
//
//    private void loginout() {
//        if (mPreference.getString(PreferenceManager.PREFERENCES_USER_ID, "") == "") {
//            login();
//        } else {
//            logoutShowDialog();
//        }
//    }


//    public void getUesrInfo() {
//        String accessTocken = mPreference.getString(PreferenceManager.PREFERENCES_USER_TOCKEN, null);
//        if (accessTocken != null) {
//            AuthenticationAPIClient authentication = new AuthenticationAPIClient(auth0);
//            authentication
//                .userInfo(accessTocken)
//                .start(new BaseCallback<UserProfile, AuthenticationException>() {
//                    @Override
//                    public void onSuccess(UserProfile information) {
//                        //user information received
//                        mPreference.putString(PreferenceManager.PREFERENCES_USER_EMAIL, information.getEmail());
//                        mPreference.putString(PreferenceManager.PREFERENCES_USER_NAME, information.getName());
//                        mPreference.putString(PreferenceManager.PREFERENCES_USER_ID, (String) information.getExtraInfo().get("sub"));
//                    }
//
//                    @Override
//                    public void onFailure(AuthenticationException error) {
//                        //user information request failed
//                        HintUtils.showToast(MainActivity.this, R.string.user_login_failed);
//                    }
//                });
//        } else {
//            HintUtils.showToast(MainActivity.this, R.string.user_login_failed);
//        }
//    }

//    @Override
//    protected void initUser() {
//        //auth0
//        auth0 = new Auth0(this);
//        auth0.setOIDCConformant(true);
//    }

    private void initDrawerToggle() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, 0, 0) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (refreshCurrentFragment()) {
                    getSupportFragmentManager().beginTransaction().show(mCurrentFragment).commit();
                } else {
                    getSupportFragmentManager().beginTransaction().add(R.id.main_fragment_container, mCurrentFragment).commit();
                }
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void initNavigation() {
        night = mPreference.getBoolean(PreferenceManager.PREF_NIGHT, false);
        mNavigationView.getMenu().findItem(R.id.drawer_night).setTitle(night ? R.string.drawer_light : R.string.drawer_night);
//        mNavigationView.getMenu().findItem(R.id.user_info)
//            .setTitle(
//                mPreference.getString(PreferenceManager.PREFERENCES_USER_NAME, "") == "" ?
//                    getString(R.string.user_login_item) :
//                    mPreference.getString(PreferenceManager.PREFERENCES_USER_NAME, "User")
//            );
        mNavigationView.setNavigationItemSelectedListener(this);
        View header = mNavigationView.getHeaderView(0);
        mLastText = header.findViewById(R.id.drawer_last_title);
        mDraweeView = header.findViewById(R.id.drawer_last_cover);
        mLastText.setOnClickListener(v -> {
            if (mPresenter.checkLocal(mLastId)) {
                Intent intent = TaskActivity.createIntent(MainActivity.this, mLastId);
                startActivity(intent);
            } else if (mLastSource != -1 && mLastCid != null) {
                Intent intent = DetailActivity.createIntent(MainActivity.this, null, mLastSource, mLastCid);
                startActivity(intent);
            } else {
                HintUtils.showToast(MainActivity.this, R.string.common_execute_fail);
            }
        });
        mControllerBuilderProvider = new ControllerBuilderProvider(this,
                SourceManager.getInstance(this).new SMGetter(), false);
    }

    private void initFragment() {
        int home = mPreference.getInt(PreferenceManager.PREF_OTHER_LAUNCH, PreferenceManager.HOME_FAVORITE);
        switch (home) {
            default:
            case PreferenceManager.HOME_FAVORITE:
            case PreferenceManager.HOME_HISTORY:
            case PreferenceManager.HOME_DOWNLOAD:
                mCheckItem = R.id.drawer_comic;
                break;
            case PreferenceManager.HOME_SOURCE:
                mCheckItem = R.id.drawer_source;
                break;
//            case PreferenceManager.HOME_TAG:
//                mCheckItem = R.id.drawer_tag;
//                break;
        }
        mNavigationView.setCheckedItem(mCheckItem);
        mFragmentArray = new SparseArray<>(FRAGMENT_NUM);
        refreshCurrentFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.main_fragment_container, mCurrentFragment).commit();
    }

    private boolean refreshCurrentFragment() {
        mCurrentFragment = mFragmentArray.get(mCheckItem);
        if (mCurrentFragment == null) {
            switch (mCheckItem) {
                case R.id.drawer_comic:
                    mCurrentFragment = new ComicFragment();
                    break;
                case R.id.drawer_source:
                    mCurrentFragment = new SourceFragment();
                    break;
//                case R.id.drawer_tag:
//                    mCurrentFragment = new TagFragment();
//                    break;
            }
            mFragmentArray.put(mCheckItem, mCurrentFragment);
            return false;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mControllerBuilderProvider.clear();
        ((App) getApplication()).getBuilderProvider().clear();
        ((App) getApplication()).getGridRecycledPool().clear();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else if (System.currentTimeMillis() - mExitTime > 2000) {
            HintUtils.showToast(this, R.string.main_double_click);
            mExitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId != mCheckItem) {
            switch (itemId) {
                case R.id.drawer_comic:
                case R.id.drawer_source:
//                case R.id.drawer_tag:
                    mCheckItem = itemId;
                    getSupportFragmentManager().beginTransaction().hide(mCurrentFragment).commit();
                    if (mToolbar != null) {
                        mToolbar.setTitle(item.getTitle().toString());
                    }
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                    break;
                case R.id.drawer_comic_list:
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.home_page_comiclist_url)));
                    try {
                        startActivity(intent);
                    } catch (Exception e) {
                        showSnackbar(R.string.about_resource_fail);
                    }
                    break;
                case R.id.drawer_night:
                    onNightSwitch();
                    mPreference.putBoolean(PreferenceManager.PREF_NIGHT, night);
                    break;
                case R.id.drawer_settings:
                    startActivityForResult(new Intent(MainActivity.this, SettingsActivity.class), REQUEST_ACTIVITY_SETTINGS);
                    break;
                case R.id.drawer_about:
                    startActivity(new Intent(MainActivity.this, AboutActivity.class));
                    break;
                case R.id.drawer_backup:
                    startActivity(new Intent(MainActivity.this, BackupActivity.class));
                    break;
//                case R.id.user_info:
//                    loginout();
//                    break;
            }
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCurrentFragment.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_ACTIVITY_SETTINGS) {
                int[] result = data.getIntArrayExtra(Extra.EXTRA_RESULT);
                if (result[0] == 1) {
                    changeTheme(result[1], result[2], result[3]);
                }
                if (result[4] == 1 && mNightMask != null) {
                    mNightMask.setBackgroundColor(result[5] << 24);
                }
            }
        }
    }

    @Override
    public void onDialogResult(int requestCode, Bundle bundle) {
        switch (requestCode) {
            case DIALOG_REQUEST_NOTICE:
                mPreference.putBoolean(PreferenceManager.PREF_MAIN_NOTICE, true);
                showPermission();
                break;
            case DIALOG_REQUEST_PERMISSION:
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                break;
//            case DIALOG_REQUEST_LOGOUT:
//                logout();
//                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ((App) getApplication()).initRootDocumentFile();
                HintUtils.showToast(this, R.string.main_permission_success);
            } else {
                HintUtils.showToast(this, R.string.main_permission_fail);
            }
        }
    }

    @Override
    public void onNightSwitch() {
        night = !night;
        mNavigationView.getMenu().findItem(R.id.drawer_night).setTitle(night ? R.string.drawer_light : R.string.drawer_night);
        if (mNightMask != null) {
            mNightMask.setVisibility(night ? View.VISIBLE : View.INVISIBLE);
        }
    }

    @Override
    public void onUpdateReady() {
        HintUtils.showToast(this, R.string.main_ready_update);
//        Update.update(this);
    }

    @Override
    public void onLastLoadSuccess(long id, int source, String cid, String title, String cover) {
        onLastChange(id, source, cid, title, cover);
    }

    @Override
    public void onLastLoadFail() {
        HintUtils.showToast(this, R.string.main_last_read_fail);
    }

    @Override
    public void onLastChange(long id, int source, String cid, String title, String cover) {
        mLastId = id;
        mLastSource = source;
        mLastCid = cid;
        mLastText.setText(title);
        ImageRequest request = ImageRequestBuilder
                .newBuilderWithSource(Uri.parse(cover))
                .setResizeOptions(new ResizeOptions(App.mWidthPixels, App.mHeightPixels))
                .build();
        DraweeController controller = mControllerBuilderProvider.get(source)
                .setOldController(mDraweeView.getController())
                .setImageRequest(request)
                .build();
        mDraweeView.setController(controller);
    }

    private void changeTheme(@StyleRes int theme, @ColorRes int primary, @ColorRes int accent) {
        setTheme(theme);
        ColorStateList itemList = new ColorStateList(new int[][]{{-android.R.attr.state_checked},
                {android.R.attr.state_checked}},
                new int[]{Color.BLACK, ContextCompat.getColor(this, accent)});
        mNavigationView.setItemTextColor(itemList);
        ColorStateList iconList = new ColorStateList(new int[][]{{-android.R.attr.state_checked},
                {android.R.attr.state_checked}},
                new int[]{0x8A000000, ContextCompat.getColor(this, accent)});
        mNavigationView.setItemIconTintList(iconList);
        mNavigationView.getHeaderView(0).setBackgroundColor(ContextCompat.getColor(this, primary));
        if (mToolbar != null) {
            mToolbar.setBackgroundColor(ContextCompat.getColor(this, primary));
        }

        for (int i = 0; i < mFragmentArray.size(); ++i) {
            ((ThemeResponsive) mFragmentArray.valueAt(i)).onThemeChange(primary, accent);
        }
    }

    private boolean showAuthorNotice() {
        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config);
        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        boolean updated = task.getResult();
                        Log.d("FireBase_FirstOpenMsg", "Config params updated: " + updated);
                    } else {
                        Log.d("FireBase_FirstOpenMsg", "Config params updated Failed. ");
                    }

                    String showMsg = mFirebaseRemoteConfig.getString("first_open_msg");
                    if (!mPreference.getBoolean(PreferenceManager.PREF_MAIN_NOTICE, false)
                            || showMsg.compareTo(mPreference.getString(PreferenceManager.PREF_MAIN_NOTICE_LAST, "")) != 0) {
                        mPreference.putString(PreferenceManager.PREF_MAIN_NOTICE_LAST, showMsg);
                        MessageDialogFragment fragment = MessageDialogFragment.newInstance(R.string.main_notice,
                                showMsg, false, DIALOG_REQUEST_NOTICE);
                        fragment.show(getSupportFragmentManager(), null);
                    }
                });

        return !mPreference.getBoolean(PreferenceManager.PREF_MAIN_NOTICE, false);
    }

    private void showPermission() {
        if (!PermissionUtils.hasStoragePermission(this)) {
            MessageDialogFragment fragment = MessageDialogFragment.newInstance(R.string.main_permission,
                    R.string.main_permission_content, false, DIALOG_REQUEST_PERMISSION);
            fragment.show(getSupportFragmentManager(), null);
        }
    }

    private void checkUpdate() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            mPresenter.checkUpdate(info.versionName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 疑似这里的 HOME_SOURCE 与 HOME_TAG 已被遗弃
    @Override
    protected String getDefaultTitle() {
        int home = mPreference.getInt(PreferenceManager.PREF_OTHER_LAUNCH, PreferenceManager.HOME_FAVORITE);
        switch (home) {
            default:
            case PreferenceManager.HOME_FAVORITE:
            case PreferenceManager.HOME_HISTORY:
            case PreferenceManager.HOME_DOWNLOAD:
            case PreferenceManager.HOME_LOCAL:
                return getString(R.string.drawer_comic);
            case PreferenceManager.HOME_SOURCE:
                return getString(R.string.drawer_source);
            case PreferenceManager.HOME_TAG:
                return getString(R.string.drawer_tag);
        }
    }


    protected int getLayoutRes() {
        return R.layout.activity_main;
    }

    @Override
    protected View getLayoutView() {
        return mDrawerLayout;
    }

}
