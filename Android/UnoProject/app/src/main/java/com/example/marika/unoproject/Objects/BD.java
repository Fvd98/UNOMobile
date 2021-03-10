package com.example.marika.unoproject.Objects;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class BD
{
    private SQLiteDatabase sql;

    public BD(SQLiteDatabase sql)
    {
        this.sql = sql;
    }

    /***
     * Méthode qui créer la table Historique qui contient les parties locales du téléphone
     */
    public void creerTable()
    {
        try
        {
            this.sql.execSQL("CREATE TABLE IF NOT EXISTS Historique (IDHistorique int, Joueur1 varchar(50), Joueur2 varchar(50), DatePartie varchar(50), Resultat varchar(50))");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /***
     * Méthode qui insère une partie dans la table Historique
     * @param idGame
     * @param joueur1
     * @param joueur2
     */
    public void Inserer(String idGame, String joueur1, String joueur2)
    {
        try
        {
            this.sql.execSQL("INSERT INTO Historique VALUES ( " + idGame + " ,'" + joueur1 + "','" + joueur2 + "','" + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.CANADA).format(new Date()) + "','Aucun')");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /***
     * Méthode qui met à jour le résultat d'une partie de la table Historique
     * @param idGame
     * @param Resultat
     */
    public void Update(String idGame, Boolean Resultat)
    {
        try
        {
            String Gagnant;
            if(Resultat)
            {
                Gagnant = "1";
            }
            else
            {
                Gagnant = "2";
            }
            this.sql.execSQL("UPDATE Historique SET Resultat = (SELECT Joueur"+ Gagnant +" FROM Historique WHERE IDHistorique = " + idGame +") WHERE IDHistorique = " + idGame);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /***
     * Méthode qui extrait les données de la table Historique dans une liste et la retourne
     * @return
     */
    public List<String> extraireDonnees()
    {
        ArrayList<String> liste = new ArrayList();
        try
        {
            // requête qui récupère les données de l'utilisateur
            String sql = "SELECT * FROM Historique";
            Cursor c = this.sql.rawQuery(sql, null);
            int joueur1 = c.getColumnIndex("Joueur1");
            int joueur2 = c.getColumnIndex("Joueur2");
            int datePartie = c.getColumnIndex("DatePartie");
            int resultat = c.getColumnIndex("Resultat");
            c.moveToFirst();
            // ajout des données dans le tableau
            do
            {
                liste.add(String.format("%s VS %s | GAGNANT : %s\n\t3%s ",c.getString(joueur1),c.getString(joueur2),c.getString(resultat),c.getString(datePartie)));
                Log.i("J'ajoute", "extraireDonnees: ");

            }
            while (c.moveToNext());
            c.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return liste;
    }
}
