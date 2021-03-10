package serveurprojet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/*** @author Francis Vermette-David et Marika Groulx */

public class PaquetCarte 
{
    private List<Carte> _PileCarte;
    private final Joueur _Proprietaire;
      
    public PaquetCarte() 
    {       
        this._Proprietaire = null;
    }
    
    public PaquetCarte(Joueur _Proprietaire) 
    {
        this._Proprietaire = _Proprietaire;
    }

    public List<Carte> getPileCarte() 
    {
        return _PileCarte;
    }

    public Joueur getProprietaire() {
        return _Proprietaire;
    }
  
    public int Count()
    {
        return this._PileCarte.size();
    }
    
    // méthode qui shuffle le deck
    public final void Shuffle()
    {
        Collections.shuffle(_PileCarte);
    } 
    
    // méthode qui prend la carte du dessus du deck, l'enlève du paquet et la retourne
    public Carte PigerCarte()
    {
        Carte pige = this._PileCarte.get(0);
        this._PileCarte.remove(pige);
        return pige;
    }   
    
    // méthode qui retourne la prochaine carte sans la retirer du paquet
    public Carte Peek()
    {
        return this._PileCarte.get(0);
    } 
    
    // méthode qui ajoute une carte au paquet
    public void Recoit(Carte C)
    {  
        this._PileCarte.add(C);      
    }
    
    // méthode qui reset le paquet
    public void Reset()
    {
       this._PileCarte = new ArrayList();
    }
    
    // méthode qui reset le deck en le regénérant (création de toutes les cartes du jeu) et le brasse
    public void ResetFullDeck()
    {
        int Compteur = 0;
        this._PileCarte = new ArrayList(Arrays.asList(new Carte[] {
            new Carte(Carte.Type.C0,Carte.Couleur.ROUGE, Compteur++),
            new Carte(Carte.Type.C0,Carte.Couleur.BLEU, Compteur++),
            new Carte(Carte.Type.C0,Carte.Couleur.JAUNE, Compteur++),
            new Carte(Carte.Type.C0,Carte.Couleur.VERT, Compteur++),
            
            new Carte(Carte.Type.C1,Carte.Couleur.ROUGE, Compteur++),new Carte(Carte.Type.C1,Carte.Couleur.ROUGE, Compteur++),
            new Carte(Carte.Type.C1,Carte.Couleur.BLEU, Compteur++),new Carte(Carte.Type.C1,Carte.Couleur.BLEU, Compteur++),
            new Carte(Carte.Type.C1,Carte.Couleur.JAUNE, Compteur++),new Carte(Carte.Type.C1,Carte.Couleur.JAUNE, Compteur++),
            new Carte(Carte.Type.C1,Carte.Couleur.VERT, Compteur++),new Carte(Carte.Type.C1,Carte.Couleur.VERT, Compteur++),
            
            new Carte(Carte.Type.C2,Carte.Couleur.ROUGE, Compteur++),new Carte(Carte.Type.C2,Carte.Couleur.ROUGE, Compteur++),
            new Carte(Carte.Type.C2,Carte.Couleur.BLEU, Compteur++),new Carte(Carte.Type.C2,Carte.Couleur.BLEU, Compteur++),
            new Carte(Carte.Type.C2,Carte.Couleur.JAUNE, Compteur++),new Carte(Carte.Type.C2,Carte.Couleur.JAUNE, Compteur++),
            new Carte(Carte.Type.C2,Carte.Couleur.VERT, Compteur++),new Carte(Carte.Type.C2,Carte.Couleur.VERT, Compteur++),
            
            new Carte(Carte.Type.C3,Carte.Couleur.ROUGE, Compteur++),new Carte(Carte.Type.C3,Carte.Couleur.ROUGE, Compteur++),
            new Carte(Carte.Type.C3,Carte.Couleur.BLEU, Compteur++),new Carte(Carte.Type.C3,Carte.Couleur.BLEU, Compteur++),
            new Carte(Carte.Type.C3,Carte.Couleur.JAUNE, Compteur++),new Carte(Carte.Type.C3,Carte.Couleur.JAUNE, Compteur++),
            new Carte(Carte.Type.C3,Carte.Couleur.VERT, Compteur++),new Carte(Carte.Type.C3,Carte.Couleur.VERT, Compteur++),
            
            new Carte(Carte.Type.C4,Carte.Couleur.ROUGE, Compteur++),new Carte(Carte.Type.C4,Carte.Couleur.ROUGE, Compteur++),
            new Carte(Carte.Type.C4,Carte.Couleur.BLEU, Compteur++),new Carte(Carte.Type.C4,Carte.Couleur.BLEU, Compteur++),
            new Carte(Carte.Type.C4,Carte.Couleur.JAUNE, Compteur++),new Carte(Carte.Type.C4,Carte.Couleur.JAUNE, Compteur++),
            new Carte(Carte.Type.C4,Carte.Couleur.VERT, Compteur++),new Carte(Carte.Type.C4,Carte.Couleur.VERT, Compteur++),
            
            new Carte(Carte.Type.C5,Carte.Couleur.ROUGE, Compteur++),new Carte(Carte.Type.C5,Carte.Couleur.ROUGE, Compteur++),
            new Carte(Carte.Type.C5,Carte.Couleur.BLEU, Compteur++),new Carte(Carte.Type.C5,Carte.Couleur.BLEU, Compteur++),
            new Carte(Carte.Type.C5,Carte.Couleur.JAUNE, Compteur++),new Carte(Carte.Type.C5,Carte.Couleur.JAUNE, Compteur++),
            new Carte(Carte.Type.C5,Carte.Couleur.VERT, Compteur++),new Carte(Carte.Type.C5,Carte.Couleur.VERT, Compteur++),
            
            new Carte(Carte.Type.C6,Carte.Couleur.ROUGE, Compteur++),new Carte(Carte.Type.C6,Carte.Couleur.ROUGE, Compteur++),
            new Carte(Carte.Type.C6,Carte.Couleur.BLEU, Compteur++),new Carte(Carte.Type.C6,Carte.Couleur.BLEU, Compteur++),
            new Carte(Carte.Type.C6,Carte.Couleur.JAUNE, Compteur++),new Carte(Carte.Type.C6,Carte.Couleur.JAUNE, Compteur++),
            new Carte(Carte.Type.C6,Carte.Couleur.VERT, Compteur++),new Carte(Carte.Type.C6,Carte.Couleur.VERT, Compteur++),
            
            new Carte(Carte.Type.C7,Carte.Couleur.ROUGE, Compteur++),new Carte(Carte.Type.C7,Carte.Couleur.ROUGE, Compteur++),
            new Carte(Carte.Type.C7,Carte.Couleur.BLEU, Compteur++),new Carte(Carte.Type.C7,Carte.Couleur.BLEU, Compteur++),
            new Carte(Carte.Type.C7,Carte.Couleur.JAUNE, Compteur++),new Carte(Carte.Type.C7,Carte.Couleur.JAUNE, Compteur++),
            new Carte(Carte.Type.C7,Carte.Couleur.VERT, Compteur++),new Carte(Carte.Type.C7,Carte.Couleur.VERT, Compteur++),
            
            new Carte(Carte.Type.C8,Carte.Couleur.ROUGE, Compteur++),new Carte(Carte.Type.C8,Carte.Couleur.ROUGE, Compteur++),
            new Carte(Carte.Type.C8,Carte.Couleur.BLEU, Compteur++),new Carte(Carte.Type.C8,Carte.Couleur.BLEU, Compteur++),
            new Carte(Carte.Type.C8,Carte.Couleur.JAUNE, Compteur++),new Carte(Carte.Type.C8,Carte.Couleur.JAUNE, Compteur++),
            new Carte(Carte.Type.C8,Carte.Couleur.VERT, Compteur++),new Carte(Carte.Type.C8,Carte.Couleur.VERT, Compteur++),
            
            new Carte(Carte.Type.C9,Carte.Couleur.ROUGE, Compteur++),new Carte(Carte.Type.C9,Carte.Couleur.ROUGE, Compteur++),
            new Carte(Carte.Type.C9,Carte.Couleur.BLEU, Compteur++),new Carte(Carte.Type.C9,Carte.Couleur.BLEU, Compteur++),
            new Carte(Carte.Type.C9,Carte.Couleur.JAUNE, Compteur++),new Carte(Carte.Type.C9,Carte.Couleur.JAUNE, Compteur++),
            new Carte(Carte.Type.C9,Carte.Couleur.VERT, Compteur++),new Carte(Carte.Type.C9,Carte.Couleur.VERT, Compteur++),
            
            new Carte(Carte.Type.CSkip,Carte.Couleur.ROUGE, Compteur++),new Carte(Carte.Type.CSkip,Carte.Couleur.ROUGE, Compteur++),
            new Carte(Carte.Type.CSkip,Carte.Couleur.BLEU, Compteur++),new Carte(Carte.Type.CSkip,Carte.Couleur.BLEU, Compteur++),
            new Carte(Carte.Type.CSkip,Carte.Couleur.JAUNE, Compteur++),new Carte(Carte.Type.CSkip,Carte.Couleur.JAUNE, Compteur++),
            new Carte(Carte.Type.CSkip,Carte.Couleur.VERT, Compteur++),new Carte(Carte.Type.CSkip,Carte.Couleur.VERT, Compteur++),
            
            new Carte(Carte.Type.CTurn,Carte.Couleur.ROUGE, Compteur++),new Carte(Carte.Type.CTurn,Carte.Couleur.ROUGE, Compteur++),
            new Carte(Carte.Type.CTurn,Carte.Couleur.BLEU, Compteur++),new Carte(Carte.Type.CTurn,Carte.Couleur.BLEU, Compteur++),
            new Carte(Carte.Type.CTurn,Carte.Couleur.JAUNE, Compteur++),new Carte(Carte.Type.CTurn,Carte.Couleur.JAUNE, Compteur++),
            new Carte(Carte.Type.CTurn,Carte.Couleur.VERT, Compteur++),new Carte(Carte.Type.CTurn,Carte.Couleur.VERT, Compteur++),
            
            new Carte(Carte.Type.CPlus2,Carte.Couleur.ROUGE, Compteur++),new Carte(Carte.Type.CPlus2,Carte.Couleur.ROUGE, Compteur++),
            new Carte(Carte.Type.CPlus2,Carte.Couleur.BLEU, Compteur++),new Carte(Carte.Type.CPlus2,Carte.Couleur.BLEU, Compteur++),
            new Carte(Carte.Type.CPlus2,Carte.Couleur.JAUNE, Compteur++),new Carte(Carte.Type.CPlus2,Carte.Couleur.JAUNE, Compteur++),
            new Carte(Carte.Type.CPlus2,Carte.Couleur.VERT, Compteur++),new Carte(Carte.Type.CPlus2,Carte.Couleur.VERT, Compteur++),      
            
            new Carte(Carte.Type.CColor,Carte.Couleur.NOIR, Compteur++),
            new Carte(Carte.Type.CColor,Carte.Couleur.NOIR, Compteur++),
            new Carte(Carte.Type.CColor,Carte.Couleur.NOIR, Compteur++),
            new Carte(Carte.Type.CColor,Carte.Couleur.NOIR, Compteur++),
            
            new Carte(Carte.Type.Cplus4,Carte.Couleur.NOIR, Compteur++),
            new Carte(Carte.Type.Cplus4,Carte.Couleur.NOIR, Compteur++),
            new Carte(Carte.Type.Cplus4,Carte.Couleur.NOIR, Compteur++),
            new Carte(Carte.Type.Cplus4,Carte.Couleur.NOIR, Compteur++)
        }));
        this.Shuffle();   
    }
    
    // méthode qui trouve une carte selon son id et la retire du paquet
    public Carte findAndRemoveCarteById(int idCarte)
    {
        Carte C = this._PileCarte.stream().filter(c -> c.getID() == idCarte).findAny().get();
        this._PileCarte.remove(C);
        return C;
    }

    //Ex : 01,C3,ROUGE|02,C9,BLEU|03,C3,VERT
    @Override
    public String toString() 
    {
        StringBuilder PaquetJoueur = new StringBuilder();
        for(Carte C : this._PileCarte)
        {
            PaquetJoueur.append(C.toString()).append("|");
        }        
        PaquetJoueur.deleteCharAt(PaquetJoueur.length() - 1);
        return PaquetJoueur.toString();
    }
}
