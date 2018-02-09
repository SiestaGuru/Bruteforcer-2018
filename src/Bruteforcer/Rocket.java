package Bruteforcer;

import bc.MapLocation;
import bc.Planet;
import bc.PlanetMap;

import java.util.ArrayList;
import java.util.HashSet;

public class Rocket extends Structure {

    public static int totalRocketsBuilt = 0;
    public static int rocketGarrisonCap = 8;


    private HashSet<Integer> lastTurnClosestRobots = new HashSet();


    private int timesAttemptedLoad = 0;
    private int timesCouldntLoad = 0;

    public Rocket( int id, boolean isMine){
        super(Type.ROCKET,id,isMine);
        amIRocket = true;
        amIBlueprint = true;
    }

    @Override
    public void init(){}
    @Override
    public void preturn() {
        if (R.amIEarth ) {
//            Debug.log(garrisonedWithin);
            int stillneeded = 0;
            if (!amIBlueprint) {
                if (garrisonedWithin.size() < rocketGarrisonCap && R.turn < 749) {
                    if (R.turn > 550) {
                        for (Robot r :  loc.getAdjacentFriendlyRobots()) {
                            if (!r.amIStructure) {
                                if(!r.amIWorker || R.turn > 735) {
                                    if (r.canMove() && Player.gc.canLoad(id,r.id)) {
                                        timesAttemptedLoad++;
                                        Player.gc.load(id, r.id);
                                        Map.robots[r.x][r.y] = null;
                                        Map.locations[r.x][r.y].containsRobot = false;
                                        r.updateLocation(loc, false, false);
                                        r.inGarrison = true;
                                        garrisonedWithin.add(r);
                                        r.moveHeat += 10;
                                        if (garrisonedWithin.size() >= rocketGarrisonCap) {
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }


                    int[] desiredContents = new int[5];

                    if (totalRocketsBuilt <= 1) {
                        if (R.KNIGHTGAME) {
                            if(Map.myUnitCounts[Type.WORKER.typeId] > 0) {
                                desiredContents[Type.WORKER.typeId] = 1;
                            }
                            desiredContents[Type.KNIGHT.typeId] = 3;
                            desiredContents[Type.RANGER.typeId] = 1;
                            desiredContents[Type.HEALER.typeId] = 2;
                        } else {
                            if(Map.myUnitCounts[Type.WORKER.typeId] > 0) {
                                desiredContents[Type.WORKER.typeId] = 1;
                            }
                            desiredContents[Type.RANGER.typeId] = 3;
                            desiredContents[Type.HEALER.typeId] = 1;
                        }


                    } else {
                        desiredContents[Type.RANGER.typeId] = rocketGarrisonCap;
                        desiredContents[Type.MAGE.typeId] = rocketGarrisonCap;
                        desiredContents[Type.KNIGHT.typeId] = rocketGarrisonCap;
                        if (R.turn < 740) {
                            desiredContents[Type.HEALER.typeId] = 2;
                        } else {
                            desiredContents[Type.HEALER.typeId] = rocketGarrisonCap;
                            desiredContents[Type.WORKER.typeId] = rocketGarrisonCap;
                        }
                    }

                    for (Robot r : garrisonedWithin) {
                        desiredContents[r.type.typeId]--;
                    }

                    HashSet<Integer> robotsFound = new HashSet<>();
                    for (int m = 0; m < desiredContents.length; m++) {
                        int i = desiredContents[m];
                        if (i > 0) {
                            stillneeded += i;

//                Debug.log("Still need type: " + m);


                            for (int j = i; j >= 1; j--) {
                                int bestDist = 25;
                                Robot closestRobot = null;

                                for (Robot r : Map.myRobots) {
                                    if (!r.inGarrison && r.type.typeId == m) {
                                        if (!robotsFound.contains(r.id)) {
                                            int dist = Map.findDistanceTo(r.loc, loc);

                                            if (lastTurnClosestRobots.contains(r.id)) {
                                                dist -= 3;
                                            }

                                            if (r.rocketCaller != null && !loc.isWithinOffset(r.loc, 1)) {
                                                dist += 30;
                                            }

                                            if (dist < 2) {
                                                if (r.canMove()) {
                                                    dist -= 5;
                                                }
                                            }


                                            if (dist < bestDist) {
                                                bestDist = dist;
                                                closestRobot = r;
                                            }
                                        }
                                    }
                                }
                                if (closestRobot != null) {
//                                Debug.log(this + " -> " + closestRobot);
                                    closestRobot.rocketCaller = this;
                                    if (closestRobot.loc.isWithinOffset(loc, 1) && !amIBlueprint) {
//                            Debug.log("Trying to load: " + closestRobot);
//                                    if (Player.gc.canLoad(id, closestRobot.id)) {
                                        if (closestRobot.canMove() && Player.gc.canLoad(id, closestRobot.id)) {
                                            timesAttemptedLoad++;
                                            closestRobot.moveHeat += 10;
                                            Player.gc.load(id, closestRobot.id);
                                            Map.robots[closestRobot.x][closestRobot.y] = null;
                                            Map.locations[closestRobot.x][closestRobot.y].containsRobot = false;
                                            closestRobot.updateLocation(loc, false, false);
                                            closestRobot.inGarrison = true;
                                            garrisonedWithin.add(closestRobot);
                                            stillneeded--;


                                        } else {
                                            timesCouldntLoad++;
                                            closestRobot.M.addCircle(loc, 2, 10000);
//                                Debug.log("CANT LOAD"  + garrisonedWithin.size());
                                        }

                                    } else {

                                        if (amIBlueprint) {

                                            if (R.turn > 650) {
                                                closestRobot.M.addGoal(loc, 300);
                                            } else {
                                                closestRobot.M.addGoal(loc, 150);
                                            }
                                            closestRobot.M.addCircle(loc, 2, -100);
                                            robotsFound.add(closestRobot.id);
                                        } else {
                                            if (R.turn > 650) {
                                                closestRobot.M.addGoal(loc, 500);
                                            } else {
                                                closestRobot.M.addGoal(loc, 250);
                                            }
                                            closestRobot.M.addCircle(loc, 2, 10000);
                                            robotsFound.add(closestRobot.id);
                                        }


//                            Debug.log("Trying to pull: " + closestRobot);
                                    }
                                } else {
                                    break;
                                }
                            }
                        }
                    }
                    lastTurnClosestRobots = robotsFound;

                }


//
//            Debug.log("fly time: " + MarsStuff.getArrivalTimeIfLaunchAt(R.turn + 1));
//        Debug.log(garrisonedWithin);


                if (!amIBlueprint) {
                    boolean normallaunch = (MapMeta.anyActivityDetected || R.turn >= 747)&&  (stillneeded == 0  ||  (R.turn - turnBorn > 150 && garrisonedWithin.size() > 2));
                    boolean emergencylaunch = R.turn >= 747 ||  ((health < 60 || (health < 100 && scaryEnemyRobotsNearSize > 0)) && garrisonedWithin.size() > 0);


                    if (emergencylaunch || normallaunch) {
                        //EVACUATE THE AREA
                        for(Loc l : adjacentPassableTiles){
                            Map.globalDesire[l.x][l.y] -= 2000;
                        }
                    }
                    if(!emergencylaunch && normallaunch){
                        if(MarsStuff.op.duration(R.turn) + 1 <= MarsStuff.op.duration(R.turn+1)){
                            normallaunch = false; //wait till the shortest time till launch
                        }
                    }



                    if (normallaunch || emergencylaunch) {
//                Debug.log("HIT THE LAUNCH BUTTON DAMMIT");
                        for (Loc l : adjacentPassableTiles) {
                            Map.globalDesire[l.x][l.y] -= 10000;
                        }
                        Map.launchingRockets.add(this);
                    }
                }

            }
        }
    }
    @Override
    public void think(){



    }




    public void launch(){
        Debug.log("LAAAUNCHING " + loc + "   " + timesAttemptedLoad + "-" + timesCouldntLoad  + " units: " + garrisonedWithin.size() );
//        Debug.log(garrisonedWithin);


        for(Robot r : garrisonedWithin){
            r.inSpace = true;
        }


        int turn = MarsStuff.getArrivalTimeIfLaunchAt(R.turn);



        int launchid = -1;


        int counter = 0;
        int size = MarsStuff.passableMarsLocs.size();

        if(size > 0) {

            while (!isValidLoc(launchid, turn, counter++)) {
                launchid = PseudoRandom.next(size);

//            launchloc = new Loc(PseudoRandom.next(Map.marsWidth),PseudoRandom.next(Map.marsHeight));
            }

            Loc launchloc = MarsStuff.passableMarsLocs.get(launchid);


            if (MarsStuff.rocketslandingon[turn] == null) {
                MarsStuff.rocketslandingon[turn] = new ArrayList<>();
            }
            MarsStuff.rocketslandingon[turn].add(launchloc);

            Player.gc.launchRocket(id, launchloc.toMarsMapLocation());
        }else{
            Debug.log("No passable mars tiles?");
        }
    }

    public boolean isValidLoc(int index, int turn, int counter) {
        if(index < 0 )return false;
        if (counter > 400) return true;

        Loc loc = MarsStuff.passableMarsLocs.get(index);

        if(index % 5 == 0) {
            Debug.log("Testing: " + loc);
        }


        if (counter < 150) {
            if (MarsStuff.sectionSizes[MarsStuff.sectionMap[loc.x][loc.y]] < 12) {
                return false;
            }
        } else if (counter < 300) {
            if (MarsStuff.sectionSizes[MarsStuff.sectionMap[loc.x][loc.y]] < 4) {
                return false;
            }
        }

        if (counter < 250) {
            if (MarsStuff.rocketslandingon[turn] != null) {
                for (Loc l : MarsStuff.rocketslandingon[turn]) {
//                    Debug.log("in the way: " + l);
                    if (l.isWithinOffset(loc, 2)) {
                        return false;
                    }
                }
            }
            if (MarsStuff.rocketslandingon[turn - 1] != null) {
                for (Loc l : MarsStuff.rocketslandingon[turn - 1]) {
                    if (l.isWithinOffset(loc, 2)) {
                        return false;
                    }
                }
            }
            if (MarsStuff.rocketslandingon[turn - 2] != null) {
                for (Loc l : MarsStuff.rocketslandingon[turn - 2]) {
                    if (l.isWithinOffset(loc, 2)) {
                        return false;
                    }
                }
            }
            if (MarsStuff.rocketslandingon[turn - 3] != null) {
                for (Loc l : MarsStuff.rocketslandingon[turn - 3]) {
                    if (l.isWithinOffset(loc, 2)) {
                        return false;
                    }
                }
            }
            if (MarsStuff.rocketslandingon[turn - 4] != null) {
                for (Loc l : MarsStuff.rocketslandingon[turn - 2]) {
                    if (l.isWithinOffset(loc, 2)) {
                        return false;
                    }
                }
            }
            if (MarsStuff.rocketslandingon[turn + 1] != null) {
                for (Loc l : MarsStuff.rocketslandingon[turn + 1]) {
                    if (l.isWithinOffset(loc, 2)) {
                        return false;
                    }
                }
            }
            if (MarsStuff.rocketslandingon[turn + 2] != null) {
                for (Loc l : MarsStuff.rocketslandingon[turn + 2]) {
                    if (l.isWithinOffset(loc, 1)) {
                        return false;
                    }
                }
            }
            if (MarsStuff.rocketslandingon[turn + 3] != null) {
                for (Loc l : MarsStuff.rocketslandingon[turn + 3]) {
                    if (l.isWithinOffset(loc, 1)) {
                        return false;
                    }
                }
            }
            if (MarsStuff.rocketslandingon[turn + 4] != null) {
                for (Loc l : MarsStuff.rocketslandingon[turn + 4]) {
                    if (l.isWithinOffset(loc, 1)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }




}