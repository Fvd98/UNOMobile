package com.example.marika.unoproject.Objects;

import android.util.Log;

public class Carte
{
    private final int _ID;
    private final Type _Type;
    private final Couleur _Couleur;
    private Couleur _ChangerCouleur;

    public Carte(Type Type, Couleur Couleur, int ID)
    {
        this._Type = Type;
        this._Couleur = Couleur;
        this._ChangerCouleur = null; // buffer qui contient le choix de couleur d'un joueur
        this._ID = ID;
    }

    public int getID()
    {
        return _ID;
    }
    public Type getType()
    {
        return _Type;
    }
    public Couleur getCouleur()
    {
        return _Couleur;
    }
    public Couleur get_ChangerCouleur()
    {
        return _ChangerCouleur;
    }

    public void setChangerCouleur(Couleur _ChangerCouleur)
    {
        this._ChangerCouleur = _ChangerCouleur;
    }

    /***
     * Méthode qui vérifie si une carte peut être jouée (retourne true) selon la dernière carte jouée
     * @param DerniereCarteRecu
     * @return
     */
    public Boolean EstCompatible(Carte DerniereCarteRecu)
    {
        Log.i("Carte  ", String.format("Derniere Carte : %s VS Carte Comparée : %s",DerniereCarteRecu.toString(),this.toString()));
        Log.i("Comparaison ",String.format("Ma carte est noir : %s, Même couleur : %s , Même type : %s , CarteChanger même couleur : %s ",String.valueOf(this._Couleur == Couleur.NOIR),String.valueOf(DerniereCarteRecu._Couleur == this._Couleur),String.valueOf(DerniereCarteRecu._Type == this._Type),String.valueOf(DerniereCarteRecu._ChangerCouleur == this._Couleur)));
        if((this._Couleur == Couleur.NOIR || DerniereCarteRecu._Couleur == this._Couleur || DerniereCarteRecu._Type == this._Type || DerniereCarteRecu._ChangerCouleur == this._Couleur) && DerniereCarteRecu != null)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public enum Type
    {
        C0,C1,C2,C3,C4,C5,C6,C7,C8,C9
        ,CPlus2,Cplus4
        ,CColor,CSkip,CTurn;
    }
    public enum Couleur
    {
        ROUGE,
        BLEU,
        JAUNE,
        VERT,
        NOIR;
    }

    @Override
    public String toString()
    {
        return String.format("%s,%s,%s", this._ID,this._Type,this._Couleur);
    }
}
