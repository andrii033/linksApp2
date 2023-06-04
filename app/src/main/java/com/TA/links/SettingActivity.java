package com.TA.links;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {
    EditText editTextPassword;
    EditText editTextName;
    EditText editTextAddress;
    Button saveButton;
    ImageButton showPasswordButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        editTextPassword = findViewById(R.id.editTextTextPassword);
        editTextName = (EditText) findViewById(R.id.editTextName);
        editTextAddress = (EditText) findViewById(R.id.editTextAddress);
        showPasswordButton = findViewById(R.id.showPasswordButton);
        showPasswordButton.setOnClickListener(this);
        saveButton = findViewById(R.id.btnSave);
        saveButton.setOnClickListener(this);

        //load from file
        try {
            String data = "";
            FileInputStream fis = openFileInput("setting.txt");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            String line;
            int i=0;
            while ((line = br.readLine()) != null) {
                if(i==0){
                    editTextName.setText(line);
                }
                if(i==1){
                    editTextAddress.setText(line);
                }
                if(i==2){
                    editTextPassword.setText(line);
                }
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.showPasswordButton){
            if(editTextPassword.getInputType()==129){
                editTextPassword.setInputType(1);
            }else{
                editTextPassword.setInputType(129);
            }
        }
        if(view.getId()==R.id.btnSave){
            String name = editTextName.getText().toString();
            String address = editTextAddress.getText().toString();
            String password = editTextPassword.getText().toString();
            if(name.length()>0 && address.length()>0 && password.length()>0){
                //save to file
                try {
                    OutputStream outputStream = openFileOutput("setting.txt", Context.MODE_PRIVATE);
                    outputStream.write(name.getBytes());
                    outputStream.write("\n".getBytes());
                    outputStream.write(address.getBytes());
                    outputStream.write("\n".getBytes());
                    outputStream.write(password.getBytes());
                    outputStream.close();
                    finish();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}