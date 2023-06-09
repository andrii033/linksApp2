package com.TA.links;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.Menu;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.TA.links.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private WebView webView, navWebView;
    private static final int REQUEST_WRITE_STORAGE = 1;
    private boolean isNavViewOpen = false;
    String fileUrl = "";
    ArrayList<String> urlsList = new ArrayList<String>();
    public static boolean isFirstStart = true;

    private Map<String,String> map = new HashMap<>();
    String startString = "To use this program, follow these steps:\n" +
            "\n" +
            "    Set up the project:\n" +
            "        Create a new Android project in your preferred development environment (e.g., Android Studio).\n" +
            "        Replace the contents of the MainActivity.java file with the provided code.\n" +
            "        Make sure to add the necessary permissions in your AndroidManifest.xml file. In this case, the code requests the WRITE_EXTERNAL_STORAGE permission.\n" +
            "        Replace the layout XML file (activity_main.xml) with your desired layout or modify it to match your needs.\n" +
            "        If required, create additional layout XML files for the navigation drawer and other UI elements.";


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.TA.links.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavigationView navigationView = findViewById(R.id.nav_view);
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        navWebView = findViewById(R.id.nav_webview);


        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                isNavViewOpen = slideOffset > 0;
            }
        });


        setSupportActionBar(binding.appBarMain.toolbar);

        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_WRITE_STORAGE
        );

        loadUrl();

        map.put("http://192.168.0.121/index.html","192.168.0.121" );
        map.put("http://192.168.1.156/index.html","192.168.1.156" );
        map.put("https://www.google.com/","google");


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
                webView.loadUrl(url);
                //hide navigation view
                //drawerLayout.closeDrawer(GravityCompat.START);
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                String name = null, password = null;
                try {
                    String data = "";
                    FileInputStream fis = openFileInput("setting.txt");
                    InputStreamReader isr = new InputStreamReader(fis);
                    BufferedReader br = new BufferedReader(isr);
                    String line;
                    int i = 0;
                    while ((line = br.readLine()) != null) {
                        if (i == 0) {

                        }
                        if (i == 1) {
                            name = line;
                        }
                        if (i == 2) {
                            password = line;
                        }
                        i++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                for (String x : urlsList) {
                    if (webView.getUrl().equals(x)) {
                        String javascriptCode = "javascript:document.getElementById('username').value = '" + name + "';" +
                                "document.getElementById('password').value = '" + password + "';";
                        view.evaluateJavascript(javascriptCode, null);
                    }
                }

            }
        });

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setBackgroundColor(Color.TRANSPARENT);

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
            Log.d("mytag", "WebView state restored");
        } else {

        }


        navWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();

                webView.loadUrl(url);
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
        navWebView.getSettings().setJavaScriptEnabled(true);
        navWebView.setBackgroundColor(Color.TRANSPARENT);

        //load navigation view
        if (file.exists()) {
            navWebView.loadDataWithBaseURL(null, fileContent, "text/html", "UTF-8", null);
            Log.d("mytag", "File loaded into NavigationView WebView");
        } else {
            navWebView.loadUrl("about:blank");
            Log.d("mytag", "File does not exist: " + file.getAbsolutePath());
        }

        ImageButton imageButton = findViewById(R.id.menuButton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });


        if (isFirstStart) {
            new DownloadFileTask().execute(fileUrl);
            drawerLayout.openDrawer(GravityCompat.START);

            //webView.loadDataWithBaseURL(null, fileContent, "text/html", "UTF-8", null);
            //webView.loadUrl("file:///android_asset/index.html");
            webView.loadData(startString, "text/html", "UTF-8");
            isFirstStart = false;
        }

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
        Log.d("mytag", "onSaveInstanceState");
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
            Log.d("mytag", "File URL: " + fileUrl);

            try {
                downloadFile(MainActivity.this, fileUrl);
                Log.d("mytag", "File downloaded");
            } catch (IOException e) {
                Log.d("mytag", "File download error");
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
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
            Log.d("mytag", "File content: " + fileContent);

            //webView.loadDataWithBaseURL(null, fileContent, "text/html", "UTF-8", null);
            //navWebView.loadDataWithBaseURL(null, fileContent, "text/html", "UTF-8", null);
            if (file.exists()) {
                navWebView.loadDataWithBaseURL(null, fileContent, "text/html", "UTF-8", null);
            } else {
                StringBuilder urlsContent = new StringBuilder();
                urlsContent.append("<br>");
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    String url = entry.getKey();
                    String name = entry.getValue();

                    urlsContent.append("<a href=\"" + url + "\">" + name + "</a><br><br>");
                    Log.d("mytag", "urlsContent: " + urlsContent);
                }


                String urlsHtmlContent = urlsContent.toString();

                if (!urlsHtmlContent.isEmpty()) {
                    navWebView.loadDataWithBaseURL(null, urlsHtmlContent, "text/html", "UTF-8", null);
                } else {
                    navWebView.loadUrl("about:blank");
                }
            }


            Log.d("mytag", "File loaded into WebView");
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
                if (i == 0) {
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
