/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chap5_vrp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.StreamTokenizer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.chocosolver.util.ESat;
import static chap5_vrp.Vrp_5_avec_TG_Vrp_5.*;


/**
 *
 * @author magondra
 */
public class Vrp_5_avec_TG_Lecture_fichier {

    public static void donnees_de_base() {
        CapaMax = 10;
        // les paramétres
        N = 10; // nombre de customers dont le dépot fictif qui est le customer 0
        V = 4;  // nombre de véhicules         
        nb_total_visite = N + V * 2;

        H = 100; // borne sup. de la distance
        D = new int[nb_total_visite];
        C = new int[V + 1];

        // les données
        T = new int[][]{
            {0, 1, 3, 5, 1, 4, 4, 3, 4, 3},
            {1, 0, 4, 4, 3, 4, 3, 1, 2, 1},
            {3, 4, 0, 4, 1, 1, 3, 1, 1, 1},
            {5, 4, 4, 0, 3, 4, 9, 3, 1, 2},
            {1, 3, 1, 3, 0, 2, 2, 1, 3, 5},
            {4, 4, 1, 4, 2, 0, 4, 5, 1, 2},
            {4, 3, 3, 9, 2, 4, 0, 2, 1, 1},
            {3, 1, 1, 3, 1, 5, 2, 0, 2, 1},
            {4, 2, 1, 1, 3, 1, 1, 2, 0, 1},
            {3, 1, 1, 2, 4, 2, 1, 1, 1, 0}
        };

        D[0] = 0;
        D[1] = 1;
        D[2] = 3;
        D[3] = 5;
        D[4] = 1;
        D[5] = 4;
        D[6] = 4;
        D[7] = 3;
        D[8] = 4;
        D[9] = 3;
        for (int i = N; i < nb_total_visite; i++) {
            D[i] = 0;
        }

        C[0] = 0;
        C[1] = CapaMax;
        C[2] = CapaMax;
        C[3] = CapaMax;
        C[4] = CapaMax;

        // les données liées aux time windows min
        TW_Min = new int[nb_total_visite];
        TW_Min[0] = 0;
        TW_Min[1] = 5;
        TW_Min[2] = 10;
        TW_Min[3] = 20;
        TW_Min[4] = 15;
        TW_Min[5] = 10;
        TW_Min[6] = 10;
        TW_Min[7] = 4;
        TW_Min[8] = 5;
        TW_Min[9] = 10;
        for (int i = N; i < nb_total_visite; i++) {
            TW_Min[i] = 0;
        }

        // les time windows max des visites
        TW_Max = new int[nb_total_visite];
        TW_Max[0] = 999;
        TW_Max[1] = 55;
        TW_Max[2] = 85;
        TW_Max[3] = 30;
        TW_Max[4] = 20;
        TW_Max[5] = 20;
        TW_Max[6] = 75;
        TW_Max[7] = 90;
        TW_Max[8] = 26;
        TW_Max[9] = 80;
        for (int i = N; i < nb_total_visite; i++) {
            TW_Max[i] = 999;
        }

        Pt = new int[nb_total_visite];
        Pt[0] = 0;
        Pt[1] = 5;
        Pt[2] = 5;
        Pt[3] = 5;
        Pt[4] = 5;
        Pt[5] = 5;
        Pt[6] = 5;
        Pt[7] = 5;
        Pt[8] = 5;
        Pt[9] = 5;
        for (int i = N; i < nb_total_visite; i++) {
            Pt[i] = 0;
        }

        // fenetre des vehicules
        TW_Min_vehicule = 1;
        TW_Max_vehicule = 100;

        // les donénes liées aux contraintes de coordination : disjonction
        nb_C1 = 3;
        Tab_1 = new int[3];
        Tab_1[0] = 6;
        Tab_1[1] = 3;
        Tab_1[2] = 8;

        // les donénes liées aux contraintes de coordination
        nb_C2 = 3;
        Tab_2 = new int[3];
        Tab_2[0] = 9;
        Tab_2[1] = 1;
        Tab_2[2] = 7;

        nb_C3 = 2;
        Tab_3 = new int[2];
        Tab_3[0] = 4;
        Tab_3[1] = 3;

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

        // solution initiale //
        // ***************** //
        MySolution_S = new int[]{0, 15, 5, 1, 16, 14, 4, 2, 3, 6, 7, 8, 9, 17, 10, 11, 12, 13};
        MySolution_a = new int[]{0, 2, 1, 2, 3, 1, 3, 1, 2, 3, 1, 2, 3, 4, 1, 2, 3, 4};
        MySolution_p = new int[]{0, 3, 2, 2, 3, 3, 2, 1, 1, 1, 0, 0, 0, 0, 4, 4, 4, 1};
        MySolution_dps = new int[]{0, 115, 105, 401, 116, 414, 204, 102, 103, 106, 307, 408, 309, 17, 10, 11, 12, 13};

    }

    public static void lire_fichier(String nom_fichier_data, String nom_fichier_tournee) {
        // lecture du fichier de données
        try {
            File source = null;
            source = new File(nom_fichier_data);
            FileReader Input = new FileReader(source);
            StreamTokenizer Lecteur;
            Lecteur = new StreamTokenizer(Input);
            Lecteur.parseNumbers();
            int nombre;

            // nb de véhicules
            Lecteur.nextToken();
            if (Lecteur.ttype == Lecteur.TT_NUMBER) {
                V = (int) Lecteur.nval;
//                System.out.println("Nombre lu = "+N);
            } else {
                System.out.println("pas de nombre entier a  lire");
            }

            // nb de visites
            Lecteur.nextToken();
            if (Lecteur.ttype == Lecteur.TT_NUMBER) {
                N = (int) Lecteur.nval;
                N++;
//                System.out.println("Nombre lu = "+N);
            } else {
                System.out.println("pas de nombre entier a  lire");
            }

            // capacite max
            Lecteur.nextToken();
            if (Lecteur.ttype == Lecteur.TT_NUMBER) {
                CapaMax = (int) Lecteur.nval;
//                System.out.println("Nombre lu = "+CapaMax);
            } else {
                System.out.println("pas de nombre entier a lire");
            }

            // nb total noeuds à ordonnancer
            nb_total_visite = N + V * 2;
            D = new int[nb_total_visite];
            C = new int[V + 1];

            // capa camions
            C[0] = 0;
            for (int i = 1; i <= V; i++) {
                C[i] = CapaMax;
            }

            // lecture des processing time
            Pt = new int[nb_total_visite];
            Pt[0] = 0;
            for (int i = 1; i < N; i++) {
                Lecteur.nextToken();
                int noeud = (int) Lecteur.nval;

                Lecteur.nextToken();
                int val = (int) Lecteur.nval;

                Pt[noeud] = val;
            }
            for (int i = N; i < nb_total_visite; i++) {
                Pt[i] = 0;
            }

            // fenetre des camions
            Lecteur.nextToken();
            int n1 = (int) Lecteur.nval;
            Lecteur.nextToken();
            int n2 = (int) Lecteur.nval;
            TW_Min_vehicule = n1;
            TW_Max_vehicule = n2;

            // les TW clients
            TW_Min = new int[nb_total_visite];
            TW_Min[0] = 0;
            TW_Max = new int[nb_total_visite];
            TW_Max[0] = 999;

            for (int i = 1; i < N; i++) {
                Lecteur.nextToken();
                int v_min = (int) Lecteur.nval;

                Lecteur.nextToken();
                int v_max = (int) Lecteur.nval;

                TW_Min[i] = v_min;
                TW_Max[i] = v_max;
            }

            for (int i = N; i < nb_total_visite; i++) {
                TW_Min[i] = 0;
            }
            for (int i = N; i < nb_total_visite; i++) {
                TW_Max[i] = 999;
            }

            // les demandes
            D[0] = 0;
            for (int i = 1; i < N; i++) {
                Lecteur.nextToken();
                int noeud = (int) Lecteur.nval;
                Lecteur.nextToken();
                int d = (int) Lecteur.nval;
                D[noeud] = d;
            }
            for (int i = N; i < nb_total_visite; i++) {
                D[i] = 0;
            }

            // les distances
            T = new int[N + 1][N + 1];
            for (int i = 1; i < N; i++) {
                for (int j = 1; j < N; j++) {
                    Lecteur.nextToken();
                    int val = (int) Lecteur.nval;
                    T[i][j] = val;
//                    System.out.println(i+" "+j+" val= "+val);                
                }
            }
            for (int i = 1; i < N; i++) {
                Lecteur.nextToken();
                int c = (int) Lecteur.nval;

                Lecteur.nextToken();
                int val = (int) Lecteur.nval;

                T[0][c] = val;
                T[c][0] = val;
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

            // nb contrainte de synchro
            Lecteur.nextToken();
            int val = (int) Lecteur.nval;

            nb_c_de_synchro = val;
            //            

            Lecteur.nextToken();
            int noeud1 = (int) Lecteur.nval;
            Lecteur.nextToken();
            int noeud2 = (int) Lecteur.nval;

            nb_C2 = 2;
            Tab_2 = new int[2];
            Tab_2[0] = noeud1;
            Tab_2[1] = noeud2;

            //    
            if (val >= 2) {
                Lecteur.nextToken();
                int noeud11 = (int) Lecteur.nval;
                Lecteur.nextToken();
                int noeud22 = (int) Lecteur.nval;

                nb_C3 = 2;
                Tab_3 = new int[2];
                Tab_3[0] = noeud11;
                Tab_3[1] = noeud22;
            }

            //    
            if (val >= 3) {
                Lecteur.nextToken();
                int noeud11 = (int) Lecteur.nval;
                Lecteur.nextToken();
                int noeud22 = (int) Lecteur.nval;

                nb_C4 = 2;
                Tab_4 = new int[2];
                Tab_4[0] = noeud11;
                Tab_4[1] = noeud22;
            }

            //    
            if (val >= 4) {
                Lecteur.nextToken();
                int noeud11 = (int) Lecteur.nval;
                Lecteur.nextToken();
                int noeud22 = (int) Lecteur.nval;

                nb_C5 = 2;
                Tab_5 = new int[2];
                Tab_5[0] = noeud11;
                Tab_5[1] = noeud22;
            }

            //    
            if (val >= 5) {
                Lecteur.nextToken();
                int noeud11 = (int) Lecteur.nval;
                Lecteur.nextToken();
                int noeud22 = (int) Lecteur.nval;

                nb_C6 = 2;
                Tab_6 = new int[2];
                Tab_6[0] = noeud11;
                Tab_6[1] = noeud22;
            }

            if (val >= 6) {
                Lecteur.nextToken();
                int noeud11 = (int) Lecteur.nval;
                Lecteur.nextToken();
                int noeud22 = (int) Lecteur.nval;

                nb_C7 = 2;
                Tab_7 = new int[2];
                Tab_7[0] = noeud11;
                Tab_7[1] = noeud22;
            }    
            
            if (val >= 7) {
                Lecteur.nextToken();
                int noeud11 = (int) Lecteur.nval;
                Lecteur.nextToken();
                int noeud22 = (int) Lecteur.nval;

                nb_C8 = 2;
                Tab_8 = new int[2];
                Tab_8[0] = noeud11;
                Tab_8[1] = noeud22;
            }            
            
            
            Input.close();
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        // lecture fichier numéro 2 : les tournées
        
        
        Tour_Geant = new int [nb_total_visite+1];
        int position = 1;
        
        try {
            File source = null;
            source = new File(nom_fichier_tournee);
            FileReader Input = new FileReader(source);
            StreamTokenizer Lecteur;
            Lecteur = new StreamTokenizer(Input);
            Lecteur.parseNumbers();
            int nombre;

            Affectation_initiale = new int[nb_total_visite + 1];
            Suivant_initiale = new int[nb_total_visite + 1];

            Lecteur.nextToken();
            int NBT = (int) Lecteur.nval;

            Lecteur.nextToken();
            int poubelle = (int) Lecteur.nval;

            for (int i = 1; i <= NBT; i++) {
            //    System.out.println("\n\n ligne numero "+ i);
          //      System.out.print("liste : ");
                
                Lecteur.nextToken();
                int numero_t = (int) Lecteur.nval;

                Lecteur.nextToken();
                int numero_e = (int) Lecteur.nval;

                if (numero_e==0)
                {
                 
                }
                else
                {
                    int prec = -1;
                    for (int j = 1; j <= numero_e; j++) {

                        Lecteur.nextToken();
                        int sommet = (int) Lecteur.nval;

                        // remplissage du tour geant
                        Tour_Geant[position] = sommet;
                        position = position + 1;


    //                    System.out.print("  " + j + "/"+sommet);
                        Affectation_initiale[sommet] = i;
                        if (prec != -1) {
                            Suivant_initiale[prec] = sommet;
                        } else {
                            Suivant_initiale[N + i - 1] = sommet;
                        }
                        prec = sommet;
                    }
                    Suivant_initiale[prec] = N + V + i - 1;
                }
            }
            Input.close();
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        // lecture du fichier des tournees   
    }

    static void melangerTableau(int t[], int nb_max) {
        int p = Math.abs(generator.nextInt());
        int pp = p % 10 + 1;
        pp = 2;
        for (int i = 1; i <= pp; i++) {
            double x = generator.nextFloat();
            double y = x * (nb_max-1);
            int z = (int) (1 + y);
                    
           
            double x2 = generator.nextFloat();
            double y2 = x2 * (nb_max-1);
            int z2 = (int) (1 + y2);
            

            int tmp = t[z2];
            t[z2] = t[z];
            t[z] = tmp;
        }
    }

    
    static void initialiser_fichier_sortie(String nom_fichier)
    {
         try {
            File destination;
            destination = new File(nom_fichier);
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


            ecrivain.write(" Instance; S; T_to_best; TT;");
            ecrivain.write("\n");

            ecrivain.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        };
        
    }
    
    
    static void ecrire_resultat_ds_fichier(String nom_fichier, int num_instance, char lettre_bredstrom,
            int cout_objectif, float temps_to_best, float temps_total)
    {
         try {
            File destination;
            destination = new File(nom_fichier);
            FileOutputStream Ouput = new FileOutputStream(destination, true);
            PrintWriter ecrivain;
            ecrivain = new PrintWriter(Ouput);

            String num=Integer.toString(num_instance);
                    
            ecrivain.write(num);
            ecrivain.write(lettre_bredstrom + ";");
       

            ecrivain.write(cout_objectif+ ";");
            ecrivain.write(temps_to_best + ";");
            ecrivain.write(temps_total + ";");
            
            ecrivain.write("\n");

            ecrivain.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        };
    }
    
    
    
    
}
