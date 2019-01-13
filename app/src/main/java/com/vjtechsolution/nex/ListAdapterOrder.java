package com.vjtechsolution.nex;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ListAdapterOrder extends BaseAdapter {

    private ArrayList<ListDetailOrder> listDetailOrder;
    private LayoutInflater layoutInflater;

    public ListAdapterOrder(Context context, ArrayList<ListDetailOrder> listDetailOrder) {
        this.listDetailOrder = listDetailOrder;
        layoutInflater = layoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return listDetailOrder.size();
    }

    @Override
    public Object getItem(int position) {
        return listDetailOrder.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ListAdapterOrder.ViewHolder holder;
        if(convertView == null){
            convertView = layoutInflater.inflate(R.layout.list_detail_order, null);
            holder = new ListAdapterOrder.ViewHolder();
            holder.nama_produk = convertView.findViewById(R.id.produk);
            holder.qty_produk = convertView.findViewById(R.id.quantity);
            convertView.setTag(holder);
        }else{
            holder = (ListAdapterOrder.ViewHolder) convertView.getTag();
        }

        holder.nama_produk.setText(listDetailOrder.get(position).getNama_produk());
        holder.qty_produk.setText(String.valueOf(listDetailOrder.get(position).getQty_produk()));

        return convertView;
    }

    static class ViewHolder {
        TextView nama_produk, qty_produk;
    }
}
