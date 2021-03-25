/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chap5_vrp;

import java.util.Date;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainLast;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.selectors.variables.DomOverWDeg;
import org.chocosolver.solver.search.strategy.selectors.variables.MaxRegret;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Task;
import org.chocosolver.util.ESat;

/**
 *
 * @author gondran
 */
public class Vrp_3_Vrp_3 {

       public static void main(String[] args) {
         
         int CapaMax=10;  
           
        // les paramétres
        int N=10; // nombre de customers dont le dépot fictif qui est le customer 0
        int V=4;  // nombre de véhicules
        
        
        // le sommet 0 est le dépot
        // Astuce : pour chaque véhicule, on créé 2 dépots : le dépot de départ et le dépot d'arrivée
        //          Et on supprime le noeud 0
        int nb_total_visite=N+V*2;
        
        
	// les données
	int H=100; // borne sup. de la distance
	int[][] T; // matrice des distances
        int[] D; 
        D= new int[nb_total_visite];
        int[] C; 
        C= new int[V+1];
        
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
        
       
        D[0]=0; D[1]=1; D[2]=3; D[3]=5; D[4]=1;
        D[5]=4; D[6]=4; D[7]=3; D[8]=4; D[9]=3;
        for (int i=N; i<nb_total_visite; i++){
            D[i]=0; 
        }
                
        C[0]=0;  
        C[1]=CapaMax;  
        C[2]=CapaMax;  
        C[3]=CapaMax;  
        C[4]=CapaMax;
        
        // les données liées aux time windows min
        int[] TW_Min; 
        TW_Min= new int[nb_total_visite];
        TW_Min[0]=0; 
        TW_Min[1]=5; TW_Min[2]=10; TW_Min[3]=20; TW_Min[4]=15;
        TW_Min[5]=10; TW_Min[6]=10; TW_Min[7]=4; TW_Min[8]=5; TW_Min[9]=10;
        for (int i=N; i<nb_total_visite; i++){
            TW_Min[i]=0; 
        }
        
        // les time windows max des visites
        int[] TW_Max; 
        TW_Max= new int[nb_total_visite];
        TW_Max[0]=999; 
        TW_Max[1]=55; TW_Max[2]=85; TW_Max[3]=30; TW_Max[4]=20;
        TW_Max[5]=20; TW_Max[6]=75; TW_Max[7]=90; TW_Max[8]=26; TW_Max[9]=80;
        for (int i=N; i<nb_total_visite; i++){
            TW_Max[i]=999; 
        }    
        
        
        // les durees de traitement des visites
        int[] Pt; 
        Pt= new int[nb_total_visite];
        Pt[0]=0; 
        Pt[1]=5; Pt[2]=5; Pt[3]=5; Pt[4]=5;
        Pt[5]=5; Pt[6]=5; Pt[7]=5; Pt[8]=5; Pt[9]=5;
        for (int i=N; i<nb_total_visite; i++){
            Pt[i]=0; 
        } 
        
        // fenetre des vehicules
        int TW_Min_vehicule = 1; 
        int TW_Max_vehicule = 100; 
        
        // les donénes liées aux contraintes de coordination
        int nb_C1 = 3;
        int [] Tab_1 = new int[3];
        Tab_1[0]=6;
        Tab_1[1]=3;
        Tab_1[2]=8;
        
        // les donénes liées aux contraintes de coordination
        int nb_C2 = 3;
        int [] Tab_2 = new int[3];
        Tab_2[0]=9;
        Tab_2[1]=1;
        Tab_2[2]=7;
        
        int nb_C3 = 2;
        int [] Tab_3 = new int[2];
        Tab_3[0]=4;
        Tab_3[1]=3;

            
        
        int[][] T_prime;
        T_prime = new int[nb_total_visite][nb_total_visite]; 
        
        // agrandissement de T
        // ajout en fin de colonnes
        for (int i=0; i<N; i++){
           for (int j=0; j<N; j++){
                T_prime[i][j]=T[i][j];
            }
            for (int j=0; j<V*2; j++){
                int position=N+j;
                T_prime[i][position]=T[i][0];
            }
        }
       
        // ajout des lignes finales
        for (int i=N; i<nb_total_visite; i++){
            for (int j=0; j<V*2; j++){
                int position=N+j;
                T_prime[i][position]=0;
            }
            for (int j=0; j<N; j++){
                T_prime[i][j]=T[0][j];
            }
        }
        
        
        // déclaration du modèle
	Model mon_modele;
	mon_modele = new Model("VRP et Synch en PPC");
              
        // Déclaration des variables
        IntVar[] s;             // le successeur de chaque visite
	s = new IntVar[nb_total_visite];
        IntVar[] a;             // la route (ou le véhicule) qui est affecté à la visite
        a=new IntVar[nb_total_visite];
        IntVar[] p;             // rang de la visite dans le trip, similaire au TSP
        p = new IntVar[nb_total_visite];
        

        
        // contraintes 1 et 2 : définition de s

        s[0] = mon_modele.intVar("s_0", 0);     // le noeud 0 ne compte pas
	for (int i=1; i<nb_total_visite; i++){
            if (i<N+V)
            {                             // concerne les clients et les dépots de départ de tous les véhicules
                s[i] = mon_modele.intVar("s_"+i, 1,nb_total_visite);
            }
            else{ // pour les dépots de retour
                s[i] = mon_modele.intVar("s_"+i, i-V);  
            } 
	}
       
        // contrainte 3 : définition de l'affectation des routes
        a[0] = mon_modele.intVar("a_0", 0); // sommet 0 est fictif
        for (int i=1; i<N; i++){
            a[i] = mon_modele.intVar("a_"+i, 1,V); 
	}
        
       // contrainte 4 : cas des dépots de départ et d'arrivée        
        for (int i=1; i<=V; i++){
            int position=N+i-1; // dépot de départ du véhicule i
            a[position] = mon_modele.intVar("a_"+position, i);
            position=N+i+V-1; // dépot d'arrivé du véhicule i
            a[position] = mon_modele.intVar("a_"+position, i);
        }
        
        // contrainte 5 : définition des rangs
        p[0] = mon_modele.intVar("p_0", 0); // sommet 0 fictif      
        for (int i=1; i<N; i++){
            p[i] = mon_modele.intVar("rang_"+i, 1,N);
        }
	for (int i=N+V; i<nb_total_visite; i++){
            p[i] = mon_modele.intVar("rang_"+i, 1,N);
        }        
        
        // contrainte 6 :  les dépot de départ sont toujours au rang 0 !
        for (int i=0; i<V; i++){
            int position=N+i;
            p[position] = mon_modele.intVar("rang_"+position, 0); 
        }
        
              
        // contrainte 7 :   tous les successeurs doivent être différent
        //                  contrainte allDifferent, mais ne prend pas en compte les variables valent 0
        mon_modele.allDifferentExcept0(s).post();
                        
        // contrainte 8 : a[i]=a[s[i]]
        for (int i=1; i<N+V; i++){
            mon_modele.element(a[i], a, s[i],0).post();
        }
                                  
        //contrainte 9 : evite les sous-tours de taille 1 (optionel)
         for (int i=1;i<N+V;i++){
            mon_modele.arithm(s[i], "!=", i).post();
        }
         
         //contrainte 10 : evite les sous tours
        for (int i=1;i<N+V;i++){
            IntVar t=mon_modele.intVar("t_"+i,0,nb_total_visite); 
            mon_modele.arithm(t, "=", p[i], "+",1).post();
            mon_modele.element(t, p, s[i], 0).post();
        }
        
        // contrainte 11 : la somme des Ds des clients ne doit pas dépasser la capacité du véhicule
        SetVar[] b;
        b = new SetVar[V+1];
        int[] x_UB = new int[nb_total_visite];   // valeurs qui peuvent appartenir à l’ensemble
        for (int i=1; i<nb_total_visite; i++) {
            x_UB[i]=i;
        }
        int[] x_LB = new int[]{};  // valeurs qui doivent appartenir à l’ensemble
        
        for (int i=0; i<=V; i++){
            b[i]=mon_modele.setVar("sets_"+i, x_LB, x_UB);
        }
        
        // contrainte 12
        mon_modele.setsIntsChanneling(b, a).post();  
        for (int i=1; i<=V; i++){
            IntVar sum=mon_modele.intVar("Sum_b_"+i,0,C[i]);    
            mon_modele.sumElements(b[i], D, sum).post();  
        }
       
        
        // contrainte 13 : calcul de la distance pour la fonction objectif : identique au TSP
        IntVar[] dp; // distance entre i et j
	IntVar d; // objectif
        dp = new IntVar[nb_total_visite];
        dp[0] = mon_modele.intVar(0);
        for (int i=1; i<N+V; i++){
          dp[i] = mon_modele.intVar("dp_"+i, 0,H);
        }
         for (int i=N+V; i<nb_total_visite; i++){
          dp[i] = mon_modele.intVar("dp_"+i, 0);
        }

        for (int i=1;i<N+V;i++){
            mon_modele.element(dp[i], T_prime[i], s[i]).post();
	}
        d = mon_modele.intVar("d", 0, H);
        mon_modele.sum(dp, "=", d).post();       
        // fonction objective
	mon_modele.setObjective(Model.MINIMIZE, d);
        
     
        // contrainte 14 : casser les symetries
        for (int i=N;i<N+V-1;i++){
            mon_modele.arithm(s[i], "<", s[i+1]).post();
        }
                    
	Solver mon_solveur= mon_modele.getSolver();


        // contrainte 20 : ajout de la variable de cumul de capacite : CAPASUM
        IntVar[] capaSumSucc = mon_modele.intVarArray("capaSumSucc", nb_total_visite, 0, CapaMax);
        IntVar[] capaSum = mon_modele.intVarArray("capaSum", nb_total_visite, 0, CapaMax);
        mon_modele.arithm(capaSum[0],"=",D[0]).post();
        for (int i=1;i<N+V;i++){
                mon_modele.element(capaSumSucc[i],capaSum,s[i],0).post();
                mon_modele.arithm(capaSum[i], "-", capaSumSucc[i], "=", D[i]).post();  
        }
        for (int i=N+V;i<nb_total_visite;i++){
                mon_modele.arithm(capaSum[i],"=",D[i]).post();
        }

        // contrainte 21
        // déclaration de DPS
        // dps = 100 * dp + s (usefull for enumeration => min dist, min index succ in one step)
        // based on T_second
        int[][] T_second = new int[nb_total_visite][nb_total_visite];
        for (int i=0; i<nb_total_visite; i++){
                for (int j=0; j<nb_total_visite; j++){
                        T_second[i][j]=T_prime[i][j]*100+j;
                }
        }

        
        // contrainte 22 - 23
        IntVar[] dps = mon_modele.intVarArray("dps",nb_total_visite, 0, 100*H+nb_total_visite,false);
        for (int i=1;i<nb_total_visite;i++){
                mon_modele.element(dps[i], T_second[i], s[i]).post();
        }
        mon_modele.arithm(dps[0], "=", 0).post();
      
        
        // contrainte 24.1 ... 24.5
        int Dmax = 999;
        // date de debut de service
        IntVar[] st;             
	st = new IntVar[nb_total_visite];
        st[0] = mon_modele.intVar("st_0", 0);     
        // date de depart
        IntVar[] dd;             
	dd = new IntVar[nb_total_visite];
        dd[0] = mon_modele.intVar("dd_0", 0);           
        // date de depart
        IntVar[] da;             
	da = new IntVar[nb_total_visite];
        da[0] = mon_modele.intVar("da_0", 0);  
        // date de fin
        IntVar[] df;             
	df = new IntVar[nb_total_visite];
        df[0] = mon_modele.intVar("df_0", 0); 
        
       
	for (int i=1; i<nb_total_visite; i++){          
            if (i<N)
            {   // concerne les clients et les dépots de départ de tous les véhicules
                st[i] = mon_modele.intVar("st_"+i,  TW_Min[i],TW_Max[i]);
                dd[i] = mon_modele.intVar("dd_"+i,  TW_Min[i],Dmax);
                da[i] = mon_modele.intVar("da_"+i,  0,TW_Max[i]);
                df[i] = mon_modele.intVar("df_"+i,  0,Dmax);
            }
            else {
                if (i<N+V)
                {
                    // concerne les clients et les dépots de départ de tous les véhicules
                    st[i] = mon_modele.intVar("st_"+i,  TW_Min_vehicule,TW_Min_vehicule);
                    dd[i] = mon_modele.intVar("dd_"+i,  TW_Min_vehicule,TW_Min_vehicule);
                    da[i] = mon_modele.intVar("da_"+i,  TW_Min_vehicule,TW_Min_vehicule);
                    df[i] = mon_modele.intVar("df_"+i,  TW_Min_vehicule,TW_Min_vehicule);
                }
                else{ // pour les dépots de retour
                    st[i] = mon_modele.intVar("st_"+i, 0,Dmax);
                    dd[i] = mon_modele.intVar("dd_"+i, Dmax);  
                    da[i] = mon_modele.intVar("da_"+i,  0,TW_Max_vehicule);   
                    df[i] = mon_modele.intVar("df_"+i,  0,Dmax);
                } 
            }
        }
               
        // contrainte 24.6 ... 24.10
        mon_modele.arithm(st[0], "=", 0).post();
	for (int i=1; i<nb_total_visite; i++){
            if (i<N+V)
            {                             // concerne les clients et les dépots de départ de tous les véhicules
                // contrainte 24.6.1
                IntVar d1 = mon_modele.intVar("d1_"+i, 0, Dmax);             
                mon_modele.element(d1, da, s[i], 0).post();
                // contrainte 24.6.2
                mon_modele.arithm(dd[i],"+",dp[i],"=", d1).post();
                // containre 24.7. et 24.8
                mon_modele.arithm(st[i],">=",TW_Min[i]).post();
                mon_modele.arithm(st[i],">=",da[i]).post();
                // contraine 24.9
                mon_modele.arithm(df[i],"=",st[i],"+",Pt[i]).post();
                // contrainte 24.10
                mon_modele.arithm(df[i],"=",dd[i]).post();
                
             }
            else
            {
              mon_modele.arithm(st[i],"=",da[i]).post();
              mon_modele.arithm(df[i],"=",st[i]).post();
            }
        }
        
        // contrainte 25
       IntVar lim_cumulative = mon_modele.intVar("Lim_cumulative", 1);
        Task[] tasks = new Task[nb_C1];
        IntVar[] heights = mon_modele.intVarArray("Heights", nb_C1, 1, 1);
        for (int i = 0; i < nb_C1; i++) {
           int j = Tab_1[i];
           System.out.println(st[j]+"  "+Pt[j]);
           tasks[i] = mon_modele.taskVar(st[j], Pt[j]);
        }
        mon_modele.cumulative(tasks, heights, lim_cumulative).post();

        // contrainte 26     
        for (int i=0; i<nb_C2-1; i++)
        {
            int ni = Tab_2[i];
            for (int j=0; j<nb_C2; j++)
            {
              int nj = Tab_2[j];  
              mon_modele.arithm(st[ni],"=",st[nj]).post();  
            }
        }

        for (int i=0; i<nb_C3-1; i++)
        {
            int ni = Tab_3[i];
            for (int j=0; j<nb_C3; j++)
            {
              int nj = Tab_3[j];  
              mon_modele.arithm(st[ni],"=",st[nj]).post();  
            }
        }          
        
        
       
       // résolution
       mon_solveur.showStatistics();

       mon_solveur.setSearch( 
               Search.intVarSearch( 
                    new MaxRegret(),     // selecteur de variable
                    new IntDomainMin(),  // choix de la valeur
                    dps)                // variable de branchement
               ,
               Search.intVarSearch( 
                    new MaxRegret(),     // selecteur de variable
                    new IntDomainMin(),  // choix de la valeur
                    st)                // variable de branchement               
	); 
        
 

       	Solution solution = new Solution(mon_modele);
                
        Date heure_debut= new Date();
	long h_debut = heure_debut.getTime();
       

        while (mon_solveur.solve()){
            solution.record(); 
            Date heure_fin = new Date();
            long h_fin= heure_fin.getTime();
            long duree = h_fin-h_debut;
            long duree_s = duree/1000;
            System.out.println("temps : " + duree_s + " s    " + d.toString());
            System.out.println("----------");
             
	}
         // affichage final si une solution a été trouvée
	if (mon_solveur.isFeasible() == ESat.TRUE){
            
              
            for (int i=0; i<nb_total_visite; i++)
            {
                int suc = solution.getIntVal(s[i]);
                int affec = solution.getIntVal(a[i]);
                int ran = solution.getIntVal(p[i]);
                int dps_sol = solution.getIntVal(dps[i]);
                int dp_sol = solution.getIntVal(dp[i]);
                
                int da_sol = solution.getIntVal(da[i]);
                int st_sol = solution.getIntVal(st[i]);
                int df_sol = solution.getIntVal(df[i]);
                int dd_sol = solution.getIntVal(dd[i]);
                
                System.out.print("Sommet "+ i + "    Succ : "+ suc+  "    |   affec "+ affec+  "    |   rang ");
                System.out.print(ran+"    |   DPS "+ dps_sol+ "   | dp = " + dp_sol+"   **  da = " + da_sol);
                System.out.println("  |  st = "+st_sol+"  |  df_sol =   "+df_sol+"  |  ddsol = "+dd_sol);
            }
		int cout =(int) mon_solveur.getBestSolutionValue(); 
		Date heure_fin = new Date();
                long h_fin= heure_fin.getTime();
                long duree = h_fin-h_debut;
                long duree_s = duree/1000;

            System.out.println("temps : " + duree_s + " s   d = " + cout);
                System.out.println("Fin");       
	}
	else
		System.out.println("pas de solution");
}
    
}
