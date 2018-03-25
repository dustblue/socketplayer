package com.rakesh.socketplayer;

import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 * Created by Rakesh on 25-03-2018.
 */

public class ClientThread extends Thread {
    private ClientActivity clientActivity;
    private String dstAddress;
    private int dstPort;

    ClientThread(ClientActivity clientActivity, String address, int port) {
        this.clientActivity = clientActivity;
        dstAddress = address;
        dstPort = port;
    }

    @Override
    public void run() {
        Socket socket = null;

        try {
            socket = new Socket(dstAddress, dstPort);

            File file = new File(
                    Environment.getExternalStorageDirectory(),
                    "received.txt");

            byte[] bytes = new byte[1024];
            InputStream is = socket.getInputStream();
            FileOutputStream fos = new FileOutputStream(file, true);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            int bytesRead = is.read(bytes, 0, bytes.length);
            bos.write(bytes, 0, bytesRead);
            bos.close();
            socket.close();

            clientActivity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(clientActivity, "Finished", Toast.LENGTH_LONG).show();
                }});

        } catch (IOException e) {

            e.printStackTrace();

            final String eMsg = "SOCKET CLIENT READ: " + e.getMessage();
            clientActivity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(clientActivity, eMsg, Toast.LENGTH_LONG).show();
                }});

        } finally {
            if(socket != null){
                try {
                    socket.close();
                } catch (IOException e) {
                    Log.e("SOCKET CLIENT CLOSE", e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
}