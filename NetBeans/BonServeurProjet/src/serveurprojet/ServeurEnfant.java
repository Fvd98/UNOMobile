/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serveurprojet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/*** @author Francis Vermette-David et Marika Groulx */

public class ServeurEnfant implements Runnable
{
    private final Partie partie;
    private final Socket sCom1;
    private final Socket sCom2;
    private final PrintWriter writer1;
    private final PrintWriter writer2;
    private final BufferedReader reader1;
    private final PaquetCarte pJeu;
    private final PaquetCarte pMilieu;
    private final PaquetCarte pProprietaire;
    private final PaquetCarte pAdversaire;
    private final Boolean isHost;
    private int CompteurChoixNull = 0;

    public ServeurEnfant(Partie partie, Socket sCom1, Socket sCom2 ,PaquetCarte pJeu ,PaquetCarte pMilieu ,PaquetCarte pProprietaire ,PaquetCarte pAdversaire, Boolean isHost) throws IOException
    {
        this.partie = partie;
        this.sCom1 = sCom1;
        this.sCom2 = sCom2;            
        this.isHost = isHost;     
        this.writer1 = new PrintWriter(sCom1.getOutputStream());
        this.writer2 = new PrintWriter(sCom2.getOutputStream());   
        this.reader1 = new BufferedReader(new InputStreamReader(sCom1.getInputStream()));
        this.pJeu = pJeu;
        this.pMilieu = pMilieu;
        this.pProprietaire = pProprietaire;
        this.pAdversaire = pAdversaire; 
    }
    
    /***
     * Méthode qui s'occupe d'un joueur lors d'une partie
     */
    @Override
    public void run()
    {         
        try 
        {                          
            this.ResetPartie();
            // le synchronized sert à mettre le deuxième ServeurEnfant en attente que le premier démarre la partie
            synchronized(this.partie)
            {
                if(this.isHost)
                {                 
                    DemarrerPartie();                 
                    this.partie.notifyAll();                  
                }
                else
                {
                    this.partie.wait();                   
                }
            }
            
            // la boucle de communication avec le client
            String messageRecu;
            Boolean RequestDeconnection = false;
            try
            {
                // tant que le message reçu n'est pas null et que le joueur ne demande pas une déconnection
                while ((messageRecu = this.reader1.readLine()) != null && !RequestDeconnection)
                {
                    TypesRequetes typeRequeteRecue;
                    // récupération du type de requête demandé par le client
                    typeRequeteRecue = TypesRequetes.values()[Integer.parseInt(messageRecu.split("♦")[0])];
                    // switch case qui traite la requête selon son type
                    switch(typeRequeteRecue)
                    {
                        case DECONNEXION:
                            // envoyer à l'autre joueur que la partie est terminée puisque l'autre joueur s'est déconnecté
                            this.EnvoyerAutreJoueur("", TypesRequetes.DECONNEXION);
                            RequestDeconnection = true;                           
                            break;
                        case SAUVEGARDER:
                            // INSERT dans base de données table SAUVEGARDE                  
                            ServeurParent.baseDonnees.InsererSauvegarde(new PaquetCarte[]{this.pJeu ,this.pProprietaire,this.pAdversaire,this.pMilieu}, this.partie.getIdPartie(),this.partie.getTour());
                            this.Envoyer("", TypesRequetes.SAUVEGARDER);
                            this.EnvoyerAutreJoueur("", TypesRequetes.SAUVEGARDER);
                            break;
                        case CHARGER:
                            // SELECT dans base de données table SAUVEGARDE
                            if(ServeurParent.baseDonnees.RecherchePartie(this.pProprietaire.getProprietaire().getUsername(), this.pAdversaire.getProprietaire().getUsername(), new PaquetCarte[]{this.pJeu,this.pProprietaire,this.pAdversaire,this.pMilieu}, this.partie, this))
                            {               
                                this.Envoyer(String.valueOf(true), TypesRequetes.CHARGER);
                                this.EnvoyerAutreJoueur(String.valueOf(true), TypesRequetes.CHARGER);    
                                this.RefreshPartie();
                                Thread.sleep(1500);
                                this.RefreshCartes();
                                // vérifie c'est à qui le tour et si la carte du milieu est une carte noire, le joueur doit faire un choix de couleur
                                if(this.pProprietaire.getProprietaire().getUsername().equals(this.partie.getTour()))
                                {
                                    if(this.pMilieu.Peek().getCouleur() == Carte.Couleur.NOIR)
                                    {
                                        this.EnvoyerAutreJoueur("", TypesRequetes.CHOISIRCOULEUR);
                                    }
                                    else
                                    {
                                        this.Envoyer("", TypesRequetes.VOTRETOUR);
                                    }   
                                }
                                else
                                {
                                    if(this.pMilieu.Peek().getCouleur() == Carte.Couleur.NOIR)
                                    {
                                        this.Envoyer("", TypesRequetes.CHOISIRCOULEUR);
                                    }
                                    else
                                    {
                                        this.EnvoyerAutreJoueur("", TypesRequetes.VOTRETOUR);
                                    }                                    
                                }
                            }
                            // si aucune partie est trouvée, envoie false
                            else
                            {
                                 this.Envoyer(String.valueOf(false), TypesRequetes.CHARGER);
                            }
                            break;
                        // redémarre une partie
                        case REDEMARRER:
                            this.EnvoyerAutreJoueur("", TypesRequetes.REDEMARRER);
                            this.partie.setIdPartie(-1);
                            this.ResetPartie();
                            this.DemarrerPartie();
                            break;
                        case CHOISIRCARTE:
                            // récupère l'id de la carte choisie
                            int idCarte = Integer.parseInt(messageRecu.split("♦")[1]);
                            // si le joueur peut jouer une carte (un id qui existe et non 666)
                            if(idCarte != 666)
                            {                               
                                // enlève la carte de la main du joueur
                                Carte cChoix = this.pProprietaire.findAndRemoveCarteById(idCarte);
                                Carte.Type typeChoix = cChoix.getType();
                                // enlève la carte actuellement au milieu
                                Carte cMilieu = this.pMilieu.PigerCarte(); 
                                // si la carte du milieu était une carte noire avec une propriété ChangerCouleur, on reset cette propriété à null 
                                if(cMilieu.getCouleur() == Carte.Couleur.NOIR)
                                {
                                    cMilieu.setChangerCouleur(null);
                                }
                                // remet la carte du milieu dans le jeu
                                this.pJeu.Recoit(cMilieu);                             
                                this.pJeu.Shuffle();
                                // met la carte choisie par le joueur au milieu
                                this.pMilieu.Recoit(cChoix);
                                RefreshPartie();
                                // si le joueur a juste une carte dans sa main, on avertit son adversaire avec un UNO
                                if(this.pProprietaire.Count() == 1)
                                {
                                    this.EnvoyerAutreJoueur("", TypesRequetes.UNO);
                                }
                                // si le joueur n'a plus de carte dans sa main, une requête de type WIN est envoyé aux deux clients et les infos des joueurs sont mis à jour 
                                if(this.pProprietaire.Count() == 0)
                                {
                                    ServeurParent.baseDonnees.ModifierResultat(this.pProprietaire.getProprietaire().getUsername(), this.partie.getIdPartie());
                                    ServeurParent.baseDonnees.UpdateNbPartie(this.pProprietaire.getProprietaire().getUsername(), "NbPartieGagnee");
                                    ServeurParent.baseDonnees.UpdateNbPartie(this.pAdversaire.getProprietaire().getUsername(), "NbPartiePerdue");
                                    this.Envoyer(String.valueOf(true), TypesRequetes.WIN);
                                    this.EnvoyerAutreJoueur(String.valueOf(false), TypesRequetes.WIN);
                                    break;
                                }                       
                                this.ResultatDeChoix(typeChoix);                                                       
                                this.CompteurChoixNull = 0;
                            }
                            else
                            {
                                this.CompteurChoixNull++;
                                this.ResultatDeChoix(null);
                            }
                            break;
                        case CHOISIRCOULEUR:
                            this.pMilieu.Peek().setChangerCouleur(Carte.Couleur.valueOf(messageRecu.split("♦")[1]));
                            this.RefreshPartie();
                            this.EnvoyerAutreJoueur("", TypesRequetes.VOTRETOUR);
                            this.partie.setTour(this.pAdversaire.getProprietaire().getUsername());
                            break;
                        case HISTORIQUEJOUEUR:             
                            // envoie en format JSON l'historique des parties du joueur et ses nombres de parties
                            this.Envoyer(
                                    String.format("%s;%s"
                                    ,this.SerialiserJSON(ServeurParent.baseDonnees.RechercheHistoriqueJoueur(this.pProprietaire.getProprietaire().getUsername()))
                                    ,this.SerialiserJSON(ServeurParent.baseDonnees.RechercheNbPartie(this.pProprietaire.getProprietaire().getUsername())))
                                    ,TypesRequetes.HISTORIQUEJOUEUR);                   
     
                            break;
                        default:
                            break;
                    }              
                }
            }
            catch(SocketException e)
            {
                System.out.println(String.format("Le joueur (%s : J1) (%s : J2) a fermé la connexion...",this.isHost,!this.isHost));
            }
        }
        catch (IOException | InterruptedException ex)
        {
            Logger.getLogger(ServeurEnfant.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally
        {
            try
            {
                if (this.reader1 != null) this.reader1.close();
                if (this.writer1 != null) this.writer1.close();
                if (this.sCom1 != null) this.sCom1.close();
                if (this.writer2 != null) this.writer2.close();
                if (this.sCom2 != null) this.sCom2.close();
            }
            catch (IOException ex)
            {
                Logger.getLogger(ServeurEnfant.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    // enum des types de requête possibles
    private static int compteur = 0;
    public static enum TypesRequetes
    {
        //SELF = (ex: "0♦" Pour CONNEXION) -car 0 est l'id de CONNEXION et qu'il n'y a pas d'information accompagnant la requête-

        //Les types d'envois//SELF = (ex: "0♦" Pour CONNEXION) -car 0 est l'id de CONNEXION et qu'il n'y a pas d'information accompagnant la requête-
        //C.   = Client -> Server
        //S.   = Server -> Client
        //S2.  = Client -> Server -> Client2

        CONNEXION /* C. "NomUtil;MDP" *//* S. "Existe?:Nom;Prenom;PartieTotal;PartieGagnée;PartiePerdue" */,
        INSCRIPTION /* C. "NomUtil;MDP;Nom;Prenom" *//* S. "Existe?:Nom;Prenom;PartieTotal;PartieGagnée;PartiePerdue" */,
        DECONNEXION /*C. SELF *//* S2. SELF */,

        RECEVOIRCARTE /*S. PaquetCarte(Liste<Carte>) */,
        VOTRETOUR /* S. SELF */,
        CHOISIRCARTE /* C. idCarte(int)|NULL */ ,
        CHOISIRCOULEUR /* S. SELF *//* C. Couleur(int (enum)) */,

        SAUVEGARDER /* C. SELF *//* S. SELF */,
        CHARGER /* C. SELF *//* S2. SELF */,
        REDEMARRER /* C. SELF *//* S. SELF *//* S2. SELF */,
        HISTORIQUEJOUEUR /* C. SELF *//* Partiejouée(Liste<String>(adapteur)) */,

        RAFRAICHISSEMENT /* C. SELF *//*S. PaquetCarte(Liste<Carte>);CarteMilieu(Carte);NbCarteJ2(int) */,

        JOUEUR2FOUND /* S. SELF */,
        
        WIN/* S. bool iWIn */,
        
        UNO/* S. SELF */,
        
        HISTORIQUELOCALUPDATE; /*S. idGAme;usernameAdversaire */
        
        public int value;
        TypesRequetes(){
            value = compteur++;
        }
    }
    
    // envoie un message au client qui a envoyé la requête
    public void Envoyer(String message, TypesRequetes type)
    {
        try {Thread.sleep(250);} catch (InterruptedException ex) {Logger.getLogger(ServeurEnfant.class.getName()).log(Level.SEVERE, null, ex);}
        String requete = String.format("%s♦%s", type.value, message);
        writer1.println(requete);
        System.out.println(String.format("J'ai envoyé : %s\nau joueur (%s : J1) (%s : J2)",requete,String.valueOf(this.isHost),String.valueOf(!this.isHost)));  
        writer1.flush();   
    }
    
    // envoie un message à l'autre client
    public void EnvoyerAutreJoueur(String message, TypesRequetes type)
    {
        try {Thread.sleep(250);} catch (InterruptedException ex) {Logger.getLogger(ServeurEnfant.class.getName()).log(Level.SEVERE, null, ex);}
        String requete = String.format("%s♦%s", type.value, message);
        writer2.println(requete);
        System.out.println(String.format("J'ai envoyé : %s\nau joueur (%s : J1) (%s : J2)",requete,String.valueOf(!this.isHost),String.valueOf(this.isHost)));  
        writer2.flush();
    }
    
    // sérialise un objet de type T sous format JSON dans le fichier
    public <T> String SerialiserJSON(T element)
    {
        // la création d'un objet Gson
        Gson gson = new GsonBuilder().create();
        // convertir en format JSon
        String formatjson = gson.toJson(element);
        return formatjson;
    }
    

    public void PigeCarte(PaquetCarte paquetJ, int nbCarte)
    {
        // distribution des cartes aux joueurs
        ArrayList<Carte> listeCartes = new ArrayList();
        for(int i = 0; i < nbCarte ; i++)
        {
            Carte c = this.pJeu.PigerCarte();
            listeCartes.add(c);
            paquetJ.Recoit(c);
        }
        // envoie les cartes aux joueurs et raffraichit leur interface
        if(paquetJ == this.pProprietaire)
        {
            this.Envoyer(this.SerialiserJSON(listeCartes), TypesRequetes.RECEVOIRCARTE); 
            this.EnvoyerAutreJoueur(String.format("%s;%d", this.SerialiserJSON(this.pMilieu.Peek()),this.pProprietaire.Count()), TypesRequetes.RAFRAICHISSEMENT);
        }   
        else
        {
            this.EnvoyerAutreJoueur(this.SerialiserJSON(listeCartes), TypesRequetes.RECEVOIRCARTE);
            this.Envoyer(String.format("%s;%d", this.SerialiserJSON(this.pMilieu.Peek()),this.pAdversaire.Count()), TypesRequetes.RAFRAICHISSEMENT);
        } 
    }
      
    // méthode qui gère le type de la carte choisie
    public void ResultatDeChoix(Carte.Type typeCarteChoisie)
    {
        if(typeCarteChoisie != null)
        {
            switch(typeCarteChoisie)
            {       
                // si c'est une carte normale (de 0 à 9) on envoie à l'autre joueur que c'est son tour
                case C0:
                case C1:
                case C2:
                case C3:
                case C4:
                case C5:
                case C6:
                case C7:
                case C8:
                case C9:
                    this.EnvoyerAutreJoueur("", TypesRequetes.VOTRETOUR);    
                    this.partie.setTour(this.pAdversaire.getProprietaire().getUsername());
                    break;
                // si c'est une carte Skip ton tour ou Change l'orientation de la partie, on remet le tour au même joueur
                case CSkip:case CTurn:
                    this.Envoyer("", TypesRequetes.VOTRETOUR);
                    this.partie.setTour(this.pProprietaire.getProprietaire().getUsername());
                    break;            
                // si c'est une carte +2, on envoie à l'autre joueur que c'est son tour en plus de 2 cartes aditionnelles
                case CPlus2:
                    this.PigeCarte(this.pAdversaire, 2);
                    this.EnvoyerAutreJoueur("", TypesRequetes.VOTRETOUR);    
                    this.partie.setTour(this.pAdversaire.getProprietaire().getUsername());
                    break;            
                // si c'est une carte +4, on envoie 4 cartes à l'autre joueur
                case Cplus4:
                    this.PigeCarte(this.pAdversaire, 4);          
                // si c'est une carte de changement de couleur, on envoie une requête de choix de couleur au même joueur
                case CColor:
                    this.Envoyer("", TypesRequetes.CHOISIRCOULEUR);
                    break;                      
                default:
                    break;
            }
        }
        else
        {
            // si le joueur ne peut pas jouer de carte, une carte lui est renvoyée
            if(this.CompteurChoixNull == 1)
            {
                this.PigeCarte(this.pProprietaire, 1);
                this.Envoyer("", TypesRequetes.VOTRETOUR);
                this.partie.setTour(this.pProprietaire.getProprietaire().getUsername());
            }
            // si le joueur ne peut pas jouer encore, son tour se termine
            else
            {
                this.CompteurChoixNull = 0;
                this.EnvoyerAutreJoueur("", TypesRequetes.VOTRETOUR);
                this.partie.setTour(this.pAdversaire.getProprietaire().getUsername());
            }        
        }               
    }
    
    // méthode qui redémarre les paquets en mettant à jour les infos des joueurs dans leur historique
    public void ResetPartie()
    {
        if(this.partie.getIdPartie() == -1 || (this.isHost && this.partie.getIdPartie() == -2))
        {
            this.partie.setIdPartie(ServeurParent.baseDonnees.InsererPartie(this.pProprietaire.getProprietaire().getUsername(), this.pAdversaire.getProprietaire().getUsername())); 
            ServeurParent.baseDonnees.UpdateNbPartie(this.pProprietaire.getProprietaire().getUsername(), "NbPartieTotale");
            ServeurParent.baseDonnees.UpdateNbPartie(this.pAdversaire.getProprietaire().getUsername(), "NbPartieTotale");  
            this.Envoyer(String.format("%s;%s",this.partie.getIdPartie(),this.pAdversaire.getProprietaire().getUsername()), TypesRequetes.HISTORIQUELOCALUPDATE);
            this.EnvoyerAutreJoueur(String.format("%s;%s",this.partie.getIdPartie(),this.pProprietaire.getProprietaire().getUsername()), TypesRequetes.HISTORIQUELOCALUPDATE);
        }
                               
        this.pJeu.ResetFullDeck();
        this.pMilieu.Reset();
        this.pProprietaire.Reset();
        this.pAdversaire.Reset();    
    }
    
    // méthode qui remplie les paquets 
    public void DemarrerPartie()
    {      
        // retourner la première carte du jeu
        this.pMilieu.Recoit(this.pJeu.PigerCarte());  
        this.PigeCarte(this.pProprietaire, 7);
        this.PigeCarte(this.pAdversaire, 7);
        this.ResultatDeChoix(this.pMilieu.Peek().getType());
    }
    
    // méthode qui raffraichit la partie
    public void RefreshPartie()
    {
        this.EnvoyerAutreJoueur(String.format("%s;%d", this.SerialiserJSON(this.pMilieu.Peek()),this.pProprietaire.Count()), TypesRequetes.RAFRAICHISSEMENT);
        this.Envoyer(String.format("%s;%d", this.SerialiserJSON(this.pMilieu.Peek()),this.pAdversaire.Count()), TypesRequetes.RAFRAICHISSEMENT);
    }
    
    public void RefreshCartes()
    {
        // envoie les cartes aux joueurs et raffraichit leur interface       
        this.Envoyer(this.SerialiserJSON(this.pProprietaire.getPileCarte()), TypesRequetes.RECEVOIRCARTE);            
        this.EnvoyerAutreJoueur(this.SerialiserJSON(this.pAdversaire.getPileCarte()), TypesRequetes.RECEVOIRCARTE);   
        this.RefreshPartie();      
    }
}
