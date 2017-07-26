package com.example.hajir.morgenlandqr;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
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

public class ScanIn_a extends AppCompatActivity {
    private Button scan_in_btn;
    private Button scan_in_toscanout_btn;
    private Button scan_in_tohome_btn;
    private ListView scan_in_listview;
    private TextView scan_in_anzahl;

    ArrayAdapter<String> listAdapter;
    ArrayList<String> arrayList;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_in_a);
        //Init Views
        scan_in_btn = (Button) findViewById(R.id.scan_in_btn);
        scan_in_toscanout_btn = (Button) findViewById(R.id.scan_in_toscan_out_btn);
        scan_in_tohome_btn = (Button) findViewById(R.id.scan_in_tohome_btn);
        scan_in_anzahl = (TextView) findViewById(R.id.scan_in_anzahl);
        //**ListView
        scan_in_listview = (ListView) findViewById(R.id.scan_in_listview);
        arrayList = new ArrayList<>();
        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayList);
        scan_in_listview.setAdapter(listAdapter);
        scan_in_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(ScanIn_a.this, "ItemClick", Toast.LENGTH_SHORT).show();
            }
        });

        //*START DB Connection
        db = new DbHelper(this).getWritableDatabase();

        //OnClickListeners
        scan_in_toscanout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent
                Intent scanOutIntent = new Intent(ScanIn_a.this, ScanOut_a.class);
                startActivity(scanOutIntent);
            }
        });
        scan_in_tohome_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent
                Intent scanOutIntent = new Intent(ScanIn_a.this, Home_A.class);
                startActivity(scanOutIntent);
            }
        });

        final Activity activity = this;
        scan_in_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator intentIntegrator = new IntentIntegrator(activity);
                intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                intentIntegrator.setPrompt("Scan IN");
                intentIntegrator.setCameraId(0);
                intentIntegrator.setBeepEnabled(true);
                intentIntegrator.setBarcodeImageEnabled(false);
                intentIntegrator.initiateScan();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Du hast das Scannen abgebrochen", Toast.LENGTH_SHORT).show();
            }else {
                final String qrText = result.getContents();
                String [] columns = {"qrText"}; //check if it exists in table
                Cursor cursor = db.query(DbHelper.TABLE_QRText, columns, DbHelper.COLUMN_QRText+" = '"+qrText+"'",
                        null,null,null,null);
                if (cursor.moveToFirst()){ //false: if cursor is emtpy
                    Toast.makeText(ScanIn_a.this, "In DB vorhanden: "+cursor.getString(0), Toast.LENGTH_SHORT).show();
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(DbHelper.COLUMN_SCANOUT, "-");
                    contentValues.put(DbHelper.COLUMN_SCANIN, "scanned-in");
                    int rowsAffected = db.update(DbHelper.TABLE_QRText,contentValues,DbHelper.COLUMN_QRText+" = '"+qrText+"'",null);
                    Toast.makeText(ScanIn_a.this, "Items in DB markiert. Markierte Zeilen:  "+rowsAffected, Toast.LENGTH_SHORT).show();

                    arrayList.add("markiert:");
                    arrayList.add(qrText);
                    listAdapter.notifyDataSetChanged();
                }else {
                    Toast.makeText(ScanIn_a.this, "Achtung, dieses Item ist nicht in der Datenbank vorhanden", Toast.LENGTH_SHORT).show();
                    arrayList.add("Scan IN ungültig:");
                    arrayList.add(result.getContents());
                    listAdapter.notifyDataSetChanged();
                }


                scan_in_anzahl.setText("Scan Anzahl: "+(arrayList.size()/2));
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
