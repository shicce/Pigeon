package com.tencent.yolov5ncnn;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.LinkedList;

import androidx.annotation.Nullable;

public class MySQLiteHelper extends SQLiteOpenHelper {
    public class Pigeon {
        public int id;
        public String blood;
        public String base64;
    }

    private static final String DATABASE_NAME = "pigeon.db";
    private static final int DATABASE_VERSION = 1;
    public MySQLiteHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_TABLE = "CREATE TABLE pigeon (id INTEGER PRIMARY KEY, blood TEXT, base64 TEXT)";
        sqLiteDatabase.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS pigeon");
        onCreate(sqLiteDatabase);
    }

    public void addAllDataItem(ArrayList<Pigeon> info){
        SQLiteDatabase db = this.getWritableDatabase();
        for (Pigeon item : info){
            addDataItem(db, item.id, item.blood, item.base64);
        }

        db.close();
    }

    private void addDataItem(SQLiteDatabase db, int imageId, String blood, String base64){

        String INSERT_DATA = "INSERT INTO pigeon (id, blood, base64) VALUES (" + imageId + ", '" + blood + "', '" + base64 + "')";
        db.execSQL(INSERT_DATA);

    }
}
