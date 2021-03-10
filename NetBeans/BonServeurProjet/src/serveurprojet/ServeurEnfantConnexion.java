/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serveurprojet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

/*** @author Francis Vermette-David et Marika Groulx */

public class ServeurEnfantConnexion implements Runnable{
    private Socket _sCom;
    private Boolean _isConnected = false;
    private final int noJ;
    private final ServerSocket serveurSock;
    private Joueur _joueur;

    public ServeurEnfantConnexion(ServerSocket serverSock, int noJ) {      
        this.serveurSock = serverSock;
        this.noJ = noJ;
    } 

    public Boolean getIsConnected() {
        return _isConnected;
    }    
    
    public Socket getsCom() 
    {
        return _sCom;
    }

    public Joueur getJoueur() {
        return _joueur;
    }
    
    /***
     * Méthode qui gère la connexion d'un client
     */
    @Override
    public void run()
    {      
        while(!this._isConnected)
        {
            try 
            {
                this._sCom = serveurSock.accept();
                System.out.println(String.format("Connexion à J%d réussie...", this.noJ));
                BufferedReader reader = new BufferedReader(new InputStreamReader(this._sCom.getInputStream()));
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(this._sCom.getOutputStream()));
                // tant que le client n'est pas connecté
                while(!this._isConnected)
                {
                    // recevoir un message
                    String messageRecu = reader.readLine();
                    if(messageRecu == null)
                    {
                        break;
                    }
                    System.out.println("Le client a envoyé: " + messageRecu);

                    // récupère le type de la requête envoyé
                    ServeurEnfant.TypesRequetes typeRequeteRecue = ServeurEnfant.TypesRequetes.values()[Integer.parseInt(messageRecu.split("♦")[0])];
                    switch(typeRequeteRecue)
                    {
                        // si le client demande une connexion, on confirme son existance dans la base de données puis on lui envoi son profil
                        case CONNEXION :
                            // récupérer le nom d'utilisateur et le mot de passe entré par le joueur
                            String[] infos = messageRecu.substring(messageRecu.indexOf("♦") + 1).split(";");
                            // recherche dans la base de données si le joueur existe
                            Joueur joueurExistant = ServeurParent.baseDonnees.RechercheJoueur(infos[0].toUpperCase(), infos[1]); 
                            // le joueur existe
                            if(joueurExistant != null)
                            {
                                this.Envoyer(String.format("%s:%s", String.valueOf(true), joueurExistant.toString()), ServeurEnfant.TypesRequetes.CONNEXION,writer);
                                this._isConnected = true;
                                this._joueur = joueurExistant;
                            }
                            // le joueur n'existe pas
                            else
                            {
                                this.Envoyer(String.valueOf(false)+":", ServeurEnfant.TypesRequetes.CONNEXION,writer);
                            }
                            break;
                        // le client demande une inscription, on insère le nouveau client dans la base de données
                        case INSCRIPTION :
                            String[] Profil = messageRecu.split("♦")[1].split(";");
                            Joueur joueurNouveau = new Joueur(Profil[0].toUpperCase(), Profil[2], Profil[3], String.valueOf(LocalDate.now()));
                            if(ServeurParent.baseDonnees.InsererJoueur(joueurNouveau, Profil[1]))
                            {
                                this.Envoyer(String.format("%s:%s", String.valueOf(false), joueurNouveau.toString()), ServeurEnfant.TypesRequetes.INSCRIPTION,writer);
                                this._isConnected = true;
                                this._joueur = joueurNouveau;
                            }
                            else
                            {
                                this.Envoyer(String.valueOf(true)+":", ServeurEnfant.TypesRequetes.INSCRIPTION,writer);
                            }
                            break;
                    }
                }
            }          
            catch (IOException ex) 
            {
                Logger.getLogger(ServeurParent.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        // quand la connexion d'un client est établie, on réveille le serveur parent
        synchronized(this.serveurSock)
        {
            this.serveurSock.notifyAll();
        } 
    } 
    
    public void Envoyer(String message, ServeurEnfant.TypesRequetes type, PrintWriter writer)
    {
        writer.println(String.format("%s♦%s", type.value, message));
        System.out.println(String.format("J'envoie  : %s♦%s", type.value, message));
        writer.flush();         
    }
}
