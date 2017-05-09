package com.example.hajir.morgenlandqr;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

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
        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayList);
        listView.setAdapter(listAdapter);

        //*START DB Connection
        db = new DbHelper(this).getWritableDatabase();

        //Add ClickListeners
        sync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Send arrayListe (filled OnStart()) to PHP script

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
        //Fill arrayListe via DB-> fill ListView via arrayListe
        updateListViewViaDB();
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }

    public void updateListViewViaDB(){
        //**SELECT * FROM QRText
        String[] columns = {"id","qrText", "timestamp", "datum"};
        Cursor cursor= db.query(DbHelper.TABLE_QRText,columns, null,null,null,null,null);
        int qrTextIndex = cursor.getColumnIndex(DbHelper.COLUMN_QRText);

        arrayList.clear();
        if (cursor.getCount() > 0){ //number of Rows in cursor
            while (cursor.moveToNext()){
                arrayList.add(cursor.getString(qrTextIndex));
            }
        }else{
            Toast.makeText(Home_A.this,"Tabelle ist leer",Toast.LENGTH_SHORT).show();
        }
        listAdapter.notifyDataSetChanged();
        home_liste_anzahl.setText("Scan Anzahl: "+arrayList.size());
        cursor.close();
    }
    public void searchForKeywordDisplayResult(String keyword){
        // SELECT qrText FROM QRText WHERE qrText LIKE '% +keyword+ %' COLLATE NOCASE; <---ignore case
        String[] columns = {"id","qrText", "timestamp", "datum"};
        Cursor cursor= db.query(DbHelper.TABLE_QRText,columns, DbHelper.COLUMN_QRText+" LIKE '%"+keyword+"%'"
                ,null,null,null,null); // COLLATE NOCASE
        int qrTextIndex = cursor.getColumnIndex(DbHelper.COLUMN_QRText);

        arrayList.clear();
        if (cursor.getCount() > 0){ //number of Rows in cursor
            while (cursor.moveToNext()){
                arrayList.add(cursor.getString(qrTextIndex));
                Toast.makeText(Home_A.this,"gefunden: "+cursor.getString(qrTextIndex),Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(Home_A.this,"Tabelle ist leer",Toast.LENGTH_SHORT).show();
        }
        listAdapter.notifyDataSetChanged();
        home_liste_anzahl.setText("Scan Anzahl: "+arrayList.size());
        cursor.close();

    }
}
