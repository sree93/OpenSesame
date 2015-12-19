package com.sreemenon.opensesame;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.sreemenon.sqlite.DBTransactions;

import net.sqlcipher.Cursor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SplashActivity extends AppCompatActivity {

    boolean splashAnimRunning = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        PopulateWebsites populateWebsites = new PopulateWebsites();
        populateWebsites.execute();

        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            ((TextView)findViewById(R.id.tvVersion)).setText("v " + version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        final Thread thread = new Thread(){
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }finally {
                    splashAnimRunning = false;
                }
            }
        };

        thread.start();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private class PopulateWebsites extends AsyncTask<Void,Void,Void> {
        List<DataItem> websiteList;

        @Override
        protected Void doInBackground(Void... params) {

            DBTransactions transactions = new DBTransactions(SplashActivity.this);
            Cursor cursor = transactions.getCompleteCursor(false, "gems", "website,uname");

            if(cursor.moveToFirst()){
                do{
                    websiteList.add(new DataItem(cursor));
                }while (cursor.moveToNext());
            }
            cursor.close();
            transactions.closeDB();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(!websiteList.equals(null)){
                boolean flag = true;
                    try {
                        while (splashAnimRunning) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                            }
                        }

                        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                        intent.putExtra("dataItemList", (Serializable)websiteList);
                        startActivity(intent);
                        SplashActivity.this.finish();
                        flag = false;
                    } catch (Exception ex) {

                    }
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            websiteList = new ArrayList<>();
        }
    }
}
