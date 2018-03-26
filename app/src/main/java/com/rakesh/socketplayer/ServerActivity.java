package com.rakesh.socketplayer;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
    public final static int QRcodeWidth = 500;
    static final int SocketServerPORT = 8080;
    TextView infoIp, infoPort, info, qrText;
    ImageView qr;
    String textToEncode, chooseText;
    Button choose;
    int length;
    ServerSocket serverSocket;
    ServerSocketThread serverSocketThread;
    Bitmap bitmap;

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

    public static Bitmap TextToImageEncode(String Value) throws WriterException {
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
                        Color.BLACK : Color.WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444);

        bitmap.setPixels(pixels, 0, 500, 0, 0, bitMatrixWidth, bitMatrixHeight);
        return bitmap;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        infoIp = findViewById(R.id.ip);
        infoPort = findViewById(R.id.port);
        info = findViewById(R.id.info);
        choose = findViewById(R.id.choose);
        qr = findViewById(R.id.qr);
        qrText = findViewById(R.id.qr_text);

        info.setText(getIntent().getStringExtra("previous"));

        infoIp.append(getIpAddress());
        infoPort.append(Integer.toString(SocketServerPORT));

        choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseText = choose.getText().toString();
                if (chooseText.equals("Choose a File")) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    try {
                        startActivityForResult(Intent.createChooser(intent,
                                "Choose Users CSV File"), CHOOSER_REQUEST_CODE);
                    } catch (android.content.ActivityNotFoundException ex) {
                        Toast.makeText(getParent(), "Please install a File Manager", Toast.LENGTH_SHORT).show();
                    }
                } else if (chooseText.equals("Generate QR") && isValid()) {
                    textToEncode = infoIp.getText().toString().split(" ")[1]
                            + "," + infoPort.getText().toString().split(":")[1].trim()
                            + "," + info.getText().toString()
                            + "," + length;
                    new GenerateQR(ServerActivity.this).execute();
                }
            }
        });

        choose.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                chooseText = choose.getText().toString();
                if (chooseText.equals("Waiting for Client")) {
                    new AlertDialog.Builder(ServerActivity.this)
                            .setTitle("Still Waiting...")
                            .setMessage("Do you want to cancel this Server?")
                            .setPositiveButton("NO", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    refresh();
                                }
                            })
                            .create()
                            .show();
                }
                return true;
            }
        });

    }

    boolean isValid() {
        //FIXME
        return true;
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
        StringBuilder ip = new StringBuilder();
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
                        ip.append(" ").append(inetAddress.getHostAddress());
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
            ip.append("Something Wrong! ").append(e.toString()).append("\n");
        }

        return ip.toString();
    }

    public void refresh() {
        finish();
        Intent i = getIntent();
        i.putExtra("previous", info.getText().toString());
        startActivity(i);
    }

    public class GenerateQR extends AsyncTask<Void, Void, Void> {
        private ProgressDialog dialog;

        GenerateQR(ServerActivity activity) {
            dialog = new ProgressDialog(activity);
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Generating the QR Code...");
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                bitmap = TextToImageEncode(textToEncode);
            } catch (WriterException e) {
                Log.e("WriterException : ", e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            qrText.setText(textToEncode);
            qr.setImageBitmap(bitmap);
            choose.setText("Waiting for Client");
        }
    }
}