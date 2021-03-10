package com.example.marika.unoproject;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.net.Socket;

/***
 * Classe service qui contient le socket pour le garder en vie entre les activités
 */
public class SocketService extends Service
{
    //Source : https://www.youtube.com/watch?v=cJsqMisTaa8
    private final  IBinder iBinder = new LocalBinder();
    private Socket _socket = new Socket();

    public SocketService()
    { }

    public class LocalBinder extends Binder
    {
        public SocketService getService()
        {
            return SocketService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return iBinder;
    }

    public Socket getSocket()
    {
        return _socket;
    }

    public void resetSocket() throws IOException {
        _socket.close();
        _socket = new Socket();
    }
}
