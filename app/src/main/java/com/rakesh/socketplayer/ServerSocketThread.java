package com.rakesh.socketplayer;

import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;

import static com.rakesh.socketplayer.ServerActivity.getPath;

/**
 * Created by Rakesh on 25-03-2018.
 */

public class ServerSocketThread extends Thread {

    File f;
    private String message;
    private ServerActivity serverActivity;
    private Uri uri;
    private String path;

    ServerSocketThread(ServerActivity serverActivity, Uri uri) {
        this.serverActivity = serverActivity;
        this.uri = uri;
    }

    @Override
    public void run() {
        Socket socket = null;
        try {
            path = getPath(serverActivity, uri);
            message = path;

        } catch (Exception e) {
            Log.e("URI SYNTAX", "URISyntaxException");
            e.printStackTrace();
        }
        if (path != null) {
            int index = path.lastIndexOf("/");
            message = path.substring(index + 1);

            try {
                f = new File(path);

                serverActivity.serverSocket = new ServerSocket(ServerActivity.SocketServerPORT);
                serverActivity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        serverActivity.infoPort.setText("Port : " + serverActivity.serverSocket.getLocalPort());
                        serverActivity.choose.setText("Generate QR");
                        serverActivity.info.setText(message);
                        serverActivity.length = (int) f.length();
                    }
                });

                while (true) {
                    socket = serverActivity.serverSocket.accept();
                    FileThread fileThread = new FileThread(serverActivity, socket, path);
                    fileThread.start();
                }
            } catch (IOException e) {
                Log.e("SOCKET SERVER ACCEPT", e.getMessage());
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        Log.e("SOCKET SERVER CLOSE", e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        } else {
            serverActivity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    serverActivity.info.setText("File Not Found!");
                }
            });
        }
    }
}
