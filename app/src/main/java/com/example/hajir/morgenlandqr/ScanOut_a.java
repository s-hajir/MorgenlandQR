package com.example.hajir.morgenlandqr;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class ScanOut_a extends AppCompatActivity {
    private Button scan_out_btn;
    private Button scan_out_toscanin_btn;
    private Button scan_out_tohome_btn;
    private ListView scan_out_listview;
    private TextView scan_out_anzahl;

    ArrayAdapter<String> listAdapter;
    ArrayList<String> arrayList;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_out_a);
        scan_out_btn = (Button) findViewById(R.id.scan_out_btn);
        scan_out_toscanin_btn = (Button) findViewById(R.id.scan_out_toscan_in_btn);
        scan_out_tohome_btn = (Button) findViewById(R.id.scan_out_tohome_btn);
        scan_out_anzahl = (TextView) findViewById(R.id.scan_out_anzahl);

        //**ListView
        scan_out_listview = (ListView) findViewById(R.id.scan_out_listview);
        arrayList = new ArrayList<>();
        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayList);
        scan_out_listview.setAdapter(listAdapter);
        //*START DB Connection
        db = new DbHelper(this).getWritableDatabase();

        //OnClickListeners
        scan_out_toscanin_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent
                Intent scanInIntent = new Intent(ScanOut_a.this, ScanIn_a.class);
                startActivity(scanInIntent);
            }
        });

        scan_out_tohome_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent
                Intent scanInIntent = new Intent(ScanOut_a.this, Home_A.class);
                startActivity(scanInIntent);
            }
        });

        final Activity activity = this;
        scan_out_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator intentIntegrator = new IntentIntegrator(activity);
                intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                intentIntegrator.setPrompt("ScanOUT");
                intentIntegrator.setCameraId(0);
                intentIntegrator.setBeepEnabled(true);
                intentIntegrator.setBarcodeImageEnabled(false);
                intentIntegrator.initiateScan();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null){
            if (result.getContents() == null){
                Toast.makeText(this, "Du hast das Scannen abgebrochen", Toast.LENGTH_SHORT).show();
            }else {
                final String qrText = result.getContents();
                //BEI jedem ScanOUT -> prüfen ob qrText in Tabelle existiert -> if yes: löschen
                //                                                        ->if no: Nutzer informieren
                String [] columns = {"qrText"};
                //**SELECT qrText FROM QRText WHERE qrText = 'someText'
                Cursor cursor = db.query(DbHelper.TABLE_QRText, columns, DbHelper.COLUMN_QRText+" = '"+qrText+"'",
                        null,null,null,null);

                if (cursor.moveToFirst()){ //false: if cursor is emtpy. true: if cursor not empty & operation is a succes
                    Toast.makeText(ScanOut_a.this, "In DB vorhanden: "+cursor.getString(0), Toast.LENGTH_SHORT).show();
                    int rowsDeleted = db.delete(DbHelper.TABLE_QRText,DbHelper.COLUMN_QRText+" = '"+qrText+"'",null);
                    Toast.makeText(ScanOut_a.this, "Anzahl Items aus DB gelöscht:  "+rowsDeleted, Toast.LENGTH_SHORT).show();
                    arrayList.add("gelöscht:");
                    arrayList.add(qrText); //1.Add Item To Array
                    listAdapter.notifyDataSetChanged(); //2.Notify Adapter( update ListView )
                }else {
                    Toast.makeText(ScanOut_a.this, "Fehler, dieses Item ist nicht in der Datenbank vorhanden", Toast.LENGTH_SHORT).show();
                }
                scan_out_anzahl.setText("Scan Anzahl: "+arrayList.size());
                cursor.close();
            }
        }else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }
}
