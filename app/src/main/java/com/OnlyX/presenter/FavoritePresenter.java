package com.OnlyX.presenter;

import com.OnlyX.core.Manga;
import com.OnlyX.manager.ComicManager;
import com.OnlyX.manager.SourceManager;
import com.OnlyX.manager.TagRefManager;
import com.OnlyX.model.Comic;
import com.OnlyX.model.MiniComic;
import com.OnlyX.rx.RxEvent;
import com.OnlyX.rx.ToAnotherList;
import com.OnlyX.ui.view.FavoriteView;

import java.util.List;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by Hiroshi on 2016/7/6.
 */
public class FavoritePresenter extends BasePresenter<FavoriteView> {

    private ComicManager mComicManager;
    private SourceManager mSourceManager;
    private TagRefManager mTagRefManager;

    @Override
    protected void onViewAttach() {
        mComicManager = ComicManager.getInstance(mBaseView);
        mSourceManager = SourceManager.getInstance(mBaseView);
        mTagRefManager = TagRefManager.getInstance(mBaseView);
    }

    @Override
    protected void initSubscription() {
        super.initSubscription();
        addSubscription(RxEvent.EVENT_COMIC_FAVORITE, rxEvent -> {
            MiniComic comic = (MiniComic) rxEvent.getData();
            mBaseView.OnComicFavorite(comic);
        });
        addSubscription(RxEvent.EVENT_COMIC_UNFAVORITE, rxEvent -> mBaseView.OnComicUnFavorite((long) rxEvent.getData()));
        addSubscription(RxEvent.EVENT_COMIC_FAVORITE_RESTORE, rxEvent -> mBaseView.OnComicRestore((List<MiniComic>) rxEvent.getData()));
        addSubscription(RxEvent.EVENT_COMIC_READ, rxEvent -> mBaseView.onComicRead((MiniComic) rxEvent.getData()));
        addSubscription(RxEvent.EVENT_COMIC_CANCEL_HIGHLIGHT, rxEvent -> mBaseView.onHighlightCancel((MiniComic) rxEvent.getData()));
    }

    public Comic load(long id) {
        return mComicManager.load(id);
    }

    public void load() {
        mCompositeSubscription.add(mComicManager.listFavoriteInRx()
                .compose(new ToAnotherList<>(MiniComic::new))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> mBaseView.onComicLoadSuccess(list), throwable -> mBaseView.onComicLoadFail()));
    }

    public void cancelAllHighlight() {
        mComicManager.cancelHighlight();
    }

    public void deleteFavoriteComic(long id) {
        Comic comic = mComicManager.load(id);
        comic.setFavorite(null);
        mTagRefManager.deleteByComic(id);
        mComicManager.updateOrDelete(comic);
        mBaseView.OnComicUnFavorite(id);
    }

    public void checkUpdate() {
        final List<Comic> list = mComicManager.listFavorite();
        mCompositeSubscription.add(Manga.checkUpdate(mSourceManager, list)
                .doOnNext(comic -> {
                    if (comic != null) {
                        mComicManager.update(comic);
                    }
                })
                .onBackpressureBuffer()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Comic>() {
                    private int count = 0;

                    @Override
                    public void onCompleted() {
                        mBaseView.onComicCheckComplete();
                    }

                    @Override
                    public void onError(Throwable e) {
                        mBaseView.onComicCheckFail();
                    }

                    @Override
                    public void onNext(Comic comic) {
                        ++count;
                        MiniComic miniComic = comic == null ? null : new MiniComic(comic);
                        mBaseView.onComicCheckSuccess(miniComic, count, list.size());
                    }
                }));
    }

}
