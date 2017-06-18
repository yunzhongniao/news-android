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

package com.drakeet.rebase.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.drakeet.rebase.Configs;
import com.drakeet.rebase.R;
import com.drakeet.rebase.Stores;
import com.drakeet.rebase.api.Retrofits;
import com.drakeet.rebase.api.type.Category;
import com.drakeet.rebase.fragment.FeedsFragment;
import com.drakeet.rebase.fragment.LoginFragment;
import com.drakeet.rebase.tool.AbstractPageChangeListener;
import com.drakeet.rebase.tool.AbstractTabSelectedListener;
import com.drakeet.rebase.tool.Analytics;
import com.drakeet.rebase.tool.Colorful;
import com.litesuits.orm.db.assit.QueryBuilder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import java.util.List;

/**
 * Note: By default, we don't log the default selected fragment.
 *
 * @author drakeet
 */
public class MainActivity extends ToolbarActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.tab_layout) TabLayout tabLayout;
    @BindView(R.id.pager) ViewPager viewPager;

    MainPagerAdapter pagerAdapter;
    List<Category> categories;

    @Nullable AbstractTabSelectedListener onTabSelectedListener;
    @Nullable AbstractPageChangeListener onPageChangeListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Colorful.init(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setDoubleClickToExitEnabled(false);
        setDisplayShowTitleEnabled(false);
        loadCategories();
    }


    private void loadCategories() {
        List<Category> categoryCaches = Stores.db.single()
            .query(QueryBuilder.create(Category.class).orderBy("rank"));
        final boolean existCaches = (categoryCaches.size() > 0);
        if (existCaches) {
            initViewPager(categoryCaches);
            initTabLayout();
        }
        Retrofits.rebase().categories(Configs.USERNAME)
            .compose(this.<List<Category>>bindToLifecycle())
            .doOnNext(new Consumer<List<Category>>() {
                @Override
                public void accept(@NonNull List<Category> categories) {
                    Stores.db.single().delete(Category.class);
                    Stores.db.single().save(categories);
                }
            })
            .filter(new Predicate<List<Category>>() {
                @Override
                public boolean test(@NonNull List<Category> categories) {
                    return !existCaches;
                }
            })
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Consumer<List<Category>>() {
                @Override
                public void accept(@NonNull List<Category> categories) {
                    initViewPager(categories);
                    initTabLayout();
                }
            });
    }


    private void initViewPager(List<Category> categories) {
        this.categories = categories;
        pagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(categories.size());
        onPageChangeListener = new AbstractPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                onPageChanged(position);
            }
        };
        viewPager.clearOnPageChangeListeners();
        viewPager.addOnPageChangeListener(onPageChangeListener);
    }


    private void initTabLayout() {
        tabLayout.setupWithViewPager(viewPager);
        onTabSelectedListener = new AbstractTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition(), false);
                String tabName = categories.get(tab.getPosition()).name;
                Analytics.of(context()).logEvent("TabSelected", "tab", tabName);
            }
        };
        tabLayout.clearOnTabSelectedListeners();
        tabLayout.addOnTabSelectedListener(onTabSelectedListener);
    }


    private void onPageChanged(int position) {
        Log.d(TAG, "[onPageChanged]" + position);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_login:
                LoginFragment.newInstance().show(getSupportFragmentManager(), "Login");
                return true;
            case R.id.action_about:
                AboutActivity.start(this);
                return true;
            case R.id.action_favorite:
                // FavoritesActivity.start(this);
                return true;
            case R.id.action_change_theme:
                Colorful.of(this).changeOne().apply();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private class MainPagerAdapter extends FragmentPagerAdapter {

        MainPagerAdapter(FragmentManager manager) {
            super(manager);
        }


        @Override
        public Fragment getItem(int position) {
            return FeedsFragment.newInstance(categories.get(position).key);
        }


        @Override
        public int getCount() {
            return categories.size();
        }


        @Override
        public CharSequence getPageTitle(int position) {
            return categories.get(position).name;
        }
    }
}
