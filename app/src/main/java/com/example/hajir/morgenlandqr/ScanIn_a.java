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
                intentIntegrator.setPrompt("ScanIN");
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
                //BEI jedem ScanIN -> prüfen ob qrText in Tabelle existiert -> if yes: nix machen
                //                                                        ->if no: in Tabelle eintragen
                Calendar c = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
                String formattedDate = df.format(c.getTime());

                String [] columns = {"qrText"};
                //**SELECT qrText FROM QRText WHERE qrText = 'someText'
                Cursor cursor = db.query(DbHelper.TABLE_QRText, columns, DbHelper.COLUMN_QRText+" = '"+qrText+"'",
                        null,null,null,null);

                if (cursor.moveToFirst()){ //false: if cursor is emtpy. true: if cursor not empty & operation is a succes
                    Toast.makeText(ScanIn_a.this, "already in table: "+cursor.getString(0), Toast.LENGTH_SHORT).show();
                }else {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(DbHelper.COLUMN_QRText, qrText);
                    contentValues.put(DbHelper.COLUMN_DATUM, formattedDate);

                    Long insertId = db.insert(DbHelper.TABLE_QRText, null, contentValues);
                    if (insertId == -1) {
                        Toast.makeText(ScanIn_a.this, "Fehler, konnte Daten nicht local speichern", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ScanIn_a.this, "Daten gespeichert. ID: " + insertId, Toast.LENGTH_SHORT).show();
                        Toast.makeText(this, qrText, Toast.LENGTH_SHORT).show();
                        arrayList.add("neu: ");
                        arrayList.add(qrText); //1.Add Item To Array
                        listAdapter.notifyDataSetChanged(); //2.Notify Adapter( update ListView )
                    }
                }
                scan_in_anzahl.setText("Scan Anzahl: "+arrayList.size());
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
