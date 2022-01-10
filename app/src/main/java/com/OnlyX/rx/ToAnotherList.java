package com.OnlyX.rx;

import java.util.List;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Hiroshi on 2016/10/23.
 */

public class ToAnotherList<T, R> implements Observable.Transformer<List<T>, List<R>> {

    private final Func1<T, R> func;

    public ToAnotherList(Func1<T, R> func) {
        this.func = func;
    }

    @Override
    public Observable<List<R>> call(Observable<List<T>> observable) {
        return observable.flatMap((Func1<List<T>, Observable<T>>) Observable::from).map(func).toList();
    }

}
