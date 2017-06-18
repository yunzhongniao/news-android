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

package com.drakeet.rebase.multitype;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.bumptech.glide.Glide;
import com.drakeet.rebase.R;
import com.drakeet.rebase.activity.FeedBrowserActivity;
import com.drakeet.rebase.api.type.Feed;
import com.drakeet.rebase.tool.Analytics;
import com.drakeet.rebase.tool.Icons;
import com.drakeet.rebase.tool.MapBuilder;
import com.drakeet.rebase.tool.Strings;
import com.drakeet.rebase.tool.TimeDesc;
import com.drakeet.rebase.tool.guava.Optional;
import de.hdodenhof.circleimageview.CircleImageView;
import me.drakeet.multitype.ItemViewBinder;

/**
 * @author drakeet
 */
public class FeedViewBinder extends ItemViewBinder<Feed, FeedViewBinder.FeedViewHolder> {

    @NonNull
    @Override
    protected FeedViewHolder onCreateViewHolder(
        @NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        View root = inflater.inflate(R.layout.item_feed, parent, false);
        return new FeedViewHolder(root);
    }


    @Override
    protected void onBindViewHolder(@NonNull FeedViewHolder holder, @NonNull Feed feed) {
        final int lastPosition = getAdapter().getItemCount() - 1;
        if (getAdapter().getItemCount() == 1) {
            holder.itemView.setBackgroundResource(R.drawable.item_feed_single_background);
        } else if (getPosition(holder) == 0) {
            holder.itemView.setBackgroundResource(R.drawable.item_feed_first_background);
        } else if (getPosition(holder) == lastPosition) {
            holder.itemView.setBackgroundResource(R.drawable.item_feed_last_background);
        } else {
            holder.itemView.setBackgroundResource(R.drawable.item_feed_normal_background);
        }
        if (getPosition(holder) == lastPosition) {
            holder.divider.setVisibility(View.GONE);
        } else {
            holder.divider.setVisibility(View.VISIBLE);
        }
        holder.setFeed(feed);
    }


    static class FeedViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.icon) CircleImageView iconView;
        @BindView(R.id.category_name) TextView categoryName;
        @BindView(R.id.title) TextView title;
        @BindView(R.id.published_at) TextView publishedAt;
        @BindView(R.id.divider) View divider;
        Feed feed;


        FeedViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }


        void setFeed(Feed feed) {
            Optional<String> icon = Optional.fromNullable(feed.coverUrl);
            Glide.with(iconView.getContext())
                .load(icon.or(Icons.defaultIcon()))
                .dontAnimate()
                .placeholder(R.color.colorControlNormal)
                .into(iconView);
            categoryName.setText(feed.category);
            title.setText(feed.title);
            publishedAt.setText(TimeDesc.format(feed.publishedAt));
            this.feed = feed;
        }


        public void onClick(View view) {
            final Context context = view.getContext();
            if (view == itemView && Strings.notNullAndEmpty(feed.url)) {
                Intent intent = FeedBrowserActivity.newIntent(context, feed);
                context.startActivity(intent);
                Analytics.of(context).logEvent("FeedClick",
                    new MapBuilder("_id", feed._id)
                        .append("title", feed.title)
                        .append("category", feed.category)
                        .build()
                );
            }
        }
    }
}