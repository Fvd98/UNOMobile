package com.example.marika.unoproject.Activities.Login_Contenu;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.marika.unoproject.Activities.Login;
import com.example.marika.unoproject.Activities.WaitingRoom;
import com.example.marika.unoproject.R;
import com.example.marika.unoproject.SocketHandler;
import com.example.marika.unoproject.SocketService;

import java.io.IOException;

/**
 * Created by Marika on 2018-04-16.
 */

public class F_Inscription extends Fragment implements SocketHandler.OnConnectionListener {

    private View controles;
    private Button btnInscription;
    private EditText txtNomUtil;
    private EditText txtMDP;
    private EditText txtReMDP;
    private EditText txtNom;
    private EditText txtPrenom;
    private CheckBox cbConnexion;
    private Boolean isConnected = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        controles = inflater.inflate(R.layout.f_inscription, container, false);
        btnInscription = controles.findViewById(R.id.InsbtnInscription);
        txtNomUtil = controles.findViewById(R.id.InstxtNomUtil);
        txtMDP = controles.findViewById(R.id.InstxtMDP);
        txtReMDP = controles.findViewById(R.id.InstxtReMDP);
        txtNom = controles.findViewById(R.id.InstxtNom);
        txtPrenom = controles.findViewById(R.id.InstxtPrenom);
        cbConnexion = controles.findViewById(R.id.InscbConnexion);
        SocketHandler.setConnectionListenerI(this);
        synchronized (SocketHandler.class){SocketHandler.class.notifyAll();}

        btnInscription.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(isConnected)
                {
                    String textNomUtil = txtNomUtil.getText().toString();
                    String textMDP = txtMDP.getText().toString();
                    String textReMDP = txtReMDP.getText().toString();
                    String textNom = txtNom.getText().toString();
                    String textPrenom = txtPrenom.getText().toString();

                    TextView erNomUtil = controles.findViewById(R.id.InsErNomUtil);
                    TextView erMDP = controles.findViewById(R.id.InsErMDP);
                    TextView erVide = controles.findViewById(R.id.InsErVide);

                    erNomUtil.setVisibility(View.GONE);
                    erMDP.setVisibility(View.GONE);
                    erVide.setVisibility(View.GONE);

                    if (!textNomUtil.equals("") && !textMDP.equals("") && !textReMDP.equals("") && !textNom.equals("") && !textPrenom.equals(""))
                    {
                        if(textMDP.equals(textReMDP))
                        {
                            //Demande au serveur l'existence du nom d'utilisateur entr??
                            SocketHandler.Envoyer(String.format("%s;%s;%s;%s", textNomUtil, textMDP, textNom, textPrenom), SocketHandler.TypesRequetes.INSCRIPTION);
                            synchronized (SocketHandler.class)
                            {
                                try
                                {
                                    //Attend la r??ponse du serveur
                                    SocketHandler.class.wait();
                                    //V??rifie la r??ponse du serveur
                                    if (!SocketHandler._Existe)
                                    {
                                        if (cbConnexion.isChecked())
                                        {
                                            //Envoie le joueur ?? la salle d'attente
                                            Intent intent = new Intent(getActivity(), WaitingRoom.class);
                                            startActivity(intent);
                                        }
                                        else
                                        {
                                            //Vide les Edittexts de leur contenu et affiche une confirmation de la r??ussite de la cr??ation du compte.
                                            btnInscription.setText("");
                                            txtNomUtil.setText("");
                                            txtMDP.setText("");
                                            txtReMDP.setText("");
                                            txtNom.setText("");
                                            txtPrenom.setText("");
                                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                            builder.setTitle("Inscription");
                                            builder.setMessage("Votre inscription a ??t?? effectu??e avec succ??s !\nBienvenue et amusez-vous !");
                                            builder.setIcon(R.drawable.back_uno_card);
                                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) { }
                                            });
                                            AlertDialog dialog = builder.create();
                                            dialog.show();
                                        }
                                    }
                                    else
                                    {
                                        erNomUtil.setVisibility(View.VISIBLE);
                                    }
                                }
                                catch (InterruptedException e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        }
                        else
                        {
                            erMDP.setVisibility(View.VISIBLE);
                        }
                    }
                    else
                    {
                        erVide.setVisibility(View.VISIBLE);
                    }
                }
                else
                {
                    try
                    {
                        //si le socket est ferm??
                        Toast.makeText(getActivity(),"D??soler, le serveur ne r??pond pas.\nR??essayer plus tard...",Toast.LENGTH_SHORT).show();
                        SocketService socketService = ((Login)getActivity()).binder.getService();
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
            }
        });
        return controles;
    }

    @Override
    public void onConnection()
    {
        isConnected = true;
    }
}
