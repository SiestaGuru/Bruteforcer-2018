package Bruteforcer;

import bc.UnitType;

import java.util.ArrayList;

public class Construction {

    public static boolean wantToMakeUnits;

    public static int turnsUnableToFindFactorySpot = 0;

//    public static int HoldUpBoysIveGotSomethingReallyImportantToDoRightHere = -10000;

    public static void DoThinking(int attempt, boolean banRocket, boolean banFactory, boolean banunits, boolean banworker) {
        if(attempt > 4) return;

        if (R.karbonite < 40) return;

        int totalFactories = Map.myUnitCounts[Type.FACTORY.typeId] + Worker.factoriesBeingConstructed.size();

        double baseWeightWorker = -25.0;
        double baseWeightFactory = 0.0;
        double baseWeightRocket = 0.0;
        double baseWeightUnit = 150.0;

        int earliestFactoryFree = 30;

        for(Factory f : Map.myFactories){
            if(!f.amIBlueprint){
                if(f.canProduceSomething){
                    earliestFactoryFree = 0;
                    break;
                }else{
                    if(f.turnsLeftProduction < earliestFactoryFree){
                        earliestFactoryFree = f.turnsLeftProduction;
                    }
                }
            }
        }

        int expectedKarboniteAtFactoryFree = R.karbonite;
        int expectedKarboniteAtFactoryFreeAssumingWorker = Math.max(0,R.karbonite - 60);
        int expectedKarboniteAtFactoryFreeAssumingFactory = Math.max(0,R.karbonite - 200);

        for(int i = 0; i < earliestFactoryFree; i++){
            expectedKarboniteAtFactoryFree +=   10 - (expectedKarboniteAtFactoryFree / 40);
            expectedKarboniteAtFactoryFreeAssumingWorker +=   10 - (expectedKarboniteAtFactoryFreeAssumingWorker / 40);
            expectedKarboniteAtFactoryFreeAssumingFactory +=   10 - (expectedKarboniteAtFactoryFreeAssumingFactory / 40);
        }

        if(expectedKarboniteAtFactoryFreeAssumingWorker < 40){
            baseWeightWorker -= 50;
//            Debug.log("Wait with replicate, don't want to lapse");
        }
        if(expectedKarboniteAtFactoryFreeAssumingFactory < 40){
            baseWeightFactory -= 20;
//            Debug.log("Wait with factory, don't want to lapse");
        }else{
            //Go goo
            baseWeightFactory += 20;
        }





        if(Map.myUsableTotalDpsCount == 0 && totalFactories > 0){
            baseWeightUnit += 100;
        }

        if(R.turn < 5){
            baseWeightWorker += 10.0;

            if(Map.myUnitCounts[Type.WORKER.typeId] < 3){
                baseWeightWorker += 50;
            }
        }

//        if(R.turn < 50) {
            baseWeightWorker -= Map.myUnitCounts[Type.WORKER.typeId] * 12;
//        }else{
//            baseWeightWorker -= Map.myUnitCounts[Type.WORKER.typeId] * 7;
//        }

//        if (Map.myUnitCounts[Type.WORKER.typeId] >   4 + Math.min(6,R.turn * 1.8) ){
//            baseWeightWorker -= 10;
//        }
//        if(R.player1) {


            if (totalFactories == 0 && R.turn > C.Clamp(MapSections.reachableTerrain / 50,6,25) ) {
//                Debug.log("First factory conserver " + MapSections.reachableTerrain);
                baseWeightWorker -= 250;
                baseWeightFactory += 200;
            }
//        }








        if(R.turn > 600){
            if(Map.myUsableTotalDpsCount > 30) {
                baseWeightUnit -= 40;
            }
            if(Map.myNonGarrisonedUnitCounts[Type.WORKER.typeId] < 14){
                baseWeightWorker += 30;
                baseWeightRocket += 30;
            }else{
                baseWeightRocket += 50;
            }

            if(Map.myNonGarrisonedUnitCounts[Type.WORKER.typeId] < 2){
                baseWeightWorker += 100;
            }
        }

        if(Worker.factoriesBeingConstructed.size() >= Map.myUnitCounts[Type.WORKER.typeId] - 1){
            double val = 20 +  10 * (Worker.factoriesBeingConstructed.size()-Map.myUnitCounts[Type.WORKER.typeId]);
            baseWeightWorker += val;
            baseWeightFactory -= val;

            if(R.turn < 30){
                baseWeightWorker += 100; //cmon, go build that first factory fast
                baseWeightFactory -= 100;
            }
        }

        double minWorker  = Math.max(20, 300 - R.turn * 5 );

        baseWeightWorker +=  Math.min(minWorker, Map.totalSuspectedReachableKarbonite * 0.030);
        baseWeightWorker += (Map.height + Map.width) * 0.05;


        if(R.amIEarth){
            if(R.turn > 450 && Worker.rocketsBeingConstructed.size() >= 1 && Map.myUnitCounts[Type.WORKER.typeId] < 15 && Map.myUsableTotalDpsCount >  Map.myUnitCounts[Type.WORKER.typeId] * 3){
                baseWeightWorker += 90;
            }
        }else{
            baseWeightWorker -= 20;
        }

        baseWeightWorker += Worker.rocketsBeingConstructed.size() * 5.0;

        if(Map.myUnitCounts[Type.WORKER.typeId] <= 2){
            if(totalFactories <= 2){
                baseWeightWorker += 40.0;
            }else {
                baseWeightWorker += 25.0;
            }
        }

        if(Map.myUnitCounts[Type.WORKER.typeId] < 6 && Map.myUnitCounts[Type.WORKER.typeId] < Map.myUsableTotalDpsCount / 8.0 ){
            baseWeightWorker += 5.0 + R.turn * 0.01 + Map.myUsableTotalDpsCount * 0.01;
        }





        double bestWorker = baseWeightWorker;
        Worker replicateWorker = null;

        Debug.beginClock(6);
        if(R.POWERSAVEMODUS){
            for (Worker r : Map.myWorkers) {
                if (r.canUseAbility()) {
                    replicateWorker = r;
                    break;
                }
            }
        }else if(!banworker) {
            for (Worker r : Map.myWorkers) {
                if (r.canUseAbility() && !r.replicateOrdered ) {
                    double score = baseWeightWorker;
                    double nearkarbo = ((double) MapMeta.getNearKarbo(r.loc, 5)) + 0.2 * (double) MapMeta.getNearKarboniteEstimate(r.loc);

                    double nearWorkers = 0;
                    for (Robot r2 : Map.myWorkers) {
                        if (r.loc.isWithinDistance(r2, 7)) {
                            nearWorkers++;
                        }
                    }
                    score += (nearkarbo / (1 + nearWorkers)) * 2.0;

                    if(r.karboInCurrentSection > 0) {
                        score += Math.min(500, r.karboInCurrentSection) * 0.5;
                    } else if(r.assignedKarboSectiongoal != null){
                        score -= Map.findDistanceTo(r.loc,r.assignedKarboSectiongoal);
                    }



                    int emptytiles = 0;
                    for (Loc l : r.adjacentPassableTiles) {
                        Robot adj = Map.robots[l.x][l.y];
                        emptytiles++;
                        if (adj != null && adj.amIStructure && adj.amIBlueprint && adj.health < adj.maxHealth * 0.6) {
                            if(Map.myUnitCounts[Type.FACTORY.typeId] <= 1) {
                                score += 60;
                            }else{
                                score += 25;
                            }
                        }
                    }


                    score +=  Math.min(400,MapSections.sectionSuspectedKarbo[r.loc.sectionId]) * 0.05;

                    score -= r.scaryEnemyRobotsNearSize * 50;

                    score += (7 - emptytiles) * 0.5;
                    double emptyrating = (1.0 - MapMeta.getNearFull(r.loc, 5));

                    score += emptyrating * 4.0;

                    if(R.amIMars && R.turn > 746){
                        score += 1000;
                    }

                    if (score > bestWorker) {
                        bestWorker = score;
                        replicateWorker = r;
                    }
                }
            }
        }


        //Prevents it from not building factories early in insane karbo maps
        bestWorker = Math.min(400,bestWorker);


        Debug.endClock(6);


        Debug.beginClock(7);
        if(R.amIEarth && Map.myUnitCounts[Type.WORKER.typeId] > 0) {
            if(!banFactory) {


                baseWeightFactory -= 30 * Worker.factoriesBeingConstructed.size();

                baseWeightFactory -= Map.myUnitCounts[Type.FACTORY.typeId] * 5;

                if (totalFactories < 4) {
                    if(Map.myUsableTotalDpsCount > 3) {
                        baseWeightFactory += 30;

                        if(Map.myUsableTotalDpsCount + Map.myUnitCounts[Type.HEALER.typeId] > 12){
                            baseWeightFactory += 100;
                        }
                    }

                    if (totalFactories < 4) {
                        baseWeightFactory += 60 + Map.myUsableTotalDpsCount / 5;
                    } else {
                        baseWeightFactory += 40 + Map.myUsableTotalDpsCount / 5;
                    }


                } else if (totalFactories > 4) {
                    baseWeightFactory -= 200;
                }

                baseWeightFactory += Math.min(2500,R.karbonite * 0.35);



                if(R.karbonite >= 240){
                    baseWeightFactory += 40;
                    baseWeightFactory += R.karbonite * 0.2;
                }
                baseWeightFactory +=   ((double)Map.myUsableTotalDpsCount / 2.0);

                if(totalFactories >= 3 && Map.myUsableTotalDpsCount < 8){
                    baseWeightFactory -= 20;
                    baseWeightUnit += 20;
                    if(totalFactories >= 4){
                        baseWeightFactory -= 20;
                        baseWeightUnit += 20;
                    }
                }


                if(Map.myUnitCounts[Type.WORKER.typeId] == 0){
                    baseWeightFactory -= 1000;
                    baseWeightRocket -= 1000;
                }

                if(totalFactories >= 5 ) {
                    baseWeightFactory -= 30;

                    if(totalFactories > Map.myTotalUnitCount / 5 && R.karbonite < 100){
                        baseWeightFactory -= 50;
                    }

                    if (totalFactories >= 6) {
                        baseWeightFactory -= 30;
                        if (totalFactories >= 7) {
                            baseWeightFactory -= 100;
                            if (totalFactories >= 8) {
                                baseWeightFactory -= 200;
                                if(totalFactories >= 11){
                                    baseWeightFactory -= 50 * totalFactories;
                                }
                            }
                        }
                    }
                }
            }

            if (Techs.canBuildRocket && !banRocket) {
                if (Rocket.totalRocketsBuilt == 0 && Worker.rocketsBeingConstructed.size() == 0) {
                    baseWeightRocket += 500;
                }

                if(!MapMeta.anyActivityDetected &&  Map.myRockets.size() < Map.myTotalUnitCount * 0.15){
                    baseWeightRocket += 150;
                }

                if (R.turn > 550) {
                    if (R.turn > 660) {
                        if (R.turn > 730) {
                            baseWeightRocket -= 10000; //cant finish, dont bother
                        } else {
                            baseWeightRocket += 70 + 12.0 * (Map.myWorkers.size() + Map.myHealers.size() + Map.myUsableTotalDpsCount - 6 * (Map.myRockets.size() + Worker.rocketsBeingConstructed.size()));

                        }
                    } else {
                        if (R.turn > 600) {
                            //turns 601-660
                            baseWeightRocket += 50 + 8.0 * (Map.myUsableTotalDpsCount - 6 * (Map.myRockets.size() + Worker.rocketsBeingConstructed.size()));

                        } else {
                            //turns 551-600
                            baseWeightRocket += 40 + 6.0 * (Map.myUsableTotalDpsCount - 6 * (Map.myRockets.size() + Worker.rocketsBeingConstructed.size()));

                        }
                    }
                }

                baseWeightRocket -= Worker.rocketsBeingConstructed.size() * 20;



                if(R.earthIsFullyDisconnected &&  Map.myUsableTotalDpsCount > 6 * (Map.myRockets.size() + Worker.rocketsBeingConstructed.size())){
                    baseWeightRocket += 50;
                }
            }
        }
        Debug.endClock(7);

//        Debug.log("Worker: " + bestWorker + " Factory: " + baseWeightFactory + " Rocker: " + baseWeightRocket + " Unit: " + baseWeightUnit);


        double bestScore = 0;
        int unittype = Type.NONE.typeId;


        if(bestWorker > bestScore && !banworker){
            bestScore = bestWorker;
            unittype = Type.WORKER.typeId;
        }

        if(R.amIEarth) {
            if (!banFactory && baseWeightFactory > bestScore) {
                bestScore = baseWeightFactory;
                unittype = Type.FACTORY.typeId;
            }
            if (!banRocket && Techs.canBuildRocket && baseWeightRocket > bestScore) {
                bestScore = baseWeightRocket;
                unittype = Type.ROCKET.typeId;
            }

            if(!banunits && Map.myFactories.size() > 0) {
                if (baseWeightUnit > bestScore) {
                    bestScore = baseWeightUnit;
                    unittype = Type.KNIGHT.typeId; //represents all units here
                    wantToMakeUnits = true;
                }
            }
        }


//        Debug.log("Scores: worker: " + bestWorker + " /" + baseWeightWorker + " factory: " + baseWeightFactory + " rocket " + baseWeightRocket + " unit " + baseWeightUnit);

//        if(banworker){
//            Debug.log("Worker banned");
//        }else{
//            Debug.log(replicateWorker);
//        }


        Debug.beginClock(8);
        if(unittype == Type.WORKER.typeId){
            if(replicateWorker != null){
//                Debug.log("Replicate worker");

                if(replicateWorker.hasDoneTurn){
                    replicateWorker.replicate();
                }else {
                    replicateWorker.replicateOrdered = true;
                }
                if(R.karbonite >= 40){
                    DoThinking(attempt + 1,banRocket,banFactory,banunits,true);
                }
            }
            else{
                Factory bestFactry = null;
                double bestFactoryScore = -10000;
                for (Factory f : Map.myCompletedFactories) {
                    if (f.canProduceSomething) {
                        double score = (1.0 - MapMeta.getNearFullEstimate(f.loc)) * 10.0;
                        score += MapMeta.getControlOf(f.loc) * 0.5;
                        if(score > bestFactoryScore){
                            bestFactry = f;
                            bestFactoryScore = score;
                        }
                    }
                }
                if(bestFactry != null){
                    bestFactry.buildWorker();
                    if(R.karbonite >= 40){
                        DoThinking(attempt + 1,banRocket,banFactory,banunits,true);
                    }
                }else{
                    if(R.karbonite >= 40){
                        DoThinking(attempt + 1,banRocket,banFactory,banunits,true);
                    }
                }


            }
        }
        else if(unittype == Type.FACTORY.typeId){

            if(R.karbonite >= Type.FACTORY.cost) {
                ArrayList<MainLoc> possibleLocations = new ArrayList<>();

                //TODO? : account for workers ability to move before dumping down factory
                for (Worker r : Map.myWorkers) {
                    if(!r.workerIsOnCooldown && !r.inSpace && !r.inGarrison && !r.amIExplorer) {
                        for (MainLoc l : r.adjacentPassableTiles) {
                            if(Map.robots[l.x][l.y] == null) {
                                possibleLocations.add(l);
                            }
                        }
                    }
                }

//                Debug.log("Factory is best, we have karbo  ");

                double bestlocscore = -10000000;

                double neededspotscore;

                if(turnsUnableToFindFactorySpot < 25 || totalFactories >= 3) {
                    neededspotscore = Math.max(turnsUnableToFindFactorySpot * -3, 100 - turnsUnableToFindFactorySpot * 15) - (60 + R.turn * 0.5);
                }else{
                    neededspotscore = -100000;
                }




                if(R.turn < 15){
                    bestlocscore -=150;
                }

                Loc bestLoc = null;

                int maxspots = 300;

                if(R.POWERSAVEMODUS){
                    maxspots = 100;
                    if(R.POWERSAVEMODUSHYPER){
                        maxspots = 10;
                    }
                }

                int counter = 0;

                for (MainLoc l : possibleLocations) {

                    if(counter++ > maxspots) break;

                    double totalscore = Math.max( baseWeightFactory * 0.25,60);

                    double spotscore =  0;

//                    Debug.lineBreak();
//                    Debug.log("Score1 : " + totalscore  + "  " + spotscore);

                    int passablecount = l.adjacentPassable.length;

                    for (Loc l2 : l.adjacentPassable) {
                        if(Map.robots[l2.x][l2.y] != null){
                            if(!Map.robots[l2.x][l2.y].amIWorker){
                                if(Map.robots[l2.x][l2.y].amIRocket){
                                    totalscore -= 100;
                                    passablecount--;
                                }
                                else if(Map.robots[l2.x][l2.y].amIFactory){
                                    totalscore -= 200;
                                    passablecount--;
                                }
                                else if(!Map.robots[l2.x][l2.y].amIWorker){
                                    totalscore -= 2;
                                }
                            }
                        }
                    }

//                    Debug.log("Score2 : " + totalscore  + "  " + spotscore);

                    spotscore +=  passablecount * 5 - 40;
                    if(passablecount <= 2){
                        spotscore -= 150;
                    }

                    Loc pathloc = Map.findPathToNextStep(l,Map.generallyPassableApproxCenter);
                    if(pathloc != null){
                        if(Map.robots[l.x][l.y] != null && Map.robots[l.x][l.y].amIStructure){
                            spotscore -= 100;
                        }
                    }
//                    Debug.log("Score3 : " + totalscore  + "  " + spotscore);
                    double startlocscount = Map.theirStartingSpots.size();
                    for(Loc l2 : Map.theirStartingSpots){
                        double dist = Map.findDistanceTo(l,l2);
                        if(dist < 100) {
                            if (GrandStrategy.strategy == GrandStrategy.KNIGHTRUSH) {
                                spotscore += 10 - dist/startlocscount;
                            } else {
                                spotscore += dist * 0.5 / startlocscount;
                            }
                        }
                    }
//                    Debug.log("Score4 : " + totalscore  + "  " + spotscore);
                    int mysection = l.sectionId;

                    boolean clutteredmap = MapSections.sectionOpenRatio[mysection] < 0.75;

                    //Bit extra score for good sections
                    totalscore +=  Math.min(400,MapSections.sectionSuspectedKarbo[mysection]) * 0.05;
                    totalscore +=  Math.min(700,MapSections.sectionSizes[mysection]) * 0.2;
                    totalscore +=  MapSections.sectionOpenRatio[mysection] * 12.0;
//                    Debug.log("Score5 : " + totalscore  + "  " + spotscore);
                    if(MapSections.sectionSizes[mysection] < MapSections.reachableTerrain / 8) {
                        if (MapSections.sectionSizes[mysection] < 15) {
                            totalscore -= 100;
                        }else{
                            totalscore -= 40;
                        }
                    }
//                    Debug.log("Score6 : " + totalscore  + "  " + spotscore);
                    if(R.turn < 500) {
                        if (MapSections.canEnemyReachSection[mysection]) {
                            totalscore += 50;
                        } else {
                            totalscore -= 50;
                        }
                    }else{
                        //Near the end of game, building where they can't reach us is an advantage, allows better rocket loading
                        if (MapSections.canEnemyReachSection[mysection]) {
                            totalscore -= 20;
                        } else {
                            totalscore += 20;
                        }
                    }
//                    Debug.log("Score7 : " + totalscore  + "  " + spotscore);

                    if(Map.myFactories.size() > 0) {
                        int closestdist = 100;
                        boolean foundsomethinginmysection = false;
                        double clusteredfactor = -0.4;

                        if(R.KNIGHTGAME && R.turn < 150){
                            clusteredfactor += 0.3; //on rush maps, build them more clustered, prevents getting too spread
                        }

                        if(clutteredmap){
                            clusteredfactor -= 1.4; //place them much further apart, don't want to get stuck
                        }

                        for (Factory f : Map.myFactories) {
                            int dist = Map.findDistanceTo(l, f.loc);

                            if (dist < closestdist) {
                                closestdist = dist;
                            }

                            spotscore += Math.max(-15, 10 - dist) * clusteredfactor;

                            if(f.loc.sectionId == mysection){
                                foundsomethinginmysection = true;
                            }
                        }

                        //factories everywhere plz
                        if(!foundsomethinginmysection){
                            if(MapSections.sectionSizes[mysection] > 100) {
                                totalscore += 100;
                            }else{
                                totalscore += 30;
                            }
                        }
                        //We'd like to be kind of close to one factory, but not to all of all clusters
                        spotscore += Math.max(-10, 10 - closestdist) * 0.6;
                    }else if(totalFactories == 0){
                        //First factory should 'always' be in the biggest section
                        totalscore +=  C.Clamp(MapSections.sectionSizes[mysection]-MapSections.largestSectionSize,-300,0) * 0.3;
                    }
//                    Debug.log("Score8 : " + totalscore  + "  " + spotscore);

                    if(!R.POWERSAVEMODUS) {
                        double freeTiles = 0;
                        int scarysnipespots = 0;

                        double factoriesalreadynear = 0;
                        ArrayList<MainLoc> nearTiles = new Circle(l,Type.RANGER.range).allContainingLocationsOnTheMap();
                        for (MainLoc m : nearTiles){
                            if(!m.isPassable){
                                freeTiles++;
                            }
                            if(!m.isWithinSquaredDistance(l,36)){
                                if(Map.findDistanceTo(l,m) > 15){
                                    //Spots within a rangers attack radius we can't defend well because it's hard to path to them
                                    scarysnipespots++;
                                }
                            }
                            if(m.containsRobot && m.getRobot().isMine && m.getRobot().amIFactory){
                                factoriesalreadynear++;
                            }
                        }

                        spotscore += (freeTiles * 1.2) / (factoriesalreadynear + 2.0);
                        spotscore -= Math.min(scarysnipespots * 5,70);


//                        Debug.log(l + " snipespots: " + scarysnipespots);

                    }else{
                        spotscore += 50;
                    }
//                    Debug.log("Score9 : " + totalscore  + "  " + spotscore);
//                    Debug.log( l + "Difficulty: " + Map.getNodeDifficultyToReach(l) + " acc: " + Map.getNodeAccessibleTiles(l));


                    Loc controlloc = MapMeta.getControlLocOf(l);
                    totalscore += C.Clamp(MapMeta.controlMap[controlloc.x][controlloc.y] * 1,-40,15);
                    totalscore +=  Math.min(80,R.turn - MapMeta.controlLastHasActivity[controlloc.x][controlloc.y]) * 0.25;
                    spotscore -=  MapMeta.getNearFullEstimate(l) * 50.0 ;
                    totalscore -=  Math.min(400,MapMeta.controlDanger[controlloc.x][controlloc.y]) * 0.1;

//                    Debug.log("Score10 : " + totalscore  + "  " + spotscore);
                    if(clutteredmap){
                        spotscore += (Map.getNodeAccessibleTiles(l) - 60) * 2.5;
                        spotscore +=  (Map.getNodeDifficultyToReach(l) - 14) * -2;
                    }else{
                        spotscore += (Map.getNodeAccessibleTiles(l) - 60) * 1.3;
                        spotscore +=  (Map.getNodeDifficultyToReach(l) - 14) * -0.5;
                    }

//                    Debug.log("Score11 : " + totalscore  + "  " + spotscore);
                    if(!R.POWERSAVEMODUSHYPER) {
                        for (Robot r : Map.getEnemiesInRange(new Circle(l, 60))) {
                            if (r.amIDps) {
                                totalscore -= 50;

                                if(r.amIKnight || r.amIMage){
                                    totalscore -= 50;
                                }

                                if (r.approxAttackPlusMoveCircle.contains(l)) {
//                                    if(GrandStrategy.strategy == GrandStrategy.KNIGHTRUSH) {
//                                        score -= (5 + r.damage * 0.1);
//                                    }else{
                                        totalscore -= (10 + r.damage * 0.4);
//                                    }
                                }
                            } else if (r.amIHealer) {
                                totalscore -= 5;
                            } else if(r.amIFactory){
                                totalscore -= 30;
                                //cant outcompete a completed factory
                            }
                            else{
                                totalscore -= 4;
                            }
                        }
                    }
//                    Debug.log("Score12 : " + totalscore  + "  " + spotscore);

                    //try to place factories where the workers are actually at to be able to build quickly
                    double workerdistfactor = 2;

                    if(R.turn < 100){
                        workerdistfactor += 2;
                    }
                    if(totalFactories <= 2){
                        workerdistfactor += 3;
                        if(totalFactories == 0){
                            workerdistfactor += 5;
                        }
                    }
                    if(clutteredmap){
                        //more important to pick good spots here rather than just dump it down whereever
                        workerdistfactor -= 1.5;
                    }





                    if(GrandStrategy.strategy == GrandStrategy.KNIGHTRUSH){
                        workerdistfactor += 3;
                    }
                    double workerscore = 0;
                    for (Worker w : Map.myWorkers) {
                        workerscore += 10 -  Math.min(10, Map.findDistanceTo(l, w.loc));
                    }
                    totalscore += Math.min(100,workerscore * workerdistfactor);

                    //Also give some extra weight to factories with passable neighbours, makes building easier
                    totalscore += Math.max(passablecount,5) * 3 * workerdistfactor;


//                    Debug.log("Score13 : " + totalscore  + "  " + spotscore);

                    totalscore -= Math.min(100,Map.karbonite[l.x][l.y]) * 0.2;

                    if(GrandStrategy.strategy == GrandStrategy.KNIGHTRUSH && Map.myFactories.size() == 0){
                        spotscore += Math.max(0, 10 - Map.findDistanceTo(l, Map.generallyPassableApproxCenter)) * -0.4;
                        spotscore += Math.max(0, 10 - l.distanceTo(Map.approxCenter)) * -0.4;

                    }else {
                        spotscore += Math.max(0, 30 - Map.findDistanceTo(l, Map.generallyPassableApproxCenter)) * -1.5;
                        spotscore += Math.max(0, 20 - l.distanceTo(Map.approxCenter)) * -1.5;
                    }
//                    Debug.log("Score14 : " + totalscore  + "  " + spotscore);


                    totalscore += spotscore;

//                    Debug.log( l + "Final : " + totalscore  + "  " + spotscore);

                    if (totalscore > bestlocscore && spotscore > neededspotscore) {
                        bestlocscore = totalscore;
                        bestLoc = l;
                    }

//                    Debug.log(score);
                }

//                if(bestLoc == null) {
//                    Debug.log("Needed: " + neededspotscore);
//                }

                if(bestLoc == null){
                    turnsUnableToFindFactorySpot++;
//                    Debug.log("Cant find okay loc");
                    DoThinking(attempt+1,banRocket,true,banunits,banworker);

                }else{

                    for(Robot r : Map.getAlliesInRange(new Circle(bestLoc,2),Type.WORKER)){
                        if(!r.workerIsOnCooldown){
                            turnsUnableToFindFactorySpot = 0;
                            ((Worker)r).buildFactory(bestLoc);
                            break;
                        }
                    }

                    if(R.karbonite >= 40){
                        DoThinking(attempt + 1,banRocket,true,banunits,banworker);
                    }
                }
            }
//            else{
//                Debug.log("Wait, not enough karbo for factory");
//            }

        }
        else if(unittype == Type.ROCKET.typeId){
            if(R.karbonite >= Type.ROCKET.cost) {
                ArrayList<Loc> possibleLocations = new ArrayList<>();

                //TODO? : account for workers ability to move before dumping down factory
                workerloop:
                for (Worker r : Map.myWorkers) {
                    if(!r.workerIsOnCooldown && !r.inSpace && !r.inGarrison && !r.amIExplorer) {

                        for (Loc l : r.adjacentPassableTiles) {
                            if(Map.robots[l.x][l.y] != null && Map.robots[l.x][l.y].amIBlueprint){
                                continue workerloop;
                            }
                        }
                        for (Loc l : r.adjacentPassableTiles) {
                            if (Map.robots[l.x][l.y] == null) {
                                possibleLocations.add(l);
                            }
                        }
                    }
                }

//                Debug.log("Rocket is best, we have karbo");
                double bestlocscore = 0;
                Loc bestLoc = null;
                for (Loc l : possibleLocations) {
                    double score = Math.min(60,Math.max( baseWeightRocket,30));
                    for (Loc l2 : l.adjacentTiles()) {
                        if(!l2.isPassable()){
                            score -= 3.5;
                        }else if(Map.robots[l2.x][l2.y] != null){
                            if(!Map.robots[l2.x][l2.y].amIWorker){
                                if(Map.robots[l2.x][l2.y].amIRocket){
                                    score -= 60;
                                }
                                else if(Map.robots[l2.x][l2.y].amIFactory){
                                    score -= 20;
                                }
                                else if(!Map.robots[l2.x][l2.y].amIWorker){
                                    score -= 2;
                                }
                            }
                        }
                    }

                    if(!R.POWERSAVEMODUSHYPER) {
                        for (Robot r : Map.getEnemiesInRange(new Circle(l, 60))) {
                            if (r.amIDps) {
                                score -= 5;
                                if (r.approxAttackPlusMoveCircle.contains(l)) {
                                    score -= (10 + r.damage * 0.2);
                                }
                            } else if (r.amIHealer) {
                                score -= 5;
                            } else if (R.turn > 50) {
                                score -= 2;
                            }
                        }

                        for(Rocket r : Map.myRockets){
                            score += Math.min(Map.findDistanceTo(l,r.loc),20) * 0.5;
                        }


                        score += Map.myDpsers.size() * 6;
                        for(Dpser d : Map.myDpsers){
                            score -= Math.min(Map.findDistanceTo(l,d.loc),30) * 0.4;
                        }
                    }



                    score += Math.max(-20,Math.min(20, MapMeta.getControlOf(l) * 2));

                    if (score > bestlocscore) {
                        bestlocscore = score;
                        bestLoc = l;
                    }
                }

                if(bestLoc == null){
//                    Debug.log("Cant find okay loc for rocket");
                    DoThinking(attempt+1,true,banFactory,banunits,banworker);

                }else{
//                    Debug.log("Rocket loc found: " + bestLoc);

                    for (Worker r : Map.myWorkers) {
                        if(!r.workerIsOnCooldown) {
                            if(r.loc.isWithinOffset(bestLoc,1)){
                                r.buildRocket(bestLoc);
                                break;
                            }
                        }
                    }
                    if(R.karbonite >= 40){
                        DoThinking(attempt + 1,true,banFactory,banunits,banworker);
                    }
                }
            }else{
//                Debug.log("Wait, not enough karbo for rocket");
            }
        }
        else if(unittype == Type.KNIGHT.typeId){
            if(R.karbonite >= Type.RANGER.cost || R.karbonite >= Type.KNIGHT.cost || R.karbonite >= Type.MAGE.cost || R.karbonite >= Type.HEALER.cost) {
                Factory bestFactry = null;
                double bestFactoryScore = -10000;

                for (Factory f : Map.myCompletedFactories) {
                    if (f.turnsLeftProduction <= 1 && Player.gc.canProduceRobot(f.id, UnitType.Mage)) {
                        double score = (1.0 - MapMeta.getNearFullEstimate(f.loc)) * 10.0;
                        //TODO: put a more reasonable goal here that actually works on disconnected maps
                        score -= Math.max(100,Map.findDistanceTo(f.loc,Map.generallyPassableApproxCenter));
                        score -= f.garrisonedWithin.size() * 10;

                        if(score > bestFactoryScore){
                            bestFactry = f;
                            bestFactoryScore = score;
                        }
                    }
                }

                if(bestFactry != null){
                    bestFactry.buildUnit();
                    if(R.karbonite >= 40){
                        DoThinking(attempt + 1,banRocket,banFactory,false,banworker);
                    }
                }else{
                    if(R.karbonite >= 40){
                        DoThinking(attempt + 1,banRocket,banFactory,true,banworker);
                    }
                }

            }


//            Debug.log("Build Unit");
        }
        Debug.endClock(8);




    }
}
