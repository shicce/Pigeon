package com.tencent.yolov5ncnn;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.pytorch.IValue;
import org.pytorch.MemoryFormat;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

public class CompareActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST_1 = 1;
    private static final int PICK_IMAGE_REQUEST_2 = 2;

    private ImageView imageView1, imageView2;
    private TextView imagePathTextView1, imagePathTextView2;
    private Button button1, button2 ,detectButton;
    private YoloV5Ncnn yolov5ncnn = new YoloV5Ncnn();
    private Bitmap bitmap = null;
    private Bitmap processedBitmap = null;
    private  Module module = null;
    private TextView SimilaritytextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.compare);

        boolean ret_init = yolov5ncnn.Init(getAssets());
        if (!ret_init)
        {
            Log.e("CompareActivity", "yolov5ncnn Init failed");
        }
        // 初始化控件
        imageView1 = findViewById(R.id.imageView1);
        imageView2 = findViewById(R.id.imageView2);
        imagePathTextView1 = findViewById(R.id.imagePathTextView1);
        imagePathTextView2 = findViewById(R.id.imagePathTextView2);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        detectButton = findViewById(R.id.detectButton);
        SimilaritytextView = findViewById(R.id.rightTextView);
        // 设置点击事件
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery(PICK_IMAGE_REQUEST_1);
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery(PICK_IMAGE_REQUEST_2);
            }
        });
        detectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    Drawable drawable1 = imageView1.getDrawable();
                    Drawable drawable2 = imageView2.getDrawable();
                    Bitmap bitmap1 = ((BitmapDrawable) drawable1).getBitmap();
                    Bitmap bitmap2 = ((BitmapDrawable) drawable2).getBitmap();
                // Ensure that imageView1 and imageView2 are properly initialized
                if (bitmap1 != null && bitmap2 != null) {
                    try {
                        module = Module.load(assetFilePath(view.getContext(), "feature64-1.12.pt"));
                    } catch (IOException e) {
//                        throw new RuntimeException(e);
                        Log.e("CompareAcitivity", "Error reading assets", e);
//                        finish();
                    }
                    // preparing input tensor
                    final Tensor inputTensor1 = TensorImageUtils.bitmapToFloat32Tensor(bitmap1,
                            TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB, MemoryFormat.CHANNELS_LAST);
                    final Tensor inputTensor2 = TensorImageUtils.bitmapToFloat32Tensor(bitmap2,
                            TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB, MemoryFormat.CHANNELS_LAST);
                    // running the model
                    final Tensor outputTensor1 = module.forward(IValue.from(inputTensor1)).toTensor();
                    final Tensor outputTensor2 = module.forward(IValue.from(inputTensor2)).toTensor();

                    final float[]  feature1= outputTensor1.getDataAsFloatArray();
                    final float[]  feature2= outputTensor2.getDataAsFloatArray();

                    // 打印 feature1 和 feature2
                    Log.d("SimilarityUtils", "Feature1: " + arrayToString(feature1));
                    Log.d("SimilarityUtils", "Feature2: " + arrayToString(feature2));
//                    float cosineSimilarityValue = cosineSimilarity(feature1, feature2);
                    float euclideanDistanceValue = euclideanDistance(feature1, feature2);
                    // showing className on UI
//                    SimilaritytextView.setText("余弦相似度: " + cosineSimilarityValue+"\n欧几里得距离: " + euclideanDistanceValue);
                    String a;
                    if (euclideanDistanceValue < 1) {
                        a = "可能是一个家族";
                    } else {
                        a = "可能不是一个家族";
                    }
                    SimilaritytextView.setText("它们的差距为:" + String.format(Locale.CHINA, "%.3f", euclideanDistanceValue)+"\n"+a);

                }else {
                    // Handle the case where bitmaps are not properly initialized
                    Toast.makeText(view.getContext(), "Bitmaps not initialized", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // 计算两个 float 数组之间的余弦相似度
    public static float cosineSimilarity(float[] feature1, float[] feature2) {
        float dotProduct = 0;
        float norm1 = 0;
        float norm2 = 0;

        // 计算点积
        for (int i = 0; i < feature1.length; i++) {
            dotProduct += feature1[i] * feature2[i];
            norm1 += feature1[i] * feature1[i];
            norm2 += feature2[i] * feature2[i];
        }

        // 计算余弦相似度
        float similarity = dotProduct / (float) (Math.sqrt(norm1) * Math.sqrt(norm2));
        return similarity;
    }
    // 计算两个 float 数组之间的欧几里得距离
    public static float euclideanDistance(float[] feature1, float[] feature2) {
        float sumSquaredDiff = 0;

        // 计算各维度上的差的平方的和
        for (int i = 0; i < feature1.length; i++) {
            float diff = feature1[i] - feature2[i];
            sumSquaredDiff += diff * diff;
        }

        // 计算欧几里得距离
        float distance = (float) Math.sqrt(sumSquaredDiff);
        return distance;
    }
    // 辅助函数：将 float 数组转换为字符串
    static String arrayToString(float[] array) {
        StringBuilder stringBuilder = new StringBuilder("[");
        for (int i = 0; i < array.length; i++) {
            stringBuilder.append(array[i]);
            if (i < array.length - 1) {
                stringBuilder.append(", ");
            }
        }
        stringBuilder.append("]");
        return stringBuilder.toString();
    }
    private void openGallery(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, requestCode);
    }
    private void processImage(final Uri selectedImageUri, final ImageView imageView) {
                try {
                    bitmap = decodeUri(selectedImageUri);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
                //用cpu yolov5 detect
                YoloV5Ncnn.Obj[] objects = yolov5ncnn.Detect(bitmap, false);
//                    YoloV5Ncnn.Obj[] objects1 = yolov5ncnn.Detect(yourSelectedImage, true);
                if (objects == null)
                {
                    imageView.setImageBitmap(bitmap);
                    return;
                }
            if (objects.length >= 1) {
                int x = (int) objects[0].x;
                int y = (int) objects[0].y;
                int w = (int) objects[0].w;
                int h = (int) objects[0].h;
                processedBitmap = Bitmap.createBitmap(bitmap, x, y, w, h);
            // 显示处理后的图片在相应的 ImageView
                imageView.setImageBitmap(processedBitmap);
            } else {
                imageView.setImageBitmap(bitmap);
            }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            String imageName = getNameFromURI(selectedImageUri);
            if (requestCode == PICK_IMAGE_REQUEST_1) {
                // 显示在imageView1
                processImage(selectedImageUri,imageView1);
                // 显示照片名字在imagePathTextView1
                imagePathTextView1.setText(imageName);

            } else if (requestCode == PICK_IMAGE_REQUEST_2) {
                // 显示在imageView2
                processImage(selectedImageUri,imageView2);
                // 显示照片名字在imagePathTextView2
                imagePathTextView2.setText(imageName);
            }
        }
    }

    /*
           decodeUri 方法用于解码图片，并处理图片的尺寸和旋转。
                */
    private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException
    {
        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o);

        // The new size we want to scale to
        final int REQUIRED_SIZE = 640;

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE
                    || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o2);

        // Rotate according to EXIF
        int rotate = 0;
        try
        {
            ExifInterface exif = new ExifInterface(getContentResolver().openInputStream(selectedImage));
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        }
        catch (IOException e)
        {
            Log.e("decodeUri", "ExifInterface IOException");
        }

        Matrix matrix = new Matrix();
        matrix.postRotate(rotate);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private String getNameFromURI(Uri contentUri) {
    String[] projection = {MediaStore.Images.Media.DATA, MediaStore.Images.Media.DISPLAY_NAME};
    Cursor cursor = getContentResolver().query(contentUri, projection, null, null, null);
    if (cursor == null) return null;

    int columnIndexDisplayName = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);

    cursor.moveToFirst();

    String displayName = cursor.getString(columnIndexDisplayName);

    cursor.close();
    // 输出日志
    Log.d("ImageInfo", ", Name: " + displayName);
    // 返回图片名字
    return displayName;
}
    /**
     * Copies specified asset to the file in /files app directory and returns this file absolute path.
     *
     * @return absolute file path
     */
    public static String assetFilePath(Context context, String assetName) throws IOException {
        File file = new File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        }
    }

}

