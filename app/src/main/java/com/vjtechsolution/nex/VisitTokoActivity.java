package com.vjtechsolution.nex;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.arasthel.asyncjob.AsyncJob;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import de.mateware.snacky.Snacky;
import io.realm.Realm;
import io.realm.RealmResults;
import me.echodev.resizer.Resizer;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class VisitTokoActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView scannerIcon;
    private TextView visitKodeAsset;
    private TextView visitLokasi;
    private TextView visitFoto;
    private TextView visitProduk;
    private ImageButton visitBtnCamera;
    private ImageButton visitBtnOrder;
    private Button visitBtnSimpan;

    private int count = 0;

    private Double latitude = 0.0;
    private Double longitude = 0.0;
    private String kodeAsset = "";
    private ArrayList<String> images = new ArrayList<>();
    private ArrayList<String > produk = new ArrayList<>();
    private ArrayList<String> quantity = new ArrayList<>();
    private Boolean tutup = false;
    private int picLimit;
    private Boolean order = false;
    private String levelUser;
    private String catatan = "";
    private String uniqId;

    private MaterialDialog.Builder builder;
    private MaterialDialog loader;

    private SharedPreferences userPref;
    private Realm realm;

    private int currentBw;
    private String idOrder;
    private String idFoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit_toko);

        userPref = getSharedPreferences("NEX", 0);
        levelUser = userPref.getString("level","");

        Realm.init(this);

        uniqId = new SimpleDateFormat("ddMMyyyyHHmmss", Locale.ENGLISH).format(new Date());

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setBackground(getResources().getDrawable(R.color.colorPrimary));
        setSupportActionBar(toolbar);
        this.setTitle("Visit Toko");

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        builder = new MaterialDialog.Builder(this);
        builder.theme(Theme.LIGHT)
                .cancelable(false)
                .content("Mohon tunggu..")
                .progress(true, 0);

        loader = builder.build();

        scannerIcon = findViewById(R.id.scannerIcon);
        visitKodeAsset = findViewById(R.id.visitKodeAsset);
        visitLokasi = findViewById(R.id.visitLokasi);
        visitFoto = findViewById(R.id.visitFoto);
        visitProduk = findViewById(R.id.visitProduk);
        visitBtnCamera = findViewById(R.id.visitBtnCamera);
        visitBtnOrder = findViewById(R.id.visitBtnOrder);
        visitBtnSimpan = findViewById(R.id.visitBtnSimpan);

        idOrder = uniqId;
        idFoto = "FT" + uniqId;

        //jika delivery, ubah form jadi catatan
        if(levelUser.equals("delivery")){
            visitProduk.setHint("Catatan");
        }

        scannerIcon.setOnClickListener(this);
        visitBtnCamera.setOnClickListener(this);
        visitBtnOrder.setOnClickListener(this);
        visitBtnSimpan.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.visit_menu, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.tokoTutup:
                /*
                if(images.size() > 0){
                    images = new ArrayList<>();
                    visitFoto.setText("");
                    //Toast.makeText(VisitTokoActivity.this, "Foto dihapus, ambil ulang foto", Toast.LENGTH_SHORT).show();
                }
                   */

                Intent listDataTokoActivity = new Intent(VisitTokoActivity.this, ListDataTokoActivity.class);
                startActivityForResult(listDataTokoActivity, 6);
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    @Override
    public void onResume(){
        super.onResume();

        Bundle extras = getIntent().getExtras();
        if(extras != null){
            kodeAsset = extras.getString("kode_asset", "");
            latitude = extras.getDouble("lat", 0);
            longitude = extras.getDouble("lng", 0);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.scannerIcon:
                //camera request
                if (ContextCompat.checkSelfPermission(VisitTokoActivity.this, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_DENIED) {
                    //ask permission
                    ActivityCompat.requestPermissions(VisitTokoActivity.this, new String[]{Manifest.permission.CAMERA}, 5);
                }else{
                    if(!tutup) {
                        launchVisitToko();
                    }else{
                        Snacky.builder()
                                .setActivity(VisitTokoActivity.this)
                                .setActionText("Reset Form")
                                .setText("Order tidak bisa dilakukan karena status toko Tutup")
                                .setActionClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        resetForm();
                                    }
                                })
                                .setDuration(Snacky.LENGTH_SHORT)
                                .build()
                                .show();
                    }
                }
                break;

            case R.id.visitBtnCamera:
                Intent cameraView = new Intent(VisitTokoActivity.this, CameraViewActivity.class);
                cameraView.putExtra("source", "visit_toko");
                cameraView.putExtra("tutup", tutup);
                startActivityForResult(cameraView, 8);


                /*
                if (ContextCompat.checkSelfPermission(VisitTokoActivity.this, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_DENIED) {
                    //ask permission
                    ActivityCompat.requestPermissions(VisitTokoActivity.this, new String[]{Manifest.permission.CAMERA}, 5);
                }else{
                    images = new ArrayList<>();
                    visitFoto.setText("");

                    startCamera();
                }
                */
                break;

            case R.id.visitBtnOrder:
                if(!tutup) {
                    if (levelUser.equals("sales")) {
                        //jika sales, buka activity add produk
                        Intent order = new Intent(VisitTokoActivity.this, OrderActivity.class);
                        startActivityForResult(order, 4);
                    } else {
                        //jika delivery, buka activity add catatan
                        Intent catatan = new Intent(VisitTokoActivity.this, CatatanDeliveryActivity.class);
                        startActivityForResult(catatan, 5);
                    }
                }else{
                    if(levelUser.equals("sales")) {
                        Snacky.builder()
                                .setActivity(VisitTokoActivity.this)
                                .setActionText("Reset Form")
                                .setText("Order tidak bisa dilakukan karena status toko Tutup")
                                .setActionClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        resetForm();
                                    }
                                })
                                .setDuration(Snacky.LENGTH_SHORT)
                                .build()
                                .show();
                    }else{
                        //jika delivery, buka activity add catatan
                        Intent catatan = new Intent(VisitTokoActivity.this, CatatanDeliveryActivity.class);
                        startActivityForResult(catatan, 5);
                    }
                }
                break;

            case R.id.visitBtnSimpan:
                if(!tutup){
                    //limit foto sales 3, deliv 2
                    picLimit = (levelUser.equals("sales")) ? 3 : 2;
                }else{
                    //jika toko tutup, limit foto 1
                    picLimit = 1;
                }

                if(!tutup) {
                    if (kodeAsset.equals("") || latitude == 0.0 || longitude == 0.0 || images.size() != picLimit || visitProduk.getText().toString().equals("")) {
                        Snacky.builder()
                                .setActivity(VisitTokoActivity.this)
                                .setActionText("Oke")
                                .setText("Gagal.. Cek kembali form yang ada isikan")
                                .setDuration(Snacky.LENGTH_LONG)
                                .build()
                                .show();

                    } else {
                        //submit visit toko
                        submitDataVisitToko();
                    }
                }else{
                    if (kodeAsset.equals("") || latitude == 0.0 || longitude == 0.0 || images.size() != picLimit) {
                        Snacky.builder()
                                .setActivity(VisitTokoActivity.this)
                                .setActionText("Oke")
                                .setText("Gagal.. Cek kembali form yang ada isikan")
                                .setDuration(Snacky.LENGTH_LONG)
                                .build()
                                .show();

                    } else {
                        //submit visit toko
                        submitDataVisitToko();
                    }
                }

                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 5: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted
                    launchVisitToko();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    //Toast.makeText(VisitTokoActivity.this, "Izin kamera diperlukan", Toast.LENGTH_LONG).show();
                    Snacky.builder()
                            .setActivity(VisitTokoActivity.this)
                            .setActionText("Oke")
                            .setText("Gagal.. Cek kembali form yang ada isikan")
                            .setDuration(Snacky.LENGTH_LONG)
                            .build()
                            .show();
                }
            }

            case 6: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCamera();
                } else {
                    Snacky.builder()
                            .setActivity(VisitTokoActivity.this)
                            .setActionText("Oke")
                            .setText("Izin kamera diperlukan")
                            .setDuration(Snacky.LENGTH_LONG)
                            .build()
                            .show();
                }
            }
        }
    }

    public void startCamera(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            File photoFile = null;
            try {
               photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(VisitTokoActivity.this,
                        "com.example.android.fileprovider",
                        photoFile);
                //takePictureIntent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, 7);
            }
        }
    }

    private File createImageFile() throws IOException {
        String path = getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString();
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss", Locale.ENGLISH).format(new Date());
        String fileName = timeStamp + "_" + String.valueOf(count);

        File file = new File(path, fileName + ".jpg");

        images.add(file.getAbsolutePath());

        return file;
    }

    public void launchVisitToko(){
        Intent barcodeScanner = new Intent(VisitTokoActivity.this, BarcodeScannerActivity.class);
        barcodeScanner.putExtra("source", "visit_toko");
        startActivityForResult(barcodeScanner, 1);
    }

    public void submitDataVisitToko() {
        loader.show();
        NetworkQuality networkQuality = new NetworkQuality();
        networkQuality.setOnEventListener(new NetworkQuality.NetworkListener() {
            @Override
            public void onEvent(Boolean internet, int kb) {
                Log.i("INET STAT", String.valueOf(internet));
                Log.i("INET KB", String.valueOf(kb));

                if(internet){
                    //visitTokoServer();
                    visitTokoLokal();
                } else {
                    visitTokoLokal();
                }
            }
        });
        networkQuality.doEvent(VisitTokoActivity.this);
    }

    public void visitTokoServer(){
        AsyncJob.doInBackground(new AsyncJob.OnBackgroundJob() {
            @Override
            public void doOnBackground() {
                MultipartBody.Builder mBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);

                //siapkan data visit toko
                mBuilder.addFormDataPart("kode_asset", kodeAsset);
                mBuilder.addFormDataPart("username", userPref.getString("username", ""));
                mBuilder.addFormDataPart("id_order", idOrder);
                mBuilder.addFormDataPart("id_foto", idFoto);
                mBuilder.addFormDataPart("lat", String.valueOf(latitude));
                mBuilder.addFormDataPart("lng", String.valueOf(longitude));
                mBuilder.addFormDataPart("toko_tutup", String.valueOf(tutup));

                //siapkan data order untuk upload (sales)
                if (levelUser.equals("sales")) {
                    for (int i = 0; i < produk.size(); i++) {
                        mBuilder.addFormDataPart("nama_produk[]", produk.get(i));
                        mBuilder.addFormDataPart("qty_produk[]", quantity.get(i));
                    }
                }

                //siapkan data catatan (delivery)
                if (levelUser.equals("delivery")) {
                    mBuilder.addFormDataPart("catatan", catatan);
                }

                //siapkan foto untuk upload
                for (int i = 0; i < images.size(); i++) {
                    File img = new File(images.get(i));
                    MediaType MEDIA_TYPE = MediaType.parse("image/jpeg");
                    mBuilder.addFormDataPart("foto[]", img.getName(), RequestBody.create(MEDIA_TYPE, img));
                }

                RequestBody requestBody = mBuilder
                        .setType(MultipartBody.FORM)
                        .build();

                final Request request = new Request.Builder()
                        .url(GlobalApiAddress.getDomain() + "/api/api.post.php?target=visit_toko")
                        .post(requestBody)
                        .build();

                OkHttpClient client = new OkHttpClient.Builder().build();
                Call call = client.newCall(request);

                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        visitTokoLokal();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        AsyncJob.doOnMainThread(new AsyncJob.OnMainThreadJob() {
                            @Override
                            public void doInUIThread() {
                                loader.dismiss();
                                Toast.makeText(VisitTokoActivity.this, "Data terkirim dan akan divalidasi di server", Toast.LENGTH_LONG).show();

                                finish();
                            }
                        });
                    }
                });

            }
        });
    }

    public void visitTokoLokal(){
        AsyncJob.doInBackground(new AsyncJob.OnBackgroundJob() {
            @Override
            public void doOnBackground() {
                DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                formatter.setTimeZone(TimeZone.getTimeZone("GMT+8"));
                String cTime = formatter.format(new Date(System.currentTimeMillis()));
                realm = Realm.getDefaultInstance();

                try {
                    realm.beginTransaction();
                    TbVisitToko tbVisitToko = realm.createObject(TbVisitToko.class);
                    tbVisitToko.setKode_asset(kodeAsset);
                    tbVisitToko.setUsername(userPref.getString("username", ""));
                    tbVisitToko.setId_order(idOrder);
                    tbVisitToko.setId_foto(idFoto);
                    tbVisitToko.setLat(latitude);
                    tbVisitToko.setLng(longitude);
                    tbVisitToko.setTutup(tutup);
                    tbVisitToko.setStatus("PENDING");
                    tbVisitToko.setTgl_add(cTime);
                    realm.commitTransaction();

                    //tb produk order
                    if (levelUser.equals("sales")) {
                        for (int i = 0; i < produk.size(); i++) {
                            realm.beginTransaction();
                            TbOrderProduk tbOrderProduk = realm.createObject(TbOrderProduk.class);
                            tbOrderProduk.setId_order(idOrder);
                            tbOrderProduk.setUsername(userPref.getString("username", ""));
                            tbOrderProduk.setNama_produk(produk.get(i));
                            tbOrderProduk.setQty_produk(Integer.valueOf(quantity.get(i)));
                            tbOrderProduk.setStatus("PENDING");
                            tbOrderProduk.setTgl_add(cTime);
                            realm.commitTransaction();
                        }
                    }

                    //tb catatan deliv
                    if (levelUser.equals("delivery")) {
                        realm.beginTransaction();
                        TbCatatan tbCatatan = realm.createObject(TbCatatan.class);
                        tbCatatan.setId_order(idOrder);
                        tbCatatan.setUsername(userPref.getString("username", ""));
                        tbCatatan.setCatatan(catatan);
                        tbCatatan.setStatus("PENDING");
                        tbCatatan.setTgl_add(cTime);
                        realm.commitTransaction();
                    }

                    //tb foto
                    for (int i = 0; i < images.size(); i++) {
                        realm.beginTransaction();
                        TbFoto tbFoto = realm.createObject(TbFoto.class);
                        tbFoto.setId_foto(idFoto);
                        tbFoto.setNama_file(images.get(i));
                        tbFoto.setStatus("PENDING");
                        realm.commitTransaction();
                    }

                    AsyncJob.doOnMainThread(new AsyncJob.OnMainThreadJob() {
                        @Override
                        public void doInUIThread() {
                            loader.dismiss();

                            Realm queRealm = Realm.getDefaultInstance();
                            RealmResults<TbVisitToko> queVisitToko = queRealm.where(TbVisitToko.class)
                                    .equalTo("status", "PENDING")
                                    .findAll();
                            int que = queVisitToko.size();
                            Toast.makeText(VisitTokoActivity.this, "Visit toko ["+kodeAsset+"] berada pada antrian ke: "+que, Toast.LENGTH_LONG).show();
                            queRealm.close();

                            finish();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    realm.close();
                }
            }
        });
    }

    private float getDistancBetweenTwoPoints(double latFrom,double lonFrom,double latTo,double lonTo) {

        float[] distance = new float[2];

        Location.distanceBetween( latFrom, lonFrom,
                latTo, lonTo, distance);

        return distance[0];
    }

    public void resetForm(){
        tutup = false;
        visitKodeAsset.setText("");
        visitLokasi.setText("");
        visitFoto.setText("");
        visitProduk.setText("");
        images = new ArrayList<>();
        produk = new ArrayList<>();
        quantity = new ArrayList<>();

        Snacky.builder()
                .setActivity(VisitTokoActivity.this)
                .setActionText("Oke")
                .setText("Form berhasil direset")
                .setDuration(Snacky.LENGTH_SHORT)
                .build()
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode == Activity.RESULT_OK) switch (requestCode) {
            case 1:
                kodeAsset = data.getStringExtra("kode_asset");
                latitude = data.getDoubleExtra("lat", 0.0);
                longitude = data.getDoubleExtra("lng", 0.0);

                visitKodeAsset.setText(kodeAsset);
                visitLokasi.setText(String.valueOf(latitude) + " / " + String.valueOf(longitude));
                break;

            case 3:
                images = data.getStringArrayListExtra("images");
                visitFoto.setText(images.size() + " Foto");
                break;

            case 4:
                //terima data orderan
                produk = data.getStringArrayListExtra("produk");
                quantity = data.getStringArrayListExtra("quantity");
                tutup = data.getBooleanExtra("status_toko", false);
                int sumQty = 0;
                for (String q : quantity) {
                    sumQty += Integer.valueOf(q);
                }
                visitProduk.setText(produk.size() + " Jenis Item " + sumQty + " Total");
                break;

            case 5:
                //terima data catatan
                catatan = data.getStringExtra("catatan");
                visitProduk.setText("1 Catatan");
                break;

            case 6:
                //terima data toko tutup
                kodeAsset = data.getStringExtra("kode_asset");
                latitude = data.getDoubleExtra("lat", 0.0);
                longitude = data.getDoubleExtra("lng", 0.0);
                tutup = data.getBooleanExtra("status_toko", true);

                //hapus foto, dan ambil ulang foto 1x
                if (images.size() > 0) {
                    images = new ArrayList<>();
                    visitFoto.setText("");
                    //Toast.makeText(VisitTokoActivity.this, "Foto dihapus, ambil ulang foto", Toast.LENGTH_SHORT).show();
                }

                //hapus produk jika ada
                if (produk.size() > 0 || quantity.size() > 0) {
                    produk = new ArrayList<>();
                    quantity = new ArrayList<>();
                    visitProduk.setText("");
                }

                    /*
                    Snacky.builder()
                            .setActivity(VisitTokoActivity.this)
                            .setActionText("Oke")
                            .setText("Toko tutup")
                            .setDuration(Snacky.LENGTH_LONG)
                            .build()
                            .show();
                            */

                visitKodeAsset.setText(kodeAsset);
                visitLokasi.setText(String.valueOf(latitude) + " / " + String.valueOf(longitude));
                break;

            case 7:
                count++;

                    /*
                    try {
                        ExifInterface e = new ExifInterface(images.get(0));

                        Log.i("DEBUGGGG EXIF", String.valueOf(e.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    */

                if (!tutup) {
                    //limit foto sales 3, deliv 2
                    picLimit = (levelUser.equals("sales")) ? 3 : 2;
                } else {
                    //jika toko tutup, limit foto 1
                    picLimit = 1;
                }

                if (images.size() != picLimit) {
                    startCamera();
                } else {
                    loader.show();
                    AsyncJob.doInBackground(new AsyncJob.OnBackgroundJob() {
                        @Override
                        public void doOnBackground() {
                            int rotation = 0;
                            for (String f : images) {
                                File sourceFile = new File(f);
                                try {

                                    ExifInterface exifInterface = new ExifInterface(f);
                                    int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

                                    if(orientation == ExifInterface.ORIENTATION_ROTATE_90){
                                        Bitmap bitmap = BitmapFactory.decodeFile(f);
                                        Matrix matrix = new Matrix();
                                        matrix.setRotate(90);
                                        Bitmap result = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);

                                        OutputStream out = new FileOutputStream(sourceFile);
                                        result.compress(Bitmap.CompressFormat.JPEG, 100, out);
                                    }

                                    new Resizer(VisitTokoActivity.this)
                                            .setTargetLength(800)
                                            .setSourceImage(sourceFile)
                                            .getResizedFile();

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            AsyncJob.doOnMainThread(new AsyncJob.OnMainThreadJob() {
                                @Override
                                public void doInUIThread() {
                                    loader.dismiss();
                                    visitFoto.setText(images.size() + " Foto");
                                }
                            });
                        }
                    });
                }

                break;

            case 8:
                images = data.getStringArrayListExtra("images");
                visitFoto.setText(images.size() + " Foto");
                break;
        }
    }
}
