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

package com.drakeet.rebase;

import android.app.Application;
import android.content.Context;
import bz.tsung.android.objectify.ObjectPreferenceLoader;
import bz.tsung.android.objectify.StringPreferenceLoader;
import com.drakeet.rebase.api.type.Login;
import com.litesuits.orm.LiteOrm;

/**
 * @author drakeet
 */
public class Stores {

    private static final String DB_NAME = "rebase.db";
    public static LiteOrm db;


    static void install(Application application) {
        db = LiteOrm.newCascadeInstance(application, DB_NAME);
        db.setDebugged(BuildConfig.DEBUG);
    }


    public static StringPreferenceLoader stringPreference(Context context, String key) {
        return new StringPreferenceLoader(context, key);
    }


    public static ObjectPreferenceLoader login(Context context) {
        return new ObjectPreferenceLoader(context, "encrypted_login", Login.class);
    }


    public static StringPreferenceLoader token(Context context) {
        return stringPreference(context, "encrypted_token");
    }
}
