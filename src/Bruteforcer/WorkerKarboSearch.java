package Bruteforcer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Stack;

public class WorkerKarboSearch {


    public static boolean blendMap = false;


    public static void think(){

        if(Map.myTotalWorkersBuilt < 10){

        }else if(Map.myTotalWorkersBuilt < 30){
            if(R.turn % 3 != 1){
                //Save some performance, this is rather heavy on some maps
                return;
            }
        } else if(Map.myTotalWorkersBuilt < 60){
            if(R.turn % 6 != 1){
                //Save some performance, this is rather heavy on some maps
                return;
            }
        }else{
            if(R.turn % 12 != 1){
                //Save some performance, this is rather heavy on some maps
                return;
            }
        }


        for(Worker w : Map.myWorkers){
            w.assignedKarboSectiongoal = null;
            w.currentlySittingOnKarboSection = 0;
            w.karboInCurrentSection = 0;
        }

        if(R.POWERSAVEMODUS || Map.totalSuspectedReachableKarbonite < 50 || (R.amIEarth && R.turn > 250) || Map.myUnitCounts[Type.WORKER.typeId] == 0){
//            Debug.log("Not bothering with worker karbo searches");
            return;
        }

        if(blendMap && Map.totalSuspectedReachableKarbonite < 400){
            blendMap = false;
        }

//        double[][] blendedmap = blendmap(Map.karbonite);
//        blendedmap = blendmap(blendedmap);

        int[][] blendedmap = Map.karbonite;
        if(blendMap){
            blendedmap = blendmap(blendedmap);
        }


        int[][] sectionmap = new int[Map.width][Map.height];

//        double[] karboniteInSection = new double[200];
        double[] adjustedKarboniteInSection = new double[700];
        double[] sectionSizes = new double[700];


        ArrayList<WorkerSectionPair> workerSectionPairs = new ArrayList<>(Map.myUnitCounts[Type.WORKER.typeId] * 10);

        double maxdist = Map.locations[0][0].distanceTo(Map.approxCenter);


        int sectioncount = 0;
        for(int x = 0; x < Map.width; x++){
            for(int y = 0; y < Map.height; y++){
                if(blendedmap[x][y] > 0 && sectionmap[x][y] == 0 && Map.reachable[x][y]){
                    MainLoc start = Map.locations[x][y];
                    Stack<MainLoc> stack = new Stack<MainLoc>();
                    stack.push(start);

                    LinkedList<MainLoc> queue = new LinkedList<MainLoc>();

                    boolean[][] visitedBFS = new boolean[Map.width][Map.height];
                    MainLoc[][] originSquares = new MainLoc[Map.width][Map.height];
                    //DFS to find karbonite sections
                    int section = ++sectioncount;
                    while(!stack.empty()) {
                        MainLoc l = stack.pop();
//                        karboniteInSection[section] += blendedmap[l.x][l.y];

                        adjustedKarboniteInSection[section] += blendedmap[l.x][l.y] * (2.5 -  (l.distanceTo(Map.approxCenter) / maxdist) ) ;
                        sectionSizes[section]++;

                        if(Map.karbonite[l.x][l.y] > 0){
                            queue.add(l);
                            visitedBFS[l.x][l.y] = true;
                            originSquares[l.x][l.y] = l;
                        }

                        for(MainLoc ml : l.adjacentPassable){
                            if(blendedmap[ml.x][ml.y] > 0 && sectionmap[ml.x][ml.y] == 0){
                                sectionmap[ml.x][ml.y] = section;
                                stack.push(ml);
                            }
                        }
                    }

                    if(adjustedKarboniteInSection[section] > 5) {
                        //Multi-source BFS to find the distance to all workers
                        //Ignoring all tiny sections for performance reasons
                        while (!queue.isEmpty()) {
                            MainLoc l = queue.removeFirst();

                            MainLoc originSquare = originSquares[l.x][l.y];
                            for (MainLoc ml : l.adjacentPassable) {
                                if (!visitedBFS[ml.x][ml.y]) {
                                    visitedBFS[ml.x][ml.y] = true;
                                    originSquares[ml.x][ml.y] = originSquare;
                                    queue.add(ml);
                                }
                            }
                            if (l.containsRobot) {
                                Robot r = l.getRobot();
                                if (r.isMine && r.amIWorker) {
                                    workerSectionPairs.add(new WorkerSectionPair((Worker) r, originSquare, section));
                                }
                            }
                        }
                    }
                }
            }
        }


        if(sectioncount > 15){
            //Dont want to kill performance too much, this will join sections next turn
            blendMap = true;

        }

//        Debug.log("Karbo sections found: " + sectioncount + "pairs: " + workerSectionPairs.size());
//        for(int i = 1 ; i <= sectioncount; i++){
//            Debug.log("Section : " + i + " arbo: " + adjustedKarboniteInSection[i]);
//        }

        double[] workersAssignedToSection = new double[200];

        while (true){
            WorkerSectionPair bestPair = null;
            double bestscore = -20;

            for(WorkerSectionPair pair : workerSectionPairs){
                if(pair.worker.assignedKarboSectiongoal == null) {
                    double score = -pair.distance;
                    if(pair.distance < 3){
                        score += 3;
                    }
                    score +=  0.1 * adjustedKarboniteInSection[pair.section] / (2.0 + workersAssignedToSection[pair.section]);
                    score += 0.2 * adjustedKarboniteInSection[pair.section] / sectionSizes[pair.section];
                    if(score > bestscore){
                        bestscore = score;
                        bestPair = pair;
                    }
                }

            }

            if(bestPair == null){
                break;
            }else{
                workersAssignedToSection[bestPair.section]++;
                bestPair.worker.assignedKarboSectiongoal = bestPair.closestSpot;
                if(bestPair.distance < 3) {
                    bestPair.worker.currentlySittingOnKarboSection =  bestPair.section;
                }
//                Debug.log(bestPair.worker.loc + " -> " + bestPair.closestSpot  + "(" + bestPair.section + ")");
            }
        }

        for(Worker w : Map.myWorkers){
            if(w.currentlySittingOnKarboSection != 0){
                w.karboInCurrentSection =   adjustedKarboniteInSection[w.currentlySittingOnKarboSection] / (2.0 + workersAssignedToSection[w.currentlySittingOnKarboSection]) ;
            }
        }


    }


    public static class WorkerSectionPair{
        public Worker worker;
        public MainLoc closestSpot;
        public int section;
        public int distance;

        public WorkerSectionPair(Worker w, MainLoc spot, int section){
            this.worker = w;
            this.closestSpot = spot;
            this.section = section;
            distance = Map.findDistanceTo(w.loc,spot);
        }
    }


    public static double[][] blendmap(double[][] startmap){
        double[][] newmap = new double[Map.width][Map.height];
        for(int x = 0 ; x < Map.width; x++){
            for(int y = 0 ; y < Map.width; y++){
                if(startmap[x][y] > 0){
                    MainLoc l = Map.locations[x][y];
                    double spread=  startmap[x][y] / ( (double)l.passableNeighboursCount + 1.0);
                    for(MainLoc l2 : l.adjacentPassable){
                        newmap[l2.x][l2.y] += spread;
                    }
                    newmap[l.x][l.y] += spread;
                }
            }
        }

        return newmap;
    }
    public static int[][] blendmap(int[][] startmap){
        int[][] newmap = new int[Map.width][Map.height];
        for(int x = 0 ; x < Map.width; x++){
            for(int y = 0 ; y < Map.height; y++){
                if(startmap[x][y] > 0){
                    MainLoc l = Map.locations[x][y];
                    int spread=  ((startmap[x][y] / (l.passableNeighboursCount + 1)));
                    int remainder = startmap[x][y] - (spread * l.passableNeighboursCount);
                    for(MainLoc l2 : l.adjacentPassable){
                        newmap[l2.x][l2.y] += spread;
                    }
                    newmap[l.x][l.y] += remainder;

                }
            }
        }

        return newmap;
    }

}
