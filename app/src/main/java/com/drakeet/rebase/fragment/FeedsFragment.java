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

package com.drakeet.rebase.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import com.drakeet.rebase.Configs;
import com.drakeet.rebase.Stores;
import com.drakeet.rebase.api.Retrofits;
import com.drakeet.rebase.api.type.Feed;
import com.drakeet.rebase.multitype.FeedViewBinder;
import com.drakeet.rebase.tool.Analytics;
import com.drakeet.rebase.tool.guava.Optional;
import com.litesuits.orm.db.assit.QueryBuilder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import java.util.List;
import me.drakeet.multitype.Items;

import static com.drakeet.rebase.Configs.PAGE_SIZE;

/**
 * @author drakeet
 */
public class FeedsFragment extends ListBaseFragment {

    private static String ARG_CATEGORY = "arg_category";

    @Nullable protected String lastId;

    private String category;


    public FeedsFragment() {}


    public static FeedsFragment newInstance(String categoryKey) {
        FeedsFragment fragment = new FeedsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY, categoryKey);
        fragment.setArguments(args);
        return fragment;
    }


    protected void parseArguments(Bundle bundle) {
        Optional<Bundle> optional = Optional.of(bundle);
        category = optional.get().getString(ARG_CATEGORY);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parseArguments(getArguments());
        adapter.register(Feed.class, new FeedViewBinder());
        resetLastId();
    }


    @Override
    protected void loadData(boolean clear) {
        loadDataFromCache();
        loadDataFromRemote(clear);
    }


    void loadDataFromCache() {
        if (items.size() == 0) {
            items.addAll(Stores.db.query(
                QueryBuilder.create(Feed.class)
                    .whereEquals(Feed.CATEGORY, category)
                    .appendOrderDescBy(Feed.PUBLISHED_AT)
                    .distinct(true)
                    .limit(0, 20)
                )
            );
            adapter.notifyDataSetChanged();
        }
    }


    void loadDataFromRemote(final boolean clear) {
        notifyLoadingStarted();
        Retrofits.rebase().feeds(Configs.USERNAME, category, lastId, PAGE_SIZE)
            .compose(this.<List<Feed>>bindToLifecycle())
            .doOnNext(new Consumer<List<Feed>>() {
                @Override
                public void accept(@NonNull List<Feed> feeds) {
                    Stores.db.save(feeds);
                    setEnd(feeds.size() == 0);
                }
            })
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally(new Action() {
                @Override
                public void run() {
                    setRefresh(false);
                    notifyLoadingFinished();
                }
            })
            .subscribe(new Consumer<List<Feed>>() {
                @Override
                public void accept(@NonNull List<Feed> feeds) {
                    Items tempItems = clear ? new Items() : new Items(items);
                    tempItems.addAll(feeds);
                    items = tempItems;
                    adapter.setItems(tempItems);
                    adapter.notifyDataSetChanged();
                }
            });
    }


    @Override
    public void onSwipeRefresh() {
        resetLastId();
        loadDataFromRemote(true);
    }


    @Override
    protected boolean onInterceptLoadMore() {
        if (!isLoading()) {
            lastId = ((Feed) items.get(items.size() - 1))._id;
            loadDataFromRemote(false);
            Analytics.of(getContext()).logEvent("LoadMore");
        }
        return true;
    }


    protected void resetLastId() { lastId = null; }
}
