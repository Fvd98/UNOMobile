package serveurprojet;

/*** @author Francis Vermette-David et Marika Groulx */

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
        this._ChangerCouleur = null;
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
 
    public void setChangerCouleur(Couleur _ChangerCouleur) 
    {
        this._ChangerCouleur = _ChangerCouleur;
    }
     
    /***
     * Méthode qui vérifie si la carte peut être jouée selon la dernière carte jouée
     * @param DerniereCarteRecu
     * @return 
     */
    private Boolean EstCompatible(Carte DerniereCarteRecu)
    {
        if(this._Couleur == Couleur.NOIR || DerniereCarteRecu._Couleur == this._Couleur || DerniereCarteRecu._Type == this._Type || DerniereCarteRecu._ChangerCouleur == this._Couleur)
        {
            DerniereCarteRecu._ChangerCouleur = null;
            return true;
        }
        else
        {
            return false;
        }
    }
    
    public enum Type   
    {
        C0,C1,C2,C3,C4,C5,C6,C7,C8,C9,
        CPlus2,Cplus4,
        CColor,CSkip,CTurn;
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