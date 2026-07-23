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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.rkosegi.barcodevault.databinding.ActivityMainBinding;
import com.google.common.collect.Maps;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import static androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult;
import static com.github.rkosegi.barcodevault.Constants.EXTRA_ITEM;

public class MainActivity extends AppCompatActivity implements ItemActionCallback {
    private static final String TAG = MainActivity.class.getSimpleName();
    private PersistenceService ps;

    private ActivityResultLauncher<Intent> addItemLauncher;
    private ActivityResultLauncher<Intent> editItemLauncher;
    private ActivityResultLauncher<String[]> pickImportLauncher;

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addItemLauncher = registerForActivityResult(new StartActivityForResult(), this::onAddItemResult);
        editItemLauncher = registerForActivityResult(new StartActivityForResult(), this::onEditItemResult);
        pickImportLauncher = registerForActivityResult(new OpenDocument(), this::onImportFilePickResult);

        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setSupportActionBar(binding.toolbar);

        ps = PersistenceService.init(this);
        binding.btnAdd.setOnClickListener(view -> addItem());
        binding.swipeRefresh.setOnRefreshListener(this::refreshItems);
        binding.mainItemList.setLayoutManager(new LinearLayoutManager(this));
        binding.mainItemList.setHasFixedSize(true);
        binding.mainItemList.setAdapter(new ItemListAdapter(ps.allItems(), this));
    }

    private void onImportFilePickResult(Uri uri) {
        final List<Item> toLoad;
        try (InputStream in = getContentResolver().openInputStream(uri)) {
            toLoad = Utils.consumeStreamAs(in, new TypeToken<>() {
            });
        } catch (IOException e) {
            Log.e(TAG, "import failed", e);
            Toast.makeText(this, "Unable to load items from JSON file: " + e.getLocalizedMessage(),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        final Map<String, Item> existing = Maps.uniqueIndex(ps.allItems(), input -> input.text);
        final List<Item> toUpdate = new ArrayList<>();
        final List<Item> toAdd = new ArrayList<>();
        for (Item item : toLoad) {
            if (existing.containsKey(item.text)) {
                toUpdate.add(item);
            } else {
                toAdd.add(item);
            }
        }

        int added = 0;
        int updated = 0;
        for (Item item : toAdd) {
            ps.addItem(item);
            added++;
        }
        for (Item item : toUpdate) {
            ps.updateItem(item);
            updated++;
        }

        Toast.makeText(this, String.format(Locale.ENGLISH,
                        "Import completed (%d added, %d updated)", added, updated),
                Toast.LENGTH_SHORT).show();
    }

    private void refreshItems() {
        try {
            binding.mainItemList.setAdapter(new ItemListAdapter(ps.allItems(), this));
        } finally {
            binding.swipeRefresh.setRefreshing(false);
        }
    }


    @Override
    protected void onResume() {
        refreshItems();
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_add) {
            addItem();
            return true;
        }
        if (id == R.id.action_export) {
            exportAndShare();
            return true;
        }
        if (id == R.id.action_import) {
            importFromFile();
            return true;
        }
        if (id == R.id.action_clear) {
            deleteItems();
            return true;
        }
        if (id == R.id.action_about) {
            aboutApp();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void aboutApp() {
        final Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    private void importFromFile() {
        pickImportLauncher.launch(new String[]{"application/json", "text/*"});
    }

    private void deleteItems() {
        new AlertDialog.Builder(this)
                .setTitle("Clear all items?")
                .setMessage("Are you sure to clear all items?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    ps.deleteAllItems();
                    refreshItems();
                    Toast.makeText(this, "Items cleared", Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void addItem() {
        final Intent intent = new Intent(this, AddItemActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        addItemLauncher.launch(intent);
    }

    private void onEditItemResult(@NonNull ActivityResult res) {
        Log.i(TAG, "onEditItemResult=" + res);
        if (res.getResultCode() != RESULT_OK) {
            return;
        }
        if (res.getData() != null) {
            final Serializable sData = res.getData().getSerializableExtra(EXTRA_ITEM);
            if (sData != null) {
                ps.updateItem((Item) sData);
                binding.mainItemList.setAdapter(new ItemListAdapter(ps.allItems(), this));
            }
        }
    }

    private void onAddItemResult(@NonNull ActivityResult res) {
        Log.i(TAG, "onAddItemResult=" + res);
        if (res.getResultCode() != RESULT_OK) {
            return;
        }
        if (res.getData() != null) {
            final Serializable sData = res.getData().getSerializableExtra(EXTRA_ITEM);
            if (sData != null) {
                ps.addItem((Item) sData);
                final List<Item> items = ps.allItems();
                Log.i(TAG, "Now got " + items.size() + " item(s)");
                binding.mainItemList.setAdapter(new ItemListAdapter(items, this));
            }
        }
    }

    private void exportAndShare() {
        final File file = new File(getCacheDir(), "barcodes.json.txt");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(Utils.toJson(ps.allItems()));
        } catch (IOException e) {
            Log.e(TAG, "Error while writing to file", e);
        }
        final Uri uri = FileProvider.getUriForFile(
                this,
                getPackageName() + ".fileprovider",
                file);

        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(intent, "Share exported barcodes"));
    }

    @Override
    public void onEdit(Item item) {
        final Intent intent = new Intent(this, AddItemActivity.class);
        intent.putExtra(EXTRA_ITEM, item);
        intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        editItemLauncher.launch(intent);
    }

    @Override
    public void onDelete(Item item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete item?")
                .setMessage("Are you sure to delete this item?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    ps.deleteItem(item.id);
                    refreshItems();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void onShow(Item item) {
        final Intent intent = new Intent(this, ShowBarcodeActivity.class);
        intent.putExtra(EXTRA_ITEM, item);
        intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivity(intent);
    }
}