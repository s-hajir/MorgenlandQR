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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import java.util.List;
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

    ArrayAdapter<Teppich> mylistAdapter;
    private List<Teppich> teppichListe;
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
        teppichListe = new ArrayList<Teppich>();
        scannedOutList = new ArrayList<>();
        scannedInList = new ArrayList<>();

        mylistAdapter = new MyListAdapter(this, teppichListe);
        listView.setAdapter(mylistAdapter);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String s = teppichListe.get(position).getQrString();
                Log.d("ItemclickListener ","    ::::::listView");
                //*Fire Intent
                Intent imgPreviewIntent = new Intent(Home_A.this, ImagePreview.class);
                imgPreviewIntent.putExtra("qrText", s);
                startActivity(imgPreviewIntent);
                return true;
            }
        });

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
        //Fill arrayList via DB-> display it via ListView
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

        teppichListe.clear();
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
                        String qrText = cursor.getString(qrTextIndex);
                        teppichListe.add(new Teppich(qrText+" -out"));
                        scannedOutList.add(qrText);
                    }else if(out.contains("-")) {
                        //'-', 'scanned-in'    <--values of scanout and scanin column
                        String qrText = cursor.getString(qrTextIndex);
                        teppichListe.add(new Teppich(qrText+" -in"));
                        scannedInList.add(qrText);
                    }
                }else {
                    if(auf_Lager != null){
                        String qrText = cursor.getString(qrTextIndex);
                        teppichListe.add(new Teppich(qrText+" **auf-Lager: "+auf_Lager));
                    }else{
                        String qrText = cursor.getString(qrTextIndex);
                        teppichListe.add(new Teppich(qrText));
                    }
                }
            }
            Toast.makeText(Home_A.this,"scanOut: "+scannedOutList.size()+", scanIn: "+scannedInList.size(),Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(Home_A.this,"Tabelle ist leer",Toast.LENGTH_SHORT).show();
        }
        mylistAdapter.notifyDataSetChanged();
        home_liste_anzahl.setText("Scan Anzahl: "+teppichListe.size());
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

        //arrayList.clear();
        teppichListe.clear();
        if (cursor.getCount() > 0){ //number of Rows in cursor
            while (cursor.moveToNext()){
                if (cursor.getString(scanout) != null ){
                    //scanned-out found
                    //arrayList.add(cursor.getString(qrTextIndex)+" -out");
                    String qrText = cursor.getString(qrTextIndex)+" -out";
                    teppichListe.add(new Teppich(qrText));
                }
                else if (cursor.getString(scanin) != null ){
                    //scanned-in found
                    String qrText = cursor.getString(qrTextIndex)+" -in";
                    teppichListe.add(new Teppich(qrText));
                }
                else{
                    //normal found
                    String auf_Lager = cursor.getString(auf_lager);
                    if (auf_Lager != null){
                        //arrayList.add(cursor.getString(qrTextIndex)+" **auf-Lager: "+auf_Lager);
                        String qrText = cursor.getString(qrTextIndex)+" **auf-Lager: "+auf_Lager;
                        teppichListe.add(new Teppich(qrText));
                    }else{
                        //arrayList.add(cursor.getString(qrTextIndex));
                        String qrText = cursor.getString(qrTextIndex);
                        teppichListe.add(new Teppich(qrText));
                    }

                }
            }
        }else{
            Toast.makeText(Home_A.this,"Leeres Suchergebnis",Toast.LENGTH_SHORT).show();
        }
        mylistAdapter.notifyDataSetChanged();
        home_liste_anzahl.setText("Scan Anzahl: "+teppichListe.size());
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
                params.put("scan-out","scan-out"); //Notify Server: this is a scan-out request. First Post Param is 'scan-out: scan-out'
                //Fill params
                for(int i=0;i<scannedOutList.size();i++){ //All following Params are '1: qrString1, 2: qrString2' etc.
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
                            //arrayList.clear();
                            teppichListe.clear();
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

                                teppichListe.add(new Teppich(jsonArrayElementProp1+" **auf-Lager: "+jsonArrayElementProp3));
                            }
                            home_liste_anzahl.setText("Scan Anzahl: "+teppichListe.size());
                            mylistAdapter.notifyDataSetChanged();
                            Toast.makeText(Home_A.this,teppichListe.size()+" Artikel vom Server erhalten",Toast.LENGTH_LONG).show();
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
                            //arrayList.clear();
                            teppichListe.clear();
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
                                teppichListe.add(new Teppich(jsonArrayElementProp1+" **auf-Lager: "+jsonArrayElementProp3));
                            }
                            home_liste_anzahl.setText("Scan Anzahl: "+teppichListe.size());
                            mylistAdapter.notifyDataSetChanged();
                            Toast.makeText(Home_A.this,teppichListe.size()+" Artikel vom Server erhalten",Toast.LENGTH_LONG).show();
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
}
