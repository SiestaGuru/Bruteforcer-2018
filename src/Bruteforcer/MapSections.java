package Bruteforcer;

import java.util.HashSet;
import java.util.Stack;

public class MapSections {

    //Analysis of continous sections on the map
//    public static ArrayList<MainLoc>[] sectionStarts = new ArrayList[625];


    public static int[] sectionSizes = new int[725];
    public static int largestSectionSize = 0;
    public static int[] sectionPassableNeighbours = new int[725];
    public static int[] sectionSuspectedKarbo = new int[725];

    public static double[] sectionOpenRatio; //0.95+ is fully open.   0.85+ is kinda open.  0.75+ is dense. below is very dense


    public static boolean[] canReachSection = new boolean[725];
    public static boolean[] canEnemyReachSection = new boolean[725];


    public  static int sectionCount = 0;
    public  static int reachableTerrain = 0;

    public static void DoAnalysis(){

        for(int x = 0; x < Map.width; x++){
            for(int y = 0; y < Map.height; y++){
                MainLoc l = Map.locations[x][y];
                if(l.isPassable){
                    if(l.sectionId <0){
//                        sectionStarts[sectionCount] = new ArrayList<>();
                        searchThroughSection(sectionCount, l);
                        sectionCount++;
                    }
                }
            }
        }

        sectionOpenRatio = new double[sectionCount];

        for(int i = 0 ; i < sectionCount; i++){
            sectionOpenRatio[i] =  ((double)sectionPassableNeighbours[i]) / (8.0 * sectionSizes[i]);

            if(sectionSizes[i] > largestSectionSize){
                largestSectionSize = sectionSizes[i];
            }

//            Debug.log("Section: " + i + " Open: " + sectionOpenRatio[i]);
        }


    }



    public static void SetCanReach(){
        for(int i = 0 ; i < sectionCount; i++){
            canReachSection[i] = false;
            canEnemyReachSection[i] = false;
            sectionSuspectedKarbo[i] = 0;
        }
        for(Robot r : Map.myRobots){
            canReachSection[r.loc.sectionId] = true;
        }

        if(R.turn < 250) {
            for (MainLoc l : Map.theirStartingSpots) {
                canEnemyReachSection[l.sectionId] = true;
            }
        }else {
            for (Robot r : Map.theirRobots) {
                canEnemyReachSection[r.loc.sectionId] = true;
            }
        }


        reachableTerrain = 0;
        for(int i = 0 ; i < sectionCount;i++){
            if(canReachSection[i]){
                reachableTerrain += sectionSizes[i];
            }
        }


        MapMeta.sectionsWithBots =  new HashSet<>();
        for(Robot r: Map.myRobots){
            MapMeta.sectionsWithBots.add(r.loc.sectionId);
        }


        for(int x =0; x < Map.width; x++){
            for(int y =0; y < Map.height; y++){
                int section = Map.locations[x][y].sectionId;
                if(MapMeta.sectionsWithBots.contains(section)) {
                    Map.reachable[x][y] = true;

                    sectionSuspectedKarbo[section] += Map.karbonite[x][y];
                    Map.totalSuspectedReachableKarbonite += Map.karbonite[x][y];
                }else{
                    Map.reachable[x][y] = false;
                }
            }
        }
    }


    private static void searchThroughSection(int section, MainLoc start){
        Stack<MainLoc> stack = new Stack();
        stack.push(start);
        start.sectionId = section;
        while(!stack.empty()) {
            MainLoc l = stack.pop();
//            sectionStarts[section].add(l);
            sectionSizes[section]++;
            sectionPassableNeighbours[section] += l.passableNeighboursCount;
            for(MainLoc ml : l.adjacentPassable){
                if(ml.sectionId < 0){
                    ml.sectionId = section;
                    stack.push(ml);
                }
            }
        }
    }


}
