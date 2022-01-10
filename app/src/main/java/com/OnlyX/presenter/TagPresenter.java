package com.OnlyX.presenter;

import com.OnlyX.manager.TagManager;
import com.OnlyX.manager.TagRefManager;
import com.OnlyX.model.Tag;
import com.OnlyX.rx.RxEvent;
import com.OnlyX.ui.view.TagView;

import java.util.List;

import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by Hiroshi on 2016/10/10.
 */

public class TagPresenter extends BasePresenter<TagView> {

    private TagManager mTagManager;
    private TagRefManager mTagRefManager;

    @Override
    protected void onViewAttach() {
        mTagManager = TagManager.getInstance(mBaseView);
        mTagRefManager = TagRefManager.getInstance(mBaseView);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void initSubscription() {
        addSubscription(RxEvent.EVENT_TAG_RESTORE, rxEvent -> mBaseView.onTagRestore((List<Tag>) rxEvent.getData()));
    }

    public void load() {
        mCompositeSubscription.add(mTagManager.listInRx()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> mBaseView.onTagLoadSuccess(list), throwable -> mBaseView.onTagLoadFail()));
    }

    public void insert(Tag tag) {
        mTagManager.insert(tag);
    }

    public void delete(final Tag tag) {
        mCompositeSubscription.add(mTagRefManager.runInRx(() -> {
            mTagRefManager.deleteByTag(tag.getId());
            mTagManager.delete(tag);
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(aVoid -> mBaseView.onTagDeleteSuccess(tag), throwable -> mBaseView.onTagDeleteFail()));
    }

}
