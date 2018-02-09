package Bruteforcer;

import java.util.ArrayList;
import java.util.HashMap;

public class Mage extends Dpser {
    public Mage( int id, boolean isMine){
        super(Type.MAGE,id,isMine);
        amIMage = true;
    }


    public static int myMageDamage = Type.MAGE.damage;
    public static int enemyMageDamage= Type.MAGE.damage;




    @Override
    public void init(){}
    @Override
    public void preturn(){}
    @Override
    public void think(){

//        Debug.log("Attack heat: " + attackHeat);
        if(canMove()) {


            if(R.SUICIDERUSH){
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
                    M.addGoal(gotospot,50);
                }else{
                    Loc l = getControlMapGoal(100, 0, 0, 20);
                    M.addGoal(l,50);
                }
            }


            int weightstrife = 4;
            if(bloodlust){
                weightstrife = 6;
            }

            if (EnemyStrategy.Strategy == EnemyStrategy.FULLKNIGHT || EnemyStrategy.Strategy == EnemyStrategy.KNIGHTHEALER || EnemyStrategy.Strategy == EnemyStrategy.KNIGHTRANGER) {
                M.addGoal(getControlMapGoal(weightstrife, 0.6, 0.2,1), (5 + 15 * R.beBallsyRating) * weightstrife);
            } else if (EnemyStrategy.Strategy == EnemyStrategy.MAGEHEAVY) {
                M.addGoal(getControlMapGoal(weightstrife, 1, 1.1,1), (5 + 15 * R.beBallsyRating) * weightstrife);
            } else {
                M.addGoal(getControlMapGoal(weightstrife, 1, 0.4,1), (5 + 15 * R.beBallsyRating) * weightstrife);
            }


            if(bloodlust){
                addDangerDesire(-1,health + 1,-0.5);
            }else if(R.turn < 200) {
                addDangerDesire(-10,health + 1,-1.2);
            } else{
                addDangerDesire(-5,health + 1,-2);
            }



            boolean enemymagenear = false;
            boolean tryToHideInFactory = false;



            for (Robot r :   C.expandToIncludeGarrisoned(   Map.getEnemiesInRange(new Circle(loc, Type.RANGER.atPlusMoveSquaredApprox)))) {
                if (!isDead) {
                    M.addCircle(r.loc,2,-3000); //dont step on them yo
                    boolean inAttackCircle = attackCircle.contains(r);
                    boolean inAttackMoveCircle = approxAttackPlusMoveCircle.contains(r);
                    Robot hitAdjacentForDamage = null;
                    if(!inAttackCircle){
                        if(inAttackMoveCircle){
                            for(Loc l : r.adjacentPassableTiles){

                                Robot r2 = l.getRobot();
                                if(r2 != null && !r2.isMine && attackCircle.contains(r2)){
                                    hitAdjacentForDamage = r2;
                                    break;
                                }
                            }
                        }

                        if(hitAdjacentForDamage == null && canMove()){
                            neighbourloop:
                            for(Loc l : r.adjacentPassableTiles){
                                Robot r2 = l.getRobot();
                                if(r2 != null && !r2.isMine && approxAttackPlusMoveCircle.contains(r2)){
                                    for(Loc l2 : adjacentPassableTiles){
                                        if(l.isWithinSquaredDistance(l2,attackRange)) {
                                            hitAdjacentForDamage = r2;
                                            break neighbourloop;
                                        }
                                    }
                                }
                            }
                        }
                    }


                    if(r.amIMage){

                        if(r.loc.isWithinSquaredDistance(loc,50)){
                            enemymagenear = true;
                        }
                        boolean doIGetToShootFirst = attackHeat <= r.attackHeat;
                        boolean canIShootWithoutMoving = (canAttack() && inAttackCircle) || attackCircle.containsNullable(hitAdjacentForDamage);

                        boolean canIGetInFiringRange = attackCircle.contains(r) || hitAdjacentForDamage != null;
                        boolean canIStepOutOfTheirRange = false;

                        double extraDamage = 0;

                        boolean canIEscapeInsideFactory = false;

                        if(!canIGetInFiringRange) {
                            for (Loc l : adjacentPassableTiles) {
                                if (!l.containsRobot()) {
                                    if (l.isWithinSquaredDistance(r.loc, attackRange)) {
                                        canIGetInFiringRange = true;
                                        break;
                                    }
                                }
                            }
                        }

                        for(Loc l : adjacentPassableTiles){
                            if(l.containsRobot()){
                                Robot rob = l.getRobot();
                                if(rob.isMine && rob.amIStructure){
                                    canIEscapeInsideFactory = true;
                                    break;
                                }
                            }
                        }







                        for (Loc l : r.adjacentPassableTiles) {
                            if(l.containsRobot()){
                                if(l.getRobot().isMine){
                                    extraDamage -= 300;
                                }else{
                                    extraDamage += myMageDamage;
                                    if(l.getRobot().health < myMageDamage){
                                        extraDamage += 100;
                                    }
                                }
                            }

                        }
                        for (Loc l : adjacentPassableTiles) {
                            if (!l.containsRobot()) {
                                if (!l.isWithinSquaredDistance(r.loc, attackRange)) {
                                    canIStepOutOfTheirRange = true;
                                    break;
                                }
                            }
                        }



//                        boolean canTheyMoveShoot = false;
//                        for (Loc l : r.adjacentPassableTiles) {
//                            if (l.isWithinSquaredDistance(loc, attackRange)) {
//                                canTheyMoveShoot = true;
//                                break;
//                            }
//                        }
                        boolean willIDieOneShot = false;
                        boolean willTheyDieOneShot = false;

                        if (r.health < myMageDamage) {
                            willTheyDieOneShot = true;
                        }
                        if (health < enemyMageDamage) {
                            willIDieOneShot = true;
                        }

                        double shootthemscore = 0;
                        double shootmescore = 0;


                        if(r.inGarrison){
//                            Debug.log("In garrison");
                            doIGetToShootFirst = false;
                            canIShootWithoutMoving = false;
                            canIGetInFiringRange = false;
                            willTheyDieOneShot = false;
                            extraDamage = 0;
                        }


                        if(canIShootWithoutMoving){
                            if(willTheyDieOneShot){
                                if(canIStepOutOfTheirRange || canIEscapeInsideFactory) {
                                    //Okay, we can already kill them. So avoid them, just in case we decide to shoot something else
                                    shootmescore = -200;
                                }
                            }else if(willIDieOneShot){
                                if(canIStepOutOfTheirRange || canIEscapeInsideFactory) {
                                    shootmescore = -5000; //kite, hope to get out of range
                                }
                                //else, hope for the best
                            } else{
                                //Probably best to try to step out of range, hope we got a free shot in. chasing is also an option. but imo better to avoid getting hit here
                                if(canIStepOutOfTheirRange || canIEscapeInsideFactory) {
                                    shootmescore = -2000;
                                }
                            }
                        }
                        else if(canAttack() && !r.inGarrison){
                            if(willTheyDieOneShot){
                                //Just shoot
                                if(canIGetInFiringRange) {
                                    shootthemscore = 4000;
                                }else{
                                    if(r.canAttack()) {
                                        if (willIDieOneShot) {
                                            //cant hit them, but they move and kill me
                                            shootmescore = -2000;
                                        } else {
                                            shootmescore = -200; //not as big of a deal
                                        }
                                    }
                                }
                            } else if(willIDieOneShot){
                                if(canIGetInFiringRange) {
                                    r.updateTheoreticalDamage();
                                    if (r.theoreticalDamage >= r.health) {
                                        //Let's maybe do it boys
                                        shootthemscore = 600 + extraDamage * 8;
                                        shootmescore = -600 + extraDamage * 8;
                                    }
                                    else {
                                        //Maybe if the extra damage is awesome
                                        shootthemscore = 300 + extraDamage * 8;
                                        shootmescore = -1500 + extraDamage * 8;
                                    }
                                }else{
                                    if(r.canAttack()) {
                                        shootmescore = -2000;
                                    }else{
                                        shootmescore = -1500;
                                    }
                                }
                            } else{
                                if(canIGetInFiringRange) {
                                    //I should win, given no extra stuff around
                                    shootthemscore = 2000;
                                    shootmescore = -1000;
                                }else{
                                    //Ill probably lose if I step in now
                                    if(r.canAttack()) {
                                        shootmescore = -2000;
                                    }
                                }
                            }
                        }
                        else if(doIGetToShootFirst){
                            if(canIGetInFiringRange) {
                                if (willTheyDieOneShot) {
                                    //I should win, so chase, but not too much to account for secondary units
                                    shootthemscore = 500 + extraDamage * 3;
                                    M.addCircle(r.loc, 17, 600);
                                    if (willIDieOneShot) {
                                        shootmescore = -300 + extraDamage * 3;
                                    } else {
                                        shootmescore = -200 + extraDamage * 3;
                                    }
                                } else if (willIDieOneShot) {
                                    //Disengage, unless extra stuff
                                    shootthemscore = 300 + extraDamage * 8;
                                    shootmescore = -1500 + extraDamage * 8;
                                } else {
                                    //I should win, so chase, but not too much to account for secondary units
                                    M.addCircle(r.loc, 17, 600);
                                    M.addGoal(r.loc, 40);
                                    shootthemscore = 350 + extraDamage * 3;
                                    shootmescore = -200 + extraDamage * 3;
                                }
                            }else{
                                if(r.canAttack()) {
                                    if (willIDieOneShot) {
                                        shootmescore = -2000;
                                    } else if(willTheyDieOneShot){
                                        shootmescore = -300; //losing advantage, just wait for them to make a mistake
                                    }
                                }else{
                                    if(willTheyDieOneShot){
                                        //position so that if they're dumb and move in, we kill them
                                        M.addCircle(r.loc,Type.MAGE.atPlusMoveSquaredApprox,400);
                                    } else if(willIDieOneShot){
                                        //would lose the battle
                                        shootmescore = -500;
                                    } else{
                                        //position so that if they're dumb and move in, we kill them
                                        M.addCircle(r.loc,Type.MAGE.atPlusMoveSquaredApprox,400);
                                    }
                                }
                            }
                        }
                        else{
                            //This also means they can shoot this turn


                            if(canIStepOutOfTheirRange || canIEscapeInsideFactory){
                                if(willIDieOneShot){
                                    shootmescore = -4000;
                                }else{
                                    if(willTheyDieOneShot){
                                        //I should win, so chase, but not too much to account for secondary units
                                        M.addCircle(r.loc,17,600);
                                        M.addGoal(r.loc,40);
                                        shootthemscore = 350 + extraDamage * 3;
                                        shootmescore = -200 + extraDamage * 3;
                                    }else {
                                        //Runnn
                                        shootmescore = -3000;
                                    }
                                }
                            }else{
                                if(willIDieOneShot){
                                    //hope for the best
                                    shootthemscore = 400;
                                }else{
                                    if(willTheyDieOneShot){
                                        //I should win, so chase, but not too much to account for secondary units
                                        M.addCircle(r.loc,17,600);
                                        M.addGoal(r.loc,40);
                                        shootthemscore = 350 + extraDamage * 3;
                                        shootmescore = -200 + extraDamage * 3;
                                    }else {
                                        //Runnn
                                        shootmescore = -3000;
                                        M.addVectorCircle(r.loc,Type.MAGE.atPlusMoveSquaredApprox,-2000,-100);
                                    }
                                }


                            }

                        }

                        if(bloodlust){
                            shootthemscore *= 1.3;
                            shootmescore *= 0.7;
                        }

//                        Debug.log("anti-mage: " + shootthemscore + " scary: " + shootmescore);


                        if(hitAdjacentForDamage != null){
                            //An alternative for shooting our target
                            M.addEnemyZone(new Circle(hitAdjacentForDamage.loc,0), 0, attackRange, shootthemscore, 0);
                        }

                        M.addEnemyZone(r.approxAttackPlusMoveCircle, r.attackRange, attackRange, shootthemscore, shootmescore);

                    }else {

                        double attackthemscore = 100;

                        if(r.amIRanger){
                            attackthemscore = 800;
                        } else if(r.amIWorker){
                            attackthemscore = 20;
                        } else if(r.amIFactory){
                            attackthemscore = 1000;
                        }

                        for(Loc l : r.adjacentPassableTiles){
                            if(l.containsRobot()){
                                if(!l.getRobot().isMine){
                                    attackthemscore += 700;
                                }
                                else{
                                    attackthemscore -= 500;
                                }
                            }
                        }

                        if(r.inGarrison){
                            attackthemscore = 0;
                        }

                        double avoidscore;
                        if (bloodlust) {
                            if (r.amIRanger) {
                                if (loc.isWithinSquaredDistance(r.loc, Type.MAGE.atPlusMoveSquaredApprox)) {
                                    //Go in hard
                                    M.addVector(r.loc, attackthemscore * 0.1);
                                    attackthemscore*=2;
                                    avoidscore = -30 - (r.damage * 1.5);
                                } else if (loc.isWithinSquaredDistance(r.loc, Type.RANGER.atPlusMoveSquaredApprox)) {
                                    if (r.scaryEnemyRobotsNearSize >= 3) {
                                        //kay, probably fine to go in
                                        M.addVector(r.loc, attackthemscore * 0.1);
                                        attackthemscore*=2;
                                        avoidscore = -30 - (r.damage * 1.5);
                                    } else {
                                        //Stay back
                                        avoidscore = -600 - (r.damage * 5);
                                    }
                                } else {
                                    if (r.scaryEnemyRobotsNearSize >= 2) {
                                        //kay, probably fine to go in
                                        M.addVector(r.loc, attackthemscore * 0.1);
                                        attackthemscore*=2;
                                        avoidscore = -30 - (r.damage * 1.5);
                                    } else {
                                        //Stay back
                                        avoidscore = -600 - (r.damage * 5);
                                    }
                                }
                            }else if(r.amIKnight){
                                avoidscore = -2000;
                            }

                            else{
                                avoidscore = 0;
                            }
                        } else if (R.turn < 200) {
                            avoidscore = -1500 - (r.damage * 5);
                        } else {
                            avoidscore = -400 - (r.damage * 5);
                        }

                        if(hitAdjacentForDamage != null){
                            M.addEnemyZone(new Circle(hitAdjacentForDamage.loc,0), 0, attackRange, attackthemscore, 0);
                        }
                        M.addEnemyZone(r.approxAttackPlusMoveCircle, r.attackRange, attackRange, attackthemscore, avoidscore);



                        if (r.amIKnight) {
                            if(r.health >  (Mage.myMageDamage - Knight.theirKnightDefense)   ) {
                                if (r.canMove()) {
                                    M.addVectorCircle(r.loc, 25, -800, -100);
                                } else {
                                    M.addVectorCircle(r.loc, 18, -800, -100);
                                }
                                if(r.health > (Mage.myMageDamage - Knight.theirKnightDefense) || !canAttack()) {
                                    M.addCircle(r.loc, 13, -500);
                                    //We have a square avoidance avoidance that can't be captured by a circle
                                    //Should prob make a rect shape, oh well
                                    M.addSpecialLocation(r.loc.add(3, 3), -500);
                                    M.addSpecialLocation(r.loc.add(-3, 3), -500);
                                    M.addSpecialLocation(r.loc.add(3, -3), -500);
                                    M.addSpecialLocation(r.loc.add(-3, -3), -500);
                                }
                                if(loc.isWithinSquaredDistance(r.loc,20)){
                                    tryToHideInFactory = true;
                                }
                            }

                        }
                    }


                    if (Techs.canBlink || Techs.canOverload) {
                        //Prepare to blink/chain overcharge
                        M.addCircle(r.loc, 90, 100);
                        int realdist = Map.findDistanceTo(loc, r.loc);
                        if (realdist < 10) {
                            M.addGoal(r.loc, 4);
                        }
                    }


                }
            }

            double amount  = -20;
            if(enemymagenear){
                amount = -100;
            }

            for (Robot r : Map.getAlliesInRange(new Circle(loc, 8))) {
                if(r.amIMage || enemymagenear || R.SPREAD) {
                    M.addVector(r.loc, -5);
                    M.addCircle(r.loc, 1, amount);
                }
                else if(tryToHideInFactory && r.amIStructure){
                    M.addGoal(r.loc,25);
                }
            }


        }







        //If we have blink, try to find a good path for destroying the enemy
        //Separate from the overcharge-strategy stuff, can be done on other turns
        ArrayList<BlinkPath> blinkAttackPaths = new ArrayList<>();

        BlinkPath bestBlinkPath = null;
        Robot finalTarget = null;
        double finalTargetScore = 0;
        double finalBlinkPathScore = 0;

        Healer overcharger = null;
        if(Techs.canOverload) {
            for (Healer h : Map.myHealers) {
                if(h.canUseAbility()){
                    overcharger = h;
                    break;
                }
            }
        }

        blinkstuff:
        if(Techs.canBlink) {
            if ((canAttack() && canUseAbility()) || overcharger != null) {
                if (!R.POWERSAVEMODUS) {
                    int range;
                    if (canMove() || overcharger != null) {
                        if (Techs.canBlink && (canUseAbility() || overcharger != null)) {
                            range = Type.MageBlinkMoveAttackRange;
                        }
                        else {
                            break blinkstuff;
                        }
//                        else {
//                            range = type.atPlusMoveSquaredApprox;
//                        }
                    } else {
                        if (Techs.canBlink && canUseAbility()) {
                            range = Type.MageBlinkAttackRange;
                        } else {
                            break blinkstuff;
                        }
                    }

                    for (Robot r : Map.getEnemiesInRange(new Circle(loc, range))) {
                        if(willIKillSomethingIfIShoot(r)) {
                            double score = targetAttackEvaluation(r, true, -1);
                            if (score > 70) { //minimum score of 70  to even consider jumping (blink is expensive)
                                ObjectSorting.AddToSort(r, score);
                            }
                        }
                    }

                    for (ObjectSorting r : ObjectSorting.SortDesc()) {
                        Circle robTargetCircle = new Circle(((Robot) r.internalobj).loc, attackRange);

                        if (robTargetCircle.contains(loc))
                            break; //We're already in range of our best remaining target, stop blinking ya fools


                        //Now let's figure out if we can actually reach the unit somehow
                        //This method is certainly not the most performant way of doing this, but it happens rarely enough that it's okay
                        if (canMove() || overcharger != null) {
//                            if (Techs.canBlink && (canUseAbility() || overcharger != null)) {
                                //Move first, then blink if needed
                                for (MainLoc l : adjacentPassableTiles) {
                                    if (!l.containsRobot()) {
                                        if (robTargetCircle.contains(l)) {
                                            blinkAttackPaths.add(new BlinkPath(null, l, l, false));
                                        } else {
                                            for (MainLoc l2 : (new Circle(l, 7).allContainingLocationsOnTheMap())) {
                                                if (l2.isEmpty() && robTargetCircle.contains(l2)) {
                                                    blinkAttackPaths.add(new BlinkPath(l2, l, l2, false));
                                                }
                                            }
                                        }
                                    }
                                }
                                //Blink first, then move as needed
                                for (MainLoc l : (new Circle(loc, 7).allContainingLocationsOnTheMap())) {
                                    if (l.isEmpty()) {

                                        if (robTargetCircle.contains(l)) {
                                            blinkAttackPaths.add(new BlinkPath(l, null, l, true));
                                        } else {
                                            for (MainLoc l2 : l.adjacentPassableTiles()) {
                                                if (Map.robots[l2.x][l2.y] == null && robTargetCircle.contains(l2)) {
                                                    blinkAttackPaths.add(new BlinkPath(l, l2, l, true));
                                                }
                                            }
                                        }
                                    }
                                }
//                            }
//                            else {
//                                for (Loc l : adjacentPassableTiles) {
//                                    if (!l.containsRobot()) {
//                                        if (robTargetCircle.contains(l)) {
//                                            blinkAttackPaths.add(new BlinkPath(null, l, l, false));
//                                        }
//                                    }
//                                }
//                            }
                        } else {
                            for (MainLoc l : (new Circle(loc, 7).allContainingLocationsOnTheMap())) {
                                if (l.isEmpty() && robTargetCircle.contains(l)) {
                                    blinkAttackPaths.add(new BlinkPath(l, null, l, true));
                                }
                            }
                        }
                        if (blinkAttackPaths.size() > 0) {
                            finalTarget = ((Robot) r.internalobj);
                            finalTargetScore = r.score;
                            break;
                        }
                    }

                    if (blinkAttackPaths.size() != 0) {
//                Debug.log("We've found paths");
                        HashMap<Loc, BlinkPath> filterDumbPaths = new HashMap<>();
                        //Filters out all the options that just result in reaching the same tile while using more resources
                        for (BlinkPath b : blinkAttackPaths) {
                            if (filterDumbPaths.containsKey(b.finalLoc)) {
                                BlinkPath cur = filterDumbPaths.get(b.finalLoc);
                                if (cur.blinkLoc != null) {
                                    if (b.blinkLoc == null) {
                                        filterDumbPaths.put(b.finalLoc, b);
                                    } else if (cur.moveLoc != null && b.moveLoc == null) {
                                        filterDumbPaths.put(b.finalLoc, b);
                                    }
                                } else if (b.blinkLoc != null) {
                                    if (cur.moveLoc != null && b.moveLoc == null) {
                                        filterDumbPaths.put(b.finalLoc, b);
                                    }
                                }
                            } else {
                                filterDumbPaths.put(b.finalLoc, b);
                            }
                        }

                        double bestScore = -99999;

                        for (BlinkPath b : filterDumbPaths.values()) {
                            double score = M.evaluateSpot(b.finalLoc, false);

                            if (b.blinkLoc != null) {
                                score -= 100;
                            }
                            if (b.moveLoc != null) {
                                score -= 50;
                            }

                            if (score > bestScore) {
//                        Debug.log("Found a new best blink path");
                                bestScore = score;
                                bestBlinkPath = b;
                                finalBlinkPathScore = score + finalTargetScore * 50.0;
                            }
                        }
                    }
                }
            }
        }



        if(bestBlinkPath != null && (finalBlinkPathScore > 0 || finalTargetScore > 100)) {  //blink if we have a target, and it's a good idea in general, or we are in a suicidal mood because the targets just so good

//            Debug.log(this + " PATH");

            boolean canproceed = true;

            if (!canAttack() || (bestBlinkPath.moveLoc != null && !canMove()) || (bestBlinkPath.blinkLoc != null && !canUseAbility())) {
                if (overcharger != null && Player.UseTargetedAbility(overcharger, Abilities.OVERCHARGE, id)) {
//                    Debug.log("Called an overcharger for mage");
                    moveHeat = 0;
                    abilityHeat = 0;
                    attackHeat = 0;
                } else {
                    canproceed = false;
                }
            }

            if (canproceed) {
                if (bestBlinkPath.moveLoc != null) {
                    if (bestBlinkPath.blinkLoc != null) {
                        if (bestBlinkPath.blinkFirst) {
//                            Debug.log(" BLINKMOVE (1)  " + loc + " -> " + bestBlinkPath.blinkLoc + " -> " + bestBlinkPath.moveLoc + " -> " + finalTarget.loc);
                            Player.UseLocationAbility(this, Abilities.BLINK, bestBlinkPath.blinkLoc);
                            bestMove = bestBlinkPath.moveLoc;

                            doMove();
                            attack(finalTarget,!hasDoneTurn);
                        } else {
//                            Debug.log(" BLINKMOVE (2)  " + loc + " -> " + bestBlinkPath.moveLoc + " -> " + bestBlinkPath.blinkLoc + " -> " + finalTarget.loc);

                            bestMove = bestBlinkPath.moveLoc;
                            doMove();
                            Player.UseLocationAbility(this, Abilities.BLINK, bestBlinkPath.blinkLoc);
                            attack(finalTarget,!hasDoneTurn);
                        }
                    } else {
//                        Debug.log(" MOVE  " + loc + " -> " + bestBlinkPath.moveLoc + " -> " + finalTarget.loc);

                        bestMove = bestBlinkPath.moveLoc;
                        doMove();
                        attack(finalTarget,!hasDoneTurn);
                    }
                } else {
//                    Debug.log(" BLINK  " + loc + " -> " + bestBlinkPath.blinkLoc + " -> " + finalTarget.loc);
                    Player.UseLocationAbility(this, Abilities.BLINK, bestBlinkPath.blinkLoc);
                    attack(finalTarget,!hasDoneTurn);

                    if (canMove()) {
                        calcMove();
                        doMove();
                    }
                }
            }
        }


        else {
            if(bestBlinkPath != null){
                Debug.log(this + " too scared to blink " + finalBlinkPathScore + "  " + finalTargetScore);
            }
            attackAndMove();
        }

        stayAliveOverchargeRequests();
    }

    private class BlinkPath{
        MainLoc blinkLoc = null;
        MainLoc moveLoc = null;
        MainLoc finalLoc = null;
        boolean blinkFirst = false;

        public BlinkPath(MainLoc blinkLoc, MainLoc moveLoc, MainLoc finalLoc, boolean blinkFirst){
            this.blinkLoc = blinkLoc;
            this.moveLoc = moveLoc;
            this.finalLoc = finalLoc;
            this.blinkFirst = blinkFirst;
        }

    }
}
