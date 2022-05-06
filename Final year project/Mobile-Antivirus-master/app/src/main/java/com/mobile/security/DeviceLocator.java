package com.mobile.security;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mobile.security.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class DeviceLocator extends AppCompatActivity {

    private static final int REQUEST_CODE;

    static {
        REQUEST_CODE = 2;
    }

    LoginDatabase mDatabaseHelper;
    private MapView mapStreetView;
    private static final String SOURCE_ID;

    static {
        SOURCE_ID = "SOURCE_ID";
    }

    private static final String ICON_ID;

    static {
        ICON_ID = "ICON_ID";
    }

    private static final String LAYER_ID;

    static {
        LAYER_ID = "LAYER_ID";
    }

    private EditText edittextLocation = null;
    private ProgressBar progressbar;
    private DatabaseReference dbLong;
    private DatabaseReference dbLat;
    String data1;
    String LOGIN_STATUS;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        //Get an instance of Mapbox.
        Mapbox.getInstance(DeviceLocator.this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_device_locator);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        //Assigns variables
        edittextLocation = findViewById(R.id.editTextLocation);
        progressbar = findViewById(R.id.progressBar1);
        //Finds a view that was identified by the android
        mapStreetView = findViewById(R.id.mapView);
        //Set the visibility state of this view.
        progressbar.setVisibility(View.INVISIBLE);

        switch (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            case PackageManager.PERMISSION_GRANTED:
                Log.v("PERMISSION CHECK", "Permission is granted");
                //File write logic here
                break;
            default:
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
                break;
        }
        // login
        mDatabaseHelper = new LoginDatabase(this);
        Cursor cursor = mDatabaseHelper.getData();
        switch (cursor.getCount()) {
            case 0:
                LOGIN_STATUS = "N";
                break;
            default:
                while (cursor.moveToNext()) {
                    data1 = cursor.getString(1);
                    LOGIN_STATUS = data1.length() >= 3 ? "Y" : "N";
                }
                break;
        }

        dbLong = FirebaseDatabase.getInstance().getReference(data1).child("Longitude");
        dbLat = FirebaseDatabase.getInstance().getReference(data1).child("Latitude");

        //provides access to the system location services
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Boolean flag = showGpsStatus();
        //check condition
        if (!flag) {
            alertbox("Gps Status!!", "Your GPS is: OFF");
        } else {
            edittextLocation.setGravity(Gravity.CENTER);
            edittextLocation.setText("Locating Your Device...");
            progressbar.setVisibility(View.VISIBLE);
            LocationListener locationListener = new MyLocationListener();

            locationManager.requestLocationUpdates(LocationManager
                    .GPS_PROVIDER, 5000, 10, locationListener);
        }
    }

    /**
     * @return
     */
    //Check GPS ON/OFF
    private Boolean showGpsStatus() {
        //initialize Content Resolver
        ContentResolver contentResolver = getBaseContext().getContentResolver();
        boolean gpsStatus = Settings.Secure.isLocationProviderEnabled(contentResolver, LocationManager.GPS_PROVIDER);
        //return the GPS status on or off.
        return gpsStatus;
    }

    /**
     * @param title
     * @param mymessage
     */
    //disable gps
    protected void alertbox(String title, String mymessage) {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        AlertDialog.Builder builder1 = builder.setMessage("Your Device's GPS is Disable").setCancelable(false).setTitle("** Gps Status **").setPositiveButton("Gps On",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // finish the current activity
                        Intent myIntent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
                        startActivity(myIntent);
                        dialog.cancel();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // cancel the dialog box
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /*----------Listener class to get coordinates ------------- */
    private class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(final Location loc) {
            //pb.setVisibility(View.INVISIBLE);
            progressbar.setVisibility(View.INVISIBLE);
            //editLocation.setText
            edittextLocation.setText("");
            edittextLocation.setGravity(Gravity.LEFT);
            edittextLocation.setTextColor(getResources().getColor(R.color.white));
            String longitude = "Longitude: " +loc.getLongitude();
            //Log.v(TAG, longitude);
            String latitude  = "Latitude : " +loc.getLatitude();
            //Log.v(TAG, latitude);
            final double longtt = loc.getLongitude();
            final double latt = loc.getLatitude();
            dbLong.setValue(longtt);
            dbLat.setValue(latt);

            //city coordinates
            String cityName = "Null";
            //initialize geoCoder
            Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
            try
            {
                //Initialize address list
                List<Address> addresses = gcd.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
                //set Latitude

                //check condition
                if (addresses.size() > 0) {
                    System.out.println(addresses.get(0).getLocality());
                    cityName = addresses.get(0).getLocality();
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }

            String s = longitude+"\n"+latitude +"\n\n : ";
            edittextLocation.setText(s);

            // map camera
            final CameraPosition position;
            position = new CameraPosition.Builder()
                    .target(new LatLng(latt, longtt)).zoom(12).tilt(20).build();

            mapStreetView.getMapAsync(new OnMapReadyCallback() {
                /**
                 * @param mapboxMap
                 */
                @Override
                public void onMapReady(@NonNull final MapboxMap mapboxMap) {
                    //Symbol Layer
                    Collection<Feature> FeatureListsymbolLayerIcon;
                    FeatureListsymbolLayerIcon = new ArrayList<>();
                    FeatureListsymbolLayerIcon.add(Feature.fromGeometry(Point.fromLngLat(longtt, latt)));
                    Objects.requireNonNull(mapboxMap).setStyle(new Style.Builder().fromUri("mapbox://styles/mapbox/cjf4m44iw0uza2spb3q0a7s41")
                            .withImage(ICON_ID, BitmapFactory.decodeResource(DeviceLocator.this.getResources(), R.drawable.mapbox_marker_icon_default))
                            .withSource(new GeoJsonSource(SOURCE_ID, FeatureCollection.fromFeatures((List<Feature>) FeatureListsymbolLayerIcon)))
                            .withLayer(new SymbolLayer(LAYER_ID, SOURCE_ID).withProperties(iconImage(ICON_ID), iconAllowOverlap(true),
                                    iconIgnorePlacement(true)
                                    )
                            ), new Style.OnStyleLoaded() {
                        @Override
                        public void onStyleLoaded(@NonNull Style style) {
                            // Map is set up and the style has loaded. Now you can add data or make other map adjustments
                            mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 10);
                        }
                    });
                }
            });
        }

        @Override
        public void onProviderDisabled(String provider) {
            /* TODO Auto-generated method stub */
        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        // Inflate the menu; this adds items to the action bar if it is present.
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