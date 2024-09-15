package com.tencent.yolov5ncnn;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

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
    }

    private void initializeDataBase(){
        boolean isInit = getResources().getBoolean(R.bool.db_initialized);
        if (isInit)
            return;

        MySQLiteHelper mySQLiteHelper = new MySQLiteHelper(this);
        mySQLiteHelper.addAllDataItem();
    }

    private ArrayList<MySQLiteHelper.Pigeon> getAllPigeonData() throws IOException {
        InputStream inputStream = getAssets().open("features64_blood.csv");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

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
            features.add(feature);
            bloodIds.add(values[1]);
            imageIds.add(values[0]);
        }
    }
}

