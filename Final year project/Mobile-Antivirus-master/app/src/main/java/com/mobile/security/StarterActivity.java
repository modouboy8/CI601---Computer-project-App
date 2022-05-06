package com.mobile.security;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mobile.security.R;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;

public class StarterActivity extends AppCompatActivity {

    // initialize variables
    LoginDatabase mDatabaseHelper;
    EditText USERID, USERPASSWORD;
    Button USERLOGIN, SKIPLOGIN;
    String LOGIN_STATUS, data1,data2, fbdata1, fbdata2;
    int token = 0;
    SimpleDateFormat s;

    {
        s = new SimpleDateFormat("yyyyMMddhhmmss");
    }

    DateTimeFormatter inputFormatter;

    {
        inputFormatter = DateTimeFormat.forPattern("yyyyMMddHHmmss").withZone(DateTimeZone.UTC);
    }

    String timestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_starter);
        getSupportActionBar().hide();

        //Assign variables
        USERID = findViewById(R.id.USERID);
        USERPASSWORD = findViewById(R.id.PASSWORD);
        USERLOGIN = findViewById(R.id.LOGIN);
        SKIPLOGIN = findViewById(R.id.SKIP);
        // Set the visibility state of this view
        USERID.setVisibility(View.GONE);
        USERPASSWORD.setVisibility(View.GONE);
        USERLOGIN.setVisibility(View.GONE);
        SKIPLOGIN.setVisibility(View.GONE);
        //Sets the list of input filters that will be used if the buffer is Editable.
        USERID.setFilters(new InputFilter[]
                {new InputFilter.AllCaps()});

        final FirebaseDatabase database;
        database = FirebaseDatabase.getInstance();
        timestamp = s.format(new Date());
        DateTime parsed = inputFormatter.parseDateTime(timestamp);
        //DateTimeFormatter outputFormatter = DateTimeFormat.forPattern("yyyyMMddHHmmss").withZone(DateTimeZone.forID("Asia/Kuala_Lumpur"));
        DateTimeFormatter outputFormatter = DateTimeFormat.forPattern("yyyyMMddHHmmss");
        timestamp = outputFormatter.print(parsed);

        //login process
        mDatabaseHelper = new LoginDatabase(this);
        Cursor cursor = mDatabaseHelper.getData();
        switch (cursor.getCount()) {
            case 0:
                LOGIN_STATUS = "N";
                break;
            default:
                while (cursor.moveToNext()) {
                    data1 = cursor.getString(1);
                    data2 = cursor.getString(2);
                    if (data1.length() < 3) {
                        LOGIN_STATUS = "N";
                    } else {
                        LOGIN_STATUS = "Y";
                        DatabaseReference dbLL = database.getReference(data1).child("LAST_LOGIN");
                        DatabaseReference dbLS = database.getReference(data1).child("LOGIN_STATUS");
                        dbLL.setValue(timestamp);
                        dbLS.setValue("Y");
                    }
                }
                break;
        }


        // if login equals to yes, open the DeviceScanner screen.
        if (LOGIN_STATUS.equalsIgnoreCase("Y"))
            startActivity(new Intent(StarterActivity.this, DeviceScanner.class));
        else {
            //Set the visibility state of this view.
            USERLOGIN.setVisibility(View.VISIBLE);
            SKIPLOGIN.setVisibility(View.VISIBLE);
            USERID.setVisibility(View.VISIBLE);
            USERPASSWORD.setVisibility(View.VISIBLE);


            USERLOGIN.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v)  {
                    final String in;
                    in = USERID.getText().toString();
                    final String password = USERPASSWORD.getText().toString();
                   // retrieve the username path location in my firebase database.
                    DatabaseReference databaseID;
                    databaseID = database.getReference(in).child("username");
                    // retrieve the password path location in my firebase database.
                    DatabaseReference databasepassword;
                    databasepassword = database.getReference(in).child("password");
                    final ValueEventListener invalid_username = databaseID.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            fbdata1 = dataSnapshot.getValue(String.class);
                            if (fbdata1 == null) {
                                Toast.makeText(StarterActivity.this, "INVALID USERNAME", Toast.LENGTH_SHORT).show();
                            } else {
                                token = 1;
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            // Failed to read value
                            Log.i("FIREBASE: ", "Failed to read value.", error.toException());
                        }
                    });
                    switch (token) {
                        case 1:
                            databasepassword.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    fbdata2 = dataSnapshot.getValue(String.class);
                                    if (!fbdata2.equals(password)) {
                                        Toast.makeText(StarterActivity.this, "INVALID PASSWORD", Toast.LENGTH_SHORT).show();
                                    } else {
                                        token = 2;
                                        //retrieve the last login details.
                                        DatabaseReference databaselastlogin = database.getReference(data1).child("LAST_LOGIN");
                                        DatabaseReference databaseloginstatus = database.getReference(data1).child("LOGIN_STATUS");
                                        databaselastlogin.setValue(timestamp);
                                        databaseloginstatus.setValue("Y");
                                        Toast.makeText(StarterActivity.this, "LOGGED IN", Toast.LENGTH_LONG).show();
                                        //open DeviceScanner Activity.
                                        startActivity(new Intent(StarterActivity.this, DeviceScanner.class));
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError error) {
                                    // Failed to read value
                                    Log.i("FIREBASE: ", "Failed to read value.", error.toException());
                                }
                            });
                            break;
                    }

                }
            });

            //skip the login process, implemented for guest users.
            SKIPLOGIN.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v)  {
                    LOGIN_STATUS = "N";
                    startActivity(new Intent(StarterActivity.this, DeviceScanner.class));
                }
            });

        }

    }
}