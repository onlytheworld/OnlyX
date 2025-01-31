package com.OnlyX.ui.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import com.OnlyX.R;
import com.OnlyX.manager.PreferenceManager;
import com.OnlyX.presenter.BackupPresenter;
import com.OnlyX.presenter.BasePresenter;
import com.OnlyX.ui.fragment.dialog.ChoiceDialogFragment;
import com.OnlyX.ui.view.BackupView;
import com.OnlyX.ui.widget.preference.CheckBoxPreference;
import com.OnlyX.utils.PermissionUtils;
import com.OnlyX.utils.StringUtils;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/10/19.
 */

public class BackupActivity extends BackActivity implements BackupView {

    private static final int DIALOG_REQUEST_RESTORE_COMIC = 0;
    private static final int DIALOG_REQUEST_RESTORE_TAG = 1;
    private static final int DIALOG_REQUEST_RESTORE_SETTINGS = 2;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.backup_layout)
    View mLayoutView;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.backup_save_comic_auto)
    CheckBoxPreference mSaveComicAuto;

    private BackupPresenter mPresenter;

    @Override
    protected BasePresenter initPresenter() {
        mPresenter = new BackupPresenter();
        mPresenter.attachView(this);
        return mPresenter;
    }

    @Override
    protected void initView() {
        super.initView();
        mSaveComicAuto.bindPreference(PreferenceManager.PREF_BACKUP_SAVE_COMIC, true);
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.backup_save_comic)
    void onSaveFavoriteClick() {
        showProgressDialog();
        if (PermissionUtils.hasStoragePermission(this)) {
            mPresenter.saveComic();
        } else {
            mPresenter.saveComic();
        }
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.backup_save_tag)
    void onSaveTagClick() {
        showProgressDialog();
        if (PermissionUtils.hasStoragePermission(this)) {
            mPresenter.saveTag();
        } else {
            mPresenter.saveTag();
        }
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.backup_save_settings)
    void onSaveSettingsClick() {
        showProgressDialog();
        if (PermissionUtils.hasStoragePermission(this)) {
            mPresenter.saveSettings();
        } else {
            mPresenter.saveSettings();
        }
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.backup_restore_comic)
    void onRestoreFavoriteClick() {
        showProgressDialog();
        if (PermissionUtils.hasStoragePermission(this)) {
            mPresenter.loadComicFile();
        } else {
            mPresenter.loadComicFile();
        }
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.backup_restore_tag)
    void onRestoreTagClick() {
        showProgressDialog();
        if (PermissionUtils.hasStoragePermission(this)) {
            mPresenter.loadTagFile();
        } else {
            mPresenter.loadTagFile();
        }
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.backup_restore_settings)
    void onRestoreSettingsClick() {
        showProgressDialog();
        if (PermissionUtils.hasStoragePermission(this)) {
            mPresenter.loadSettingsFile();
        } else {
            mPresenter.loadSettingsFile();
        }
    }

    @Override
    public void onDialogResult(int requestCode, Bundle bundle) {
        switch (requestCode) {
            case DIALOG_REQUEST_RESTORE_COMIC:
                showProgressDialog();
                mPresenter.restoreComic(bundle.getString(EXTRA_DIALOG_RESULT_VALUE));
                break;
            case DIALOG_REQUEST_RESTORE_TAG:
                showProgressDialog();
                mPresenter.restoreTag(bundle.getString(EXTRA_DIALOG_RESULT_VALUE));
                break;
            case DIALOG_REQUEST_RESTORE_SETTINGS:
                showProgressDialog();
                mPresenter.restoreSetting(bundle.getString(EXTRA_DIALOG_RESULT_VALUE));
                break;
        }
    }

    @Override
    public void onComicFileLoadSuccess(String[] file) {
        showChoiceDialog(R.string.backup_restore_comic, file, DIALOG_REQUEST_RESTORE_COMIC);
    }

    @Override
    public void onTagFileLoadSuccess(String[] file) {
        showChoiceDialog(R.string.backup_restore_tag, file, DIALOG_REQUEST_RESTORE_TAG);
    }

    @Override
    public void onSettingsFileLoadSuccess(String[] file) {
        showChoiceDialog(R.string.backup_restore_settings, file, DIALOG_REQUEST_RESTORE_SETTINGS);
    }

    private void showChoiceDialog(int title, String[] item, int request) {
        hideProgressDialog();
        ChoiceDialogFragment fragment = ChoiceDialogFragment.newInstance(title, item, -1, request);
        fragment.show(getSupportFragmentManager(), null);
    }

    @Override
    public void onFileLoadFail() {
        hideProgressDialog();
        showSnackbar(R.string.backup_restore_not_found);
    }

    @Override
    public void onBackupRestoreSuccess() {
        hideProgressDialog();
        showSnackbar(R.string.common_execute_success);
    }

    @Override
    public void onBackupRestoreFail() {
        hideProgressDialog();
        showSnackbar(R.string.common_execute_fail);
    }

    @Override
    public void onBackupSaveSuccess(int size) {
        hideProgressDialog();
        showSnackbar(StringUtils.format(getString(R.string.backup_save_success), size));
    }

    @Override
    public void onBackupSaveFail() {
        hideProgressDialog();
        showSnackbar(R.string.common_execute_fail);
    }

    @Override
    protected String getDefaultTitle() {
        return getString(R.string.drawer_backup);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_backup;
    }

    @Override
    protected View getLayoutView() {
        return mLayoutView;
    }

}
