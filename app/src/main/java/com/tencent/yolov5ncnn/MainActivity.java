package com.tencent.yolov5ncnn;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.util.Base64;
import android.view.View;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

public class MainActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Button buttonIrishComparison = findViewById(R.id.buttonIrishComparison);
        Button buttonIrishRecognition = findViewById(R.id.buttonIrishRecognition);
        buttonIrishComparison.setAlpha(0.6f);
        buttonIrishRecognition.setAlpha(0.6f);
        buttonIrishComparison.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CompareActivity.class));
            }
        });

        buttonIrishRecognition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, IdentifyActivity.class));
            }
        });

        try {
            initializeDataBase();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void initializeDataBase() throws IOException{
        SharedPreferences sharedPreferences = getSharedPreferences("DBPref", MODE_PRIVATE);
        sharedPreferences.getBoolean("isInit", false);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.apply();
        boolean isInit = getResources().getBoolean(R.bool.db_initialized);
        if (isInit)
            return;

        MySQLiteHelper mySQLiteHelper = new MySQLiteHelper(this);
        ArrayList<Pigeon> pigeons = getAllPigeonData();
        mySQLiteHelper.addAllDataItem(pigeons);
    }

    private ArrayList<Pigeon> getAllPigeonData() throws IOException {
        InputStream inputStream = getAssets().open("features64_blood.csv");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        ArrayList<Pigeon> pigeons = new ArrayList<>();
        // 读取CSV文件内容
        String line;
        while ((line = reader.readLine()) != null) {
            String[] values = line.split(",");
            Log.d("values ", Arrays.toString(values));
            // 解析特征向量
            float[] feature = new float[values.length - 2];
            Log.d("feature",feature.toString());
            for (int i = 2; i < values.length; i++) {
                feature[i - 2] = Float.parseFloat(values[i]);
            }
            int imageId = Integer.parseInt(values[0]);
            String blood = values[1];
            Drawable drawable = ResourcesCompat.getDrawable(getResources(), imageId, null);
            if (drawable == null)
                continue;
            Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            String base64String = Base64.encodeToString(byteArray, Base64.DEFAULT);

            // 创建Pigeon
            Pigeon pigeon = new Pigeon();
            pigeon.id = imageId;
            pigeon.blood = blood;
            pigeon.base64 = base64String;

            pigeons.add(pigeon);
        }

        return pigeons;
    }
}

