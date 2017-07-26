package com.example.hajir.morgenlandqr;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.List;

/**
 * Created by hajir on 06.06.2017.
 */

public class MyListAdapter extends ArrayAdapter<Teppich>{
    public Context context;
    public List<Teppich> teppiche;

    public MyListAdapter(Context context, List<Teppich> teppiche) {
        super(context, R.layout.item_scrollview_parent, teppiche);
        this.context = context;
        this.teppiche = teppiche;
    }

    @NonNull
    @Override               //wird für jedes Element von 'List<Teppiche> teppiche' aufgerufen -> füllt einzelne ROWs
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.item_scrollview_parent, parent, false);

        String qrText = teppiche.get(position).getQrString();
        String[] strArray = qrText.split("_");

        //FILL Row
        ((TextView) view.findViewById(R.id.textViewUidSku)).setText(strArray[0]+"_"+strArray[2]);
        //**********2.set image src = strArray[0]+"_"+strArray[1]+".jpg"
        ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
        File imgFile = new File("/storage/emulated/0/Pictures/MorgnlandQR/"+strArray[0]+"_"+strArray[2]+"-small.jpg"); //braucht permission read/write external storage
        if(imgFile.exists()){
            imageView.setImageBitmap(BitmapFactory.decodeFile("/storage/emulated/0/Pictures/MorgnlandQR/"+strArray[0]+"_"+strArray[2]+"-small.jpg"));
        }else {
            //set placeholder image src
            imageView.setImageResource(R.drawable.not_found);
        }



        ((TextView) view.findViewById(R.id.gewicht_preis)).setText(strArray[3]+", "+strArray[4]);
        ((TextView) view.findViewById(R.id.herkunft_alter)).setText(strArray[5]+", "+strArray[6]);
        ((TextView) view.findViewById(R.id.knoten)).setText(strArray[7]);
        ((TextView) view.findViewById(R.id.kette_flor_fhoehe)).setText(strArray[8]+", "+strArray[9]+", "+strArray[10]+"mm");
        ((TextView) view.findViewById(R.id.verarbeitung)).setText(strArray[11]);
        ((TextView) view.findViewById(R.id.zustand_auf_lager)).setText(strArray[12]);
        return view;

    }

}
