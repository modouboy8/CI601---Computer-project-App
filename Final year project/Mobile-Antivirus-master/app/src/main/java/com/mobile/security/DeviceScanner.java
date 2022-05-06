package com.mobile.security;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.mobile.security.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;


public class DeviceScanner extends AppCompatActivity {
    // initialize variables
    private static final String TAG;

    static {
        TAG = "DeviceScanner";
    }

    private static final int REQUEST_CODE;

    static {
        REQUEST_CODE = 1;
    }

    Intent myServiceIntent;
    private service myService;
    DatabaseScanner mDatabaseHelper;
    private Button devicescanButton;
    private TextView lastScanValueText;
    String[] method_type = new String[]{"Signature Based", "Dex Code Based"};

    @SuppressLint("SetTextI18n")
    @Override
    // Create a service.
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_scanner);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        ToggleButton togglebutton = findViewById(R.id.protectionButton);
        mDatabaseHelper = new DatabaseScanner(this);
        myService = new service();
        myServiceIntent = new Intent(this, myService.getClass());
        togglebutton.setChecked(isMyServiceRunning(myService.getClass()));

        //returnHistoryTextView
        lastScanValueText = findViewById(R.id.lastScanValueText);

        //get the spinner from the XML.
        final Spinner dropdown = findViewById(R.id.dropdown);
        // create a adapter that describes how the item are displayed.
        // Adapters are used in numerous places within Android
        // There a numerous variations of this, but this is the basic variant.
        ArrayAdapter<String> dropdownAdapter;
        dropdownAdapter = new ArrayAdapter<>(this, R.layout.list_dropdown_text, method_type);
        // set the spinner adapter to the previously created one.
        dropdown.setAdapter(dropdownAdapter);

        //return current dateTime
        Date currentTime;
        currentTime = Calendar.getInstance().getTime();
        @SuppressLint("SimpleDateFormat") DateFormat dateFormat;
        // this is the data format
        dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String currentDateTime;
        currentDateTime = dateFormat.format(currentTime);
        // current year
        String currentYear;
        currentYear = currentDateTime.substring(0,4);
        // current month
        String currentMonth;
        currentMonth = currentDateTime.substring(5,7);
        // current day
        String currentDay;
        currentDay = currentDateTime.substring(8,10);
        // current hour
        String currentHour;
        currentHour = currentDateTime.substring(11,13);
        // current minute
        String currentMinute;
        currentMinute = currentDateTime.substring(14,16);
        // current second
        String currentSecond;
        currentSecond = currentDateTime.substring(17,19);

        //RetrieveLastScanDateTime
        Cursor cursor = mDatabaseHelper.getData();
        switch (cursor.getCount()) {
            case 0:
                Toast.makeText(getApplicationContext(), "NO DATA", Toast.LENGTH_SHORT).show();
                break;
            default:
                while (cursor.moveToNext()) {
                    //differentiateDateTime
                    String historyDateTime = cursor.getString(1);
                    String historyYear = historyDateTime.substring(0, 4);
                    String historyMonth = historyDateTime.substring(5, 7);
                    String historyDay = historyDateTime.substring(8, 10);
                    String historyHour = historyDateTime.substring(11, 13);
                    String historyMinute = historyDateTime.substring(14, 16);
                    String historySecond = historyDateTime.substring(17, 19);
                    //calculateDateTime
                    if (Integer.parseInt(currentYear) != Integer.parseInt(historyYear)) {
                        int cY = Integer.parseInt(currentYear);
                        int hY = Integer.parseInt(historyYear);
                        int getYear = cY - hY;
                        String StringGetYear = Integer.toString(getYear);
                        if (getYear > 1) {
                            lastScanValueText.setText(StringGetYear + "years ago");
                        } else {
                            lastScanValueText.setText(StringGetYear + "year ago");
                        }
                    } else {
                        if (Integer.parseInt(currentMonth) != Integer.parseInt(historyMonth)) {
                            int cM = Integer.parseInt(currentMonth);
                            int hM = Integer.parseInt(historyMonth);
                            int getMonth = cM - hM;
                            String StringGetMonth = Integer.toString(getMonth);
                            if (getMonth > 1) {
                                lastScanValueText.setText(StringGetMonth + " months ago");
                            } else {
                                lastScanValueText.setText(StringGetMonth + " month ago");
                            }
                        } else {
                            if (Integer.parseInt(currentDay) != Integer.parseInt(historyDay)) {
                                int cD = Integer.parseInt(currentDay);
                                int hD = Integer.parseInt(historyDay);
                                int getDay = cD - hD;
                                String StringGetDay = Integer.toString(getDay);
                                if (getDay > 1) {
                                    lastScanValueText.setText(StringGetDay + " days ago");
                                } else {
                                    lastScanValueText.setText(StringGetDay + " day ago");
                                }
                            } else {
                                if (Integer.parseInt(currentHour) != Integer.parseInt(historyHour)) {
                                    int cH = Integer.parseInt(currentHour);
                                    int hH = Integer.parseInt(historyHour);
                                    int getHour = cH - hH;
                                    String StringGetHour = Integer.toString(getHour);
                                    if (getHour > 1) {
                                        lastScanValueText.setText(StringGetHour + " hours ago");
                                    } else {
                                        lastScanValueText.setText(StringGetHour + " hour ago");
                                    }
                                } else {
                                    if (Integer.parseInt(currentMinute) != Integer.parseInt(historyMinute)) {
                                        int cMin = Integer.parseInt(currentMinute);
                                        int hMin = Integer.parseInt(historyMinute);
                                        int getMinute = cMin - hMin;
                                        String StringGetMinute = Integer.toString(getMinute);
                                        if (getMinute > 1) {
                                            lastScanValueText.setText(StringGetMinute + " minutes ago");
                                        } else {
                                            lastScanValueText.setText(StringGetMinute + " minute ago");
                                        }
                                    } else {
                                        if (Integer.parseInt(currentSecond) == Integer.parseInt(historySecond)) {
                                            lastScanValueText.setText("Just now");
                                        } else {
                                            int cS = Integer.parseInt(currentSecond);
                                            int hS = Integer.parseInt(historySecond);
                                            int getSecond = cS - hS;
                                            String StringGetSecond = Integer.toString(getSecond);
                                            if (getSecond <= 1) {
                                                lastScanValueText.setText(StringGetSecond + " second ago");
                                            } else {
                                                lastScanValueText.setText(StringGetSecond + " seconds ago");
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                break;
        }
        final String strDateTimeAdd = currentDateTime;

        devicescanButton = findViewById(R.id.scanButton);
        devicescanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)  {
                AddData(strDateTimeAdd);
                //get dropdown value
                String selected = dropdown.getSelectedItem().toString();
                switch (selected) {
                    case "Dex Code Based":
                        finish();
                        overridePendingTransition(0, 0);
                        startActivity(getIntent());
                        overridePendingTransition(0, 0);
                        startActivity(new Intent(DeviceScanner.this, FilesScanner.class));
                        break;
                    case "Signature Based":
                        finish();
                        overridePendingTransition(0, 0);
                        startActivity(getIntent());
                        overridePendingTransition(0, 0);
                        startActivity(new Intent(DeviceScanner.this, SignatureTracking.class));
                        break;
                }
            }
        });

        final Intent intent;
        intent = new Intent(DeviceScanner.this, service.class);
        togglebutton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    //stopService(mServiceIntent);
                    Toast.makeText(getBaseContext(), "Protection OFF", Toast.LENGTH_SHORT).show();
                    NotificationManager notificationManager = getSystemService(NotificationManager.class);
                    notificationManager.cancel(100);
                    stopService(intent);
                } else {
                    Toast.makeText(getBaseContext(), "Protection ON", Toast.LENGTH_SHORT).show();
                    startService(intent);
                }
            }
        });

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        } else {
            Log.v("PERMISSION CHECK","Permission is granted");
            //File write logic here
        }

    }

    public void AddData(String newEntry){
        boolean insertData = mDatabaseHelper.addData(newEntry);
        if (insertData) {
            return;
        }
        Toast.makeText(getBaseContext(), "Unexpected Error", Toast.LENGTH_SHORT ).show();
    }


    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager activitymanager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices = activitymanager.getRunningServices(Integer.MAX_VALUE);
        int i = 0, runningServicesSize = runningServices.size();
        while (i < runningServicesSize) {
            ActivityManager.RunningServiceInfo service = runningServices.get(i);
            if (!serviceClass.getName().equals(service.service.getClassName())) {
                i++;
            } else {
                Log.i("Service status", "Running");
                return true;
            }
        }
        Log.i ("Service status", "Not running");
        return false;
    }

    @Override
    protected void onDestroy() {
        ToggleButton toggle = findViewById(R.id.protectionButton);
        if (toggle.isChecked()) {
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction("restartservice");
            broadcastIntent.setClass(this, TheRestarter.class);
            this.sendBroadcast(broadcastIntent);
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int itemId = item.getItemId();
        if (itemId == R.id.scanyourDevice) {
            Intent intent1 = new Intent(this, DeviceScanner.class);
            startActivity(intent1);
            return true;
        } else if (itemId == R.id.locatedevice) {
            Intent intent2 = new Intent(this, DeviceLocator.class);
            startActivity(intent2);
            return true;
        } else if (itemId == R.id.webprotection) {
            Intent intent4 = new Intent(this, SafeInternetBrowsing.class);
            startActivity(intent4);
            return true;
        } else if (itemId == R.id.usersetting) {
            Intent intent6 = new Intent(this, UserSetting.class);
            startActivity(intent6);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
