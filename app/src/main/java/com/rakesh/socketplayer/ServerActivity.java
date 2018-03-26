package com.rakesh.socketplayer;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.util.Enumeration;

public class ServerActivity extends AppCompatActivity {

    public static final int CHOOSER_REQUEST_CODE = 100;
    TextView infoIp, infoPort, info;
    ImageView qr;
    String textToEncode;
    Button choose;
    int length;
    static final int SocketServerPORT = 8080;
    ServerSocket serverSocket;
    ServerSocketThread serverSocketThread;

    public final static int QRcodeWidth = 500;
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        infoIp = findViewById(R.id.ip);
        infoPort = findViewById(R.id.port);
        info = findViewById(R.id.info);
        choose = findViewById(R.id.choose);
        qr = findViewById(R.id.qr);

        infoIp.append(getIpAddress());
        infoPort.append(Integer.toString(SocketServerPORT));

        choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (choose.getText().toString().equals("Choose a File")) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    try {
                        startActivityForResult(Intent.createChooser(intent,
                                "Choose Users CSV File"), CHOOSER_REQUEST_CODE);
                    } catch (android.content.ActivityNotFoundException ex) {
                        Toast.makeText(getParent(), "Please install a File Manager", Toast.LENGTH_SHORT).show();
                    }
                } else if (choose.getText().toString().equals("Generate QR") && isValid()){
                    textToEncode = infoIp.getText().toString().split(":")[2].trim().split(" ")[0]
                            + "," + infoPort.getText().toString().split(":")[1].trim()
                            + "," + info.getText().toString()
                            + "," + length;
                    try {
                        bitmap = TextToImageEncode(textToEncode);
                    } catch (WriterException e) {
                        Toast.makeText(getParent(), "WriterException : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                    qr.setImageBitmap(bitmap);
                }
            }
        });

    }

    private boolean isValid() {
        //FIXME
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHOOSER_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            serverSocketThread = new ServerSocketThread(this, uri);
            serverSocketThread.start();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += "SiteLocalAddress: "
                                + inetAddress.getHostAddress() + "\n";
                    }

                }

            }

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }

        return ip;
    }

    Bitmap TextToImageEncode(String Value) throws WriterException {
        BitMatrix bitMatrix;
        try {
            bitMatrix = new MultiFormatWriter().encode(
                    Value,
                    BarcodeFormat.QR_CODE,
                    QRcodeWidth, QRcodeWidth, null
            );

        } catch (IllegalArgumentException Illegalargumentexception) {

            return null;
        }
        int bitMatrixWidth = bitMatrix.getWidth();

        int bitMatrixHeight = bitMatrix.getHeight();

        int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];

        for (int y = 0; y < bitMatrixHeight; y++) {
            int offset = y * bitMatrixWidth;

            for (int x = 0; x < bitMatrixWidth; x++) {

                pixels[offset + x] = bitMatrix.get(x, y) ?
                        getResources().getColor(R.color.QRCodeBlackColor, getTheme())
                        : getResources().getColor(R.color.QRCodeWhiteColor, getTheme());
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444);

        bitmap.setPixels(pixels, 0, 500, 0, 0, bitMatrixWidth, bitMatrixHeight);
        return bitmap;
    }

    @Nullable
    public static String getPath(Context context, Uri uri) throws URISyntaxException {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {"_data"};

            try (Cursor cursor = context.getContentResolver().query(uri, projection,
                    null, null, null)) {

                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

}