package com.example.hajir.morgenlandqr;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Debug;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.sql.SQLData;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MorgenlandQR extends AppCompatActivity {
    private Button scan_btn;
    private Button sync_btn;
    private Button showTable_btn;
    private Button clear_table_btn;
    private TextView scan_anzahl;
    ArrayAdapter<String> listAdapter;
    ArrayList<String> arrayList;
    ListView listView;
    SQLiteDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_morgenland_qr);
        scan_anzahl = (TextView) findViewById(R.id.scan_anzahl);
        //*START DB Connection
        db = new DbHelper(this).getWritableDatabase();
        //*END
        //*START Scan Button
        scan_btn = (Button) findViewById(R.id.scan_btn);
        final Activity activity = this;
        scan_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator intentIntegrator = new IntentIntegrator(activity);
                intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                intentIntegrator.setPrompt("Scan");
                intentIntegrator.setCameraId(0);
                intentIntegrator.setBeepEnabled(false);
                intentIntegrator.setBarcodeImageEnabled(false);
                intentIntegrator.initiateScan();
            }
        });
        //*END
        //*START sync NUEUER INTENT !!!!!
        sync_btn = (Button) findViewById(R.id.sync_btn);
        sync_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        //*START Show Table Button
        showTable_btn = (Button) findViewById(R.id.showDB_btn);
        showTable_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent homeInt = new Intent(MorgenlandQR.this, Home_A.class);
                startActivity(homeInt);
            }
        });
        //*END
        //*START Clear Table Button
        clear_table_btn = (Button) findViewById(R.id.clear_table_btn);
        clear_table_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.delete(DbHelper.TABLE_QRText,null,null); //clear Table
                arrayList.clear(); //clear ListView
                listAdapter.notifyDataSetChanged(); //Update
                scan_anzahl.setText("Scan Anzahl: "+arrayList.size());
            }
        });
        //*END
        //*START ListView
        arrayList = new ArrayList<String>();

        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayList);
        listView = (ListView) findViewById(R.id.theListView);
        listView.setAdapter(listAdapter);

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position , long id) {
                String s = "LONG geklickt: "+ String.valueOf(parent.getItemAtPosition(position));
                Toast.makeText(MorgenlandQR.this, s, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        //*END
    }

    @Override  //SHOW Table Content onStart
    protected void onStart() {
        super.onStart();
        //**SELECT id, qrText, timestamp from QRText WHERE datum = '08-05-2017'
        String[] columns = {"id","qrText", "timestamp", "datum"};
        Cursor cursor= db.query(DbHelper.TABLE_QRText,columns, null,null,null,null,null);  //<--- WHERE daum = '+datum-Variable+'
        int qrTextIndex = cursor.getColumnIndex(DbHelper.COLUMN_QRText);

        arrayList.clear();
        if (cursor.getCount() > 0){ //number of Rows in cursor
            while (cursor.moveToNext()){
                arrayList.add(cursor.getString(qrTextIndex));
            }
        }else{
            Toast.makeText(MorgenlandQR.this,"Tabelle ist leer",Toast.LENGTH_SHORT).show();
        }
        listAdapter.notifyDataSetChanged();
        scan_anzahl.setText("Scan Anzahl: "+arrayList.size());
        cursor.close();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null){
            if (result.getContents() == null){
                Toast.makeText(this, "Du hast das Scannen abgebrochen", Toast.LENGTH_SHORT).show();
            }else {
                final String qrText = result.getContents();
                //BEI jedem Scan -> prüfen ob qrText in Tabelle existiert -> if yes: nix machen
                //                                                        ->if no: eintragen ->dann in arrayList eintragen
                Calendar c = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
                String formattedDate = df.format(c.getTime());

                        String [] columns = {"qrText"};
                        //**SELECT qrText FROM QRText WHERE qrText = 'someText'
                        Cursor cursor = db.query(DbHelper.TABLE_QRText, columns, DbHelper.COLUMN_QRText+" = '"+qrText+"'",
                                null,null,null,null);

                        if (cursor.moveToFirst()){ //false: if cursor is emtpy. true: if cursor not empty & operation is a succes
                            Toast.makeText(MorgenlandQR.this, "already in table: "+cursor.getString(0), Toast.LENGTH_SHORT).show();
                        }else {
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(DbHelper.COLUMN_QRText, qrText);
                            contentValues.put(DbHelper.COLUMN_DATUM, formattedDate);

                            Long insertId = db.insert(DbHelper.TABLE_QRText, null, contentValues);
                            if (insertId == -1) {
                                Toast.makeText(MorgenlandQR.this, "Fehler, konnte Daten nicht local speichern", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MorgenlandQR.this, "Daten gespeichert. ID: " + insertId, Toast.LENGTH_SHORT).show();
                                Toast.makeText(this, qrText, Toast.LENGTH_SHORT).show();
                                arrayList.add(qrText); //1.Add Item To Array
                                listAdapter.notifyDataSetChanged(); //2.Notify Adapter( update ListView )
                            }
                        }
                    scan_anzahl.setText("Scan Anzahl: "+arrayList.size());
                    cursor.close();
            }
        }else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onDestroy() {
        //Close DB
            db.close();
            super.onDestroy();
    }
}
