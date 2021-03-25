package chap4_jobshop;

import java.io.*;
import java.text.*;
import java.util.*;

import org.chocosolver.solver.*;
import org.chocosolver.solver.search.strategy.*;
import static org.chocosolver.solver.search.strategy.assignments.DecisionOperatorFactory.makeIntSplit;
import org.chocosolver.solver.search.strategy.selectors.values.*;
import org.chocosolver.solver.search.strategy.selectors.variables.AntiFirstFail;
import org.chocosolver.solver.search.strategy.selectors.variables.DomOverWDeg;
import org.chocosolver.solver.search.strategy.selectors.variables.FirstFail;
import org.chocosolver.solver.search.strategy.selectors.variables.InputOrder;
import org.chocosolver.solver.search.strategy.selectors.variables.MaxRegret;
import org.chocosolver.solver.variables.*;

import org.chocosolver.util.ESat;

/**
 *
 * @author magondra
 */
public class JobShop_Bierwirth_Matth {

    public static String nom_instance;
    public static String chemin_instance;       // = "./instance//" + nom_instance + ".txt";
    public static int ecart_maximal = 4;
    public static int timelimit = 5;
    public static int nb_max_iter = 60;
    public static int duree_maximale = 300;

    public static int nb_job = -1;              // jobs
    public static int nb_machine = -1;		// machines
    public static int nt = -1;                  // nt (=nb_job*nb_machine+1)

    public static int Dmax = 3000;            	// taille de l’horizon
    public static int[][] Machine = null;       // la machine de l'opération i,j
    public static int[][] Gamme = null;
    public static int[][] Pt = null;            //Pt des operations
    public static int[] Pt_lineaire = null;     //Pt linéaire des opérations, (la 1° opération est la numéro 1, correspondant à Gamme[0][0])
    public static int[][] Tab = null;           //Tab[i][j]= position de l'opération dans le vecteur st
    public static int[][] op_par_machine = null;// opérations utilisant la machine i

    public static Random generator;
    public static int borne_inf;

    // variables :
    public static Model mon_modele;
    public static IntVar[] lambda;          //vecteur de Bierwirth
    public static IntVar[] rang;            //le rang de chaque opération dans le vecteur de Bierwith
    public static IntVar[] st;              //le st des opérations
    public static IntVar[] ft;              //le ft des opérations
    public static IntVar[] pred_ma;         //predecesseur machine d'une opération
    public static int[] lambda_depart;      // vecteur de bierwirth de départ
    public static Solution solution_init;   // solution de départ
    public static IntVar Cmax;              // valeur de la fonction objectf
    public static Solver mon_solveur;
    public static int best_sol;
    public static long best_temps;
    public static float avg_sol;

    // *******************************************************************//
    // --                   melangerTableau()                           --//
    // -------------------------------------------------------------------//
    // --   Description :                                               --//
    // --             Creation d'un vecteur de Bierwirth                --//
    // --                                                               --//
    // --                                                               --//
    // *******************************************************************//
    static void melangerTableau(int t[]) {
        int p = Math.abs(generator.nextInt());
        int pp = p % 4 + 1;
        for (int i = 1; i <= pp; i++) {
//        for (int i = 1; i < t.length/2; i++) {
            double x = generator.nextFloat();
            double y = x * t.length;
            int z = (int) (1 + y);
            int r = (int) (x * 10) % t.length + 1;

            double x2 = generator.nextFloat();
            double y2 = x2 * t.length;
            int z2 = (int) (1 + y2);
            int r2 = (int) (x2 * 10) % t.length + 1;

            int tmp = t[r2];
            t[r2] = t[r];
            t[r] = tmp;
        }
    }

    // *******************************************************************//
    // --                   lire_instance()                             --//
    // -------------------------------------------------------------------//
    // --   Description :                                               --//
    // --             Lecture de l'instance de Bierwirth                --//
    // --                                                               --//
    // --                                                               --//
    // *******************************************************************//
    static void lire_instance() {
        // lecture du fichier
        try {
            File source;
            source = new File(chemin_instance); // instance lue

            FileReader Input = new FileReader(source);
            StreamTokenizer Lecteur;
            Lecteur = new StreamTokenizer(Input);
            Lecteur.parseNumbers();

            int nombre_ligne = -1;

            // nb lignes        
            Lecteur.nextToken();
            if (Lecteur.ttype == Lecteur.TT_NUMBER) {
                nombre_ligne = (int) Lecteur.nval;
                nb_job = nombre_ligne;
            }

            // nb colonnes
            int nombre_colonne = -1;
            Lecteur.nextToken();
            if (Lecteur.ttype == Lecteur.TT_NUMBER) {
                nombre_colonne = (int) Lecteur.nval;
                nb_machine = nombre_colonne;
            }

            nt = nb_job * nb_machine + 1; // on ajoute l'opération fictive 0

            // création
            Machine = new int[nb_job][nb_machine];
            Pt = new int[nb_job][nb_machine];
            Pt_lineaire = new int[nt];
            Tab = new int[nb_job][nb_machine];
            op_par_machine = new int[nb_machine][nb_job];
            borne_inf = 0;

            Lecteur.nextToken();
            if (Lecteur.ttype == Lecteur.TT_NUMBER) {
                int n = (int) Lecteur.nval;
                borne_inf = n;
            }

            int compteur = 0;

            int[] compteur_machine = new int[nb_machine];
            for (int i = 0; i < nb_machine; i++) {
                compteur_machine[i] = 0;
            }

            for (int i = 0; i < nombre_ligne; i++) {
                for (int j = 0; j < nombre_colonne; j++) {
                    compteur++;
                    // lecture machine
                    Lecteur.nextToken();
                    if (Lecteur.ttype == Lecteur.TT_NUMBER) {
                        Machine[i][j] = 1 + (int) Lecteur.nval;
                        int machi = Machine[i][j] - 1;
                        int pos = compteur_machine[Machine[i][j] - 1];
                        op_par_machine[machi][pos] = compteur;
                        compteur_machine[machi]++;
                    }
                    // lecture processing
                    Lecteur.nextToken();
                    if (Lecteur.ttype == Lecteur.TT_NUMBER) {
                        Pt[i][j] = (int) Lecteur.nval;
                        Pt_lineaire[compteur] = Pt[i][j];
                    }
                    Tab[i][j] = compteur;
                }
            }
            Input.close();
        } catch (Exception e) {
            System.out.println("Erreur lecture fichier");
            System.out.println(e.getMessage());
        };
        // fin lecture
    }

    // *******************************************************************//
    // --                   creation_lambda()                           --//
    // -------------------------------------------------------------------//
    // --   Description :                                               --//
    // --             Création d'un vecteur lambda (ou vecteur de       --//
    // --             Bierwirth)                                        --//
    // --                                                               --//
    // *******************************************************************//
    static void creation_lambda() {
        lambda_depart = new int[nt];
        int pos_courante = 1;
        int k = Math.abs(generator.nextInt());
        k = k % nb_job;
        k = k + 1;

        for (int j = 0; j < nb_machine; j++) {
            for (int i = 0; i < nb_job; i++) {
                lambda_depart[pos_courante] = k;
                k = (k % nb_job) + 1;
                pos_courante++;
            }
        }
        melangerTableau(lambda_depart);
    }

    // *******************************************************************//
    // --                   lecture_lambda()                           --//
    // -------------------------------------------------------------------//
    // --   Description :                                               --//
    // --             Lecture d'un vecteur lambda contenu dans un       --//
    // --             fichier                                           --//
    // --                                                               --//
    // *******************************************************************//
    static void lecture_lambda(int iter) {
        // lecture du fichier
        lambda_depart = new int[nt];
        try {
            File source;
            String chemin_vb = "./VB//VB_" + nom_instance + "_" + iter + ".txt";
            source = new File(chemin_vb); // instance lue

            FileReader Input = new FileReader(source);
            StreamTokenizer Lecteur;
            Lecteur = new StreamTokenizer(Input);
            Lecteur.parseNumbers();

            for (int i = 0; i < nt; i++) {
                Lecteur.nextToken();
                if (Lecteur.ttype == Lecteur.TT_NUMBER) {

                    int n = (int) Lecteur.nval;
                    if (i != 0) {
                        n++;
                    }
                    lambda_depart[i] = n;
                }
                Lecteur.nextToken(); // suppression de la virgule
            }

            Input.close();
        } catch (Exception e) {
            System.out.println("Erreur lecture fichier");
            System.out.println(e.getMessage());
        };
        // fin lecture
    }

    // *******************************************************************//
    // --                   solution_depart()                           --//
    // -------------------------------------------------------------------//
    // --   Description :                                               --//
    // --             Initialisation d'une solution de départ           --//
    // --                                                               --//
    // --                                                               --//
    // *******************************************************************//
    static void solution_depart() {
        solution_init = new Solution(mon_modele);
        solution_init.setIntVal(lambda[0], 0);
        for (int i = 1; i < nt; i++) {
            solution_init.setIntVal(lambda[i], lambda_depart[i]);
        }
    }

    // *******************************************************************//
    // --                   contraintes()                               --//
    // -------------------------------------------------------------------//
    // --   Description :                                               --//
    // --             Contraintes du modèle PPC                         --//
    // --                                                               --//
    // --                                                               --//
    // *******************************************************************//
    static void contraintes() {
        lambda = new IntVar[nt];   //vecteur de Bierwirth
        rang = new IntVar[nt];     //le rang de chaque opération dans le vecteur de Bierwith
        st = new IntVar[nt];      //le st des opérations
        ft = new IntVar[nt];      //le ft des opérations
        pred_ma = new IntVar[nt]; //predecesseur machine d'une opération
        
        // contraintes 1 et 2
        // déclaration des st 
        st[0] = mon_modele.intVar("st_0", 0);
        for (int i = 1; i < nt; i++) {
                st[i] = mon_modele.intVar("st_" + i, 0, Dmax, false);
        }

        // contraintes 3 et 4
        // déclaration des ft
        ft[0] = mon_modele.intVar("ft_0", 0);
        for (int i = 1; i < nt; i++) {
                ft[i] = mon_modele.intVar("ft_" + i, 0, Dmax, false);
        }
                
        // contrainte 5 : st[i]+Pt[i]=ft[i]
        for (int i = 1; i < nt; i++) {
            mon_modele.arithm(ft[i], "=", st[i], "+", Pt_lineaire[i]).post();
        }
        
        // contrainte 15 déclaration des lambda 
        lambda[0] = mon_modele.intVar("L_0", 0);
        // contrainte 16
        for (int i = 1; i < nt; i++) {
            lambda[i] = mon_modele.intVar("L_" + i, 1, nb_job, false);
        }
        
        // contrainte 9 : les prédécesseurs machines possible 
        pred_ma[0] = mon_modele.intVar("pred_ma_0", 0);
        for (int i = 0; i < nb_job; i++) {
            for (int j = 0; j < nb_machine; j++) {
                int op = Tab[i][j];
                int machine = Machine[i][j] - 1;
                int[] pred_possible = new int[nb_job];

                for (int k = 0; k < nb_job; k++) {
                    if (op_par_machine[machine][k] != op) {
                        pred_possible[k] = op_par_machine[machine][k];
                    } else {
                        pred_possible[k] = 0;
                    }
                }
                pred_ma[op] = mon_modele.intVar("pred_ma_" + op, pred_possible);
            }
        }
        
        
        // contrainte 6 : st[i]=max(ft[pred_ma[i]],ft[operation-1 dans gamme])
        for (int i = 0; i < nb_job; i++) {
            for (int j = 0; j < nb_machine; j++) {
                int k = Tab[i][j];
                IntVar ftm = mon_modele.intVar("ftm" + k, 0, Dmax, false);
                mon_modele.element(ftm, ft, pred_ma[k], 0).post();

                IntVar ftj;
                if (j != 0){ // pas la première operation de la gamme
                    ftj = ft[k - 1];
                }
                else {
                    ftj = mon_modele.intVar("ftj" + k, 0);
                }
                IntVar ft_max = mon_modele.intVar("ft_max_" + k, 0, Dmax);
                mon_modele.max(ft_max, ftm, ftj).post();
                mon_modele.arithm(st[k], "=", ft_max).post();
            }
        }
        
        // contraintes 7 et 8
        // déclaration des rangs 
        rang[0] = mon_modele.intVar("R_0", 0);
        for (int i = 1; i < nt; i++) {
            rang[i] = mon_modele.intVar("R_" + i, 1, nt + 1, true);
        }
        
                      
        // contrainte 10
        mon_modele.allDifferent(rang).post();
        
        // contrainte 11 : pour deux opérations de la même gamme, le rang de la 1° op doit être plus petit
        for (int i = 0; i < nb_job; i++) {
            for (int j = 1; j < nb_machine; j++) {
                int k_moins_1 = Tab[i][j - 1];
                int k = Tab[i][j];
                mon_modele.arithm(rang[k_moins_1], "<", rang[k]).post();
            }
        }
        
         // contrainte 12 : le rang de l'opération prec en dijonction machine doit être plus petit
        for (int i = 1; i < nt; i++) {
            IntVar rang_op_prec_disj = mon_modele.intVar("rang_op_prec_disj_" + i, 0, nt, true);
            mon_modele.element(rang_op_prec_disj, rang, pred_ma[i], 0).post();
            mon_modele.arithm(rang_op_prec_disj, "<", rang[i]).post();
        }
        
        // contrainte 13 : lambda[rang[i]]=Job[i];
        for (int i = 0; i < nb_job; i++) {
            for (int j = 0; j < nb_machine; j++) {
                int op = Tab[i][j];
                IntVar job_vecteur = mon_modele.intVar("job_vecteur_" + op, i + 1);
                mon_modele.element(job_vecteur, lambda, rang[op], 0).post();
            }
        }
        
        // contrainte 14 : les prédécesseurs machines doivent être differents pour chaque machine
        for (int i = 0; i < nb_machine; i++) {
            IntVar[] pred_ma_local = mon_modele.intVarArray("pred_ma_local_" + i, nb_job, 0, nt, false);
            for (int j = 0; j < nb_job; j++) {
                int op = op_par_machine[i][j];
                pred_ma_local[j] = pred_ma[op];
            }
            mon_modele.allDifferent(pred_ma_local).post();
        }

        
        // contrainte 17 : dans le vecteur lambda, un job ne doit être présent qu'un nombre limité de fois
        IntVar limite = mon_modele.intVar("limite", nb_machine);
        for (int i = 1; i < nb_job; i++) {
            IntVar val_job = mon_modele.intVar("val_job_" + i + 1, i + 1);
            mon_modele.count(val_job, lambda, limite).post();
        }

        
        // Objectif : contrainte 18
        Cmax = mon_modele.intVar("Cmax", 0, Dmax);
        
        //contrainte 19
        for (int i = 1; i < nt; i++) {
            mon_modele.arithm(Cmax, ">=", ft[i]).post();
        }

        // contrainte 20
        mon_modele.arithm(Cmax, ">=", borne_inf).post();
               
        
         // contrainte 21 : contrainte DiffN
        IntVar[] ones = mon_modele.intVarArray("ones", nt, 1, 1);
        IntVar[] y = new IntVar[nt];
        IntVar[] d = new IntVar[nt];
        y[0] = mon_modele.intVar("y_0", 0);
        d[0] = mon_modele.intVar("duree_0", 0);
        for (int i = 0; i < nb_job; i++) {
            for (int j = 0; j < nb_machine; j++) {
                int k = Tab[i][j];
                int m = Machine[i][j] - 1;
                y[k] = mon_modele.intVar("y_" + k, m);
                d[k] = mon_modele.intVar("duree_" + k, Pt[i][j]);
            }
        }
        mon_modele.diffN(st, y, d, ones, true).post();
        
        
        
        // calcul ecart entre vecteur :
        
        
        
        // contrainte 22
        IntVar Dist = mon_modele.intVar("Distance_gamma", 0, nt);
        // contrainte 23
        BoolVar[] dist_L = mon_modele.boolVarArray("dist_gamma", nt); // distance entre i et j
        for (int u = 0; u < nt; u++) {
            // contrainte 24
            mon_modele.reifyXneC(lambda[u], lambda_depart[u], dist_L[u]);
        }          
        // contrainte 25
        mon_modele.sum(dist_L, "=", Dist).post();
        // contrainte 26
        mon_modele.arithm(Dist, "<=", ecart_maximal).post();
        

    }

    static void affichage(Solver mon_solveur, Solution s, IntVar Cmax) {
        if (mon_solveur.getTimeCount() < timelimit) {
            System.out.println("  ---   Preuve Complete   ---- ");
        }
        System.out.println("TT total =  " + mon_solveur.getTimeCount() + " seconde ;");
        System.out.println("Nb noeud total =  " + mon_solveur.getNodeCount() + ";");
        System.out.println("Nb backtrack total =  " + mon_solveur.getBackTrackCount() + ";");

        // affichage de la solution
        int detail = 0;
        if (mon_solveur.isFeasible() == ESat.TRUE) {

            int CC = s.getIntVal(Cmax);
            System.out.println("Cmax = " + CC);

            if (detail == 1) {
                System.out.println("");
                System.out.println("Meilleure solution");
                System.out.println("");
                System.out.print("Lambda = ");
                for (int i = 0; i < nt; i++) {
                    System.out.print(s.getIntVal(lambda[i]));
                    System.out.print(" - ");
                }
                System.out.println("");
                System.out.print("dates : ");
                for (int i = 0; i < nt; i++) {
                    int deb = s.getIntVal(st[i]);
                    int fin = s.getIntVal(ft[i]);
                    System.out.print(deb + "/" + fin + "   ");
                }
                System.out.println("");
                System.out.print("pred_ma : ");
                for (int i = 0; i < nt; i++) {
                    int pred = s.getIntVal(pred_ma[i]);

                    System.out.print(pred + "   ");
                }
                System.out.println("");
                System.out.print("rang :    ");
                for (int i = 0; i < nt; i++) {
                    int r = s.getIntVal(rang[i]);

                    System.out.print(r + "   ");
                }
                System.out.println("");

            }

        } else {
            System.out.println("pas de sol.faisable\n");
        }
    }

    public static void fct_objectif() {
        
        

        mon_modele.setObjective(Model.MINIMIZE, Cmax);
    }

    public static void multi_start()
    {
         String nom_sortie = "./resultat//resume.csv";
        intialiser_fichier_resultat(nom_sortie);

        for (int inst = 2; inst <= 2; inst++) {

            if (inst < 10) {
                nom_instance = "la0" + Integer.toString(inst);
            } else {
                nom_instance = "la" + Integer.toString(inst);
            }

            chemin_instance = "./data/chap4_jobshop/instance/" + nom_instance + ".txt";
            best_sol = Dmax;
            best_temps = 9999;

            avg_sol = 0;

           // nb_max_iter=1;
            for (int iter = 1; iter <= nb_max_iter; iter++) {
                System.out.println("iter=  " + iter);
                System.out.println("");

                generator = new Random(314 + iter);
                generator.setSeed(iter);

                lire_instance();
                creation_lambda(); // aléatoire 

                int iteration = iter - 1;

                System.out.print("Lambda depart = ");
                for (int i = 0; i < nt; i++) {
                    System.out.print(lambda_depart[i] + " - ");
                }
                System.out.println("");

                mon_modele = new Model(nom_instance);

                contraintes();          // déclaration des contraintes
                solution_depart();      // solution depart
                fct_objectif();         // fonction objectif
                parametrage_solveur();  //parametrage du solveur            

            } // fin iter

            avg_sol = avg_sol / nb_max_iter;
            fichier_resultat(nom_sortie, inst);

            System.out.println("\n\n ********************************* \n");
            System.out.println("       Meilleur Makespan : " + best_sol);
            System.out.println("       Makespan Moyen    : " + avg_sol);
            System.out.println("\n\n ********************************* \n");
        }
    
    }
    
    
    
    
    public static void grasp_els()
    {
         String nom_sortie = "./resultat//resume.csv";
        intialiser_fichier_resultat2(nom_sortie);
        

        generator = new Random();

        for (int inst = 1; inst <= 10; inst++) 
        {
          
            if (inst < 10) {
                nom_instance = "la0" + Integer.toString(inst);
            } else {
                nom_instance = "la" + Integer.toString(inst);
            }

            chemin_instance = "./data/chap4_jobshop/instance/" + nom_instance + ".txt";
            best_sol = Dmax;
            best_temps = 9999;

            avg_sol = 0;

            System.out.println(" ********************** ");
            System.out.println(chemin_instance);
            System.out.println(" ********************** ");
            System.out.println();
            
           // nb_max_iter=1;
           long debut = new java.util.Date().getTime();
           
            for (int iter = 1; iter <= 1; iter++) {
                System.out.println("iter=  " + iter);
                System.out.println("");

                debut = new java.util.Date().getTime();
                
                generator.setSeed(123456789+iter);

                lire_instance();
                creation_lambda(); // aléatoire 

                    int[] sauve_lambda;
                    sauve_lambda= new int[nt];                

                    int[] Best_lambda;
                    Best_lambda= new int[nt];    
                    double best_cout = 9999;                    
                    
                    int stop=0;
                    int nb_els = 0;
                    
                    do{

                    nb_els = nb_els +1 ;
                    
                    int iteration = iter - 1;

                    System.out.print("Lambda depart = ");
                    for (int i = 0; i < nt; i++) {
                        System.out.print(lambda_depart[i] + " - ");
                    }
                    System.out.println("");

                    mon_modele = new Model(nom_instance);

                    contraintes();          // déclaration des contraintes
                    solution_depart();      // solution depart
                    fct_objectif();         // fonction objectif

                    // evaluation            
                    mon_solveur = mon_modele.getSolver();

                    String tps_limite = timelimit + "s";
                    mon_solveur.limitTime(tps_limite);
                    
                    // mon_solveur.showShortStatistics();
                   
                    
                    mon_solveur.setSearch(

                    Search.intVarSearch(
                        new FirstFail(mon_modele), // selecteur de variable
                        new IntDomainLast(solution_init, new IntDomainRandom(314)), // choix de la valeur
                       lambda),
                    Search.intVarSearch(
                        new FirstFail(mon_modele), // selecteur de variable
                        new IntDomainMin(),
                       Cmax)
                      );

                    mon_solveur.setLDS(500 * ecart_maximal);
                    Solution s = new Solution(mon_modele);
                    
                    while (mon_solveur.solve()) {
                          s.record();
                      }
                    affichage(mon_solveur, s, Cmax);
                    double val_dep = s.getIntVal(Cmax);

                    // on modifie lambda
                    for (int k=0; k<nt;k++)
                    {
                      int val = s.getIntVal(lambda[k]);
                      lambda_depart[k] = val;
                    }
                    System.out.print("Lambda final : ");
                    for (int k=0; k<nt;k++)
                    {
                      System.out.print(lambda_depart[k]+" - ");
                    }                
                    System.out.println();

                    // meilleure solution connue battue
                    if (val_dep<best_cout)
                    {
                      best_cout = val_dep;
                      for (int k=0; k<nt;k++)
                      {
                        Best_lambda[k] = lambda_depart[k];
                       }                      
                    }
                    
                    // generation des voisins
                    
                    int[] best_lambda_voisin;
                    best_lambda_voisin= new int[nt];
                    double best_voisin_cout=9999999;

                    System.out.println(best_cout+" / "+val_dep);
                    
                    // 10 voisins
                    for (int j=1; j<=10; j++)
                    {
                      System.out.println("******** "+j+" ******");

                     for (int o=1; o<=1; o++)
                     {
                      // on genere un voisin
                      int p1 = Math.abs(generator.nextInt());
                      p1 = p1 % (nt-1) + 1;
                      int p2 = -1;
                      do{
                        p2 = Math.abs(generator.nextInt());
                        p2 = p2 % (nt-1) + 1;
                      }while (lambda_depart[p1]==lambda_depart[p2]);

                      // sauver_lambda_depart
                      for (int k=0; k<nt;k++)
                      {
                        sauve_lambda[k] = lambda_depart[k] ;
                      }                   


                      // permuter
                      int valeur = lambda_depart[p1];
                      lambda_depart[p1] = lambda_depart[p2];
                      lambda_depart[p2]=valeur;
                     }  
                      

                      mon_solveur.restart();
                      mon_solveur.reset();

                      mon_modele = new Model(nom_instance);

                      contraintes();          // déclaration des contraintes
                      solution_depart();      // solution depart
                      fct_objectif();         // fonction objectif

                        // evaluation            
                      mon_solveur = mon_modele.getSolver();

                      mon_solveur.limitTime(tps_limite);
                  //    mon_solveur.showShortStatistics();
                      mon_solveur.setSearch(

                      Search.intVarSearch(
                            new FirstFail(mon_modele), // selecteur de variable
                            new IntDomainLast(solution_init, new IntDomainRandom(314)), // choix de la valeur
                           lambda),
                      Search.intVarSearch(
                            new FirstFail(mon_modele), // selecteur de variable
                            new IntDomainMin(),
                           Cmax)
                          );

                      
                      //mon_solveur.showShortStatistics();
                      s = new Solution(mon_modele);
                      mon_solveur.setLDS(500 * ecart_maximal);
                      while (mon_solveur.solve()) {
                              s.record();
                      }

                      int val1 =(int) mon_solveur.getBestSolutionValue(); 

                      if (val1<best_voisin_cout) 
                      {
                            best_voisin_cout = val1;
                            for (int k=0; k<nt;k++)
                            {
                              int val = s.getIntVal(lambda[k]);
                              best_lambda_voisin[k] = val;
                            }        
                            
                            if (best_voisin_cout<best_cout)
                            {
                              best_cout = best_voisin_cout;
                              for (int k=0; k<nt;k++)
                              {
                                Best_lambda[k] = best_lambda_voisin[k];
                               }                      
                              if (best_cout==borne_inf) j=11;
                            }
                            
                            
                            
                            
                      } // meilleure voiris

                    long fin2 = new java.util.Date().getTime();
                    long duree2 = fin2-debut;
                    double duree_double2 = (double)duree2/(double)1000;                      
                      
                      System.out.println(duree_double2+" : " +best_cout+" / "+val_dep+" / "+best_voisin_cout);
                      
                      // on revient au lambda de depart
                      for (int k=0; k<nt;k++)
                      {
                        lambda_depart[k] = sauve_lambda[k] ;
                      } 



                    } // fin des voisins
                       
                      // on repart du meilleur des lambda
                    for (int k=0; k<nt;k++)
                    {
                      lambda_depart[k] = best_lambda_voisin[k] ;
                    }                     
                         
                    if (best_cout==borne_inf)
                        stop=1;
                    if (nb_els==100)
                        stop=1;
                    
                    long fin = new java.util.Date().getTime();
                    long duree = fin-debut;
                    double duree_double = (double)duree/(double)1000;
                    if (duree_double>duree_maximale)
                    {
                        stop=1;
                        System.out.println("duree = "+duree);
                    }
                      
                }while(stop==0);
                
                long fin = new java.util.Date().getTime();
                long duree = fin-debut;
                double duree_double = (double)duree/(double)1000;
                
                
                fichier_resultat2(nom_sortie, inst, best_cout, duree_double);
                
                System.out.println("\n\n ********************************* \n");
                System.out.println("       Meilleur Makespan : " + best_cout);
                System.out.println("       Duree totale : " + duree_double);
                System.out.print("       Lambda : ");
                for (int k=0; k<nt;k++)
                {
                 System.out.print(Best_lambda[k]+" - ");
                }                
                System.out.println();       
                System.out.println("\n\n ********************************* \n");
                           
      } // fin iteration
    } // instances
} // procedure
    
    
    
    
    public static void main(String[] args) {

        grasp_els();
        
//       multi_start();
    }

    private static void parametrage_solveur() {

        mon_solveur = mon_modele.getSolver();

        String tps_limite = timelimit + "s";
        mon_solveur.limitTime(tps_limite);
        mon_solveur.showShortStatistics();
        mon_solveur.setSearch(
                Search.intVarSearch(
                        new FirstFail(mon_modele), // selecteur de variable
                        new IntDomainLast(solution_init, new IntDomainRandom(314)), // choix de la valeur
                       //new IntDomainLast(solution_init, new IntDomainMiddle(true)), // choix de la valeur
                        lambda),
                Search.intVarSearch(
                        new FirstFail(mon_modele), // selecteur de variable
                        new IntDomainMin(),
                        Cmax)
        );

        mon_solveur.setLDS(50 * ecart_maximal);

        Solution s = new Solution(mon_modele);

        long debut = new java.util.Date().getTime();

        //mon_solveur.showDashboard(2);
        
        while (mon_solveur.solve()) {
            s.record();
        }

        affichage(mon_solveur, s, Cmax);
        

        if (mon_solveur.isFeasible() == ESat.TRUE) {
            if (best_sol > s.getIntVal(Cmax)) {
                best_sol = s.getIntVal(Cmax);
                long fin = new java.util.Date().getTime();
                best_temps = fin - debut;
            }
            avg_sol += s.getIntVal(Cmax);

        } else {
            avg_sol += Dmax;
        }

        best_temps = (long) best_temps / (long) 1000;
        System.out.println("\n\n");
    }

    public static void fichier_resultat(String nom, int num_instance) {
        try {
            File destination;
            destination = new File(nom);
            FileOutputStream Ouput = new FileOutputStream(destination, true);
            PrintWriter ecrivain;
            ecrivain = new PrintWriter(Ouput);

            String num = "la" + Integer.toString(num_instance);

            ecrivain.write(num + ";");

            ecrivain.write(timelimit + ";");
            ecrivain.write(borne_inf + ";");
            ecrivain.write(best_sol + ";");
            ecrivain.write(best_temps + ";");
            ecrivain.write(avg_sol + ";");
            ecrivain.write(nb_max_iter + ";");

            ecrivain.write("\n");

            ecrivain.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        };

    }

    
    public static void fichier_resultat2(String nom, int num_instance, 
                                         double best_sol, double duree) {
        try {
            File destination;
            destination = new File(nom);
            FileOutputStream Ouput = new FileOutputStream(destination, true);
            PrintWriter ecrivain;
            ecrivain = new PrintWriter(Ouput);

            String num = "la" + Integer.toString(num_instance);

            ecrivain.write(num + ";");

            ecrivain.write(timelimit + ";");
            ecrivain.write(borne_inf + ";");
            ecrivain.write(best_sol + ";");
            ecrivain.write(duree + ";");


            ecrivain.write("\n");

            ecrivain.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        };

    }
    
    
    public static void intialiser_fichier_resultat(String nom) {

        try {
            File destination;
            destination = new File(nom);
            FileOutputStream Ouput = new FileOutputStream(destination, true);
            PrintWriter ecrivain;
            ecrivain = new PrintWriter(Ouput);

            ecrivain.write("\n\n\n");
            ecrivain.write("Jour ; Heure");
            ecrivain.write("\n");
            Date actuelle = new Date();
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            String dat = dateFormat.format(actuelle);
            ecrivain.write(dat + ";");

            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm");
            String texte_date = sdf.format(new Date());

            ecrivain.write(texte_date + ";");
            ecrivain.write("\n");
            ecrivain.write("\n");

            ecrivain.write("\n");
            ecrivain.write("\n");

            ecrivain.write(" Instance; TL; Borne_inf; Best_sol ; Best Temps ; Avg_sol; Nb_repliques");
            ecrivain.write("\n");

            ecrivain.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        };

    }




public static void intialiser_fichier_resultat2(String nom) {

        try {
            File destination;
            destination = new File(nom);
            FileOutputStream Ouput = new FileOutputStream(destination, true);
            PrintWriter ecrivain;
            ecrivain = new PrintWriter(Ouput);

            ecrivain.write("\n\n\n");
            ecrivain.write("Jour ; Heure");
            ecrivain.write("\n");
            Date actuelle = new Date();
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            String dat = dateFormat.format(actuelle);
            ecrivain.write(dat + ";");

            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm");
            String texte_date = sdf.format(new Date());

            ecrivain.write(texte_date + ";");
            ecrivain.write("\n");
            ecrivain.write("\n");

            ecrivain.write("\n");
            ecrivain.write("\n");

            ecrivain.write(" Instance; TL; Borne_inf; Best_sol ; Best Temps ");
            ecrivain.write("\n");

            ecrivain.flush();
            ecrivain.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        };

    }

}