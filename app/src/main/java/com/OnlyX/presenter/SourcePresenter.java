package com.OnlyX.presenter;

import com.OnlyX.manager.SourceManager;
import com.OnlyX.model.Source;
import com.OnlyX.ui.view.SourceView;

import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by Hiroshi on 2016/8/11.
 */
public class SourcePresenter extends BasePresenter<SourceView> {

    private SourceManager mSourceManager;

    @Override
    protected void onViewAttach() {
        mSourceManager = SourceManager.getInstance(mBaseView);
    }

    public void load() {
        mCompositeSubscription.add(mSourceManager.list()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> mBaseView.onSourceLoadSuccess(list), throwable -> mBaseView.onSourceLoadFail()));
    }

    public void update(Source source) {
        mSourceManager.update(source);
    }

}
