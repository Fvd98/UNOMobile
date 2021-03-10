package com.example.marika.unoproject.Activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.marika.unoproject.Objects.BD;
import com.example.marika.unoproject.Objects.Carte;
import com.example.marika.unoproject.Objects.CarteButton;
import com.example.marika.unoproject.R;
import com.example.marika.unoproject.SocketHandler;
import com.example.marika.unoproject.SocketService;

import java.util.List;

public class UNO_Gameroom extends AppCompatActivity implements SocketHandler.ServerResponseListener
{
    SocketService socketService;
    final Context _Context = this;
    private LinearLayout _MesCartes;
    ImageView _ImageCarteCentre;
    Carte _CarteCentre = null;
    TextView _NbCarteAdversaire;
    final UNO_Gameroom This = this;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            SocketService.LocalBinder binder = (SocketService.LocalBinder) service;
            socketService = binder.getService();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) { }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uno__gameroom);

        _MesCartes = findViewById(R.id.LayoutMesCartes);
        _ImageCarteCentre = findViewById(R.id.imageCarteCentre);
        _NbCarteAdversaire = findViewById(R.id.txtNbCarteAdversaire);

        //Création de la barre d'action
        ActionBar menu = getSupportActionBar();
        menu.setTitle("");
        menu.setBackgroundDrawable(getResources().getDrawable(R.drawable.board_pale));
        menu.setDisplayUseLogoEnabled(true);
        menu.setDisplayShowHomeEnabled(true);

        SocketHandler.setServerResponseListener(this);
        //Garder le service en vie
        Intent intent = new Intent(UNO_Gameroom.this,SocketService.class);
        startService(intent);
        //Se lier au service
        this.bindService(intent, serviceConnection , Context.BIND_AUTO_CREATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.profil:
                //demander au serveur le profil du joueur
                SocketHandler.Envoyer("", SocketHandler.TypesRequetes.HISTORIQUEJOUEUR);
                return true;
            case R.id.deconnexion:
                //demander au serveur de mettre fin à la salle de jeu
                SocketHandler.Envoyer("", SocketHandler.TypesRequetes.DECONNEXION);
                this.Deconnexion();
                return true;
            case R.id.aide:
                //affiche les règles du jeux
                View regles = getLayoutInflater().inflate(R.layout.dialog_info_jeux,null);
                Button btnX = regles.findViewById(R.id.btnCompris);
                AlertDialog.Builder builderRecevoirCarte = new AlertDialog.Builder(this,R.style.dialogTransparent);
                builderRecevoirCarte.setCancelable(true);
                builderRecevoirCarte.setView(regles);
                final AlertDialog dialogRecevoirCarte = builderRecevoirCarte.create();
                btnX.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        dialogRecevoirCarte.dismiss();
                    }
                });
                dialogRecevoirCarte.show();
                return true;
            case R.id.charger_partie:
                //demande au serveur de charger la dernière partie sauvegardée entre les deux joueurs
                SocketHandler.Envoyer("", SocketHandler.TypesRequetes.CHARGER);
                Toast.makeText(this,"Chargement de la partie sauvegardée...",Toast.LENGTH_SHORT).show();
                return true;
            case R.id.sauvegarder_partie:
                //demande au serveur de sauvegarder la partie actuel
                SocketHandler.Envoyer("", SocketHandler.TypesRequetes.SAUVEGARDER);
                Toast.makeText(this,"Sauvegarde de la partie en cours...",Toast.LENGTH_SHORT).show();
                return true;
            case R.id.nouvelle_partie:
                //demande au serveur de redémarrer la partie
                SocketHandler.Envoyer("", SocketHandler.TypesRequetes.REDEMARRER);
                Toast.makeText(this,"Redémarrage de la partie en cours...",Toast.LENGTH_SHORT).show();
                Intent intentRedemarer = new Intent(UNO_Gameroom.this, UNO_Gameroom.class);
                startActivity(intentRedemarer);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void OnJoueur2Disconnect()
    {
        //déconnecte le joueur actuel car l'autre s'est déconnecté
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(_Context,"L'autre joueur s'est déconnecté...",Toast.LENGTH_LONG).show();
            }
        });
        this.Deconnexion();
    }

    @Override
    public void OnSaveSucceed()
    {
        //confirme la sauvegarde de la partie
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(_Context,"La partie a été sauvegardée.",Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void OnHostLoadGame(Boolean GameFound)
    {
        //si une partie est sauvegardée
        if(GameFound)
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(_Context,"L'autre joueur a chargé la dernière partie sauvegardée.",Toast.LENGTH_LONG).show();
                }
            });
            //redémarre la salle de jeu pour accueillir la nouvelle partie
            Intent intentRedemarer = new Intent(UNO_Gameroom.this, UNO_Gameroom.class);
            startActivity(intentRedemarer);
        }
        else
        {
            //avertie le joueur qu'aucune partie n'est sauvegardée
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(_Context,"Aucune partie à charger...",Toast.LENGTH_LONG).show();
                }
            });
        }

    }

    @Override
    public void OnHostRestartGame()
    {
        //confirme le redémarrage de la partie
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(_Context,"L'autre joueur a redémarré la partie.",Toast.LENGTH_LONG).show();
            }
        });
        //redémarre la salle de jeu pour accueillir la nouvelle partie
        Intent intentRedemarer = new Intent(UNO_Gameroom.this, UNO_Gameroom.class);
        startActivity(intentRedemarer);
    }

    @Override
    public void OnMyTurn()
    {
        //avertie le joueur que c'est son tour
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(_Context,"C'est votre tour!",Toast.LENGTH_SHORT).show();
            }
        });
        //Active les cartes jouables
        Log.i("Nb Carte Testée ", String.valueOf(_MesCartes.getChildCount()));
        Boolean UneCarteJouable =false;
        for (int i = 0; i < _MesCartes.getChildCount(); i++)
        {
            final CarteButton carte = (CarteButton) _MesCartes.getChildAt(i);
            if(carte.getCarte().EstCompatible(this._CarteCentre))
            {
                Log.i("Carte Activée ", carte.getCarte().toString());
                UneCarteJouable = true;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        carte.setEnabled(true);
                    }
                });
            }
            else{Log.i("Carte Non-compatible ", carte.getCarte().toString());}
        }
        if(!UneCarteJouable)
        {
            //si aucune carte n'est jouable envoyer une carte non-existante au serveur pour confirmer l'incapacité du joueur
            SocketHandler.Envoyer("666", SocketHandler.TypesRequetes.CHOISIRCARTE);
            //désactiver toute les carte
            for (int i = 0; i < _MesCartes.getChildCount(); i++)
            {
                final CarteButton carte = (CarteButton) _MesCartes.getChildAt(i);
                runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            carte.setEnabled(false);
                        }
                    });
            }
        }
    }

    @Override
    public void OnCardsAdded(List<Carte> cardsAdded)
    {
        //Création du layout du "RecevoirCarte" pour le dialogue
        View RecevoirCarte = getLayoutInflater().inflate(R.layout.dialog_recevoir_carte,null);
        //Extraction du boutton pour définir son évènement par la suite
        final Button btnOk = RecevoirCarte.findViewById(R.id.btnOkRecevoirCarte);
        final LinearLayout cartesRecu = RecevoirCarte.findViewById(R.id.scrollRecevoirCarte);
        //Personnalisation du dialogue
        final AlertDialog.Builder builderRecevoirCarte = new AlertDialog.Builder(this,R.style.dialogTransparentFullscreen);
        builderRecevoirCarte.setCancelable(true);
        builderRecevoirCarte.setView(RecevoirCarte);
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                final AlertDialog dialogRecevoirCarte = builderRecevoirCarte.create();
                DisplayMetrics metrics = getResources().getDisplayMetrics();
                int width = metrics.widthPixels;
                int height = metrics.heightPixels;
                dialogRecevoirCarte.getWindow().setLayout(width,height);
                btnOk.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        dialogRecevoirCarte.dismiss();
                    }
                });
                dialogRecevoirCarte.show();
            }
        });
        //Ajouter et personnaliser les cartes reçues
        for(final Carte C : cardsAdded)
        {
            final CarteButton carteBouton = new CarteButton(this,C);
            final CarteButton carteBouton2 = new CarteButton(this,C);
            carteBouton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SocketHandler.Envoyer(String.valueOf(carteBouton.get_ID()), SocketHandler.TypesRequetes.CHOISIRCARTE);
                    for (int i = 0; i < _MesCartes.getChildCount(); i++)
                    {
                        final CarteButton carte = (CarteButton) _MesCartes.getChildAt(i);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                carte.setEnabled(false);
                            }
                        });
                    }
                    //http://android-coding.blogspot.ca/2012/11/remove-view-dynamically-using-java-code.html
                    ViewGroup parent = (ViewGroup)carteBouton.getParent();
                    parent.removeView(carteBouton);
                }
            });
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    if(carteBouton.getParent()!=null)
                        ((ViewGroup)carteBouton.getParent()).removeView(carteBouton);
                    _MesCartes.addView(carteBouton);

                    if(carteBouton2.getParent()!=null)
                        ((ViewGroup)carteBouton2.getParent()).removeView(carteBouton2);
                    cartesRecu.addView((View)carteBouton2);
                }
            });
        }
    }

    @Override
    public void OnColorSelect()
    {
        //Création du layout du "ColorPicker" pour le dialogue
        View colorPicker = getLayoutInflater().inflate(R.layout.dialog_choix_couleur,null);
        //Extraction des bouttons pour définir leur évènement par la suite
        final ImageButton btnVert = colorPicker.findViewById(R.id.btnVert);
        final ImageButton btnRouge = colorPicker.findViewById(R.id.btnRouge);
        final ImageButton btnJaune = colorPicker.findViewById(R.id.btnJaune);
        final ImageButton btnBleu = colorPicker.findViewById(R.id.btnBleu);
        //Personnalisation du dialogue
        final AlertDialog.Builder builderColorPicker = new AlertDialog.Builder(this,R.style.dialogTransparent);
        builderColorPicker.setCancelable(false);
        builderColorPicker.setView(colorPicker);
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                final AlertDialog dialogColorPicker = builderColorPicker.create();
                //Programmation des boutton de couleur (Envoie au serveur et fermeture du dialogue)
                btnVert.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        SocketHandler.Envoyer(Carte.Couleur.VERT.name(), SocketHandler.TypesRequetes.CHOISIRCOULEUR);
                        dialogColorPicker.dismiss();
                    }
                });
                btnRouge.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        SocketHandler.Envoyer(Carte.Couleur.ROUGE.name(), SocketHandler.TypesRequetes.CHOISIRCOULEUR);
                        dialogColorPicker.dismiss();
                    }
                });
                btnJaune.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        SocketHandler.Envoyer(Carte.Couleur.JAUNE.name(), SocketHandler.TypesRequetes.CHOISIRCOULEUR);
                        dialogColorPicker.dismiss();
                    }
                });
                btnBleu.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        SocketHandler.Envoyer(Carte.Couleur.BLEU.name(), SocketHandler.TypesRequetes.CHOISIRCOULEUR);
                        dialogColorPicker.dismiss();
                    }
                });
                //Affichage du dialogue
                dialogColorPicker.show();
            }
        });
    }

    @Override
    public void OnRefresh(Carte carteMilieu, final int nbCarteJ2)
    {
        //Affiche les modification apportées par le serveur.
        this._CarteCentre = carteMilieu;
        final CarteButton carteCentre = new CarteButton(this,carteMilieu);
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                _ImageCarteCentre.setImageDrawable(carteCentre.getDrawable());
                _ImageCarteCentre.setBackgroundDrawable(carteCentre.getBackground());
                _NbCarteAdversaire.setText(String.valueOf(nbCarteJ2));
                if(nbCarteJ2 <= 1)
                {
                    _NbCarteAdversaire.setTextColor(Color.RED);
                }
                else
                {
                    _NbCarteAdversaire.setTextColor(Color.WHITE);
                }
            }
        });
    }

    @Override
    public void OnGameHistoryReceived(List<String> monHistorique, final List<String> nbParties)
    {
        //personnalise le dialogue de profil
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, monHistorique)
        {
            @Override
            public View getView(int position, View convertView, ViewGroup parent)
            {
                View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(android.R.id.text1);
                textView.setTextColor(getResources().getColor(R.color.colorAccent));
                return view;
            }
        };

        View Profil = getLayoutInflater().inflate(R.layout.dialog_profil,null);

        final Button btnX = Profil.findViewById(R.id.btnXprofil);
        final TextView txtNom = Profil.findViewById(R.id.txtNomProfil);
        final TextView txtTotal = Profil.findViewById(R.id.txtTotalProfil);
        final TextView txtGagner = Profil.findViewById(R.id.txtGagnerProfil);
        final TextView txtPerdu = Profil.findViewById(R.id.txtPerduProfil);
        final ListView listhistorique = Profil.findViewById(R.id.listHistoriqueProfil);

        final AlertDialog.Builder builderRecevoirCarte = new AlertDialog.Builder(this,R.style.dialogTransparent);
        builderRecevoirCarte.setCancelable(true);
        builderRecevoirCarte.setView(Profil);

        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                final AlertDialog dialogRecevoirCarte = builderRecevoirCarte.create();
                btnX.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        dialogRecevoirCarte.dismiss();
                    }
                });
                txtNom.setText(String.format("%s (%s %s)",SocketHandler._Profil[0],SocketHandler._Profil[2],SocketHandler._Profil[1]));
                txtTotal.setText(nbParties.get(0));
                txtGagner.setText(nbParties.get(1));
                txtPerdu.setText(nbParties.get(2));
                listhistorique.setAdapter(adapter);
                dialogRecevoirCarte.show();
            }
        });
    }

    @Override
    public void OnUNO()
    {
        //Affiche un UNO
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                View vToast = getLayoutInflater().inflate(R.layout.toast_perso_image,null);
                final Toast toast = Toast.makeText(This, "",Toast.LENGTH_SHORT);
                toast.setView(vToast);
                toast.show();
            }
        });
    }

    @Override
    public void OnWin(final Boolean iWin)
    {
        //Affiche un Win / lose
        final String TWin = "Vous avez Gangné!";
        final String TLose = "Vous avez Perdu...";
        final String tWin = "Bravo! Bravo! Bravo!";
        final String tLose = "Meilleur chance la prochaine fois...";

        // affiche une notification et un toast personnalisé aux deux joueurs
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(This)
                        .setSmallIcon(R.drawable.card_plus2_t)
                        .setColor(Color.WHITE)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                View vToast = getLayoutInflater().inflate(R.layout.toast_perso_image,null);
                ImageView iToast = vToast.findViewById(R.id.imageToast);
                if(iWin)
                {
                    mBuilder.setContentTitle(TWin);
                    mBuilder.setContentText(tWin);
                    iToast.setBackgroundDrawable(getResources().getDrawable(R.drawable.first));
                }
                else
                {
                    mBuilder.setContentTitle(TLose);
                    mBuilder.setContentText(tLose);
                    iToast.setBackgroundDrawable(getResources().getDrawable(R.drawable.second));
                }

                Toast toast = Toast.makeText(This, "" ,Toast.LENGTH_SHORT);
                toast.setView(vToast);
                toast.show();

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(This);
                notificationManager.notify(666, mBuilder.build());
            }
        });
    }

    // envoie le joueur à la page d'accueil quand il se déconnecte
    private void Deconnexion()
    {
        Intent intentDeconexion = new Intent(UNO_Gameroom.this, Login.class);
        startActivity(intentDeconexion);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        this.unbindService(serviceConnection);
    }
}
