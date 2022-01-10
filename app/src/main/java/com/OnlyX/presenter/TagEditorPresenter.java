package com.OnlyX.presenter;

import com.OnlyX.manager.TagManager;
import com.OnlyX.manager.TagRefManager;
import com.OnlyX.misc.Switcher;
import com.OnlyX.model.TagRef;
import com.OnlyX.rx.RxBus;
import com.OnlyX.rx.RxEvent;
import com.OnlyX.rx.ToAnotherList;
import com.OnlyX.ui.view.TagEditorView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Hiroshi on 2016/12/2.
 */

public class TagEditorPresenter extends BasePresenter<TagEditorView> {

    private TagManager mTagManager;
    private TagRefManager mTagRefManager;
    private long mComicId;
    private Set<Long> mTagSet;

    @Override
    protected void onViewAttach() {
        mTagManager = TagManager.getInstance(mBaseView);
        mTagRefManager = TagRefManager.getInstance(mBaseView);
        mTagSet = new HashSet<>();
    }

    public void load(long id) {
        mComicId = id;
        mCompositeSubscription.add(mTagManager.listInRx()
                .doOnNext(list -> {
                    for (TagRef ref : mTagRefManager.listByComic(mComicId)) {
                        mTagSet.add(ref.getTid());
                    }
                })
                .compose(new ToAnotherList<>(tag -> new Switcher<>(tag, mTagSet.contains(tag.getId()))))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> mBaseView.onTagLoadSuccess(list), throwable -> mBaseView.onTagLoadFail()));
    }

    private void updateInTx(final List<Long> list) {
        mTagRefManager.runInTx(() -> {
            for (long id : list) {
                if (!mTagSet.contains(id)) {
                    mTagRefManager.insert(new TagRef(null, id, mComicId));
                }
            }
            mTagSet.removeAll(list);
            for (long id : mTagSet) {
                mTagRefManager.delete(id, mComicId);
            }
        });
    }

    public void updateRef(List<Long> list) {
        mCompositeSubscription.add(Observable.just(list)
                .doOnNext(list1 -> {
                    updateInTx(list1);
                    mTagSet.clear();
                    mTagSet.addAll(list1);
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list12 -> {
                    mBaseView.onTagUpdateSuccess();
                    RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_TAG_UPDATE, mComicId, list12));
                }, throwable -> mBaseView.onTagUpdateFail()));
    }

}
