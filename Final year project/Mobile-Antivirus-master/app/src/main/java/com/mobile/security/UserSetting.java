package com.mobile.security;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mobile.security.R;

public class UserSetting extends AppCompatActivity {

    //Assign variables
    LoginDatabase mDatabaseHelper;
    Button LOGOUT;
    Button CHANGEPASSWORD;
    String LOGIN_STATUS;
    String lastuserlogin;
    String data1;
    String data2;
    String password;
    String oldpassword;
    String newpassword;
    String newpassword2;
    TextView lastlogin;
    EditText oldpasswordtv;
    EditText newpasswordtv;
    EditText newpasswordtv2;
    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference databaseLastLogin,databasePassword;

    /**
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_setting);

        //assign variables
        LOGOUT = findViewById(R.id.LOGOUT);
        CHANGEPASSWORD = findViewById(R.id.change);

        // login process
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
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
                    LOGIN_STATUS = data1.length() >= 3 ? "N" : "Y";
                }
                break;
        }

        // logout process
        LOGOUT.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                LOGIN_STATUS = "N";
                AddData("N","N");
                DatabaseReference dbLS;
                dbLS = database.getReference(data1).child("LOGIN_STATUS");
                dbLS.setValue("N");
                Toast.makeText(UserSetting.this,"LOGGED OUT", Toast.LENGTH_LONG).show();
                startActivity(new Intent(UserSetting.this, StarterActivity.class));
            }
        });

        //last login
        databaseLastLogin = database.getReference(data1).child("LAST_LOGIN");
        databasePassword = database.getReference(data1).child("PASSWORD");
        databaseLastLogin.addValueEventListener(new ValueEventListener() {
            /**
             * @param dataSnapshot
             */
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                lastuserlogin = dataSnapshot.getValue(String.class);
                lastlogin = findViewById(R.id.lastlogin);
                String year;
                year = lastuserlogin.substring(0,4);
                String month;
                month = lastuserlogin.substring(4,6);
                String day;
                day = lastuserlogin.substring(6,8);
                String hour;
                hour = lastuserlogin.substring(8,10);
                String minute;
                minute = lastuserlogin.substring(10,12);
                lastlogin.setText(year+"/"+month+"/"+day+" "+hour+":"+minute+":");
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.i("FIREBASE: ", "Failed to read value.", error.toException());
            }
        });





        final ValueEventListener valueEventListener1 = databasePassword.addValueEventListener(new ValueEventListener() {
            /**
             * @param dataSnapshot
             */
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                password = dataSnapshot.getValue(String.class);
            }

            /**
             * @param error
             */
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.i("FIREBASE: ", "Failed to read value.", error.toException());
            }
        });

        // change password
        CHANGEPASSWORD.setOnClickListener(new View.OnClickListener() {
            /**
             * @param v
             */
            public void onClick(View v) {
                AlertDialog.Builder builder;
                builder = new AlertDialog.Builder(UserSetting.this);
                LayoutInflater layoutInflater;
                layoutInflater = UserSetting.this.getLayoutInflater();
                builder.setTitle("Change Password");
                builder.setView(R.layout.change_password);
                View container = layoutInflater.inflate(R.layout.change_password, null);
                builder.setView(container);
                oldpasswordtv = container.findViewById(R.id.oldpw);
                newpasswordtv = container.findViewById(R.id.newpw);
                newpasswordtv2 = container.findViewById(R.id.newpw2);
                builder.setPositiveButton("Change", new DialogInterface.OnClickListener() {
                    /**
                     * @param dialog
                     * @param which
                     */
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        oldpassword = oldpasswordtv.getText().toString();
                        newpassword = newpasswordtv.getText().toString();
                        newpassword2 = newpasswordtv2.getText().toString();
                        if (oldpassword.equalsIgnoreCase(password)) {
                            if (newpassword.equalsIgnoreCase(newpassword2)) {
                                databasePassword.setValue(newpassword);
                                Toast.makeText(UserSetting.this,"Password Changed!", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(UserSetting.this,"New Password not match", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(UserSetting.this, "Invalid Old Password", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
                builder.create();
            }
        });

    }

    /**
     * @param newEntry1
     * @param newEntry2
     */
    public void AddData(String newEntry1, String newEntry2){
        boolean insertData = mDatabaseHelper.addData(newEntry1, newEntry2);
    }


    /**
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    /**
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int itemId = item.getItemId();
        //check condition
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