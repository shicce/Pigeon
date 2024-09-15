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
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

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


        MySQLiteHelper sqlHelper = new MySQLiteHelper(this);
//        try {
//            initializeDataBase();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        } catch (NoSuchFieldException e) {
//            throw new RuntimeException(e);
//        } catch (IllegalAccessException e) {
//            throw new RuntimeException(e);
//        }
    }

    private void initializeDataBase() throws IOException, NoSuchFieldException, IllegalAccessException {
        SharedPreferences sharedPreferences = getSharedPreferences("DBPref", MODE_PRIVATE);
        boolean isInit = sharedPreferences.getBoolean("isInit", false);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isInit", true);
        editor.apply();

        MySQLiteHelper mySQLiteHelper = new MySQLiteHelper(this);
        ArrayList<Pigeon> pigeons = getAllPigeonData();
        mySQLiteHelper.addAllDataItem(pigeons);
    }

    private ArrayList<Pigeon> getAllPigeonData() throws IOException, NoSuchFieldException, IllegalAccessException {
        InputStream inputStream = getAssets().open("features64_blood.csv");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        ArrayList<Pigeon> pigeons = new ArrayList<>();
        // 读取CSV文件内容
        String line;
        Map<String, Integer> dic = new HashMap<>();
        Field[] fields = R.drawable.class.getFields();
        for (Field field : fields) {
            String name = field.getName();
            if (name.startsWith("a")) {
                dic.put(name, field.getInt(null));
            }
        }

        while ((line = reader.readLine()) != null) {
            String[] values = line.split(",");
            Log.d("values ", Arrays.toString(values));
            // 解析特征向量

            int imageId = Integer.parseInt(values[0]);
            String blood = values[1];

            if (!dic.containsKey("a" + imageId))
                continue;

            int resourceId = dic.get("a" + imageId);
            Drawable drawable = ResourcesCompat.getDrawable(getResources(), resourceId, null);
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
            Log.i("image id", "getAllPigeonData: a" + imageId);
        }

        return pigeons;
    }
}

