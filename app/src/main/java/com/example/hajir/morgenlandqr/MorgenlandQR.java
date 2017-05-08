package com.example.hajir.morgenlandqr;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MorgenlandQR extends AppCompatActivity {
    private Button scan_btn;
    private Button save_btn;
    private Button showTable_btn;
    ArrayAdapter<String> listAdapter;
    ArrayList<String> arrayList;
    ListView listView;
    SQLiteDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_morgenland_qr);
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
        //*START Save Button
        save_btn = (Button) findViewById(R.id.save_btn);
        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //****NEU: Save arrayList to SQL-lite 'QRText' Table
                if (arrayList.size()>0) {
                    for (int i = 0; i < arrayList.size(); i++) {
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(DbHelper.COLUMN_QRText, arrayList.get(i));
                        contentValues.put(DbHelper.COLUMN_DATUM, "08-05-201" + i);
                        //Datum generieren
                        db.insert(DbHelper.TABLE_QRText, null, contentValues);
                    }
                }else   Toast.makeText(MorgenlandQR.this, "Deine Liste ist leer", Toast.LENGTH_SHORT).show();
            }
        });
        //*START Show Table Button
        showTable_btn = (Button) findViewById(R.id.showDB_btn);
        showTable_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //**SELECT qrText, timestamp from QRText WHERE datum = '08-05-2017'
                String[] columns = {"id","qrText", "timestamp", "datum"};
                //Cursor cursor= db.query(DbHelper.TABLE_QRText,columns, DbHelper.COLUMN_DATUM+" = "+"'08-05-201777'",null,null,null,null);
                Cursor cursor= db.query(DbHelper.TABLE_QRText,columns, null,null,null,null,null);

                int idIndex = cursor.getColumnIndex(DbHelper.COLUMN_ID);
                int qrTextIndex = cursor.getColumnIndex(DbHelper.COLUMN_QRText);
                int timestampIndex = cursor.getColumnIndex(DbHelper.COLUMN_TIMESTAMP);
                int datumIndex = cursor.getColumnIndex(DbHelper.COLUMN_DATUM);
                Log.d("LOG", "IDIndx: "+idIndex);
                Log.d("LOG", "qrIndex: "+qrTextIndex);
                Log.d("LOG", "timestampIndex: "+timestampIndex);
                Log.d("LOG", "datumIndex: "+datumIndex);

                while (cursor.moveToNext()){
                    Toast.makeText(MorgenlandQR.this,cursor.getString(idIndex)+", "+cursor.getString(qrTextIndex)+
                            ", "+cursor.getString(timestampIndex)+", "+cursor.getString(datumIndex), Toast.LENGTH_LONG).show();
                }
                cursor.close();
            }
        });
        //*END

        //*START ListView
        arrayList = new ArrayList<String>();

        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayList);
        listView = (ListView) findViewById(R.id.theListView);
        listView.setAdapter(listAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String s = "Du hast geklickt: "+ String.valueOf(parent.getItemAtPosition(position));
                Toast.makeText(MorgenlandQR.this, s, Toast.LENGTH_LONG).show();
            }
        });
        //*END
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null){
            if (result.getContents() == null){
                Toast.makeText(this, "Du hast das Scannen abgebrochen", Toast.LENGTH_LONG).show();
            }else {
                //result.getContents()  enthält den String aus dem QR-Scan
                final String qrText = result.getContents();
                Toast.makeText(this, qrText, Toast.LENGTH_LONG).show();

                //*Add new Item to ListView
                arrayList.add(qrText+"update"); //1.Add Item To Array
                listAdapter.notifyDataSetChanged(); //2.Notify Adapter(which then gives the Item-array to the ListView)
                //*END

                //*START POST data to PHP-Script
                RequestQueue queue = Volley.newRequestQueue(this);
                String url = "http://morgenland-teppiche.com/warenbestand/liste.php";

                //StringRequest(method, url, responseListener, errorListener){anon}  <-- 4 Parameter + 1 anon Class
                StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Toast.makeText(MorgenlandQR.this, "Response: "+response, Toast.LENGTH_LONG).show();
                            }

                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(MorgenlandQR.this, "Volley error: "+error.toString(), Toast.LENGTH_LONG).show();
                            }
                        }
                )
                {     //anonymous Class
                        @Override  //override POST Parameters
                        protected Map<String, String> getParams(){
                            Map<String, String> params = new HashMap<String, String>();
                            //****NEU: loop through arrayList -> Fill params with arrayList-elements: params.put(""+i, arrayList[i])
                            //-> send params to server
                            params.put("qrText", qrText);
                            return params;
                        }
                };

                //Execute Request
                queue.add(stringRequest);
                //*END
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
