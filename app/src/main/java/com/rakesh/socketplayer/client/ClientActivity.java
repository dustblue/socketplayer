package com.rakesh.socketplayer.client;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.rakesh.socketplayer.MediaActivity;
import com.rakesh.socketplayer.R;
import com.rakesh.socketplayer.barcode.BarcodeCaptureActivity;

public class ClientActivity extends AppCompatActivity {

    public static final int BARCODE_REQUEST_CODE = 123;
    ProgressDialog progressDialog;
    EditText editTextAddress, textPort, editTextFileLength, editTextFilename;
    Button buttonConnect;
    FloatingActionButton scan, last;
    long length = -1;
    int port;
    String filename = "", ip, finalPath;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = prefs.edit();

        editTextAddress = findViewById(R.id.address);
        editTextFileLength = findViewById(R.id.file_length);
        editTextFilename = findViewById(R.id.filename);
        textPort = findViewById(R.id.portx);
        textPort.setHint("Port :" + 8080);
        buttonConnect = findViewById(R.id.connect);
        scan = findViewById(R.id.fab);
        last = findViewById(R.id.last);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Receiving File from Server...");
        progressDialog.setMessage("Downloading");
        progressDialog.setCancelable(false);

        buttonConnect.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                ip = editTextAddress.getText().toString();
                port = Integer.parseInt(textPort.getText().toString());
                filename = editTextFilename.getText().toString();
                length = Long.parseLong(editTextFileLength.getText().toString());
                ClientThread clientThread =
                        new ClientThread(ClientActivity.this,
                                ip, port);

                clientThread.start();
                progressDialog.show();
            }
        });

        scan.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ClientActivity.this, BarcodeCaptureActivity.class);
                startActivityForResult(intent, BARCODE_REQUEST_CODE);
            }
        });

        last.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String lastPath = prefs.getString("lastFile", "none");
                if (lastPath.equals("none")) {
                    Toast.makeText(ClientActivity.this, "No file found", Toast.LENGTH_LONG).show();
                } else {
                    Intent intent = new Intent(ClientActivity.this, MediaActivity.class);
                    intent.putExtra("file", lastPath);
                    startActivity(intent);
                }
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
                    editTextFilename.setText(value.split(",")[2]);
                    editTextFileLength.setText(value.split(",")[3]);
                    buttonConnect.callOnClick();
                } else {
                    //TODO Default Values
                }
            } else
                Log.e("Barcode Error", CommonStatusCodes.getStatusCodeString(resultCode));
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }

    public void startPlayer() {
        editor.putString("lastFile", finalPath);
        editor.commit();
        Intent intent = new Intent(ClientActivity.this, MediaActivity.class);
        intent.putExtra("file", finalPath);
        startActivity(intent);
    }

}
