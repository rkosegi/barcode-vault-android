/*
Copyright 2026 Richard Kosegi

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.github.rkosegi.barcodevault;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.common.base.Strings;

import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

public class ItemListAdapter extends RecyclerView.Adapter<ItemListAdapter.ViewHolder> {
    private static final String TAG = ItemListAdapter.class.getSimpleName();
    private final List<Item> items;
    private final ItemActionCallback callback;

    public ItemListAdapter(List<Item> items, ItemActionCallback callback) {
        this.items = items;
        this.callback = callback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final LayoutInflater li = LayoutInflater.from(parent.getContext());
        final View listItem = li.inflate(R.layout.item_row, parent, false);
        return new ViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Item item = items.get(position);
        Log.i(TAG, "onBindViewHolder(pos=" + position + ",item=" + item + ")");
        holder.desc.setText(item.desc);
        holder.type.setText(item.type);
        if (!Strings.isNullOrEmpty(item.iconUrl)) {
            Glide.with(holder.icon).load(Uri.parse(item.iconUrl)).into(holder.icon);
        }
        holder.itemView.setOnClickListener(v -> callback.onShow(item));
        holder.itemView.setOnLongClickListener(v -> {
            final PopupMenu pm = new PopupMenu(v.getContext(), v);
            pm.getMenu().add("Edit");
            pm.getMenu().add("Delete");
            pm.setOnMenuItemClickListener(menuItem -> {
                switch (Objects.requireNonNull(menuItem.getTitle()).toString()) {
                    case "Edit":
                        callback.onEdit(item);
                        return true;
                    case "Delete":
                        callback.onDelete(item);
                        return true;
                }
                return false;
            });
            pm.show();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView icon;
        private final TextView desc;
        private final TextView type;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.item_icon);
            desc = itemView.findViewById(R.id.item_desc);
            type = itemView.findViewById(R.id.item_type);
        }
    }
}
