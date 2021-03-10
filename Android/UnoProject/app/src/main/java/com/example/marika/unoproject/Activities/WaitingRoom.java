package com.example.marika.unoproject.Activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.marika.unoproject.R;
import com.example.marika.unoproject.SocketHandler;
import com.example.marika.unoproject.SocketService;
//https://github.com/koral--/android-gif-drawable
//Activité avec un gif qui fait patienter le joueur et garde le service en vie
public class WaitingRoom extends AppCompatActivity implements SocketHandler.OnPlayer2FoundListener
{
    SocketService socketService;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            SocketService.LocalBinder binder = (SocketService.LocalBinder) service;
            socketService =  binder.getService();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) { }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_room);
        SocketHandler.setPlayer2FoundListener(this);
        Intent intent = new Intent(WaitingRoom.this,SocketService.class);
        startService(intent);
        this.bindService(intent, serviceConnection , Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStart()
    {
        super.onStart();
        Log.i("StatutSocket",String.valueOf(SocketHandler.socket.isConnected()));
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        this.unbindService(serviceConnection);
    }

    @Override
    public void onPlayer2Found()
    {
        // quand 2 joueurs se trouvent dans la salle d'attente, l'activité principale démarre (UNO_Gameroom)
        Intent intent = new Intent(WaitingRoom.this, UNO_Gameroom.class);
        startActivity(intent);
    }
}
