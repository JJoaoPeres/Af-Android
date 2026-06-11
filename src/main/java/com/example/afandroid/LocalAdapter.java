package com.example.afandroid;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class LocalAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<LocalApi> listaLocais;

    public LocalAdapter(Context context, ArrayList<LocalApi> listaLocais) {
        this.context = context;
        this.listaLocais = listaLocais;
    }

    @Override
    public int getCount() {
        return listaLocais.size();
    }

    @Override
    public Object getItem(int position) {
        return listaLocais.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        TextView textView;

        if (convertView == null) {
            textView = new TextView(context);
            textView.setTextSize(15);
            textView.setPadding(20, 20, 20, 20);
        } else {
            textView = (TextView) convertView;
        }

        LocalApi local = listaLocais.get(position);

        String texto = local.getNome() +
                "\nCategoria: " + local.getTipo() +
                "\nDistância: " + String.format("%.0f", local.getDistancia()) + " metros" +
                "\nLatitude: " + local.getLatitude() +
                "\nLongitude: " + local.getLongitude();

        textView.setText(texto);

        return textView;
    }
}