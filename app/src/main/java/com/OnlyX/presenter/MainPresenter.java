package com.OnlyX.presenter;

import com.OnlyX.core.Update;
import com.OnlyX.manager.ComicManager;
import com.OnlyX.model.Comic;
import com.OnlyX.model.MiniComic;
import com.OnlyX.rx.RxEvent;
import com.OnlyX.ui.view.MainView;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

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
        addSubscription(RxEvent.EVENT_COMIC_READ, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                MiniComic comic = (MiniComic) rxEvent.getData();
                mBaseView.onLastChange(comic.getId(), comic.getSource(), comic.getCid(),
                        comic.getTitle(), comic.getCover());
            }
        });
    }

    public boolean checkLocal(long id) {
        Comic comic = mComicManager.load(id);
        return comic != null && comic.getLocal();
    }

    public void loadLast() {
        mCompositeSubscription.add(mComicManager.loadLast()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Comic>() {
                    @Override
                    public void call(Comic comic) {
                        if (comic != null) {
                            mBaseView.onLastLoadSuccess(comic.getId(), comic.getSource(), comic.getCid(), comic.getTitle(), comic.getCover());
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onLastLoadFail();
                    }
                }));
    }

    public void checkUpdate(final String version) {
        mCompositeSubscription.add(Update.check()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        if (-1 == version.indexOf(s) && -1 == version.indexOf("t")) {
                            mBaseView.onUpdateReady();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                    }
                }));
    }

}
