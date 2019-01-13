package com.vjtechsolution.nex;

import io.realm.RealmObject;

/**
 * Created by Aryo on 3/22/2018.
 */

public class TbFoto extends RealmObject {
    String nama_file, id_foto, status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getId_foto() {
        return id_foto;
    }

    public void setId_foto(String id_foto) {
        this.id_foto = id_foto;
    }

    public String getNama_file() {
        return nama_file;
    }

    public void setNama_file(String nama_file) {
        this.nama_file = nama_file;
    }
}
