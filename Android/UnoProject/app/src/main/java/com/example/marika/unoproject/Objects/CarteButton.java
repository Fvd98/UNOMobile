package com.example.marika.unoproject.Objects;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.LinearLayout;

import com.example.marika.unoproject.R;

public class CarteButton extends android.support.v7.widget.AppCompatImageButton
{
    private Carte carte;
    private Drawable couleur;
    private Drawable type;

    /***
     * Constructeur qui créer une Carte Bouton avec la bonne couleur et le bon type en supperposant deux drawables.
     * @param context
     * @param carte
     */
    @SuppressLint("DefaultLocale")
    public CarteButton(Context context, Carte carte)
    {
        super(context);
        this.setScaleType(ScaleType.CENTER_CROP);
        this.setLayoutParams(new LinearLayout.LayoutParams(216, 324));
        this.carte = carte;
        this.setEnabled(false);

        if(this.carte.get_ChangerCouleur() == null)
        {
            switch (this.carte.getCouleur())
            {
                case BLEU:
                    couleur = getResources().getDrawable(R.drawable.back_bleu);
                    break;
                case JAUNE:
                    couleur = getResources().getDrawable(R.drawable.back_jaune);
                    break;
                case VERT:
                    couleur = getResources().getDrawable(R.drawable.back_vert);
                    break;
                case ROUGE:
                    couleur = getResources().getDrawable(R.drawable.back_rouge);
                    break;
                case NOIR:
                    couleur = getResources().getDrawable(R.drawable.back_noir);
            }
        }
        switch (this.carte.getType())
        {
            case C0:
                type = getResources().getDrawable(R.drawable.card0_t);
                break;
            case C1:
                type = getResources().getDrawable(R.drawable.card1_t);
                break;
            case C2:
                type = getResources().getDrawable(R.drawable.card2_t);
                break;
            case C3:
                type = getResources().getDrawable(R.drawable.card3_t);
                break;
            case C4:
                type = getResources().getDrawable(R.drawable.card4_t);
                break;
            case C5:
                type = getResources().getDrawable(R.drawable.card5_t);
                break;
            case C6:
                type = getResources().getDrawable(R.drawable.card6_t);
                break;
            case C7:
                type = getResources().getDrawable(R.drawable.card7_t);
                break;
            case C8:
                type = getResources().getDrawable(R.drawable.card8_t);
                break;
            case C9:
                type = getResources().getDrawable(R.drawable.card9_t);
                break;
            case CSkip:
                type = getResources().getDrawable(R.drawable.card_skip_t);
                break;
            case CTurn:
                type = getResources().getDrawable(R.drawable.card_swap_t);
                break;
            case CPlus2:
                type = getResources().getDrawable(R.drawable.card_plus2_t);
                break;
            case CColor:
                type = getResources().getDrawable(R.drawable.card_color_change_t);
                Carte.Couleur ChangerCouleurCC = this.carte.get_ChangerCouleur();
                if(ChangerCouleurCC != null)
                {
                    AssignerCouleur(ChangerCouleurCC);
                }
                break;
            case Cplus4:
                type = getResources().getDrawable(R.drawable.card_plus4_t);
                Carte.Couleur ChangerCouleurCP4 = this.carte.get_ChangerCouleur();
                if(ChangerCouleurCP4 != null)
                {
                    AssignerCouleur(ChangerCouleurCP4);
                }
                break;
            default:
                type = null;
                break;
        }
        this.setImageDrawable(type);
        this.setBackgroundDrawable(couleur);
    }

    public int get_ID()
    {
        return carte.getID();
    }

    public Carte getCarte()
    {
        return carte;
    }

    private void AssignerCouleur(Carte.Couleur couleurTest)
    {
        switch (couleurTest)
        {
            case BLEU:
                couleur = getResources().getDrawable(R.drawable.back_bleu);
                break;
            case JAUNE:
                couleur = getResources().getDrawable(R.drawable.back_jaune);
                break;
            case VERT:
                couleur = getResources().getDrawable(R.drawable.back_vert);
                break;
            case ROUGE:
                couleur = getResources().getDrawable(R.drawable.back_rouge);
                break;
            case NOIR:
                couleur = getResources().getDrawable(R.drawable.back_noir);
        }
    }
}

