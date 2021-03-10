package serveurprojet;

import java.net.Socket;

/*/*** @author Francis Vermette-David et Marika Groulx */

public class Joueur 
{
    private final String _Username;
    private final String _Nom;
    private final String _Prenom;
    private final String _DateInscription;
    private Socket _scomm;

    public Joueur(String username, String nom, String prenom, String dateInscription) 
    {
        this._Username = username;
        this._Nom = nom;
        this._Prenom = prenom;
        this._DateInscription = dateInscription;
        this._scomm = new Socket();
    }

    public String getDateInscription()
    {
        return _DateInscription;
    }
    
    public String getUsername() 
    {
        return _Username;
    }
    
    public String getNom() 
    {
        return _Nom;
    }
    
    public String getPrenom() 
    {
        return _Prenom;
    }
    
    public Socket getScomm() 
    {
        return _scomm;
    }
    
    public void setScomm(Socket _scomm) 
    {
        this._scomm = _scomm;
    }  

    @Override
    public String toString() {
        return String.format("%s;%s;%s;%s", _Username, _Nom, _Prenom, _DateInscription);
    }
}
