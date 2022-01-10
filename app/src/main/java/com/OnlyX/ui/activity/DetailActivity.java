package com.OnlyX.ui.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.OnlyX.App;
import com.OnlyX.R;
import com.OnlyX.global.Extra;
import com.OnlyX.manager.PreferenceManager;
import com.OnlyX.manager.SourceManager;
import com.OnlyX.model.Chapter;
import com.OnlyX.model.Comic;
import com.OnlyX.model.Task;
import com.OnlyX.presenter.DetailPresenter;
import com.OnlyX.service.DownloadService;
import com.OnlyX.ui.adapter.DetailAdapter;
import com.OnlyX.ui.view.DetailView;
import com.OnlyX.utils.StringUtils;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/7/2.
 */
public class DetailActivity extends CoordinatorActivity implements DetailView {

    public static final int REQUEST_CODE_DOWNLOAD = 0;

    private DetailAdapter mDetailAdapter;
    private DetailPresenter mPresenter;
//    private ImagePipelineFactory mImagePipelineFactory;

    private boolean mAutoBackup;
    private int mBackupCount;

    public static Intent createIntent(Context context, Long id, int source, String cid) {
        Intent intent = new Intent(context, DetailActivity.class);
        intent.putExtra(Extra.EXTRA_ID, id);
        intent.putExtra(Extra.EXTRA_SOURCE, source);
        intent.putExtra(Extra.EXTRA_CID, cid);
        return intent;
    }

    @Override
    protected DetailPresenter initPresenter() {
        mPresenter = new DetailPresenter();
        mPresenter.attachView(this);
        return mPresenter;
    }

    @Override
    protected DetailAdapter initAdapter() {
        mDetailAdapter = new DetailAdapter(this, new ArrayList<>());
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        return mDetailAdapter;
    }

    @Override
    protected RecyclerView.LayoutManager initLayoutManager() {
        return new GridLayoutManager(this, 3);
    }

    @Override
    protected void initView() {
        super.initView();
        mAutoBackup = mPreference.getBoolean(PreferenceManager.PREF_BACKUP_SAVE_COMIC, true);
        mBackupCount = mPreference.getInt(PreferenceManager.PREF_BACKUP_SAVE_COMIC_COUNT, 0);
        long id = getIntent().getLongExtra(Extra.EXTRA_ID, -1);
        int source = getIntent().getIntExtra(Extra.EXTRA_SOURCE, -1);
        String cid = getIntent().getStringExtra(Extra.EXTRA_CID);
        mPresenter.load(id, source, cid);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAutoBackup) {
            mPreference.putInt(PreferenceManager.PREF_BACKUP_SAVE_COMIC_COUNT, mBackupCount);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if (mImagePipelineFactory != null) {
//            mImagePipelineFactory.getImagePipeline().clearMemoryCaches();
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        if (!isProgressBarShown()) {
            switch (item.getItemId()) {
//                case R.id.detail_history:
//                    if (!mDetailAdapter.getDateSet().isEmpty()) {
//                        String path = mPresenter.getComic().getLast();
//                        if (path == null) {
//                            path = mDetailAdapter.getItem(mDetailAdapter.getDateSet().size() - 1).getPath();
//                        }
//                        startReader(path);
//                    }
//                    break;
                case R.id.detail_download:
                    if (!mDetailAdapter.getDateSet().isEmpty()) {
                        intent = ChapterActivity.createIntent(this, new ArrayList<>(mDetailAdapter.getDateSet()));
                        startActivityForResult(intent, REQUEST_CODE_DOWNLOAD);
                    }
                    break;
                case R.id.detail_tag:
                    if (mPresenter.getComic().getFavorite() != null) {
                        intent = TagEditorActivity.createIntent(this, mPresenter.getComic().getId());
                        startActivity(intent);
                    } else {
                        showSnackbar(R.string.detail_tag_favorite);
                    }
                    break;
                case R.id.detail_search_title:
                    if (!StringUtils.isEmpty(mPresenter.getComic().getTitle())) {
                        if (App.getPreferenceManager().getBoolean(PreferenceManager.PREF_OTHER_FIREBASE_EVENT, true)) {
                            Bundle bundle = new Bundle();
                            bundle.putString(FirebaseAnalytics.Param.CONTENT, mPresenter.getComic().getTitle());
                            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "byTitle");
                            bundle.putInt(FirebaseAnalytics.Param.SOURCE, mPresenter.getComic().getSource());
                            FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
                            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SEARCH, bundle);
                        }
                        intent = ResultActivity.createIntent(this, mPresenter.getComic().getTitle(), null, ResultActivity.LAUNCH_MODE_SEARCH);
                        startActivity(intent);
                    } else {
                        showSnackbar(R.string.common_keyword_empty);
                    }
                    break;
                case R.id.detail_search_author:
                    if (!StringUtils.isEmpty(mPresenter.getComic().getAuthor())) {
                        intent = ResultActivity.createIntent(this, mPresenter.getComic().getAuthor(), null, ResultActivity.LAUNCH_MODE_SEARCH);
                        startActivity(intent);
                    } else {
                        showSnackbar(R.string.common_keyword_empty);
                    }
                    break;
                case R.id.detail_share_url:
                    String url = mPresenter.getComic().getUrl();
                    intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, url);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(Intent.createChooser(intent, url));

                    // firebase analytics
                    if (App.getPreferenceManager().getBoolean(PreferenceManager.PREF_OTHER_FIREBASE_EVENT, true)) {
                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.CONTENT, url);
                        bundle.putInt(FirebaseAnalytics.Param.SOURCE, mPresenter.getComic().getSource());
                        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE, bundle);
                    }
                    break;
                case R.id.detail_reverse_list:
                    mDetailAdapter.reverse();
                    break;
//                case R.id.detail_disqus:
//                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.home_page_cimqus_url) + "/onlyx/" + mPresenter.getComic().getTitle()));
//                    try {
//                        startActivity(intent);
//                    } catch (Exception e) {
//                        showSnackbar(R.string.about_resource_fail);
//                    }
//                    break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_DOWNLOAD) {
                showProgressDialog();
                List<Chapter> list = data.getParcelableArrayListExtra(Extra.EXTRA_CHAPTER);
                mPresenter.addTask(mDetailAdapter.getDateSet(), list);
            }
        }
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.coordinator_action_button)
    void onActionButtonClick() {
        //todo: add comic to mangodb
        if (mPresenter.getComic().getFavorite() != null) {
            mPresenter.unfavoriteComic();
            increment();
            mActionButton.setImageResource(R.drawable.ic_favorite_border_white_24dp);
            showSnackbar(R.string.detail_unfavorite);
        } else {
            mPresenter.favoriteComic();
            increment();
            mActionButton.setImageResource(R.drawable.ic_favorite_white_24dp);
            showSnackbar(R.string.detail_favorite);
        }
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.coordinator_action_button2)
    void onActionButton2Click() {
        if (!mDetailAdapter.getDateSet().isEmpty()) {
            String path = mPresenter.getComic().getLast();
            if (path == null) {
                path = mDetailAdapter.getItem(mDetailAdapter.getDateSet().size() - 1).getPath();
            }
            startReader(path);
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        if (position != 0) {
            String path = mDetailAdapter.getItem(position - 1).getPath();
            startReader(path);
        }
    }

    @Override
    public boolean onItemLongClick(View view, int position) {
        if (position == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(mDetailAdapter.title)
                    .setMessage(mDetailAdapter.intro)
                    .setPositiveButton(R.string.dialog_close, null)
                    .show();
        }
        return false;
    }


    private void startReader(String path) {
        long id = mPresenter.updateLast(path);
        mDetailAdapter.setLast(path);
        int mode = mPreference.getInt(PreferenceManager.PREF_READER_MODE, PreferenceManager.READER_MODE_PAGE);
        Intent intent = ReaderActivity.createIntent(DetailActivity.this, id, mDetailAdapter.getDateSet(), mode);
        startActivity(intent);
    }

    @Override
    public void onLastChange(String last) {
        mDetailAdapter.setLast(last);
    }


    @Override
    public void onTaskAddSuccess(ArrayList<Task> list) {
        Intent intent = DownloadService.createIntent(this, list);
        startService(intent);
        updateChapterList(list);
        showSnackbar(R.string.detail_download_queue_success);
        hideProgressDialog();
    }

    private void updateChapterList(List<Task> list) {
        Set<String> set = new HashSet<>();
        for (Task task : list) {
            set.add(task.getPath());
        }
        for (Chapter chapter : mDetailAdapter.getDateSet()) {
            if (set.contains(chapter.getPath())) {
                chapter.setDownload(true);
            }
        }
    }

    @Override
    public void onTaskAddFail() {
        hideProgressDialog();
        showSnackbar(R.string.detail_download_queue_fail);
    }

    @Override
    public void onComicLoadSuccess(Comic comic) {
        mDetailAdapter.setInfo(comic.getCover(), comic.getTitle(), comic.getAuthor(),
                comic.getIntro(), comic.getFinish(), comic.getUpdate(), comic.getLast(), comic.getSource());
        if (comic.getTitle() != null && comic.getCover() != null) {
//            mImagePipelineFactory = ImagePipelineFactoryBuilder.build(this, Headers.of(SourceManager.getInstance(this).getParser(comic.getSource()).getHeader()), false);
//            mDetailAdapter.setControllerSupplier(ControllerBuilderSupplierFactory.get(this, mImagePipelineFactory));

            mDetailAdapter.setSMGetter(SourceManager.getInstance(this).new SMGetter());
            int resId = comic.getFavorite() != null ? R.drawable.ic_favorite_white_24dp : R.drawable.ic_favorite_border_white_24dp;
            mActionButton.setImageResource(resId);
            mActionButton.setVisibility(View.VISIBLE);
            mActionButton2.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onChapterLoadSuccess(List<Chapter> list) {
        hideProgressBar();
        if (App.getPreferenceManager().getBoolean(PreferenceManager.PREF_OTHER_FIREBASE_EVENT, true)) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.CONTENT, mPresenter.getComic().getTitle());
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Title");
            bundle.putInt(FirebaseAnalytics.Param.SOURCE, mPresenter.getComic().getSource());
            bundle.putBoolean(FirebaseAnalytics.Param.SUCCESS, true);
            FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle);
        }
        if (mPresenter.getComic().getTitle() != null && mPresenter.getComic().getCover() != null) {
            mDetailAdapter.addAll(list);
        }
    }

    @Override
    public void onParseError() {
        if (App.getPreferenceManager().getBoolean(PreferenceManager.PREF_OTHER_FIREBASE_EVENT, true)) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.CONTENT, mPresenter.getComic().getTitle());
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Title");
            bundle.putInt(FirebaseAnalytics.Param.SOURCE, mPresenter.getComic().getSource());
            bundle.putBoolean(FirebaseAnalytics.Param.SUCCESS, false);
            FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle);
        }
        hideProgressBar();
        showSnackbar(R.string.common_parse_error);
    }

    private void increment() {
        if (mAutoBackup && ++mBackupCount == 10) {
            mBackupCount = 0;
            mPreference.putInt(PreferenceManager.PREF_BACKUP_SAVE_COMIC_COUNT, 0);
            mPresenter.backup();
        }
    }

    @Override
    protected String getDefaultTitle() {
        return getString(R.string.detail);
    }

}
