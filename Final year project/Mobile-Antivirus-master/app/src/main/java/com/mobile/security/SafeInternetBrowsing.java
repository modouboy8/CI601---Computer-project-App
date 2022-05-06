package com.mobile.security;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static android.view.View.VISIBLE;
import static com.mobile.security.R.id.scanyourDevice;
import static com.mobile.security.R.id.webprotection;
import static com.mobile.security.R.id.locatedevice;
import static com.mobile.security.R.id.back_arrow;
import static com.mobile.security.R.id.forward_arrow;
import static com.mobile.security.R.id.go_button;
import static com.mobile.security.R.id.home;
import static com.mobile.security.R.id.progress_bar;
import static com.mobile.security.R.id.scanyourDevice;
import static com.mobile.security.R.id.usersetting;
import static com.mobile.security.R.id.web_address_edit_text;
import static com.mobile.security.R.id.web_view;

public class SafeInternetBrowsing extends AppCompatActivity {

    // initialize variables
    WebView webView;
    EditText editText;
    ProgressBar progressedBar;
    ImageButton backarrow;
    ImageButton forwardarror;
    ImageButton stopicon;
    ImageButton refreshicon;
    ImageButton homeButton;
    Button goButton;
    ArrayList<String> FlagURL;

    {
        FlagURL = new ArrayList<>();
    }

    int token = 0;
    String loadtheURL = "";

    /**
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        // setmProgressBarIndeterminateVisibility(false);

        setContentView(R.layout.activity_web_protection);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

          // Assign variables
        editText = findViewById(web_address_edit_text);
        backarrow = findViewById(back_arrow);
        forwardarror = findViewById(forward_arrow);
        stopicon = findViewById(R.id.stop);
        goButton = findViewById(go_button);
        refreshicon = findViewById(R.id.refresh);
        homeButton = findViewById(home);
        progressedBar = findViewById(progress_bar);

        //Set the upper range of the progress bar max.
        progressedBar.setMax(100);
        //Set the visibility state of this view.
        progressedBar.setVisibility(VISIBLE);
        webView = findViewById(web_view);

        // BufferReader will be used to read from a text file(MaliciousURLlist.txt) in the asset folder within Android studio
        BufferedReader reader = null;
        try { reader = new BufferedReader(
                new InputStreamReader(getAssets().open("MaliciousURLlist.txt"), StandardCharsets.UTF_8));
            // do reading, usually loop until end of file reading
            String mLine;
            //check condition
            if (null != (mLine = reader.readLine())) {
                //process line
                do FlagURL.add(mLine); while ((mLine = reader.readLine()) != null);
            }
        } catch (IOException e) {
            switch (Log.i("Read URL Error: ", String.valueOf(e))) {
            }
            //log the exception
        } finally {
            //check condition
            if (reader == null) {
            } else {
                try {
                    reader.close();
                } catch (IOException e) {
                    switch (Log.i("Read IO Error: ", String.valueOf(e))) {
                        //log the exception
                    }
                }
            }
        }

        // If there is a previous instance restore it in the webview
        if (null == savedInstanceState) {
            // Some settings
            // Enable Javascript
            webView.getSettings().setJavaScriptEnabled(true);

            // Use WideViewport and Zoom out if there is no viewport defined
            webView.getSettings().setUseWideViewPort(true);
            //Sets whether the WebView loads pages in overview mode, that is, zooms out the content to fit on screen by width.
            webView.getSettings().setLoadWithOverviewMode(true);

            // Sets whether the WebView should support zooming using its on-screen zoom controls and gestures
            webView.getSettings().setSupportZoom(true);

            // Sets whether the WebView whether supports multiple windows
            webView.getSettings().setSupportMultipleWindows(true);
            //The scrollbar style to display the scrollbars inside the content area, without increasing the padding.
            webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
            webView.setBackgroundColor(Color.WHITE);

            webView.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onProgressChanged(WebView view, int newProgress) {
                    // tell the host application the current progress of loading a page.
                    super.onProgressChanged(view, newProgress);
                    // View would be the WebView that initiated the callback.
                    progressedBar.setProgress(newProgress);
                    // current loading progress, represented by an integer between 0 and 100.
                    if (newProgress >= 100 || progressedBar.getVisibility() != ProgressBar.GONE) {
                    } else {
                        //Set the visibility state of this view.
                        progressedBar.setVisibility(VISIBLE);
                    }
                    switch (newProgress) {
                        case 100:
                            progressedBar.setVisibility(ProgressBar.GONE);
                            break;
                        default:
                            //Set the visibility state of this view.
                            progressedBar.setVisibility(VISIBLE);
                            break;
                    }
                }
            });
        } else {
            webView.restoreState(savedInstanceState);
        }

        webView.setWebViewClient(new TheWebViewClient());
        //Register a callback to be invoked when this view is clicked
        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                token = 0;
                String tempX = editText.getText().toString();
                //check is the string starts with https://.
                if (!tempX.startsWith("https://"))
                {
                    //Tests if this string starts with https://.
                    if (tempX.startsWith("http://")) {
                        //Return the text that TextView is displaying,
                        loadtheURL = "https://" + (editText.getText().toString()).substring(7);
                        Toast.makeText(SafeInternetBrowsing.this,"auto changed to HTTPS", Toast.LENGTH_SHORT).show();
                    } else {
                        //Return the text that TextView is displaying,
                        loadtheURL = "https://" + editText.getText().toString();
                    }
                } else {
                    //Return the text that TextView is displaying,
                    loadtheURL = editText.getText().toString();
                }
                int i = 0;
                while (i < FlagURL.size()) {
                    if (!loadtheURL.equals(FlagURL.get(i))) {
                    } else {
                        token = 1;
                    }
                    i++;
                }
                try {
                    if (!TheNetworkState.connectionAvailable(SafeInternetBrowsing.this)) {
                        Toast.makeText(SafeInternetBrowsing.this, R.string.check_connection, Toast.LENGTH_SHORT).show();
                    } else {
                        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                        if (token > 0) {
                            new AlertDialog.Builder(SafeInternetBrowsing.this)
                                    .setTitle("Malicious Link Detected")
                                    .setMessage(" Are you sure you want to continue to this site?")
                                    // This will specify a listener that enable an action being taken before the dialog gets dismiss
                                    // When the dialog button gets clicked, it will automatically dismissed the dialog.
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            //Return the text that TextView is displaying.
                                            webView.loadUrl("https://" + editText.getText().toString());
                                            //Sets the text to be displayed
                                            editText.setText("");
                                        }
                                    })

                                    // The null listener enables the button to dismiss the dialog and take no further action.
                                    .setNegativeButton(android.R.string.no, null)
                                    //Set the resource id of the Drawable to be used in the title
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                        } else {
                            webView.loadUrl(loadtheURL);
                            editText.setText("");
                        }
                    }

                    //This exception is thrown by the resource APIs when a requested resource can not be found
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        //callback to be invoked when the user clicks the back icon
        backarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!webView.canGoBack()) {
                    return;
                }
                webView.goBack();
            }
        });
        //callback to be invoked when the user clicks the forward icon
        forwardarror.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check condition
                if (!webView.canGoForward())
                    return;
                webView.goForward();
            }
        });
        //callback to be invoked when the user clicks the stop icon
        stopicon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.stopLoading();
            }
        });
        //callback to be invoked when the user clicks the refresh icon.
        refreshicon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.reload();
            }
        });
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.loadUrl("https://www.google.com");
            }
        });
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

    // This displays the menu in the app.
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int itemId = item.getItemId();
        //check condition
        if (itemId == scanyourDevice) {
            Intent intent1 = new Intent(this, DeviceScanner.class);
            startActivity(intent1);
            return true;
        } else if (itemId == locatedevice) {
            Intent intent2 = new Intent(this, DeviceLocator.class);
            startActivity(intent2);
            return true;
        } else if (itemId == webprotection) {
            Intent intent4 = new Intent(this, SafeInternetBrowsing.class);
            startActivity(intent4);
            return true;
        } else if (itemId == usersetting) {
            Intent intent6 = new Intent(this, UserSetting.class);
            startActivity(intent6);
            return true;
        }
        //This hook is called whenever an item in your options menu is selected
        return super.onOptionsItemSelected(item);
    }

}