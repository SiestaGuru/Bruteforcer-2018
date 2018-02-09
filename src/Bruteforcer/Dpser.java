package Bruteforcer;

import bc.Unit;

import java.util.ArrayList;
import java.util.HashSet;

public class Dpser extends Robot {


    public boolean bloodlust = false;


    public Dpser(Type type, int id, boolean isMine){
        super(type,id,isMine);
        amIDps = true;
    }

    public void infectNearby(int range, int maxdepth){
        for(Robot r : Map.getAlliesInRange(new Circle(loc,range))){
            if(r.amIDps){
                if(!((Dpser)r).bloodlust){
                    if(!r.amIRanger) {
                        ((Dpser) r).bloodlust = true;
                        if (maxdepth > 0) {
                            ((Dpser) r).infectNearby(range, maxdepth - 1);
                        }
//                            Debug.log(r.loc + " BLOODLUST INFECTED");
                    }
                }
            }
        }
    }


    public static void doBloodlustCalcs() {

        if (R.KNIGHTGAME) {

            if (R.turn < 110) {
                for (Knight r : Map.myKnights) {
                    r.bloodlust = true;
                }

                for (Mage r : Map.myMages) {
                    r.bloodlust = true;
                }

            } else if (R.turn % 60 == 15) { //turns 135, 195, etc.

                ArrayList<Dpser> infectionSources = new ArrayList<>();
                for (Dpser r : Map.myDpsers) {
                    if (!r.bloodlust) {
                        if (r.amIKnight && r.scaryEnemyRobotsNearSize > 0) {
                            r.bloodlust = true;
//                            Debug.log(r.loc + " BLOODLUST RAGE");
                            infectionSources.add(r);
                        }
                    } else {
                        if (R.turn - r.lastSeenEnemy > 20) {
                            r.bloodlust = false;
//                            Debug.log(r.loc + " bloodlust lost");
                        } else {
                            infectionSources.add(r);
                        }
                    }
                }

                for (Dpser r : infectionSources) {
                    r.infectNearby(20,3);
                }
            }
        } else if(GrandStrategy.strategy == GrandStrategy.ADAPTIVERANGER || GrandStrategy.strategy == GrandStrategy.STDRANGER){

            if(false) {
                if (R.turn > 200) {
                    if (R.turn % 20 == 0) {


                        int enemystucktracker = 0;

                        for (Robot r : Map.theirRobots) {
                            if (!r.amIStructure) {
                                enemystucktracker += (R.turn - r.lastMoved) - 4;
                            }
                        }
                        boolean isenemystuck = enemystucktracker > 0;
                        double desire = -140;
                        if (Map.theirTotalUnitCount > 0) {
                            desire += (((double) Map.myTotalUnitCount) / ((double) Map.theirTotalUnitCount * 6.0)) * 60.0; //numerical advantage increases odds. of course we cant see all enemies, so it's just an estimate

                            desire += ((double) Map.theirUnitCounts[Type.HEALER.typeId]) / ((double) Map.theirTotalUnitCount) * 160.0; //if they overdo healers, they could be weak
                            desire += ((double) Map.theirUnitCounts[Type.WORKER.typeId]) / ((double) Map.theirTotalUnitCount) * 10.0;
                            desire -= ((double) Map.theirUnitCounts[Type.RANGER.typeId]) / ((double) Map.theirTotalUnitCount) * 30.0; //if they overdo ranger, it's harder
                            desire -= ((double) Map.theirUnitCounts[Type.KNIGHT.typeId]) / ((double) Map.theirTotalUnitCount) * 10.0;

                        }

                        desire += Math.min(30, Map.myUsableTotalDpsCount * 0.4); //Just plain numbers make it more likely to do this

                        if (R.heavilystuck) {
                            desire += 25;
                        }
                        if (isenemystuck) {
                            desire += 25;
                        }

                        if (R.POWERSAVEMODUS) {
                            desire += 20; //just suicide some units for numbers control
                            if (R.POWERSAVEMODUSHYPER) {
                                desire -= 40;//too few units we can use
                            }
                        }

//                    Debug.log("Bloodlust score  " + desire);


                        if (desire > 0) {
//                        Debug.log("BLOODLUUUUSST");
//                        Debug.log("BLOODLUUUUSST");
                            ArrayList<Dpser> infectionSources = new ArrayList<>();
                            for (Dpser d : Map.myDpsers) {
                                if (d.scaryEnemyRobotsNearSize > 3 && d.robotsInSight.size() > 10) {

                                    double power = 0;
                                    for (Robot r : Map.getAlliesInRange(new Circle(d.loc, 6))) {
                                        if (r.amIDps || r.amIHealer) {
                                            power += (r.health / r.maxHealth);
                                        }
                                    }

                                    if (power > d.scaryEnemyRobotsNearSize) {
                                        d.bloodlust = true;
//                                    Debug.log("Bloodlust source: " + d.loc);
                                        infectionSources.add(d);
                                    }
                                }
                            }
                            for (Dpser r : infectionSources) {
                                r.infectNearby(9, 2);
                            }


                        } else {
                            for (Dpser d : Map.myDpsers) {
                                d.bloodlust = false;
                            }
                        }

                    }
                }
            }
        }
    }

    public void doDps(Robot bestTarget){

        if(!canAttack()) return;



        HashSet<Integer> filterlist = new HashSet<>();

        if(bestTarget != null && !Player.gc.canAttack(id,bestTarget.id)){
            Debug.log(loc + " Had to filter " + bestTarget.loc + " out (1)");
            filterlist.add(bestTarget.id);
            bestTarget = null;
        }

        while(bestTarget == null){
            bestTarget = findBestTarget(false,filterlist);
            if(bestTarget == null){
                break;
            }
            else {
                if (!Player.gc.canAttack(id, bestTarget.id)) {
                    Debug.log(loc + " Had to filter " + bestTarget.loc + " out (2)");
                    filterlist.add(bestTarget.id);
                    bestTarget = null;
                }
            }
        }


        if(bestTarget != null){
            attack(bestTarget,!hasDoneTurn);
            lastShotAtEnemy = R.turn;
        }

    }

    //ignores canAttack
    public boolean canShootAt(Robot r, Loc from){
        return from.isWithinSquaredDistance(r,attackRange) && (!amIRanger ||  !from.isWithinSquaredDistance(r,15));
    }


    public Robot findBestTarget(boolean allowmove, HashSet<Integer> filter){
        if(!canAttack()) return null;

        ArrayList<Robot> possibletargets;
        if(canMove() && allowmove){
            possibletargets = enemyRobotsInApproxAttackRange;
        }else{
            possibletargets = enemyRobotsInAttackRange;
        }


        int dpt = (int)(MapMeta.enemyDamageArray[x][y] * 0.5) - MapMeta.healerSupportArray[x][y] * 10;

        int estimatedturnscanshoot;
        if(dpt <= 0){
            estimatedturnscanshoot = -1;
        }else{
            estimatedturnscanshoot =  1 +  health / dpt;
        }

        double bestscore = -50;
        Robot bestTarget = null;

        for(Robot r : possibletargets){
            double score = -9999;
            if(canShootAt(r,loc)){
                score = targetAttackEvaluation(r, true, estimatedturnscanshoot);
            }else if(allowmove){
                for(Loc l : adjacentPassableTiles){
                    if(!l.containsRobot() && canShootAt(r,l)){
                        score = targetAttackEvaluation(r,true,estimatedturnscanshoot);
                        break;
                    }
                }
            }
            if(score > bestscore){
                if(filter == null || !filter.contains(r.id)) {
                    bestscore = score;
                    bestTarget = r;
                }
            }
        }
        return bestTarget;
    }


    public void attackAndMove(){
        if(canMove()) {
            if(canAttack()) {
                //This bit determines whether to attack or move first depending on who we want to hit
                Robot besttarget = findBestTarget(true,null);
                if(besttarget == null) {
                    M.calcBestMove();
                    doMove();
                    doDps(null);
                }else if(canShootAt(besttarget,loc)){
                    doDps(besttarget);
                    M.calcBestMove();
                    doMove();
                }else{
                    M.calcBestMove();
                    if(canShootAt(besttarget,bestMove)){
                        //If we end up loading into a structure, doMove will still force a doDps first
                        doMove();
                        doDps(null);
                    }else{
                        doDps(null);
                        doMove();
                        doDps(null);
                    }
                }
            }else{
                //could still be unloading
                M.calcBestMove();
                doMove();
                doDps(null);
            }
        }else{
            doDps(null);
        }
    }


    public void attack(Robot r, boolean allowOverchargeRequest){
        try {
//        if(Player.gc.canAttack(id,r.id)){
            Player.gc.attack(id, r.id);


            int basedmg = damage;
            if(amIMage){
                if(Techs.techMage >= 3){
                    basedmg = Type.MAGE.damage + 45;
                } else if(Techs.techMage == 2){
                    basedmg = Type.MAGE.damage + 30;
                } else if(Techs.techMage == 1){
                    basedmg = Type.MAGE.damage + 15;
                }
            }

            int knightdamage = basedmg;
            if(r.amIKnight) {
                switch (Techs.enemyTechKnights) {
                    case 0:
                        knightdamage -= 5;
                        break;
                    case 1:
                        knightdamage -= 10;
                        break;
                    default:
                        knightdamage -= 15;
                        break;
                }
            }



            if(r.amIKnight){
                r.health -= knightdamage;
            }else {
                r.health -= basedmg;
            }
            attackHeat += attackCd;

            if (r.health <= 0) {
                Map.robots[r.x][r.y] = null;
                Map.locations[r.x][r.y].containsRobot = false;
                Map.theirRobots.remove(r);
                r.isDead = true;
            }

            if (amIMage) {
                for (Loc l : r.loc.adjacentPassableTiles()) {
                    Robot side = Map.robots[l.x][l.y];
                    if (side != null) {

                        if(side.amIKnight){
                            side.health -= knightdamage;
                        }else {
                            side.health -= basedmg;
                        }

                        if (side.health <= 0) {
                            Map.robots[l.x][l.y] = null;
                            Map.locations[l.x][l.y].containsRobot = false;
                            Map.theirRobots.remove(side);
                            side.isDead = true;
                        }
                    }
                }
            }

            if(Techs.canOverload && allowOverchargeRequest && r.health > 0 && !r.amIWorker){

                OverchargeRequest or = (new OverchargeRequest(this, r));
                or.updateShotsRequired();
                if(or.shotsReq < MapMeta.overchargeSupportArray[x][y]) {
                    R.overchargeReqs.add(or);
                }
            }

//        }else{
//            Debug.log("CANT ATTACK " + this + " -> " + r);
//        }
        }catch (Exception ex){
            Debug.log(ex);
            Debug.log("CANT ATTACK " + this + " -> " + r);
        }
    }

    public double targetAttackEvaluation(Robot r, boolean allowrecursion, int estimatedTimesAbleToShoot){

        if(r.isMine || r.isDead || r.inGarrison) return -10000000;

        int realdamage = damage;
        if(amIMage){
            if(Techs.techMage >= 3){
                realdamage = Type.MAGE.damage + 45;
            } else if(Techs.techMage == 2){
                realdamage = Type.MAGE.damage + 30;
            } else if(Techs.techMage == 1){
                realdamage = Type.MAGE.damage + 15;
            }
        }


        double score = 0;
        switch (r.type){
            case RANGER:
                score = 25;
                if(amIRanger){
                    score += 15;
                }

                if(bloodlust){
                    score += 20;
                }
                break;
            case HEALER:
                score = 30;
                break;
            case MAGE:
                score = 25;
                if(r.enemyRobotsInAttackRange.size() > 0){
                    score += 20;
                }
                if(bloodlust){
                    score += 20;
                }
                if(amIMage){
                    score += 15;
                }
                if(amIKnight){
                    score += 100;
                }
                break;
            case KNIGHT:
                score = 20;
                if(r.enemyRobotsInAttackRange.size() > 0){
                    score += 10;
                }
                if(amIMage){
                    score += 15;
                }
                if(amIKnight){
                    score += 10;
                }
                if(r.loc.isWithinSquaredDistance(loc,18)){
                    score += 20;
                }

                switch (Techs.enemyTechKnights){
                    case 0:
                        realdamage -= 5;
                        break;
                    case 1:
                        realdamage -= 10;
                        break;
                    default:
                        realdamage -= 15;
                        break;
                }

                break;
            case WORKER:
                score = 2;
                break;
            case FACTORY:
                score = 5;


                if(estimatedTimesAbleToShoot * realdamage > r.health){
                    score += 10;
                }

                score += ((Structure)r).garrisonedWithin.size() * 20;


                if(Map.myUsableTotalDpsCount > 7){
                    score += 15;
                }
                if(!r.amIBlueprint){
                    score += 15;
                    int turnsleft = ((Factory)r).turnsLeftProduction;
                    if (turnsleft <= 4 && turnsleft > 0) {
                        if (r.health <= realdamage * ((turnsleft + 1) / 2)) {
                            //kill it fast before they can produce
                            score += 30;

                        }
                    }
                }
                if(r.health < realdamage){
                    score += 30;
                }
                score -= Map.theirUnitCounts[Type.FACTORY.typeId] * 2;
                score = Math.min(2,score);
                break;
            case ROCKET:
                score = 2;
                score += ((Structure)r).garrisonedWithin.size() * 30;
                break;
        }

        if(!amIMage) {
            if (r.amIStructure && r.health > realdamage) {
                int workers = r.loc.getAdjacentEnemyRobots(Type.WORKER).size();
                if ((workers + 2) * 10 > damage) {
                    score -= 80; //we can be outhealed here, first shoot other shit
                }else{
                    score -= workers * 5;
                }
            }
        }

        if(r.health < realdamage){
            if(r.amIWorker){
                score += 10;
            }else {
                score += 40 + ((double) r.health) * 0.01;
            }
        }
        else{
            score -= 10 * (r.health / r.maxHealth);
        }

        if(!r.amIWorker) {
            score += r.theoreticalDamage * 0.5;
        }


        if(estimatedTimesAbleToShoot * realdamage > r.health){
            score += 10;
        }

        if(r.theoreticalDamage >= r.health ){
            if(amIWorker){
                score += 10;
            }
            else if(amIFactory){
                score += 60;
            }else{
                score += 40;
            }

        } else if(r.theoreticalDamageIncludingNextTurn >= r.health + 30){
            if(amIWorker){
                score += 5;
            }
            else if(amIFactory){
                score += 40;
            }else{
                score += 25;
            }
        }else if(r.theoreticalDamageIncludingNextTurn >= r.health){
            if(amIWorker){
                score += 5;
            }
            else if(amIFactory){
                score += 40;
            }else{
                score += 20;
            }
        }

        if(r.amIDps && r.attackCircle.contains(loc)){
            score += 10;
        }

        if(amIRanger){
            //Rangers should shoot at close stuff first so they won't end up in our inner circle
            score -= r.loc.distanceTo(loc);
        }

        if(amIMage && allowrecursion && !R.POWERSAVEMODUSHYPER ){
            Loc[] adjacent = r.loc.adjacentPassableTiles();
            for(Loc l : adjacent){
                Robot mr = Map.robots[l.x][l.y];

                if(mr != null){
                    if(mr.isMine){
                        if(mr.amIWorker) {
                            score -= 40;
                        }
                        else if(mr.amIStructure && r.amIKnight){
                            //welp, gotta do something
                            score -= 10;
                            if(mr.health < realdamage){
                                score -= 250;
                            }
                        }
                        else{
                            score *= 0.6;
                            score -= 150;
                            if(mr.health < realdamage){
                                score -= 250;
                            }
                        }
                    } else if(!mr.isDead && !mr.inGarrison){
                        score += targetAttackEvaluation(mr,false,estimatedTimesAbleToShoot);
                    }
                }
            }
        }
        return score;
    }


    public boolean willIKillSomethingIfIShoot(Robot r){
        if(r.health <= damage){
            return true;
        }
        if(amIMage){

            if(r.isMine){
                for(Robot r2 : r.loc.getAdjacentFriendlyRobots()){
                    if(r2.health <= damage){
                        return true;
                    }
                }

            }else{
                for(Robot r2 : r.loc.getAdjacentEnemyRobots()){
                    if(r2.health <= damage){
                        return true;
                    }
                }
            }
        }
        return false;


    }


}
