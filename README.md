# PC_Avancee

All samples from the book https://perso.isima.fr/~phlacomm/PPC_Avancee/ (GPL2 License), Mavenized and formatted

The classes were renamed as follow: 

               Sample's .rar file
                   |
                   v 
               /--------------------\
    chap2_vrp.Choco_VRP_avec_symetries_Vrp_3
     \-----/                            \-/
       ^                                 ^
       |                                 |
    Chapter                        Original class name


## How to run the Samples
This is a sample mvn project, so the command line is e.g.
 
    mvn clean package exec:java -Dexec.mainClass="chap2_vrp.Choco_VRP_avec_symetries_Vrp_3"

Also, most IDEs (Eclipse,Netbean,Idea,...) are able to open a maven project
