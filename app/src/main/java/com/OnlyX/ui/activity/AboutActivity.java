package com.OnlyX.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.OnlyX.App;
import com.OnlyX.Constants;
import com.OnlyX.R;
import com.OnlyX.manager.PreferenceManager;
import com.OnlyX.presenter.AboutPresenter;
import com.OnlyX.presenter.BasePresenter;
import com.OnlyX.ui.view.AboutView;
import com.OnlyX.utils.StringUtils;


import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/9/21.
 */

public class AboutActivity extends BackActivity implements AboutView, AdapterView.OnItemSelectedListener {

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.about_update_summary)
    TextView mUpdateText;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.about_version_name)
    TextView mVersionName;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.about_layout)
    View mLayoutView;

    protected void initData() {
    }

    @Override
    protected BasePresenter<AboutView> initPresenter() {
        AboutPresenter mPresenter = new AboutPresenter();
        mPresenter.attachView(this);
        return mPresenter;
    }

    // TODO 更新源独立出一个模块？
    @Override
    protected void initView() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            mVersionName.setText(StringUtils.format("version: %s (%s)", info.versionName, info.versionCode));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.home_page_btn)
    void onHomeClick() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.home_page_url)));
        try {
            startActivity(intent);
        } catch (Exception e) {
            showSnackbar(R.string.about_resource_fail);
        }
    }

//    @OnClick(R.id.home_page_cimqus_btn)
//    void onCimqusClick() {
//        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.home_page_cimqus_url)));
//        try {
//            startActivity(intent);
//        } catch (Exception e) {
//            showSnackbar(R.string.about_resource_fail);
//        }
//    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.about_support_btn)
    void onSupportClick() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.about_support_url)));
        try {
            startActivity(intent);
        } catch (Exception e) {
            showSnackbar(R.string.about_resource_fail);
        }
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.about_resource_btn)
    void onResourceClick() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.about_resource_url)));
        try {
            startActivity(intent);
        } catch (Exception e) {
            showSnackbar(R.string.about_resource_fail);
        }
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.about_resource_ori_btn)
    void onOriResourceClick() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.about_resource_ori_url)));
        try {
            startActivity(intent);
        } catch (Exception e) {
            showSnackbar(R.string.about_resource_fail);
        }
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.about_update_btn)
    void onUpdateClick() {
//        if (update) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.about_update_url)));
        try {
            startActivity(intent);
        } catch (Exception e) {
            showSnackbar(R.string.about_resource_fail);
        }
//        } else if (!checking) {
//            try {
//                PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
//                mUpdateText.setText(R.string.about_update_doing);
//                checking = true;
//                mPresenter.checkUpdate(info.versionName);
//            } catch (Exception e){
//                mUpdateText.setText(R.string.about_update_fail);
//                checking = false;
//            }
//        }
    }

    @Override
    protected String getDefaultTitle() {
        return getString(R.string.drawer_about);
    }

    @Override
    protected View getLayoutView() {
        return mLayoutView;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_about;
    }

    private void update() {
//        if (Update.update(this)) {
        mUpdateText.setText(R.string.about_update_summary);
//        } else {
//            showSnackbar(R.string.about_resource_fail);
//        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                showSnackbar("请选择一个下载源");
//                checkSpinnerSelected(spinner_download_source);
                break;
            case 1:
                App.setUpdateCurrentUrl(Constants.UPDATE_GITHUB_URL);
                App.getPreferenceManager().putString(PreferenceManager.PREF_UPDATE_CURRENT_URL, App.getUpdateCurrentUrl());
                break;
            case 2:
                App.setUpdateCurrentUrl(Constants.UPDATE_GITEE_URL);
                App.getPreferenceManager().putString(PreferenceManager.PREF_UPDATE_CURRENT_URL, App.getUpdateCurrentUrl());
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

}
