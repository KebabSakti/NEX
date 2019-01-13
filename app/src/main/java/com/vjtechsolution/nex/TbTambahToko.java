package com.vjtechsolution.nex;

import io.realm.RealmObject;

/**
 * Created by Aryo on 3/19/2018.
 */

public class TbTambahToko extends RealmObject {

    String username,kode_asset,nama_toko,status,waktu_add, tgl_add;
    Double lat,lng;

    public String getTgl_add() {
        return tgl_add;
    }

    public void setTgl_add(String tgl_add) {
        this.tgl_add = tgl_add;
    }

    public String getStatus() {
        return status;
    }

    public String getWaktu_add() {
        return waktu_add;
    }

    public void setWaktu_add(String waktu_add) {
        this.waktu_add = waktu_add;
    }

    public void setStatus(String status) {
        this.status = status;

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
