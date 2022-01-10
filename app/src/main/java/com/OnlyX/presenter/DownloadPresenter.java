package com.OnlyX.presenter;

import androidx.collection.LongSparseArray;

import com.OnlyX.core.Download;
import com.OnlyX.manager.ComicManager;
import com.OnlyX.manager.SourceManager;
import com.OnlyX.manager.TaskManager;
import com.OnlyX.model.Comic;
import com.OnlyX.model.MiniComic;
import com.OnlyX.model.Task;
import com.OnlyX.rx.RxEvent;
import com.OnlyX.rx.ToAnotherList;
import com.OnlyX.ui.view.DownloadView;
import com.OnlyX.utils.ComicUtils;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by Hiroshi on 2016/9/1.
 */
public class DownloadPresenter extends BasePresenter<DownloadView> {

    private ComicManager mComicManager;
    private TaskManager mTaskManager;
    private SourceManager mSourceManager;

    @Override
    protected void onViewAttach() {
        mComicManager = ComicManager.getInstance(mBaseView);
        mTaskManager = TaskManager.getInstance(mBaseView);
        mSourceManager = SourceManager.getInstance(mBaseView);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void initSubscription() {
        addSubscription(RxEvent.EVENT_TASK_INSERT, rxEvent -> mBaseView.onDownloadAdd((MiniComic) rxEvent.getData()));
        addSubscription(RxEvent.EVENT_DOWNLOAD_REMOVE, rxEvent -> mBaseView.onDownloadDelete((long) rxEvent.getData()));
        addSubscription(RxEvent.EVENT_DOWNLOAD_CLEAR, rxEvent -> {
            for (long id : (List<Long>) rxEvent.getData()) {
                mBaseView.onDownloadDelete(id);
            }
        });
        addSubscription(RxEvent.EVENT_DOWNLOAD_START, rxEvent -> mBaseView.onDownloadStart());
        addSubscription(RxEvent.EVENT_DOWNLOAD_STOP, rxEvent -> mBaseView.onDownloadStop());
    }

    public void deleteComic(long id) {
        mCompositeSubscription.add(Observable.just(id)
                .doOnNext(id1 -> {
                    Comic comic = mComicManager.callInTx(() -> {
                        Comic comic1 = mComicManager.load(id1);
                        mTaskManager.deleteByComicId(id1);
                        comic1.setDownload(null);
                        mComicManager.updateOrDelete(comic1);
                        return comic1;
                    });
                    Download.delete(mBaseView.getAppInstance().getDocumentFile(), comic, mSourceManager.getParser(comic.getSource()).getTitle());
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(id12 -> mBaseView.onDownloadDeleteSuccess(id12), throwable -> mBaseView.onExecuteFail()));
    }

    public Comic load(long id) {
        return mComicManager.load(id);
    }

    public void load() {
        mCompositeSubscription.add(mComicManager.listDownloadInRx()
                .compose(new ToAnotherList<>(MiniComic::new))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> mBaseView.onComicLoadSuccess(list), throwable -> mBaseView.onComicLoadFail()));
    }

    public void loadTask() {
        mCompositeSubscription.add(mTaskManager.listInRx()
                .flatMap((Func1<List<Task>, Observable<Task>>) Observable::from)
                .filter(task -> !task.isFinish())
                .toList()
                .doOnNext(list -> {
                    LongSparseArray<Comic> array = ComicUtils.buildComicMap(mComicManager.listDownload());
                    for (Task task : list) {
                        Comic comic = array.get(task.getKey());
                        if (comic != null) {
                            task.setSource(comic.getSource());
                            task.setCid(comic.getCid());
                        }
                        task.setState(Task.STATE_WAIT);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> mBaseView.onTaskLoadSuccess(new ArrayList<>(list)), throwable -> mBaseView.onExecuteFail()));
    }

}
