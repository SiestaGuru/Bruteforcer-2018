package Bruteforcer;

import java.util.ArrayList;
import java.util.HashMap;

public class Map{

    public static int width;
    public static int height;


    public static int marsWidth;
    public static int marsHeight;



    public static int widthMinusOne;
    public static int heightMinusOne;



    public static boolean blocked[][];
    public static boolean marsBlocked[][];
    public static int karbonite[][];

    public static int totalVisibleKarbonite;
    public static int totalSuspectedReachableKarbonite;

    public static Robot[][] robots;
    public static int[][] nodeIds;
    public static MainLoc[][] locations;
    public static boolean[][] inSight;
    public static boolean[][] reachable;
    public static double[][] globalDesire;
    public static ArrayList<Loc> visibleTiles = new ArrayList<>();


    public static ArrayList<Robot> myRobots;
    public static ArrayList<Robot> postponedBots;
    public static ArrayList<Robot> latecomers;
    public static ArrayList<Worker> myWorkers;
    public static ArrayList<Knight> myKnights;
    public static ArrayList<Dpser> myDpsers;
    public static ArrayList<Mage> myMages;
    public static ArrayList<Factory> myFactories;
    public static ArrayList<Factory> myCompletedFactories;
    public static ArrayList<Rocket> myCompletedRockets;
    public static ArrayList<Rocket> myRockets;
    public static ArrayList<Healer> myHealers;
    public static ArrayList<Ranger> myRangers;
    public static ArrayList<Robot> myDamagedRobots;
    public static ArrayList<Robot> theirRobots;
    public static ArrayList<Robot> theirDps;
    public static ArrayList<Mage> theirMages;
    public static ArrayList<Factory> theirFactories;
    public static ArrayList<Robot> allRobots;
    public static ArrayList<Robot> mySpaceUnits;
    public static ArrayList<Robot> botsJustUnloaded;
    public static HashMap<Integer,Robot> robotsById;
    public static HashMap<Integer,Factory> theirFactoriesById;


    public static HashMap<Integer,Robot> theirGhostImages;


    public static ArrayList<Rocket> launchingRockets;


    public static MainLoc[] nodes;
    public static MainLoc[][] fastestToFirstStep;
//    public static Loc[][] fastestToAhead;
    public static int[][] distanceTo;
    public static double[] nodeAverageDistance;
    public static int[] nodeTilesQuicklyReachable;
    public static boolean alreadyPathfinding; //for concurrency

    public static int nodeCount;

    public static ArrayList<MainLoc> startingSpots;
    public static ArrayList<MainLoc> myStartingSpots;
    public static ArrayList<MainLoc> theirStartingSpots;

    public static int totalUnitCount = 0;
    public static int myTotalKnightsBuilt = 0;
    public static int myTotalUnitsBuilt = 0;
    public static int myTotalWorkersBuilt = 0;
    public static int myTotalDpsBuilt = 0;
    public static int myTotalHealersBuilt = 0;
    public static int myTotalMagesBuilt = 0;
    public static int myTotalRangersBuilt = 0;
    public static int myTotalUnitsFinished = 0;
    public static int myTotalStructuresBuilt = 0;
    public static int myTotalUnitCount = 0;
    public static int myUsableTotalDpsCount = 0;
    public static int theirTotalUnitCount = 0;
    public static int[] myUnitCounts = new int[8];
    public static int[] startedBuildCount = new int[8];
    public static int[] finishedUnitCounts = new int[8];
    public static int[] myNonGarrisonedUnitCounts = new int[8];
    public static int[] theirUnitCounts = new int[8];
    public static double[] theirRollingUnitCounts = new double[8];
    public static int[] mySpaceCounts = new int[8];

    public static Loc approxCenter;
    public static Loc generallyPassableApproxCenter;


    public static int totalKarboHarvested = 0;
    public static int initialTotalKarbo = 0;

    public static void init(int width, int height){
        Map.width = width;
        Map.height = height;
        Map.widthMinusOne = Map.width - 1;
        Map.heightMinusOne = Map.height - 1;
        blocked = new boolean[width][height];
        reachable = new boolean[width][height];
        karbonite = new int[width][height];
        robots = new Robot[width][height];
        locations = new MainLoc[width][height];
        nodeIds = new int[width][height];
        nodeCount = width * height;
        nodes = new MainLoc[nodeCount];
        fastestToFirstStep = new MainLoc[nodeCount][];
//        fastestToAhead = new Loc[nodeCount][];
        distanceTo = new int[nodeCount][];
        alreadyPathfinding = false;

        myStartingSpots = new ArrayList<>();
        theirStartingSpots = new ArrayList<>();



        distanceCache = new int[nodeCount];
        theirRobots = new ArrayList<>();
        theirDps = new ArrayList<>();
        theirMages = new ArrayList<>();
        theirFactories = new ArrayList<>();
        myRobots = new ArrayList<>();
        postponedBots = new ArrayList<>();
        latecomers = new ArrayList<>();
        botsJustUnloaded = new ArrayList<>();
        allRobots = new ArrayList<>();
        robotsById = new HashMap<>();
        theirFactoriesById = new HashMap<>();
        theirGhostImages = new HashMap<>();
        mySpaceUnits = new ArrayList<>();
        myHealers = new ArrayList<>();
        myWorkers = new ArrayList<>();
        myKnights = new ArrayList<>();
        myDpsers = new ArrayList<>();
        myMages = new ArrayList<>();
        myRangers = new ArrayList<>();
        myFactories = new ArrayList<>();
        myCompletedFactories = new ArrayList<>();
        myCompletedRockets = new ArrayList<>();
        myRockets = new ArrayList<>();
        myDamagedRobots = new ArrayList<>();
        launchingRockets = new ArrayList<>();

        for(int x =0; x < width;x++){
            for(int y =0; y < height;y++){
                locations[x][y] = new MainLoc(x,y);
                nodeIds[x][y] = x + y * width;

                int nodeid = x + y * width;
                nodes[nodeid] = locations[x][y];
                nodes[nodeid].nodeId = nodeid;
            }
        }
        nodeAverageDistance = new double[nodeCount];
        nodeTilesQuicklyReachable = new int[nodeCount];
        for(int i = 0 ; i < nodeCount; i++){
            nodeAverageDistance[i] = -1;
            nodeTilesQuicklyReachable[i] = -1;
        }
    }




    public static ArrayList<Robot> getRobotsInRangeFilterId(Circle c, int id){
        ArrayList<Robot> list = new ArrayList<>();

        int lookdist = (int)LookupSqrt.sqrt[c.radiusSquared] + 1;

        for(int x = Math.max(0,c.x-lookdist);  x <=  Math.min(widthMinusOne,c.x+lookdist); x++){

            for(int y = Math.max(0,c.y-lookdist);  y <=  Math.min(heightMinusOne,c.y+lookdist); y++){


//                if(id == 4 && R.turn < 5)
//                System.out.print("[" + x + ", " + y + "]");

                Robot r = Map.robots[x][y];
                if(r != null){
//                    if(id == 4 && R.turn < 5)Debug.log(r);
                    if(c.contains(r.loc)){
//                        if(id == 4 && R.turn < 5)Debug.log("Contains");
                        if(r.id != id){
//                            if(id == 4 && R.turn < 5)Debug.log("Id");
//                            if(id == 4 && R.turn < 5 && r.amIFactory) Debug.log("GREAT SUCCESS");
                            list.add(r);
                        }
                    }
                }
            }
        }
        return list;
    }

    public static ArrayList<Robot> getRobotsInRange(Circle c){
        ArrayList<Robot> list = new ArrayList<>();

        int lookdist = (int)LookupSqrt.sqrt[c.radiusSquared]  + 1;
        for(int x = Math.max(0,c.x-lookdist);  x <=  Math.min(widthMinusOne,c.x+lookdist); x++){
            for(int y = Math.max(0,c.y-lookdist);  y <=  Math.min(heightMinusOne,c.y+lookdist); y++){
                Robot r = robots[x][y];

                if(r != null && c.contains(r.loc)){
                    list.add(r);
                }
            }
        }
        return list;
    }

    public static ArrayList<Robot> getRobotsInRange(Circle c, Type type){
        ArrayList<Robot> list = new ArrayList<>();

        int lookdist = (int)LookupSqrt.sqrt[c.radiusSquared]  + 1;
        for(int x = Math.max(0,c.x-lookdist);  x <=  Math.min(widthMinusOne,c.x+lookdist); x++){
            for(int y = Math.max(0,c.y-lookdist);  y <=  Math.min(heightMinusOne,c.y+lookdist); y++){
                Robot r = robots[x][y];
                if(r != null && r.type == type && c.contains(r.loc)){
                    list.add(r);
                }
            }
        }
        return list;
    }


    public static ArrayList<Robot> getEnemiesInRange(Circle c) {
        ArrayList<Robot> list = new ArrayList<>();

        if(Map.theirRobots.size() > c.radiusSquared * 7){ //Performance wise, there's some kind of point where one method gets better performance. This is not necessarily that point, but may be approximately right
            int lookdist = (int)LookupSqrt.sqrt[c.radiusSquared]  + 1;
            for(int x = Math.max(0,c.x-lookdist);  x <=  Math.min(widthMinusOne,c.x+lookdist); x++){
                for(int y = Math.max(0,c.y-lookdist);  y <=  Math.min(heightMinusOne,c.y+lookdist); y++){
                    Robot r = robots[x][y];
                    if(r != null && !r.isMine && c.contains(r.loc)){
                        list.add(r);
                    }
                }
            }
        }else{
            for(Robot r: Map.theirRobots){
                if(c.contains(r.loc) && !r.inGarrison && !r.isDead){ //Other method doesn't do garrisoned/dead either
                    list.add(r);
                }
            }
        }
        return list;
    }

    public static ArrayList<Robot> getEnemiesInRange(Circle c, Type type) {
        ArrayList<Robot> list = new ArrayList<>();

        if(Map.theirRobots.size() > c.radiusSquared * 7){ //Performance wise, there's some kind of point where one method gets better performance. This is not necessarily that point, but may be approximately right
            int lookdist = (int)LookupSqrt.sqrt[c.radiusSquared]  + 1;
            for(int x = Math.max(0,c.x-lookdist);  x <=  Math.min(widthMinusOne,c.x+lookdist); x++){
                for(int y = Math.max(0,c.y-lookdist);  y <=  Math.min(heightMinusOne,c.y+lookdist); y++){
                    Robot r = robots[x][y];
                    if(r != null && !r.isMine && r.type == type && c.contains(r.loc)){
                        list.add(r);
                    }
                }
            }
        }else{
            for(Robot r: Map.theirRobots){
                if(r.type == type && c.contains(r.loc) && !r.inGarrison && !r.isDead){ //Other method doesn't do garrisoned/dead either
                    list.add(r);
                }
            }
        }
        return list;
    }


    public static ArrayList<Robot> getAlliesInRange(Circle c) {
        ArrayList<Robot> list = new ArrayList<>();

        if(Map.myRobots.size() > c.radiusSquared * 7){ //Performance wise, there's some kind of point where one method gets better performance. This is not necessarily that point, but may be approximately right
            int lookdist = (int)LookupSqrt.sqrt[c.radiusSquared]  + 1;
            for(int x = Math.max(0,c.x-lookdist);  x <=  Math.min(widthMinusOne,c.x+lookdist); x++){
                for(int y = Math.max(0,c.y-lookdist);  y <=  Math.min(heightMinusOne,c.y+lookdist); y++){
                    Robot r = robots[x][y];
                    if(r != null && r.isMine && c.contains(r.loc)){
                        list.add(r);
                    }
                }
            }
        }else{
            for(Robot r: Map.myRobots){
                if(c.contains(r.loc) && !r.inGarrison && !r.isDead){ //Other method doesn't do garrisoned/dead either
                    list.add(r);
                }
            }
        }
        return list;
    }

    public static ArrayList<Robot> getAlliesInRange(Circle c, Type type) {
        ArrayList<Robot> list = new ArrayList<>();

        if(Map.myRobots.size() > c.radiusSquared * 7){ //Performance wise, there's some kind of point where one method gets better performance. This is not necessarily that point, but may be approximately right
            int lookdist = (int)LookupSqrt.sqrt[c.radiusSquared]  + 1;
            for(int x = Math.max(0,c.x-lookdist);  x <=  Math.min(widthMinusOne,c.x+lookdist); x++){
                for(int y = Math.max(0,c.y-lookdist);  y <=  Math.min(heightMinusOne,c.y+lookdist); y++){
                    Robot r = robots[x][y];
                    if(r != null && r.type == type && r.isMine && c.contains(r.loc)){
                        list.add(r);
                    }
                }
            }
        }else{
            for(Robot r: Map.myRobots){
                if(r.type == type && c.contains(r.loc) && !r.inGarrison && !r.isDead){ //Other method doesn't do garrisoned/dead either
                    list.add(r);
                }
            }
        }
        return list;
    }



    public static Loc findPathToNextStep(int nodestart, int nodeend){
        if(fastestToFirstStep[nodestart] == null){
            calcPathfindingFromLocationNew(nodestart);
        }
        return fastestToFirstStep[nodestart][nodeend];
    }
    public static Loc findPathToNextStep(Loc startLoc, Loc goal){
        int nodestart = nodeIds[startLoc.x][startLoc.y];
        int nodeend = nodeIds[goal.x][goal.y];

        if(fastestToFirstStep[nodestart] == null){
            calcPathfindingFromLocationNew(nodestart);
        }
        return fastestToFirstStep[nodestart][nodeend];
    }

//    public static Loc findPathAhead(Loc startLoc, Loc goal){
//        int nodestart = nodeIds[startLoc.x][startLoc.y];
//        int nodeend = nodeIds[goal.x][goal.y];
//
//        if(fastestToAhead[nodestart] == null){
//            calcPathfindingFromLocation(nodestart);
//        }
//
//        return fastestToAhead[nodestart][nodeend];
//    }

    public static int findDistanceTo(Loc startLoc, Loc goal){
        int nodestart = nodeIds[startLoc.x][startLoc.y];
        int nodeend = nodeIds[goal.x][goal.y];

        if(distanceTo[nodestart] == null){
            if(R.POWERSAVEMODUSHYPER){
                //Distance is 5, what did you think?
                return 5;
            }
//            long time1 = System.nanoTime();
//            calcPathfindingFromLocation(nodestart);
//            long time2 = System.nanoTime();
            calcPathfindingFromLocationNew(nodestart);
//            long time3 = System.nanoTime();

//            Debug.log("time: " +  ((double)(time3-time2))/1000000.0);



//            timesum1 +=  (time2-time1);
//            timesum2 +=  (time3-time2);

//            Debug.log("1: " + timesum1 + " 2: " + timesum2);


        }
        return distanceTo[nodestart][nodeend];
    }


    public static int findDistanceTo(int nodestart, int nodeend){
        if(distanceTo[nodestart] == null){
            if(R.POWERSAVEMODUSHYPER){
                return 5;
            }
            calcPathfindingFromLocationNew(nodestart);
        }
        return distanceTo[nodestart][nodeend];
    }

    public static double getNodeDifficultyToReach(Loc loc){
        int node = nodeIds[loc.x][loc.y];
        if(nodeAverageDistance[node] < 0){
            calcPathfindingFromLocationNew(node);
        }
        return nodeAverageDistance[node];

    }
    public static double getNodeAccessibleTiles(Loc loc){
        int node = nodeIds[loc.x][loc.y];
        if(nodeTilesQuicklyReachable[node] < 0){
            calcPathfindingFromLocationNew(node);
        }
        return nodeTilesQuicklyReachable[node];
    }


    public static boolean isOnTheMap(int x, int y){
        return x>= 0 && x<width && y>= 0 && y<height;
    }


    private static int distanceCache[];
    private static MainLoc step1Cache[];
//    private static Loc step4Cache[];


    private static final int maxQueueSize = 10000;
    private static int[] xQueue = new int[maxQueueSize];
    private static int[] yQueue = new int[maxQueueSize];
    private static int[] currentWinner = new int[maxQueueSize];
    private static MainLoc[] step1Queue = new MainLoc[maxQueueSize];
//    private static Loc[] step4Queue = new Loc[maxQueueSize];
    private static int queueCounter = 0;
    private static int queueSize = 1;


    private static MainLoc[] mainqueue = new MainLoc[maxQueueSize];

    //Ugh, why isn't this actually faster? :/
    private static void calcPathfindingFromLocationNew(int node) {
        for(MainLoc m : nodes){
            m.pathfindingFirstStep = null;
            m.pathfindingStepsReq = 10000;
        }

        mainqueue[0] = nodes[node];
        mainqueue[0].pathfindingStepsReq = 0;

        int entries = 1;
        int step = 0;
        distanceTo[node] = new int[nodeCount];
        fastestToFirstStep[node] = new MainLoc[nodeCount];

        for(int i = 0 ; i < nodeCount; i++){
            distanceTo[node][i] = 10000;
        }


        while(step < entries){
            MainLoc curItem = mainqueue[step];
//            Debug.log("s: " + step + " " +  curItem);

            if(curItem.pathfindingStepsReq <= 1){
                curItem.pathfindingFirstStep = curItem;
                fastestToFirstStep[node][curItem.nodeId] = curItem;
            }else {
                fastestToFirstStep[node][curItem.nodeId] = curItem.pathfindingFirstStep;
            }
            distanceTo[node][curItem.nodeId] = curItem.pathfindingStepsReq;

            int newdist = curItem.pathfindingStepsReq + 1;
            for(int i = curItem.passableNeighboursCount - 1 ; i >= 0 ; i--){
                MainLoc m = curItem.adjacentPassable[i];
                if(m.pathfindingFirstStep == null) {
                    m.pathfindingStepsReq = newdist;
                    m.pathfindingFirstStep = curItem.pathfindingFirstStep;
                    mainqueue[entries++] = m;
                }
            }
            step++;
        }

        double distancesum = 0;
        int quicklyreachable = 0;
        for(int i = 0 ; i < nodeCount; i++){

            distancesum += Math.min(distanceTo[node][i],15);
            if(distanceTo[node][i] < 8){
                quicklyreachable++;
            }

        }
        nodeAverageDistance[node] = distancesum / ((double)nodeCount);
        nodeTilesQuicklyReachable[node] = quicklyreachable;

    }


    private static void calcPathfindingFromLocation(int node) {

        while (alreadyPathfinding) {
            try {
                Thread.sleep(1);
            } catch (Exception ex) {
            }
        }
        alreadyPathfinding = true;

        step1Cache = new MainLoc[nodeCount];
//        step4Cache = new Loc[nodeCount];

        for (int i = 0; i < nodeCount; i++) {
            distanceCache[i] = 10000;
        }

        queueCounter = 0;
        queueSize = 0;
//        addToQueue(nodes[node].x, nodes[node].y, -1, null, null);
        addToQueue(nodes[node].x, nodes[node].y, 0, null);

        while (queueCounter < queueSize) {
            int x = xQueue[queueCounter];
            int y = yQueue[queueCounter];
            int curNode = nodeIds[x][y];

            if (currentWinner[curNode] == queueCounter) {
                //System.out.println(x + "," + y);
                int step = distanceCache[curNode] + 1;

                if (step <= 2) {
                    step1Queue[queueCounter] = locations[x][y];
                }
                MainLoc step1 = step1Queue[queueCounter];
//                if (step <= 5) {
//                    step4Queue[queueCounter] = locations[x][y];
//                }

                step1Cache[curNode] = step1;
//                step4Cache[curNode] = step4Queue[queueCounter];

//                addToQueue(x - 1, y - 1,  step,  step1Queue[queueCounter], step4Queue[queueCounter]);
//                addToQueue(x - 1, y + 1,  step,  step1Queue[queueCounter], step4Queue[queueCounter]);
//                addToQueue(x + 1, y - 1,  step,  step1Queue[queueCounter], step4Queue[queueCounter]);
//                addToQueue(x + 1, y + 1, step,  step1Queue[queueCounter], step4Queue[queueCounter]);
//
//                addToQueue(x - 1, y, step,  step1Queue[queueCounter], step4Queue[queueCounter]);
//                addToQueue(x + 1, y,  step,  step1Queue[queueCounter], step4Queue[queueCounter]);
//                addToQueue(x, y - 1,  step,  step1Queue[queueCounter], step4Queue[queueCounter]);
//                addToQueue(x, y + 1,  step,  step1Queue[queueCounter], step4Queue[queueCounter]);


                if (x > 0) {
                    if(y > 0) {
                        addToQueue(x - 1, y - 1, step, step1);
                    }
                    if( y < heightMinusOne) {
                        addToQueue(x - 1, y + 1, step, step1);
                    }
                }
                if (x < widthMinusOne) {
                    if(y > 0) {
                        addToQueue(x + 1, y - 1, step, step1);
                    }
                    if( y < heightMinusOne) {
                        addToQueue(x + 1, y + 1, step, step1);
                    }
                    addToQueue(x + 1, y, step, step1);
                }

                if (x > 0) {
                    addToQueue(x - 1, y, step, step1);
                }
                if(y > 0) {
                    addToQueue(x, y - 1, step, step1);
                }
                if( y < heightMinusOne) {
                    addToQueue(x, y + 1, step, step1);
                }
            }
            queueCounter++;
        }


        fastestToFirstStep[node] = step1Cache.clone();
//        fastestToAhead[node] = step4Cache.clone();
        distanceTo[node] = distanceCache.clone();

        alreadyPathfinding = false;
    }

//    private static void addToQueue(int x, int y, int step, Loc step1, Loc step4){
    private static void addToQueue(int x, int y, int step, MainLoc step1){
        if(!blocked[x][y]) {
            int node = nodeIds[x][y];
            if (step < distanceCache[node]) {
                distanceCache[node] = step;
                currentWinner[node] = queueSize;
                xQueue[queueSize] = x;
                yQueue[queueSize] = y;
                //step4Queue[queueSize] = step4;
                step1Queue[queueSize++] = step1;
            }
        }
    }




//    private static void addToQueue(int x, int y, boolean diagonal, int step, int distance, Loc step1, Loc step4 ){
//        if(x <0 || y < 0 || x >= width || y >= width || !blocked[x][y]) return;
//
//        if(diagonal){
//            distance += 14;
//        }else{
//            distance += 10;
//        }
//        int node = nodeIds[x][y];
//
//        if(distance >= distanceCache[node]) return;
//
//        if(step <= 1){
//            step1 = locations[x][y];
//        }
//        if(step <= 4){
//            step4 = locations[x][y];
//        }
//        step1Cache[node] = step1;
//        step4Cache[node] = step4;
//
//        step++;
//
//        recursive(x-1,y-1,true,step,distance,step1,step4);
//        recursive(x-1,y-1,true,step,distance,step1,step4);
//        recursive(x-1,y-1,true,step,distance,step1,step4);
//        recursive(x-1,y-1,true,step,distance,step1,step4);
//
//
//    }












}
