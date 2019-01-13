package com.vjtechsolution.nex;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ListAdapterRiwayatOrder extends BaseAdapter {

    ArrayList<ListRiwayatOrder> listRiwayatOrder;
    LayoutInflater layoutInflater;

    public ListAdapterRiwayatOrder(Context context, ArrayList<ListRiwayatOrder> listRiwayatOrder) {
        this.listRiwayatOrder = listRiwayatOrder;
        layoutInflater = layoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return listRiwayatOrder.size();
    }

    @Override
    public Object getItem(int position) {
        return listRiwayatOrder.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ListAdapterRiwayatOrder.ViewHolder holder;
        if(convertView == null){
            convertView = layoutInflater.inflate(R.layout.list_riwayat_order, null);
            holder = new ListAdapterRiwayatOrder.ViewHolder();
            holder.namaToko = convertView.findViewById(R.id.ro_nama_toko);
            holder.tglOrder = convertView.findViewById(R.id.ro_tgl_order);
            convertView.setTag(holder);
        } else {
            holder = (ListAdapterRiwayatOrder.ViewHolder) convertView.getTag();
        }

        holder.namaToko.setText(listRiwayatOrder.get(position).getNama_toko());
        holder.tglOrder.setText(listRiwayatOrder.get(position).getTgl_order());

        return convertView;
    }

    static class ViewHolder {
        TextView namaToko, tglOrder;
    }
}
