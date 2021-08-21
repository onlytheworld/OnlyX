package com.OnlyX.presenter;

import com.OnlyX.core.Update;
import com.OnlyX.manager.ComicManager;
import com.OnlyX.model.Comic;
import com.OnlyX.model.MiniComic;
import com.OnlyX.rx.RxEvent;
import com.OnlyX.ui.view.MainView;

import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by Hiroshi on 2016/9/21.
 */

public class MainPresenter extends BasePresenter<MainView> {

    private ComicManager mComicManager;

    @Override
    protected void onViewAttach() {
        mComicManager = ComicManager.getInstance(mBaseView);
    }

    @Override
    protected void initSubscription() {
        addSubscription(RxEvent.EVENT_COMIC_READ, rxEvent -> {
            MiniComic comic = (MiniComic) rxEvent.getData();
            mBaseView.onLastChange(comic.getId(), comic.getSource(), comic.getCid(),
                    comic.getTitle(), comic.getCover());
        });
    }

    public boolean checkLocal(long id) {
        Comic comic = mComicManager.load(id);
        return comic != null && comic.getLocal();
    }

    public void loadLast() {
        mCompositeSubscription.add(mComicManager.loadLast()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(comic -> {
                    if (comic != null) {
                        mBaseView.onLastLoadSuccess(comic.getId(), comic.getSource(), comic.getCid(), comic.getTitle(), comic.getCover());
                    }
                }, throwable -> mBaseView.onLastLoadFail()));
    }

    public void checkUpdate(final String version) {
        mCompositeSubscription.add(Update.check()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    if (!version.contains(s) && !version.contains("t")) {
                        mBaseView.onUpdateReady();
                    }
                }, throwable -> {
                }));
    }

}
