package com.OnlyX.presenter;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.OnlyX.core.Download;
import com.OnlyX.core.Storage;
import com.OnlyX.manager.ComicManager;
import com.OnlyX.manager.TaskManager;
import com.OnlyX.model.Comic;
import com.OnlyX.model.MiniComic;
import com.OnlyX.model.Task;
import com.OnlyX.rx.RxBus;
import com.OnlyX.rx.RxEvent;
import com.OnlyX.saf.DocumentFile;
import com.OnlyX.ui.view.SettingsView;

import java.util.List;

import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by Hiroshi on 2016/7/22.
 */
public class SettingsPresenter extends BasePresenter<SettingsView> {

    private ComicManager mComicManager;
    private TaskManager mTaskManager;

    @Override
    protected void onViewAttach() {
        mComicManager = ComicManager.getInstance(mBaseView);
        mTaskManager = TaskManager.getInstance(mBaseView);
    }

    public void clearCache() {
        Fresco.getImagePipeline().clearDiskCaches();
    }

    public void moveFiles(DocumentFile dst) {
        mCompositeSubscription.add(Storage.moveRootDir(mBaseView.getAppInstance().getContentResolver(),
                mBaseView.getAppInstance().getDocumentFile(), dst)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(msg -> RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_DIALOG_PROGRESS, msg)), throwable -> mBaseView.onExecuteFail(), () -> mBaseView.onFileMoveSuccess()));
    }

    private void updateKey(long key, List<Task> list) {
        for (Task task : list) {
            task.setKey(key);
        }
    }

    public void scanTask() {
        // Todo 重写一下
        mCompositeSubscription.add(Download.scan(mBaseView.getAppInstance().getContentResolver(), mBaseView.getAppInstance().getDocumentFile())
                .doOnNext(pair -> {
                    Comic comic = mComicManager.load(pair.first.getSource(), pair.first.getCid());
                    if (comic == null) {
                        mComicManager.insert(pair.first);
                        updateKey(pair.first.getId(), pair.second);
                        mTaskManager.insertInTx(pair.second);
                        comic = pair.first;
                    } else {
                        comic.setDownload(System.currentTimeMillis());
                        mComicManager.update(comic);
                        updateKey(comic.getId(), pair.second);
                        mTaskManager.insertIfNotExist(pair.second);
                    }
                    RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_TASK_INSERT, new MiniComic(comic)));
                    RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_DIALOG_PROGRESS, comic.getTitle()));
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pair -> {
                }, throwable -> mBaseView.onExecuteFail(), () -> mBaseView.onExecuteSuccess()));
    }

}
