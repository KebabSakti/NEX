package com.vjtechsolution.nex;

/**
 * Created by Aryo on 3/23/2018.
 */

public class ListDataPesan {
    String id_pesan,id_distributor,sender,subjek,deskripsi,tgl_kirim;

    public String getId_pesan() {
        return id_pesan;
    }

    public void setId_pesan(String id_pesan) {
        this.id_pesan = id_pesan;
    }

    public String getId_distributor() {
        return id_distributor;
    }

    public void setId_distributor(String id_distributor) {
        this.id_distributor = id_distributor;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSubjek() {
        return subjek;
    }

    public void setSubjek(String subjek) {
        this.subjek = subjek;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public String getTgl_kirim() {
        return tgl_kirim;
    }

    public void setTgl_kirim(String tgl_kirim) {
        this.tgl_kirim = tgl_kirim;
    }
}
