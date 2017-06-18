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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.drakeet.rebase.Configs;
import com.drakeet.rebase.R;
import com.drakeet.rebase.api.Retrofits;
import com.drakeet.rebase.api.type.Feed;
import com.drakeet.rebase.tool.TimeDesc;
import com.drakeet.rebase.tool.Toasts;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

import static com.drakeet.rebase.tool.Strings.emptyToNull;

public class AdminActivity extends ToolbarActivity {

    @BindView(R.id.category) AutoCompleteTextView categoryView;
    @BindView(R.id.title) EditText title;
    @BindView(R.id.url) EditText url;
    @BindView(R.id.cover_url) EditText coverUrl;
    @BindView(R.id.content) EditText content;


    public static void start(Context context) {
        Intent intent = new Intent(context, AdminActivity.class);
        context.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        ButterKnife.bind(this);
    }


    private void onPost() {
        String category = categoryView.getText().toString();
        Feed feed = new Feed();
        feed.title = emptyToNull(title.getText().toString());
        feed.url = emptyToNull(url.getText().toString());
        feed.coverUrl = emptyToNull(coverUrl.getText().toString());
        feed.content = emptyToNull(content.getText().toString());

        Retrofits.rebase().newFeed(Configs.USERNAME, category, feed)
            .compose(this.<Feed>bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Consumer<Feed>() {
                @Override
                public void accept(@NonNull Feed feed) {
                    Toasts.showShort(getString(R.string.has_posted_at) +
                        TimeDesc.gsonFormat(feed.publishedAt));
                    AdminActivity.this.finish();
                }
            });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_post:
                onPost();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public boolean canBack() { return true; }
}
