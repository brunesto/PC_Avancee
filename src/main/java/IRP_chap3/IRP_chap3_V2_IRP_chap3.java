/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package IRP_chap3;

import java.io.BufferedReader;
import java.util.*;
import org.chocosolver.solver.*;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.selectors.variables.DomOverWDeg;
import org.chocosolver.solver.search.strategy.selectors.variables.MaxRegret;
import org.chocosolver.solver.variables.*;
import org.chocosolver.util.ESat;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StreamTokenizer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainLast;
import org.chocosolver.solver.search.strategy.selectors.variables.FirstFail;
import org.chocosolver.util.ESat;

public class IRP_chap3_V2_IRP_chap3 {

    // var. Globale      
    public static int CapaMax = 289;  // capacité des camions
    public static int G = 9999;
    public static int borne_sup_distance = 30000;
    public static int N; // nombre de customers dont le dépot fictif qui est le customer 0
    public static int V;  // nombre de véhicules
    public static int V_etoile; // camion fictif qui n'existe nulle part       
    public static int K;  // nombre de périodes
    public static int nb_total_visite;
    public static int H; // borne sup. de la distance
    public static int H_stock; // borne sup. du stock

    public static double[] Coor_x;
    public static double[] Coor_y;
    public static int[][] T; // matrice des distances       

    public static int[][] D;
    public static int[] C;
    public static int[] Stock_max_client;
    public static int[] Stock_min_client;
    public static int[] Stock_Initial_client;
    public static int[] Stock_min_Producteur;
    public static int[] Stock_max_Producteur;
    public static int Stock_Initial_Producteur;
    public static float cout_stockage_producteur;
    public static float[] cout_stockage_client;
    public static int[][] T_prime;
    public static int[] Prod;
    public static int[][] Sv;

    public static Solution solution;
    public static IntVar[] y_linear;
    public static IntVar[] Stock_linear;
    public static IntVar[] p_linear;
    public static IntVar[] g_linear;             // quantité livrée
    public static IntVar[] PStock;
    public static IntVar[][] s;

    public static int[] g_linear_initial;
    public static int[] Stock_linear_initial;
    public static int[] y_linear_initial;
    public static int[] p_linear_initial;
    public static int[] PStock_initial;

    public static double[] mon_obj;

    public static void lire_fichier(String nom_fichier_data) {
        try {
            File source = null;
            source = new File(nom_fichier_data);
            FileReader Input = new FileReader(source);
            StreamTokenizer Lecteur;
            Lecteur = new StreamTokenizer(Input);
            Lecteur.parseNumbers();
            int nombre = 0;

            G = 9999;
            borne_sup_distance = 30000;

            // nb de client
            Lecteur.nextToken();
            if (Lecteur.ttype == Lecteur.TT_NUMBER) {
                N = (int) Lecteur.nval;
            } else {
                System.out.println("pas de nombre entier a  lire");
            }
            // autant de véhicule que de clients
            V = 2;
            //V_etoile = 5 ; // camion fictif qui n'existe nulle part

            // nb de période
            Lecteur.nextToken();
            if (Lecteur.ttype == Lecteur.TT_NUMBER) {
                K = (int) Lecteur.nval;
            } else {
                System.out.println("pas de nombre entier a  lire");
            }

            // le sommet 0 est le dépot
            nb_total_visite = N + V * 2;
            // les données
            H = 30000; // borne sup. de la distance
            H_stock = 300000; // borne sup. du stock
            // creation des tableaux contenant les coordonnées
            Coor_x = new double[N + 1];
            Coor_y = new double[N + 1];
            Prod = new int[K + 1];
            Stock_Initial_client = new int[N];
            Stock_min_Producteur = new int[1];
            Stock_min_client = new int[N];
            Stock_max_client = new int[N];
            D = new int[nb_total_visite][K];
            C = new int[V + 1];
            cout_stockage_client = new float[N];
            T = new int[N][N];
            T_prime = new int[nb_total_visite][nb_total_visite];

            // capamax
            Lecteur.nextToken();
            if (Lecteur.ttype == Lecteur.TT_NUMBER) {
                CapaMax = (int) Lecteur.nval;
            } else {
                System.out.println("pas de nombre entier a  lire");
            }

            CapaMax = (int) (CapaMax / 2);
            C[0] = 0;
            for (int i = 1; i <= V; i++) {
                C[i] = CapaMax;
            }

            // lecture ligne fournisseur
            int bidon;
            Lecteur.nextToken();
            if (Lecteur.ttype == Lecteur.TT_NUMBER) {
                bidon = (int) Lecteur.nval;
            } else {
                System.out.println("pas de nombre entier a  lire");
            }
            // lecture ligne fournisseur
            Lecteur.nextToken();
            if (Lecteur.ttype == Lecteur.TT_NUMBER) {
                Coor_x[0] = (int) Lecteur.nval;
            } else {
                System.out.println("pas de nombre entier a  lire");
            }
            Lecteur.nextToken();
            if (Lecteur.ttype == Lecteur.TT_NUMBER) {
                Coor_y[0] = (int) Lecteur.nval;
            } else {
                System.out.println("pas de nombre entier a  lire");
            }
            Lecteur.nextToken();
            if (Lecteur.ttype == Lecteur.TT_NUMBER) {
                Stock_Initial_Producteur = (int) Lecteur.nval;
            } else {
                System.out.println("pas de nombre entier a  lire");
            }
            Lecteur.nextToken();
            if (Lecteur.ttype == Lecteur.TT_NUMBER) {
                Prod[0] = (int) Lecteur.nval;
            } else {
                System.out.println("pas de nombre entier a  lire");
            }

            for (int p = 1; p <= K; p++) {
                Prod[p] = Prod[0];
            }

            Lecteur.nextToken();
            if (Lecteur.ttype == Lecteur.TT_NUMBER) {
                cout_stockage_producteur = (float) Lecteur.nval;
            } else {
                System.out.println("pas de nombre entier a  lire");
            }

            Stock_max_Producteur = new int[1];
            Stock_max_Producteur[0] = 5000;

            // les clients
            for (int i = 1; i < N; i++) {
                Lecteur.nextToken();
                if (Lecteur.ttype == Lecteur.TT_NUMBER) {
                    bidon = (int) Lecteur.nval;
                } else {
                    System.out.println("pas de nombre entier a  lire");
                }
                // lecture ligne fournisseur
                Lecteur.nextToken();
                if (Lecteur.ttype == Lecteur.TT_NUMBER) {
                    Coor_x[i] = (int) Lecteur.nval;
                } else {
                    System.out.println("pas de nombre entier a  lire");
                }
                Lecteur.nextToken();
                if (Lecteur.ttype == Lecteur.TT_NUMBER) {
                    Coor_y[i] = (int) Lecteur.nval;
                } else {
                    System.out.println("pas de nombre entier a  lire");
                }
                Lecteur.nextToken();
                if (Lecteur.ttype == Lecteur.TT_NUMBER) {
                    Stock_Initial_client[i] = (int) Lecteur.nval;
                } else {
                    System.out.println("pas de nombre entier a  lire");
                }
                Lecteur.nextToken();
                if (Lecteur.ttype == Lecteur.TT_NUMBER) {
                    Stock_max_client[i] = (int) Lecteur.nval;
                } else {
                    System.out.println("pas de nombre entier a  lire");
                }
                Lecteur.nextToken();
                if (Lecteur.ttype == Lecteur.TT_NUMBER) {
                    Stock_min_client[i] = (int) Lecteur.nval;
                } else {
                    System.out.println("pas de nombre entier a  lire");
                }
                Lecteur.nextToken();
                if (Lecteur.ttype == Lecteur.TT_NUMBER) {
                    D[i][1] = (int) Lecteur.nval;
                } else {
                    System.out.println("pas de nombre entier a  lire");
                }
                D[i][0] = 0;
                D[0][0] = 0;
                for (int j = 0; j < K; j++) {
                    D[i][j] = (int) Lecteur.nval;
                }

                Lecteur.nextToken();
                if (Lecteur.ttype == Lecteur.TT_NUMBER) {
                    cout_stockage_client[i] = (float) Lecteur.nval;
                } else {
                    System.out.println("pas de nombre entier a  lire");
                }
            }

            for (int i = 0; i < N; i++) {
                double ox = Coor_x[i];
                double oy = Coor_y[i];
                for (int j = 0; j < N; j++) {
                    double ox2 = Coor_x[j];
                    double oy2 = Coor_y[j];
                    double dist = Math.abs(Coor_x[j] - Coor_x[i]) * Math.abs(Coor_x[j] - Coor_x[i]);
                    dist = dist + Math.abs(Coor_y[j] - Coor_y[i]) * Math.abs(Coor_y[j] - Coor_y[i]);
                    dist = Math.sqrt(dist);
                    T[i][j] = (int) (Math.round(dist));
                }
            }

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

            Input.close();
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        //******************************************************// 
    }

    public static void resoudre(int distance_max,
            int repartir_solution,
            int duree_maximale) {
        // déclaration du modèle
        Model mon_modele;
        mon_modele = new Model("IRP en PPC");

        //*****************************************************************************//
        // contrainte 1.1 et 1.2 
        //   IntVar[][] s;             // le successeur de chaque visite et par période
        s = new IntVar[nb_total_visite][K];

        IntVar[][] s_t;             // le successeur de chaque visite et par période
        s_t = new IntVar[K][nb_total_visite];

        // contrainte 2.1 et 2.2
        for (int k = 0; k < K; k++) {
            s[0][k] = mon_modele.intVar("s_0_" + k, 0);
            s_t[k][0] = mon_modele.intVar("s_t_" + k + "_0", 0);
        }

        for (int k = 0; k < K; k++) {
            for (int i = 1; i < nb_total_visite; i++) {
                if (i < N + V) {
                    s[i][k] = mon_modele.intVar("s_" + i + "_" + k, 1, nb_total_visite);
                    s_t[k][i] = mon_modele.intVar("s_t_" + k + "_" + i, 1, nb_total_visite);     // le noeud 0 ne compte pas
                } else {
                    s[i][k] = mon_modele.intVar("s_" + i + "_" + k, 0);
                    s_t[k][i] = mon_modele.intVar("s_t_" + k + "_" + i, 0);
                }
            }
        }

        // contrainte 2.3
        for (int k = 0; k < K; k++) {
            for (int i = 1; i < nb_total_visite; i++) {

                mon_modele.arithm(s[i][k], "=", s_t[k][i]).post();
            }
        }

        // contrainte 2 bis
        IntVar[][] g;
        g = new IntVar[nb_total_visite][K];

        for (int i = 1; i < nb_total_visite; i++) {
            for (int k = 0; k < K; k++) {
                g[i][k] = mon_modele.intVar("g_" + i + "_" + k, 0, G);
            }
        }

        // contrainte 3, 4 et 4 bis
        IntVar[][] a;
        a = new IntVar[nb_total_visite][K];

        for (int k = 0; k < K; k++) {
            a[0][k] = mon_modele.intVar("a_0_" + k, 0); // sommet 0 est fictif
        }

        for (int k = 0; k < K; k++) {
            for (int i = 1; i < nb_total_visite; i++) {
                a[i][k] = mon_modele.intVar("a_" + i + "_" + k, 0, V);
            }
        }

        for (int k = 0; k < K; k++) {
            for (int i = 1; i <= V; i++) {
                int position = N + i - 1; // dépot de départ du véhicule i
                a[position][k] = mon_modele.intVar("a_" + position + "_" + k, i);
                position = N + i + V - 1; // dépot d'arrivé du véhicule i
                a[position][k] = mon_modele.intVar("a_" + position + "_" + k, i);
            }
        }

        // contrainte 5-6
        IntVar[][] p;
        p = new IntVar[nb_total_visite][K];

        // contrainte 5 : définition des rangs
        for (int k = 0; k < K; k++) {
            p[0][k] = mon_modele.intVar("p_0_" + k, 0); // sommet 0 fictif
        }
        for (int k = 0; k < K; k++) {
            for (int i = 1; i < nb_total_visite; i++) {
                p[i][k] = mon_modele.intVar("p_" + i + "_" + k, 0, N);
            }
        }

        // contrainte 6 :  les dépot de départ sont toujours au rang 0 !
        for (int k = 0; k < K; k++) {
            for (int i = 0; i < V; i++) {
                int position = N + i;
                p[position][k] = mon_modele.intVar("p_" + position, 0);
            }
        }

        // contrainte 7.1 et 7.2.
        for (int k = 0; k < K; k++) {
            IntVar[] listes = new IntVar[nb_total_visite];

            listes[0] = mon_modele.intVar("listes_0", -1);

            for (int i = 1; i < nb_total_visite; i++) {
                listes[i] = mon_modele.intVar("listes_" + i, 0, nb_total_visite);
                mon_modele.arithm(listes[i], "=", s[i][k]).post();
            }
            mon_modele.allDifferentExcept0(listes).post();
        }

        // contrainte 8 : a[i]=a[s[i]]
        IntVar[][] inv_a;
        inv_a = new IntVar[K][nb_total_visite];

        for (int k = 0; k < K; k++) {
            inv_a[k][0] = mon_modele.intVar("inv_" + k + "_0", 0); // sommet 0 est fictif
        }

        // contrainte 8.1.
        for (int k = 0; k < K; k++) {
            for (int i = 1; i < nb_total_visite; i++) {
                inv_a[k][i] = mon_modele.intVar("a_inv" + k + "_" + i, 0, V);
                mon_modele.arithm(a[i][k], "=", inv_a[k][i]).post();
            }
        }

        // contrainte 8.2.
        for (int k = 0; k < K; k++) {
            for (int i = 1; i < N + V; i++) {
                mon_modele.element(a[i][k], inv_a[k], s[i][k], 0).post();

            }
        }

        // contrainte 9.1 a 9.3
        BoolVar[][] bb = new BoolVar[N + V][K];
        for (int i = 1; i < N + V; i++) {
            for (int k = 0; k < K; k++) {

                bb[i][k] = mon_modele.boolVar("bb_" + i + "_" + k);

                // contrainte 9.1.
                mon_modele.reifyXgtC(g[i][k], 0, bb[i][k]);

                // contrainte 9.2 et 9.3.
                mon_modele.ifThenElse(bb[i][k],
                        mon_modele.arithm(s[i][k], "!=", i),
                        mon_modele.arithm(s[i][k], "=", i)
                );

                mon_modele.ifThenElse(bb[i][k],
                        mon_modele.arithm(s[i][k], "!=", i),
                        mon_modele.arithm(a[i][k], "=", 0)
                );
            }
        }

        // contraintes 10 
        // contrainte 10.3.1        
        IntVar[][] inv_p;
        inv_p = new IntVar[K][nb_total_visite];

        for (int k = 0; k < K; k++) {
            inv_p[k][0] = mon_modele.intVar("inv_p_" + k + "_0", 0);
            for (int i = 1; i < nb_total_visite; i++) {
                inv_p[k][i] = mon_modele.intVar("inv_p_" + k + "_" + i, 0, N);
                mon_modele.arithm(p[i][k], "=", inv_p[k][i]).post();
            }
        }

        for (int i = 1; i < N + V; i++) {
            for (int u = 0; u < K; u++) {
                // contrainte 10.3.2.   
                IntVar t = mon_modele.intVar("t_" + i + "_" + u, 0, nb_total_visite);
                // contrainte 10.3.3.
                mon_modele.arithm(t, "=", p[i][u], "+", bb[i][u]).post();
                // contrainte 10.3.3.
                mon_modele.element(t, inv_p[u], s[i][u], 0).post();
            }
        }

        // contrainte 11.1
        BoolVar[][][] w;
        w = new BoolVar[nb_total_visite][V + 1][K];
        for (int k = 0; k < K; k++) {
            for (int j = 1; j <= V; j++) {
                for (int i = 1; i < nb_total_visite; i++) {
                    w[i][j][k] = mon_modele.boolVar("w_" + i + "_" + j + "_" + k);
                }
            }
        }

        // contrainte 11.2      
        for (int i = 1; i < nb_total_visite; i++) {
            for (int j = 1; j <= V; j++) {
                for (int k = 0; k < K; k++) {

                    mon_modele.reification(w[i][j][k],
                            mon_modele.arithm(a[i][k], "=", j)
                    );
                }
            }
        }

        // contrainte 11.3. 
        for (int k = 0; k < K; k++) // periode
        {
            for (int v = 1; v <= V; v++) {
                IntVar[] coeffs = new IntVar[nb_total_visite];
                for (int i = 0; i < nb_total_visite; i++) {
                    coeffs[i] = w[i][v][k];
                }
                IntVar[] ligne = new IntVar[nb_total_visite];
                for (int i = 0; i < nb_total_visite; i++) {
                    ligne[i] = g[i][k];
                }

                IntVar[] produit = new IntVar[N];
                produit[0] = mon_modele.intVar("produit_0_0_" + k, 0);

                for (int j = 1; j < N; j++) {
                    produit[j] = mon_modele.intVar("produit_" + j + "_" + v + "_" + k, 0, 9999);
                    mon_modele.arithm(coeffs[j], "*", ligne[j], "=", produit[j]).post();
                }
                mon_modele.sum(produit, "<=", C[v]).post();
            }
        }

        // C12. calculer la distance  
        IntVar[][] dp; // distance entre i et j et ceci pour chaque période
        IntVar[] d; // objectif par période

        dp = new IntVar[K][N + V];
        d = new IntVar[K];

        for (int u = 0; u < K; u++) {
            // contrainte 12.1.
            dp[u][0] = mon_modele.intVar(0);
            for (int i = 1; i < N + V; i++) {
                dp[u][i] = mon_modele.intVar("dp_" + i, 0, H);
            }
            // contrainte 12.2
            for (int i = 1; i < N + V; i++) {
                mon_modele.element(dp[u][i], T_prime[i], s[i][u]).post();
            }
            // contrainte 12.3
            d[u] = mon_modele.intVar("d_" + u, 0, H);
            mon_modele.sum(dp[u], "=", d[u]).post();
        }

        // contrainte 13
        IntVar d_total = mon_modele.intVar("distance", 0, borne_sup_distance);
        mon_modele.sum(d, "=", d_total).post();

        // contrainte 14    
        for (int u = 0; u < K; u++) {
            for (int i = N; i < N + V - 1; i++) {
                mon_modele.arithm(s[i][u], "<", s[i + 1][u]).post();
            }
        }

        //contrainte 15 - 18
        // contrainte 17.1
        IntVar[][] capaSumSucc = new IntVar[nb_total_visite][K];
        IntVar[][] capaSum = new IntVar[K][nb_total_visite];

        for (int k = 0; k < K; k++) // periode
        {
            capaSum[k][0] = mon_modele.intVar("capaSum_" + k + "_0", 0);
            for (int i = 1; i < nb_total_visite; i++) {
                capaSumSucc[i][k] = mon_modele.intVar("capaSumSucc" + i + "_" + k, 0, CapaMax);
                capaSum[k][i] = mon_modele.intVar("capaSum_" + k + "_" + i, 0, CapaMax);
            }
        }

        for (int u = 0; u < K; u++) {
            mon_modele.arithm(capaSum[u][0], "=", 0).post();
            for (int i = 1; i < N + V; i++) {
                // contrainte 17.2
                mon_modele.element(capaSumSucc[i][u], capaSum[u], s[i][u], 0).post();
                // contrainte 17.3
                mon_modele.arithm(capaSumSucc[i][u], "=", capaSum[u][i], "+", g[i][u]).post();
            }
            // contrainte 18
            for (int i = N + V; i < nb_total_visite; i++) {
                mon_modele.arithm(capaSum[u][i], "<=", CapaMax).post();
            }
        }

        // contrainte 19-21
        // contrainte 19
        IntVar[][] Stock = new IntVar[N][K + 1];
        for (int i = 1; i < N; i++) {
            for (int k = 0; k <= K; k++) {
                Stock[i][k] = mon_modele.intVar("stock_" + i + "_" + k, Stock_min_client[i], Stock_max_client[i]);
            }
        }

        for (int i = 1; i < N; i++) {
            int[] Coeffs1 = new int[2];
            Coeffs1[0] = 1;
            Coeffs1[1] = -1;
            IntVar[] ligne1 = new IntVar[2];
            ligne1[0] = Stock[i][1];
            ligne1[1] = g[i][0];

            // contrainte 20
            mon_modele.scalar(ligne1, Coeffs1, "=", Stock_Initial_client[i] - D[i][0]).post();

            // contrainte 21           
            for (int u = 2; u <= K; u++) {
                int[] Coeffs2 = new int[3];
                Coeffs2[0] = 1;
                Coeffs2[1] = -1;
                Coeffs2[2] = -1;
                IntVar[] ligne2 = new IntVar[3];
                ligne2[0] = Stock[i][u];
                ligne2[1] = Stock[i][u - 1];
                ligne2[2] = g[i][u - 1];
                mon_modele.scalar(ligne2, Coeffs2, "=", -D[i][u - 1]).post();
            }
        }

        // contrainte 22-14        
        // Contrainte 22
        PStock = new IntVar[K + 1];
        PStock[0] = mon_modele.intVar("Pstock_0", Stock_Initial_Producteur, Stock_Initial_Producteur);

        for (int k = 1; k <= K; k++) {
            PStock[k] = mon_modele.intVar("Pstock_" + k, Stock_min_Producteur[0], Stock_max_Producteur[0]);
        }

        // contrainte 23
        // PStock[1] + Somme sur i des g[i][0]  = +Stock_Initial_Producteur[0] + Prod[0]
        IntVar somme = mon_modele.intVar("somme_0", 0, H);
        int[] CoeffsS = new int[N - 1];
        IntVar[] ligneS = new IntVar[N - 1];
        for (int i = 1; i < N; i++) {
            CoeffsS[i - 1] = 1;
            ligneS[i - 1] = g[i][0];
        }
        mon_modele.scalar(ligneS, CoeffsS, "=", somme).post();

        int[] Coeffs4 = new int[2];
        Coeffs4[0] = 1;
        Coeffs4[1] = 1;
        IntVar[] ligne4 = new IntVar[2];
        ligne4[0] = PStock[1];
        ligne4[1] = somme;

        mon_modele.scalar(ligne4, Coeffs4, "=", Stock_Initial_Producteur + Prod[0]).post();

        // contrainte 24           
        for (int k = 2; k <= K; k++) {
            IntVar somme2S = mon_modele.intVar("somme_" + k, 0, H);

            int[] CoeffsS2 = new int[N - 1];
            IntVar[] ligneS2 = new IntVar[N - 1];
            for (int i = 1; i < N; i++) {
                CoeffsS2[i - 1] = 1;
                ligneS2[i - 1] = g[i][k - 1];
            }
            mon_modele.scalar(ligneS2, CoeffsS2, "=", somme2S).post();

            int[] Coeffs5 = new int[3];
            Coeffs5[0] = 1;
            Coeffs5[1] = -1;
            Coeffs5[2] = +1;
            IntVar[] ligne5 = new IntVar[3];
            ligne5[0] = PStock[k];
            ligne5[1] = PStock[k - 1];
            ligne5[2] = somme2S;

            mon_modele.scalar(ligne5, Coeffs5, "=", +Prod[k - 1]).post();
            // PStock[u]=PStock[u-1]-somme des g[i][u-1] + Prod[u-1] ;  
        }

        // contrainte 25 : le cout de stockage des clients et du 26
        RealVar cout_stockage = mon_modele.realVar("cout_stockage", 0, 10 * H, 0.01);

        // contrainte 26 : le cout de stockage des clients et du producteur
        int taille = N * K + 1;
        double[] CoeffsT = new double[taille];
        Variable[] ligneT = new Variable[taille];

        int pos = 0;
        for (int k = 0; k < K; k++) {
            CoeffsT[pos] = cout_stockage_producteur;
            ligneT[pos] = PStock[k + 1];
            pos++;
        }

        for (int i = 1; i < N; i++) {
            for (int k = 0; k < K; k++) {
                CoeffsT[pos] = cout_stockage_client[i];
                ligneT[pos] = Stock[i][k + 1];
                pos++;
            }
        }

        CoeffsT[pos] = -1;
        ligneT[pos] = cout_stockage;

        mon_modele.scalar(ligneT, CoeffsT, "=", 0).post();

        // contrainte 27-28
        IntVar[][] y = new IntVar[K][N];
        for (int k = 0; k < K; k++) {

            y[k][0] = mon_modele.intVar(0);
            for (int i = 1; i < N; i++) {
                y[k][i] = mon_modele.intVar("y_" + k + "_" + i, 0, 100 * H + N);
            }

            int[] coeffs33 = new int[3];
            coeffs33[0] = 1;
            coeffs33[1] = -1000;
            coeffs33[2] = -1;
            for (int i = 1; i < N; i++) {
                IntVar[] ligne33 = new IntVar[3];
                ligne33[0] = y[k][i];
                ligne33[1] = a[i][k];
                ligne33[2] = s[i][k];
                mon_modele.scalar(ligne33, coeffs33, "=", 0).post();
            }

        }

        // contrainte 29
        RealVar mon_objectif = mon_modele.realVar("mon_objectif", 0, 100 * H, 0.001);

        //RealVar mon_objectif = mon_modele.realVar("mon_objectif", borne_inf_cout, borne_sup_cout, 0.001);
        // contrainte 30
        int tailleO = 3;
        double[] CoeffsO = new double[tailleO];
        Variable[] ligneO = new Variable[tailleO];

        CoeffsO[0] = 1;
        ligneO[0] = d_total;

        CoeffsO[1] = 1;
        ligneO[1] = cout_stockage;

        CoeffsO[2] = -1;
        ligneO[2] = mon_objectif;

        mon_modele.scalar(ligneO, CoeffsO, "=", 0).post();

        // contrainte 31-34
        if (distance_max != -1) {
            K = K - 1;
            // contrainte 31
            BoolVar[][] dist_M; // distance entre i et j
            dist_M = new BoolVar[N + V][K];
            for (int u = 0; u < K; u++) {
                dist_M[0][u] = mon_modele.boolVar("dist_M_0_" + u, false);
                for (int i = 1; i < N + V; i++) {
                    dist_M[i][u] = mon_modele.boolVar("dist_M_" + i + "_" + u);
                }
            }
            // contrainte 32
            for (int i = 1; i < N + V; i++) {
                //  System.out.println("i= "+i);
                for (int u = 0; u < K; u++) {
                    mon_modele.reification(dist_M[i][u], mon_modele.arithm(s[i][u], "!=", Sv[i][u]));
                }
            }
            // contrainte 33
            int taille_totale = (N - 1) * K; //(N+V-1)*(K);
            int[] Coeffs = new int[taille_totale];
            for (int c = 0; c < taille_totale; c++) {
                Coeffs[c] = 1;
            }
            int c = 0;
            IntVar[] ligne = new IntVar[taille_totale];
            for (int i = 1; i < N; i++) {
                for (int u = 0; u < K; u++) {
                    ligne[c] = dist_M[i][u];
                    c++;
                }
            }
            IntVar Distance = mon_modele.intVar("Distance", 0, 9999);
            mon_modele.scalar(ligne, Coeffs, "=", Distance).post();

            // contrainte 34
            mon_modele.arithm(Distance, "<=", distance_max).post();
            K = K + 1;
        }

        // ***************** MINIMISATION ***************//
        mon_modele.setObjective(Model.MINIMIZE, mon_objectif);

        Solver mon_solveur = mon_modele.getSolver();

        solution = new Solution(mon_modele);

        Date heure_debut = new Date();
        long h_debut = heure_debut.getTime();

        mon_solveur.limitTime(duree_maximale + "s");

        // il faut lineariser le y
        y_linear = new IntVar[K * N];
        int r = 0;
        for (int k = 0; k < K; k++) {
            y_linear[r] = mon_modele.intVar("y_linear_" + r, 0, 100 * H + N);
            mon_modele.arithm(y_linear[r], "=", y[k][0]).post();
            r++;
            //y[k][0] = mon_modele.intVar(0);

            for (int i = 1; i < N; i++) {
                y_linear[r] = mon_modele.intVar("y_linear_" + r, 0, 100 * H + N);
                mon_modele.arithm(y_linear[r], "=", y[k][i]).post();
                r++;
            }
        }
        // il faut lineariser le Stock[i][u]
        Stock_linear = new IntVar[(N - 1) * (K)];
        r = 0;

        for (int u = 0; u < K; u++) {
            for (int i = 1; i < N; i++) {
                Stock_linear[r] = mon_modele.intVar("stock_linear_" + r, Stock_min_client[i], Stock_max_client[i]);
                mon_modele.arithm(Stock_linear[r], "=", Stock[i][u]).post();
                r++;
                //Stock[i][u] = mon_modele.intVar("stock_"+i+"_"+u,Stock_min_client[i],Stock_max_client[i]);      
                // Stock_final[i][u] = mon_modele.intVar("stock_final"+i+"_"+u,Stock_min_client[i],Stock_max_client[i]*10);      
            }
        }
        // il faut linéariser p
        p_linear = new IntVar[(nb_total_visite) * (K)];
        r = 0;
        for (int k = 0; k < K; k++) {
            p_linear[r] = mon_modele.intVar("p_linear_" + r, 0); // sommet 0 fictif
            mon_modele.arithm(p_linear[r], "=", p[0][k]).post();

            r++;
            // p[0][k] = mon_modele.intVar("p_0_"+k, 0); // sommet 0 fictif
        }
        for (int k = 0; k < K; k++) {
            for (int i = 1; i < nb_total_visite; i++) {
                p_linear[r] = mon_modele.intVar("p_linear_" + r, 0, N);
                mon_modele.arithm(p_linear[r], "=", p[i][k]).post();
                r++;
                // p[i][k] = mon_modele.intVar("p_"+i+"_"+k, 1,N);
            }
        }
        // il faut lineariser le g
        g_linear = new IntVar[(nb_total_visite - 1) * K];
        r = 0;
        for (int k = 0; k < K; k++) {
            for (int i = 1; i < nb_total_visite; i++) {
                g_linear[r] = mon_modele.intVar("g_linear_" + r, 0, 9999);
                mon_modele.arithm(g_linear[r], "=", g[i][k]).post();
                r++;
            }
        }

        //******************************************************************************//
        //*** repartir solution                                                      ***//
        //***                                                                        ***//
        //******************************************************************************//
        Solution solution_init = new Solution(mon_modele);
        if (repartir_solution == 1) {

            for (int i = 0; i < (nb_total_visite - 1) * (K); i++) {
                solution_init.setIntVal(g_linear[i], g_linear_initial[i]);
            }

            for (int i = 0; i < K * N; i++) {
                solution_init.setIntVal(y_linear[i], y_linear_initial[i]);
            }

            for (int i = 0; i < (N - 1) * (K); i++) {
                solution_init.setIntVal(Stock_linear[i], Stock_linear_initial[i]);
            }

            for (int i = 0; i < (nb_total_visite) * (K); i++) {
                solution_init.setIntVal(p_linear[i], p_linear_initial[i]);

            }
            for (int k = 0; k <= K; k++) {
                solution_init.setIntVal(PStock[k], PStock_initial[k]);
            }
        }

        if (repartir_solution == 1) {
            mon_solveur.setSearch(
                    Search.intVarSearch(
                            new FirstFail(mon_modele), // selecteur de variable
                            new IntDomainLast(solution_init, new IntDomainMin()), // choix de la valeur
                            g_linear),
                    Search.intVarSearch(
                            new FirstFail(mon_modele), // selecteur de variable
                            new IntDomainLast(solution_init, new IntDomainMin()), // choix de la valeur
                            y_linear),
                    Search.intVarSearch(
                            new FirstFail(mon_modele), // selecteur de variable
                            new IntDomainLast(solution_init, new IntDomainMin()), // choix de la valeur
                            Stock_linear),
                    Search.intVarSearch(
                            new FirstFail(mon_modele), // selecteur de variable
                            new IntDomainLast(solution_init, new IntDomainMin()), // choix de la valeur
                            PStock),
                    Search.intVarSearch(
                            new FirstFail(mon_modele), // selecteur de variable
                            new IntDomainLast(solution_init, new IntDomainMin()), // choix de la valeur
                            p_linear)
            );

        } else {
// search strategy
            // 1/ g compatible with D & indomainMin  ==>  Z1=|gik-Dik] with Z2=2Z1  Z=Z2+greaterBoolean
            //    Start with Z=0 ie gik=Dik then Z1=1 with gik=Dik-1 and gik=Dik+1; Z=2 gik=Dik+-2 etc
            IntVar[] Z1 = mon_modele.intVarArray("Z1", (nb_total_visite - 1) * K, 0, 10000);
            IntVar[] Z1PlusMoins = mon_modele.intVarArray("Z1+-", (nb_total_visite - 1) * K, -100000, 10000);
            IntVar[] Z2 = mon_modele.intVarArray("Z1", (nb_total_visite - 1) * K, 0, 10000);
            BoolVar[] GGreaterD = mon_modele.boolVarArray("G>D", (nb_total_visite - 1) * K);
            IntVar[] Z = mon_modele.intVarArray("Z", (nb_total_visite - 1) * K, 0, 10000);
            for (int i = 1; i < nb_total_visite - 1; i++) {
                for (int k = 0; k < K; k++) {
                    mon_modele.arithm(Z1PlusMoins[i * K + k], "+", g_linear[i * K + k], "=", D[i][k]);
                    mon_modele.absolute(Z1[i * K + k], Z1PlusMoins[i * K + k]).post();
                    mon_modele.scalar(new IntVar[]{Z1[i * K + k], Z2[i * K + k]}, new int[]{2, -1}, "=", 0).post();
                    mon_modele.reifyXgtC(g_linear[i * K + k], D[i][k], GGreaterD[i * K + k]);
                    mon_modele.arithm(Z2[i * K + k], "+", GGreaterD[i * K + k], "=", Z[i * K + k]).post();
                }
            }
            mon_solveur.setSearch(
                    new DomOverWDeg(Z, 0, new IntDomainMin()), // 
                    Search.intVarSearch(
                            new MaxRegret(), // selecteur de variable
                            new IntDomainMin(), // choix de la valeur
                            g_linear
                    ),
                    Search.intVarSearch(
                            new MaxRegret(), // selecteur de variable
                            new IntDomainMin(), // choix de la valeur
                            y_linear
                    ),
                    Search.intVarSearch(
                            new MaxRegret(), // selecteur de variable
                            new IntDomainMin(), // choix de la valeur
                            Stock_linear
                    ),
                    Search.intVarSearch(
                            new MaxRegret(), // selecteur de variable
                            new IntDomainMin(), // choix de la valeur
                            PStock
                    ),
                    Search.intVarSearch(
                            new MaxRegret(), // selecteur de variable
                            new IntDomainMin(), // choix de la valeur
                            p_linear
                    )
            );
        }

        while (mon_solveur.solve()) {
            solution.record();
            Date heure_fin = new Date();
            long h_fin = heure_fin.getTime();
            long duree = h_fin - h_debut;
            long duree_s = duree / 1000;
            //System.out.println("temps : " + duree_s + " s    " + d.toString());

            double valeur = mon_objectif.getLB();
            double valeur2 = mon_objectif.getUB();

            System.out.println("temps : " + duree_s + " s    " + valeur);

        }

        if (mon_solveur.isFeasible() == ESat.TRUE) {

            for (int u = 0; u < K; u++) {
                System.out.println("Periode " + u);
                for (int i = 0; i < nb_total_visite; i++) {
                    int suc = solution.getIntVal(s[i][u]);
                    int affec = solution.getIntVal(a[i][u]);
                    int ran = solution.getIntVal(p[i][u]);
                    System.out.print("Sommet " + i + "    Succ : " + suc + "    |   affec " + affec + "    |   rang " + ran);
                    System.out.println(" |  T= " + T_prime[i][suc]);

                }
            }

//            for (int i = 1; i < N; i++) {
//                System.out.println("Client " + i + " : ");
//                System.out.print(Stock_Initial_client[i] + " |  ");
//                for (int u = 1; u < K; u++) {
//                    int vv = Stock[i][u].getLB();
//                    int ss = solution.getIntVal(Stock[i][u]);
//                    System.out.print(ss + " |  ");
//                }
//                System.out.println();
//                System.out.println("Client " + i + " : ");
//                for (int u = 0; u < K; u++) {
//                    int ss = solution.getIntVal(g[i][u]);
//                    System.out.print(ss + " |  ");
//                }
//                System.out.println();
//                System.out.println();
//                System.out.println();
//            }
            double[] Cs = solution.getRealBounds(cout_stockage);
            int cout_sol = solution.getIntVal(d_total);
            mon_obj = solution.getRealBounds(mon_objectif);

            Date heure_fin = new Date();
            long h_fin = heure_fin.getTime();
            long duree = h_fin - h_debut;
            long duree_s = duree / 1000;

            System.out.println("Cs : " + Cs[0]);
            System.out.println("cout_sol : " + cout_sol);
            System.out.println("mon_objectif : " + mon_obj[0]);

            System.out.println("temps : " + duree_s + "s");
            System.out.println("Fin");
        } else {
            System.out.println("pas de solution");
        }

    }

    // ******************************************************************* //
    // *************** programmpe principal                            *** //
    // ******************************************************************* //
    // public static void resoudre(int distance_max, 
    //                            int repartir_solution,
    //                            int duree_maximale)
    public static void main(String[] args) {

        try {
            String fichier_entree = "./data/irp/abs1n5_10p.dat";

            lire_fichier(fichier_entree);
            int nb_periode = K;

            // pour une résolution directe de abs1n5_10p.dat
            // faire
            // K = 10;
            // resoudre(-1, 0, 300);        
            // resolution pour le période 1.
            K = 1;
            resoudre(-1, 0, 30);

            int borne_inf = -1;
            borne_inf = (int) mon_obj[0];

            // on sauvergarde la solution
            g_linear_initial = new int[(nb_total_visite - 1) * (K + 1)];
            for (int i = 0; i < (nb_total_visite - 1) * (K); i++) {
                g_linear_initial[i] = solution.getIntVal(g_linear[i]);
            }
            for (int i = (nb_total_visite - 1) * (K); i < (nb_total_visite - 1) * (K + 1); i++) {
                g_linear_initial[i] = 0;
            }

            y_linear_initial = new int[(K + 1) * N];

            for (int i = 0; i < K * N; i++) {
                y_linear_initial[i] = solution.getIntVal(y_linear[i]);
            }
            for (int i = K * N; i < (K + 1) * N; i++) {
                y_linear_initial[i] = 0;
            }
            Stock_linear_initial = new int[(N - 1) * (K + 1)];
            for (int i = 0; i < (N - 1) * (K); i++) {
                Stock_linear_initial[i] = solution.getIntVal(Stock_linear[i]);
            }
            for (int i = (N - 1) * (K); i < (N - 1) * (K + 1); i++) {
                Stock_linear_initial[i] = 0;
            }

            p_linear_initial = new int[(nb_total_visite) * (K + 1)];
            for (int i = 0; i < (nb_total_visite) * (K); i++) {
                p_linear_initial[i] = solution.getIntVal(p_linear[i]);
            }
            for (int i = (nb_total_visite) * (K); i < (nb_total_visite) * (K + 1); i++) {
                p_linear_initial[i] = 0;
            }

            PStock_initial = new int[K + 2];
            PStock_initial[0] = solution.getIntVal(PStock[0]);

            for (int k = 1; k <= K; k++) {
                PStock_initial[k] = solution.getIntVal(PStock[k]);
            }
            for (int k = K + 1; k <= K + 1; k++) {
                PStock_initial[k] = 0;
            }

            // définition de SV
            Sv = new int[nb_total_visite][K];
            for (int i = 1; i < N + V; i++) {
                for (int u = 0; u < K; u++) {
                    Sv[i][u] = solution.getIntVal(s[i][u]);
                }
            }

            // les iterations 
            for (int periode = 2; periode <= nb_periode; periode++) {
                System.out.println("--------- " + periode + " ---------");
                System.out.println();

                /* System.gc();
            Runtime.getRuntime().gc();
            try{
            Thread.sleep(3000);            
            }catch(InterruptedException e){
            System.out.println(e.getMessage()); 
            }    */
                K = periode;
                resoudre(4, 1, periode * 10);

                borne_inf = (int) mon_obj[0];

                g_linear_initial = new int[(nb_total_visite - 1) * (K + 1)];
                for (int i = 0; i < (nb_total_visite - 1) * (K); i++) {
                    g_linear_initial[i] = solution.getIntVal(g_linear[i]);
                }
                for (int i = (nb_total_visite - 1) * (K); i < (nb_total_visite - 1) * (K + 1); i++) {
                    g_linear_initial[i] = 0;
                }

                y_linear_initial = new int[(K + 1) * N];

                for (int i = 0; i < K * N; i++) {
                    y_linear_initial[i] = solution.getIntVal(y_linear[i]);
                }
                for (int i = K * N; i < (K + 1) * N; i++) {
                    y_linear_initial[i] = 0;
                }
                Stock_linear_initial = new int[(N - 1) * (K + 1)];
                for (int i = 0; i < (N - 1) * (K); i++) {
                    Stock_linear_initial[i] = solution.getIntVal(Stock_linear[i]);
                }
                for (int i = (N - 1) * (K); i < (N - 1) * (K + 1); i++) {
                    Stock_linear_initial[i] = 0;
                }

                p_linear_initial = new int[(nb_total_visite) * (K + 1)];
                for (int i = 0; i < (nb_total_visite) * (K); i++) {
                    p_linear_initial[i] = solution.getIntVal(p_linear[i]);
                }
                for (int i = (nb_total_visite) * (K); i < (nb_total_visite) * (K + 1); i++) {
                    p_linear_initial[i] = 0;
                }

                PStock_initial = new int[K + 2];
                PStock_initial[0] = solution.getIntVal(PStock[0]);

                for (int k = 1; k <= K; k++) {
                    PStock_initial[k] = solution.getIntVal(PStock[k]);
                }
                for (int k = K + 1; k <= K + 1; k++) {
                    PStock_initial[k] = 0;
                }

                // définition de SV
                Sv = new int[nb_total_visite][K];
                for (int i = 1; i < N + V; i++) {
                    for (int u = 0; u < K; u++) {
                        Sv[i][u] = solution.getIntVal(s[i][u]);
                    }
                }

            } // fin des iterations
            System.gc();
            System.out.println("fin des iterations");
        } catch (Exception E) {
            System.out.println(E.getMessage());
        }
    }

}
