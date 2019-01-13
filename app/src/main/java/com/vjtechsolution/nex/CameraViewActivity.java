package com.vjtechsolution.nex;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.arasthel.asyncjob.AsyncJob;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraUtils;
import com.otaliastudios.cameraview.CameraView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import me.echodev.resizer.Resizer;

public class CameraViewActivity extends AppCompatActivity {

    private CameraView cameraView;
    private ImageView cameraTrigger;

    private int count = 0;
    private int picLimit;
    private TextView picCountText;
    private SharedPreferences userPref;
    private ArrayList<String> images;

    private ArrayList<byte[]> byteList = new ArrayList<>();

    private Boolean tutup;

    private MaterialDialog.Builder builder;
    private MaterialDialog loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_view);

        userPref = getSharedPreferences("NEX", 0);

        Bundle extras = getIntent().getExtras();
        if(extras != null){
            tutup = extras.getBoolean("tutup", false);
        }

        if(!tutup){
            //limit foto sales 3, deliv 2
            picLimit = (userPref.getString("level", "").equals("sales")) ? 3 : 2;
        }else{
            //jika toko tutup, limit foto 1
            picLimit = 1;
        }

        builder = new MaterialDialog.Builder(this);
        builder.theme(Theme.LIGHT)
                .cancelable(false)
                .content("Mengompress gambar, mohon tunggu")
                .progress(true, 0);

        loader = builder.build();

        cameraView = findViewById(R.id.camera);
        cameraTrigger = findViewById(R.id.cameraTrigger);
        picCountText = findViewById(R.id.ft_count);
        images = new ArrayList<>();

        picCountText.setText(String.valueOf(picLimit));

        cameraTrigger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count++;
                cameraView.captureSnapshot();
                loader.show();
            }
        });

        cameraView.addCameraListener(new CameraListener() {
            @Override
            public void onPictureTaken(final byte[] jpeg) {
                super.onPictureTaken(jpeg);

                CameraUtils.decodeBitmap(jpeg, new CameraUtils.BitmapCallback() {
                    @Override
                    public void onBitmapReady(final Bitmap bitmap) {

                        AsyncJob.doInBackground(new AsyncJob.OnBackgroundJob() {
                            @Override
                            public void doOnBackground() {
                                try {

                                    String path = getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString();
                                    String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss", Locale.ENGLISH).format(new Date());
                                    String fileName = timeStamp + "_" + String.valueOf(count);

                                    OutputStream out = null;
                                    File file = new File(path, fileName + ".jpg");
                                    out = new FileOutputStream(file);

                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

                                    new Resizer(CameraViewActivity.this)
                                            .setTargetLength(800)
                                            .setQuality(80)
                                            .setSourceImage(file)
                                            .getResizedFile();

                                    images.add(file.getAbsolutePath());

                                    out.close();

                                    // Send the result to the UI thread and show it on a Toast
                                    AsyncJob.doOnMainThread(new AsyncJob.OnMainThreadJob() {
                                        @Override
                                        public void doInUIThread() {
                                            loader.dismiss();
                                            picCountText.setText(String.valueOf(picLimit-images.size()));

                                            if(images.size() == picLimit){
                                                Intent visitToko = new Intent(CameraViewActivity.this, VisitTokoActivity.class);
                                                visitToko.putStringArrayListExtra("images", images);
                                                setResult(Activity.RESULT_OK, visitToko);
                                                finish();
                                            }
                                        }
                                    });

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                    }
                });

                /*
                AsyncJob.doInBackground(new AsyncJob.OnBackgroundJob() {
                    @Override
                    public void doOnBackground() {
                        try {

                            String path = getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString();
                            String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss", Locale.ENGLISH).format(new Date());
                            String fileName = timeStamp + "_" + String.valueOf(count);

                            Bitmap bitmap = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length);

                            OutputStream out = null;
                            File file = new File(path, fileName + ".jpg");
                            out = new FileOutputStream(file);

                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

                            /*
                            ExifInterface exifInterface = new ExifInterface(file.getAbsolutePath());
                            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

                            if(orientation == ExifInterface.ORIENTATION_ROTATE_90){
                                Bitmap bitmapRotate = BitmapFactory.decodeFile(file.getAbsolutePath());
                                Matrix matrix = new Matrix();
                                matrix.setRotate(90);
                                Bitmap result = Bitmap.createBitmap(bitmapRotate, 0, 0, bitmapRotate.getWidth(), bitmapRotate.getHeight(), matrix, false);

                                OutputStream outRotate = new FileOutputStream(file);
                                result.compress(Bitmap.CompressFormat.JPEG, 100, outRotate);

                                outRotate.close();
                            }


                            new Resizer(CameraViewActivity.this)
                                    .setTargetLength(800)
                                    .setQuality(80)
                                    .setSourceImage(file)
                                    .getResizedFile();

                            images.add(file.getAbsolutePath());

                            out.close();

                            // Send the result to the UI thread and show it on a Toast
                            AsyncJob.doOnMainThread(new AsyncJob.OnMainThreadJob() {
                                @Override
                                public void doInUIThread() {
                                    loader.dismiss();
                                    picCountText.setText(String.valueOf(picLimit-images.size()));

                                    if(images.size() == picLimit){
                                        Intent visitToko = new Intent(CameraViewActivity.this, VisitTokoActivity.class);
                                        visitToko.putStringArrayListExtra("images", images);
                                        setResult(Activity.RESULT_OK, visitToko);
                                        finish();
                                    }
                                }
                            });

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                */
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraView.destroy();
    }
}
