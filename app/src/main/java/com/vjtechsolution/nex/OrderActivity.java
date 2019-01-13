package com.vjtechsolution.nex;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import de.mateware.snacky.Snacky;

public class OrderActivity extends AppCompatActivity implements View.OnClickListener {

    private LinkedHashMap<String, EditText> linkedHashMapProduk = new LinkedHashMap<>();
    private LinkedHashMap<String, EditText> linkedHashMapQty = new LinkedHashMap<>();
    private ArrayList<ImageView> btnHapusProduk = new ArrayList<>();

    private ArrayList<String> produk = new ArrayList<>();
    private ArrayList<String> quantity = new ArrayList<>();

    private LinearLayout itemContainer;
    private Button itemBtn;
    private Button simpanBtn;

    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setBackground(getResources().getDrawable(R.color.colorPrimary));
        setSupportActionBar(toolbar);
        this.setTitle("Buat Order");

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        itemContainer = findViewById(R.id.produkItemContainer);
        itemBtn = findViewById(R.id.btnAddProduk);
        simpanBtn = findViewById(R.id.btnSimpanProduk);

        itemBtn.setOnClickListener(this);
        simpanBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btnAddProduk:
                count++;

                EditText item = new EditText(OrderActivity.this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(100, 0,30,20);
                params.weight = 7;
                params.gravity = Gravity.LEFT;
                item.setLayoutParams(params);

                item.setBackgroundResource(R.drawable.round_form);
                item.setInputType(InputType.TYPE_CLASS_TEXT);
                item.setGravity(Gravity.LEFT);
                item.setMinHeight(40);
                item.setPadding(40,20,40,20);
                item.setTextColor(getResources().getColor(R.color.textSecondary));
                item.setHintTextColor(getResources().getColor(R.color.colorAccent));
                item.setTextSize(18);
                item.setHint("Jenis Produk");

                EditText qty = new EditText(OrderActivity.this);
                LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
                params2.setMargins(0, 0,40,20);
                params2.weight = 2;
                params2.gravity = Gravity.RIGHT;
                qty.setLayoutParams(params2);
                qty.setBackgroundResource(R.drawable.round_form);
                qty.setInputType(InputType.TYPE_CLASS_NUMBER);
                qty.setGravity(Gravity.CENTER);
                qty.setMinHeight(40);
                qty.setPadding(40,20,40,20);
                qty.setTextColor(getResources().getColor(R.color.textSecondary));
                qty.setHintTextColor(getResources().getColor(R.color.colorAccent));
                qty.setTextSize(18);
                qty.setHint("Qty");

                ImageView btnHapusOrder = new ImageView(OrderActivity.this);
                LinearLayout.LayoutParams params4 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
                params4.setMargins(0,0,100,20);
                params4.weight = 1;
                params4.gravity = Gravity.RIGHT;
                btnHapusOrder.setLayoutParams(params4);
                btnHapusOrder.setPadding(0,15,0,15);
                btnHapusOrder.setImageResource(R.drawable.ic_delete_black_24dp);
                btnHapusOrder.setTag(count);

                LinearLayout linearLayout = new LinearLayout(OrderActivity.this);
                LinearLayout.LayoutParams params3 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                linearLayout.setLayoutParams(params3);
                linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                linearLayout.setWeightSum(10);

                //simpan Edit text produk dan quantity pada map untuk mengambil datanya nanti
                linkedHashMapProduk.put("produk_"+count, item);
                linkedHashMapQty.put("qty_"+count, qty);
                btnHapusProduk.add(btnHapusOrder);

                linearLayout.addView(item);
                linearLayout.addView(qty);
                linearLayout.addView(btnHapusOrder);

                itemContainer.addView(linearLayout);

                if(btnHapusProduk.size() > 0){
                    for(ImageView b:btnHapusProduk){
                        b.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //count--;
                                LinearLayout parent = (LinearLayout) v.getParent().getParent();
                                LinearLayout child = (LinearLayout) v.getParent();
                                String p = "produk_"+v.getTag();
                                String q = "qty_"+v.getTag();
                                parent.removeView(child);

                                linkedHashMapProduk.remove(p);
                                linkedHashMapQty.remove(q);
                            }
                        });
                    }
                }

                break;

            case R.id.btnSimpanProduk:
                if(linkedHashMapProduk.size() > 0 || linkedHashMapQty.size() > 0) {
                    //simpan data produk ke array list
                    for(Map.Entry<String, EditText> entry : linkedHashMapProduk.entrySet()){
                        EditText pr = entry.getValue();

                        if(!pr.getText().toString().equals("")) {
                            produk.add(pr.getText().toString());
                        }
                    }
                    //simpan data qty produk ke array list
                    for(Map.Entry<String, EditText> entry : linkedHashMapQty.entrySet()){
                        EditText qt = entry.getValue();

                        if(!qt.getText().toString().equals("")) {
                            quantity.add(qt.getText().toString());
                        }
                    }

                    if(produk.size() > 0 || quantity.size() > 0){
                        Intent visitToko = new Intent(OrderActivity.this, VisitTokoActivity.class);
                        visitToko.putStringArrayListExtra("produk", produk);
                        visitToko.putStringArrayListExtra("quantity", quantity);
                        visitToko.putExtra("status_toko", false);
                        setResult(Activity.RESULT_OK, visitToko);
                        finish();
                    }else{
                        Snacky.builder()
                                .setActivity(OrderActivity.this)
                                .setActionText("Oke")
                                .setText("Detail order tidak boleh kosong")
                                .setDuration(Snacky.LENGTH_LONG)
                                .build()
                                .show();
                    }

                }else{
                    //Toast.makeText(this, "Detail order tidak boleh kosong", Toast.LENGTH_SHORT).show();

                    Snacky.builder()
                            .setActivity(OrderActivity.this)
                            .setActionText("Oke")
                            .setText("Detail order tidak boleh kosong")
                            .setDuration(Snacky.LENGTH_LONG)
                            .build()
                            .show();
                }
                break;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
