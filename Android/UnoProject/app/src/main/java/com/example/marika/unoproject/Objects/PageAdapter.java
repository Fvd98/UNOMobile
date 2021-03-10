package com.example.marika.unoproject.Objects;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.marika.unoproject.Activities.Login_Contenu.F_Connexion;
import com.example.marika.unoproject.Activities.Login_Contenu.F_Historique;
import com.example.marika.unoproject.Activities.Login_Contenu.F_Inscription;


/**
 * Created by Marika on 2018-04-16.
 */

public class PageAdapter extends FragmentStatePagerAdapter {
    int nbTabs;

    public PageAdapter(FragmentManager fm, int nbTabs) {
        super(fm);
        this.nbTabs = nbTabs;
    }

    /***
     * Méthode qui permet d'associer le fragment qui sera visible à l'onglet sélectionné.
     * @param position
     * @return
     */
    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                F_Connexion tab1 = new F_Connexion();
                return tab1;
            case 1:
                F_Inscription tab2 = new F_Inscription();
                return tab2;
            case 2:
                F_Historique tab3 = new F_Historique();
                return tab3;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return this.nbTabs;
    }
}
