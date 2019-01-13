package com.vjtechsolution.nex;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Aryo on 3/22/2018.
 */

public class ListAdapterDataToko extends BaseAdapter {

    ArrayList<ListDataToko> listDataToko;
    LayoutInflater layoutInflater;

    public ListAdapterDataToko(Context context, ArrayList<ListDataToko> listDataToko) {
        this.listDataToko = listDataToko;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return listDataToko.size();
    }

    @Override
    public Object getItem(int position) {
        return listDataToko.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null){
            convertView = layoutInflater.inflate(R.layout.list_data_toko, null);
            holder = new ViewHolder();
            holder.kode_asset = convertView.findViewById(R.id.listDataKodeAsset);
            holder.nama_toko = convertView.findViewById(R.id.listDataNamaToko);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        holder.kode_asset.setText(listDataToko.get(position).getKode_asset());
        holder.nama_toko.setText(listDataToko.get(position).getNama_toko());

        return convertView;
    }

    static class ViewHolder {
        TextView kode_asset,nama_toko;
    }
}
