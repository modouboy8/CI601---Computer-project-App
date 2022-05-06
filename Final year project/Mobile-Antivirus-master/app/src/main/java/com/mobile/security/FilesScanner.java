package com.mobile.security;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class FilesScanner extends AppCompatActivity {

    // initialize variables
    ArrayAdapter<String> adapter;
    ArrayList<String> appNames;
    Collection<PackageInfo> packList;
    String serverHash, token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files_scanner);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        packList = getPackageManager().getInstalledPackages(0);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        ArrayList<String> exclusion;
        exclusion = new ArrayList<>();
        exclusion.add("Photos");
        exclusion.add("Drive");
        exclusion.add("Calendar");
        exclusion.add("Google");
        exclusion.add("Youtube");
        exclusion.add("com.android.sharedstoragebackup");
        exclusion.add("Google Play services");
        exclusion.add("com.android.carrierconfig");
        exclusion.add("com.android.wallpaperbackup");
        exclusion.add("com.google.android.overlay.emulatorconfig");
        exclusion.add("com.android.server.NetworkPermissionConfig");
        exclusion.add("com.android.emulator.radio.config");
        exclusion.add("com.android.cellbroadcastreceiver");
        exclusion.add("com.android.providers.media");
        exclusion.add("com.android.ons");
        exclusion.add("com.google.android.overlay.permissioncontroller");
        exclusion.add("com.android.systemui.plugin.globalactions.wallet");
        exclusion.add("com.android.localtransport");
        exclusion.add("com.android.backupconfirm");
        exclusion.add("com.google.android.sdksetup");
        exclusion.add("Gboard");
        exclusion.add("com.android.service.ims.RcsServiceApp");

        //show progress dialog
        final ProgressBar progressSpin = (ProgressBar) findViewById(R.id.progressBar);
        progressSpin.setVisibility(View.GONE);

        PackageManager packageManager = getPackageManager();
        //find View by id
        ListView listView = findViewById(R.id.apps_list);
        final List<String> sourceDir = new ArrayList<>();
        appNames = new ArrayList<>();
        //get a list of installed apps on the device.
        List<ApplicationInfo> packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<String>());
        // add all the app name in string list
        int i = 0;
        while (i < packages.size()) {
            ApplicationInfo packageInfo = packages.get(i);
            String appName = (String) packageInfo.loadLabel(packageManager);

            // create a list with size of total number of apps
            token = "0";
            int y = 0;
            while (y < exclusion.size()) {
                //check condition
                if (appName.equalsIgnoreCase(exclusion.get(y))) {
                    token = "1";
                }
                y++;
            }
            if (token.equals("1")) {
                i++;
                continue;
            }
            adapter.add(appName);
            appNames.add(appName);
            sourceDir.add(packageInfo.sourceDir);
            i++;
        }
        // set all the apps name in list view
        listView.setAdapter(adapter);

        //check the database
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                progressSpin.setVisibility(View.VISIBLE);
                String appName = appNames.get(position);
                //check condition
                if (!appName.contains(".")) {
                } else {
                    appName.replace(".", "-");
                }
                // converting byte to string
    // hash given byte array using SHA1Hash algorithm.
    // return the hash of an algortihm
                DatabaseReference databaseDH;
                databaseDH = database.getReference("DexHashes").child(appName);
                String s = sourceDir.get(position);
                byte[] b = getFile(s);
                byte[] dex = unzipDex(b, s);
                String byteArray = Arrays.toString(dex);
                final String computedHash = sha1Hash(byteArray);
                Log.d(appName, sha1Hash(byteArray));


                databaseDH.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        serverHash = dataSnapshot.getValue(String.class);
                        // create a new Looper Handler
                        final Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                progressSpin.setVisibility(View.GONE);
                                AlertDialog.Builder builder;
                                try {
                                    if (serverHash.equals(computedHash)) {
                                        // creating a AlertDailog.
                                        // choose the theme
                                        builder = new AlertDialog.Builder(FilesScanner.this, R.style.AlertDialogStyle);
                                        final AlertDialog.Builder hash_matches = builder.setTitle("Hash Matches");
                                        builder.setMessage("Malicious Package!");
                                        builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                            }
                                        });
                                    } else {
                                        // create a new AlertDialog
                                        // choose the theme
                                        builder = new AlertDialog.Builder(FilesScanner.this, R.style.PassDialogStyle);
                                        builder.setTitle("Hash Unmatched");
                                        builder.setMessage("This package is safe to keep.");
                                        builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                            }
                                        });
                                    }
                                    builder.show();

                                } catch (Exception exception) {
                                    Log.e(serverHash, String.valueOf(exception));
                                    builder = new AlertDialog.Builder(FilesScanner.this, R.style.AlertDialogStyle);
                                    builder.setTitle("No Data Found");
                                    builder.setMessage("Please take precautions NO data is found");
                                    builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    });
                                    builder.show();

                                }
                            }
                        }, 3000);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        AlertDialog.Builder builder;
                        builder = new AlertDialog.Builder(FilesScanner.this, R.style.AlertDialogStyle);
                        builder.setTitle("No Data");
                        builder.setMessage("No data found. Take precautions.");
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
        });

    }

    // hashing strings with SHA256 algorithm in java.
    String sha1Hash(String toHash){
        String hash = null;
        try{
            //Create a MessageDigest object for SHA256 algorithm
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            // convert the string into bytes
            byte[] bytes;
            bytes = toHash.getBytes(StandardCharsets.UTF_8);
            // Update input string in message digest
            digest.update(bytes, 0, bytes.length);
            bytes = digest.digest();
            hash = bytesToHex(bytes);
        }
        catch(NoSuchAlgorithmException e){
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return hash;
    }

    // converting a byte array to hex string
    // output size will be exactly "0123456789ABCDEF" byte array lenght.
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes){
        char[] hexChars = new char[bytes.length*2];
        int j=0;
        //
        while (j<bytes.length) {
            int v = bytes[j] & 0xFF;
            hexChars[j*2] = hexArray[v>>>4];
            hexChars[j*2+1] = hexArray[v&0x0F];
            j++;
        }
        return new String(hexChars);
    }

// create a random access file.
// This will provide a way to access files using reading and writing operations.
    byte[] getFile(String filename){
        try {
            // create a random access file stream to read/write from.
            RandomAccessFile f = new RandomAccessFile(filename, "r");
            byte[] b = new byte[(int)f.length()];
            f.readFully(b);
            return b;
        } catch(IOException exception){
            exception.printStackTrace();
        }
        return null;
    }

    // unzip a zip file
    public byte[] unzipDex(byte[] bytes, String filename){
        try{
            // create a new zipped file
            ZipFile zipFile = new ZipFile(filename);
            ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(bytes));
            ZipEntry ze = zis.getNextEntry();
            if (ze != null) {
                do {
                    String entryName = ze.getName();
                    if (!entryName.equals("classes.dex")) {
                        ze = zis.getNextEntry();
                        continue;
                    }
                    InputStream is = zipFile.getInputStream(ze);
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    int nRead;
                    byte[] data = new byte[16384];
                    //16384
                    if ((nRead = is.read(data, 0, data.length)) != -1) {
                        do {
                            buffer.write(data, 0, nRead);
                        } while ((nRead = is.read(data, 0, data.length)) != -1);
                    }
                    buffer.flush();
                    return buffer.toByteArray();
                } while (ze != null);
            }
        }catch(IOException ex){
            ex.printStackTrace();
        }
        return null;
    }
}