package com.mobile.security;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class SignatureTracking extends AppCompatActivity {

    // initialize variables
    private PackageManager packageManager;
    private ArrayList<String> packageNames;
    private ArrayList<String> Signaturedatabase;
    private ArrayAdapter<String> adapter;
    private ListView listView;
    private String token;
    private String signatureBase256;
    private String Signaturelist;

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        setContentView(R.layout.activity_signature_tracking);
        //Assigns variables
        listView = (ListView) findViewById(R.id.listView1);

        packageManager = getPackageManager();
        // initialize the adapter
        //find View by id
        adapter = new ArrayAdapter<>(this, R.layout.list_app_control, new ArrayList<String>());
        // Attach the data to the list view.
        packageNames = new ArrayList<>();
        // query the package manager for all apps.
        List<ResolveInfo> activities;
        activities = packageManager.queryIntentActivities(new Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER), 0);
        //Sort the applications by alphabetical order and add them to the list.
        Collections.sort(activities, new ResolveInfo.DisplayNameComparator(packageManager));

        // BufferReader will be used to read from a text file(VirusSignatureHashDatabase.txt) in the asset folder within Android studio
        BufferedReader reader;
        Signaturedatabase = new ArrayList<>();
        try {
            reader = new BufferedReader(
                    new InputStreamReader(getAssets().open("VirusSignatureHashDatabase.txt"), "UTF-8"));
            // do reading, usually loop until end of file reading
            String mLine;
            //check condition
            if ((mLine = reader.readLine()) == null) {
            } else {
                do {
                    Signaturedatabase.add(mLine);
                } while ((mLine = reader.readLine()) != null);
            }
        } catch (IOException e) {
            Log.i("Read URL Error: ", String.valueOf(e));
            //log the exception
        }

        // this will get the signature from the apk file.
        Iterator<ResolveInfo> iterator = activities.iterator();
        label1:
        do {
            if (!iterator.hasNext()) break;
            ResolveInfo resolverinfo = iterator.next();
            String appName = (String) resolverinfo.loadLabel(packageManager);
            String tempPN = resolverinfo.activityInfo.packageName;
            try {
                final PackageInfo packageInfo = packageManager.getPackageInfo(tempPN, PackageManager.GET_SIGNING_CERTIFICATES);
                final Signature[] signatures = packageInfo.signingInfo.getApkContentsSigners();
                final MessageDigest md = MessageDigest.getInstance("SHA-256");
                int i = 0;
                while (i < signatures.length) {
                    Signature signature = signatures[i];
                    md.update(signature.toByteArray());
                    signatureBase256 = new String(Base64.encode(md.digest(), Base64.DEFAULT));
                    i++;
                }
                token = "0";
                int q = 0;
                label2:
                while (q < Signaturedatabase.size()) {
                    String tempSDB = Signaturedatabase.get(q);
                    String temp256 = signatureBase256.substring(0, 44);
                    if (!tempSDB.equals(temp256)) {
                        q++;
                    } else {
                        token = "1";
                        q++;
                        continue label2;
                    }
                }
                switch (token) {
                    case "0":
                        adapter.add(appName);
                        packageNames.add(resolverinfo.activityInfo.packageName);
                        continue label1;
                }
            } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

        } while (true);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String signature256 = packageNames.get(position);
                final PackageInfo packageInfo;
                try {
                    // PackageInfoGET_SIGNING_CERTIFICATES
                    // Retrieve overall information about an application package installed on the device.
                    packageInfo = packageManager.getPackageInfo(signature256, PackageManager.GET_SIGNING_CERTIFICATES);
                    //get the certificate associated with the application package.
                    final Signature[] signatures;
                    signatures = packageInfo.signingInfo.getApkContentsSigners();
                    // SHA1 the signature
                    final MessageDigest md = MessageDigest.getInstance("SHA-256");
                    int i = 0;
                    while (i < signatures.length) {
                        Signature signature = signatures[i];
                        md.update(signature.toByteArray());
                        Signaturelist = new String(Base64.encode(md.digest(), Base64.DEFAULT));
                        i++;
                    }
                    Log.e("SHA-256",Signaturelist);
                } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(SignatureTracking.this, R.style.AlertDialogStyle);
                builder.setTitle("Unrecognized Signature");
                builder.setMessage("SHA-256:\n"+Signaturelist);
                builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    // This displays the menu in the app.
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
        //This hook is called whenever an item in your options menu is selected
        return super.onOptionsItemSelected(item);
    }
}