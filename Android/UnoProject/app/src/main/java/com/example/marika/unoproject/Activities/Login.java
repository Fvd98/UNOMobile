package com.example.marika.unoproject.Activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.marika.unoproject.Objects.BD;
import com.example.marika.unoproject.Objects.PageAdapter;
import com.example.marika.unoproject.R;
import com.example.marika.unoproject.SocketHandler;
import com.example.marika.unoproject.SocketService;

import java.io.IOException;
import java.net.Socket;

public class Login extends AppCompatActivity
{
    public SocketService.LocalBinder binder;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            try
            {
                //préparation du socket
                binder = (SocketService.LocalBinder) service;
                SocketService socketService = binder.getService();
                socketService.resetSocket();
                SocketHandler.setSocket(socketService.getSocket());
                SocketHandler.setReuseAddress();
                SocketHandler.Bind();
                SocketHandler.Connect();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName name) { }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Source pour la création des onglets: http://www.truiton.com/2015/06/android-tabs-example-fragments-viewpager/
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText("Connexion"));
        tabLayout.addTab(tabLayout.newTab().setText("Inscription"));
        tabLayout.addTab(tabLayout.newTab().setText("Historique"));
        tabLayout.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        tabLayout.setTabTextColors(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        final PageAdapter adapter = new PageAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
        {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        //création de la base de données SQLite et de la table Historique
        SocketHandler.bd = new BD(this.openOrCreateDatabase("sqlhistorique", Context.MODE_PRIVATE,null));
        SocketHandler.bd.creerTable();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        //Démarrage du BindService du socket
        Intent intent = new Intent(Login.this,SocketService.class);
        startService(intent);
        this.bindService(intent, serviceConnection , Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        //délier le service du Login
        this.unbindService(serviceConnection);
    }
}
