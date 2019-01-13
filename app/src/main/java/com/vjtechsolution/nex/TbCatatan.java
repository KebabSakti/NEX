package com.vjtechsolution.nex;

import io.realm.RealmObject;

/**
 * Created by Aryo on 3/22/2018.
 */

public class TbCatatan extends RealmObject {
    String username;
    String catatan;
    String id_order;
    String status;
    String tgl_add;

    public String getStatus() {
        return status;
    }

    public String getTgl_add() {
        return tgl_add;
    }

    public void setTgl_add(String tgl_add) {
        this.tgl_add = tgl_add;
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

    public String getCatatan() {
        return catatan;
    }

    public String getId_order() {
        return id_order;
    }

    public void setId_order(String id_order) {
        this.id_order = id_order;
    }

    public void setCatatan(String catatan) {
        this.catatan = catatan;
    }
}
