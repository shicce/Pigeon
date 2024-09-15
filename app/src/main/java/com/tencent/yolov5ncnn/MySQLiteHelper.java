package com.tencent.yolov5ncnn;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;

import java.util.ArrayList;
import java.util.LinkedList;

import androidx.annotation.Nullable;

public class MySQLiteHelper extends SQLiteOpenHelper {

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

    public Drawable getDrawable(int imageId, Resources resources){
        SQLiteDatabase db = this.getReadableDatabase();
        String SELECT_DATA = "SELECT * FROM pigeon WHERE id = " + imageId;
        Drawable drawable = null;
        try {
            String base64 = db.rawQuery(SELECT_DATA, null).getString(2);
            byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            drawable = new BitmapDrawable(resources, bitmap);
        } catch (Exception e){
            e.printStackTrace();
        }

        return drawable;
    }

    public Bitmap getBitmap(int imageId){
        SQLiteDatabase db = this.getReadableDatabase();
        String SELECT_DATA = "SELECT * FROM pigeon WHERE id = " + imageId;
        Bitmap bitmap = null;
        try {
            Cursor cursor = db.rawQuery(SELECT_DATA, null);
            String base64 = "";
            if (cursor != null && cursor.moveToFirst()) {
                base64 = cursor.getString(2);
                // 其他操作
            }
            byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private void addDataItem(SQLiteDatabase db, int imageId, String blood, String base64){

        String INSERT_DATA = "INSERT INTO pigeon (id, blood, base64) VALUES (" + imageId + ", '" + blood + "', '" + base64 + "')";
        db.execSQL(INSERT_DATA);

    }
}
