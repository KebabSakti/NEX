package com.vjtechsolution.nex;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ListAdapterDataOffline extends BaseAdapter {
    ArrayList<ListDataOffline> listDataOfflines;
    LayoutInflater layoutInflater;

    public ListAdapterDataOffline(Context context, ArrayList<ListDataOffline> listDataOfflines) {
        this.listDataOfflines = listDataOfflines;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return listDataOfflines.size();
    }

    @Override
    public Object getItem(int position) {
        return listDataOfflines.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ListAdapterDataOffline.ViewHolder holder;
        if(convertView == null){
            convertView = layoutInflater.inflate(R.layout.list_item_offline_container, null);
            holder = new ListAdapterDataOffline.ViewHolder();
            holder.kode_asset = convertView.findViewById(R.id.off_kode_asset);
            holder.tgl_add = convertView.findViewById(R.id.off_tgl_add);
            holder.status = convertView.findViewById(R.id.off_status);
            convertView.setTag(holder);
        }else{
            holder = (ListAdapterDataOffline.ViewHolder) convertView.getTag();
        }

        holder.kode_asset.setText(listDataOfflines.get(position).getKode_asset());
        holder.tgl_add.setText(listDataOfflines.get(position).getTgl_add());
        holder.status.setText(listDataOfflines.get(position).getStatus());

        return convertView;
    }

    static class ViewHolder {
        TextView kode_asset,tgl_add,status;
    }
}
