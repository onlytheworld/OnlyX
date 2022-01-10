package com.OnlyX.presenter;

import com.OnlyX.rx.RxBus;
import com.OnlyX.rx.RxEvent;
import com.OnlyX.ui.view.BaseView;

import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by Hiroshi on 2016/7/4.
 */
public abstract class BasePresenter<T extends BaseView> {

    protected T mBaseView;
    protected CompositeSubscription mCompositeSubscription;

    public void attachView(T view) {
        this.mBaseView = view;
        onViewAttach();
        mCompositeSubscription = new CompositeSubscription();
        addSubscription(RxEvent.EVENT_SWITCH_NIGHT, rxEvent -> mBaseView.onNightSwitch());
        initSubscription();
    }

    protected void onViewAttach() {
    }

    protected void initSubscription() {
    }

    protected void addSubscription(@RxEvent.EventType int type, Action1<RxEvent> action) {
        mCompositeSubscription.add(RxBus.getInstance().toObservable(type).subscribe(action, Throwable::printStackTrace));
    }

    public void detachView() {
        if (mCompositeSubscription != null) {
            mCompositeSubscription.unsubscribe();
        }
        mBaseView = null;
    }

}
