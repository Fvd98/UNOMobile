package serveurprojet;


/*** @author Francis Vermette-David et Marika Groulx */

public class ServeurProjet 
{   
    public static void main(String[] args) 
    {    
        // configurer votre adresse IP
        new ServeurParent("10.4.129.13", 7777).start();
    }    
}
