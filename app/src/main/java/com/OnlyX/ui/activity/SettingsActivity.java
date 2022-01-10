package com.OnlyX.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.OnlyX.App;
import com.OnlyX.R;
import com.OnlyX.global.Extra;
import com.OnlyX.manager.PreferenceManager;
import com.OnlyX.manager.SourceManager;
import com.OnlyX.model.Source;
import com.OnlyX.presenter.BasePresenter;
import com.OnlyX.presenter.SettingsPresenter;
import com.OnlyX.saf.DocumentFile;
import com.OnlyX.service.DownloadService;
import com.OnlyX.source.Luo;
import com.OnlyX.ui.activity.settings.ReaderConfigActivity;
import com.OnlyX.ui.fragment.dialog.MessageDialogFragment;
import com.OnlyX.ui.fragment.dialog.StorageEditorDialogFragment;
import com.OnlyX.ui.view.SettingsView;
import com.OnlyX.ui.widget.preference.CheckBoxPreference;
import com.OnlyX.ui.widget.preference.ChoicePreference;
import com.OnlyX.ui.widget.preference.SliderPreference;
import com.OnlyX.utils.HintUtils;
import com.OnlyX.utils.PermissionUtils;
import com.OnlyX.utils.ServiceUtils;
import com.OnlyX.utils.ThemeUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/9/21.
 */

public class SettingsActivity extends BackActivity implements SettingsView {

    private static final int DIALOG_REQUEST_OTHER_LAUNCH = 0;
    private static final int DIALOG_REQUEST_READER_MODE = 1;
    private static final int DIALOG_REQUEST_OTHER_THEME = 2;
    private static final int DIALOG_REQUEST_OTHER_STORAGE = 3;
    private static final int DIALOG_REQUEST_DOWNLOAD_THREAD = 4;
    private static final int DIALOG_REQUEST_DOWNLOAD_SCAN = 6;
    private static final int DIALOG_REQUEST_OTHER_NIGHT_ALPHA = 7;
    private static final int DIALOG_REQUEST_READER_SCALE_FACTOR = 8;
    private static final int DIALOG_REQUEST_READER_CONTROLLER_TRIG_THRESHOLD = 9;

    @BindViews({R.id.settings_reader_title, R.id.settings_download_title, R.id.settings_other_title, R.id.settings_search_title})
    List<TextView> mTitleList;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.settings_layout)
    View mSettingsLayout;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.settings_reader_keep_bright)
    CheckBoxPreference mReaderKeepBright;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.settings_reader_hide_info)
    CheckBoxPreference mReaderHideInfo;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.settings_reader_hide_nav)
    CheckBoxPreference mReaderHideNav;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.settings_reader_ban_double_click)
    CheckBoxPreference mReaderBanDoubleClick;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.settings_reader_paging)
    CheckBoxPreference mReaderPaging;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.settings_reader_paging_reverse)
    CheckBoxPreference mReaderPagingReverse;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.settings_reader_white_edge)
    CheckBoxPreference mReaderWhiteEdge;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.settings_reader_white_background)
    CheckBoxPreference mReaderWhiteBackground;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.settings_search_auto_complete)
    CheckBoxPreference mSearchAutoComplete;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.settings_other_check_update)
    CheckBoxPreference mCheckUpdate;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.settings_reader_mode)
    ChoicePreference mReaderMode;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.settings_other_launch)
    ChoicePreference mOtherLaunch;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.settings_other_theme)
    ChoicePreference mOtherTheme;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.settings_reader_scale_factor)
    SliderPreference mReaderScaleFactor;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.settings_reader_controller_trig_threshold)
    SliderPreference mReaderControllerTrigThreshold;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.settings_reader_show_topbar)
    CheckBoxPreference mOtherShowTopbar;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.settings_other_night_alpha)
    SliderPreference mOtherNightAlpha;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.settings_download_thread)
    SliderPreference mDownloadThread;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.settings_other_connect_only_wifi)
    CheckBoxPreference mConnectOnlyWifi;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.settings_other_loadcover_only_wifi)
    CheckBoxPreference mLoadCoverOnlyWifi;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.settings_firebase_event)
    CheckBoxPreference mFireBaseEvent;

    private SettingsPresenter mPresenter;

    private String mStoragePath;
    private String mTempStorage;

    private final int[] mResultArray = new int[6];
    private final Intent mResultIntent = new Intent();

    @Override
    protected BasePresenter initPresenter() {
        mPresenter = new SettingsPresenter();
        mPresenter.attachView(this);
        return mPresenter;
    }

    @Override
    protected void initView() {
        super.initView();
//        getSupportFragmentManager()
//                .beginTransaction()
//                .replace(R.id.activity_settings_container, new SettingsFragment())
//                .commit();
        mStoragePath = getApplication().getFilesDir().toString();
        mReaderKeepBright.bindPreference(PreferenceManager.PREF_READER_KEEP_BRIGHT, false);
        mReaderHideInfo.bindPreference(PreferenceManager.PREF_READER_HIDE_INFO, false);
        mReaderHideNav.bindPreference(PreferenceManager.PREF_READER_HIDE_NAV, false);
        mReaderBanDoubleClick.bindPreference(PreferenceManager.PREF_READER_BAN_DOUBLE_CLICK, false);
        mReaderPaging.bindPreference(PreferenceManager.PREF_READER_PAGING, false);
        mReaderPagingReverse.bindPreference(PreferenceManager.PREF_READER_PAGING_REVERSE, false);
        mReaderWhiteEdge.bindPreference(PreferenceManager.PREF_READER_WHITE_EDGE, false);
        mReaderWhiteBackground.bindPreference(PreferenceManager.PREF_READER_WHITE_BACKGROUND, false);
        mSearchAutoComplete.bindPreference(PreferenceManager.PREF_SEARCH_AUTO_COMPLETE, false);
        mCheckUpdate.bindPreference(PreferenceManager.PREF_OTHER_CHECK_UPDATE, false);
        mConnectOnlyWifi.bindPreference(PreferenceManager.PREF_OTHER_CONNECT_ONLY_WIFI, false);
        mLoadCoverOnlyWifi.bindPreference(PreferenceManager.PREF_OTHER_LOADCOVER_ONLY_WIFI, false);
        mFireBaseEvent.bindPreference(PreferenceManager.PREF_OTHER_FIREBASE_EVENT, true);
        mOtherShowTopbar.bindPreference(PreferenceManager.PREF_OTHER_SHOW_TOPBAR, false);
        mReaderMode.bindPreference(getSupportFragmentManager(), PreferenceManager.PREF_READER_MODE,
                PreferenceManager.READER_MODE_PAGE, R.array.reader_mode_items, DIALOG_REQUEST_READER_MODE);
        mOtherLaunch.bindPreference(getSupportFragmentManager(), PreferenceManager.PREF_OTHER_LAUNCH,
                PreferenceManager.HOME_FAVORITE, R.array.launch_items, DIALOG_REQUEST_OTHER_LAUNCH);
        mOtherTheme.bindPreference(getSupportFragmentManager(), PreferenceManager.PREF_OTHER_THEME,
                ThemeUtils.THEME_BLUE, R.array.theme_items, DIALOG_REQUEST_OTHER_THEME);
        mReaderScaleFactor.bindPreference(getSupportFragmentManager(), PreferenceManager.PREF_READER_SCALE_FACTOR, 200,
                R.string.settings_reader_scale_factor, DIALOG_REQUEST_READER_SCALE_FACTOR);
        mReaderControllerTrigThreshold.bindPreference(getSupportFragmentManager(), PreferenceManager.PREF_READER_CONTROLLER_TRIG_THRESHOLD, 30,
                R.string.settings_reader_controller_trig_threshold, DIALOG_REQUEST_READER_CONTROLLER_TRIG_THRESHOLD);
        mOtherNightAlpha.bindPreference(getSupportFragmentManager(), PreferenceManager.PREF_OTHER_NIGHT_ALPHA, 0xB0,
                R.string.settings_other_night_alpha, DIALOG_REQUEST_OTHER_NIGHT_ALPHA);
        mDownloadThread.bindPreference(getSupportFragmentManager(), PreferenceManager.PREF_DOWNLOAD_THREAD, 2,
                R.string.settings_download_thread, DIALOG_REQUEST_DOWNLOAD_THREAD);
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.settings_reader_config)
    void onReaderConfigBtnClick() {
        Intent intent = new Intent(this, ReaderConfigActivity.class);
        startActivity(intent);
    }

    @SuppressLint("WrongConstant")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == DIALOG_REQUEST_OTHER_STORAGE) {
                showProgressDialog();
                Uri uri = data.getData();
                int flags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                getContentResolver().takePersistableUriPermission(uri, flags);
                mTempStorage = uri.toString();
                mPresenter.moveFiles(DocumentFile.fromTreeUri(this, uri));
            }
        }
    }

    @Override
    public void onDialogResult(int requestCode, Bundle bundle) {
        switch (requestCode) {
            case DIALOG_REQUEST_READER_MODE:
                mReaderMode.setValue(bundle.getInt(EXTRA_DIALOG_RESULT_INDEX));
                break;
            case DIALOG_REQUEST_READER_SCALE_FACTOR:
                mReaderScaleFactor.setValue(bundle.getInt(EXTRA_DIALOG_RESULT_VALUE));
                break;
            case DIALOG_REQUEST_READER_CONTROLLER_TRIG_THRESHOLD:
                mReaderControllerTrigThreshold.setValue(bundle.getInt(EXTRA_DIALOG_RESULT_VALUE));
                break;
            case DIALOG_REQUEST_OTHER_LAUNCH:
                mOtherLaunch.setValue(bundle.getInt(EXTRA_DIALOG_RESULT_INDEX));
                break;
            case DIALOG_REQUEST_OTHER_THEME:
                int index = bundle.getInt(EXTRA_DIALOG_RESULT_INDEX);
                if (mOtherTheme.getValue() != index) {
                    mOtherTheme.setValue(index);
                    int theme = ThemeUtils.getThemeById(index);
                    setTheme(theme);
                    int primary = ThemeUtils.getResourceId(this, R.attr.colorPrimary);
                    int accent = ThemeUtils.getResourceId(this, R.attr.colorAccent);
                    changeTheme(primary, accent);
                    mResultArray[0] = 1;
                    mResultArray[1] = theme;
                    mResultArray[2] = primary;
                    mResultArray[3] = accent;
                    mResultIntent.putExtra(Extra.EXTRA_RESULT, mResultArray);
                    setResult(Activity.RESULT_OK, mResultIntent);
                }
                break;
            case DIALOG_REQUEST_OTHER_STORAGE:
                showSnackbar(R.string.settings_other_storage_not_found);
                break;
            case DIALOG_REQUEST_DOWNLOAD_THREAD:
                mDownloadThread.setValue(bundle.getInt(EXTRA_DIALOG_RESULT_VALUE));
                break;
            case DIALOG_REQUEST_DOWNLOAD_SCAN:
                showProgressDialog();
                mPresenter.scanTask();
                break;
            case DIALOG_REQUEST_OTHER_NIGHT_ALPHA:
                int alpha = bundle.getInt(EXTRA_DIALOG_RESULT_VALUE);
                mOtherNightAlpha.setValue(alpha);
                if (mNightMask != null) {
                    mNightMask.setBackgroundColor(alpha << 24);
                }
                mResultArray[4] = 1;
                mResultArray[5] = alpha;
                mResultIntent.putExtra(Extra.EXTRA_RESULT, mResultArray);
                setResult(Activity.RESULT_OK, mResultIntent);
                break;
        }
    }

    private void changeTheme(int primary, int accent) {
        if (mToolbar != null) {
            mToolbar.setBackgroundColor(ContextCompat.getColor(this, primary));
        }
        for (TextView textView : mTitleList) {
            textView.setTextColor(ContextCompat.getColor(this, primary));
        }
        ColorStateList stateList = new ColorStateList(new int[][]{{-android.R.attr.state_checked}, {android.R.attr.state_checked}},
                new int[]{0x8A000000, ContextCompat.getColor(this, accent)});
        mReaderKeepBright.setColorStateList(stateList);
        mReaderHideInfo.setColorStateList(stateList);
        mReaderHideNav.setColorStateList(stateList);
        mReaderBanDoubleClick.setColorStateList(stateList);
        mReaderPaging.setColorStateList(stateList);
        mReaderPagingReverse.setColorStateList(stateList);
        mReaderWhiteEdge.setColorStateList(stateList);
        mReaderWhiteBackground.setColorStateList(stateList);
        mSearchAutoComplete.setColorStateList(stateList);
        mCheckUpdate.setColorStateList(stateList);
        mConnectOnlyWifi.setColorStateList(stateList);
        mLoadCoverOnlyWifi.setColorStateList(stateList);
        mFireBaseEvent.setColorStateList(stateList);
        mOtherShowTopbar.setColorStateList(stateList);
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.settings_other_storage)
    void onOtherStorageClick() {
        if (ServiceUtils.isServiceRunning(this, DownloadService.class)) {
            showSnackbar(R.string.download_ask_stop);
        } else {
            if (!PermissionUtils.hasStoragePermission(this)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            }
            StorageEditorDialogFragment fragment = StorageEditorDialogFragment.newInstance(R.string.settings_other_storage,
                    mStoragePath, DIALOG_REQUEST_OTHER_STORAGE);
            fragment.show(getSupportFragmentManager(), null);
        }
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.settings_import_source)
    void onImportSourceClick() {
        if (ServiceUtils.isServiceRunning(this, DownloadService.class)) {
            showSnackbar(R.string.download_ask_stop);
        } else {
            try {
                List<Source> list = Luo.getDefaultSource(getAppInstance().getDaoSession().getSourceDao(), mStoragePath);
                getAppInstance().getDaoSession().getSourceDao().insertOrReplaceInTx(list);
                SourceManager.getInstance(this).updateManager(this);
                HintUtils.showToast(this, R.string.settings_import_source_success);
            } catch (Exception ignored) {
                HintUtils.showToast(this, R.string.settings_import_source_fail);
            }
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

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.settings_download_scan)
    void onDownloadScanClick() {
        if (ServiceUtils.isServiceRunning(this, DownloadService.class)) {
            showSnackbar(R.string.download_ask_stop);
        } else {
            MessageDialogFragment fragment = MessageDialogFragment.newInstance(R.string.dialog_confirm,
                    R.string.settings_download_scan_confirm, true, DIALOG_REQUEST_DOWNLOAD_SCAN);
            fragment.show(getSupportFragmentManager(), null);
        }
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.settings_other_clear_cache)
    void onOtherCacheClick() {
        showProgressDialog();
        mPresenter.clearCache();
        showSnackbar(R.string.common_execute_success);
        hideProgressDialog();
    }

    @Override
    public void onFileMoveSuccess() {
        hideProgressDialog();
        mPreference.putString(PreferenceManager.PREF_OTHER_STORAGE, mTempStorage);
        mStoragePath = mTempStorage;
        ((App) getApplication()).initRootDocumentFile();
        showSnackbar(R.string.common_execute_success);
    }

    @Override
    public void onExecuteSuccess() {
        hideProgressDialog();
        showSnackbar(R.string.common_execute_success);
    }

    @Override
    public void onExecuteFail() {
        hideProgressDialog();
        showSnackbar(R.string.common_execute_fail);
    }

    @Override
    protected String getDefaultTitle() {
        return getString(R.string.drawer_settings);
    }

    @Override
    protected View getLayoutView() {
        return mSettingsLayout;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_settings;
    }

}
