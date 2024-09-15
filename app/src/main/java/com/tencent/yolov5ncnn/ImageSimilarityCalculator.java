package com.tencent.yolov5ncnn;

import android.content.Context;
import android.util.Log;

import org.apache.commons.lang3.tuple.Triple;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ImageSimilarityCalculator {
    public static List<Triple<String, String, Float>> calculateSimilarities(Context context, float[] userFeature) {
        List<Triple<String, String, Float>> similarityList = new ArrayList<>();

        // 从assets文件夹中读取A_updated.csv
        List<float[]> features = new ArrayList<>();
        List<String> bloodIds = new ArrayList<>();
        List<String> imageIds = new ArrayList<>();

        try {
            InputStream inputStream = context.getAssets().open("features64_blood.csv");
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

            // 计算余弦相似度
            for (int i = 0; i < features.size(); i++) {
                float[] feature = features.get(i);
                float similarity = calculateCosineSimilarity(userFeature, feature);

                similarityList.add(Triple.of(imageIds.get(i), bloodIds.get(i), similarity));
            }

            // 对相似度进行降序排序
            Collections.sort(similarityList, (triple1, triple2) -> Float.compare(triple2.getRight(), triple1.getRight()));
            // 输出相似度计算结果
            Log.d("ImageSimilarityCalculator", "Similarity List Size: " + similarityList.size());

// 对相似度进行降序排序
            // 关闭读取器
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return similarityList;
    }

    //计算余弦相似度
    private static float calculateCosineSimilarity(float[] vector1, float[] vector2) {
        if (vector1.length != vector2.length) {
            throw new IllegalArgumentException("Vector dimensions must be the same");
        }

        float dotProduct = 0;
        float norm1 = 0;
        float norm2 = 0;

        for (int i = 0; i < vector1.length; i++) {
            dotProduct += vector1[i] * vector2[i];
            norm1 += Math.pow(vector1[i], 2);
            norm2 += Math.pow(vector2[i], 2);
        }

        if (norm1 == 0 || norm2 == 0) {
            return 0;
        }

        return (float) (dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2)));
    }
}
