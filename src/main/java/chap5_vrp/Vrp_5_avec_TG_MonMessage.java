/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chap5_vrp;

import org.chocosolver.solver.trace.IMessage;
import org.chocosolver.solver.variables.IntVar;
import static chap5_vrp.Vrp_5_avec_TG_Vrp_5.*;


/**
 *
 * @author lacomme
 */
class Vrp_5_avec_TG_MonMessage implements IMessage {
    
     IntVar[] _TG;
    IntVar[] _a;
    IntVar[] _st;
    IntVar[] _s;
    IntVar[] _pred;
    IntVar[] _Position_TG;
    IntVar[] _y;
    
    public Vrp_5_avec_TG_MonMessage(IntVar[] TG, IntVar[] a, IntVar[] st, IntVar[] s, IntVar[] pred) {
        _TG=TG;
        _a=a;
        _st=st;
        _s=s;
        _pred=pred;

    }

    @Override
    public String print() {
        
        String ecrire = "\n   --  Affichage :           ";
        ecrire += "\n";
        ecrire += "            ";

        
        ecrire += "\n";
        ecrire += "            ";
        for (int i = 0; i < N; i++) {
            ecrire += _TG[i].toString() + " ";
            if (i % 5 == 0) {
                ecrire += "\n";
                ecrire += "            ";
            }
        }
        ecrire += "\n";
        
        ecrire += "\n";
        ecrire += "            ";
        for (int i = 0; i < nb_total_visite; i++) {
            ecrire += _a[i].toString() + " ";
            if (i % 5 == 0) {
                ecrire += "\n";
                ecrire += "            ";
            }
        }
        ecrire += "\n";
        
        
        
        ecrire += "\n";
        ecrire += "            ";
        for (int i = 0; i < nb_total_visite; i++) {
            ecrire += _s[i].toString() + " ";
            if (i % 5 == 0) {
                ecrire += "\n";
                ecrire += "            ";
            }
        }
        ecrire += "\n";
        
        
        
        ecrire += "\n";
        ecrire += "            ";
        for (int i = 0; i < nb_total_visite; i++) {
            ecrire += _st[i].toString() + " ";
            if (i % 5 == 0) {
                ecrire += "\n";
                ecrire += "            ";
            }
        }
        ecrire += "\n";
        
        
         ecrire += "\n";
        ecrire += "            ";
        for (int i = 0; i < nb_total_visite; i++) {
            ecrire += _pred[i].toString() + " ";
            if (i % 5 == 0) {
                ecrire += "\n";
                ecrire += "            ";
            }
        }
        ecrire += "\n";
        
        

        
        
        return ecrire;
        
        
    }
    
    
}
