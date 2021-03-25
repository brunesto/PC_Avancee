/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chap2_vrp;

import java.util.Date;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.selectors.variables.DomOverWDeg;
import org.chocosolver.solver.search.strategy.selectors.variables.MaxRegret;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Task;
import org.chocosolver.util.ESat;

public class Modele_VRP_GT_Vrp_3 {

    public static void main(String[] args) {

        int CapaMax = 45;

        // les paramétres
        int N = 20; // nombre de customers dont le dépot fictif qui est le customer 0
        int V = 4; // nombre de véhicules

        // le sommet 0 est le dépot
        int nb_total_visite = N + V * 2;

        // les données
        int H = 1000; // borne sup. de la distance
        int[][] T; // matrice des distances
        int[] D;
        D = new int[nb_total_visite];
        int[] C;
        C = new int[V + 1];

        // les données
        T = new int[][] {
                { 0, 12, 31, 54, 71, 42, 41, 38, 47, 36, 17, 32, 51, 14, 41, 48, 32, 49, 130, 21 },
                { 10, 0, 4, 4, 3, 4, 3, 1, 2, 1, 5, 8, 12, 4, 25, 21, 3, 9, 4, 6 },
                { 30, 4, 0, 4, 1, 1, 3, 1, 1, 1, 1, 1, 3, 1, 5, 2, 0, 2, 1, 16 },
                { 50, 4, 4, 0, 3, 40, 9, 3, 1, 2, 10, 1, 2, 4, 2, 1, 1, 5, 0, 12 },
                { 10, 3, 1, 3, 0, 20, 2, 1, 3, 5, 10, 4, 1, 1, 3, 1, 1, 1, 12, 1 },
                { 40, 4, 1, 40, 2, 0, 4, 5, 1, 12, 1, 1, 3, 1, 15, 2, 0, 2, 11, 16 },
                { 40, 3, 3, 9, 2, 4, 0, 2, 1, 1, 3, 5, 10, 4, 1, 1, 3, 8, 9, 2 },
                { 30, 1, 1, 3, 1, 5, 2, 0, 2, 1, 12, 5, 10, 14, 11, 1, 13, 8, 9, 2 },
                { 40, 2, 1, 1, 3, 1, 1, 2, 0, 1, 12, 15, 9, 14, 41, 11, 13, 8, 9, 2 },
                { 30, 1, 1, 2, 4, 2, 1, 1, 1, 0, 12, 15, 1, 1, 11, 1, 13, 8, 9, 2 },
                { 31, 2, 4, 4, 3, 4, 3, 1, 2, 1, 0, 8, 12, 4, 25, 21, 3, 9, 4, 6 },
                { 32, 9, 7, 4, 1, 1, 3, 1, 1, 1, 1, 0, 3, 1, 5, 2, 0, 2, 1, 16 },
                { 54, 4, 4, 3, 3, 40, 9, 3, 1, 2, 10, 1, 0, 4, 2, 1, 1, 5, 0, 12 },
                { 15, 3, 1, 9, 0, 20, 2, 1, 3, 5, 10, 4, 1, 0, 3, 1, 1, 1, 12, 1 },
                { 46, 4, 1, 40, 2, 0, 4, 5, 1, 12, 1, 1, 3, 1, 0, 2, 0, 2, 11, 16 },
                { 41, 3, 3, 12, 2, 4, 0, 2, 1, 1, 3, 5, 10, 4, 1, 0, 3, 8, 9, 2 },
                { 13, 1, 8, 3, 1, 5, 2, 0, 2, 3, 12, 5, 10, 14, 11, 1, 0, 8, 9, 2 },
                { 43, 2, 1, 1, 3, 1, 1, 2, 0, 1, 14, 15, 1, 8, 41, 11, 9, 0, 9, 2 },
                { 33, 10, 1, 2, 4, 2, 4, 1, 9, 0, 78, 15, 1, 1, 11, 1, 13, 8, 0, 2 },
                { 33, 1, 1, 2, 4, 2, 1, 2, 1, 0, 45, 15, 1, 1, 11, 1, 23, 8, 1, 0 }
        };

        D[0] = 0;
        D[1] = 20;
        D[2] = 15;
        D[3] = 14;
        D[4] = 18;
        D[5] = 7;
        D[6] = 8;
        D[7] = 8;
        D[8] = 4;
        D[9] = 2;
        D[10] = 3;
        D[11] = 3;
        D[12] = 3;
        D[13] = 7;
        D[14] = 3;
        D[15] = 3;
        D[16] = 5;
        D[17] = 3;
        D[18] = 3;
        D[19] = 14;

        for (int i = N; i < nb_total_visite; i++) {
            D[i] = 0;
        }

        C[0] = 0;
        for (int i = 1; i <= V; i++)
            C[i] = CapaMax;

        int[][] T_prime;
        T_prime = new int[nb_total_visite][nb_total_visite];

        // agrandissement de T
        // ajout en fin de colonnes
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                T_prime[i][j] = T[i][j];
            }
            for (int j = 0; j < V * 2; j++) {
                int position = N + j;
                T_prime[i][position] = T[i][0];
            }
        }

        // ajout des lignes finales
        for (int i = N; i < nb_total_visite; i++) {
            for (int j = 0; j < V * 2; j++) {
                int position = N + j;
                T_prime[i][position] = 0;
            }
            for (int j = 0; j < N; j++) {
                T_prime[i][j] = T[0][j];
            }
        }

        // déclaration du modèle
        Model mon_modele;
        mon_modele = new Model("VRP en PPC");

        // Déclaration des variables
        IntVar[] s; // le successeur de chaque visite
        s = new IntVar[nb_total_visite];
        IntVar[] a; // la route (ou le véhicule) qui est affecté à la visite
        a = new IntVar[nb_total_visite];
        IntVar[] p; // rang de la visite dans le trip, similaire au TSP
        p = new IntVar[nb_total_visite];

        // contraintes 1 et 2 : définition de s

        s[0] = mon_modele.intVar("s_0", 0); // le noeud 0 ne compte pas
        for (int i = 1; i < nb_total_visite; i++) {
            if (i < N + V) {
                s[i] = mon_modele.intVar("s_" + i, 1, nb_total_visite);
            } else { // pour les dépots de retour
                s[i] = mon_modele.intVar("s_" + i, i - V);
            }
        }

        // contrainte 3 : définition de l'affectation des routes
        a[0] = mon_modele.intVar("a_0", 0); // sommet 0 est fictif
        for (int i = 1; i < N; i++) {
            a[i] = mon_modele.intVar("a_" + i, 1, V);
        }

        // contrainte 4 : cas des dépots de départ et d'arrivée        
        for (int i = 1; i <= V; i++) {
            int position = N + i - 1; // dépot de départ du véhicule i
            a[position] = mon_modele.intVar("a_" + position, i);
            position = N + i + V - 1; // dépot d'arrivé du véhicule i
            a[position] = mon_modele.intVar("a_" + position, i);
        }

        // contrainte 5 : définition des rangs
        p[0] = mon_modele.intVar("rang_0", -1); // sommet 0 fictif
        for (int i = 1; i < N; i++) {
            p[i] = mon_modele.intVar("rang_" + i, 1, N);
        }

        for (int i = N + V; i < nb_total_visite; i++) {
            p[i] = mon_modele.intVar("rang_" + i, 1, N);
        }

        // contrainte 6 :  les dépot de départ sont toujours au rang 0 !
        for (int i = 0; i < V; i++) {
            int position = N + i;
            p[position] = mon_modele.intVar("p_" + position, 0);
        }

        // contrainte 7 :   tous les successeurs doivent être différent
        //                  contrainte allDifferent, mais ne prend pas en compte les variables valent 0
        mon_modele.allDifferentExcept0(s).post();

        // contrainte 8 : a[i]=a[s[i]]
        for (int i = 1; i < N + V; i++) {
            mon_modele.element(a[i], a, s[i], 0).post();
        }

        //contrainte 9 : evite les sous-tours de taille 1 (optionel)
        for (int i = 1; i < N + V; i++) {
            mon_modele.arithm(s[i], "!=", i).post();
        }

        //contrainte 10 : evite les sous tours
        for (int i = 1; i < N + V; i++) {
            IntVar t = mon_modele.intVar("t_" + i, 0, nb_total_visite);
            mon_modele.arithm(t, "=", p[i], "+", 1).post();
            mon_modele.element(t, p, s[i], 0).post();
        }

        // contrainte 11 : la somme des Ds des clients ne doit pas dépasser la capacité du véhicule
        SetVar[] b;
        b = new SetVar[V + 1];
        int[] x_UB = new int[nb_total_visite]; // valeurs qui peuvent appartenir à l’ensemble
        for (int i = 1; i < nb_total_visite; i++) {
            x_UB[i] = i;
        }
        int[] x_LB = new int[] {}; // valeurs qui doivent appartenir à l’ensemble

        for (int i = 0; i <= V; i++) {
            b[i] = mon_modele.setVar("sets_" + i, x_LB, x_UB);
        }

        // Contrainte 12
        mon_modele.setsIntsChanneling(b, a).post();
        for (int i = 1; i <= V; i++) {
            IntVar sum = mon_modele.intVar("Sum_b_" + i, 0, C[i]);
            mon_modele.sumElements(b[i], D, sum).post();
        }

        // Contrainte 13
        IntVar[] dp; // distance entre i et j
        IntVar d; // objectif
        dp = new IntVar[nb_total_visite];
        dp[0] = mon_modele.intVar(0);
        for (int i = 1; i < nb_total_visite; i++) {
            dp[i] = mon_modele.intVar("dp_" + i, 0, H);
        }

        for (int i = 1; i < N + V; i++) {
            mon_modele.element(dp[i], T_prime[i], s[i]).post();
        }
        for (int i = N + V; i < nb_total_visite; i++) {
            mon_modele.arithm(dp[i], "=", 0).post();
        }
        d = mon_modele.intVar("d", 0, H);
        mon_modele.sum(dp, "=", d).post();
        // fonction objective
        mon_modele.setObjective(Model.MINIMIZE, d);

        // contrainte 14    
        for (int i = N; i < N + V - 1; i++) {
            mon_modele.arithm(s[i], "<", s[i + 1]).post();
        }

        Solver mon_solveur = mon_modele.getSolver();

        // lier les variables
        IntVar[] y;
        y = new IntVar[nb_total_visite];
        y[0] = mon_modele.intVar(0);
        for (int i = 1; i < nb_total_visite; i++) {
            y[i] = mon_modele.intVar("y_" + i, 0, 100 * H + nb_total_visite);
        }

        int[] coeffs = new int[3];
        coeffs[0] = 1;
        coeffs[1] = -100;
        coeffs[2] = -1;
        for (int i = 1; i < nb_total_visite; i++) {
            IntVar[] ligne = new IntVar[3];
            ligne[0] = y[i];
            ligne[1] = a[i];
            ligne[2] = s[i];
            mon_modele.scalar(ligne, coeffs, "=", 0).post();
        }

        //        contrainte cumulative
        Task[] tasks = new Task[N - 1];
        for (int i = 0; i < N - 1; i++) {
            tasks[i] = mon_modele.taskVar(a[i + 1], 1);
        }
        IntVar Demande[] = new IntVar[N - 1];
        for (int i = 0; i < N - 1; i++) {
            Demande[i] = mon_modele.intVar(D[i + 1]);
        }
        IntVar Int_CapaMax;
        Int_CapaMax = mon_modele.intVar(CapaMax);

        mon_modele.cumulative(tasks, Demande, Int_CapaMax).post();

        // contraintes de channeling
        IntVar[] pred = mon_modele.intVarArray("pred", nb_total_visite, 0, nb_total_visite);
        mon_modele.inverseChanneling(s, pred).post();

        //ajout de la variable de cumul de capacite : CAPASUM
        IntVar[] capaSumSucc = mon_modele.intVarArray("capaSumSucc", nb_total_visite, 0, CapaMax);
        IntVar[] capaSum = mon_modele.intVarArray("capaSum", nb_total_visite, 0, CapaMax);
        mon_modele.arithm(capaSum[0], "=", D[0]).post();
        for (int i = 1; i < N + V; i++) {
            mon_modele.element(capaSumSucc[i], capaSum, s[i], 0).post();
            mon_modele.arithm(capaSum[i], "-", capaSumSucc[i], "=", D[i]).post();
        }
        for (int i = N + V; i < nb_total_visite; i++) {
            mon_modele.arithm(capaSum[i], "=", D[i]).post();
        }

        // version 1
        /*
        int[][] T_second = new int[nb_total_visite][nb_total_visite];
        for (int i=0; i<nb_total_visite; i++){
                for (int j=0; j<nb_total_visite; j++){
                        T_second[i][j]=T_prime[i][j]*1000+j;
                }
        }
        */

        // version 2

        int[][] T_second = new int[nb_total_visite][nb_total_visite];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < nb_total_visite; j++) {
                T_second[i][j] = T_prime[i][j] * 1000 + j;
            }
        }

        for (int i = N; i < N + 2 * V; i++) {
            for (int j = 0; j < nb_total_visite; j++) {
                T_second[i][j] = T_prime[i][j];
            }
        }

        IntVar[] dps = mon_modele.intVarArray("dps", nb_total_visite, 0, 100 * H + nb_total_visite, false);
        for (int i = 1; i < nb_total_visite; i++) {
            mon_modele.element(dps[i], T_second[i], s[i]).post();
        }
        mon_modele.arithm(dps[0], "=", 0).post();

        mon_solveur.showStatistics();

        // Stratégie 1 : DomOverWDeg
        mon_solveur.setSearch(
                new DomOverWDeg(dps, // variable de branchement
                        0, // seed
                        new IntDomainMin() // choix de la valeur    
                ));

        // Stratégie 2 : MaxRegret
        mon_solveur.setSearch(
                Search.intVarSearch(
                        new MaxRegret(), // selecteur de variable
                        new IntDomainMin(), // choix de la valeur
                        dps) // variable de branchement
        );

        Solution solution = new Solution(mon_modele);

        Date heure_debut = new Date();
        long h_debut = heure_debut.getTime();

        mon_solveur.limitTime("60s");

        while (mon_solveur.solve()) {
            solution.record();
            Date heure_fin = new Date();
            long h_fin = heure_fin.getTime();
            long duree = h_fin - h_debut;
            long duree_s = duree / 1000;
            System.out.println("temps : " + duree_s + " s    " + d.toString());
            System.out.println("----------");
        }

        // affichage final si une solution a été trouvée
        if (mon_solveur.isFeasible() == ESat.TRUE) {

            for (int i = 0; i < nb_total_visite; i++) {
                int suc = solution.getIntVal(s[i]);
                int affec = solution.getIntVal(a[i]);
                int ran = solution.getIntVal(p[i]);
                System.out.println("Sommet " + i + "    Succ : " + suc + "    |   affec " + affec + "    |   rang " + ran);
            }
            int cout = (int) mon_solveur.getBestSolutionValue();
            Date heure_fin = new Date();
            long h_fin = heure_fin.getTime();
            long duree = h_fin - h_debut;
            long duree_s = duree / 1000;

            System.out.println("temps : " + duree_s + " s   d = " + cout);
            System.out.println("Fin");
        } else
            System.out.println("pas de solution");
    }

}
