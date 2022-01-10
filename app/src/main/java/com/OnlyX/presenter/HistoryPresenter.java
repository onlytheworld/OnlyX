package com.OnlyX.presenter;

import com.OnlyX.manager.ComicManager;
import com.OnlyX.model.Comic;
import com.OnlyX.model.MiniComic;
import com.OnlyX.rx.RxEvent;
import com.OnlyX.rx.ToAnotherList;
import com.OnlyX.ui.view.HistoryView;

import java.util.List;

import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by Hiroshi on 2016/7/18.
 */
public class HistoryPresenter extends BasePresenter<HistoryView> {

    private ComicManager mComicManager;

    @Override
    protected void onViewAttach() {
        mComicManager = ComicManager.getInstance(mBaseView);
    }

    @Override
    protected void initSubscription() {
        super.initSubscription();
        addSubscription(RxEvent.EVENT_COMIC_READ, rxEvent -> mBaseView.onItemUpdate((MiniComic) rxEvent.getData()));
        addSubscription(RxEvent.EVENT_COMIC_HISTORY_RESTORE, rxEvent -> mBaseView.OnComicRestore((List<MiniComic>) rxEvent.getData()));
    }

    public Comic load(long id) {
        return mComicManager.load(id);
    }

    public void load() {
        mCompositeSubscription.add(mComicManager.listHistoryInRx()
                .compose(new ToAnotherList<>(MiniComic::new))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> mBaseView.onComicLoadSuccess(list), throwable -> mBaseView.onComicLoadFail()));
    }

    public void delete(long id) {
        Comic comic = mComicManager.load(id);
        comic.setHistory(null);
        mComicManager.updateOrDelete(comic);
        mBaseView.onHistoryDelete(id);
    }

    public void clear() {
        mCompositeSubscription.add(mComicManager.listHistoryInRx()
                .doOnNext(list -> mComicManager.runInTx(() -> {
                    for (Comic comic : list) {
                        comic.setHistory(null);
                        mComicManager.updateOrDelete(comic);
                    }
                }))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> mBaseView.onHistoryClearSuccess(), throwable -> mBaseView.onExecuteFail()));
    }

}
