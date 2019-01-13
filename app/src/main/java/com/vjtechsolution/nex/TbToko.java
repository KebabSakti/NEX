package com.vjtechsolution.nex;

import io.realm.RealmObject;

public class TbToko extends RealmObject {
    int id;
    String username,kode_asset,nama_toko,tgl_add;
    Double lat,lng;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getKode_asset() {
        return kode_asset;
    }

    public void setKode_asset(String kode_asset) {
        this.kode_asset = kode_asset;
    }

    public String getNama_toko() {
        return nama_toko;
    }

    public void setNama_toko(String nama_toko) {
        this.nama_toko = nama_toko;
    }

    public String getTgl_add() {
        return tgl_add;
    }

    public void setTgl_add(String tgl_add) {
        this.tgl_add = tgl_add;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }
}
