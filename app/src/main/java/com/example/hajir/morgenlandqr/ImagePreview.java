package com.example.hajir.morgenlandqr;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;

import uk.co.senab.photoview.PhotoViewAttacher;

public class ImagePreview extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_preview);

        ImageView imagePreview = (ImageView) findViewById(R.id.imagePreview);
        Intent passedToUs = getIntent();
        String qrText = passedToUs.getStringExtra("qrText");
        //explode -> find imagesrc -> put into imagePreview
        String[] strArray = qrText.split("_");
        File imgFile = new File("/storage/emulated/0/Pictures/MorgnlandQR/"+strArray[0]+"_"+strArray[2]+"-big.jpg");
        if(imgFile.exists()){
            imagePreview.setImageBitmap(BitmapFactory.decodeFile("/storage/emulated/0/Pictures/MorgnlandQR/"+strArray[0]+"_"+strArray[2]+"-big.jpg"));
        }else {
            //set placeholder image src
            imagePreview.setImageResource(R.drawable.not_found);
        }
        //**ZOOM
        PhotoViewAttacher photoViewAttacher = new PhotoViewAttacher(imagePreview);
        photoViewAttacher.update();
        //**

        Button back = (Button) findViewById(R.id.buttonBack);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent home = new Intent(ImagePreview.this, Home_A.class);

                startActivity(home);
            }
        });


    }
}
