package com.example.marika.unoproject.Activities.Login_Contenu;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.marika.unoproject.Activities.Login;
import com.example.marika.unoproject.Activities.WaitingRoom;
import com.example.marika.unoproject.Objects.BD;
import com.example.marika.unoproject.R;
import com.example.marika.unoproject.SocketHandler;

import java.util.ArrayList;

public class F_Historique extends Fragment{
    private View controles;
    private ListView liste;
    private Login Parent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Parent = (Login)getActivity();
        controles = inflater.inflate(R.layout.f_historique, container, false);
        liste = controles.findViewById(R.id.liste);
        //définition du style et du contenu de la listview
        ArrayAdapter adapter = new ArrayAdapter<String>(Parent, android.R.layout.simple_list_item_1, SocketHandler.bd.extraireDonnees())
        {
            //définition du style des items de la listview
            @Override
            public View getView(int position, View convertView, ViewGroup parent)
            {
                View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(android.R.id.text1);
                textView.setTextColor(getResources().getColor(R.color.colorAccent));
                return view;
            }
        };
        liste.setAdapter(adapter);
        return controles;
    }

    @Override
    public void onStart() {
        super.onStart();
    }
}

