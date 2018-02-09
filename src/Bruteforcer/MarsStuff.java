package Bruteforcer;

import bc.*;

import java.util.ArrayList;
import java.util.Stack;

public class MarsStuff {

    public static OrbitPattern op;
    public static AsteroidPattern ap;


    public static ArrayList<Loc>[] rocketslandingon = new ArrayList[750];

    public static ArrayList<Loc> passableMarsLocs = new ArrayList<>();

    public static int[] sectionSizes = new int[725];
    public  static int sectionCount = 0;

    public static int[][] sectionMap;


    public static int getArrivalTimeIfLaunchAt(int time){
        return (int)op.duration(time);
    }



    public static void avoidRocketLandings(){




        VecRocketLanding thisTurn = Player.gc.rocketLandings().landingsOn(R.turn);
        VecRocketLanding nextTurn = Player.gc.rocketLandings().landingsOn(R.turn + 1);


        for(long i = 0 ; i < thisTurn.size(); i++){
            MapLocation loc = thisTurn.get(i).getDestination();
            int x  = loc.getX();
            int y  = loc.getY();
//            Debug.log("Rocket landing on : " + x + ", " + y + " this turn");
            Loc maploc = Map.locations[x][y];
            Map.globalDesire[x][y] -= 50000;
            for(Loc l : maploc.adjacentPassableTiles()){
                Map.globalDesire[l.x][l.y] -= 10000;
            }
        }
        for(long i = 0 ; i < nextTurn.size(); i++){
            MapLocation loc = nextTurn.get(i).getDestination();
            int x  = loc.getX();
            int y  = loc.getY();
//            Debug.log("Rocket landing on : " + x + ", " + y + " next turn");
            Loc maploc = Map.locations[x][y];
            Map.globalDesire[x][y] -= 50000;
            for(Loc l : maploc.adjacentPassableTiles()){
                Map.globalDesire[l.x][l.y] -= 10000;
            }
        }

    }




    public static void initMarsPassability(PlanetMap marsMap){
        if(R.amIMars){
            return;
        }
        Map.marsBlocked = new boolean[ Map.marsWidth][Map.marsHeight];
        for(int x = 0 ; x < Map.marsWidth; x++){
            for(int y = 0 ; y < Map.marsHeight; y++){
                Map.marsBlocked[x][y] = marsMap.isPassableTerrainAt(new MapLocation(Planet.Mars,x,y)) == 0;
                if(!Map.marsBlocked[x][y]){
                    passableMarsLocs.add(new Loc(x,y));
                }

            }
        }

        sectionMap = new int[Map.marsWidth][Map.marsHeight];


        int section = 0;
        sectionCount = 1;


        for(int x = 0 ; x < Map.marsWidth; x++) {
            for (int y = 0; y < Map.marsHeight; y++) {

                if(sectionMap[x][y] == 0){
                    if(Map.marsBlocked[x][y]){
                        sectionMap[x][y] = -1;
                    }else{
                        section++;
                        sectionCount++;

                        Stack<Loc> stack = new Stack();
                        stack.push(new Loc(x,y));
                        sectionMap[x][y] = section;
                        //dfs to fill the sectionmap
                        while(!stack.empty()) {
                            Loc l = stack.pop();
                            sectionSizes[section]++;
                            for(Loc ml : l.adjacentLocsWithin(0,Map.marsWidth,0,Map.marsHeight)){
                                if(sectionMap[ml.x][ml.y] == 0 && !Map.marsBlocked[ml.x][ml.y]){
                                    sectionMap[ml.x][ml.y] = section;
                                    stack.push(ml);
                                }
                            }
                        }

                    }
                }
            }
        }
//        Debug.log("Mars sections");
//        for(int i = 0 ; i < sectionCount; i++){
//            Debug.log("Section: " + i + " Size: " + sectionSizes[i]);
//        }





    }

}
