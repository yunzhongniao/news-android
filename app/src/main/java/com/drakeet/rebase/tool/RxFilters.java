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

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Predicate;
import java.util.Collection;

/**
 * @author drakeet
 */
public class RxFilters {

    /**
     * Just for the Collections.
     *
     * @param <T> The input.
     * @return true if the input collection is not empty.
     * @throws ClassCastException If the input is not an instance of Collection.
     */
    public static <T> Predicate<T> notEmpty() {
        return new Predicate<T>() {
            @Override
            public boolean test(@NonNull T t) throws Exception {
                return !((Collection) t).isEmpty();
            }
        };
    }


    public static <T> Predicate<T> notNull() {
        return new Predicate<T>() {
            @Override
            public boolean test(@NonNull T t) {
                return t != null;
            }
        };
    }


    public static <T> Predicate<?> event(final Class<T> eventClass) {
        return new Predicate<Object>() {
            @Override
            public boolean test(@NonNull Object o) {
                return o.getClass().equals(eventClass);
            }
        };
    }
}
