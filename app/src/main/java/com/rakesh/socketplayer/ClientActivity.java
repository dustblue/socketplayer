package com.rakesh.socketplayer;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import java.util.Locale;

public class ClientActivity extends AppCompatActivity {

    public static final int BARCODE_REQUEST_CODE = 123;
    EditText editTextAddress, textPort;
    Button buttonConnect;
    FloatingActionButton scan;
    int length = -1, port;
    String filename = "", ip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        editTextAddress = findViewById(R.id.address);
        textPort = findViewById(R.id.port_client);
        textPort.setHint("Port :" + 8080);
        buttonConnect = findViewById(R.id.connect);
        scan = findViewById(R.id.fab);

        buttonConnect.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                ip = editTextAddress.getText().toString();
                port = Integer.parseInt(textPort.getText().toString());
                ClientThread clientThread =
                        new ClientThread(ClientActivity.this,
                                ip , port);

                clientThread.start();
            }
        });

        scan.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ClientActivity.this, BarcodeCaptureActivity.class);
                startActivityForResult(intent, BARCODE_REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BARCODE_REQUEST_CODE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    Point[] p = barcode.cornerPoints;
                    String value = barcode.displayValue;

                    editTextAddress.setText(value.split(",")[0]);
                    textPort.setText(value.split(",")[1]);
                    filename = value.split(",")[2];
                    length = Integer.parseInt(value.split(",")[3]);
                } else {
                    //TODO Default Values
                }
            } else
                Log.e("Barcode Error", CommonStatusCodes.getStatusCodeString(resultCode));
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }

}
