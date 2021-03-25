/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chap3_irp;

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
import java.io.PrintWriter;
import java.io.StreamTokenizer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainLast;
import org.chocosolver.solver.search.strategy.selectors.variables.FirstFail;
import org.chocosolver.util.ESat;

public class IRP_chap3_IRP_chap3 {

    // var. Globale      
    public static int CapaMax = 144;  // capacité des camions
    public static int G = 9999;
    public static int borne_sup_distance = 3000;
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

    public static int[] g_linear_initial;
    public static int[] Stock_linear_initial;
    public static int[] y_linear_initial;
    public static int[] p_linear_initial;
    public static int[] PStock_initial;

    public static void demonstration() {

        CapaMax = 289;  // capacité des camions

        G = 9999;

        borne_sup_distance = 3000;

        // les paramétres
        N = 6; // nombre de customers dont le dépot fictif qui est le customer 0
        V = 2;  // nombre de véhicules
        V_etoile = 5; // camion fictif qui n'existe nulle part

        K = 3;  // nombre de périodes
        //  int MS=10; // stock maximal (identique pour tous les clients)

        // le sommet 0 est le dépot
        // Astuce : pour chaque véhicule, on créé 2 dépots : le dépot de départ et le dépot d'arrivée
        //          Et on supprime le noeud 0
        nb_total_visite = N + V * 2;

        // les données
        H = 3000; // borne sup. de la distance
        H_stock = 300000; // borne sup. du stock

        // pour chaque client i, D_i_p_k demande en produit p pour la période k
        D = new int[nb_total_visite][K];
        C = new int[V + 1];

        // les données // 0 est le depot
        T = new int[][]{
            {0, 85, 349, 17, 203, 289},
            {85, 0, 265, 102, 214, 226},
            {349, 265, 0, 365, 368, 238},
            {17, 102, 366, 0, 207, 302},
            {203, 214, 368, 207, 0, 431},
            {289, 226, 238, 302, 431, 0}
        };

        // produit 0 période 0          
        D[0][0] = 0;
        D[1][0] = 65;
        D[2][0] = 35;
        D[3][0] = 58;
        D[4][0] = 24;
        D[5][0] = 11;
        // produit 0 période 1          
        D[0][1] = 0;
        D[1][1] = 65;
        D[2][1] = 35;
        D[3][1] = 58;
        D[4][1] = 24;
        D[5][1] = 11;
        // produit 0 période 1          
        D[0][2] = 0;
        D[1][2] = 65;
        D[2][2] = 35;
        D[3][2] = 58;
        D[4][2] = 24;
        D[5][2] = 11;

        for (int i = N; i < nb_total_visite; i++) {
            for (int k = 1; k < K; k++) {
                D[i][k] = 0;
            }
        }

        C[0] = 0;
        C[1] = CapaMax;
        C[2] = CapaMax;
//        C[3]=CapaMax;  
        // C[4]=CapaMax;

        Stock_max_client = new int[N];
        Stock_max_client[1] = 195;
        Stock_max_client[2] = 105;
        Stock_max_client[3] = 116;
        Stock_max_client[4] = 72;
        Stock_max_client[5] = 22;

        Stock_min_client = new int[N];
        Stock_min_client[1] = 0;
        Stock_min_client[2] = 0;
        Stock_min_client[3] = 0;
        Stock_min_client[4] = 0;
        Stock_min_client[5] = 0;

        Stock_Initial_client = new int[N];
        Stock_Initial_client[1] = 130;
        Stock_Initial_client[2] = 70;
        Stock_Initial_client[3] = 58;
        Stock_Initial_client[4] = 48;
        Stock_Initial_client[5] = 11;

        Stock_min_Producteur = new int[1];
        Stock_max_Producteur = new int[1];

        // **************** le producteur ******************** //
        Stock_min_Producteur[0] = 0;
        Stock_max_Producteur[0] = 5000;
        Stock_Initial_Producteur = 510;

        Prod = new int[K];

        Prod[0] = 193;
        Prod[1] = 193;
        Prod[2] = 193;

        //******************************************************//
        cout_stockage_producteur = (float) (0.3);
        cout_stockage_client = new float[N];

        cout_stockage_client[1] = (float) (0.23);
        cout_stockage_client[2] = (float) (0.32);
        cout_stockage_client[3] = (float) (0.33);
        cout_stockage_client[4] = (float) (0.23);
        cout_stockage_client[5] = (float) (0.18);

        //******************************************************//
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

    }

    public static void initialiser_sol_depart() {
        // contient des informations sur la solution de depart
        Sv = new int[nb_total_visite][K];
        for (int i = 1; i < N + V; i++) {
            for (int u = 0; u < K; u++) {
                Sv[i][u] = i;
            }
        }
        Sv[6][0] = 1;
        Sv[1][0] = 8;

        Sv[2][0] = 2;
        Sv[3][0] = 3;
        Sv[4][0] = 4;
        Sv[5][0] = 5;
        //
        Sv[7][1] = 3;
        Sv[3][1] = 9;

        Sv[6][1] = 2;
        Sv[2][1] = 4;
        Sv[4][1] = 5;
        Sv[5][1] = 8;

        Sv[1][2] = 1;
        Sv[2][2] = 2;
        Sv[3][2] = 3;
        Sv[4][2] = 4;
        Sv[5][2] = 5;
    }

    public static void resoudre(int distance_max, int duree_max) {
        // déclaration du modèle
        Model mon_modele;
        mon_modele = new Model("IRP en PPC");

        //*****************************************************************************//
        // contrainte 1.1 et 1.2 
        IntVar[][] s;             // le successeur de chaque visite et par période
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
        }

        // Sol de Archetti qui vaut 2366 
        /*
        mon_modele.arithm(s[6][0], "=", 1).post();
        mon_modele.arithm(s[1][0], "=", 8).post();
        
        mon_modele.arithm(s[2][0], "=", 2).post();
        mon_modele.arithm(s[3][0], "=", 3).post();
        mon_modele.arithm(s[4][0], "=", 4).post();
        mon_modele.arithm(s[5][0], "=", 5).post();
        //
        mon_modele.arithm(s[7][1], "=", 3).post();
        mon_modele.arithm(s[3][1], "=", 9).post();
        
        mon_modele.arithm(s[6][1], "=", 2).post();
        mon_modele.arithm(s[2][1], "=", 4).post();
        mon_modele.arithm(s[4][1], "=", 5).post();
        mon_modele.arithm(s[5][1], "=", 8).post();
        
        mon_modele.arithm(s[1][2], "=", 1).post();
        mon_modele.arithm(s[2][2], "=", 2).post();
        mon_modele.arithm(s[3][2], "=", 3).post();
        mon_modele.arithm(s[4][2], "=", 4).post();
        mon_modele.arithm(s[5][2], "=", 5).post();
        
        mon_modele.arithm(g[1][0], "=", 65).post();
        mon_modele.arithm(g[2][1], "=", 35).post();
        mon_modele.arithm(g[3][1], "=", 116).post();
        mon_modele.arithm(g[4][1], "=", 48).post();
        mon_modele.arithm(g[5][1], "=", 22).post();   
         */
        // resultat d'execution
        // Pstock_1=638, Pstock_2=610, Pstock_3=803, 
        // somme_0=65, somme_2=221, somme_3=0, 
        // temps : 0 s   d = 1636
        // Fin
        // *************************************** //        
        mon_modele.setObjective(Model.MINIMIZE, mon_objectif);

        Solver mon_solveur = mon_modele.getSolver();

        solution = new Solution(mon_modele);

        Date heure_debut = new Date();
        long h_debut = heure_debut.getTime();

        mon_solveur.limitTime(duree_max + "s");

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

        mon_solveur.setSearch(
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

        // affichage final si une solution a été trouvée
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

            for (int i = 1; i < N; i++) {
                System.out.println("Client " + i + " : ");
                System.out.print(Stock_Initial_client[i] + " |  ");
                for (int u = 1; u < K; u++) {
                    int vv = Stock[i][u].getLB();
                    int ss = solution.getIntVal(Stock[i][u]);
                    System.out.print(ss + " |  ");
                }
                System.out.println();
                System.out.println("Client " + i + " : ");
                for (int u = 0; u < K; u++) {
                    int ss = solution.getIntVal(g[i][u]);
                    System.out.print(ss + " |  ");
                }
                System.out.println();
                System.out.println();
                System.out.println();

            }

            double[] Cs = solution.getRealBounds(cout_stockage);
            int cout_sol = solution.getIntVal(d_total);
            double[] mon_obj = solution.getRealBounds(mon_objectif);

            //System.out.println(solution.toString());
            //	int cout =(int) mon_solveur.getBestSolutionValue(); 
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
    public static void main(String[] args) {

        demonstration();

        initialiser_sol_depart();

        resoudre(3, 10);

    }

}
