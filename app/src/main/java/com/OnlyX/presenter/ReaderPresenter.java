package com.OnlyX.presenter;

import android.net.Uri;
import android.os.Build;

import com.OnlyX.core.Download;
import com.OnlyX.core.Local;
import com.OnlyX.core.Manga;
import com.OnlyX.core.Storage;
import com.OnlyX.manager.ComicManager;
import com.OnlyX.manager.SourceManager;
import com.OnlyX.model.Chapter;
import com.OnlyX.model.Comic;
import com.OnlyX.model.ImageUrl;
import com.OnlyX.rx.RxBus;
import com.OnlyX.rx.RxEvent;
import com.OnlyX.saf.DocumentFile;
import com.OnlyX.ui.view.ReaderView;
import com.OnlyX.utils.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by Hiroshi on 2016/7/8.
 */
public class ReaderPresenter extends BasePresenter<ReaderView> {

    private final static int LOAD_NULL = 0;
    private final static int LOAD_INIT = 1;
    private final static int LOAD_PREV = 2;
    private final static int LOAD_NEXT = 3;

    private ChapterManger mChapterManger;
    private ComicManager mComicManager;
    private SourceManager mSourceManager;
    private Comic mComic;
    public int mSource;

    private boolean isShowNext = true;
    private boolean isShowPrev = true;
    private int count = 0;
    private int status = LOAD_INIT;

    @Override
    protected void onViewAttach() {
        mComicManager = ComicManager.getInstance(mBaseView);
        mSourceManager = SourceManager.getInstance(mBaseView);
    }

    @Override
    protected void initSubscription() {
        addSubscription(RxEvent.EVENT_PICTURE_PAGING, rxEvent -> mBaseView.onPicturePaging((ImageUrl) rxEvent.getData()));
    }

    public void lazyLoad(final ImageUrl imageUrl) {
        mCompositeSubscription.add(Manga.loadLazyUrl(mSourceManager.getParser(mComic.getSource()), imageUrl.getUrl())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(url -> {
                    if (url == null) {
                        mBaseView.onImageLoadFail(imageUrl.getId());
                    } else {
                        mBaseView.onImageLoadSuccess(imageUrl.getId(), url);
                    }
                }, throwable -> mBaseView.onImageLoadFail(imageUrl.getId())));
    }

    public void loadInit(long id, Chapter[] array) {
        mComic = mComicManager.load(id);
        mSource = mComic.getSource();
        for (int i = 0; i != array.length; ++i) {
            if (array[i].getPath().equals(mComic.getLast())) {
                this.mChapterManger = new ChapterManger(array, i);
                images(getObservable(array[i]));
            }
        }
    }

    public void loadNext() {
        if (status == LOAD_NULL && isShowNext) {
            Chapter chapter = mChapterManger.getNextChapter();
            if (chapter != null) {
                status = LOAD_NEXT;
                images(getObservable(chapter));
                mBaseView.onNextLoading();
            } else {
                isShowNext = false;
                mBaseView.onNextLoadNone();
            }
        }
    }

    public void loadPrev() {
        if (status == LOAD_NULL && isShowPrev) {
            Chapter chapter = mChapterManger.getPrevChapter();
            if (chapter != null) {
                status = LOAD_PREV;
                images(getObservable(chapter));
                mBaseView.onPrevLoading();
            } else {
                isShowPrev = false;
                mBaseView.onPrevLoadNone();
            }
        }
    }

    private Observable<List<ImageUrl>> getObservable(Chapter chapter) {
        if (mComic.getLocal()) {
            DocumentFile dir = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ?
                    DocumentFile.fromSubTreeUri(mBaseView.getAppInstance(), Uri.parse(chapter.getPath())) :
                    DocumentFile.fromFile(new File(Uri.parse(chapter.getPath()).getPath()));
            return Local.images(dir, chapter);
        }
        return chapter.isComplete() ? Download.images(mBaseView.getAppInstance().getDocumentFile(),
                mComic, chapter, mSourceManager.getParser(mComic.getSource()).getTitle()) :
                Manga.getChapterImage(mSourceManager.getParser(mComic.getSource()), mComic.getCid(), chapter.getPath());
    }

    public void toNextChapter() {
        Chapter chapter = mChapterManger.nextChapter();
        if (chapter != null) {
            updateChapter(chapter, true);
        }
    }

    public void toPrevChapter() {
        Chapter chapter = mChapterManger.prevChapter();
        if (chapter != null) {
            updateChapter(chapter, false);
        }
    }

    private void updateChapter(Chapter chapter, boolean isNext) {
        mBaseView.onChapterChange(chapter);
        mComic.setLast(chapter.getPath());
        mComic.setChapter(chapter.getTitle());
        mComic.setPage(isNext ? 1 : chapter.getCount());
        mComicManager.update(mComic);
        RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_COMIC_UPDATE, mComic.getId()));
    }

    public void savePicture(InputStream inputStream, String url, String title, int page) {
        mCompositeSubscription.add(Storage.savePicture(mBaseView.getAppInstance().getContentResolver(),
                mBaseView.getAppInstance().getDocumentFile(), inputStream, buildPictureName(title, page, url))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(uri -> mBaseView.onPictureSaveSuccess(uri), throwable -> {
                    throwable.printStackTrace();
                    mBaseView.onPictureSaveFail();
                }));
    }

    private String buildPictureName(String title, int page, String url) {
        String suffix = StringUtils.split(url, "\\.", -1);
        suffix = suffix == null ? "jpg" : suffix.split("\\?")[0];
        return StringUtils.format("%s_%s_%03d.%s", StringUtils.filter(mComic.getTitle()), StringUtils.filter(title), page, suffix);
    }

    public void updateComic(int page) {
        if (status != LOAD_INIT) {
            mComic.setPage(page);
            mComicManager.update(mComic);
            RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_COMIC_UPDATE, mComic.getId()));
        }
    }

    public void switchNight() {
        RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_SWITCH_NIGHT));
    }

    private void images(Observable<List<ImageUrl>> observable) {
        mCompositeSubscription.add(observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                    Chapter chapter;
                    switch (status) {
                        case LOAD_INIT:
                            chapter = mChapterManger.moveNext();
                            chapter.setCount(list.size());
                            if (!chapter.getTitle().equals(mComic.getTitle())) {
                                mComic.setChapter(chapter.getTitle());
                                mComicManager.update(mComic);
                            }
                            mBaseView.onChapterChange(chapter);
                            mBaseView.onInitLoadSuccess(list, mComic.getPage(), mComic.getSource(), mComic.getLocal());
                            break;
                        case LOAD_PREV:
                            chapter = mChapterManger.movePrev();
                            chapter.setCount(list.size());
                            mBaseView.onPrevLoadSuccess(list);
                            break;
                        case LOAD_NEXT:
                            chapter = mChapterManger.moveNext();
                            chapter.setCount(list.size());
                            mBaseView.onNextLoadSuccess(list);
                            break;
                    }
                    status = LOAD_NULL;
                }, throwable -> {
                    mBaseView.onParseError();
                    if (status != LOAD_INIT && ++count < 2) {
                        status = LOAD_NULL;
                    }
                }));
    }

    private static class ChapterManger {

        private final Chapter[] array;
        private int index;
        private int prev;
        private int next;

        ChapterManger(Chapter[] array, int index) {
            this.array = array;
            this.index = index;
            prev = index + 1;
            next = index;
        }

        Chapter getPrevChapter() {
            return prev < array.length ? array[prev] : null;
        }

        Chapter getNextChapter() {
            return next >= 0 ? array[next] : null;
        }

        Chapter prevChapter() {
            if (index + 1 < prev) {
                return array[++index];
            }
            return null;
        }

        Chapter nextChapter() {
            if (index - 1 > next) {
                return array[--index];
            }
            return null;
        }

        Chapter movePrev() {
            return array[prev++];
        }

        Chapter moveNext() {
            return array[next--];
        }

    }

}
