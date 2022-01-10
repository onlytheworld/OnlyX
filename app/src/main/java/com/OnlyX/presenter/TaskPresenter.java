package com.OnlyX.presenter;

import com.OnlyX.core.Download;
import com.OnlyX.manager.ComicManager;
import com.OnlyX.manager.SourceManager;
import com.OnlyX.manager.TaskManager;
import com.OnlyX.model.Chapter;
import com.OnlyX.model.Comic;
import com.OnlyX.model.MiniComic;
import com.OnlyX.model.Task;
import com.OnlyX.rx.RxBus;
import com.OnlyX.rx.RxEvent;
import com.OnlyX.rx.ToAnotherList;
import com.OnlyX.ui.view.TaskView;

import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Hiroshi on 2016/9/7.
 */
public class TaskPresenter extends BasePresenter<TaskView> {

    private TaskManager mTaskManager;
    private ComicManager mComicManager;
    private SourceManager mSourceManager;
    private Comic mComic;

    @Override
    protected void onViewAttach() {
        mTaskManager = TaskManager.getInstance(mBaseView);
        mComicManager = ComicManager.getInstance(mBaseView);
        mSourceManager = SourceManager.getInstance(mBaseView);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void initSubscription() {
        addSubscription(RxEvent.EVENT_TASK_STATE_CHANGE, rxEvent -> {
            long id = (long) rxEvent.getData(1);
            switch ((int) rxEvent.getData()) {
                case Task.STATE_PARSE:
                    mBaseView.onTaskParse(id);
                    break;
                case Task.STATE_ERROR:
                    mBaseView.onTaskError(id);
                    break;
                case Task.STATE_PAUSE:
                    mBaseView.onTaskPause(id);
                    break;
            }
        });
        addSubscription(RxEvent.EVENT_TASK_PROCESS, rxEvent -> {
            long id = (long) rxEvent.getData();
            mBaseView.onTaskProcess(id, (int) rxEvent.getData(1), (int) rxEvent.getData(2));
        });
        addSubscription(RxEvent.EVENT_TASK_INSERT, rxEvent -> {
            List<Task> list = (List<Task>) rxEvent.getData(1);
            Task task = list.get(0);
            if (task.getKey() == mComic.getId()) {
                mBaseView.onTaskAdd(list);
            }
        });
        addSubscription(RxEvent.EVENT_COMIC_UPDATE, rxEvent -> {
            if (mComic.getId() != null && mComic.getId() == (long) rxEvent.getData()) {
                Comic comic = mComicManager.load(mComic.getId());
                mComic.setPage(comic.getPage());
                mComic.setLast(comic.getLast());
                mBaseView.onLastChange(mComic.getLast());
            }
        });
    }

    public Comic getComic() {
        return mComic;
    }

    private void updateTaskList(List<Task> list) {
        for (Task task : list) {
            int state = task.isFinish() ? Task.STATE_FINISH : Task.STATE_PAUSE;
            task.setCid(mComic.getCid());
            task.setSource(mComic.getSource());
            task.setState(state);
        }
    }

    public void load(long id, final boolean asc) {
        mComic = mComicManager.load(id);
        mCompositeSubscription.add(mTaskManager.listInRx(id)
                .doOnNext(list -> {
                    updateTaskList(list);
                    if (!mComic.getLocal()) {
                        final List<String> sList = Download.getComicIndex(mBaseView.getAppInstance().getContentResolver(),
                                mBaseView.getAppInstance().getDocumentFile(), mComic, mSourceManager.getParser(mComic.getSource()).getTitle());
                        if (sList != null) {
                            Collections.sort(list, (lhs, rhs) -> asc ? sList.indexOf(rhs.getPath()) - sList.indexOf(lhs.getPath()) :
                                    sList.indexOf(lhs.getPath()) - sList.indexOf(rhs.getPath()));
                        }
                    } else {
                        Collections.sort(list, (lhs, rhs) -> asc ? lhs.getTitle().compareTo(rhs.getTitle()) :
                                rhs.getTitle().compareTo(lhs.getTitle()));
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> mBaseView.onTaskLoadSuccess(list, mComic.getLocal()), throwable -> mBaseView.onTaskLoadFail()));
    }

    public void deleteTask(List<Chapter> list, final boolean isEmpty) {
        final long id = mComic.getId();
        mCompositeSubscription.add(Observable.just(list)
                .subscribeOn(Schedulers.io())
                .doOnNext(list1 -> {
                    deleteFromDatabase(list1, isEmpty);
                    if (!mComic.getLocal()) {
                        if (isEmpty) {
                            Download.delete(mBaseView.getAppInstance().getDocumentFile(), mComic,
                                    mSourceManager.getParser(mComic.getSource()).getTitle());
                        } else {
                            Download.delete(mBaseView.getAppInstance().getDocumentFile(), mComic,
                                    list1, mSourceManager.getParser(mComic.getSource()).getTitle());
                        }
                    }
                })
                .compose(new ToAnotherList<>(Chapter::getTid))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list12 -> {
                    if (isEmpty) {
                        RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_DOWNLOAD_REMOVE, id));
                    }
                    mBaseView.onTaskDeleteSuccess(list12);
                }, throwable -> mBaseView.onTaskDeleteFail()));
    }

    private void deleteFromDatabase(final List<Chapter> list, final boolean isEmpty) {
        mComicManager.runInTx(() -> {
            for (Chapter chapter : list) {
                mTaskManager.delete(chapter.getTid());
            }
            if (isEmpty) {
                mComic.setDownload(null);
                mComicManager.updateOrDelete(mComic);
                Download.delete(mBaseView.getAppInstance().getDocumentFile(), mComic,
                        mSourceManager.getParser(mComic.getSource()).getTitle());
            }
        });
    }

    public long updateLast(String path) {
        if (mComic.getFavorite() != null) {
            mComic.setFavorite(System.currentTimeMillis());
        }
        mComic.setHistory(System.currentTimeMillis());
        if (!path.equals(mComic.getLast())) {
            mComic.setLast(path);
            mComic.setPage(1);
        }
        mComicManager.update(mComic);
        RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_COMIC_READ, new MiniComic(mComic), false));
        return mComic.getId();
    }

}