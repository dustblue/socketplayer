package com.rakesh.socketplayer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ClientActivity extends AppCompatActivity {

    EditText editTextAddress;
    Button buttonConnect;
    TextView textPort;
    static final int SocketServerPORT = 8080;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        editTextAddress = findViewById(R.id.address);
        textPort = findViewById(R.id.port);
        textPort.setHint("Port :" + SocketServerPORT);
        buttonConnect = findViewById(R.id.connect);

        buttonConnect.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                int port = SocketServerPORT;
                String input = textPort.getText().toString();
                if(!input.equals(""))
                    port = Integer.parseInt(input);
                ClientThread clientThread =
                        new ClientThread(ClientActivity.this,
                                editTextAddress.getText().toString(), port);

                clientThread.start();
            }
        });
    }

}
