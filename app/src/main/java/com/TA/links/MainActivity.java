package com.TA.links;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.Menu;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import com.TA.links.databinding.ActivityMainBinding;
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
    //String fileUrl = "https://drive.google.com/uc?export=download&id=1E4JAx8G3AbcbCdTy77oitn6gVMotPr_c";
    String fileUrl="";

    private Bundle webViewState;
    static String currentUrl="";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.TA.links.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);

        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_WRITE_STORAGE
        );

        loadUrl();

        new DownloadFileTask().execute(fileUrl);

        File file = new File(getFilesDir(), "index.html");
        StringBuilder content = new StringBuilder();

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

        webView = findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                currentUrl=url;
                Log.d("mytag", "shouldOverrideUrlLoading: "+currentUrl);
                return false;
            }
        });
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setBackgroundColor(Color.TRANSPARENT);

        if (file.exists()) {
            webView.loadDataWithBaseURL(null, fileContent, "text/html", "UTF-8", null);
            Log.d("mytag", "loadDataWithBaseURL");
        } else {
            webView.loadUrl("about:blank");
            Log.d("mytag", "File does not exist: " + file.getAbsolutePath());
        }

        binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (file.exists()) {
                    webView.loadDataWithBaseURL(null, fileContent, "text/html", "UTF-8", null);
                } else {
                    Log.d("mytag", "File does not exist: " + file.getAbsolutePath());
                }
            }
        });
    }

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
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

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
        if (fileUrl == null || fileUrl.isEmpty()) {
            Log.d("mytag", "Invalid file URL");
            return;
        }

        HttpTransport httpTransport = new NetHttpTransport();
        HttpResponse response = httpTransport.createRequestFactory().buildGetRequest(new GenericUrl(fileUrl)).execute();

        File file = new File(context.getFilesDir(), "index.html");
        try (OutputStream outputStream = new FileOutputStream(file)) {
            IOUtils.copy(response.getContent(), outputStream);
        }
        Log.d("mytag", "File downloaded: " + file.getAbsolutePath());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingActivity.class);
            startActivity(intent);
            Log.d("mytag", "setting");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void loadUrl() {
        try {
            FileInputStream fis = openFileInput("setting.txt");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            String line;
            int i = 0;
            while ((line = br.readLine()) != null) {
                if (i == 1) {
                    fileUrl = line;
                }
                i++;
            }
            br.close();
            isr.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
