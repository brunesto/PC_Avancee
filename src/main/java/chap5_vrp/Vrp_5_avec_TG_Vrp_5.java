/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chap5_vrp;

import static chap5_vrp.Vrp_5_avec_TG_Lecture_fichier.ecrire_resultat_ds_fichier;
import static chap5_vrp.Vrp_5_avec_TG_Lecture_fichier.initialiser_fichier_sortie;
import static chap5_vrp.Vrp_5_avec_TG_Lecture_fichier.lire_fichier;
import static chap5_vrp.Vrp_5_avec_TG_Lecture_fichier.melangerTableau;
import static org.chocosolver.solver.search.strategy.assignments.DecisionOperatorFactory.makeIntSplit;

import java.util.Date;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainLast;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMiddle;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.selectors.variables.FirstFail;
import org.chocosolver.solver.search.strategy.selectors.variables.MaxRegret;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Task;
import org.chocosolver.util.ESat;

public class Vrp_5_avec_TG_Vrp_5 {

    static int CapaMax = 10;

    // les paramétres
    static int N = 10; // nombre de customers dont le dépot fictif qui est le customer 0
    static int V = 4; // nombre de véhicules
    static int nb_total_visite = N + V * 2;

    static int H = 20000; // borne sup. de la distance
    static int[][] T; // matrice des distances
    static int[] D;
    static int[] C;
    static int[] TW_Min;
    static int[] TW_Max;
    static int[] Pt;
    static int TW_Min_vehicule;
    static int TW_Max_vehicule;

    static int nb_C1;
    static int[] Tab_1;

    // les synchro
    // -----------
    static int nb_C2;
    static int[] Tab_2;

    static int nb_C3;
    static int[] Tab_3;

    static int nb_C4;
    static int[] Tab_4;

    static int nb_C5;
    static int[] Tab_5;

    static int nb_C6;
    static int[] Tab_6;

    static int nb_C7;
    static int[] Tab_7;

    static int nb_C8;
    static int[] Tab_8;

    //-------------------
    static int[][] T_prime;

    static int[] MySolution_S;
    static int[] MySolution_a;
    static int[] MySolution_p;
    static int[] MySolution_dps;

    static int[] Affectation_initiale;
    static int[] Suivant_initiale;

    static int nb_c_de_synchro = 0;

    public static java.util.Random generator;

    public static Solution solution;

    public static IntVar[] a;
    public static IntVar[] s;
    public static IntVar[] p;
    public static Model mon_modele;
    public static IntVar[] dp; // distance entre i et j
    public static IntVar d; // objectif
    public static IntVar[] st;
    public static IntVar[] dd;
    public static IntVar[] da;
    public static IntVar[] df;
    public static int Dmax = 200000;
    public static int[] Y_dps;

    public static int[] Tour_Geant;
    public static IntVar[] TG;
    public static int distance_max_au_tg = -1;

    public static IntVar[] capaSum;
    public static IntVar[] pred;

    //**************************** resolution "limitee" ************************

    public static boolean resoudre(int distance1,
            int distance2,
            int limitesup,
            int borne_inf,
            int duree_max) {

        // déclaration du modèle
        mon_modele = new Model("VRP + Synch en PPC");

        // Déclaration des variables        
        s = new IntVar[nb_total_visite]; // le successeur de chaque visite         
        a = new IntVar[nb_total_visite]; // la route (ou le véhicule) qui est affecté à la visite              
        p = new IntVar[nb_total_visite]; // rang de la visite dans le trip, similaire au TSP

        dp = new IntVar[nb_total_visite];
        d = mon_modele.intVar("d", 0, H);

        // date de debut de service         
        st = new IntVar[nb_total_visite];
        // date de depart          
        dd = new IntVar[nb_total_visite];
        // date de depart    
        da = new IntVar[nb_total_visite];
        // date de fin         
        df = new IntVar[nb_total_visite];

        // tour geant
        TG = new IntVar[N];

        // contraintes 1 et 2 : définition de s
        s[0] = mon_modele.intVar("s_0", 0); // le noeud 0 ne compte pas
        for (int i = 1; i < nb_total_visite; i++) {
            if (i < N + V) { // concerne les clients et les dépots de départ de tous les véhicules
                s[i] = mon_modele.intVar("s_" + i, 1, nb_total_visite, false);
            } else { // pour les dépots de retour
                s[i] = mon_modele.intVar("s_" + i, i - V);
            }
        }

        // contrainte 3 : définition de l'affectation des routes
        a[0] = mon_modele.intVar("a_0", 0); // sommet 0 est fictif
        for (int i = 1; i < N; i++) {
            a[i] = mon_modele.intVar("a_" + i, 1, V, false);
        }

        // contrainte 4 : cas des dépots de départ et d'arrivée        
        for (int i = 1; i <= V; i++) {
            int position = N + i - 1; // dépot de départ du véhicule i
            a[position] = mon_modele.intVar("a_" + position, i);
            position = N + i + V - 1; // dépot d'arrivé du véhicule i
            a[position] = mon_modele.intVar("a_" + position, i);
        }

        // contrainte 5 : définition des rangs
        p[0] = mon_modele.intVar("p_0", 0); // sommet 0 fictif
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

        // contrainte 12
        mon_modele.setsIntsChanneling(b, a).post();
        for (int i = 1; i <= V; i++) {
            IntVar sum = mon_modele.intVar("Sum_b_" + i, 0, C[i]);
            mon_modele.sumElements(b[i], D, sum).post();
        }

        // contrainte 13
        dp[0] = mon_modele.intVar(0);
        for (int i = 1; i < N + V; i++) {
            dp[i] = mon_modele.intVar("dp_" + i, 0, H);
        }
        for (int i = N + V; i < nb_total_visite; i++) {
            dp[i] = mon_modele.intVar("dp_" + i, 0);
        }
        for (int i = 1; i < N + V; i++) {
            mon_modele.element(dp[i], T_prime[i], s[i]).post();
        }

        mon_modele.sum(dp, "=", d).post();

        //contrainte 20
        IntVar[] capaSumSucc = mon_modele.intVarArray("capaSumSucc", nb_total_visite, 0, CapaMax);
        capaSum = mon_modele.intVarArray("capaSum", nb_total_visite, 0, CapaMax);
        mon_modele.arithm(capaSum[0], "=", D[0]).post();
        for (int i = 1; i < N + V; i++) {
            mon_modele.element(capaSumSucc[i], capaSum, s[i], 0).post();
            mon_modele.arithm(capaSum[i], "-", capaSumSucc[i], "=", D[i]).post();
        }
        for (int i = N + V; i < nb_total_visite; i++) {
            mon_modele.arithm(capaSum[i], "=", D[i]).post();
        }

        // contrainte 21
        // declaration de DPS
        // dps = 100 * dp + s (usefull for enumeration => min dist, min index succ in one step)
        // based on T_second
        int[][] T_second = new int[nb_total_visite][nb_total_visite];
        for (int i = 0; i < nb_total_visite; i++) {
            for (int j = 0; j < nb_total_visite; j++) {
                T_second[i][j] = T_prime[i][j] * 100 + j;
            }
        }

        // contrainte 22 - 23
        IntVar[] dps = mon_modele.intVarArray("dps", nb_total_visite, 0, 100 * H + nb_total_visite, false);
        for (int i = 1; i < nb_total_visite; i++) {
            mon_modele.element(dps[i], T_second[i], s[i]).post();
        }
        mon_modele.arithm(dps[0], "=", 0).post();

        // contrainte 24.1 ... 24.5
        st[0] = mon_modele.intVar("st_0", 0);
        dd[0] = mon_modele.intVar("dd_0", 0);
        da[0] = mon_modele.intVar("da_0", 0);
        df[0] = mon_modele.intVar("df_0", 0);
        for (int i = 1; i < nb_total_visite; i++) {
            if (i < N) { // concerne les clients et les dépots de départ de tous les véhicules
                st[i] = mon_modele.intVar("st_" + i, TW_Min[i], TW_Max[i]);
                dd[i] = mon_modele.intVar("dd_" + i, TW_Min[i], Dmax);
                da[i] = mon_modele.intVar("da_" + i, 0, TW_Max[i]);
                df[i] = mon_modele.intVar("df_" + i, 0, Dmax);
            } else if (i < N + V) {
                // concerne les clients et les dépots de départ de tous les véhicules
                st[i] = mon_modele.intVar("st_" + i, TW_Min_vehicule);
                dd[i] = mon_modele.intVar("dd_" + i, TW_Min_vehicule);
                da[i] = mon_modele.intVar("da_" + i, TW_Min_vehicule);
                df[i] = mon_modele.intVar("df_" + i, TW_Min_vehicule);
            } else { // pour les dépots de retour
                st[i] = mon_modele.intVar("st_" + i, 0, Dmax); // les successeurs des dépots d'arrivée valent 0 (pas de successeurs possibles)
                dd[i] = mon_modele.intVar("dd_" + i, Dmax);
                da[i] = mon_modele.intVar("da_" + i, 0, TW_Max_vehicule);
                df[i] = mon_modele.intVar("df_" + i, 0, Dmax);
            }
        }

        // contrainte 24.5.
        mon_modele.arithm(st[0], "=", 0).post();

        // contrainte 24.6 ... 24.10
        for (int i = 1; i < nb_total_visite; i++) {
            if (i < N + V) { // concerne les clients et les dépots de départ de tous les véhicules
                // contrainte 24.6.1
                IntVar d1 = mon_modele.intVar("d1_" + i, 0, Dmax);
                mon_modele.element(d1, da, s[i], 0).post();
                // contrainte 24.6.2
                mon_modele.arithm(dd[i], "+", dp[i], "=", d1).post();
                // containre 24.7. et 24.8
                mon_modele.arithm(st[i], ">=", TW_Min[i]).post();
                mon_modele.arithm(st[i], ">=", da[i]).post();
                //  contraine 24.9
                mon_modele.arithm(df[i], "=", st[i], "+", Pt[i]).post();
                // contrainte 24.10
                mon_modele.arithm(df[i], "=", dd[i]).post();
            } else {
                mon_modele.arithm(st[i], "=", da[i]).post();
                mon_modele.arithm(df[i], "=", st[i]).post();
            }
        }

        // contrainte 25     
        if (nb_C1 > 0) {
            IntVar lim_cumulative = mon_modele.intVar("Lim_cumulative", 1);
            Task[] tasks = new Task[nb_C1];
            IntVar[] heights = mon_modele.intVarArray("Heights", nb_C1, 1, 1);
            for (int i = 0; i < nb_C1; i++) {
                int j = Tab_1[i];
                System.out.println(st[j] + "  " + Pt[j]);
                tasks[i] = mon_modele.taskVar(st[j], Pt[j]);
            }
            mon_modele.cumulative(tasks, heights, lim_cumulative).post();
        }

        // contrainte 26     
        if (nb_c_de_synchro >= 1) {
            int ni = Tab_2[0];
            int nj = Tab_2[1];
            mon_modele.arithm(st[ni], "=", st[nj]).post();
            // mon_modele.arithm(a[ni], "!=", a[nj]).post();
        }
        if (nb_c_de_synchro >= 2) {
            int ni = Tab_3[0];
            int nj = Tab_3[1];
            mon_modele.arithm(st[ni], "=", st[nj]).post();
            // mon_modele.arithm(a[ni], "!=", a[nj]).post();
        }
        if (nb_c_de_synchro >= 3) {
            int ni = Tab_4[0];
            int nj = Tab_4[1];
            mon_modele.arithm(st[ni], "=", st[nj]).post();
            //  mon_modele.arithm(a[ni], "!=", a[nj]).post();
        }
        if (nb_c_de_synchro >= 4) {
            int ni = Tab_5[0];
            int nj = Tab_5[1];
            mon_modele.arithm(st[ni], "=", st[nj]).post();
            //   mon_modele.arithm(a[ni], "!=", a[nj]).post();
        }
        if (nb_c_de_synchro >= 5) {
            int ni = Tab_6[0];
            int nj = Tab_6[1];
            mon_modele.arithm(st[ni], "=", st[nj]).post();
            //   mon_modele.arithm(a[ni], "!=", a[nj]).post();
        }

        if (nb_c_de_synchro >= 6) {
            int ni = Tab_7[0];
            int nj = Tab_7[1];
            mon_modele.arithm(st[ni], "=", st[nj]).post();
            //   mon_modele.arithm(a[ni], "!=", a[nj]).post();
        }

        if (nb_c_de_synchro >= 7) {
            int ni = Tab_8[0];
            int nj = Tab_8[1];
            mon_modele.arithm(st[ni], "=", st[nj]).post();
            //   mon_modele.arithm(a[ni], "!=", a[nj]).post();
        }

        // containte 27
        TG[0] = mon_modele.intVar("TG_0", 0);
        for (int i = 1; i < N; i++) {
            TG[i] = mon_modele.intVar("TG_" + i, 1, N - 1, false);
        }

        // contrainte numéro 28.1
        IntVar cour1 = mon_modele.intVar("cour", 1, N);
        mon_modele.arithm(cour1, "=", TG[1]).post();
        // contrainte numéro 28.2
        IntVar a_cour1 = mon_modele.intVar("a_cour", 0, N);
        mon_modele.element(a_cour1, a, cour1, 0).post();
        // contrainte numéro 28.3
        mon_modele.arithm(a_cour1, "=", 1).post();

        // contrainte 29.x
        for (int i = 1; i < N - 1; i++) {
            // contrainte 29.1 
            IntVar cour = mon_modele.intVar("cour", 1, N);
            mon_modele.arithm(cour, "=", TG[i]).post();
            // contrainte 29.2
            IntVar a_cour = mon_modele.intVar("a_cour", 0, N);
            mon_modele.element(a_cour, a, cour, 0).post();
            // contrainte 29.3 
            IntVar suiv = mon_modele.intVar("suiv", 1, N);
            mon_modele.arithm(suiv, "=", TG[i + 1]).post();
            // contrainte 29.4 
            IntVar a_suiv = mon_modele.intVar("a_suiv", 0, N);
            mon_modele.element(a_suiv, a, suiv, 0).post();
            // contrainte 29.5 
            mon_modele.arithm(a_suiv, ">=", a_cour).post();
            // contrainte 30.1.
            BoolVar Change = mon_modele.boolVar("Change"); // distance entre i et j
            mon_modele.reification(Change, mon_modele.arithm(a_cour, "!=", a_suiv));
            // contrainte 30.2.           
            IntVar suivant_de_cour = mon_modele.intVar("suivant_de_cour", 0, N + V + V);
            mon_modele.element(suivant_de_cour, s, cour, 0).post();
            // contrainte 30.3.           
            mon_modele.reification(Change, mon_modele.arithm(suivant_de_cour, ">=", N + V));
        }

        // contrainte 31.x
        for (int i = 2; i < N; i++) {
            IntVar cour = mon_modele.intVar("cour", 1, N);
            mon_modele.arithm(cour, "=", TG[i]).post();

            for (int j = 1; j < i; j++) {
                // contrainte 31.1 
                IntVar precj = mon_modele.intVar("prec" + j, 0, N);
                mon_modele.arithm(precj, "=", TG[j]).post();
                // contrainte 31.2
                IntVar succ_cour = mon_modele.intVar("succ_cour", 0, nb_total_visite);
                mon_modele.element(succ_cour, s, cour, 0).post();
                // contrainte 31.3
                mon_modele.arithm(succ_cour, "!=", precj).post();
            }
        }

        // contrainte 32
        mon_modele.allDifferent(TG).post();

        // contrainte 33
        IntVar DistTG = mon_modele.intVar("DistanceTG", 0, N);
        // contrainte 34
        BoolVar[] dist_TG = mon_modele.boolVarArray("DistanceTG", N);
        for (int u = 1; u < N; u++) {
            // contrainte 35
            mon_modele.reifyXneC(TG[u], Tour_Geant[u], dist_TG[u]);
        }
        // contrainte 36
        mon_modele.sum(dist_TG, "=", DistTG).post();
        // contrainte 37
        mon_modele.arithm(DistTG, "<=", distance_max_au_tg).post();

        // contraintes 38
        IntVar Dista = mon_modele.intVar("Distance", 0, N);
        // contraintes 39
        BoolVar[] dist_a = mon_modele.boolVarArray("Distance", N); // distance entre i et j
        mon_modele.arithm(dist_a[0], "=", 0).post();
        for (int u = 1; u < N; u++) {
            // contrainte 49
            mon_modele.reifyXneC(a[u], Affectation_initiale[u], dist_a[u]);
        }
        // contrainte 41
        mon_modele.sum(dist_a, "=", Dista).post();
        // contrainte 42
        mon_modele.arithm(Dista, "<=", distance1).post();

        // contraintes 43
        IntVar DistS = mon_modele.intVar("DistanceS", 0, N);
        // contraintes 44
        BoolVar[] dist_S = mon_modele.boolVarArray("DistanceS", N); // distance entre i et j
        mon_modele.arithm(dist_S[0], "=", 0).post();

        for (int u = 1; u < N; u++) {
            // contrainte 45
            mon_modele.reifyXneC(s[u], Suivant_initiale[u], dist_S[u]);
        }
        // contrainte 46
        mon_modele.sum(dist_S, "=", DistS).post();
        // contrainte 47
        mon_modele.arithm(DistS, "<=", distance2).post();

        // containte 48 et 49 
        mon_modele.arithm(d, ">=", borne_inf).post();
        mon_modele.arithm(d, "<=", limitesup).post();

        // fonction objective
        mon_modele.setObjective(Model.MINIMIZE, d);

        // définition du solveur
        Solver mon_solveur = mon_modele.getSolver();

        // option pendant la résolution
        mon_solveur.showShortStatistics();

        // personnalisation de l'affichage
        Vrp_5_avec_TG_MonMessage message = new Vrp_5_avec_TG_MonMessage(TG, a, st, s, pred);

        // Repartir d'une solution
        Solution solution_init = new Solution(mon_modele);
        for (int i = 0; i < N; i++) {
            if (i == 0) {
                solution_init.setIntVal(a[i], 0);
            } else if (i < N + V) {
                solution_init.setIntVal(a[i], Affectation_initiale[i]);
            }
        }
        for (int i = 1; i <= V; i++) {
            int position = N + i - 1; // dépot de départ du véhicule i
            solution_init.setIntVal(a[position], i);
        }

        for (int i = 0; i < nb_total_visite; i++) {
            solution_init.setIntVal(s[i], Suivant_initiale[i]);
        }
        for (int i = 0; i < N; i++) {
            solution_init.setIntVal(TG[i], Tour_Geant[i]);
        }

        // définir la politique de branchement

        mon_solveur.setSearch(
                Search.intVarSearch(
                        new FirstFail(mon_modele), // selecteur de variable
                        new IntDomainLast(solution_init, new IntDomainMin()), // choix de la valeur
                        TG),
                Search.intVarSearch(
                        new FirstFail(mon_modele), // selecteur de variable
                        new IntDomainLast(solution_init, new IntDomainMin()), // choix de la valeur
                        s),
                Search.intVarSearch(
                        new FirstFail(mon_modele), // selecteur de variable
                        new IntDomainMin(), // choix de la valeur
                        d));

        solution = new Solution(mon_modele);

        // debut de la recherche

        Date heure_debut = new Date();
        long h_debut = heure_debut.getTime();

        String chaine = duree_max + "s";
        mon_solveur.limitTime(chaine);

        while (mon_solveur.solve()) {
            solution.record();
            System.out.println("-------------");
            Date heure_fin = new Date();
            long h_fin = heure_fin.getTime();
            long duree = h_fin - h_debut;
            long duree_s = duree / 1000;
            System.out.println("temps : " + duree_s + " s    " + d.toString());
            System.out.println("----------");
        }

        // affichage final si une solution a été trouvée
        if (mon_solveur.isFeasible() == ESat.TRUE) {
            System.out.println("Solution trouvee");
            affichage_solution_short(solution, mon_solveur, s, a, p, capaSum, st);
            return true;
        } else {
            System.out.println("pas de solution");
            return false;
        }
    }

    public static int resoudre_Normale(int distance1,
            int distance2,
            int limitesup,
            int borne_inf,
            int duree_max) {

        // déclaration du modèle
        mon_modele = new Model("VRP en PPC");

        // Déclaration des variables        
        s = new IntVar[nb_total_visite]; // le successeur de chaque visite         
        a = new IntVar[nb_total_visite]; // la route (ou le véhicule) qui est affecté à la visite              
        p = new IntVar[nb_total_visite]; // rang de la visite dans le trip, similaire au TSP

        dp = new IntVar[nb_total_visite];
        d = mon_modele.intVar("d", 0, H);

        // date de debut de service         
        st = new IntVar[nb_total_visite];
        // date de depart          
        dd = new IntVar[nb_total_visite];
        // date de depart    
        da = new IntVar[nb_total_visite];
        // date de fin         
        df = new IntVar[nb_total_visite];

        // tour geant
        TG = new IntVar[N];

        // contraintes 1 et 2 : définition de s
        s[0] = mon_modele.intVar("s_0", 0); // le noeud 0 ne compte pas
        for (int i = 1; i < nb_total_visite; i++) {
            if (i < N + V) { // concerne les clients et les dépots de départ de tous les véhicules
                s[i] = mon_modele.intVar("s_" + i, 1, nb_total_visite, false);
            } else { // pour les dépots de retour
                s[i] = mon_modele.intVar("s_" + i, i - V); // les successeurs des dépots d'arrivée valent 0 (pas de successeurs possibles)
            }
        }

        // contrainte 3 : définition de l'affectation des routes
        a[0] = mon_modele.intVar("a_0", 0); // sommet 0 est fictif
        for (int i = 1; i < nb_total_visite; i++) {
            a[i] = mon_modele.intVar("a_" + i, 1, V, false);
        }

        // contrainte 4 : cas des dépots de départ et d'arrivée        
        for (int i = 1; i <= V; i++) {
            int position = N + i - 1; // dépot de départ du véhicule i
            a[position] = mon_modele.intVar("a_" + position, i);
            position = N + i + V - 1; // dépot d'arrivé du véhicule i
            a[position] = mon_modele.intVar("a_" + position, i);
        }

        // contrainte 5 : définition des rangs
        p[0] = mon_modele.intVar("p_0", 0); // sommet 0 fictif
        for (int i = 1; i < nb_total_visite; i++) {
            p[i] = mon_modele.intVar("p_" + i, 1, N, false);
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

        // contrainte 13
        dp[0] = mon_modele.intVar(0);
        for (int i = 1; i < N + V; i++) {
            dp[i] = mon_modele.intVar("dp_" + i, 0, H);
        }
        for (int i = N + V; i < nb_total_visite; i++) {
            dp[i] = mon_modele.intVar("dp_" + i, 0);
        }

        for (int i = 1; i < N + V; i++) {
            mon_modele.element(dp[i], T_prime[i], s[i]).post();
        }

        mon_modele.sum(dp, "=", d).post();

        //contrainte 20
        IntVar[] capaSumSucc = mon_modele.intVarArray("capaSumSucc", nb_total_visite, 0, CapaMax);
        capaSum = mon_modele.intVarArray("capaSum", nb_total_visite, 0, CapaMax);
        mon_modele.arithm(capaSum[0], "=", D[0]).post();
        for (int i = 1; i < N + V; i++) {
            mon_modele.element(capaSumSucc[i], capaSum, s[i], 0).post();
            mon_modele.arithm(capaSum[i], "-", capaSumSucc[i], "=", D[i]).post();
        }
        for (int i = N + V; i < nb_total_visite; i++) {
            mon_modele.arithm(capaSum[i], "=", D[i]).post();
        }

        // dÃ©claration de DPS
        // dps = 100 * dp + s (usefull for enumeration => min dist, min index succ in one step)
        // based on T_second
        // contrainte 21
        int[][] T_second = new int[nb_total_visite][nb_total_visite];
        for (int i = 0; i < nb_total_visite; i++) {
            for (int j = 0; j < nb_total_visite; j++) {
                T_second[i][j] = T_prime[i][j] * 100 + j;
            }
        }

        // contrainte 22 - 23
        IntVar[] dps = mon_modele.intVarArray("dps", nb_total_visite, 0, 100 * H + nb_total_visite, false);
        for (int i = 1; i < nb_total_visite; i++) {
            mon_modele.element(dps[i], T_second[i], s[i]).post();
        }
        mon_modele.arithm(dps[0], "=", 0).post();

        //      contrainte 24.1 ... 24.5
        st[0] = mon_modele.intVar("st_0", 0);
        dd[0] = mon_modele.intVar("dd_0", 0);
        da[0] = mon_modele.intVar("da_0", 0);
        df[0] = mon_modele.intVar("df_0", 0);
        for (int i = 1; i < nb_total_visite; i++) {
            if (i < N) { // concerne les clients et les dépots de départ de tous les véhicules
                st[i] = mon_modele.intVar("st_" + i, TW_Min[i], TW_Max[i]);
                dd[i] = mon_modele.intVar("dd_" + i, TW_Min[i], Dmax);
                da[i] = mon_modele.intVar("da_" + i, 0, TW_Max[i]);
                df[i] = mon_modele.intVar("df_" + i, 0, Dmax);
            } else if (i < N + V) {
                // concerne les clients et les dépots de départ de tous les véhicules
                st[i] = mon_modele.intVar("st_" + i, TW_Min_vehicule);
                dd[i] = mon_modele.intVar("dd_" + i, TW_Min_vehicule);
                da[i] = mon_modele.intVar("da_" + i, TW_Min_vehicule);
                df[i] = mon_modele.intVar("df_" + i, TW_Min_vehicule);
            } else { // pour les dépots de retour
                st[i] = mon_modele.intVar("st_" + i, 0, Dmax); // les successeurs des dépots d'arrivée valent 0 (pas de successeurs possibles)
                dd[i] = mon_modele.intVar("dd_" + i, Dmax);
                da[i] = mon_modele.intVar("da_" + i, 0, TW_Max_vehicule);
                df[i] = mon_modele.intVar("df_" + i, 0, Dmax);
            }
        }

        // contrainte 24.5.
        mon_modele.arithm(st[0], "=", 0).post();

        // contrainte 24.6.
        for (int i = 1; i < nb_total_visite; i++) {
            if (i < N + V) { // concerne les clients et les dépots de départ de tous les véhicules
                // contrainte 24.6.1
                IntVar d1 = mon_modele.intVar("d1_" + i, 0, Dmax);
                mon_modele.element(d1, da, s[i], 0).post();
                // contrainte 24.6.2
                mon_modele.arithm(dd[i], "+", dp[i], "=", d1).post();
                // containre 24.7. et 24.8
                mon_modele.arithm(st[i], ">=", TW_Min[i]).post();
                mon_modele.arithm(st[i], ">=", da[i]).post();
                //  contraine 24.9
                mon_modele.arithm(df[i], "=", st[i], "+", Pt[i]).post();
                // contrainte 24.10
                mon_modele.arithm(df[i], "=", dd[i]).post();
            } else {
                mon_modele.arithm(st[i], "=", da[i]).post();
                mon_modele.arithm(df[i], "=", st[i]).post();
            }
        }

        // contrainte 25     
        // -------------
        if (nb_C1 > 0) {
            IntVar lim_cumulative = mon_modele.intVar("Lim_cumulative", 1);
            Task[] tasks = new Task[nb_C1];
            IntVar[] heights = mon_modele.intVarArray("Heights", nb_C1, 1, 1);
            for (int i = 0; i < nb_C1; i++) {
                int j = Tab_1[i];
                System.out.println(st[j] + "  " + Pt[j]);
                tasks[i] = mon_modele.taskVar(st[j], Pt[j]);
            }
            mon_modele.cumulative(tasks, heights, lim_cumulative).post();
        }

        // contrainte 26     
        // -------------
        if (nb_c_de_synchro >= 1) {
            int ni = Tab_2[0];
            int nj = Tab_2[1];
            mon_modele.arithm(st[ni], "=", st[nj]).post();
            // mon_modele.arithm(a[ni], "!=", a[nj]).post();
        }
        if (nb_c_de_synchro >= 2) {
            int ni = Tab_3[0];
            int nj = Tab_3[1];
            mon_modele.arithm(st[ni], "=", st[nj]).post();
            // mon_modele.arithm(a[ni], "!=", a[nj]).post();
        }
        if (nb_c_de_synchro >= 3) {
            int ni = Tab_4[0];
            int nj = Tab_4[1];
            mon_modele.arithm(st[ni], "=", st[nj]).post();
            //  mon_modele.arithm(a[ni], "!=", a[nj]).post();
        }
        if (nb_c_de_synchro >= 4) {
            int ni = Tab_5[0];
            int nj = Tab_5[1];
            mon_modele.arithm(st[ni], "=", st[nj]).post();
            //   mon_modele.arithm(a[ni], "!=", a[nj]).post();
        }
        if (nb_c_de_synchro >= 5) {
            int ni = Tab_6[0];
            int nj = Tab_6[1];
            mon_modele.arithm(st[ni], "=", st[nj]).post();
            //   mon_modele.arithm(a[ni], "!=", a[nj]).post();
        }

        if (nb_c_de_synchro >= 6) {
            int ni = Tab_7[0];
            int nj = Tab_7[1];
            mon_modele.arithm(st[ni], "=", st[nj]).post();
            //   mon_modele.arithm(a[ni], "!=", a[nj]).post();
        }

        if (nb_c_de_synchro >= 7) {
            int ni = Tab_8[0];
            int nj = Tab_8[1];
            mon_modele.arithm(st[ni], "=", st[nj]).post();
            //   mon_modele.arithm(a[ni], "!=", a[nj]).post();
        }

        // containte 27
        TG[0] = mon_modele.intVar("TG_0", 0);
        for (int i = 1; i < N; i++) {
            TG[i] = mon_modele.intVar("TG_" + i, 1, N - 1, false);
        }

        // contrainte numéro 28.1
        IntVar cour1 = mon_modele.intVar("cour", 1, N);
        mon_modele.arithm(cour1, "=", TG[1]).post();
        // contrainte numéro 28.2
        IntVar a_cour1 = mon_modele.intVar("a_cour", 0, N);
        mon_modele.element(a_cour1, a, cour1, 0).post();
        // contrainte numéro 28.3
        mon_modele.arithm(a_cour1, "=", 1).post();

        // contrainte 29.x
        for (int i = 1; i < N - 1; i++) {
            // contrainte 29.1 
            IntVar cour = mon_modele.intVar("cour", 1, N);
            mon_modele.arithm(cour, "=", TG[i]).post();
            // contrainte 29.2
            IntVar a_cour = mon_modele.intVar("a_cour", 0, N);
            mon_modele.element(a_cour, a, cour, 0).post();
            // contrainte 29.3 
            IntVar suiv = mon_modele.intVar("suiv", 1, N);
            mon_modele.arithm(suiv, "=", TG[i + 1]).post();
            // contrainte 29.4 
            IntVar a_suiv = mon_modele.intVar("a_suiv", 0, N);
            mon_modele.element(a_suiv, a, suiv, 0).post();
            // contrainte 29.5 
            mon_modele.arithm(a_suiv, ">=", a_cour).post();

            // contrainte 30.1.
            BoolVar Change = mon_modele.boolVar("Change"); // distance entre i et j
            mon_modele.reification(Change,
                    mon_modele.arithm(a_cour, "!=", a_suiv));
            // contrainte 30.2.           
            IntVar suivant_de_cour = mon_modele.intVar("suivant_de_cour", 0, N + V + V);
            mon_modele.element(suivant_de_cour, s, cour, 0).post();
            // contrainte 30.3.           
            mon_modele.reification(Change,
                    mon_modele.arithm(suivant_de_cour, ">=", N + V));
        }

        // contrainte 31.x
        for (int i = 2; i < N; i++) {
            IntVar cour = mon_modele.intVar("cour", 1, N);
            mon_modele.arithm(cour, "=", TG[i]).post();

            for (int j = 1; j < i; j++) {
                // contrainte 31.1 
                IntVar precj = mon_modele.intVar("prec" + j, 0, N);
                mon_modele.arithm(precj, "=", TG[j]).post();
                // contrainte 31.2
                IntVar succ_cour = mon_modele.intVar("succ_cour", 0, nb_total_visite);
                mon_modele.element(succ_cour, s, cour, 0).post();
                // contrainte 31.3
                mon_modele.arithm(succ_cour, "!=", precj).post();
            }
        }

        // contrainte 32
        mon_modele.allDifferent(TG).post();

        // contrainte 33
        IntVar DistTG = mon_modele.intVar("DistanceTG", 0, N);
        // contrainte 34
        BoolVar[] dist_TG = mon_modele.boolVarArray("DistanceTG", N);
        for (int u = 1; u < N; u++) {
            // contrainte 35
            mon_modele.reifyXneC(TG[u], Tour_Geant[u], dist_TG[u]);
        }
        // contrainte 36
        mon_modele.sum(dist_TG, "=", DistTG).post();
        // contrainte 37
        mon_modele.arithm(DistTG, "<=", distance_max_au_tg).post();

        // contraintes 38
        IntVar Dista = mon_modele.intVar("Distance", 0, N);
        // contraintes 39
        BoolVar[] dist_a = mon_modele.boolVarArray("Distance", N); // distance entre i et j
        for (int u = 1; u < N; u++) {
            // contrainte 49
            mon_modele.reifyXneC(a[u], Affectation_initiale[u], dist_a[u]);
        }
        // contrainte 41
        mon_modele.sum(dist_a, "=", Dista).post();
        // contrainte 42
        mon_modele.arithm(Dista, "<=", distance1).post();

        // contraintes 43
        IntVar DistS = mon_modele.intVar("DistanceS", 0, N);
        // contraintes 44
        BoolVar[] dist_S = mon_modele.boolVarArray("DistanceS", N); // distance entre i et j
        for (int u = 1; u < N; u++) {
            // contrainte 45
            mon_modele.reifyXneC(s[u], Suivant_initiale[u], dist_S[u]);
        }
        // contrainte 46
        mon_modele.sum(dist_S, "=", DistS).post();
        // contrainte 47
        mon_modele.arithm(DistS, "<=", distance2).post();

        // containte 48 et 49 
        mon_modele.arithm(d, ">=", borne_inf).post();
        mon_modele.arithm(d, "<=", limitesup).post();

        // contrainte 50
        IntVar[] y = mon_modele.intVarArray("y_", nb_total_visite, 0, 100 * H + nb_total_visite, false);
        // contrainte 51
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
        mon_modele.arithm(y[0], "=", 0).post();

        // fonction objective
        mon_modele.setObjective(Model.MINIMIZE, d);

        // définition du solveur
        Solver mon_solveur = mon_modele.getSolver();

        // option pendant la résolution
        mon_solveur.showShortStatistics();

        // personnalisation de l'affichage
        Vrp_5_avec_TG_MonMessage message = new Vrp_5_avec_TG_MonMessage(TG, a, st, s, pred);

        // Repartir d'une solution
        Solution solution_init = new Solution(mon_modele);
        for (int i = 0; i < N; i++) {
            if (i == 0) {
                solution_init.setIntVal(a[i], 0);
            } else if (i < N + V) {
                solution_init.setIntVal(a[i], Affectation_initiale[i]);
            }
        }
        for (int i = 1; i <= V; i++) {
            int position = N + i - 1; // dépot de départ du véhicule i
            solution_init.setIntVal(a[position], i);
        }
        for (int i = 0; i < nb_total_visite; i++) {
            solution_init.setIntVal(s[i], Suivant_initiale[i]);
        }
        for (int i = 0; i < N; i++) {
            solution_init.setIntVal(TG[i], Tour_Geant[i]);
        }
        for (int i = 0; i < nb_total_visite; i++) {
            if (i == 0) {
                solution_init.setIntVal(y[i], 0);
            } else {
                solution_init.setIntVal(y[i], Y_dps[i]);
            }
        }

        // définir la politique de branchement

        mon_solveur.setSearch(
                Search.intVarSearch(
                        new FirstFail(mon_modele), // selecteur de variable
                        new IntDomainLast(solution_init, new IntDomainMin()), // choix de la valeur
                        TG),
                Search.intVarSearch(
                        new FirstFail(mon_modele), // selecteur de variable
                        new IntDomainLast(solution_init, new IntDomainMin()), // choix de la valeur
                        y),
                Search.intVarSearch(
                        new MaxRegret(), // selecteur de variable
                        new IntDomainMin(), // choix de la valeur
                        d),
                Search.intVarSearch(
                        new FirstFail(mon_modele),
                        new IntDomainMiddle(true),
                        makeIntSplit(),
                        da));

        solution = new Solution(mon_modele);

        // debut de la recherche

        Date heure_debut = new Date();
        long h_debut = heure_debut.getTime();

        String chaine = duree_max + "s";
        mon_solveur.limitTime(chaine);

        while (mon_solveur.solve()) {
            solution.record();
            System.out.println("-------------");
            Date heure_fin = new Date();
            long h_fin = heure_fin.getTime();
            long duree = h_fin - h_debut;
            long duree_s = duree / 1000;
            System.out.println("temps : " + duree_s + " s    " + d.toString());
            System.out.println("----------");
        }

        // affichage final si une solution a été trouvée
        if (mon_solveur.isFeasible() == ESat.TRUE) {
            System.out.println("Solution trouvee");
            affichage_solution(solution, mon_solveur, h_debut,
                    s, a, p, dps, dp, st, da, df, dd);
            return solution.getIntVal(d);
        } else {
            System.out.println("pas de solution");
            return 0;
        }

    }

    //*********************************************************************//
    public static void main(String[] args) {

        // les données   
        //donnees_de_base();
        char lettre_bredstrom = 1;
        for (int l = 1; l <= 3; l++) {

            if (l == 1) {
                lettre_bredstrom = 'S';
            }
            if (l == 2) {
                lettre_bredstrom = 'M';
            }
            if (l == 3) {
                lettre_bredstrom = 'L';
            }
            String nom_fichier = "Resultat.csv";
            initialiser_fichier_sortie(nom_fichier);

            //************

            for (int instance_numero = 1; instance_numero <= 7; instance_numero++) {

                System.out.println("\n********* INSTANCE " + instance_numero + " ************ \n");

                int cout_objectif = -1;
                float temps_to_best = -1;
                float temps_total = -1;

                String fichier_entree = "./data/chap5_vrp5/Bred_0" + instance_numero + lettre_bredstrom + ".txt";
                String fichier_tournee = "./data/chap5_vrp5/tournee_Bred_0" + instance_numero + lettre_bredstrom + "_GC.txt";

                if (instance_numero >= 10) {
                    fichier_entree = "./data/chap5_vrp5/Bred_" + instance_numero + lettre_bredstrom + ".txt";
                    fichier_tournee = "./data/chap5_vrp5/tournee_Bred_" + instance_numero + lettre_bredstrom + "_GC.txt";
                }

                lire_fichier(fichier_entree, fichier_tournee);

                int[] Sauve_Affectation_initiale = new int[N + 1];
                int[] Sauve_Tour_Geant_initial = new int[N + 1];

                // int[] depart = new int[]{19, 30, 66, 78};

                for (int iter = 1; iter <= 10; iter++) {

                    Date heure_debut = new Date();
                    long h_debut = heure_debut.getTime();
                    cout_objectif = -1;

                    System.out.println("");
                    System.out.println("");

                    System.out.println("*********** " + iter + " ************");

                    generator = new java.util.Random(314 + 156 + iter);
                    generator.setSeed(iter);

                    System.out.println(iter + " - " + 314 + iter);

                    int[] liste_borne_sup = new int[] { 0, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 2000, 3000, 700 };
                    int[] liste_borne_inf = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

                    if (lettre_bredstrom == 'S') {
                        liste_borne_inf = new int[] { 0, 274, 346, 283, 378, 313, 608, 607, 746, 952, 615 };
                    } else if (lettre_bredstrom == 'M') {
                        liste_borne_inf = new int[] { 0, 274, 290, 260, 349, 381, 575, 541, 668, 871, 544 };
                    } else if (lettre_bredstrom == 'L') {
                        liste_borne_inf = new int[] { 0, 262, 277, 257, 316, 266, 533, 498, 626, 832, 526 };
                    }

                    for (int i = 0; i <= N; i++) {
                        Sauve_Tour_Geant_initial[i] = Tour_Geant[i];
                        Sauve_Affectation_initiale[i] = Affectation_initiale[i];
                    }

                    System.out.print("Affectation_initiale : ");
                    affichage_vecteur_a();

                    if (iter > 1) {
                        melangerTableau(Tour_Geant, N);
                    }

                    System.out.print("Affectation_initiale après modif : ");
                    affichage_vecteur_a();

                    boolean solution_obtenue = false;

                    // param 1. distance sur les a_i
                    // param 2. distance sur les s_i
                    // param 3. distance sur le TG

                    int p1 = 4;
                    int p2 = 8;
                    distance_max_au_tg = 4;

                    solution_obtenue = resoudre(p1,
                            p2,
                            liste_borne_sup[instance_numero],
                            liste_borne_inf[instance_numero], // borne inf
                            30);

                    if (solution_obtenue == true) {

                        System.out.println("\n\n RESOLUTION AVEC DATES");

                        Y_dps = new int[nb_total_visite];
                        for (int i = 0; i < nb_total_visite; i++) {
                            Affectation_initiale[i] = solution.getIntVal(a[i]);
                            Suivant_initiale[i] = solution.getIntVal(s[i]);
                            Y_dps[i] = 100 * solution.getIntVal(a[i]) + solution.getIntVal(s[i]);
                        }
                        for (int i = 0; i < N; i++) {

                            Tour_Geant[i] = solution.getIntVal(TG[i]);
                        }

                        p1 = 8;
                        p2 = 16;
                        distance_max_au_tg = 8;

                        cout_objectif = resoudre_Normale(p1,
                                p2,
                                liste_borne_sup[instance_numero],
                                liste_borne_inf[instance_numero], // borne inf
                                30);
                    }

                    Date heure_fin = new Date();
                    long h_fin = heure_fin.getTime();
                    long duree_totale = h_fin - h_debut;

                    float duree = (float) duree_totale / (float) 1000;

                    System.out.println("duree totale de resolution = " + duree);

                    if (cout_objectif == -1)
                        duree = -1;
                    ecrire_resultat_ds_fichier(nom_fichier, instance_numero, lettre_bredstrom, cout_objectif, duree, duree);

                    for (int i = 0; i <= N; i++) {
                        Tour_Geant[i] = Sauve_Tour_Geant_initial[i];
                        Affectation_initiale[i] = Sauve_Affectation_initiale[i];
                    }
                }
            }
        }

    }

    // *******************************************************************//
    // --                     affichage_vecteur_a()                     --//
    // -------------------------------------------------------------------//

    public static void affichage_vecteur_a() {
        for (int i = 0; i <= N; i++) {
            System.out.print(Affectation_initiale[i] + " - ");
        }
        System.out.println();
        System.out.println("--------------------------------");
        System.out.println();
    }

    // *******************************************************************//
    // --                     affichage_solution()                     --//
    // -------------------------------------------------------------------//

    public static void affichage_solution(Solution solution, Solver mon_solveur, long h_debut,
            IntVar[] s, IntVar[] a, IntVar[] p, IntVar[] dps, IntVar[] dp, IntVar[] st,
            IntVar[] da, IntVar[] df, IntVar[] dd) {

        for (int i = 0; i < nb_total_visite; i++) {
            int suc = solution.getIntVal(s[i]);
            int affec = solution.getIntVal(a[i]);
            int ran = solution.getIntVal(p[i]);
            int dp_sol = solution.getIntVal(dp[i]);

            int da_sol = solution.getIntVal(da[i]);

            int st_sol = solution.getIntVal(st[i]);

            int df_sol = solution.getIntVal(df[i]);

            int dd_sol = solution.getIntVal(dd[i]);

            System.out.print("Sommet " + i + "    Succ : " + suc + "    |   affec " + affec);
            System.out.print("    |   rang " + ran);
            System.out.print("   | dp = " + dp_sol + "   **  da = " + da_sol);
            System.out.println("  |  st = " + st_sol + "  |  df_sol =   " + df_sol + "  |  ddsol = " + dd_sol);

        }
        int cout = (int) mon_solveur.getBestSolutionValue();
        Date heure_fin = new Date();
        long h_fin = heure_fin.getTime();
        long duree = h_fin - h_debut;
        long duree_s = duree / 1000;

        System.out.println("temps : " + duree_s + " s   d = " + cout);
        System.out.println("Fin");
    }

    // *******************************************************************//
    // --                     affichage_solution_short()                --//
    // -------------------------------------------------------------------//

    public static void affichage_solution_short(Solution solution, Solver mon_solveur,
            IntVar[] s, IntVar[] a, IntVar[] p, IntVar[] capaSum, IntVar[] st) {

        for (int i = 0; i < nb_total_visite; i++) {
            int suc = solution.getIntVal(s[i]);
            int affec = solution.getIntVal(a[i]);
            int ran = solution.getIntVal(p[i]);
            int cap = solution.getIntVal(capaSum[i]);

            System.out.print("Sommet " + i + "    Succ : " + suc + "    |   affec " + affec);
            System.out.print("    |   rang " + ran + "    |   CapaSum " + cap);
            System.out.println("    |   " + st[i].toString());
        }
    }
}
