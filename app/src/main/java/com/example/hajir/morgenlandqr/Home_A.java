package com.example.hajir.morgenlandqr;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Debug;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Home_A extends AppCompatActivity {
    private Button sync;
    private Button home_toscan_in_btn;
    private Button home_toscan_out_btn;
    private Button home_search_btn;
    private Button home_show_table;
    private ListView listView;
    private EditText home_serachword;
    private TextView home_liste_anzahl;

    ArrayAdapter<String> listAdapter;
    ArrayList<String> arrayList;
    ArrayList<String> scannedOutList;
    ArrayList<String> scannedInList;
    SQLiteDatabase db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_);
        //Init all Views
        sync = (Button) findViewById(R.id.sync);
        home_toscan_in_btn = (Button) findViewById(R.id.home_toscan_in_btn);
        home_toscan_out_btn = (Button) findViewById(R.id.home_toscan_out_btn);
        home_search_btn = (Button) findViewById(R.id.home_search_btn);
        home_show_table = (Button) findViewById(R.id.home_show_table);
        listView = (ListView) findViewById(R.id.home_listview);
        home_serachword = (EditText) findViewById(R.id.home_searchword);
        home_liste_anzahl = (TextView) findViewById(R.id.home_liste_anzahl);
        //**ListView
        listView = (ListView) findViewById(R.id.home_listview);
        arrayList = new ArrayList<>();
        scannedOutList = new ArrayList<>();
        scannedInList = new ArrayList<>();
        //TextView t = android.R.layout.simple_expandable_list_item_1;android.R.layout.simple_list_item_1
        listAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1 , arrayList);
        listView.setAdapter(listAdapter);

        //*START DB Connection
        db = new DbHelper(this).getWritableDatabase();

        //Add ClickListeners
        sync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Send arrayListe (filled OnStart()) to PHP script
                sync();

            }
        });
        home_toscan_in_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Open new Intent
                Intent scanInIntent = new Intent(Home_A.this, ScanIn_a.class);
                startActivity(scanInIntent);
            }
        });
        home_toscan_out_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Open new Intent
                Intent scanOutIntent = new Intent(Home_A.this, ScanOut_a.class);
                startActivity(scanOutIntent);
            }
        });
        home_search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Search DB for keyword -> update listView
                String keyword = Home_A.this.home_serachword.getText().toString();
                searchForKeywordDisplayResult(keyword);
            }
        });
        home_show_table.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Select all contents from table -> update listView
                Home_A.this.updateListViewViaDB();
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        //Fill arrayList via DB-> fill ListView via arrayList
        //Fill scannedOutList and scannedInList via DB
        updateListViewViaDB();
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }

    public void updateListViewViaDB(){
        Toast.makeText(Home_A.this,"Datenbank wird aktualisiert",Toast.LENGTH_SHORT).show();
        //**SELECT * FROM QRText
        String[] columns = {"id","qrText", "timestamp", "scanout", "scanin", "auf_lager"};
        Cursor cursor= db.query(DbHelper.TABLE_QRText,columns, null,null,null,null,null);
        int qrTextIndex = cursor.getColumnIndex(DbHelper.COLUMN_QRText);
        int scanout = cursor.getColumnIndex(DbHelper.COLUMN_SCANOUT);
        int scanin = cursor.getColumnIndex(DbHelper.COLUMN_SCANIN);
        int auf_lager = cursor.getColumnIndex(DbHelper.COLUMN_AUFLAGER);

        arrayList.clear();
        scannedOutList.clear();
        scannedInList.clear();
        Toast.makeText(Home_A.this,"Select Cursorsize:"+cursor.getCount(),Toast.LENGTH_SHORT).show();
        if (cursor.getCount() > 0){ //number of Rows in cursor
            while (cursor.moveToNext()){
                String auf_Lager = cursor.getString(auf_lager);  //könnte null leifern

                if (cursor.getString(scanout) != null ){ //cursor.getString(scanout) returns NULL if column has no value
                    //we can expect: if column scanout has a value -> then scanin also has a value
                    String out = cursor.getString(scanout);
                    if(out.contains("scanned-out")){
                        //'scanned-out', '-'   <--values of scanout and scanin column
                        arrayList.add(cursor.getString(qrTextIndex)+" -out");
                        scannedOutList.add(cursor.getString(qrTextIndex));
                    }else if(out.contains("-")) {
                        //'-', 'scanned-in'    <--values of scanout and scanin column
                        arrayList.add(cursor.getString(qrTextIndex)+" -in");
                        scannedInList.add(cursor.getString(qrTextIndex));
                    }
                }else {
                    if(auf_Lager != null){
                        arrayList.add(cursor.getString(qrTextIndex)+" **auf-Lager: "+auf_Lager);
                    }else arrayList.add(cursor.getString(qrTextIndex));
                }
            }
            Toast.makeText(Home_A.this,"scanOut: "+scannedOutList.size()+", scanIn: "+scannedInList.size(),Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(Home_A.this,"Tabelle ist leer",Toast.LENGTH_SHORT).show();
        }
        listAdapter.notifyDataSetChanged();
        home_liste_anzahl.setText("Scan Anzahl: "+arrayList.size());
        cursor.close();
    }
    public void searchForKeywordDisplayResult(String keyword){
        // SELECT qrText FROM QRText WHERE qrText LIKE '% +keyword+ %' COLLATE NOCASE; <---ignore case
        String[] columns = {"id","qrText", "timestamp","scanout","scanin", "auf_lager"};
        Cursor cursor= db.query(DbHelper.TABLE_QRText,columns, DbHelper.COLUMN_QRText+" LIKE '%"+keyword+"%'",null,null,null,null); // COLLATE NOCASE
        int qrTextIndex = cursor.getColumnIndex(DbHelper.COLUMN_QRText);
        int scanout = cursor.getColumnIndex(DbHelper.COLUMN_SCANOUT);
        int scanin = cursor.getColumnIndex(DbHelper.COLUMN_SCANIN);
        int auf_lager = cursor.getColumnIndex(DbHelper.COLUMN_AUFLAGER);

        arrayList.clear();
        if (cursor.getCount() > 0){ //number of Rows in cursor
            while (cursor.moveToNext()){
                if (cursor.getString(scanout) != null ){
                    //scanned-out found
                    arrayList.add(cursor.getString(qrTextIndex)+" -out");
                }
                else if (cursor.getString(scanin) != null ){
                    //scanned-in found
                    arrayList.add(cursor.getString(qrTextIndex)+" -in");
                }
                else{
                    //normal found
                    String auf_Lager = cursor.getString(auf_lager);
                    if (auf_Lager != null){
                        arrayList.add(cursor.getString(qrTextIndex)+" **auf-Lager: "+auf_Lager);
                    }else arrayList.add(cursor.getString(qrTextIndex));
                }
            }
        }else{
            Toast.makeText(Home_A.this,"Leeres Suchergebnis",Toast.LENGTH_SHORT).show();
        }
        listAdapter.notifyDataSetChanged();
        home_liste_anzahl.setText("Scan Anzahl: "+arrayList.size());
        cursor.close();
    }

    void sync(){
        int scannedOutListSize = scannedOutList.size();
        int scannedInListSize = scannedInList.size();
        if (scannedOutListSize >0 && scannedInListSize >0){
            Toast.makeText(Home_A.this,"scanOut und scanIn initialisieren",Toast.LENGTH_SHORT).show();
            Log.d(" sync: ->", " call scanOutAndScanIn() ");
            scanOutAndScanIn();
        }
        else if (scannedOutListSize > 0){
            Toast.makeText(Home_A.this,"scanOut initialisieren",Toast.LENGTH_SHORT).show();
            Log.d(" sync: ->", " call scanOut() ");
            scanOut();
        }
        else if (scannedInListSize > 0){
            Toast.makeText(Home_A.this,"scanIn initialisieren",Toast.LENGTH_SHORT).show();
            Log.d(" sync: ->", " call scanIn()");
            scanIn();
        }
        else {
            Toast.makeText(Home_A.this,"Daten vom Server holen",Toast.LENGTH_SHORT).show();
            Log.d(" sync: ->", " call updateLocalTableViaServer() ");
            updateLocalTableViaServer();
        }
    }

    void scanOutAndScanIn(){
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://morgenland-teppiche.com/warenbestand/script.php";

        //StringRequest(method, url, responseListener, errorListener){anon}
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response == "ok"){
                            Toast.makeText(Home_A.this, "ScanOutAndScanIn(): response ok ", Toast.LENGTH_LONG).show();
                            Log.d("scanOutAndScanIn(): ->", " call scanIn()");
                            scanIn();    //SEND scanIn List
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(Home_A.this, "Volley error: "+error.toString(), Toast.LENGTH_LONG).show();
            }
        }
        )
        {     //anon Class
            @Override
            protected Map<String, String> getParams(){
                Map<String, String> params = new HashMap<String, String>();
                params.put("scan-out","scan-out"); //Notify Server: this is a scan-out request
                //Fill params
                for(int i=0;i<scannedOutList.size();i++){
                    params.put(""+i, scannedOutList.get(i));
                }
                return params;
            }
        };
        queue.add(stringRequest);
    }

    void scanOut(){
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://morgenland-teppiche.com/warenbestand/script.php";

        //StringRequest(method, url, responseListener, errorListener){anon}
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                            Toast.makeText(Home_A.this, "ScanOut(): response "+response, Toast.LENGTH_LONG).show();
                            Log.d("scanOut() ->", " call updateLocalTableViaServer() "+response);
                            updateLocalTableViaServer();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(Home_A.this, "Volley error: "+error.toString(), Toast.LENGTH_LONG).show();
            }
        }
        )
        {     //anon Class
            @Override
            protected Map<String, String> getParams(){
                Map<String, String> params = new HashMap<String, String>();
                params.put("scan-out","scan-out"); //Notify Server: this is a scan-out request
                //Fill params
                for(int i=0;i<scannedOutList.size();i++){
                    params.put(""+i, scannedOutList.get(i));
                }
                return params;
            }
        };
        queue.add(stringRequest);
    }
    void scanIn(){
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://morgenland-teppiche.com/warenbestand/script.php";

        //StringRequest(method, url, responseListener, errorListener){anon}
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("scanIn() ->", " Toast response: "+response);
                        scannedInList.clear(); //empty the list

                        //**NEU
                        try {
                            // clear local table content -> insert new values
                            db.delete(DbHelper.TABLE_QRText,null,null); //clear Table
                            arrayList.clear();
                            ContentValues contentValues = new ContentValues();

                            JSONArray jsonArray = new JSONArray(response);
                            int jsonArrayLength = jsonArray.length();
                            //Iterate through objArray
                            for (int i=0; i<jsonArrayLength;i++){
                                JSONArray jsonArrayElement = (JSONArray) jsonArray.get(i);
                                //get 3 properties of our arrayElement
                                String jsonArrayElementProp0 = jsonArrayElement.optString(0);
                                String jsonArrayElementProp1 = jsonArrayElement.optString(1);
                                String jsonArrayElementProp2 = jsonArrayElement.optString(2);
                                String jsonArrayElementProp3 = jsonArrayElement.optString(3);

                                contentValues.clear();                                              //clear
                                contentValues.put(DbHelper.COLUMN_QRText, jsonArrayElementProp1);   //insert new values
                                contentValues.put(DbHelper.COLUMN_AUFLAGER, jsonArrayElementProp3); //empty string will also be inserted
                                Long insertId = db.insert(DbHelper.TABLE_QRText, null, contentValues);
                                arrayList.add(jsonArrayElementProp1+" **auf-Lager: "+jsonArrayElementProp3);
                            }
                            home_liste_anzahl.setText("Scan Anzahl: "+arrayList.size());
                            listAdapter.notifyDataSetChanged();
                            Toast.makeText(Home_A.this,arrayList.size()+" Artikel vom Server erhalten",Toast.LENGTH_LONG).show();
                        }catch (JSONException e) {
                            Toast.makeText(Home_A.this, "OnResponse: JSONArray parse Problem", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                        //**NEU
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(Home_A.this, "Volley error: "+error.toString(), Toast.LENGTH_LONG).show();
            }
        }
        )
        {     //anon Class
            @Override
            protected Map<String, String> getParams(){
                Map<String, String> params = new HashMap<String, String>();
                params.put("scan-in","scan-in"); //Notify Server: this is a scan-out request
                //Fill params
                for(int i=0;i<scannedInList.size();i++){
                    params.put(""+i, scannedInList.get(i));
                }
                return params;
            }
        };
        queue.add(stringRequest);
    }

    void updateLocalTableViaServer(){
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://morgenland-teppiche.com/warenbestand/script.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("updateTableViaServer->"," OnRespnse");
                        scannedOutList.clear(); //empty the list
                        //If response != "Fehler"; -> proceed
                        try {
                            // clear local table content -> insert new values
                            db.delete(DbHelper.TABLE_QRText,null,null); //clear Table
                            arrayList.clear();
                            ContentValues contentValues = new ContentValues();

                            JSONArray jsonArray = new JSONArray(response);
                            int jsonArrayLength = jsonArray.length();
                            //Iterate through objArray
                            for (int i=0; i<jsonArrayLength;i++){
                                JSONArray jsonArrayElement = (JSONArray) jsonArray.get(i);
                                //get 3 properties of our arrayElement
                                String jsonArrayElementProp0 = jsonArrayElement.optString(0);
                                String jsonArrayElementProp1 = jsonArrayElement.optString(1);
                                String jsonArrayElementProp2 = jsonArrayElement.optString(2);
                                String jsonArrayElementProp3 = jsonArrayElement.optString(3);

                                contentValues.clear();                                              //clear
                                contentValues.put(DbHelper.COLUMN_QRText, jsonArrayElementProp1);   //insert new values
                                contentValues.put(DbHelper.COLUMN_AUFLAGER, jsonArrayElementProp3); //empty string will also be inserted
                                Long insertId = db.insert(DbHelper.TABLE_QRText, null, contentValues);
                                arrayList.add(jsonArrayElementProp1+" **auf-Lager: "+jsonArrayElementProp3);
                            }
                            home_liste_anzahl.setText("Scan Anzahl: "+arrayList.size());
                            listAdapter.notifyDataSetChanged();
                            Toast.makeText(Home_A.this,arrayList.size()+" Artikel vom Server erhalten",Toast.LENGTH_LONG).show();
                        }catch (JSONException e) {
                            Toast.makeText(Home_A.this, "OnResponse: JSONArray parse Problem", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(Home_A.this, "Volley error: "+error.toString(), Toast.LENGTH_LONG).show();
            }
        }
        )
        {
            @Override
            protected Map<String, String> getParams(){
                Map<String, String> params = new HashMap<String, String>();
                params.put("showTable","showTable");
                return params;
            }
        };
        queue.add(stringRequest);
    }
    void sync2(){
        //1.Send Scan-Out Items to Server
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://morgenland-teppiche.com/warenbestand/script.php";

        //StringRequest(method, url, responseListener, errorListener){anon}
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            int jsonArrayLength = jsonArray.length();
                            //Iterate through objArray
                            for (int i=0; i<jsonArrayLength;i++){
                                JSONArray jsonArrayElement = (JSONArray) jsonArray.get(i);
                                //JSONArray jsonArrayElement = jsonArray.getJSONArray(i);  <---2.Versuch
                                //get 3 properties of our arrayElement
                                String jsonArrayElementProp0 = jsonArrayElement.optString(0);
                                String jsonArrayElementProp1 = jsonArrayElement.optString(1);
                                String jsonArrayElementProp2 = jsonArrayElement.optString(2);
                                String jsonArrayElementProp3 = jsonArrayElement.optString(3);
                                Toast.makeText(Home_A.this,"jsonArrayElement---"+ jsonArrayElement.toString()+" jsonArrayElementPR0---"+ jsonArrayElementProp0
                                        +" jsonArrayElementPR1---"+ jsonArrayElementProp1 +" jsonArrayElementPR2---"+ jsonArrayElementProp2
                                        +" jsonArrayElementPR3---"+jsonArrayElementProp3
                                        , Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(Home_A.this, "OnResponse: JSONArray parse Problem", Toast.LENGTH_SHORT);
                            e.printStackTrace();
                        }
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(Home_A.this, "Volley error: "+error.toString(), Toast.LENGTH_LONG).show();
            }
        }
        )
        {     //anon Class
            @Override
            protected Map<String, String> getParams(){
                Map<String, String> params = new HashMap<String, String>();
                //get Table contents -> Fill params
                String [] columns = {"qrText"};
                Cursor cursor = db.query(DbHelper.TABLE_QRText, columns, DbHelper.COLUMN_SCANOUT+" = 'scanned-out'",
                        null,null,null,null);
                if (cursor.getCount() > 0){
                    params.put("scan-out","scan-out"); //Info for serverside
                    int i=0;
                    int qrTxtIndex = cursor.getColumnIndex(DbHelper.COLUMN_QRText);
                    while (cursor.moveToNext()){
                        i++;
                        params.put("qrText"+i, cursor.getString(qrTxtIndex));
                        Log.d("post param put: "+i, cursor.getString(qrTxtIndex));
                    }
                }else {
                    //Toast.makeText(Home_A.this, "No Items with scanned-out tag ", Toast.LENGTH_SHORT).show();
                    //RAUSNEHMEN !!
                    params.put("scan-out","scan-out");
                }
                cursor.close();
                return params;
            }
        };
        queue.add(stringRequest);

        //2.Receive 'OK' from Server ->Receive updated Table Items ->update local Table(clear & refill)

    }
}
