package com.example.example;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.Group;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.example.databinding.ActivityMainBinding;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //textView = findViewById(R.id.txt);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        setSupportActionBar(binding.appBarMain.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        Menu menu = navigationView.getMenu();
        int i=1;

        //------------------------------------------------------------------------------------------
        // load json file
        String json = readJsonFile(this, "links.json");
        Gson gson = new Gson();
        LinksData data = gson.fromJson(json, LinksData.class); // deserialize

        List<Category> categories = data.getCategories();
        for (Category category : categories) {
            String categoryName = category.getName();
            List<Link> links = category.getLinks();
            menu.add(0, i + 1, 0, categoryName).setEnabled(false).setOnMenuItemClickListener(null);
            for (Link link : links) {
                String title = link.getTitle();
                String url = link.getUrl();
                //Log.d("mytag", "Category: " + categoryName + ", Title: " + title + ", URL: " + url);
                menu.add(0, i, 0, title);
                i++;
            }
        }
        mAppBarConfiguration = new AppBarConfiguration.Builder(menu).setOpenableLayout(drawer).build();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // Обработка выбора пунктов меню
                String menuTitle = item.getTitle().toString();
                for (Category category : categories) {
                    String categoryName = category.getName();
                    List<Link> links = category.getLinks();
                    for (Link link : links) {
                        String title = link.getTitle();
                        String url = link.getUrl();
                        if (menuTitle.equals(title)) {
                            webView = findViewById(R.id.webView);
                            webView.setWebViewClient(new WebViewClient());
                            webView.getSettings().setJavaScriptEnabled(true); // enable javascript
                            webView.setBackgroundColor(Color.TRANSPARENT);
                            webView.loadUrl(url);
                            drawer.closeDrawer(GravityCompat.START);
                            return true;
                        }
                    }
                }
                return false;
            }
        });

    }//onCreate

    private String readJsonFile(Context context, String fileName) {
        try {
            InputStream inputStream = context.getAssets().open(fileName);
            InputStreamReader reader = new InputStreamReader(inputStream);

            StringBuilder stringBuilder = new StringBuilder();
            char[] buffer = new char[4096];
            int bytesRead;
            while ((bytesRead = reader.read(buffer)) != -1) {
                stringBuilder.append(buffer, 0, bytesRead);
            }

            reader.close();
            inputStream.close();

            return stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public class Link {
        private String title;
        private String url;

        public String getTitle() {
            return title;
        }

        public String getUrl() {
            return url;
        }
    }

    public class Category {
        private String name;
        private List<Link> links;

        public String getName() {
            return name;
        }

        public List<Link> getLinks() {
            return links;
        }
    }

    public class LinksData {
        private List<Category> categories;

        public List<Category> getCategories() {
            return categories;
        }
    }

}

