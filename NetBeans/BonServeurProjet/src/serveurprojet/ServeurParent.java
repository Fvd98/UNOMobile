/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serveurprojet;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/*** @author Francis Vermette-David et Marika Groulx */

public class ServeurParent extends Thread
{
    private int _port;
    private String _ipaddress;
    public static BaseDonnees baseDonnees;
    
    // Le constructeur
    public ServeurParent(String ipaddress, int port)
    {
        this._ipaddress = ipaddress;
        this._port = port;        
        try 
        {
            // pour authentification Windows
            //ServeurParent.baseDonnees = new BaseDonnees("jdbc:sqlserver://localhost;databaseName=SALLEDEJEU;integratedSecurity=true");  
            // pour authentification SQL
            ServeurParent.baseDonnees = new BaseDonnees("jdbc:sqlserver://localhost;databaseName=SALLEDEJEU;user=sa;password=sql");
        } 
        catch (ClassNotFoundException ex)
        {
            Logger.getLogger(ServeurParent.class.getName()).log(Level.SEVERE, null, ex);
        }             
    }
    
    // la méthode démarre les deux serveurs enfant quand les deux joueurs sont connectés 
    @Override
    public void run()
    {     
        // création d'un ThreadPoolExecutor
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        ThreadPoolExecutor executorPool = new ThreadPoolExecutor(Integer.MAX_VALUE, Integer.MAX_VALUE, 10, TimeUnit.SECONDS, new ArrayBlockingQueue(2), threadFactory, (Runnable r, ThreadPoolExecutor executor) -> {
            throw new UnsupportedOperationException("Not supported yet.");
        });
        try
        {
            // la création du socket d'écoute
            ServerSocket serveurSock = new ServerSocket();
            serveurSock.setReuseAddress(true);
            // associer une adresse ip et un port
            SocketAddress sockAddress = new InetSocketAddress(this._ipaddress, this._port);
            serveurSock.bind(sockAddress);
            
            // La boucle d’écoute
            for(;;)
            {
                // prendre un joueur 'Host'               
                Socket sJ1 = null;
                Socket sJ2 = null;
                ServeurEnfantConnexion tJ1 = new ServeurEnfantConnexion(serveurSock , 1);
                ServeurEnfantConnexion tJ2 = new ServeurEnfantConnexion(serveurSock, 2);                           
                executorPool.execute(tJ1);                               
                executorPool.execute(tJ2);
                
                // on met le socket en attente que les deux joueurs soient connectés
                while(!tJ1.getIsConnected() || !tJ2.getIsConnected())
                {
                    synchronized(serveurSock)
                    {
                        serveurSock.wait();
                        if(tJ1.getIsConnected() && sJ1 == null)
                        {
                            sJ1 = tJ1.getsCom();
                        }
                        if(tJ1.getIsConnected() && sJ2 == null)
                        {
                            sJ2 = tJ2.getsCom();
                        }
                    }                   
                }
                
                Thread.sleep(1000);
                
                this.Envoyer("", ServeurEnfant.TypesRequetes.JOUEUR2FOUND, new PrintWriter(sJ1.getOutputStream()));
                this.Envoyer("", ServeurEnfant.TypesRequetes.JOUEUR2FOUND, new PrintWriter(sJ2.getOutputStream()));
                               
                Joueur j1 = tJ1.getJoueur();
                Joueur j2 = tJ2.getJoueur(); 
                
                PaquetCarte pJeu = new PaquetCarte();
                PaquetCarte pMilieu = new PaquetCarte();
                PaquetCarte pHost = new PaquetCarte(j1);
                PaquetCarte pAdversaire = new PaquetCarte(j2);
                Partie partie = new Partie(-2);
                         
                // démarrage des deux serveurs enfant (un pour chaque joueur)
                executorPool.execute(new ServeurEnfant(partie,sJ1 , sJ2, pJeu, pMilieu, pHost, pAdversaire, true));
                executorPool.execute(new ServeurEnfant(partie,sJ2 ,sJ1, pJeu, pMilieu, pAdversaire, pHost, false));               
            }               
        } 
        catch (IOException | InterruptedException ex) 
        {     
            Logger.getLogger(ServeurParent.class.getName()).log(Level.SEVERE, null, ex);
        }     
    }
       
    public void Envoyer(String message, ServeurEnfant.TypesRequetes type, PrintWriter writer)
    {
        try {Thread.sleep(250);} catch (InterruptedException ex) {Logger.getLogger(ServeurEnfant.class.getName()).log(Level.SEVERE, null, ex);}
        String requete = String.format("%s♦%s", type.value, message);
        writer.println(requete);
        System.out.println(String.format("J'envoie : %s", requete));
        writer.flush();         
    }
}


