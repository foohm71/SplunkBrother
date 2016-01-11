package com.megadodo.splunkbrother;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends ActionBarActivity {

    public String mDeviceID = "";
    public static final String PREFS_NAME = "DeviceID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText inputBox = (EditText) findViewById(R.id.editText);

        Button submitButton = (Button) findViewById(R.id.button);

        // Restore preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        mDeviceID = settings.getString("deviceID", "");
        inputBox.setText(mDeviceID);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDeviceID = inputBox.getText().toString().trim();
                Log.v("MainActivity", "Setting Shared Preferences for " + mDeviceID);

                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("deviceID", mDeviceID);

                // Commit the edits!
                editor.commit();
            }
        });

        if (!mDeviceID.contentEquals("")) {
            Intent intent = new Intent(this, SplunkService.class);
            PendingIntent pingService = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(pingService);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 600000, pingService);
        }

        // startService(intent);
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
}
