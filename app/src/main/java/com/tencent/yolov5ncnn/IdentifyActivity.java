package com.tencent.yolov5ncnn;

import static com.tencent.yolov5ncnn.CompareActivity.arrayToString;
import static com.tencent.yolov5ncnn.CompareActivity.assetFilePath;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import org.apache.commons.lang3.tuple.Triple;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.pytorch.IValue;
import org.pytorch.MemoryFormat;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class IdentifyActivity extends AppCompatActivity {
    private ImageView imageView;
    private Button buttonSelectImage, buttonUpload;
    private YoloV5Ncnn yolov5ncnn = new YoloV5Ncnn();
    private Bitmap bitmap = null;
    private Bitmap processedBitmap = null;
    private TextView textViewImageName;
    private RecyclerView recyclerView;

    private Module module = null;

    private List<Bird> birdList;
    private BirdAdapter birdAdapter;
    List<Triple<String, String, Float>> similarityList;
    private static final int PICK_IMAGE_REQUEST = 1;
    private final int bird_nums = 10 ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.identify);
        boolean ret_init = yolov5ncnn.Init(getAssets());
        if (!ret_init)
        {
            Log.e("IndentifyActivity", "yolov5ncnn Init failed");
        }
        // 初始化视图元素
        imageView = findViewById(R.id.imageView);
        buttonSelectImage = findViewById(R.id.buttonSelectImage);
        textViewImageName = findViewById(R.id.textViewImageName);
        buttonUpload = findViewById(R.id.buttonUpload);
        recyclerView = findViewById(R.id.recyclerView);
        // 初始化 Bird 数据
        birdList = new ArrayList<>();
        // 初始化 RecyclerView
        birdAdapter = new BirdAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(birdAdapter);

        // 设置按钮的点击事件,进入图库
        buttonSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent,PICK_IMAGE_REQUEST);
            }
        });

        // 设置开始对比按钮的点击事件
        buttonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Drawable drawable = imageView.getDrawable();//从iamgeView获取
                Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                if(bitmap != null){
                    try{
                        module = Module.load(assetFilePath(v.getContext(), "feature64-1.12.pt"));
                        final Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(bitmap,TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB, MemoryFormat.CHANNELS_LAST);
                        final Tensor outputTensor = module.forward(IValue.from(inputTensor)).toTensor();
                        final float[]  inputfeature= outputTensor.getDataAsFloatArray();

                        Log.d("Similarity", "inputfeature: " + arrayToString(inputfeature));

                        // 调用 ImageSimilarityCalculator 类的 calculateSimilarities() 方法
                        similarityList = ImageSimilarityCalculator.calculateSimilarities(v.getContext(), inputfeature);

                        birdList.clear();

                        // 显示前五个相似度最高的结果
                        for (int i = 0; i < Math.min(bird_nums, similarityList.size()); i++) {
                            Triple<String, String, Float> triple = similarityList.get(i);
                            String imageId = triple.getLeft();
                            String bloodId = triple.getMiddle();
                            Float similarity = triple.getRight();
                            // 创建 Bird 对象
                            Bird bird = new Bird(imageId, bloodId);
                            bird.setSimilarity(similarity);
                            birdList.add(bird);
                        }
                        // 通知 RecyclerView 更新数据
                        birdAdapter.notifyDataSetChanged();
                    } catch (IOException e) {
                        Log.e("IndentifyAcitivity", "Error reading assets", e);
                    }
                }
            }
        });
    }
    // BirdAdapter 内部类
    private class BirdAdapter extends RecyclerView.Adapter<BirdAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bird, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Bird bird = birdList.get(position);
            int resourceId = holder.itemView.getContext().getResources().getIdentifier("a"+bird.getId(), "drawable", holder.itemView.getContext().getPackageName());

            MySQLiteHelper helper = new MySQLiteHelper(holder.itemView.getContext());
            Bitmap bitmap = helper.getBitmap(Integer.parseInt(bird.getId()));
            if (bitmap == null){
                holder.imageView.setImageResource(R.drawable.eye);
            }else{
                holder.imageView.setImageBitmap(bitmap);
            }
            // 设置图片到 ImageView
//            if (resourceId != 0) {
//                holder.imageView.setImageBitmap();
//                holder.imageView.setImageResource(resourceId);
//            } else {
//                // 如果找不到资源，设置默认图片
//                holder.imageView.setImageResource(R.drawable.eye); // 替换成你的默认图片资源 ID
//            }
            String displayText = "图片id:" +bird.getId() + "\n" +"血统:" + bird.getBlood() + "\n" +"相似度:"+ bird.getSimilarity();
            holder.textView.setText(displayText);
        }

        @Override
        public int getItemCount() {
            return birdList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            TextView textView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.itemImageView);
                textView = itemView.findViewById(R.id.itemTextView);
            }
        }
    }
    // 从图库选择图片后的处理逻辑
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            String imageName = getNameFromURI(selectedImageUri);
            if (requestCode == PICK_IMAGE_REQUEST) {
                // 显示在imageView
                processImage(selectedImageUri,imageView);
                // 显示照片名字在textViewImageName
                textViewImageName.setText(imageName);
            }
        }
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
}
