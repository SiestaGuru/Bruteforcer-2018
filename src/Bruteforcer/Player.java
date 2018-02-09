package Bruteforcer;// import the API.
// See xxx for the javadocs.
import bc.*;
import bc.Location;

import java.util.ArrayList;


//Just having player wrap stuff from the starterpackage
public class Player {

    public static GameController gc;

    private static long totalTime = 0;
    private static long lastGameTime = 0;

    private static ArrayList<Unit> garrisonedUnits = new ArrayList<>();

    public static PlanetMap earthMap;
    public static PlanetMap marsMap;

    public static void main(String[] args) {

        gc = new GameController();
        System.out.println("Version Name here");
        R.initGame();
        long lastspend = 0;

        while (true) {
            try {
                int timeleft = (int)gc.getTimeLeftMs();
                long round = gc.round();
                if(timeleft > 300) {
                    long manualtime = System.currentTimeMillis();

                    R.doTurn(round, timeleft);

                    if (round % 50 == 49) {
                        System.runFinalization(); //"Killed" prevention
                    }

                    if (Debug.release) {
//                        System.out.println("karbo: " + R.karbonite);
                        if (R.turn % 10 == 0) {
                            System.out.println("T" + R.turn + " time: " + timeleft + "    " + (timeleft - lastGameTime) + "  Avg per turn: " + ((double) ((timeleft - 10000) - (50 * R.turn))) / ((double) R.turn));

                        }
                        lastGameTime = timeleft;
                    }else{
//                        Debug.log("Amount: " + Robot.amountexecuted);
//                        System.out.println("T" + R.turn + " time: " + timeleft + "  Spend:  " +  -1 *( timeleft - (lastGameTime + 50))  + " real: " + lastspend    +  "  .next time:  "  +    ((-1 *( timeleft - (lastGameTime + 50))) -  lastspend)  );
                        lastGameTime = timeleft;
                        lastspend = (System.currentTimeMillis() - manualtime);
                    }
                }

            } catch (Exception ex){
                Debug.log(ex);
            }
            gc.nextTurn();
        }
    }


    public static void QueueMovementAction(Robot r, Direction dir){
        gc.moveRobot(r.id,dir);
    }



    public static void ParseMapInitial() {
        R.myPlanet = gc.planet();
        if (R.myPlanet == Planet.Earth) {
            R.oppositePlanet = Planet.Mars;
            R.amIEarth = true;
        } else {
            R.oppositePlanet = Planet.Earth;
            R.amIMars = true;
        }
        R.myTeam = gc.team();


        MarsStuff.op = gc.orbitPattern();
        MarsStuff.ap = gc.asteroidPattern();

        PlanetMap pm = gc.startingMap(R.myPlanet);

        marsMap = gc.startingMap(Planet.Mars);
        earthMap = gc.startingMap(Planet.Earth);

        Map.marsWidth = (int) marsMap.getWidth();
        Map.marsHeight = (int) marsMap.getHeight();


        Map.init((int) pm.getWidth(), (int) pm.getHeight());


        for (int x = 0; x < Map.width; x++) {
            for (int y = 0; y < Map.height; y++) {
                MapLocation m = new MapLocation(R.myPlanet, x, y);
                Map.blocked[x][y] = pm.isPassableTerrainAt(m) == 0;
                Map.locations[x][y].isPassable = !Map.blocked[x][y];
                Map.karbonite[x][y] = (int) pm.initialKarboniteAt(m);
                Map.initialTotalKarbo += Map.karbonite[x][y];
            }
        }


        Map.startingSpots = new ArrayList<>();
        VecUnit v = pm.getInitial_units();
        for (int i = 0; i < v.size(); i++) {
            Loc l2 = Loc.fromMapLocation(v.get(i).location().mapLocation());
            MainLoc l =  Map.locations[l2.x][l2.y];
            Map.startingSpots.add(l);
            if (v.get(i).team().equals(R.myTeam)) {
                Map.myStartingSpots.add(l);
            } else {
                Map.theirStartingSpots.add(l);
            }

            v.get(i).delete();
        }


        Map.approxCenter = new Loc(Map.width / 2, Map.height / 2);
        Map.generallyPassableApproxCenter = Map.approxCenter;
        //This because a couple of functions like having to path to the center
        if (!Map.generallyPassableApproxCenter.isPassable()) {
            Loc bestClosest = null;
            double bestsqr = -10000;
            for (int x = Map.approxCenter.x - 5; x <= Map.approxCenter.x + 5; x++) {
                for (int y = Map.approxCenter.y - 5; y <= Map.approxCenter.y + 5; y++) {
                    if (!Map.blocked[x][y]) {
                        int dx = Map.approxCenter.x - x;
                        int dy = Map.approxCenter.y - y;
                        int dist = dx * dx + dy * dy;
                        if (dist < bestsqr) {
                            bestsqr = dist;
                            bestClosest = Map.locations[x][y];
                        }
                    }

                }
            }
            if (bestClosest != null) {
                Map.generallyPassableApproxCenter = bestClosest;
            }
        }

        for (int x = 0; x < Map.width; x++) {
            for (int y = 0; y < Map.height; y++) {
                Map.locations[x][y].initAdjacent();
            }
        }

        MapSections.DoAnalysis();
        MarsStuff.initMarsPassability(marsMap);

        pm.delete();
        v.delete();
    }

    public static void ParseTurn() {

//        for(Robot r : Map.allRobots){
//            r.memoryClear();
//        }
        VecUnit v = gc.units();
        Map.myRobots.clear();
        Map.postponedBots.clear();
        Map.latecomers.clear();
        Map.botsJustUnloaded.clear();
        Map.myHealers.clear();
        Map.myWorkers.clear();
        Map.myKnights.clear();
        Map.myDpsers.clear();
        Map.myMages.clear();
        Map.myRangers.clear();
        Map.myFactories.clear();
        Map.myCompletedFactories.clear();
        Map.myCompletedRockets.clear();
        Map.myRockets.clear();
        Map.myDamagedRobots.clear();
        Map.launchingRockets.clear();
        Map.theirRobots.clear();
        Map.theirDps.clear();
        Map.theirMages.clear();
        Map.theirFactories.clear();
        Map.robots = new Robot[Map.width][Map.height];
        Map.myUnitCounts = new int[8];
        Map.myNonGarrisonedUnitCounts = new int[8];
        Map.theirUnitCounts = new int[8];
        Map.mySpaceCounts = new int[8];
        R.karbonite = (int)gc.karbonite();
        Map.allRobots.clear();
        Map.mySpaceUnits.clear();
        Map.totalUnitCount = 0;
        Worker.factoriesBeingConstructed.clear();
        Worker.rocketsBeingConstructed.clear();
        Map.globalDesire = new double[Map.width][Map.height];
        Map.myUsableTotalDpsCount = 0;
        Movement.globalAntiClusterGoalCount = 0;
        garrisonedUnits.clear();
        R.heavilystuck = false;
        R.unusedHealers.clear();
        Worker.nonsuicidalWorkersCount = 0;

        for(int x = 0; x < Map.width; x++){
            for(int y = 0; y < Map.height; y++){
                Map.locations[x][y].containsRobot = false;
            }
        }


        Map.totalUnitCount = (int)v.size();

        for (int i = 0; i <  v.size(); i++) {
            Unit u = v.get(i);
            parseUnit(u);
            if(!garrisonedUnits.contains(u)) {
                u.delete();
            }
           // Debug.log(Loc.fromMapLocation(v.get(i).location().mapLocation()));
        }


        for(Unit u : garrisonedUnits){
            Location iLoc = u.location();
            Robot r = Map.robotsById.get(u.id());


            if(iLoc.isInSpace()){
                if(r.isMine) {
                    Map.mySpaceUnits.add(r);
                    Map.mySpaceCounts[r.type.typeId]++;
                }
                r.inSpace = true;
                r.inGarrison = false;
            }
//            else if(r.amIStructure){
//                Debug.log("WAWEFAAGSFSASADGADAGGDA");
//            }
            else{
                Structure garrisonedIn = (Structure)Map.robotsById.get(iLoc.structure());

                garrisonedIn.garrisonedWithin.add(r);
                if(r.isMine){
                    Map.myRobots.add(r);
                    Map.myUnitCounts[r.type.typeId]++;

                    if(r.turnBorn == R.turn) {
                        //otherwise unit count for production gets annoying
                        Map.myNonGarrisonedUnitCounts[r.type.typeId]++;
                    }

                    if(r.amIHealer){
                        Map.myHealers.add((Healer)r);
                    }else if(r.amIRanger){
                        Map.myRangers.add((Ranger) r);
                        Map.myDpsers.add((Ranger) r);
                    } else if(r.amIWorker){
                        Map.myWorkers.add((Worker) r);
                    } else if(r.amIKnight){
                        Map.myKnights.add((Knight) r);
                        Map.myDpsers.add((Knight) r);
                    }else if(r.amIMage){
                        Map.myMages.add((Mage) r);
                        Map.myDpsers.add((Mage) r);
                    }
                    if(r.health < r.maxHealth){
                        Map.myDamagedRobots.add(r);
                    }
                }else{
                    Map.theirRobots.add(r);
                    if(r.amIDps) {
                        Map.theirDps.add(r);
                        if(r.amIMage){
                            Map.theirMages.add((Mage)r);
                        }
                    }
                    if(r.amIFactory){
                        Map.theirFactories.add((Factory)r);
                    }
                    Map.theirUnitCounts[r.type.typeId]++;
                }
                r.turnUpdate(garrisonedIn.loc, (int) u.health(), (int) u.movementHeat(), (int) u.attackHeat(), (int) u.abilityHeat());
                r.inGarrison = true;
                r.inSpace = false;

                Map.allRobots.add(r);
            }
            u.delete();
        }

        for(Robot r : Map.allRobots){
            r.updateInSight();
        }


        Map.myTotalUnitCount = Map.myRobots.size();
        Map.theirTotalUnitCount = Map.theirRobots.size();

//        Debug.log("their ghost images");
//        Debug.log(Map.theirGhostImages.keySet());

        v.delete();
    }



    //for garrisoned units, only do this start of turn
    public static Robot parseUnit(Unit u){
        int id = u.id();
        Robot r;
        if(Map.robotsById.containsKey(id)){
            r = Map.robotsById.get(id);
        }else{
            UnitType ut = u.unitType();
            boolean ismine = u.team() == R.myTeam;

            if(ut == UnitType.Factory){
                r = new Factory(id,ismine);
            } else if(ut == UnitType.Healer){
                r = new Healer(id,ismine);
            }else if(ut == UnitType.Mage){
                r = new Mage(id,ismine);
            }else if(ut == UnitType.Knight){

                r = new Knight(id,ismine);
            }else if(ut == UnitType.Worker){
                if(ismine) {
                    Map.myTotalWorkersBuilt++;
                }
                r = new Worker(id,ismine);
            }else if(ut == UnitType.Ranger){
                r = new Ranger(id,ismine);
            }else if(ut == UnitType.Rocket){
                r = new Rocket(id,ismine);
            } else{
                r = null;
            }

            if(ismine){
                r.myunitNr = Map.myTotalUnitsFinished;

                Map.myTotalUnitsFinished++;

                Map.finishedUnitCounts[r.type.typeId]++;

                if(r.amIStructure){
                    Map.myTotalStructuresBuilt++;
                }
            }else{
                if(r.amIFactory){
                    Map.theirFactoriesById.put(id,(Factory)r);
                }
            }
            r.turnBorn = R.turn;
            Map.robotsById.put(id,r);
        }

        Location iLoc = u.location();

        boolean ingarrison = false;



        if(!r.isMine){
            if(r.amIRanger){
                if(Techs.enemyTechRangers == 0){
                    if(u.movementCooldown() < Type.RANGER.moveCd) {
                        Techs.enemyTechRangers = 1;
                    }
                }
                if(Techs.enemyTechRangers == 1) {
                    if(u.visionRange() > Type.RANGER.sight) {
                        Techs.enemyTechRangers = 2;
                    }
                }
                if(Techs.enemyTechRangers == 2) {
                    if(r.abilityHeat > 0) {
                        Techs.enemyTechRangers = 3;
                    }
                }
            } else if(r.amIKnight){
                if(Techs.enemyTechKnights == 0){
                    if(u.knightDefense() > 5){
                        Techs.enemyTechKnights = 1;
                        Knight.theirKnightDefense = 10;
                    }
                }
                if(Techs.enemyTechKnights == 1){
                    if(u.knightDefense() > 10){
                        Techs.enemyTechKnights = 2;
                        Knight.theirKnightDefense = 15;
                    }
                }
                if(Techs.enemyTechKnights == 2){
                    if(u.abilityHeat() > 0){
                        Techs.enemyTechKnights = 3;
                        Knight.theirKnightDefense = 15;
                    }
                }
            } else if(r.amIMage){

                if(Techs.enemyTechMages == 0){
                    if(u.damage() > Type.MAGE.damage){
                        Techs.enemyTechMages = 1;
                        Mage.enemyMageDamage = Type.MAGE.damage + 15;
                    }
                }
                if(Techs.enemyTechMages == 1){
                    if(u.damage() > Type.MAGE.damage + 15){
                        Techs.enemyTechMages = 2;
                        Mage.enemyMageDamage = Type.MAGE.damage + 30;
                    }
                }
                if(Techs.enemyTechMages == 2){
                    if(u.damage() > Type.MAGE.damage + 30){
                        Techs.enemyTechMages = 3;
                        Mage.enemyMageDamage = Type.MAGE.damage + 45;
                    }
                }
                if(Techs.enemyTechMages == 3){
                    if(u.abilityHeat() > 0){
                        Techs.enemyTechMages = 4;
                    }
                }
            } else if(r.amIHealer){
                if(Techs.enemyTechHealers == 0){
                    if(u.damage() < -10){
                        Techs.enemyTechHealers = 1;
                    }
                }
                if(Techs.enemyTechHealers == 1){
                    if(u.damage() < -14){
                        Techs.enemyTechHealers = 2;
                    }

                    if(R.turn < 175){
                        Techs.enemyIsGoingForOvercharge = true;
                    }

                }
                if(Techs.enemyTechHealers == 2){

                    if(Techs.enemyIsGoingForOvercharge && R.turn > 220){
                        //We'll just assume they're going to be able to press the button soon
                        Techs.enemyCanOverCharge = true;
                    }

                    if(u.abilityHeat() > 0){
                        Techs.enemyTechHealers = 3;
                        Techs.enemyCanOverCharge = true;
                    }

                }


            }
        }


        r.isDead = false;

        if(r.amIStructure) {
            r.turnUpdate(Loc.fromMapLocation(iLoc.mapLocation()), (int)u.health(), 0, 0, 0);

            if(((Structure)r).rangerShootSpots == null){
                ((Structure)r).initstructure();
            }

            if (u.structureIsBuilt() == 1) {
                if (r.amIBlueprint) {




                    r.amIBlueprint = false;
                    if (r.isMine && r.amIRocket) {
                        Rocket.totalRocketsBuilt++;
                    }
                }

                if(r.amIFactory){
                    if(u.isFactoryProducing() == 1) {
                        ((Factory) r).turnsLeftProduction = (int) u.factoryRoundsLeft();
                    }else{
                        ((Factory) r).turnsLeftProduction = -1;
                    }
                }

                if(r.isMine){
                    if (r.amIFactory) {
                        Map.myFactories.add((Factory) r);
                        ((Factory) r).canProduceSomething =  u.isFactoryProducing() == 0 || u.factoryRoundsLeft() == 0;

                        if(!r.amIBlueprint){
                            Map.myCompletedFactories.add((Factory) r);
                        }
                    }else{
                        Map.myRockets.add((Rocket) r);
                        if(!r.amIBlueprint){
                            Map.myCompletedRockets.add((Rocket) r);
                        }
                    }
                }
            } else {
                if(r.isMine) {

                    if (r.amIFactory) {
                        Worker.factoriesBeingConstructed.add((Factory) r);
                    } else if (!u.location().isInSpace()) {
                        Worker.rocketsBeingConstructed.add((Rocket) r);
                    }
                }
            }
            ((Structure)r).garrisonedWithin.clear();


        }else{
            if(iLoc.isInGarrison() || iLoc.isInSpace()) {
                garrisonedUnits.add(u);
                ingarrison = true;
            }else {
                if(r.amIDps){
                    Map.myUsableTotalDpsCount++;
                }
                r.turnUpdate(Loc.fromMapLocation(u.location().mapLocation()), (int) u.health(), (int) u.movementHeat(), (int) u.attackHeat(), (int) u.abilityHeat());
            }
        }

        if(r.isMine){
            Map.totalUnitCount++;
        }
        if(!ingarrison) {
            r.inSpace = false;
            r.inGarrison = false;

            if (r.isMine) {
                Map.myRobots.add(r);
                Map.myUnitCounts[r.type.typeId]++;
                Map.myNonGarrisonedUnitCounts[r.type.typeId]++;
                if(r.amIHealer){
                    Map.myHealers.add((Healer)r);
                } else if(r.amIRanger){
                    Map.myRangers.add((Ranger)r);
                    Map.myDpsers.add((Ranger)r);
                } else if(r.amIWorker){
                    r.workerIsOnCooldown = false;
                    Map.myWorkers.add((Worker)r);

                    if(((Worker) r).goKillYourself == 0.0){
                        Worker.nonsuicidalWorkersCount++;
                    }

                } else if(r.amIKnight){
                    Map.myKnights.add((Knight)r);
                    Map.myDpsers.add((Knight)r);
                } else if(r.amIMage){
                    Map.myMages.add((Mage) r);
                    Map.myDpsers.add((Mage)r);
                }
                if(r.health < r.maxHealth){
                    Map.myDamagedRobots.add(r);
                }

            } else {
                Map.theirRobots.add(r);

                if(r.amIDps){
                    Map.theirDps.add(r);
                    if(r.amIMage){
                        Map.theirMages.add((Mage)r);
                    }
                }
                if(r.amIFactory){
                    Map.theirFactories.add((Factory)r);
                }

                Map.theirUnitCounts[r.type.typeId]++;
            }
            Map.allRobots.add(r);
            Map.robots[r.loc.x][r.loc.y] = r;
            Map.locations[r.loc.x][r.loc.y].containsRobot = true;
        }


        for(int i = 0; i < Map.theirUnitCounts.length; i++){
            Map.theirRollingUnitCounts[i] = Math.max(Map.theirRollingUnitCounts[i] * 0.99, Map.theirUnitCounts[i] );
        }


       if(!r.isMine){
            Map.theirGhostImages.remove(r.id);
       }

        return r;
    }


    public static boolean UseLocationAbility(Robot r, Abilities a, Loc l){
        Direction dir;
       switch (a) {
           case BUILD_FACTORY:

               dir = l.getDirectionFrom(r.loc);
               if(gc.karbonite() >= 100) {
                   if (gc.canBlueprint(r.id, UnitType.Factory, dir)) {
//                       System.out.println("------ Build a factory ---");
                       gc.blueprint(r.id, UnitType.Factory, dir);
                       R.karbonite -= Type.FACTORY.cost;

                       Factory dummy = new Factory(-500 - R.turn,true);
                       dummy.turnUpdate(l,0,0,0,0);
                       Worker.factoriesBeingConstructed.add(dummy);
                       Map.robots[l.x][l.y] = dummy;
                       Map.locations[l.x][l.y].containsRobot = true;

                       r.workerIsOnCooldown = true;
                       return true;
                   }else{
                       Debug.log("cant blueprint on: " + l);
                   }
               }else{
                   Debug.log("not enough karbo error");
               }

               break;
           case BUILD_ROCKET:

               dir = l.getDirectionFrom(r.loc);
               gc.blueprint(r.id, UnitType.Rocket, dir);
               R.karbonite -= Type.ROCKET.cost;

               Rocket dummy = new Rocket(-500 - R.turn, true);
               dummy.turnUpdate(l, 0, 0, 0, 0);
               Map.robots[l.x][l.y] = dummy;
               Map.locations[l.x][l.y].containsRobot = true;
               Worker.rocketsBeingConstructed.add(dummy);

               r.workerIsOnCooldown = true;
               return true;

           case HARVEST:
               dir = l.getDirectionFrom(r.loc);
               if(gc.canHarvest(r.id,dir)) {
                   gc.harvest(r.id, dir);

                   int harvested = Math.min(Worker.karbonitePerHarvest,Map.karbonite[l.x][l.y]);
                   R.karbonite += harvested;
                   Map.totalKarboHarvested += harvested;
                   Map.karbonite[l.x][l.y] -= harvested;
                   R.weGainedKarboniteThisTurn = true;
                   r.workerIsOnCooldown = true;
                   return true;
               }
               break;
           case SNIPE:
               if(gc.canBeginSnipe(r.id,l.toMapLocation())) {
                   gc.beginSnipe(r.id, l.toMapLocation());
                   return true;
               }
               break;
           case  REPLICATE:
               dir = l.getDirectionFrom(r.loc);
               if(gc.canReplicate(r.id,dir)) {
                   gc.replicate(r.id, dir);
                   r.abilityHeat += Type.WORKER.activeCd;
                   R.karbonite -= 60;
                   Map.myUnitCounts[Type.WORKER.typeId]++;
                   Map.myNonGarrisonedUnitCounts[Type.WORKER.typeId]++;



                   if(gc.hasUnitAtLocation(l.toMapLocation())){
                       Unit u =  gc.senseUnitAtLocation(l.toMapLocation());
                       Robot spawnedUnit = parseUnit(u);
                       if(spawnedUnit != null) {
                           spawnedUnit.turnUpdate(l, Type.WORKER.maxHealth, 0, 0, 10);
                           Map.robots[l.x][l.y] = spawnedUnit;
                           Map.locations[l.x][l.y].containsRobot = true;
                           Map.latecomers.add(spawnedUnit);

                           ((Worker)spawnedUnit).assignedKarboSectiongoal = ((Worker)r).assignedKarboSectiongoal;
                       }
                   }
//                   else{



//                   Worker wdummy = new Worker(-500 - R.turn,true);
//                   wdummy.turnUpdate(l,0,0,0,0);
//                   Map.robots[l.x][l.y] = wdummy;
               }
               break;
           case  BLINK:
//               if(gc.canBlink(r.id,l.toMapLocation())) {
                   gc.blink(r.id, l.toMapLocation());
                   r.abilityHeat += Type.MAGE.activeCd;
                   r.updateLocation(Map.locations[l.x][l.y],true,true);
                  return true;
//               }
//               break;

       }
       return false;
    }


    public static boolean UseTargetedAbility(Robot r, Abilities a, int targetId){

        switch (a){
            case CONSTRUCT:
                if(gc.canBuild(r.id,targetId)){
                    gc.build(r.id,targetId);
                    r.workerIsOnCooldown = true;
                    r.lastConstructed = R.turn;

                    r.health = Math.min(r.health + 5,r.maxHealth);

                    if(r.health >= r.maxHealth){
                        r.amIBlueprint = false;
                        if(r.amIFactory){
                            ((Factory)r).canProduceSomething = true;
                            ((Factory)r).turnsLeftProduction = 0;
                        }
                    }

                    return true;
                }
                break;
            case REPAIR:
                if(gc.canRepair(r.id,targetId)){
                    gc.repair(r.id,targetId);
                    r.workerIsOnCooldown = true;
                    r.lastConstructed = R.turn;
                    Robot target =  Map.robotsById.get(targetId);
                    target.health =  Math.min(target.health + Worker.repairBuildings,target.maxHealth);
                    return true;
                }
                break;
            case JAVELIN:
                if(gc.canJavelin(r.id,targetId)) {
                    gc.javelin(r.id, targetId);
                    r.abilityHeat += Type.KNIGHT.activeCd;
                    return true;
                }
            case OVERCHARGE:
                if(gc.canOvercharge(r.id,targetId)) {
                    gc.overcharge(r.id, targetId);
                    r.abilityHeat += Type.HEALER.activeCd;
                    return true;
                }
        }
        return false;
    }
}