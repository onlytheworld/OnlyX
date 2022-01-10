package com.OnlyX.presenter;

import com.OnlyX.manager.TagManager;
import com.OnlyX.ui.view.ComicView;

import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by Hiroshi on 2016/10/11.
 */

public class ComicPresenter extends BasePresenter<ComicView> {

    private TagManager mTagManager;

    @Override
    protected void onViewAttach() {
        mTagManager = TagManager.getInstance(mBaseView);
    }

    public void loadTag() {
        mCompositeSubscription.add(mTagManager.listInRx()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> mBaseView.onTagLoadSuccess(list), throwable -> mBaseView.onTagLoadFail()));
    }

}
