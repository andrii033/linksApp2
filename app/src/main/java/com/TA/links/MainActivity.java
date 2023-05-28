package com.TA.links;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.Menu;
import android.view.View;
import android.webkit.WebBackForwardList;
import android.webkit.WebHistoryItem;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import com.TA.links.databinding.ActivityMainBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.IOUtils;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private static final String WEBVIEW_STATE_KEY = "webview_state_key";
    private static final int REQUEST_WRITE_STORAGE = 1;
    String fileUrl = "https://drive.google.com/uc?export=download&id=1E4JAx8G3AbcbCdTy77oitn6gVMotPr_c";
    private Bundle webViewState;
    static String currentUrl="";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //private AppBarConfiguration mAppBarConfiguration;
        com.TA.links.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);


        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_WRITE_STORAGE
        );

//        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                1);

        // Start the download task
        new DownloadFileTask().execute(fileUrl);

        // Assuming the file is downloaded and saved at "/data/data/com.TA.links/files/index.html"
        File file = new File(getFilesDir(), "index.html");
        //Log.d("mytag", "File path: "+file.getAbsolutePath());
        //Log.d("mytag", "File dir: "+getFilesDir());

        StringBuilder content = new StringBuilder();

        //Log.d("mytag", "File: "+file);

        try {
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);

            String line;
            while ((line = br.readLine()) != null) {
                content.append(line);
            }

            br.close();
            isr.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String fileContent = content.toString();
        //Log.d("mytag", "File content: "+fileContent);

        webView = findViewById(R.id.webView);
        //webView.setWebViewClient(new WebViewClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                currentUrl=url;
                Log.d("mytag", "shouldOverrideUrlLoading: "+currentUrl);
                return false;
            }
        });
        webView.getSettings().setJavaScriptEnabled(true); // enable javascript
        webView.setBackgroundColor(Color.TRANSPARENT);

        if(currentUrl.equals("")) {
            webView.loadDataWithBaseURL(null, fileContent, "text/html", "UTF-8", null);
            Log.d("mytag", "loadDataWithBaseURL");
        }
        else {
            webView.loadUrl(currentUrl);
            Log.d("mytag", "loadUrl");
        }

        binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webView.loadDataWithBaseURL(null, fileContent, "text/html", "UTF-8", null);
            }
        });


    }//onCreate

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        webView.loadUrl(currentUrl);
    }

    @Override
    public void onBackPressed() {
        // Check if the WebView can go back to the previous page
        if (webView.canGoBack()) {
            webView.goBack(); // Go back to the previous page
        } else {
            super.onBackPressed(); // Allow the default back button behavior (exit the app)
        }
    }


    // AsyncTask for downloading the file in the background
    private class DownloadFileTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... urls) {
            String fileUrl = urls[0];

            try {
                downloadFile(MainActivity.this, fileUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // File download completed, you can proceed with further operations
        }
    }

    public static void downloadFile(Context context, String fileUrl) throws IOException {
        HttpTransport httpTransport = new NetHttpTransport();
        HttpResponse response = httpTransport.createRequestFactory().buildGetRequest(new GenericUrl(fileUrl)).execute();

        File file = new File(context.getFilesDir(), "index.html");
        try (OutputStream outputStream = new FileOutputStream(file)) {
            IOUtils.copy(response.getContent(), outputStream);
        }
        //Log.d("mytag", "File downloaded: " + file.getAbsolutePath());
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            // Handle the click on the "Settings" item
            //openSettingsActivity(); // Replace with your desired action
            Log.d("mytag", "setting");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}