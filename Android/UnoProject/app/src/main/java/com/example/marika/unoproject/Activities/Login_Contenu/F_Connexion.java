package com.example.marika.unoproject.Activities.Login_Contenu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
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

import static android.content.Context.MODE_PRIVATE;

public class F_Connexion extends Fragment implements SocketHandler.OnConnectionListener
{
    private View controles;
    private SharedPreferences pref;
    private Button btnConnexion;
    private EditText nomUtil;
    private EditText mdp;
    private CheckBox sauvegarder;
    private Boolean isConnected = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        controles = inflater.inflate(R.layout.f_connexion, container, false);
        btnConnexion = controles.findViewById(R.id.CobtnConnexion);
        pref = getActivity().getSharedPreferences(getString(R.string.SHARED_PREF), MODE_PRIVATE);
        nomUtil = controles.findViewById(R.id.CotxtNomUtil);
        mdp = controles.findViewById(R.id.CotxtMDP);
        sauvegarder = controles.findViewById(R.id.CocbRappeller);
        charger();
        SocketHandler.setConnectionListenerC(this);
        btnConnexion.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(isConnected)
                {
                    String textNomUtil = nomUtil.getText().toString();
                    String textMDP = mdp.getText().toString();
                    TextView erreur = controles.findViewById(R.id.CoErreur);
                    erreur.setVisibility(View.GONE);
                    //Validation NomUtil/MDP
                    if(!textNomUtil.equals("") && !textMDP.equals(""))
                    {
                        //Demande au serveur l'existence du compte entré
                        SocketHandler.Envoyer(String.format("%s;%s", textNomUtil, textMDP), SocketHandler.TypesRequetes.CONNEXION);
                        synchronized (SocketHandler.class)
                        {
                            try
                            {
                                //Attend la réponse du serveur
                                SocketHandler.class.wait();
                                //Vérifie la réponse du serveur
                                if (SocketHandler._Existe)
                                {
                                    if(sauvegarder.isChecked())
                                    {
                                        sauver();
                                    }
                                    //Envoie le joueur à la salle d'attente
                                    Intent intent = new Intent(getActivity(), WaitingRoom.class);
                                    startActivity(intent);
                                }
                                else
                                {
                                    erreur.setVisibility(View.VISIBLE);
                                }
                            }
                            catch (InterruptedException e) {e.printStackTrace();}
                        }
                    }
                    else
                    {
                        erreur.setVisibility(View.VISIBLE);
                    }
                }
                else
                {
                    try
                    {
                        //si le socket est fermé
                        Toast.makeText(getActivity(),"Désoler, le serveur ne répond pas.\nRéessayer plus tard...",Toast.LENGTH_SHORT).show();
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

    // Méthode qui permet de sauvegarder des valeurs dans un fichier xml
    private void sauver(){
        // Créer un éditeur
        SharedPreferences.Editor editeur = pref.edit();
        editeur.putString("nomUtilisateur", nomUtil.getText().toString());
        editeur.putString("motdepasse", mdp.getText().toString());
        // Enregistrer les ajouts
        editeur.commit();
    }

    // Méthode qui permet de récupérer les valeurs d'un fichier xml et de les afficher dans les EditText appropriés
    private void charger() {
        // vérifie si la clé de la paire (clé,valeur) existe dans le fichier xml
        if (pref.contains("nomUtilisateur") && pref.contains("motdepasse"))
        {
            nomUtil.setText(pref.getString("nomUtilisateur", "DEFAULT"));
            mdp.setText(pref.getString("motdepasse", "DEFAULT"));
            sauvegarder.setChecked(true);
            pref.edit().clear();
        }
    }

    @Override
    public void onConnection()
    {
        isConnected = true;
    }

    @Override
    public void onStart() {
        super.onStart();
    }
}
