/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serveurprojet;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;

/*/*** @author Francis Vermette-David et Marika Groulx */

public class BaseDonnees {
    private String _url;
    private Connection _cn;
    
    public BaseDonnees(String url) throws ClassNotFoundException
    {
        this._url = url;
        // le chargement du provider
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
    }
    
    /***
     * Méthode quiinsère un objet de type Joueur dans la base de données
     * @param joueur
     * @param motdepasse
     * @return true si l'insertion fonctionne, false si elle ne fonctionne pas
     */
    public boolean InsererJoueur(Joueur joueur, String motdepasse)
    {
        try
        {
            // Ouverture de la connexion
            _cn = DriverManager.getConnection(this._url);
            // Préparation de la requête INSERT
            String requete = "INSERT INTO JOUEURS VALUES(?, ?, ?, ?, ?, 0, 0, 0)";
            PreparedStatement pcmd = _cn.prepareStatement(requete); 
            // associer les valeurs aux paramètres
            pcmd.setString(1, joueur.getUsername());
            pcmd.setString(2, joueur.getNom());
            pcmd.setString(3, joueur.getPrenom());
            pcmd.setDate(4, Date.valueOf(joueur.getDateInscription()));
            pcmd.setString(5, motdepasse);
            // Exécution de la requête INSERT
            pcmd.executeUpdate();
            // Fermeture de la connexion
            this.CloseConnection(_cn);
            return true;          
        }
        catch(SQLException ex)
        {
            System.out.println(ex.getMessage());
            this.CloseConnection(_cn);
            return false;
        }
    }
    
   /***
    * Méthode qui insère une sauveagarde dans la base de données
    * @param paquets
    * @param idPartie
    * @param usernameTour
    */
    public void InsererSauvegarde(PaquetCarte[] paquets, int idPartie, String usernameTour)
    {
        try
        {
            // Ouverture de la connexion
            _cn = DriverManager.getConnection(this._url);
            // Préparation de la requête INSERT          
            StringBuilder requeteSauvegardes = new StringBuilder("INSERT INTO SAUVEGARDES VALUES ");
            Statement pcmd = _cn.createStatement();  
            // pour chaque paquet, si le paquet n'est pas le paquet du jeu, on associe les cartes au bon paquet (dans celui du joueur 1, celui du joueur 2 ou celui de la carte du milieu)
            for(PaquetCarte p : paquets)
            {
                if(p != paquets[0])
                {                                                       
                    for(Carte c : p.getPileCarte())
                    {
                        int idCarte = c.getID();
                        String PropriName;
                        if(p.getProprietaire() != null)
                        {
                            PropriName = String.format("'%s'", p.getProprietaire().getUsername());
                        }
                        else
                        {
                            // null représente le paquet de la carte du milieu
                            PropriName = "NULL";
                        }
                        requeteSauvegardes.append(String.format("(%d,%d,%s),", idCarte, idPartie, PropriName));
                        System.out.println(String.format("j'insère Carte:%d,Partie:%d,Paquet:%s),", idCarte, idPartie, PropriName));
                    }
                }
            }
            // Exécution de la requête INSERT
            requeteSauvegardes.setCharAt(requeteSauvegardes.length() - 1, ' ');
            pcmd.executeUpdate(requeteSauvegardes.toString());
            // Update qui sauvegarde c'est à qui le tour 
            String requeteTour = "UPDATE PARTIES SET JoueurTour = ? WHERE IDPartie = ?";
            PreparedStatement pcmd2 = _cn.prepareStatement(requeteTour); 
            pcmd2.setString(1, usernameTour);
            pcmd2.setInt(2, idPartie);
            pcmd2.executeUpdate(); 
            // Fermeture de la connexion
            this.CloseConnection(_cn);
        }
        catch(SQLException ex)
        {
            System.out.println(ex.getMessage());
            this.CloseConnection(_cn);
        }
    }
    
    /***
     * Méthode qui insère une partie d'UNO dans la base de données quand 2 joueurs se connectent ensemble
     * @param userName
     * @param userName2
     * @return le ID de la partie si l'insertion fonctionne, sinon retourne -1
     */
    public int InsererPartie(String userName, String userName2)
    {
        try
        {
            // Ouverture de la connexion
            _cn = DriverManager.getConnection(this._url);
            // Préparation de la requête INSERT
            String requete = "INSERT INTO PARTIES VALUES(?, ?, ?, 'Aucun', NULL)";
            PreparedStatement pcmd = _cn.prepareStatement(requete); 
            // associer les valeurs aux paramètres
            pcmd.setString(1, userName);
            pcmd.setString(2, userName2);
            pcmd.setDate(3, Date.valueOf(LocalDate.now()));
            // Exécution de la requête INSERT
            pcmd.executeUpdate();           
            requete = "SELECT MAX(IDPartie) FROM PARTIES";
            Statement stm = _cn.createStatement();
            ResultSet rs = stm.executeQuery(requete);  
            rs.next();
            int IDPartie = rs.getInt(1);
            System.out.println(String.format("J'ai généré la partie No.%d",IDPartie));
             
            return rs.getInt(1);           
        }
        catch(SQLException ex)
        {
            System.out.println(ex.getMessage());
            this.CloseConnection(_cn);
            return -1;
        }
    }
    
    /***
     * Méthode qui recherche une partie (qui retourne vrai si elle existe et false si elle n'existe pas) et qui distribue les cartes dans les bons paquets
     * @param nomUtil1
     * @param nomUtil2
     * @param paquets
     * @param partie
     * @param sE
     * @return 
     */
    public Boolean RecherchePartie(String nomUtil1, String nomUtil2, PaquetCarte[] paquets, Partie partie, ServeurEnfant sE)
    {
        try
        {
            _cn = DriverManager.getConnection(this._url);                
            String requete = "SELECT IDPartie,IDCarte,NomUtilisateur FROM SAUVEGARDES WHERE IDPartie IN(SELECT IDPartie FROM PARTIES WHERE (Joueur1 = ? AND Joueur2 = ?) OR (Joueur1 = ? AND Joueur2 = ?))";
            PreparedStatement cmd = _cn.prepareStatement(requete);
            cmd.setString(1, nomUtil1);
            cmd.setString(2, nomUtil2);
            cmd.setString(3, nomUtil2);
            cmd.setString(4, nomUtil1);
            ResultSet rs = cmd.executeQuery();
            if(rs.next())
            {
                sE.ResetPartie();
                partie.setIdPartie(rs.getInt(1));
                
                final String Username1 = paquets[1].getProprietaire().getUsername();
                final String Username2 = paquets[2].getProprietaire().getUsername();
                
                int IdCarte;  
                int idPaquet = 0;
                String PropriName;
                do
                {                   
                    IdCarte = rs.getInt(2);
                    PropriName = rs.getString(3);
                    // si la carte n'a pas de propriétaire, c'est la carte du paquet du milieu (index 3)
                    if(PropriName == null)
                    {
                        idPaquet = 3;
                    }    
                    // si le propriétaire de la carte est le nom d'utilisateur du joueur 1 (index 1)
                    else if(PropriName.equals(Username1))
                    {
                        idPaquet = 1;
                    }
                    // si le propriétaire de la carte est le nom d'utilisateur du joueur 2 (index 2)
                    else if(PropriName.equals(Username2))
                    {
                        idPaquet = 2;
                    }
                    // distribution des cartes à leur propriétaire tout en retirant les cartes du deck
                    paquets[idPaquet].Recoit(paquets[0].findAndRemoveCarteById(IdCarte));
                } 
                while(rs.next());
                // affectation du tour 
                requete = "SELECT JoueurTour FROM PARTIES WHERE IDPartie = ?";
                cmd = _cn.prepareStatement(requete);
                cmd.setInt(1, partie.getIdPartie());
                rs = cmd.executeQuery();
                rs.next();
                partie.setTour(rs.getString(1));  
                return true;
            }
            else
            {
                return false;
            }       
        }
        catch(SQLException ex)
        {
            System.out.println(ex.getMessage());
        }
        CloseConnection(_cn);
        return false;
    }
    
    /***
     * Méthode qui incrémente les nombres de partie quand une partie est créée ou terminée
     * @param userName
     * @param NomColonneAj 
     */
    public void UpdateNbPartie(String userName, String NomColonneAj)
    {
        try
        {
            System.out.println(String.format("J'ajoute une partie %s au joueur %s",NomColonneAj,userName));
            // Ouverture de la connexion
            _cn = DriverManager.getConnection(this._url);
            // Préparation de la requête INSERT
            String requete = String.format("UPDATE Joueurs SET %s = %s + 1 WHERE NomUtilisateur = ?", NomColonneAj,NomColonneAj);
            PreparedStatement pcmd = _cn.prepareStatement(requete); 
            // associer les valeurs aux paramètres
            pcmd.setString(1, userName);
            pcmd.executeUpdate();                     
        }
        catch(SQLException ex)
        {
            System.out.println(ex.getMessage());
            this.CloseConnection(_cn);          
        }
    }
    
    /***
     * Méthode qui ferme la connexion à la base de données
     * @param cn 
     */
    private void CloseConnection(Connection cn)
    {
        try
        {
            cn.close();
        }
        catch(SQLException ex)
        {
            System.out.println("La connexion n'a pas été fermée correctement.");
        }
    }
    
    /***
     * Méthode qui recherche un joueur dans la base de données et qui le retourne s'il existe
     * @param nomUtil
     * @param motDePasse
     * @return 
     */
    public Joueur RechercheJoueur(String nomUtil, String motDePasse)
    {
        try
        {
            _cn = DriverManager.getConnection(this._url);                
            String requete = "SELECT * FROM JOUEURS WHERE NomUtilisateur = ? AND MotDePasse = ?";
            PreparedStatement cmd = _cn.prepareStatement(requete);
            cmd.setString(1, nomUtil);
            cmd.setString(2, motDePasse);
            ResultSet rs = cmd.executeQuery();
            if(rs.next())
            {
                //String username, String nom, String prenom, String dateInscription, Integer nbPartieTotale, Integer nbPartieGagnee, Integer nbPartiePerdue
                Joueur joueur = new Joueur(rs.getString(1), rs.getString(2), rs.getString(3), String.valueOf(rs.getDate(4)));
                CloseConnection(_cn);
                return joueur;
            }
        }
        catch(SQLException ex)
        {
            System.out.println(ex.getMessage());
        }
        CloseConnection(_cn);
        return null;
    }
    
    /***
     * Méthode qui récupère les informations nécessaires à l'affichage de l'historique d'un joueur
     * @param nomUtil
     * @return 
     */
    public ArrayList<String> RechercheHistoriqueJoueur(String nomUtil)
    {
        try
        {
            ArrayList<String> listHistorique = new ArrayList();
            _cn = DriverManager.getConnection(this._url);                
            String requete = "SELECT TOP 100 * FROM PARTIES WHERE Joueur1 = ? OR Joueur2 = ? ORDER BY IDPartie desc";
            PreparedStatement cmd = _cn.prepareStatement(requete);
            cmd.setString(1, nomUtil);
            cmd.setString(2, nomUtil);
            ResultSet rs = cmd.executeQuery();
            // configuration de l'affichage de l'historique des parties d'un joueur
            while(rs.next())
            {
                listHistorique.add(String.format("%s VS %s | GAGNANT : %s | %s",rs.getString(2),rs.getString(3),rs.getString(5),rs.getDate(4).toString()));         
            }
            return listHistorique;
        }
        catch(SQLException ex)
        {
            System.out.println(ex.getMessage());
        }
        CloseConnection(_cn);
        return null;
    }
    
    /***
     * Méthode qui récupère les nombres de parties totales, gagnées et perdues d'un joueur
     * @param nomUtil1
     * @return 
     */
    public ArrayList<String> RechercheNbPartie(String nomUtil1)
    {
        try
        {
            ArrayList<String> parties = new ArrayList();
            _cn = DriverManager.getConnection(this._url);                
            String requete = "SELECT NbPartieTotale, NbPartieGagnee, NbPartiePerdue FROM Joueurs WHERE NomUtilisateur = ?";         
            PreparedStatement cmd = _cn.prepareStatement(requete);
            cmd.setString(1, nomUtil1);
            ResultSet rs = cmd.executeQuery();
            if(rs.next())
            {
                parties.add(String.valueOf(rs.getInt(1)));
                parties.add(String.valueOf(rs.getInt(2)));
                parties.add(String.valueOf(rs.getInt(3)));
            }
            return parties;
        }
        catch(SQLException ex)
        {
            System.out.println(ex.getMessage());
        }
        CloseConnection(_cn);
        return null;
    }
    
    /***
     * Méthode qui met à jour le résultat d'une partie quand celle-ci se termine
     * @param nomUtilGagnant
     * @param idPartie 
     */
    public void ModifierResultat(String nomUtilGagnant, int idPartie)
    {
        try
        {
            _cn = DriverManager.getConnection(this._url);                
            String requete = "UPDATE PARTIES SET Resultat = ? WHERE IDPartie = ? ";
            PreparedStatement cmd = _cn.prepareStatement(requete);
            cmd.setString(1, nomUtilGagnant);
            cmd.setInt(2, idPartie);
            cmd.executeUpdate();
        }
        catch(SQLException ex)
        {
            System.out.println(ex.getMessage());
        }
        CloseConnection(_cn);
    }
}
