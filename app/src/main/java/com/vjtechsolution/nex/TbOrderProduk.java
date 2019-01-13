package com.vjtechsolution.nex;

import io.realm.RealmObject;

/**
 * Created by Aryo on 3/22/2018.
 */

public class TbOrderProduk extends RealmObject {

    String username,nama_produk,id_order,status,tgl_add;
    int qty_produk;

    public String getTgl_add() {
        return tgl_add;
    }

    public void setTgl_add(String tgl_add) {
        this.tgl_add = tgl_add;
    }

    public String getUsername() {
        return username;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getId_order() {
        return id_order;
    }

    public void setId_order(String id_order) {
        this.id_order = id_order;
    }

    public String getNama_produk() {
        return nama_produk;
    }

    public void setNama_produk(String nama_produk) {
        this.nama_produk = nama_produk;
    }

    public int getQty_produk() {
        return qty_produk;
    }

    public void setQty_produk(int qty_produk) {
        this.qty_produk = qty_produk;
    }
}
