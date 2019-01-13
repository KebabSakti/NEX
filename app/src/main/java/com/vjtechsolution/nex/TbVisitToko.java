package com.vjtechsolution.nex;

import io.realm.RealmObject;

/**
 * Created by Aryo on 3/21/2018.
 */

public class TbVisitToko extends RealmObject {
    String kode_asset,username,id_order,id_foto, status, tgl_add;

    public String getTgl_add() {
        return tgl_add;
    }

    public void setTgl_add(String tgl_add) {
        this.tgl_add = tgl_add;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    Double lat,lng;
    Boolean tutup;

    public Boolean getTutup() {
        return tutup;
    }

    public void setTutup(Boolean tutup) {
        this.tutup = tutup;
    }

    public String getKode_asset() {
        return kode_asset;
    }

    public void setKode_asset(String kode_asset) {
        this.kode_asset = kode_asset;
    }

    public String getUsername() {
        return username;
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

    public String getId_foto() {
        return id_foto;
    }

    public void setId_foto(String id_foto) {
        this.id_foto = id_foto;
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
