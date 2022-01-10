package com.OnlyX.presenter;

import com.OnlyX.core.Backup;
import com.OnlyX.core.Download;
import com.OnlyX.core.Manga;
import com.OnlyX.manager.ComicManager;
import com.OnlyX.manager.SourceManager;
import com.OnlyX.manager.TagRefManager;
import com.OnlyX.manager.TaskManager;
import com.OnlyX.model.Chapter;
import com.OnlyX.model.Comic;
import com.OnlyX.model.MiniComic;
import com.OnlyX.model.Task;
import com.OnlyX.rx.RxBus;
import com.OnlyX.rx.RxEvent;
import com.OnlyX.ui.view.DetailView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Hiroshi on 2016/7/4.
 */
public class DetailPresenter extends BasePresenter<DetailView> {

    private ComicManager mComicManager;
    private TaskManager mTaskManager;
    private TagRefManager mTagRefManager;
    private SourceManager mSourceManager;
    private Comic mComic;

    @Override
    protected void onViewAttach() {
        mComicManager = ComicManager.getInstance(mBaseView);
        mTaskManager = TaskManager.getInstance(mBaseView);
        mTagRefManager = TagRefManager.getInstance(mBaseView);
        mSourceManager = SourceManager.getInstance(mBaseView);
    }

    @Override
    protected void initSubscription() {
        addSubscription(RxEvent.EVENT_COMIC_UPDATE, rxEvent -> {
            if (mComic.getId() != null && mComic.getId() == (long) rxEvent.getData()) {
                Comic comic = mComicManager.load(mComic.getId());
                mComic.setPage(comic.getPage());
                mComic.setLast(comic.getLast());
                mComic.setChapter(comic.getChapter());
                mBaseView.onLastChange(mComic.getLast());
            }
        });
    }

    public void load(long id, int source, String cid) {
        if (id == -1) {
            mComic = mComicManager.loadOrCreate(source, cid);
        } else {
            mComic = mComicManager.load(id);
        }
        cancelHighlight();
        load();
    }

    private void updateChapterList(List<Chapter> list) {
        Map<String, Task> map = new HashMap<>();
        for (Task task : mTaskManager.list(mComic.getId())) {
            map.put(task.getPath(), task);
        }
        if (!map.isEmpty()) {
            for (Chapter chapter : list) {
                Task task = map.get(chapter.getPath());
                if (task != null) {
                    chapter.setDownload(true);
                    chapter.setCount(task.getProgress());
                    chapter.setComplete(task.isFinish());
                }
            }
        }
    }

    private void load() {
        mCompositeSubscription.add(Manga.getComicInfo(mSourceManager.getParser(mComic.getSource()), mComic)
                .doOnNext(list -> {
                    if (mComic.getId() != null) {
                        updateChapterList(list);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                    mBaseView.onComicLoadSuccess(mComic);
                    mBaseView.onChapterLoadSuccess(list);
                }, throwable -> {
                    mBaseView.onComicLoadSuccess(mComic);
                    mBaseView.onParseError();
                }));
    }

    private void cancelHighlight() {
        if (mComic.getHighlight()) {
            mComic.setHighlight(false);
            mComic.setFavorite(System.currentTimeMillis());
            mComicManager.update(mComic);
            RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_COMIC_CANCEL_HIGHLIGHT, new MiniComic(mComic)));
        }
    }

    /**
     * 更新最后阅读
     *
     * @param path 最后阅读
     * @return 漫画ID
     */
    public long updateLast(String path) {
        if (mComic.getFavorite() != null) {
            mComic.setFavorite(System.currentTimeMillis());
        }
        mComic.setHistory(System.currentTimeMillis());
        if (!path.equals(mComic.getLast())) {
            mComic.setLast(path);
            mComic.setPage(1);
        }
        mComicManager.updateOrInsert(mComic);
        RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_COMIC_READ, new MiniComic(mComic)));
        return mComic.getId();
    }

    public Comic getComic() {
        return mComic;
    }

    public void backup() {
        mComicManager.listFavoriteOrHistoryInRx()
                .doOnNext(list -> Backup.saveComicAuto(mBaseView.getAppInstance().getContentResolver(),
                        mBaseView.getAppInstance().getDocumentFile(), list))
                .subscribe();
    }

    public void favoriteComic() {
        mComic.setFavorite(System.currentTimeMillis());
        mComicManager.updateOrInsert(mComic);
        RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_COMIC_FAVORITE, new MiniComic(mComic)));
    }

    public void unfavoriteComic() {
        long id = mComic.getId();
        mComic.setFavorite(null);
        mTagRefManager.deleteByComic(id);
        mComicManager.updateOrDelete(mComic);
        RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_COMIC_UNFAVORITE, id));
    }

    private ArrayList<Task> getTaskList(List<Chapter> list) {
        ArrayList<Task> result = new ArrayList<>(list.size());
        for (Chapter chapter : list) {
            Task task = new Task(null, -1, chapter.getPath(), chapter.getTitle(), 0, 0);
            task.setSource(mComic.getSource());
            task.setCid(mComic.getCid());
            task.setState(Task.STATE_WAIT);
            result.add(task);
        }
        return result;
    }

    /**
     * 添加任务到数据库
     *
     * @param cList 所有章节列表，用于写索引文件
     * @param dList 下载章节列表
     */
    public void addTask(final List<Chapter> cList, final List<Chapter> dList) {
        mCompositeSubscription.add(Observable.create((Observable.OnSubscribe<ArrayList<Task>>) subscriber -> {
            final ArrayList<Task> result = getTaskList(dList);
            mComic.setDownload(System.currentTimeMillis());
            mComicManager.runInTx(() -> {
                mComicManager.updateOrInsert(mComic);
                for (Task task : result) {
                    task.setKey(mComic.getId());
                    mTaskManager.insert(task);
                }
            });
            Download.updateComicIndex(mBaseView.getAppInstance().getContentResolver(),
                    mBaseView.getAppInstance().getDocumentFile(), cList, mComic);
            subscriber.onNext(result);
            subscriber.onCompleted();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                    RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_TASK_INSERT, new MiniComic(mComic), list));
                    mBaseView.onTaskAddSuccess(list);
                }, throwable -> mBaseView.onTaskAddFail()));
    }

}
