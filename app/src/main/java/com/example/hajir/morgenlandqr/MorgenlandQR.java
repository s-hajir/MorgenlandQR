package com.example.hajir.morgenlandqr;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.HashMap;
import java.util.Map;

public class MorgenlandQR extends AppCompatActivity {
    private Button scan_btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_morgenland_qr);
        scan_btn = (Button) findViewById(R.id.scan_btn);
        final Activity activity = this;
        scan_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator intentIntegrator = new IntentIntegrator(activity);
                intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                intentIntegrator.setPrompt("Scann");
                intentIntegrator.setCameraId(0);
                intentIntegrator.setBeepEnabled(false);
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
                Toast.makeText(this, "Du hast das Scannen abgebrochen", Toast.LENGTH_LONG).show();
            }else {
                //result.getContents()  enthält den String aus dem QR-Scan
                final String qrText = result.getContents();
                Toast.makeText(this, qrText, Toast.LENGTH_LONG).show();

                //POST data to PHP-Script
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
                            params.put("qrText", qrText);
                            return params;
                        }
                };

                //Execute Request
                queue.add(stringRequest);
            }
        }else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
