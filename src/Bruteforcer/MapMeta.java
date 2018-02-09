package Bruteforcer;

import java.util.ArrayList;
import java.util.HashSet;

public class MapMeta {

    //High res
    public static int[][] healerSupportArray;
    public static int[][] overchargeSupportArray;
    public static int[][] withinAttackRangeArray;
    public static int[][] myImmediateDangerArray;
    public static double[][] dangerArray; //items go from about 0-2000

    //These are the various hypothetical damage arrays
    //Enemydamagearray is hypothetical damage done either next turn or the turnthereafter
    //The Real array is: what can the enemy actually do next turn
    //The without movement is: what can the enemy actually do without having to move (also allows 2 turns)
    //The restrictive array is: what can the enemy do without having to move robots aside
    //The idea is, never step on a spot where enemyDamageArray exceeds health
    //But if somehow, you can'r avoid it, then if you have the ability to step to a spot that requires something extra (an extra turn to kill you,
    //they need to move, they need to shuffle units around) then that's a decent possible escape plan
    public static double[][] enemyDamageArray;
    public static double[][] enemyDamageArrayPlusGhost;
    public static double[][] enemyDamageArrayReal;
    public static double[][] enemyDamageArrayRealWithoutMovement;
    public static double[][] enemyDamageArrayRealRestrictive;
    public static double[][] enemyDamageArrayRealisticallyInsta;
    public static double[][] enemyDamageArrayRealisticallyNext;

    //Low res (data about 5x5 squares)
    public static double[][] controlMap;
    public static int[][] controlSize;
    public static Loc[][] controlLocations;
    public static boolean[][] controlIsReachable;
    public static boolean[][] controlHasActivity;
    public static int[][] controlLastHasActivity;
    public static int[][] controlLastHadUnitOfMine;
    public static double[][] controlDanger;
    public static double[][] controlRollingActivity;
    public static double[][] controlSuspectedEnemyPresence;
    public static int[][] controlKarbonite;
    public static int[][] controlUnitCounts;
    public static int[][] controlImpassable;
    public static int[][] controlenemyFactories;


    public static int[][] passableTilesAround;


    public static Loc[][][][][] controlZonePaths; //nothing wrong with a good ol' 5 dimensional array. Worst case scenario is a 50x50 map, wherein we'd have 10*10*10*10*pathsize entries. Shouldn't be that terrible


    public static int controlXShift;
    public static int controlYShift;
    public static int controlRows;
    public static int controlColumns;

    public static boolean anyActivityDetected;

    public static HashSet<Integer> sectionsWithBots;



    public static void UpdateDangerMap(){
        Map.inSight = new boolean[Map.width][Map.height];
        Map.visibleTiles.clear();
        dangerArray = new double[Map.width][Map.height];
        enemyDamageArray = new double[Map.width][Map.height];
        enemyDamageArrayReal = new double[Map.width][Map.height];
        enemyDamageArrayPlusGhost = new double[Map.width][Map.height];
        enemyDamageArrayRealWithoutMovement = new double[Map.width][Map.height];
        enemyDamageArrayRealRestrictive = new double[Map.width][Map.height];
        enemyDamageArrayRealisticallyInsta = new double[Map.width][Map.height];
        enemyDamageArrayRealisticallyNext = new double[Map.width][Map.height];
        controlSuspectedEnemyPresence = new double[Map.width][Map.height];
        healerSupportArray = new int[Map.width][Map.height];
        overchargeSupportArray = new int[Map.width][Map.height];
        withinAttackRangeArray = new int[Map.width][Map.height];
        myImmediateDangerArray = new int[Map.width][Map.height];
        controlUnitCounts = new int[Map.width][Map.height];
        controlenemyFactories = new int[Map.width][Map.height];
        controlRollingActivity = new double[Map.width][Map.height];
        Map.totalVisibleKarbonite = 0;
        Map.totalSuspectedReachableKarbonite = 0;
        anyActivityDetected = false;




        for(Robot r : Map.theirGhostImages.values()){
            if(r.amIDps) {
                ArrayList<MainLoc> tiles = r.approxAttackPlusMoveCircle.allContainingLocationsOnTheMap();
                for (MainLoc l : tiles) {
                    enemyDamageArrayPlusGhost[l.x][l.y] += r.damage;
                }
            }
        }


        if(!R.POWERSAVEMODUS) {
            for (Robot r : Map.myRobots) {
                ArrayList<MainLoc> tiles = r.sightCircle.allContainingLocationsOnTheMap();
                // Debug.log( r.type.toString() + " sight: " + tiles.size());
                for (Loc l : tiles) {
                    if (!Map.inSight[l.x][l.y]) {
                        try {
                            if (Player.gc.canSenseLocation(l.toMapLocation())) {
                                Map.inSight[l.x][l.y] = true;
                                Map.visibleTiles.add(l);
                                int karbonite = (int) Player.gc.karboniteAt(l.toMapLocation());
                                Map.karbonite[l.x][l.y] = karbonite;
                                Map.totalVisibleKarbonite += karbonite;
                            }
                        } catch (Exception ex) {
                            //to allow testing without the lib
                        }
                    }
                }
                if (!R.POWERSAVEMODUSHYPER) {
                    if (r.amIDps) {
                        tiles = r.attackCircle.allSpotsAfterMove();
                        for (Loc l : tiles) {
                            withinAttackRangeArray[l.x][l.y]++;
                            myImmediateDangerArray[l.x][l.y]+= r.damage;
                        }
                    } else if (r.amIHealer) {

                        if(r.canMove()){
                            tiles = r.approxAttackPlusMoveCircle.allContainingLocationsOnTheMap();
                            for (Loc l : tiles) {
                                healerSupportArray[l.x][l.y]++;
                                if(Techs.canOverload && r.canUseAbility()) {
                                    overchargeSupportArray[l.x][l.y]++;
                                }
                            }
                        }else{
                            tiles = r.attackCircle.allContainingLocationsOnTheMap();
                            for (Loc l : tiles) {
                                healerSupportArray[l.x][l.y]++;
                                if(Techs.canOverload && r.canUseAbility()) {
                                    overchargeSupportArray[l.x][l.y]++;
                                }
                            }
                        }

                    }
                }
            }
        }

        for(Robot r : Map.theirRobots){
            if(r.amIDps || r.amIHealer) {
//            ArrayList<Loc> tiles = r.attackCircle.allSpotsAfterMove();
                ArrayList<MainLoc> tiles = r.approxAttackPlusMoveCircle.allContainingLocationsOnTheMap();
                double danger = Math.max(0, r.damage);

                if (r.amIRanger) {
                    danger += 30;
                }
                if (r.amIHealer) {
                    danger += 10;

                    for (Loc l : tiles) {
                        dangerArray[l.x][l.y] += danger - l.distanceTo(r.loc) * 0.2;
                    }
                }


                double damage = r.damage;
                if (damage > 0) {
//                    if(r.attackHeat >= 10){
//                        damage *= 0.5;
//                    }
                    for (MainLoc l : tiles) {


                        boolean canBeReachedDirectly = r.attackCircle.contains(l);
                        boolean isActuallyReachable = canBeReachedDirectly;
                        boolean canBeReachedWithoutOtherMoves = canBeReachedDirectly;
                        boolean canBeReachedWithoutSteppingIntoMyUnits = canBeReachedDirectly;

                        if(!isActuallyReachable){
                            for(MainLoc l2 : l.adjacentPassable){
                                if(r.attackCircle.contains(l2)){
                                    isActuallyReachable = true;

                                    if(!l2.containsRobot){
                                        canBeReachedWithoutOtherMoves = true;

                                        if(r.maxHealth > myImmediateDangerArray[l2.x][l2.y]){
                                            canBeReachedWithoutSteppingIntoMyUnits = true;
                                        }
                                    }
                                }
                            }
                        }

                        if(isActuallyReachable) {
                            enemyDamageArray[l.x][l.y] += damage;
                            enemyDamageArrayPlusGhost[l.x][l.y] += damage;

                            dangerArray[l.x][l.y] += danger - l.distanceTo(r.loc) * 0.2;
                            if (canBeReachedDirectly) {
                                if (r.attackHeat < 10) {
                                    enemyDamageArrayReal[l.x][l.y] += damage;
                                    enemyDamageArrayRealisticallyInsta[l.x][l.y] += damage;
                                }
                                else{
                                    enemyDamageArrayRealisticallyNext[l.x][l.y] += damage;
                                }
                                enemyDamageArrayRealWithoutMovement[l.x][l.y] += damage;
                                enemyDamageArrayRealRestrictive[l.x][l.y] += damage;
                            } else if (r.moveHeat < 10) {
                                if (r.attackHeat < 10) {
                                    enemyDamageArrayReal[l.x][l.y] += damage;
                                    if(canBeReachedWithoutOtherMoves && canBeReachedWithoutSteppingIntoMyUnits){
                                        enemyDamageArrayRealisticallyInsta[l.x][l.y] += damage;
                                    }
                                }
                                else{
                                    if(canBeReachedWithoutOtherMoves && canBeReachedWithoutSteppingIntoMyUnits){
                                        enemyDamageArrayRealisticallyNext[l.x][l.y] += damage;
                                    }
                                }
                                if(canBeReachedWithoutOtherMoves){
                                    enemyDamageArrayRealRestrictive[l.x][l.y] += damage;
                                }
                            } else if(r.moveHeat < 20){
                                if(canBeReachedWithoutOtherMoves && canBeReachedWithoutSteppingIntoMyUnits){
                                    enemyDamageArrayRealisticallyNext[l.x][l.y] += damage;
                                }
                            }

                        }

                    }
                }
            }
        }


        if(controlColumns == 0) {
            InitStuff();
        }

        controlMap = new double[controlColumns][controlRows];
        controlDanger = new double[controlColumns][controlRows];
        controlHasActivity = new boolean[controlColumns][controlRows];

        controlKarbonite = new int[controlColumns][controlRows];




        for(int x =0; x < Map.width; x++){
            for(int y =0; y < Map.height; y++){
                if(Map.reachable[x][y]) {
                    Map.totalSuspectedReachableKarbonite += Map.karbonite[x][y];
                }
            }
        }


        for(int x =0; x < Map.width; x++){
            int column = (x - controlXShift) / 5;
            if(column >= 0 && column < controlColumns ) {
                for (int y = 0; y < Map.height; y++) {
                    int row = (y - controlYShift) / 5;
                    if(row >= 0 && row < controlRows ) {

                        if(!R.POWERSAVEMODUSHYPER){ //not collecting enough data in hyper mode
                            controlMap[column][row] += withinAttackRangeArray[x][y] * 4.0 + healerSupportArray[x][y] * 7.0 - (dangerArray[x][y] * 0.05);
                        }

                        if(dangerArray[x][y] > 20){
                            controlHasActivity[column][row] = true;
                        }
                        controlDanger[column][row] += dangerArray[x][y];


                        double rollscore = dangerArray[x][y] * 0.05;


                        Robot r = Map.robots[x][y];
                        if(r!= null){

                            if(r.isMine){
                                controlLastHadUnitOfMine[column][row] = R.turn;
                            }

                            controlUnitCounts[column][row]++;
                            if(r.amIWorker){
                                if(r.isMine){
                                    controlMap[column][row] += 5;
                                }else{
                                    controlMap[column][row] -= 7;
                                    controlHasActivity[column][row] = true;
                                    rollscore += 0.4;
                                }
                            } else if(r.amIFactory){
                                if(r.isMine){
                                    controlMap[column][row] += 5;
                                }else{
                                    controlenemyFactories[column][row]++;
                                    controlMap[column][row] -= 10;
                                    controlHasActivity[column][row] = true;
                                    rollscore += 0.5;
                                }
                            } else if(r.amIRocket){
                                if(r.isMine){
                                    controlMap[column][row] += 1;
                                }else{
                                    controlMap[column][row] -= 5;
                                    controlHasActivity[column][row] = true;
                                    rollscore += 0.5;
                                }
                            }

                            if(R.POWERSAVEMODUSHYPER){
                                //Replacement for standard method to get at least something
                                if(r.isMine){
                                    controlMap[column][row] += 5;
                                }else{
                                    controlMap[column][row] -= 5;
                                }
                            }
                        }

                        controlRollingActivity[column][row] = Math.max(controlRollingActivity[column][row],rollscore);

                        if(Map.reachable[x][y]) {
                            controlKarbonite[column][row] += Map.karbonite[x][y];
                        }
//                        else{
//                            if(R.turn == 100) {
//                                Debug.log("No section?"  + x + "," + y + " section: " + section);
//                            }
//                        }
                    }
                }
            }
        }



        for(int x = 0; x < controlColumns; x++) {
            for (int y = 0; y < controlRows; y++) {
                if(R.turn < 100) {
                    for (Loc l : Map.theirStartingSpots) {
                        if (l.isWithinDistance(controlLocations[x][y], 8)) {
                            controlMap[x][y] -= 100;
                            break;
                        }
                    }
                }
                controlRollingActivity[x][y] *= 0.96;
                controlMap[x][y] /= (double)(controlSize[x][y]);

                if(controlHasActivity[x][y]){
                    anyActivityDetected = true;
                    controlLastHasActivity[x][y] = R.turn;
                }
            }
        }

        for(int x = 0; x < controlColumns; x++) {
            for (int y = 0; y < controlRows; y++) {
                if(R.turn - controlLastHadUnitOfMine[x][y] > 25){
                    double suspicion = 200 +  0.5 * (R.turn - controlLastHadUnitOfMine[x][y]);

                    suspicion +=   Math.min(300,R.turn - controlLastHasActivity[x][y]) * -0.4;


                    if(x < controlColumns - 1) {
                        suspicion += Math.min(300, R.turn - controlLastHasActivity[x + 1][y]) * -0.3;
                        if(y < controlRows - 1) {
                            suspicion += Math.min(300, R.turn - controlLastHasActivity[x + 1][y + 1]) * -0.2;
                        }
                        if(y > 0) {
                            suspicion += Math.min(300, R.turn - controlLastHasActivity[x + 1][y - 1]) * -0.2;
                        }
                    }
                    if(x > 0) {
                        suspicion += Math.min(300, R.turn - controlLastHasActivity[x - 1][y]) * -0.3;
                        if(y < controlRows - 1) {
                            suspicion += Math.min(300, R.turn - controlLastHasActivity[x - 1][y + 1]) * -0.2;
                        }
                        if(y > 0) {
                            suspicion += Math.min(300, R.turn - controlLastHasActivity[x - 1][y - 1]) * -0.2;
                        }
                    }

                    if(y < controlRows - 1) {
                        suspicion += Math.min(300, R.turn - controlLastHasActivity[x][y + 1]) * -0.3;
                    }
                    if(y > 0) {
                        suspicion += Math.min(300, R.turn - controlLastHasActivity[x][y - 1]) * -0.3;
                    }



                    suspicion -= controlMap[x][y] * 0.5;

                    for(Loc l : Map.theirStartingSpots){
                        suspicion -= Math.min(20,controlLocations[x][y].distanceTo(l));
                    }

                    controlSuspectedEnemyPresence[x][y] = suspicion;

                }
            }
        }




    }

    public static Loc getControlLocOf(Loc l){
        int column = (l.x - controlXShift) / 5;
        int row = (l.y - controlYShift) / 5;

        return new Loc(C.Clamp(column,0,controlColumns - 1),C.Clamp(row,0,controlRows - 1));
    }

    public static double getControlOf(Loc l){
        int column = (l.x - controlXShift) / 5;
        int row = (l.y - controlYShift) / 5;

        return controlMap[C.Clamp(column,0,controlColumns - 1)][C.Clamp(row,0,controlRows - 1)];
    }

    private static void InitStuff(){

        passableTilesAround = new int[Map.width][Map.height];
        for(int x = 0; x < Map.width; x++){
            for(int y = 0; y < Map.height; y++){
                passableTilesAround[x][y] = Map.locations[x][y].adjacentPassableTiles().length;
            }
        }


        int modwidth = Map.width % 5;
        int modheight = Map.height % 5;
        controlRows = 0;
        controlColumns = 0;

        switch (modwidth) {
            case 0:
                controlColumns = Map.width / 5;
                controlXShift = 0;
                break;
            case 1:
                controlColumns = (Map.width / 5);
                controlXShift = 0;
                break;
            case 4:
                controlColumns = (Map.width / 5);
                controlXShift = 0;
                break;
            case 2:
                controlColumns = (Map.width / 5) - 1;
                controlXShift = 1;
                break;
            case 3:
                controlColumns = (Map.width / 5) - 1;
                controlXShift = -1;
                break;
        }
        switch (modheight) {
            case 0:
                controlRows = Map.height / 5;
                controlYShift = 0;
                break;
            case 1:
                controlRows = (Map.height / 5);
                controlYShift = 0;
                break;
            case 4:
                controlRows = (Map.height / 5);
                controlYShift = 0;
                break;
            case 2:
                controlRows = (Map.height / 5) - 1;
                controlYShift = 1;
                break;
            case 3:
                controlRows = (Map.height / 5) - 1;
                controlYShift = -1;
                break;
        }

        controlSize = new int[controlColumns][controlRows];
        controlIsReachable = new boolean[controlColumns][controlRows];
        controlMap = new double[controlColumns][controlRows];
        controlLocations = new Loc[controlColumns][controlRows];
        controlImpassable = new int[controlColumns][controlRows];
        controlLastHasActivity = new int[controlColumns][controlRows];
        controlLastHadUnitOfMine = new int[controlColumns][controlRows];

        for(int x =0; x < Map.width; x++){
            int column = (x - controlXShift) / 5;
            if(column >= 0 && column < controlColumns ) {
                for (int y = 0; y < Map.height; y++) {
                    int row = (y - controlYShift) / 5;
                    if(row >= 0 && row < controlRows ) {
                        controlSize[column][row]++;
                        if(Map.blocked[x][y]) {
                            controlImpassable[column][row]++;
                        }
                    }
                }
            }
        }

        for(int x = 0; x < controlColumns; x++){
            for(int y = 0; y < controlRows; y++){
                int centerX =  x * 5 + controlXShift + 2;
                int centerY =  y * 5 + controlYShift + 2;

                //Okay lol, there should be a better way of doing this. Were walking through the adjacent spots to check if also blocked
                //Cause blocked is bad
                if(Map.blocked[centerX][centerY]){
                    centerX++;
                    if(Map.blocked[centerX][centerY]){
                        centerY--;
                        if(Map.blocked[centerX][centerY]){
                            centerX--;
                            if(Map.blocked[centerX][centerY]){
                                centerX--;
                                if(Map.blocked[centerX][centerY]){
                                    centerY++;
                                    if(Map.blocked[centerX][centerY]){
                                        centerY++;
                                        if(Map.blocked[centerX][centerY]){
                                            centerX++;
                                            if(Map.blocked[centerX][centerY]){
                                                centerX++;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Loc controlLoc = new Loc(centerX,centerY);
                controlLocations[x][y] = controlLoc;
                if(Map.blocked[centerX][centerY]){
                    controlIsReachable[x][y] = false;
                }else {
                    if (R.amIMars) {
                        controlIsReachable[x][y] = true; // May need something better. Just assuming now we'll rocket there
                    } else {
                        for (Loc l : Map.myStartingSpots) {
                            if (Map.findPathToNextStep(l,controlLoc) != null){
                                controlIsReachable[x][y] = true;
                                break;
                            }
                        }

                    }
                }
            }
        }


        CalcControlMapPathing();
    }




    public static int getNearKarboniteEstimate(Loc myloc){
        Loc l = getControlLocOf(myloc);

        int xmin1 = l.x - 1;
        int xplus1 = l.x + 1;
        int ymin1 = l.y - 1;
        int yplus1 = l.y + 1;

        int sum = controlKarbonite[l.x][l.y];

        if(ymin1 >= 0){
            sum += controlKarbonite[l.x][ymin1];
        }
        if(yplus1 < controlRows){
            sum += controlKarbonite[l.x][yplus1];
        }


        if(xmin1 >= 0){
            if(ymin1 >= 0){
                sum += controlKarbonite[xmin1][ymin1];
            }
            if(yplus1 < controlRows){
                sum += controlKarbonite[xmin1][yplus1];
            }
            sum+= controlKarbonite[xmin1][l.y];
        }

        if(xplus1 <controlColumns){
            if(ymin1 >= 0){
                sum += controlKarbonite[xplus1][ymin1];
            }
            if(yplus1 < controlRows){
                sum += controlKarbonite[xplus1][yplus1];
            }
            sum+= controlKarbonite[xplus1][l.y];
        }

        return sum;
    }

    public static int getNearKarbo(Loc l, int offsetDistance){
        int karb = 0;
        for(int x = Math.max(0,l.x-offsetDistance);  x <=  Math.min(Map.widthMinusOne,l.x+offsetDistance); x++) {
            for (int y = Math.max(0, l.y - offsetDistance); y <= Math.min(Map.heightMinusOne, l.y + offsetDistance); y++) {
                if(Map.reachable[x][y]) {
                    karb += Map.karbonite[x][y];
                }
            }
        }
        return karb;

    }


    public static double getNearFull(Loc l, int offsetDistance){

        int totalSquares = offsetDistance * offsetDistance;
        int free = 0;

        for(int x = Math.max(0,l.x-offsetDistance);  x <=  Math.min(Map.widthMinusOne,l.x+offsetDistance); x++) {
            for (int y = Math.max(0, l.y - offsetDistance); y <= Math.min(Map.heightMinusOne, l.y + offsetDistance); y++) {
                if(!Map.blocked[x][y] && Map.robots[x][y] == null){
                    free++;
                }
            }
        }
        return 1.0 - (((double)free) / ((double)totalSquares));

    }

    //proportion of tiles blocked (0-1), includes off-map as blocked
    public static double getNearFullEstimate(Loc myloc){
        Loc l = getControlLocOf(myloc);

        int xmin1 = l.x - 1;
        int xplus1 = l.x + 1;
        int ymin1 = l.y - 1;
        int yplus1 = l.y + 1;

        int sum = controlImpassable[l.x][l.y] + controlUnitCounts[l.x][l.y];

        if(ymin1 >= 0){
            sum += controlImpassable[l.x][ymin1] + controlUnitCounts[l.x][ymin1];
        }
        if(yplus1 < controlRows){
            sum += controlImpassable[l.x][yplus1] + controlUnitCounts[l.x][yplus1];
        }


        if(xmin1 >= 0){
            if(ymin1 >= 0){
                sum += controlImpassable[xmin1][ymin1] + controlUnitCounts[xmin1][ymin1];
            }
            if(yplus1 < controlRows){
                sum += controlImpassable[xmin1][yplus1] + controlUnitCounts[xmin1][yplus1];
            }
            sum+= controlImpassable[xmin1][l.y] + controlUnitCounts[xmin1][l.y];
        }

        if(xplus1 < controlColumns){
            if(ymin1 >= 0){
                sum += controlImpassable[xplus1][ymin1] + controlUnitCounts[xplus1][ymin1];
            }
            if(yplus1 < controlRows){
                sum += controlImpassable[xplus1][yplus1] + controlUnitCounts[xplus1][yplus1];
            }
            sum+= controlImpassable[xplus1][l.y] + controlUnitCounts[xplus1][l.y];
        }

        return ((double)sum) / 225.0;
    }



    public static void CalcControlMapPathing(){
        controlZonePaths = new Loc[controlColumns][controlRows][controlColumns][controlRows][];
        for(int column1 = 0; column1 < controlColumns; column1++){
            for(int row1 = 0; row1 < controlRows; row1++){

                ArrayList[][] results = GetAllPathsOriginatingFrom(column1,row1);
                for(int column2 = 0; column2 < controlColumns; column2++){
                    for(int row2 = 0; row2 < controlRows; row2++){
                        controlZonePaths[column1][row1][column2][row2] = (Loc[])results[column2][row2].toArray( new Loc[results[column2][row2].size()]);
                    }
                }
            }
        }

    }


    private static ArrayList<Loc>[][] pathcache;
    private static int[][] distancecache;

    private static ArrayList[][] GetAllPathsOriginatingFrom(int column, int row){
        pathcache = new ArrayList[controlColumns][controlRows];
        distancecache = new int[controlColumns][controlRows];

        for(int x = 0; x < controlColumns; x++){
            for(int y = 0; y < controlRows; y++){
                distancecache[x][y] = 999999;
            }
        }
        recursivelyFindPath(column,row,0,new ArrayList<>(),0,0,true);
        return pathcache;
    }

    private static void recursivelyFindPath(int x, int y,int dist,ArrayList<Loc> path, int lastX, int lastY, boolean first){

        if(x >=0 && y>=0 && x<controlColumns && y<controlRows) {
            dist += Map.findDistanceTo(controlLocations[lastX][lastY],controlLocations[x][y]);
            if (dist < distancecache[x][y]) {
                if(!first) {
                    path.add(Map.locations[x][y]);
                }
                distancecache[x][y] = dist;
                pathcache[x][y] = path;

                if(x-1 != lastX)recursivelyFindPath(x-1,y,dist,(ArrayList<Loc>)path.clone(),x,y,false);
                if(x+1 != lastX)recursivelyFindPath(x+1,y,dist,(ArrayList<Loc>)path.clone(),x,y,false);
                if(y-1 != lastY)recursivelyFindPath(x,y-1,dist,(ArrayList<Loc>)path.clone(),x,y,false);
                if(y+-1 != lastY)recursivelyFindPath(x,y+1,dist,(ArrayList<Loc>)path.clone(),x,y,false);
//                if(x-1 != lastX || y-1 != lastY)recursivelyFindPath(x-1,y-1,dist,(ArrayList<Loc>)path.clone(),x,y);
//                if(x-1 != lastX || y+1 != lastY)recursivelyFindPath(x-1,y+1,dist,(ArrayList<Loc>)path.clone(),x,y);
//                if(x+1 != lastX || y-1 != lastY)recursivelyFindPath(x+1,y-1,dist,(ArrayList<Loc>)path.clone(),x,y);
//                if(x+1 != lastX || y+1 != lastY)recursivelyFindPath(x+1,y+1,dist,(ArrayList<Loc>)path.clone(),x,y);



            }
        }

    }



}
