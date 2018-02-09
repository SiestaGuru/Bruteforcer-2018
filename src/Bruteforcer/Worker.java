package Bruteforcer;

import java.util.ArrayList;

public class Worker extends Robot {


    public static ArrayList<Factory> factoriesBeingConstructed = new ArrayList<>();
    public static ArrayList<Rocket> rocketsBeingConstructed = new ArrayList<>();

    public static int karbonitePerHarvest = 3;
    public static int repairBuildings = 10;

    public boolean amIExplorer;

    public MainLoc assignedKarboSectiongoal = null;//for use in workerkarbosearch
    public int currentlySittingOnKarboSection = 0;//for use in workerkarbosearch
    public double karboInCurrentSection = 0;//for use in workerkarbosearch

    public double goKillYourself = 0.0;
    public static int nonsuicidalWorkersCount;

    public boolean replicateOrdered = false;


    public Worker(int id, boolean isMine){
        super(Type.WORKER,id,isMine);
        amIWorker = true;
        amIProducer = true;

//        if(R.turn < 70 && (Map.myTotalWorkersBuilt % 5 == 0) && Map.totalSuspectedReachableKarbonite > 250 && (Map.width + Map.height) > 55){
//            amIExplorer = true;
//        }




//        if(amIExplorer){
//            Debug.log("Explorer!" + this);
//        }
    }

    @Override
    public void init(){}
    @Override
    public void preturn(){
        replicateOrdered = false;
        goKillYourself = Math.max(0,goKillYourself - 0.1);
    }


    boolean foundgoal;

    @Override
    public void think() {
//        Debug.log("Think start");

        if (amIExplorer && R.turn - turnBorn > 70) {
            amIExplorer = false;
        }

        foundgoal = false;

        boolean enemymagenear = false;
        for(Robot r : Map.getEnemiesInRange(new Circle(loc,50))){
            if(r.amIMage){
                enemymagenear = true;
            }
        }

        tryActions();

        if (!amIExplorer) {
            Structure bestBuilding = null;

            double bestBuildingScore;

            if (Map.myCompletedFactories.size() <= 2) {
                bestBuildingScore = 4;
            } else {
                bestBuildingScore = 5;
            }

            if (R.POWERSAVEMODUSHYPER) {
                if (rocketsBeingConstructed.size() > 0) {
                    bestBuilding = rocketsBeingConstructed.get(0);
                }
            } else {
                for (Factory f : factoriesBeingConstructed) {

                    double dist = Map.findDistanceTo(loc, f.loc);
                    if(dist < 200) {
                        double helpercount = f.alreadyComingInToBuild.size() + f.alreadyComingInToBuildLast.size();
                        for (Loc l : f.adjacentPassableTiles) {
                            Robot rob = l.getRobot();
                            if (rob != null && rob.amIWorker && rob.isMine && !(f.alreadyComingInToBuild.contains((Worker)rob) || f.alreadyComingInToBuildLast.contains((Worker)rob))) {
                                helpercount++;
                            }
                        }
                        double timeleft = 40;
                        if (helpercount != 0) {
                            timeleft = (f.maxHealth - f.health) / (helpercount * Worker.repairBuildings * 0.8);
                        }

                        double score = 40 - dist * 2;


                        if (R.turn > 30 || dist > 20) {
                            if (helpercount > 2 && dist > 7) {
                                score += timeleft - 40;
                            }
                        }

                        boolean alreadygoing = f.alreadyComingInToBuildLast.contains(this);
                        if(alreadygoing){
                            score += 10;
                        }
                        if (helpercount > 4 &&  !alreadygoing) {
                            if(dist > 4) {
                                score -= 50;
                            }
                            if(helpercount > f.adjacentPassableTiles.length){
                                score -= 50;
                            }
                        }

                        if(helpercount > loc.adjacentPassable.length){
                            score -= 10;
                        }

                        if (dist < 2) {
                            score += 10;
                        }

                        if (score > bestBuildingScore) {
                            bestBuilding = f;
                            bestBuildingScore = score;
                        }
                    }
                }
            }


            if (R.POWERSAVEMODUSHYPER) {
                if (rocketsBeingConstructed.size() > 0) {
                    bestBuilding = rocketsBeingConstructed.get(0);
                }
            } else {
                for (Rocket r : rocketsBeingConstructed) {
                    if (r.amIBlueprint && r.health < r.maxHealth) {
                        double dist = Map.findDistanceTo(loc, r.loc);
                        if(dist < 200) {
                            double helpercount = r.alreadyComingInToBuild.size() + r.alreadyComingInToBuildLast.size();
                            for (Loc l : r.adjacentPassableTiles) {
                                Robot rob = l.getRobot();
                                if (rob != null && rob.amIWorker && rob.isMine && !(r.alreadyComingInToBuild.contains((Worker)rob) || r.alreadyComingInToBuildLast.contains((Worker)rob))) {
                                    helpercount++;
                                }
                            }
                            double timeleft = 40;
                            if (helpercount != 0) {
                                timeleft = (r.maxHealth - r.health) / (helpercount * Worker.repairBuildings * 0.8);
                            }


                            double score = 50 - dist * 2;

                            if (helpercount > 2) {
                                score += timeleft - 40;
                            }

                            boolean alreadygoing = r.alreadyComingInToBuildLast.contains(this);
                            if(alreadygoing){
                                score += 10;
                            }
                            if (helpercount > 4 &&  !alreadygoing) {
                                if(dist > 4) {
                                    score -= 50;
                                }
                                if(helpercount > r.adjacentPassableTiles.length){
                                    score -= 50;
                                }
                            }



                            if (R.turn > 600) {
                                score += 50;
                            }

                            if (score > bestBuildingScore) {
                                bestBuilding = r;
                                bestBuildingScore = score;
                            }
                        }
                    }
                }
            }

            if (bestBuilding != null) {

                bestBuilding.alreadyComingInToBuild.add(this);

                M.addGoal(bestBuilding.loc, 10);
                M.addCircle(bestBuilding.loc, 2, 200);

                if (loc.isWithinOffset(bestBuilding.loc, 1)) {
                    for (Robot r : Map.getRobotsInRangeFilterId(new Circle(loc, 8), id)) {
                        if (r.id != bestBuilding.id) {
                            M.addCircle(r.loc, 2, -30); //try to spread aorund the building
                        }
                    }
                }

                foundgoal = true;
//                    if(R.turn > 125 && R.turn < 150){
//                        Debug.log(this + "  Building");
//                    }

            }
        }

        boolean hypervigilant = false;
        if(R.amIEarth && Map.myFactories.size() <= 1 && R.turn > 130 && Map.myUsableTotalDpsCount < 5 ){
            hypervigilant = true;
            if(!R.hyperVigilanceHasBeenTriggered){
                R.hyperVigilanceHasBeenTriggered = true;
                Debug.log("Hyper vigilant triggered");
            }

        }

        if(hypervigilant && !foundgoal){
            M.addVector(Map.approxCenter,-20);
            M.addGoal(Map.approxCenter,-10);
            foundgoal = true;
        }


        if (R.amIEarth || R.turn < 700) {
            if(hypervigilant) {
                addDangerDesire(-15,health + 1,-1);
            }
            else if(goKillYourself > 2){
                addDangerDesire(0,health + 1,1);
            }
            else if(R.turn > 100){
                addDangerDesire(-5,health + 1,0);
            }
        }





        Factory bestRepairFactory = null;
        if (canMove()) {

            if (!R.POWERSAVEMODUSHYPER && !amIExplorer && !enemymagenear) {
                double bestRepairScore = -4;
                for (Factory f : Map.myCompletedFactories) {
                    if (f.health < f.maxHealth) {

                        int dist = Map.findDistanceTo(loc, f.loc);
                        if(dist < 7) {
                            double score = (f.maxHealth - health) * 0.1 - dist;

                            int spotsfree = 0;
                            for(Loc l : f.adjacentPassableTiles){
                                if(l.containsRobot() && l.getRobot().id != id){
                                    score -= 0.5;
                                }else{
                                    spotsfree++;
                                }
                            }

                            if(spotsfree == 0 && f.garrisonedWithin.size() >0){
                                score -= 15;
                            }

                            if (dist <= 2) {
                                score += 5;
                            }

                            if (score > bestRepairScore) {
                                bestRepairScore = score;
                                bestRepairFactory = f;
                            }
                        }
                    }
                }
                if (bestRepairFactory != null) {


                    M.addGoal(bestRepairFactory.loc, 15.0);
                    M.addCircle(bestRepairFactory.loc, 2, 60);

                    foundgoal = true;

//                    if(R.turn > 125 && R.turn < 150){
//                        Debug.log(this + "  Repairing");
//                    }

                }
            }

            if(!foundgoal){
                if(assignedKarboSectiongoal != null){
                    if(Map.findDistanceTo(loc,assignedKarboSectiongoal) > 2){
                        M.addGoal(assignedKarboSectiongoal, 15);
                        foundgoal = true;
//                        Debug.log(" W " + loc + " -> " + assignedKarboSectiongoal);
                    }
                }
            }

            if (!foundgoal) {
                double best = -15 - loc.distanceTo(Map.approxCenter) * 0.25;
                Loc goFor = null;

                int size = 7;
                if (amIExplorer) {
                    size = 15;
                }

                if (R.POWERSAVEMODUSHYPER) {
                    size = 3;
                } else if (R.POWERSAVEMODUS) {
                    size = 6;
                }

                for (int x1 = Math.max(x - size, 0); x1 <= Math.min(x + size, Map.widthMinusOne); x1++) {
                    for (int y1 = Math.max(y - size, 0); y1 <= Math.min(y + size, Map.heightMinusOne); y1++) {
                        if (Map.karbonite[x1][y1] > 0) {
                            double dist = ((double) Map.findDistanceTo(loc, Map.locations[x1][y1]));


                            double score;

                            if (amIExplorer) {
                                score = 50 + Map.karbonite[x1][y1] * 2 - dist * 0.2;
                                if (MapMeta.getControlOf(Map.locations[x1][y1]) > 5) {
                                    score -= 25;
                                }
                            } else if (dist <= 2) {
                                score = 50 + Map.karbonite[x1][y1] * 2;
                            } else {
                                score = Map.karbonite[x1][y1] - 2 * Math.pow(dist, 1.3);
                            }

                            score -= Map.locations[x1][y1].distanceTo(Map.approxCenter) * 0.25;

                            if (score > best) {
                                best = score;
                                goFor = Map.locations[x1][y1];
                            }
                        }
                    }
                }

                if (goFor != null) {

                    if (amIExplorer) {
                        M.addGoal(goFor, 8);
                    } else {
                        M.addGoal(goFor, 20);
                        M.addCircle(goFor,2,30);
                        foundgoal = true;
//                    if(R.turn > 125 && R.turn < 150){
//                        Debug.log(this + "  KArbonite  " +  goFor);
//                    }
                    }
                }
            }


            if(R.SUICIDERUSHWORKERS || goKillYourself > 1){
                int best = 50;
                Loc gotospot = null;
                for(Robot r: Map.theirDps){
                    int dist = Map.findDistanceTo(loc,r.loc);
                    if(dist < best){
                        best = dist;
                        gotospot = r.loc;
                    }
                }
                if(gotospot != null){
                    M.addGoal(gotospot,2000);
                }else{
                    Loc l = getControlMapGoal(100, 0, 0, 20);
                    M.addGoal(l,2000);
                }

            }else {
                if (!foundgoal) {
                    Loc l;
                    if (R.amIEarth || R.turn < 700) {
                        if (amIExplorer) {
                            l = getControlMapGoal(-1, 5, 3 + Construction.turnsUnableToFindFactorySpot * 0.3, 0.1);

                        } else {
                            l = getControlMapGoal(-3, 5, 1.5 + Construction.turnsUnableToFindFactorySpot * 0.3, 1);
                        }
                    } else {
                        l = getControlMapGoal(0, 3, 1 + Construction.turnsUnableToFindFactorySpot * 0.3, 1);
                    }
//            if(R.turn > 125 && R.turn < 150) {
//                Debug.log(this + " -> " + l);
//            }
                    M.addGoal(l, 20);
                    //Movement.addGoal(getRandomGoal(), 5);
                }
            }
//        else if(R.turn > 125 && R.turn < 150){
//            Debug.log(this + "  Some other goal");
//        }


//        Debug.log("scary: " + scaryEnemyRobotsNear.size());


            if(hypervigilant){

                for(Robot r: Map.getAlliesInRange(new Circle(loc,50))){
                    M.addVector(r.loc,-5);
                }


                for(Robot r: Map.getEnemiesInRange(new Circle(loc,120))){
                    M.addVectorCircle(r.sightCircle,-300,-20);
                    if(r.amIDps){
                        M.addVectorCircle(r.approxAttackPlusMoveCircle, -1000, -20);
                    }
                    M.addVector(r.loc,-6);
                }
            }else {

                if(goKillYourself < 2) {
                    for (Robot r : scaryEnemyRobotsNear) {
                        double fleedesire = r.damage;
                        if (r.enemyRobotsInAttackRange.size() > 0) {
                            fleedesire /= r.enemyRobotsInAttackRange.size();
                        }
                        if (R.turn < 90) {
                            if (foundgoal) {
                                fleedesire *= 0.3;
                            } else {
                                fleedesire *= 0.5;
                            }
                        }


                        if (!r.amIMage) {
                            for (Robot f : r.attackCircle.containingAlliedRobots(Type.FACTORY)) {
                                fleedesire *= 0.5; //gotta repair
                            }
                        }

                        M.addVectorCircle(r.approxAttackPlusMoveCircle, -10 * fleedesire, -fleedesire * 0.5);
                    }
                }


                if(enemymagenear || R.SPREAD) {
                    double amount  = -20;
                    if(enemymagenear){
                        amount = -100;
                    }
                    for (Robot r : Map.getAlliesInRange(new Circle(loc, 8))) {
                        M.addVector(r.loc, -5);
                        M.addCircle(r.loc, 1, amount);
                    }
                }
            }

            int count = 0;
            for (Robot r : robotsInSight) {
                if (r.isMine) {

                    if (count++ < 15) {
                        //To prevent workers from getting everyone else stuck
                        M.addVector(r.loc, -1);

                        if (r.amIFactory && !r.amIBlueprint  && r.adjacentPassableTiles.length - r.loc.getAdjacentRobots().size() <= 2) {
                            M.addCircle(r.loc, 2, -20);
                        }
                    }
                }
            }


            M.calcBestMove();
            doMove();
        }

        if (!R.POWERSAVEMODUSHYPER) {
            tryActions();
        }

        if(replicateOrdered){
            replicate();
        }



//        if(bestRepairFactory != null && !workerIsOnCooldown) {
//            if (bestRepairFactory.loc.isWithinOffset(loc, 1)) {
//                if (Player.UseTargetedAbility(this, Abilities.REPAIR, bestRepairFactory.id)) {
//                    bestRepairFactory.health += repairBuildings;
//                }
//            }
//        }

    }



    public void replicate(){
        Loc firsstep = null;

        if(assignedKarboSectiongoal != null) {
            firsstep = Map.findPathToNextStep(loc, assignedKarboSectiongoal);
        }
        if (firsstep != null && firsstep.isEmpty() && !firsstep.equals(loc)) {
            Player.UseLocationAbility(this, Abilities.REPLICATE, firsstep);
        }else {
            Loc l = Map.findPathToNextStep(loc, Map.generallyPassableApproxCenter);
            if (l != null && l.isEmpty() && !l.equals(loc)) {
                Player.UseLocationAbility(this, Abilities.REPLICATE, l);
            } else {
                for (Loc l2 : adjacentPassableTiles) {
                    if (!l2.containsRobot()) {
                        if (Player.UseLocationAbility(this, Abilities.REPLICATE, l2)) {
                            break;
                        }

                    }
                }
            }
        }

    }

    public void buildFactory(Loc l){
        if (Player.UseLocationAbility(this, Abilities.BUILD_FACTORY, l)) {
//            Debug.log("Build a factory");
            if(canMove()) {
                M.addCircle(l, 2, 50);
            }
        }
    }

    public void buildRocket(Loc l){
        if (Player.UseLocationAbility(this, Abilities.BUILD_ROCKET, l)) {
            if(canMove()) {
                M.addCircle(l, 2, 50);
            }
        }
    }





    public void tryActions() {
//        if (id == 4){
//            for(Robot r: Map.myRobots){
//                Debug.log(r);
//
//                Debug.log(Map.robots[r.x][r.y]);
//                Debug.lineBreak();
//            }
//
//
//        }


//        boolean shouldReplicate = false;
//        if(R.turn - Construction.HoldUpBoysIveGotSomethingReallyImportantToDoRightHere > 5 ) {
//            if (canUseAbility() && R.karbonite >= 15) {
//                int karboestimate = MapMeta.getNearKarboniteEstimate(loc);
//                if (R.amIEarth) {
//                    if (MapMeta.getNearFull(loc,5) < 0.6 && Map.myUnitCounts[Type.WORKER.typeId] < 7 && (R.turn < 5  || (scaryEnemyRobotsNear.size() == 0  && Map.myUnitCounts[Type.WORKER.typeId] < Map.totalUnitCount / 8))) {
//                        shouldReplicate = true;
//                    }
//                    else if(karboestimate > 100){
//                        int nearby = 0;
//                        for(Robot r : Map.myWorkers){
//                            if(loc.isWithinDistance(r,14)){
//                                nearby++;
//                            }
//                        }
//                        if(karboestimate > nearby * 40){
//                            shouldReplicate = true;
//                        }
//                    }
//
//                } else {
//                    if (R.turn > 750 || scaryEnemyRobotsNear.size() == 0 && (Map.myUnitCounts[Type.WORKER.typeId] < 5 || (Map.myUnitCounts[Type.WORKER.typeId] < 8 && MapMeta.getNearKarboniteEstimate(loc) > 80))) {
//                        shouldReplicate = true;
//                    }
//                }
//            }
//        }
//        if(shouldReplicate){
//            Loc l = Map.findPathToNextStep(loc, Map.approxCenter);
//            if (l != null && l.isEmpty()) {
//                Player.UseLocationAbility(this, Abilities.REPLICATE, l);
//            } else {
//                for (Loc l2 : adjacentTiles) {
//                    if (l2.isEmpty()) {
//                        if (Player.UseLocationAbility(this, Abilities.REPLICATE, l2)) {
//                            break;
//                        }
//
//                    }
//                }
//            }
//        }

        if(R.amIEarth) {

//            if (Techs.canBuildRocket) {
//                if (rocketsBeingConstructed.size() != 0) {
//                    Debug.log("construct");
//                }
//                if (MapMeta.getControlOf(loc) <= 1) {
//                    Debug.log("control");
//                }
//                if (Map.myUsableTotalDpsCount <= 15) {
//                    Debug.log("dps");
//                }
//                if (Rocket.totalRocketsBuilt != 0) {
//                    Debug.log("no rockets");
//                }
//            }

//            if (Techs.canBuildRocket && Map.myUsableTotalDpsCount - 6*(Map.myRockets.size() + Worker.rocketsBeingConstructed.size()) > 5  && (R.turn > 600 || ((rocketsBeingConstructed.size() == 0 || R.turn > 550) && MapMeta.getControlOf(loc) > 1 && Map.myUsableTotalDpsCount > 20  && (R.turn > 450 || Rocket.totalRocketsBuilt == 0)))) {
//                boolean nextToRocket = false;
//                for (Robot r : Map.getRobotsInRange(new Circle(loc, 2))) {
//                    if (r.amIRocket && r.amIBlueprint) {
//                        nextToRocket = true;
//                        break;
//                    }
//
//                }
//                if (!nextToRocket) {
//                    //TODO: check for disconnected map
//                    if (R.karbonite >= Type.ROCKET.cost) {
//                        //First exploratory rocket
//                        for (Loc l : adjacentTiles) {
//                            if (l.isEmpty()) {
//                                if (Player.UseLocationAbility(this, Abilities.BUILD_ROCKET, l)) {
//                                    Construction.HoldUpBoysIveGotSomethingReallyImportantToDoRightHere = -100000;
//                                    if (canMove()) {
//                                        M.addCircle(l, 2, 20);
//                                    }
//                                    break;
//                                }
//                            }
//                        }
//                    } else {
//                        Construction.HoldUpBoysIveGotSomethingReallyImportantToDoRightHere = R.turn;
//                    }
//                }
//            }

            if (!workerIsOnCooldown) {
                for (Robot r : robotsInSight) {
                    if (r.amIStructure &&  !r.amIBlueprint && r.isMine && r.health < r.maxHealth) {
                        if (abilityCircle.contains(r.loc)) {
                            if (Player.UseTargetedAbility(this, Abilities.REPAIR, r.id)) {
                                break;
                            }
                        }
                    }
                }
            }

            if (!workerIsOnCooldown) {
                for (Robot r : robotsInSight) {
                    if (r.amIBlueprint && r.isMine) {
                        if (abilityCircle.contains(r.loc)) {
                            if (Player.UseTargetedAbility(this, Abilities.CONSTRUCT, r.id)) {
                                break;
                            }
                        }
                    }
                }
            }






//            if (R.turn - Construction.HoldUpBoysIveGotSomethingReallyImportantToDoRightHere > 5) {
//                int factcount = Map.myUnitCounts[Type.FACTORY.typeId];
//
//                int closestFactoryDistance = 9999999;
//                for (Factory f : Map.myFactories) {
//                    int dist = Map.findDistanceTo(loc, f.loc);
//                    if (dist < closestFactoryDistance) {
//                        closestFactoryDistance = dist;
//                    }
//                }
//
//                if (!workerIsOnCooldown && Map.myUnitCounts[Type.FACTORY.typeId] < 6 && (factoriesBeingConstructed.size() == 0 || closestFactoryDistance > 25)) {
//                    if (R.karbonite >= Type.FACTORY.cost && ((R.turn < 40 && factcount <= 2) || (scaryEnemyRobotsNear.size() == 0 && (
//                            factcount < 3 ||
//                                    (R.karbonite > 160 && factcount <= 6 && closestFactoryDistance > 5) ||
//                                    (factcount < 8 && closestFactoryDistance > (Map.width + Map.height) / 3) ||
//                                    (factcount <= 4 && Map.myUnitCounts[Type.FACTORY.typeId] < Map.myTotalUnitCount / 5.0))))) {
//
//                        for (Loc l : adjacentTiles) {
//                            if (l.isEmpty()) {
//                                if (Player.UseLocationAbility(this, Abilities.BUILD_FACTORY, l)) {
//                                    M.addCircle(l, 2, 20);
//                                    break;
//                                }
//
//                            }
//                        }
//                    }
//                }
//            }


        }

        if(!workerIsOnCooldown){

            //Determines whether we actually want to harvest
            //If we have way too much karbonite, and this karbonite is essentially free to grab, it's better to wait till later
            //So we don't waste basic income
            // 1 karbonite grabbed later may actually be better than 2 karbo grabbed now
            boolean allow = Map.totalVisibleKarbonite > 2500 || R.turn < 40 || health < maxHealth || scaryEnemyRobotsNearSize >0 || R.karbonite < 80 || Map.myUnitCounts[Type.FACTORY.typeId] + Worker.factoriesBeingConstructed.size() <= 4 || (R.karbonite < 300 && !Construction.wantToMakeUnits );
            if(!allow){
                Loc controlloc = MapMeta.getControlLocOf(loc);

                if(R.turn - MapMeta.controlLastHasActivity[controlloc.x][controlloc.y] < 6){
                    allow = true;
                }

                if(!allow){
                    allow = sightCircle.containingEnemyRobots(Type.WORKER).size() > 0;
                }
            }



            if(allow && R.karbonite < 60000) {
                int best = 0;
                Loc bestloc = null;

                for (Loc l : adjacentTilesIncludingThis) {
                    if (Map.karbonite[l.x][l.y] > best) {
                        best = Map.karbonite[l.x][l.y];
                        bestloc = l;

                    }
                }
                if (bestloc != null) {
                    Player.UseLocationAbility(this, Abilities.HARVEST, bestloc);

                }
            }
//            else {
//                Debug.log("Waiting with harvesting");
//            }
        }



    }
}
