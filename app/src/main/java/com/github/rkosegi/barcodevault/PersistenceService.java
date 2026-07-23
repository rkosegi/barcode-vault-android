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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class PersistenceService extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "barcodes.db";
    private static final String TABLE_BARCODES = "barcodes";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_BARCODE_TEXT = "barcode_text";
    private static final String COLUMN_BARCODE_DESC = "barcode_desc";
    private static final String COLUMN_BARCODE_TYPE = "barcode_type";
    private static final String COLUMN_ICON_URL = "icon_url";
    private static final String COLUMN_CREATED_AT = "created_at";


    private static final String[] ALL_COLUMNS = new String[]{
            COLUMN_ID,
            COLUMN_BARCODE_TEXT,
            COLUMN_BARCODE_DESC,
            COLUMN_BARCODE_TYPE,
            COLUMN_ICON_URL,
            COLUMN_CREATED_AT
    };
    private static final int DATABASE_VERSION = 4;
    private static PersistenceService singleton;

    public static PersistenceService init(Context context) {
        singleton = new PersistenceService(context);
        return singleton;
    }

    public PersistenceService(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        singleton = this;
    }

    public static PersistenceService getInstance() {
        return singleton;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_BARCODES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_BARCODE_TEXT + " TEXT," +
                COLUMN_BARCODE_DESC + " TEXT," +
                COLUMN_BARCODE_TYPE + " TEXT," +
                COLUMN_ICON_URL + " TEXT," +
                COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BARCODES);
        onCreate(db);
    }

    public void addItem(Item item) {
        final ContentValues cv = contentFromItem(item);
        getWritableDatabase().insert(TABLE_BARCODES, null, cv);
    }

    public void deleteItem(int id) {
        getWritableDatabase().delete(TABLE_BARCODES, COLUMN_ID+"=?", new String[]{ String.valueOf(id) });
    }

    public void updateItem(Item item) {
        final ContentValues cv = contentFromItem(item);
        getWritableDatabase().update(TABLE_BARCODES, cv, COLUMN_ID+"=?", new String[]{ String.valueOf(item.id) });
    }

    public void deleteAllItems() {
        getWritableDatabase().delete(TABLE_BARCODES, null, null);
    }

    public List<Item> allItems() {
        final List<Item> ret = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query(TABLE_BARCODES, ALL_COLUMNS, null,
                null, null, null,  COLUMN_CREATED_AT + " ASC")) {
            if (c.moveToFirst()) {
                while (!c.isAfterLast()) {
                    ret.add(new Item(
                            c.getInt(c.getColumnIndexOrThrow(COLUMN_ID)),
                            c.getString(c.getColumnIndexOrThrow(COLUMN_BARCODE_TEXT)),
                            c.getString(c.getColumnIndexOrThrow(COLUMN_BARCODE_DESC)),
                            c.getString(c.getColumnIndexOrThrow(COLUMN_BARCODE_TYPE)),
                            c.getString(c.getColumnIndexOrThrow(COLUMN_ICON_URL)),
                            c.getString(c.getColumnIndexOrThrow(COLUMN_CREATED_AT)))
                    );
                    c.moveToNext();
                }
            }
        }
        return ret;
    }

    private ContentValues contentFromItem(Item item) {
        final ContentValues cv = new ContentValues();
        cv.put(COLUMN_BARCODE_TEXT, item.text);
        cv.put(COLUMN_BARCODE_DESC, item.desc);
        cv.put(COLUMN_BARCODE_TYPE, item.type);
        cv.put(COLUMN_ICON_URL, item.iconUrl);
        return cv;
    }
}
