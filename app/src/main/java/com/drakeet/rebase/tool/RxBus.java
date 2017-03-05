/*
 * Copyright (C) 2017 Drakeet <drakeet.me@gmail.com>
 *
 * This file is part of rebase-android
 *
 * rebase-android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * rebase-android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with rebase-android. If not, see <http://www.gnu.org/licenses/>.
 */

package com.drakeet.rebase.tool;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

/**
 * @author drakeet
 */
public class RxBus {

    private final PublishSubject<Object> bus = PublishSubject.create();


    public static RxBus singleton() { return LazyLoad.BUS; }


    public boolean hasObservers() {
        return bus.hasObservers();
    }


    public void post(final Object o) {
        bus.onNext(o);
    }


    public Observable<Object> toObservable() {
        return bus;
    }


    private static class LazyLoad {
        static final RxBus BUS = new RxBus();
    }
}
