package com.OnlyX.ui.activity;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.OnlyX.App;
import com.OnlyX.R;
import com.OnlyX.manager.PreferenceManager;
import com.OnlyX.presenter.BasePresenter;
import com.OnlyX.ui.fragment.dialog.ProgressDialogFragment;
import com.OnlyX.ui.view.BaseView;
import com.OnlyX.ui.widget.ViewUtils;
import com.OnlyX.utils.HintUtils;
import com.OnlyX.utils.ThemeUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 所有activity的抽象基类，重新包装一层接口
 * 包装一层 onCreate 函数，后续继承类只需要重定义 initView、getLayoutRes 和 getDefaultTitle
 * 统一初始化了 主题、绑定View、夜间模式、Toolbar、Presenter（不明）和对话框
 * Created by Hiroshi on 2016/7/1.
 *
 * @param <T> the type parameter
 */
public abstract class BaseActivity<T extends BaseView> extends AppCompatActivity implements BaseView {

    protected PreferenceManager mPreference;
    @SuppressLint("NonConstantResourceId")
    @Nullable
    @BindView(R.id.custom_night_mask)
    View mNightMask;
    @SuppressLint("NonConstantResourceId")
    @Nullable
    @BindView(R.id.custom_toolbar)
    Toolbar mToolbar;
    private ProgressDialogFragment mProgressDialog;
    private BasePresenter<T> mBasePresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreference = App.getPreferenceManager();
        initTheme();
        setContentView(getLayoutRes());
        ButterKnife.bind(this);
        initNight();
        initToolbar();
        mBasePresenter = initPresenter();
        mProgressDialog = ProgressDialogFragment.newInstance();
        initView();
    }

    // TODO 检查销毁是否完全
    @Override
    protected void onDestroy() {
        if (mBasePresenter != null) {
            mBasePresenter.detachView();
        }
        super.onDestroy();
    }

    // 接口，使得非 Activity 类可以访问 App
    @Override
    public App getAppInstance() {
        return (App) getApplication();
    }

    // 接口，切换 日/夜间模式
    @Override
    public void onNightSwitch() {
        initNight();
    }

    /**
     * 初始化主题偏好
     */
    protected void initTheme() {
        int theme = mPreference.getInt(PreferenceManager.PREF_OTHER_THEME, ThemeUtils.THEME_BLUE);
        setTheme(ThemeUtils.getThemeById(theme));
        if (isNavTranslation() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    /**
     * 初始化日间/夜间模式，透明度为 B0
     */
    protected void initNight() {
        if (mNightMask != null) {
            boolean night = mPreference.getBoolean(PreferenceManager.PREF_NIGHT, false);
            int color = mPreference.getInt(PreferenceManager.PREF_OTHER_NIGHT_ALPHA, 0xB0) << 24;
            mNightMask.setBackgroundColor(color);
            mNightMask.setVisibility(night ? View.VISIBLE : View.INVISIBLE);
        }
    }

    /**
     * 初始化上方 toolbar
     */
    protected void initToolbar() {
        if (mToolbar != null) {
            mToolbar.setTitle(getDefaultTitle());
            setSupportActionBar(mToolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mToolbar.setPadding(0, ViewUtils.getStatusBarHeight(this), 0, mToolbar.getPaddingBottom());
            }
        }
    }

    /**
     * 初始化函数，用来代替 onCreate
     */
    protected abstract void initView();

    /**
     * 返回当前 View 的 R.id
     *
     * @return R.id. layout res
     */
    protected abstract int getLayoutRes();

    /**
     * 返回当前 Toolbar 的标题
     *
     * @return String 标题
     */
    protected abstract String getDefaultTitle();

    /**
     * Presenter 作用不明，似乎是View的控制？？？
     *
     * @return the base presenter
     */
    protected abstract BasePresenter<T> initPresenter();

    /**
     * Is nav translation boolean.
     *
     * @return the boolean
     */
    protected boolean isNavTranslation() {
        return false;
    }


    /**
     * Gets layout view.
     *
     * @return the layout view
     */
    protected View getLayoutView() {
        return null;
    }

    /**
     * Show snackbar.
     *
     * @param msg the msg
     */
    protected void showSnackbar(String msg) {
        HintUtils.showSnackbar(getLayoutView(), msg);
    }

    /**
     * Show snackbar.
     *
     * @param resId the res id
     */
    protected void showSnackbar(int resId) {
        showSnackbar(getString(resId));
    }

    /**
     * Show progress dialog.
     */
    public void showProgressDialog() {
        mProgressDialog.show(getSupportFragmentManager(), null);
    }

    /**
     * Hide progress dialog.
     */
    public void hideProgressDialog() {
        // 可能 onSaveInstanceState 后任务结束，需要取消对话框，直接 dismiss 会抛异常
        mProgressDialog.dismissAllowingStateLoss();
    }

}
