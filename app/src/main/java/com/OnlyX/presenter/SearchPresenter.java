package com.OnlyX.presenter;

import com.OnlyX.core.Manga;
import com.OnlyX.manager.SourceManager;
import com.OnlyX.model.Source;
import com.OnlyX.ui.view.SearchView;

import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by Hiroshi on 2016/10/11.
 */

public class SearchPresenter extends BasePresenter<SearchView> {

    private SourceManager mSourceManager;

    @Override
    protected void onViewAttach() {
        mSourceManager = SourceManager.getInstance(mBaseView);
    }

    public void loadSource() {
        mCompositeSubscription.add(mSourceManager.listEnableInRx()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> mBaseView.onSourceLoadSuccess(list), throwable -> mBaseView.onSourceLoadFail()));
    }

    public Source loadSource(int source) {
        return mSourceManager.load(source);
    }

    public void loadAutoComplete(String keyword) {
        mCompositeSubscription.add(Manga.loadAutoComplete(keyword)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> mBaseView.onAutoCompleteLoadSuccess(list), Throwable::printStackTrace));
    }

}
