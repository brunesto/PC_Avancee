/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chap4_jobshop;

import org.chocosolver.solver.trace.IMessage;
import org.chocosolver.solver.variables.IntVar;

/**
 *
 * @author magondra
 */
public class MonMessage implements IMessage {
    IntVar[] _lambda;
    IntVar[] _st;
    int _nb_operation;
    int _nb_machine;
    IntVar[] _rang;

    IntVar[] _pred_machine;

    MonMessage(int nb_operation, IntVar[] st, IntVar[] lambda, IntVar[] pred_machine,
            int nb_machine, IntVar[] rang) {
        _st = st;
        _lambda = lambda;
        _nb_operation = nb_operation;
        _pred_machine = pred_machine;
        _nb_machine = nb_machine;
        _rang = rang;
    }

    @Override
    public String print() {

        String ecrire = "\n   --  Affichage :           ";
        ecrire += "\n";
        ecrire += "            ";

        ecrire += "\n";
        ecrire += "            ";
        for (int i = 0; i < _nb_operation; i++) {
            ecrire += _lambda[i].toString() + " ";
            if (i % _nb_machine == 0) {
                ecrire += "\n";
                ecrire += "            ";
            }
        }
        ecrire += "\n";

        ecrire += "\n";
        ecrire += "            ";
        for (int i = 0; i < _nb_operation; i++) {
            ecrire += _rang[i].toString() + " ";
            if (i % _nb_machine == 0) {
                ecrire += "\n";
                ecrire += "            ";
            }
        }

        ecrire += "\n";
        ecrire += "            ";
        for (int i = 0; i < _nb_operation; i++) {
            ecrire += _st[i].toString() + " ";
            if (i % _nb_machine == 0) {
                ecrire += "\n";
                ecrire += "            ";
            }
        }

        ecrire += "\n";

        ecrire += "\n";
        ecrire += "            ";
        for (int i = 0; i < _nb_operation; i++) {
            ecrire += _pred_machine[i].toString() + " ";
            if (i % _nb_machine == 0) {
                ecrire += "\n";
                ecrire += "            ";
            }
        }
        ecrire += "\n";

        ecrire += "\n";
        ecrire += "            ";

        return ecrire;
    }

}
