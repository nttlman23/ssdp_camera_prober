package com.example.nttlman8.ssdp_prober;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class MainActivity extends AppCompatActivity {

    final static String MULTI_ADDR = "239.255.255.250";
    final static int PORT = 1900;
    private static final String TAG = "MainActivity";

    private TextView tvName;
    private TextView tvManufacturer;
    private TextView tvManufacturerURL;
    private TextView tvModelDescription;
    private TextView tvModelName;
    private TextView tvModelNumber;
    private TextView tvPresentationURL;

    private Entry device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DiscoverTask().execute();

//                Snackbar.make(view, "New device: " + txv.getText(), Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show();
            }
        });

        device = new Entry();

        tvName = (TextView)findViewById(R.id.textViewName);
        tvManufacturer = (TextView)findViewById(R.id.textViewManufacturer);
        tvManufacturerURL = (TextView)findViewById(R.id.textViewManufacturerURL);
        tvModelDescription = (TextView)findViewById(R.id.textViewModelDescription);
        tvModelName = (TextView)findViewById(R.id.textViewModelName);
        tvModelNumber = (TextView)findViewById(R.id.textViewModelNumber);
        tvPresentationURL = (TextView)findViewById(R.id.textViewPresentationURL);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class DiscoverTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);

        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                return discoverService();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }

            return "Error";
        }

        @Override
        protected void onPostExecute(String result) {

            if (result.equals("Not found") ||
                    result.equals("No connection") ||
                    result.equals("No wi-fi connection")) {
                Log.d(TAG, result);
                Toast toast = Toast.makeText(getApplicationContext(),
                        result, Toast.LENGTH_LONG);
                toast.show();
            } else {

                tvName.setText(device.Name);
                tvManufacturer.setText(device.Manufacturer);
                tvManufacturerURL.setText(device.ManufacturerURL);
                tvModelDescription.setText(device.ModelDescription);
                tvModelName.setText(device.ModelName);
                tvModelNumber.setText(device.ModelNumber);
                tvPresentationURL.setText(device.PresentationURL);
            }
        }
    }

    private String discoverService() throws UnknownHostException {

        ConnectivityManager connMngr;
        NetworkInfo activeNetInfo;

        connMngr = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        activeNetInfo = connMngr.getActiveNetworkInfo();

        if (activeNetInfo != null && activeNetInfo.isConnected()) {
            if ( activeNetInfo.getType() != ConnectivityManager.TYPE_WIFI) {
                return "No wi-fi connection";
            }
        } else {
            return "No connection";
        }

        // Get the address that we are going to connect to.
        InetAddress address = InetAddress.getByName(MULTI_ADDR);

        // Create a buffer of bytes, which will be used to store
        // the incoming bytes containing the information from the server.
        // Since the message is small here, 256 bytes should be enough.
        byte[] buf = new byte[1024];

        // Create a new Multicast socket (that will allow other sockets/programs
        // to join it as well.
        try (MulticastSocket clientSocket = new MulticastSocket(PORT)){
            //Joint the Multicast group.
            clientSocket.joinGroup(address);

            for(int i = 0;i < 10;i++) {
                Log.d(TAG, "try #" + Integer.toString(i));
                // Receive the information and print it.
                DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
                clientSocket.receive(msgPacket);

                String msg = new String(buf, "UTF-8");

                int index = msg.indexOf("Location:");

                if (index == -1) {
                    continue;
                }

                index = msg.indexOf("http", index);

                if (index == -1) {
                    Log.e(TAG, "Parse Err");
                    continue;
                }

                String res;

                res = msg.substring(index, (msg.indexOf("\r", index)));

                Log.d(TAG,"res: " + res);

                URLConnection conn = new URL(res).openConnection();
                conn.setConnectTimeout(1000);
                conn.setReadTimeout(1000);

                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(new InputSource(conn.getInputStream()));
                doc.getDocumentElement().normalize();

                if(device.setFields(doc) == false) {
                    continue;
                }

                clientSocket.close();

                return "Found";
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }

        return "Not found";
    }
}