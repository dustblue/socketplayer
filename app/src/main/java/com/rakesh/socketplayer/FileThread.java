package com.rakesh.socketplayer;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by Rakesh on 25-03-2018.
 */

public class FileThread extends Thread {
    private ServerActivity serverActivity;
    private Socket socket;
    private String path;

    FileThread(ServerActivity serverActivity, Socket socket, String path) {
        this.serverActivity = serverActivity;
        this.socket = socket;
        this.path = path;
    }

    @Override
    public void run() {
        File file = new File(path);

        byte[] bytes = new byte[(int) file.length()];
        BufferedInputStream bis;
        try {
            bis = new BufferedInputStream(new FileInputStream(file));
            bis.read(bytes, 0, bytes.length);
            OutputStream os = socket.getOutputStream();
            os.write(bytes, 0, bytes.length);
            os.flush();
            socket.close();

            final String msg = path + "\nFile sent to: " + socket.getInetAddress();
            serverActivity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    serverActivity.info.setText(msg);
                    serverActivity.choose.setText("Choose File");
                }
            });

        } catch (Exception e) {
            Log.e("SOCKET FILE SEND", e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                Log.e("SOCKET SERVER CLOSE", e.getMessage());
                e.printStackTrace();
            }
        }
    }
}

