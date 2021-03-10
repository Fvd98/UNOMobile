package com.example.marika.unoproject;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;

import com.example.marika.unoproject.Objects.BD;
import com.example.marika.unoproject.Objects.Carte;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class SocketHandler
{
    //region Variables
    // configurer votre adresse IP
    static final private String _AdresseIPServeur = "10.4.129.13";
    static public String idGame;
    static final private int _PortServeur = 7777;
    static public String[] _Profil;
    static public boolean _Existe;
    static public Socket socket;
    static public BD bd;

    public static void setSocket(Socket socket)
    {
        SocketHandler.socket = socket;
    }

    //endregion

    //region TypesRequetes
    private static int Compteur = 0;
    public enum TypesRequetes
    {
        //SELF = (ex: "0♦" Pour CONNEXION) -car 0 est l'id de CONNEXION et qu'il n'y a pas d'information accompagnant la requête-

        //C.   = Client -> Server
        //S.   = Server -> Client
        //S2.  = Client -> Server -> Client2

        CONNEXION /* C. "NomUtil;MDP" *//* S. "Existe?:Nom;Prenom;PartieTotal;PartieGagnée;PartiePerdue" */,
        INSCRIPTION /* C. "NomUtil;MDP;Nom;Prenom" *//* S. "Existe?:Nom;Prenom;PartieTotal;PartieGagnée;PartiePerdue" */,
        DECONNEXION /*C. SELF *//* S2. SELF */,

        RECEVOIRCARTE /*S. PaquetCarte(Liste<Carte>) */,
        VOTRETOUR /* S. SELF */,
        CHOISIRCARTE /* C. idCarte(int)|NULL */ ,
        CHOISIRCOULEUR /* S. SELF *//* C. Couleur.Name */,

        SAUVEGARDER /* C. SELF *//* S. SELF */,
        CHARGER /* C. SELF *//* S2. SELF */,
        REDEMARRER /* C. SELF *//* S2. SELF */,
        HISTORIQUEJOUEUR /* C. SELF *//* Partiejouée(Liste<String>(adapteur)) */,

        RAFRAICHISSEMENT /* C. SELF *//*S. PaquetCarte(Liste<Carte>);CarteMilieu(Carte);NbCarteJ2(int) */,

        JOUEUR2FOUND /* S. SELF */,

        WIN/* S. bool iWIn */,
        UNO/* S. SELF */,

        HISTORIQUELOCALUPDATE; /*S. idGAme;usernameAdversaire */

        public int value;
        TypesRequetes(){
            value = Compteur++;
        }
    }
    //endregion

    //region Listeners

    //region OnPlayer2Found
    static private OnPlayer2FoundListener player2FoundListener;
    public interface OnPlayer2FoundListener
    {
        void onPlayer2Found();
    }
    public static void setPlayer2FoundListener(OnPlayer2FoundListener player2FoundListener)
    {
        SocketHandler.player2FoundListener = player2FoundListener;
    }
    //endregion

    //region OnServerResponse
    static private ServerResponseListener serverResponseListener;
    public interface ServerResponseListener
    {
        //Methodes
        void OnJoueur2Disconnect();
        void OnSaveSucceed();
        void OnHostLoadGame(Boolean GameFound);
        void OnHostRestartGame();
        void OnMyTurn();
        void OnCardsAdded(List<Carte> cartesAjoute);
        void OnColorSelect();
        void OnRefresh(Carte carteMilieu, int nbCarteJ2);
        void OnGameHistoryReceived(List<String> monHistorique, List<String> nbParties);
        void OnUNO();
        void OnWin(Boolean iWin);
    }
    public static void setServerResponseListener(ServerResponseListener serverResponseListener)
    {
        SocketHandler.serverResponseListener = serverResponseListener;
    }
    //endregion

    //region OnConnectionListener
    static private OnConnectionListener connectionListenerC;
    static private OnConnectionListener connectionListenerI;
    public interface OnConnectionListener
    {
        void onConnection();
    }
    public static void setConnectionListenerC(OnConnectionListener connectionListener)
    {
        connectionListenerC = connectionListener;
    }
    public static void setConnectionListenerI(OnConnectionListener connectionListener)
    {
        connectionListenerI = connectionListener;
    }
    //endregion

    //endregion

    //region Methodes
    public static void setReuseAddress()
    {
        try
        {
            socket.setReuseAddress(true);
        }
        catch (SocketException e)
        {
            e.printStackTrace();
        }
    }

    /***
     * Méthode qui donne la permission aux threads d'utiliser le réseau et bind le socket à son adresse locale
     */
    public static void Bind()
    {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        SocketAddress AdresseLocal = new InetSocketAddress(socket.getLocalAddress(),0);
        try
        {
            socket.bind(AdresseLocal);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @SuppressLint("StaticFieldLeak")
    public static void Connect()
    {
        new AsyncTask<Void,Void,Void>()
        {
            @Override
            protected Void doInBackground(Void... voids)
            {
                try
                {
                    socket.connect(new InetSocketAddress(_AdresseIPServeur,_PortServeur));
                    // averti les fragments du login que le client est connecté au serveur
                    connectionListenerC.onConnection();
                    connectionListenerI.onConnection();
                    // démarre le receveur
                    DemarerReceveur();
                }
                catch (IOException e) { e.printStackTrace(); }
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /***
     * Méthode qui démarre une boucle d'écoute infinie dans un AsyncTask
     */
    @SuppressLint("StaticFieldLeak")
    public static void DemarerReceveur()
    {
        new AsyncTask<Void,Void,Void>()
        {
            @Override
            protected Void doInBackground(Void... voids)
            {
                String Message;
                try
                {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    // boucle d'écoute infinie
                    while (true)
                    {
                        // lecture d'un message
                        Message = reader.readLine();
                        if(Message == null)
                        {
                            // rupture de la boucle d'écoute quand le serveur se déconnecte
                            break;
                        }
                        Log.i("Je reçois ",Message);
                        TypesRequetes typeRequeteRecue = null;
                        try
                        {
                            // récupère le type de requête reçu
                            typeRequeteRecue = TypesRequetes.values()[Integer.parseInt(Message.split("♦")[0])];
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                        synchronized (SocketHandler.class)
                        {
                            // Switch case selon le type de requête reçu
                            switch (typeRequeteRecue) {
                                case CONNEXION:

                                    _Existe = Boolean.parseBoolean(Message.split("♦")[1].split(":")[0]);
                                    if(_Existe)
                                    {
                                        // reçoit le profil
                                        _Profil = Message.split("♦")[1].split(":")[1].split(";");
                                    }
                                    // réveille le fragment de connexion quand la réponse du serveur est reçue
                                    SocketHandler.class.notifyAll();
                                    break;
                                case INSCRIPTION:
                                    _Existe = Boolean.parseBoolean(Message.split("♦")[1].split(":")[0]);
                                    if(!_Existe)
                                    {
                                        // reçoit le profil
                                        _Profil = Message.split("♦")[1].split(":")[1].split(";");
                                    }
                                    // réveille le fragment d'inscription quand la réponse du serveur est reçue
                                    SocketHandler.class.notifyAll();
                                    break;
                                case DECONNEXION:
                                    // avertit la salle de jeu que l'autre joueur s'est déconnecté
                                    serverResponseListener.OnJoueur2Disconnect();
                                    break;
                                case SAUVEGARDER:
                                    // avertit la salle de jeu que la sauvegarde a réussie
                                    serverResponseListener.OnSaveSucceed();
                                    break;
                                case CHARGER:
                                    // avertit la salle de jeu que la partie a été chargée
                                    serverResponseListener.OnHostLoadGame(Boolean.parseBoolean(Message.split("♦")[1]));
                                    break;
                                case REDEMARRER:
                                    // avertit la salle de jeu que la partie a été redémarrée
                                    serverResponseListener.OnHostRestartGame();
                                    break;
                                case JOUEUR2FOUND:
                                    // avertit la salle d'attente qu'un deuxième joueur a été trouvé
                                    player2FoundListener.onPlayer2Found();
                                    break;
                                case VOTRETOUR:
                                    // avertit la salle de jeu que c'est notre tour
                                    serverResponseListener.OnMyTurn();
                                    break;
                                case RECEVOIRCARTE:
                                    // avertit la salle de jeu qu'on a reçu des cartes
                                    String CartesRecuesSeri = Message.split("♦")[1];
                                    List<Carte> CartesRecues = deserialiserJSON(CartesRecuesSeri, new TypeToken<ArrayList<Carte>>(){}.getType());
                                    serverResponseListener.OnCardsAdded(CartesRecues);
                                    break;
                                case CHOISIRCOULEUR:
                                    // avertit la salle de jeu qu'un choix de couleur est à faire
                                    serverResponseListener.OnColorSelect();
                                    break;
                                case RAFRAICHISSEMENT:
                                    // avertit la salle de jeu qu'il faut raffraichir l'interface
                                    String[] Rafraichissent = Message.split("♦")[1].split(";");
                                    String CarteMilieuSeri = Rafraichissent[0];
                                    int nbCartesJ2 = Integer.parseInt(Rafraichissent[1]);
                                    Carte CarteMilieu = deserialiserJSON(CarteMilieuSeri, Carte.class);
                                    serverResponseListener.OnRefresh(CarteMilieu, nbCartesJ2);
                                    break;
                                case HISTORIQUEJOUEUR:
                                    // avertit la salle de jeu qu'il faut afficher le profil
                                    String HistoriqueSeri = Message.split("♦")[1];
                                    String[] HistoriqueDefragment = HistoriqueSeri.split(";");
                                    List<String> HistoriqueDeseri = deserialiserJSON(HistoriqueDefragment[0], new TypeToken<ArrayList<String>>(){}.getType());
                                    List<String> NbPartieDeseri = deserialiserJSON(HistoriqueDefragment[1], new TypeToken<ArrayList<String>>(){}.getType());
                                    for(int i = 0; i < HistoriqueDeseri.size(); i++)
                                    {
                                        HistoriqueDeseri.set(i,HistoriqueDeseri.get(i).replace('_',' '));
                                    }
                                    serverResponseListener.OnGameHistoryReceived(HistoriqueDeseri, NbPartieDeseri);
                                    break;
                                case UNO:
                                    // avertit la salle de jeu qu'il y a un UNO
                                    serverResponseListener.OnUNO();
                                    break;
                                case WIN:
                                    // avertit la salle de jeu qu'il y a un gagnant
                                    Boolean iWin = Boolean.parseBoolean(Message.split("♦")[1]);
                                    serverResponseListener.OnWin(iWin);
                                    bd.Update(idGame,iWin);
                                    break;
                                case HISTORIQUELOCALUPDATE:
                                    // insère la nouvelle partie dans la base de données locale
                                    String Insertion[] = Message.split("♦")[1].split(";");
                                    String idGameCreer = Insertion[0];
                                    String UsernameAdversaire = Insertion[1];
                                    idGame = idGameCreer;
                                    bd.Inserer(idGameCreer,_Profil[0],UsernameAdversaire);
                                default:
                                    break;
                            }
                        }
                    }
                }
                catch (NullPointerException | IOException  e)
                {
                    if(e.getClass() != SocketException.class)
                    {
                        e.printStackTrace();
                    }
                }
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /***
     * Méthode qui permet d'envoyer un message au serveur
     * @param message
     * @param typesRequetes
     */
    @SuppressLint("StaticFieldLeak")
    public static void Envoyer(final String message, final TypesRequetes typesRequetes)
    {
        new AsyncTask <String,Void,Void>()
        {
            @SuppressLint("DefaultLocale")
            @Override
            protected Void doInBackground(String...messages)
            {
                try
                {
                    PrintWriter writer = new PrintWriter(socket.getOutputStream());
                    writer.println(String.format("%d♦%s", typesRequetes.value, messages[0]));
                    writer.flush();
                    Log.i("J'envoie ",message);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,message);
    }

    // Méthode qui permet de désérialiser un objet de type T à partir d'une string reçue en JSON
    private static <T> T deserialiserJSON(String message, Type type) throws IOException
    {
        return new Gson().fromJson(message, type);
    }
    //endregion
}
