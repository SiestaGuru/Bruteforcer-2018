package Bruteforcer;

//import bc.Direction;

import bc.Direction;

import java.util.ArrayList;
import java.util.HashSet;

public class Robot {

    public MainLoc loc;
    public final int cost;
    public final int maxHealth;
    public final int damage;
    public final int attackRange;
    public int sightRange;
    public int moveCd;
    public final int attackCd;
    public final int abilityCd;
    public final int abilityRange;
    public final Type type;
    public final int id;

    public int turnLastSeen;
    public int health;
    public int expectedHealthAfterVolley;
    public int moveHeat;
    public int attackHeat;
    public int abilityHeat;

    public int turnBorn;

    public boolean isMine;
    public boolean isDead;

    public Loc bestMove;

    public Movement M;


    public Circle attackCircle;
    public Circle approxAttackPlusMoveCircle;
    public Circle sightCircle;
    public Circle abilityCircle;


    public boolean amIMage;
    public boolean amIKnight;
    public boolean amIWorker;
    public boolean amIHealer;
    public boolean amIRanger;
    public boolean amIRocket;
    public boolean amIFactory;
    public boolean amIStructure;
    public boolean amIProducer;
    public boolean amIDps;
    public boolean amIBlueprint;


    public int lastConstructed = -1000;

    public int theoreticalDamage = 0;
    public int theoreticalDamageIncludingNextTurn = 0;


    public int myunitNr;

    public int x;
    public int y;

    public boolean workerIsOnCooldown = false;
    public boolean inSpace = false;
    public boolean inGarrison = false;
    public boolean justUnloaded = false;

    public ArrayList<Robot> robotsInSight  = new ArrayList<>();
    public ArrayList<Robot> robotsInAttackRange  = new ArrayList<>();
    public ArrayList<Robot> enemyRobotsInAttackRange  = new ArrayList<>();
    public ArrayList<Robot> enemyRobotsInApproxAttackRange  = new ArrayList<>();
    public ArrayList<Robot> robotsInApproxAttackRange  = new ArrayList<>();
    public ArrayList<Robot> robotsInAbilityRange  = new ArrayList<>();





    public ArrayList<Robot> scaryEnemyRobotsNear = new ArrayList<>();
    public int scaryEnemyRobotsNearSize = 0;


    public MainLoc[] adjacentTiles;
    public MainLoc[] adjacentPassableTiles;
    public MainLoc[] adjacentTilesIncludingThis;
    public MainLoc[] adjacentPassableTilesIncludingThis;

    public int lastSeenEnemy = 0;
    public int lastShotAtEnemy = 0;
    public int lastMoved = 0;

    public Rocket rocketCaller;

    public boolean postponedUnit = false;
    public boolean hasDoneTurn = false;

    public static long amountexecuted = 0;


    public int expectedRemainingHealthMagePlan;
    public double initialDmgScore;

    public Robot(Type t, int id, boolean isMine){
        type = t;
        cost = t.cost;
        maxHealth = t.maxHealth;
        damage = t.damage;
        attackRange = t.range;
        sightRange = t.sight;
        moveCd = t.moveCd;
        attackCd = t.attackCd;
        abilityCd = t.activeCd;
        abilityRange = t.activeRange;
        lastMoved = R.turn;
        this.id = id;
        this.isMine = isMine;
        M = new Movement(this);

    }

    public void turnUpdate(Loc loc, int health, int moveHeat, int attackHeat, int abilityHeat){
        //  if(id == 4){Debug.log("Turn update");}
//        Debug.log("myloc: " + loc  +   "   " + R.turn);

        rocketCaller = null;
        turnLastSeen = R.turn;
        this.moveHeat = moveHeat;
        this.health = health;
        this.attackHeat = attackHeat;
        this.abilityHeat = abilityHeat;
        updateLocation(loc,false, false);

        bestMove = null;
        scaryEnemyRobotsNear.clear();
        scaryEnemyRobotsNearSize = 0;
        justUnloaded = false;
        M.reset();

        if(amIStructure){
            ((Structure)this).alreadyComingInToBuildLast = ((Structure)this).alreadyComingInToBuild;
            ((Structure)this).alreadyComingInToBuild = new HashSet<>();
        }
    }


    public boolean canAttack(){
        return attackHeat < 10 && !inSpace && !inGarrison;
    }
    public boolean canMove(){
        return moveHeat < 10 && !inSpace;
    }
    public boolean canUseAbility(){
        return abilityHeat < 10 && !inSpace && !inGarrison;
    }


    public void updateLocation(Loc newSpot, boolean updateMap, boolean updateInSight){

        if(!newSpot.equals(this.loc)){
            lastMoved = R.turn;
        }

        if(updateMap){
            Map.robots[x][y] = null;
            Map.locations[x][y].containsRobot = false;
        }
        loc = Map.locations[newSpot.x][newSpot.y];
        x = loc.x;
        y = loc.y;

        if(updateMap) {
            Map.robots[x][y] = this;
            Map.locations[x][y].containsRobot = true;
        }

        attackCircle = new Circle(loc,attackRange);
        abilityCircle = new Circle(loc, abilityRange);
        sightCircle = new Circle(loc, sightRange);

//        double approxAttackMove = LookupSqrt.sqrt[attackRange] + 1.5;


        if(moveHeat < 10 || !isMine){
            approxAttackPlusMoveCircle = new Circle(loc,type.atPlusMoveSquaredApprox);
        }else{
            approxAttackPlusMoveCircle = new Circle(loc,attackRange);
        }


//        approxAttackPlusMoveCircle = new Circle(loc, (int)(approxAttackMove * approxAttackMove));


        adjacentTiles = loc.adjacentTiles();
        adjacentPassableTiles = loc.adjacentPassableTiles();
        adjacentTilesIncludingThis = loc.adjacentTilesIncludingThis();
        adjacentPassableTilesIncludingThis = loc.adjacentPassableTilesIncludingThis();

        M.myLocation = loc;

        if(updateInSight) {
            updateInSight();
        }
    }


    public void stayAliveOverchargeRequests(){
        if(!Techs.canOverload || hasDoneTurn || MapMeta.overchargeSupportArray[x][y] == 0 || canMove()) return;

        if(health <= MapMeta.enemyDamageArrayReal[x][y]){
//            Debug.log("Probably dying");

            double lowest = 9999999;
            Loc hint = null;


            for(MainLoc l : loc.adjacentPassable){
                if(!l.containsRobot){
                    double damage =  MapMeta.enemyDamageArrayReal[l.x][l.y];

                    if(health > damage){

                        if(damage < lowest){
                            lowest = damage;
                            hint = l;
                        }


                    }
                }
            }
            if(hint != null){
//                Debug.log("Found escape " + hint);
                R.overchargeReqs.add(new OverchargeRequest(this,hint));
            }

        }
    }

    public void updateTheoreticalDamage(){
        if(!R.POWERSAVEMODUSHYPER) {
            theoreticalDamage = 0;
            theoreticalDamageIncludingNextTurn = 0;
            boolean willBeableToMoveNext = moveHeat < 10;

            ArrayList<Robot> plzMove = new ArrayList<>();

            double highestoverchargedamage = 0;

            for (Robot r : scaryEnemyRobotsNear) {

                boolean canattackthisturn = r.attackHeat < 10 && !r.hasDoneTurn;


                if (r.attackCircle.contains(this)) {
                    int realdamage = r.damage;
                    if(r.amIMage && r.isMine){
                        if(Techs.techMage >= 3){
                            realdamage = Type.MAGE.damage + 45;
                        } else if(Techs.techMage == 2){
                            realdamage = Type.MAGE.damage + 30;
                        } else if(Techs.techMage == 1){
                            realdamage = Type.MAGE.damage + 15;
                        }
                    }

                    if(canattackthisturn) {
                        theoreticalDamage += realdamage;
                    }
                    theoreticalDamageIncludingNextTurn += realdamage;

                    if(Techs.canOverload){
                        highestoverchargedamage = Math.max(highestoverchargedamage, realdamage * Math.min(5, MapMeta.overchargeSupportArray[r.x][r.y]));
                    }

                } else if(r.moveHeat < 10  || (!willBeableToMoveNext && r.moveHeat < 20)){
                    for (Loc l : r.loc.adjacentPassable) {
                        if (l.isWithinSquaredDistance(loc, r.attackRange)) {

                            int realdamage = r.damage;
                            if(r.amIMage  && r.isMine){
                                if(Techs.techMage >= 3){
                                    realdamage = Type.MAGE.damage + 45;
                                } else if(Techs.techMage == 2){
                                    realdamage = Type.MAGE.damage + 30;
                                } else if(Techs.techMage == 1){
                                    realdamage = Type.MAGE.damage + 15;
                                }
                            }

                            //ignored robots that can be in the way
                            if(l.containsRobot()){
                                if(r.moveHeat < 10 && canattackthisturn) {
                                    theoreticalDamage += realdamage * 0.3;
                                }
                                theoreticalDamageIncludingNextTurn += realdamage * 0.3;
                            }else {
                                if(r.moveHeat < 10 && canattackthisturn) {
                                    theoreticalDamage += realdamage;
                                }
                                theoreticalDamageIncludingNextTurn += realdamage;
                            }

                            if(l.containsRobot()){
                                plzMove.add(l.getRobot());
                            }

//                            if (!R.CrawlMode && l.containsRobot()) {
//                                Robot moveplz = l.getRobot();
//                                if (moveplz.isMine) {
//                                    moveplz.M.addSpecialLocation(l, -20);
//                                }
//                            }

                            break;
                        }
                    }
                }


            }

            theoreticalDamage += highestoverchargedamage;
            theoreticalDamageIncludingNextTurn += highestoverchargedamage;

            if(theoreticalDamage > health){
                //just to help a little, not overdoing it, because this method may be called multiple times per unit
                //makes robots move out of the spots we need to use to damage the enemy
                for(Robot r  : plzMove){
                    if(r.isMine){
                        r.M.addSpecialLocation(r.loc,-10);
                    }
                }
            }



                //This stuff has a wrong assumption: no enemy healers
                //Either the scary unit must be able to attack this turn, or it must be able to attack next turn while our target is frozen
//                if ((!r.hasDoneTurn && r.canAttack()) || (!willBeableToMoveNext && r.attackHeat < 20)) {
//
//                    //If we can attack straight away, great
//                    if (r.attackCircle.contains(this)) {
//                        theoreticalDamage += r.damage;
//                    }
//                    //Else, our target must be able to move to hit the unit this turn or the next
//                    else if(r.moveHeat < 10 ||  (!willBeableToMoveNext && r.moveHeat < 20)  ) {
//                        if (r.approxAttackPlusMoveCircle.contains(loc)) {
//                            for (Loc l : r.loc.adjacentPassable) {
//                                if (l.isWithinSquaredDistance(loc, r.attackRange)) {
//                                    theoreticalDamage += r.damage;
//                                    break;
//                                }
//                            }
//                        }
//                    }
//                }


//            if (theoreticalDamage > 0) {
//                Debug.log(theoreticalDamage + " on: " + this);
//            }
        }

    }

    public void updateInSight(){
        robotsInSight = Map.getRobotsInRangeFilterId(sightCircle, id);
        if (amIDps || amIHealer) {
            if (sightRange == attackRange) {
                robotsInAttackRange = (ArrayList<Robot>) robotsInSight.clone();
                enemyRobotsInAttackRange.clear();

                for(Robot r : robotsInAttackRange){
                    if(!r.isMine){
                        enemyRobotsInAttackRange.add(r);
                    }
                }
            } else {
                robotsInAttackRange.clear();
                enemyRobotsInAttackRange.clear();
                for (Robot r : robotsInSight) {
                    if (attackCircle.contains(r.loc)) {
                        robotsInAttackRange.add(r);
                        if(!r.isMine){
                            enemyRobotsInAttackRange.add(r);
                        }
                    }
                }
            }
            robotsInApproxAttackRange = Map.getRobotsInRangeFilterId(approxAttackPlusMoveCircle,id);
            enemyRobotsInApproxAttackRange = Map.getEnemiesInRange(approxAttackPlusMoveCircle);
        }

        if (!amIRanger && !amIStructure) {
            robotsInAbilityRange.clear();
            for (Robot r : robotsInSight) {
                if (abilityCircle.contains(r.loc)) {
                    robotsInAbilityRange.add(r);
                }
            }

        }

        if(amIDps){

            if(amIKnight){
                for (Robot r : Map.getRobotsInRange(new Circle(loc,13))  ) {
                    if (r.isMine != isMine) {
                        if (!r.scaryEnemyRobotsNear.contains(this)) {
                            r.scaryEnemyRobotsNear.add(this);
                            r.scaryEnemyRobotsNearSize++;
                        }
                    }
                }
            }else {
                for (Robot r : robotsInApproxAttackRange) {
                    if (r.isMine != isMine) {
                        if (!r.scaryEnemyRobotsNear.contains(this)) {
                            r.scaryEnemyRobotsNear.add(this);
                            r.scaryEnemyRobotsNearSize++;
                        }
                    }
                }
            }
        }



    }


    //TODO: have units enter garrisons if they haven't done anything for a few rounds
    public void calcMove(){
        Debug.beginClock(9);
        M.calcBestMove();
        Debug.endClock(9);
    }
    public void doMove(){
        if(canMove()) {


            boolean allowmove = true;



            if (M.bestmovescore < -800 && !postponedUnit) {
//                    Debug.log(this + "IM IN PERIL");
                ArrayList<Robot> robots = loc.getAdjacentFriendlyRobots();
                if(robots.size() > 0){
                    double newbest = M.bestmovescore;
                    Loc pleaseDontStandHere = null;
                    for(Robot r : robots){
                        if(!r.amIStructure && r.canMove() && !r.hasDoneTurn) {
                            double newscore = M.evaluateSpot(r.loc,false);
                            if(newscore > newbest){
                                newbest = newscore;
                                pleaseDontStandHere = r.loc;
                            }
                        }
                    }

                    if(newbest > M.bestmovescore + 250){
                        for(Robot r :  pleaseDontStandHere.getAdjacentRobots()){
                            if(r.id != id){
//                                    Debug.log(id + ":  Please don't stand on " + pleaseDontStandHere + " mr: " + r.id);
                                r.M.addSpecialLocation(pleaseDontStandHere,(newbest - M.bestmovescore) * 0.4);
                            }
                        }
                        Map.postponedBots.add(this);
                        M.reset();
                        allowmove = false;
                        postponedUnit = true;
                    }
                }
            }
            if (bestMove != null) {

                if(allowmove) {
                    Direction dir = bestMove.getDirectionFrom(loc);

                    if (dir != null && !dir.equals(Direction.Center)) {
                        try {

                            if (Map.robots[bestMove.x][bestMove.y] == null) {


                                if(inGarrison){
                                    Structure garrison = (Structure)Map.robots[x][y];
                                    if(Player.gc.canUnload(garrison.id,dir)){
                                        Player.gc.unload(garrison.id, dir);
                                        garrison.garrisonedWithin.remove(this);

                                        moveHeat = moveCd;
                                        updateLocation(bestMove, false, true);
                                        Map.robots[bestMove.x][bestMove.y] = this;
                                        Map.locations[bestMove.x][bestMove.y].containsRobot = true;
                                        inGarrison = false;
                                    }else{
                                        Debug.log(type.toString() + "Std says cant unload to " + bestMove);
                                    }
                                }
                                else {
                                    if (Player.gc.canMove(id, dir)) {
                                        Player.QueueMovementAction(this, dir);
                                        moveHeat = moveCd;
                                        updateLocation(bestMove, true, true);

                                    } else {
                                        Debug.log(type.toString() + "Std says cant move to " + bestMove);
                                    }
                                }


                            } else if (Map.robots[bestMove.x][bestMove.y].amIStructure) {
                                if (amIDps) {
                                    ((Dpser) this).doDps(null);
                                }
                                Player.gc.load(Map.robots[bestMove.x][bestMove.y].id, id);
                                Map.robots[x][y] = null;
                                Map.locations[x][y].containsRobot = false;
                                updateLocation(bestMove, false, true);
                                ((Structure) Map.robots[bestMove.x][bestMove.y]).garrisonedWithin.add(this);
                                inGarrison = true;
                            }

                        } catch (Exception ex) {

                            if(inGarrison){
                                Debug.log(ex);
                            }else {


                                int realX = Player.gc.unit(id).location().mapLocation().getX();
                                int realY = Player.gc.unit(id).location().mapLocation().getY();
                                int realX2 = Player.gc.unit(id).location().mapLocation().add(dir).getX();
                                int realY2 = Player.gc.unit(id).location().mapLocation().add(dir).getY();


                                if (realX2 != bestMove.x || realY2 != bestMove.y) {
                                    Debug.log(type.toString() + "  Best move " + dir.toString() + " is not correct??" + loc + " -->  " + realX2 + ", " + realY2 + "  != " + bestMove.x + "," + bestMove.y);
                                }
                                if (realX != x || realY != y) {
                                    Debug.log(type.toString() + "X /  Y are not correct?" + realX + ", " + realY + "  != " + x + "," + y);
                                }

                                if (ex.toString().contains("not empty")) {
                                    Debug.log(type.toString() + " Tried Movement: " + loc + " -> " + bestMove + " but not empty");
                                } else {
                                    Debug.log(ex);
                                    Debug.log(type.toString() + " Tried Movement: " + loc + " -> " + bestMove + " but failed");
                                }
//                        Debug.log(type.toString() + " Tried Movement: "  + Player.gc.unit(id).location().mapLocation().toString() + " -> " +  Player.gc.unit(id).location().mapLocation().add(dir).toString());
                            }

                        }

                        moveHeat += 10;

                    }
                }
            }

        }
    }




    public void addDangerDesire(double dangerratio, double cutoff, double visionDanger){
        M.dangerCutOff = cutoff;
        M.deathPenalty = 5000 * dangerratio;
        M.dangerDesireFactor = dangerratio * 0.2  *  (1.1- (((double)health)/((double)maxHealth))  );

        if(!R.POWERSAVEMODUS) {
            M.invisibleTileRangerRadius = visionDanger;
        }

        if(health < maxHealth){
            M.healerDesireFactor = 8 + (maxHealth - health) * 0.1;

            if(health < Type.RANGER.damage * 2){
                M.healerDesireFactor += 30;
            }
        }
    }

    public boolean addFindHealerGoal(double force, boolean allowGoingForFactory){
        if(MapMeta.healerSupportArray[x][y] > 0) return false;

        double bestdist = 15;
        Loc bestLoc = null;

        Healer healmeplz = null;

        for(Healer h : Map.myHealers){
            double d = Map.findDistanceTo(loc,h.loc);
            if(d < bestdist){
                bestdist =d;
                bestLoc = h.loc;
                healmeplz = h;
            }
        }

        if(bestLoc == null && allowGoingForFactory){
            for(Factory f : Map.myFactories){
                double d = Map.findDistanceTo(loc,f.loc);
                if(d < bestdist){
                    bestdist =d;
                    bestLoc = f.loc;
                    healmeplz = null;
                }
            }
        }

        if(bestLoc != null){
            M.addGoal(bestLoc, force);
            if(healmeplz != null){
                healmeplz.heallocRequests.add(loc);
            }
            return true;
        }
        return false;

    }
    public void basePreTurn(){
        postponedUnit = false;
        hasDoneTurn = false;
        if(scaryEnemyRobotsNearSize >0 ) lastSeenEnemy = R.turn;
    }
    public void baseThink(){

    }

    @Override
    public String toString(){
        return type.toString() + "(" + id + ")  [" + x + "," + y + "]" +  health +  "/" + attackHeat + "/" + moveHeat + "/" + abilityHeat   +  "  " +  (isMine?"M":"E") +  (amIBlueprint?"B":"") + (isDead?"D":"") +  (inGarrison?"G":"") +  (inSpace?"D":"");


    }


    //To be used for dpsers, not healers/workers
    public Loc getControlMapGoal(double weightStrife, double weightKarbonite, double weightFreeSpace, double distweight){

        if(R.POWERSAVEMODUSHYPER){
            return getRandomGoal();
        }

        Loc bestLoc = null;
        double bestscore = -999999999;

        Loc currentzone = MapMeta.getControlLocOf(loc);

        Debug.beginClock(11);
        for(int x = 0; x < MapMeta.controlColumns; x++){
            for(int y = 0; y < MapMeta.controlRows; y++){
                if(MapMeta.controlIsReachable[x][y]){
                    //TODO: Add Mage blink stuff
                    Loc controlloc = MapMeta.controlLocations[x][y];
                    int dist  = Map.findDistanceTo(loc,controlloc);

                    if(dist > 4 && dist < 100){
                        double score = Math.max(dist * distweight,-100);

                        double control = Math.max(-40,Math.min(MapMeta.controlMap[x][y],40));

                        score -= control * 2.0 * weightStrife;

                        if(control < -18){
                            score -= 20;
                        }

                        if(control > 13 && weightStrife > 0){
                            score -=  control * 2.0;
                        }

                        score += MapMeta.controlRollingActivity[x][y] * weightStrife * 0.2;

                        score += MapMeta.controlenemyFactories[x][y] * 10.0 * weightStrife;

                        if(amIWorker){
                            if(R.turn < 550) {
                                if(Map.totalSuspectedReachableKarbonite < 250) {
                                    score += controlloc.distanceTo(Map.approxCenter) * 0.05;
                                }
                            }else{
                                //Run to robots for rockets
                                score -= controlloc.distanceTo(Map.approxCenter) * 0.7;
                                score += control * 3;
                                score += MapMeta.controlUnitCounts[x][y] * 8;
                            }
                            if(control == 0){
                                score += 5;
                            }

                            if(R.turn < 100){

                                double weight = 0.05;

                                if(MapMeta.controlKarbonite[x][y] > 0 ){
                                    weight += 0.05;
                                }
                                if(GrandStrategy.strategy == GrandStrategy.KNIGHTRUSH) {
                                    weight += 0.25;
                                    for(Loc l : Map.theirStartingSpots){
                                        score -= Map.findDistanceTo(controlloc,l) * weight;
                                    }
                                }
                                score -= controlloc.distanceTo(Map.approxCenter) * weight;

                            }
                        }
                        else{
                            score -= controlloc.distanceTo(Map.approxCenter) * 0.05;
                        }


                        if(R.hyperVigilanceHasBeenTriggered && Map.myFactories.size() <= 4){
                            score +=  controlloc.distanceTo(Map.approxCenter) * 3;
                            if(MapMeta.controlHasActivity[x][y]){
                                score -= 40;
                            }
                            score += control * 2;
                        }

                        if(MapMeta.controlHasActivity[x][y]){
                            score += 30.0 * weightStrife;

                            if(!amIWorker) {
                                if (x == 0 || x == MapMeta.controlColumns - 1) {
                                    if (y == 0 || y == MapMeta.controlRows - 1) {
                                        score += 12 * weightStrife ; //seems to be hard to get to the four corners
                                    }
                                }
                            }
                        }


                        double suspectedfactor = 0.1;


                        if(!MapMeta.anyActivityDetected  && R.turn < 650){
                            suspectedfactor = 0.25;
                        }
                        score += MapMeta.controlSuspectedEnemyPresence[x][y] * suspectedfactor * weightStrife;

                        if (control > 0) {
                            score -= Math.min(30, (R.turn - MapMeta.controlLastHasActivity[x][y]) * 0.3) * weightStrife;
                        }


                        double totalTilesBlocked = 0;
                        Loc[] roughpath = MapMeta.controlZonePaths[currentzone.x][currentzone.y][x][y];
                        if(roughpath.length > 0) {
                            for (Loc pathloc : roughpath) {
                                totalTilesBlocked += MapMeta.controlUnitCounts[pathloc.x][pathloc.y] + MapMeta.controlImpassable[pathloc.x][pathloc.y] + 0.5 *( 25 - MapMeta.controlSize[pathloc.x][pathloc.y]);
                            }

                            //doubling the final spot
                            Loc l = roughpath[roughpath.length-1];
                            totalTilesBlocked += MapMeta.controlUnitCounts[l.x][l.y] + MapMeta.controlImpassable[l.x][l.y] +  0.5 *(25 - MapMeta.controlSize[l.x][l.y]);

                            double weight = 0;
                            if(amIWorker){
                                weight = -10.0;
                            } else if(!amIHealer) {
                                weight = -8.0;
                            }


                            if(R.SPREAD){
                                weight *= 1.5;
                            }



                            score += (totalTilesBlocked * weight * weightFreeSpace)  / (double)roughpath.length;
                        }
                        double goalspotblocked = (MapMeta.controlUnitCounts[x][y] + MapMeta.controlImpassable[x][y] + 0.5 *(25 - MapMeta.controlSize[x][y]));

                        score -= 10.0 * goalspotblocked * weightFreeSpace;

                        score += Math.min(200, MapMeta.controlKarbonite[x][y]) * 0.08 * weightKarbonite;

                        if(MapMeta.controlKarbonite[x][y] > 10){
                            score += 10 * weightKarbonite;
                        }



//                        if(amIWorker && R.turn == 125){
//                            Debug.log(id + " ["+x+","+y+"]" + score + "  " + control + "  " +  MapMeta.controlKarbonite[x][y]);
//                        }

//                        if(goalspotblocked > 0){
//                            Debug.log(id + " ["+x+","+y+"]" + score + "  " + control + "  " +  MapMeta.controlKarbonite[x][y]   + "  " +   totalTilesBlocked + "/" + roughpath.length  + " " + goalspotblocked);
//                        }


                        if(score > bestscore){
                            bestscore = score;
                            bestLoc = MapMeta.controlLocations[x][y];
                        }
                    }
                }
            }
        }
        Debug.endClock(11);
        if(bestLoc == null) return Map.generallyPassableApproxCenter; //I don't know..

        return bestLoc;


    }


    private int lastSetRandom = -1000;
    private Loc randomgoal = null;
    public Loc getRandomGoal(){

        if(R.turn - lastSetRandom > 30){
            randomgoal = new Loc(PseudoRandom.next(Map.width), PseudoRandom.next(Map.height));
        }

        return randomgoal;
    }

    public void init(){}
    public void preturn(){}
    public void think(){}



}
