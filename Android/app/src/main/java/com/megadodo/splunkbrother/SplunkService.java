package com.megadodo.splunkbrother;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class SplunkService extends Service {
    public static final String PREFS_NAME = "DeviceID";
    public String mDeviceID = "";

    protected String hostname = "10.0.0.12";
    protected int port = 8088;

    private class SendToSplunkHTTP extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            String url = (String) params[0];
            String header = (String) params[1];
            String headerValue = (String) params[2];
            String payload = (String) params[3];

            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(url);
            post.addHeader(header, headerValue);
            post.setHeader("Accept", "application/json");
            post.setHeader("Content-type", "application/json");

            Log.i("SplunkService", url);
            Log.i("SplunkService", header + ":" + headerValue);
            Log.i("SplunkService", payload);

            HttpResponse response;
            try {
                StringEntity se = new StringEntity(payload, null);
                post.setEntity(se);
                response = client.execute(post);
                // Examine the response status
                Log.i("SplunkService",response.getStatusLine().toString());

            } catch (Exception e) {
                Log.e("SplunkService", e.getMessage());
            }

            return null;
        }
    };

    private class SendToSplunkSocket extends AsyncTask{

        @Override
        protected Object doInBackground(Object[] params) {
            String hostname = (String) params[0];
            Integer port = (Integer) params[1];
            String message = (String) params[2];

            Socket sock = null;
            DataOutputStream os = null;
            try {
                sock = new Socket(hostname, port.intValue());
                os = new DataOutputStream(sock.getOutputStream());
            } catch (UnknownHostException e) {
                Log.e("SplunkService", "Don't know about host: " + hostname);
            } catch (IOException e) {
                Log.e("SplunkService", "Couldn't get I/O for the connection to: " + hostname);
            }

            if (sock != null && os != null) {
                Log.v("SplunkService", "Sending to Splunk server from " + mDeviceID);
                try {
                    os.writeBytes(message + "\n");
                    os.close();
                    sock.close();
                } catch (UnknownHostException e) {
                    Log.e("SplunkService", "Trying to connect to unknown host: " + e);
                } catch (IOException e) {
                    Log.e("SplunkService", "IOException:  " + e);
                }
            } else {
                Log.e("SplunkService", "Something is null");
            }
            return null;
        }
    };

    public SplunkService() {
    }

    public Runnable ping = new Runnable() {


        @Override
        public void run() {


            if (!mDeviceID.contentEquals("")) {
                Log.v("SplunkService", "Splunk Service Started for DeviceID " + mDeviceID);

                LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                Location loc = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                Log.v("SplunkService", "Lat:" + loc.getLatitude() + " Long:" + loc.getLongitude());
                // This is for Splunk TCP listener
                // new SendToSplunkSocket().execute(hostname, new Integer(port), "Ping from deviceID " + mDeviceID);

                // This is for Splunk HTTP event listener
                String url = "http://" + hostname + ":" + port + "/services/collector/event";
                String header = "Authorization";
                String headerValue = "Splunk 9106823C-1F66-4337-8B28-2FF59C42D1A5";
                String payload = "{ \"event\": {\"deviceID\": " + mDeviceID +
                        ", \"lat\": " + loc.getLatitude() + ", \"long\": " + loc.getLongitude() + "}}";

                new SendToSplunkHTTP().execute(url, header, headerValue, payload);
            }
        }


    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //TODO do something useful
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        mDeviceID = settings.getString("deviceID", "");

        this.ping.run();
        this.stopSelf();
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
