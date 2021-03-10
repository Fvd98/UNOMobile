/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serveurprojet;

/**
 *
 * @author info1
 */
public class Partie 
{
    private int idPartie;
    private String tour;

    public String getTour() 
    {
        return tour;
    }
    public void setTour(String tour) 
    {
        this.tour = tour;
    }

    public int getIdPartie() 
    {
        return idPartie;
    }   
    public void setIdPartie(int idPartie) 
    {
        this.idPartie = idPartie;
    }    
    public Partie() 
    {       
    } 
    public Partie(int idPartie) 
    {    
        this.idPartie = idPartie;
    } 
}
