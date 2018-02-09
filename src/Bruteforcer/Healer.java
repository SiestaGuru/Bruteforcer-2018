package Bruteforcer;


import java.util.ArrayList;

public class Healer extends Robot {


    public double overchargeDamagePotential = 0;
    public boolean overchargeSpend = false;


    public ArrayList<Loc> heallocRequests = new ArrayList<>();

    public Healer(int id, boolean isMine){
        super(Type.HEALER,id,isMine);
        amIHealer = true;
    }

    @Override
    public void init(){}
    @Override
    public void preturn(){}

    public static int healerHealing = -Type.HEALER.damage;





    private int lastHealTargetId = -1;

    @Override
    public void think(){

        if(!inGarrison) {
            tryHealing(canMove());
        }

        for(Loc l : heallocRequests){
            M.addGoal(l,4);
//            Debug.log(id+ " So bothersome " + l);
        }
        heallocRequests.clear();


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
                    M.addGoal(gotospot,2000);
                }else{
                    Loc l = getControlMapGoal(100, 0, 0, 20);
                    M.addGoal(l,2000);
                }

            }else {
                M.addGoal(getControlMapGoal(2.5, 0, 0, 1), 6);
                addDangerDesire(-6, health + 1,-2);
            }

            for (Robot r : Map.getEnemiesInRange(new Circle(loc, 100))) {

                if (r.amIRanger) {
                    M.addVectorCircle(r.approxAttackPlusMoveCircle, -400, -20);
                    if(!Techs.canOverload) {
                        //If we can overload, we want to stand closer, so we can do boom
                        M.addCircle(new Circle(r.loc, 90), -100);
                    }

                } else if (r.amIKnight) {
                    M.addVectorCircle(new Circle(r.loc, 30), -500, -20);
                } else if (r.amIMage) {
                    M.addVectorCircle(new Circle(r.loc, 50), -800, -20);
                }
            }

            double bestScore = -100;
            Robot bestHealTarget = null;
            for (Robot r : Map.myDamagedRobots) {
                if (!r.amIStructure && r.id != id && !r.inGarrison && !r.isDead) {
                    if(r.health < r.maxHealth - 5 || (r.amIKnight && ((Dpser)r).bloodlust )) {

                        int distToHeal =   Math.abs(Map.findDistanceTo(loc, r.loc) - 3);

                        double score = ((r.maxHealth - r.health) / 20.0) - (distToHeal);

                        if (r.amIWorker) {
                            score -= 10;
                        }

                        if(r.amIKnight && ((Knight)r).bloodlust ){
                            score += 8;
                        }

                        if(r.health <= Type.RANGER.damage * 2){
                            score += 2;
                        }

                        if(lastHealTargetId == r.id){
                            score += 2;
                        }

                        score += r.scaryEnemyRobotsNearSize * 5;

                        if (score > bestScore) {
                            bestScore = score;
                            bestHealTarget = r;
                        }
                    }

                }
            }

            boolean enemymagenear = false;
            for(Robot r : Map.getEnemiesInRange(new Circle(loc,50))){
                if(r.amIMage){
                    enemymagenear = true;
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

            if (bestHealTarget != null) {
                lastHealTargetId = bestHealTarget.id;
                double force = 12 + Math.max(0, (bestScore + 20) / 5.0);

                M.addGoal(bestHealTarget.loc, force);
                //Debug.log(id + "  healer tar: " + bestHealTarget + "  " + force);
            } else {
                lastHealTargetId = -1;
                //Todo: Probably spread out from healers and go towards dpsers
                M.addGoal(new Loc(PseudoRandom.next(Map.width), PseudoRandom.next(Map.height)), 2);
            }


            M.calcBestMove();
            doMove();
        }


        if(!inGarrison && canAttack()) {
            tryHealing(false);
        }


        if(!inGarrison && canAttack()){
            R.unusedHealers.add(this);
        }

//        stayAliveOverchargeRequests();

    }

    public void tryHealing(boolean canstillmove){


        if(!canAttack() && !canstillmove) return;
        double bestscore = 0;
        Robot bestorbot = null;



        for (Robot r : robotsInSight) { //not using robotsinattack because of movement
            if (r.isMine && !r.amIStructure && !r.inGarrison && !r.isDead) {
                if(r.health < r.maxHealth){
                    double score= ((double)(r.maxHealth - r.health));


                    int hypotheticalhealth = r.health;

                    for(Robot e : r.scaryEnemyRobotsNear){
                        if(e.amIDps){
                            hypotheticalhealth -= e.damage;
                        }
                    }

                    if(hypotheticalhealth <= 0){
                        if(hypotheticalhealth > -10){
                            score += 50;
                        }
                        else if(hypotheticalhealth < -100){
                            score -= 5; //lost cause
                        }
                        else {
                            score += 20;
                        }
                    }


                    if(r.amIHealer || r.amIRanger){
                        score += 20;
                    }

                    if(r.health < Type.RANGER.damage){
                        if(r.health + healerHealing > Type.RANGER.damage){
                            score += 40;
                        }else{
                            score += 20;
                        }
                    }


                    if(canstillmove){
                        if( attackCircle.contains(r.loc)){
                            if(score > bestscore){
                                bestscore = score;
                                bestorbot = r;
                            }
                        }else if(approxAttackPlusMoveCircle.contains(r.loc)){
                            if(score > bestscore){
                                bestscore = score;
                                bestorbot = r;
                            }
                        }
                    }
                    else{
                        if( attackCircle.contains(r.loc)){
                            if(score > bestscore){
                                bestscore = score;
                                bestorbot = r;
                            }
                        }

                    }
                }
            }
        }

        if(bestorbot != null){
            if(attackCircle.contains(bestorbot) && canAttack()) {
                if (Player.gc.canHeal(id, bestorbot.id)) {
                    Player.gc.heal(id, bestorbot.id);
                    bestorbot.health += healerHealing;
                    attackHeat += 10;
                }
            }else{
                M.addCircle(bestorbot.loc,attackRange,150);
            }
//            else{
//                Debug.log("Heal fail: " + this + " ->" + bestorbot);
//            }
        }


    }



    public static void fulfilloverchargeRequests() {
        if(R.overchargeReqs.size() > 0){
            for(Healer h : Map.myHealers){
                if(h.canUseAbility()) {
                    double score = -MapMeta.getControlOf(h.loc);
                    ObjectSorting.AddToSort(h, score);
                }
            }
            ArrayList<Object> sortedHealers = ObjectSorting.SortAscObjects();

            for(OverchargeRequest o : R.overchargeReqs){

                if(o.soThatWeCanKill != null) {
                    if (o.soThatWeCanKill.health > 0 && !o.requestFrom.isDead && !o.requestFrom.inGarrison && o.requestFrom.attackCircle.contains(o.soThatWeCanKill.loc)) {
                        o.updateShotsRequired();

                        boolean allow = true;
                        if (o.shotsReq > 3) {
                            if (o.shotsReq > 5 && !R.heavilystuck && (R.turn - o.requestFrom.lastMoved) < 20) {
                                //If we can still move, let's not use 6+ overcharges just for one kill
                                //If stuck though, maybe this'll get us out of that position
                                allow = false;
                            } else if (Techs.canBlink && !R.heavilystuck) {
                                allow = false;
                            } else {
                                for (Mage m : Map.myMages) {
                                    if (Map.findDistanceTo(m.loc, o.requestFrom.loc) < 10) {
                                        //Don't want to make it impossible to do mage pathing
                                        allow = false;
                                    }
                                }
                            }
                        }

                        if (allow) {
                            double score = -o.shotsReq;
                            if (o.soThatWeCanKill.amIStructure) {
                                score += 2;
                            }
                            ObjectSorting.AddToSort(o, score);
                        }
                    }
                }else{
                    if(o.requestFrom.health < MapMeta.enemyDamageArrayReal[o.requestFrom.x][o.requestFrom.y]) {
                        o.shotsReq = 1;
                        ObjectSorting.AddToSort(o, -3);
                    }
                }
            }

            ArrayList<Object> sortedRequests = ObjectSorting.SortDescObjects();


            if(sortedHealers.size() > 0){
                //Those healers that are least important should fulfill requests whenever possible
                for(Object obj  : sortedRequests){
                    try {
                        OverchargeRequest req = (OverchargeRequest) obj;
                        req.updateShotsRequired();

                        if (req.shotsReq > 0) {
                            ArrayList<Healer> usingHealers = new ArrayList<>();
                            for (Object o : sortedHealers) {
                                Healer h = (Healer) o;
                                if (h.loc.isWithinSquaredDistance(req.requestFrom.loc, h.abilityRange) && h.canUseAbility()) {
                                    usingHealers.add(h);

                                    if (usingHealers.size() >= req.shotsReq) {
                                        break;
                                    }
                                }
                            }
                            if (usingHealers.size() >= req.shotsReq) {

//                                if(req.soThatWeCanKill != null) {
//                                    Debug.log("Overcharging" + req.requestFrom + " on request to kill " + req.soThatWeCanKill + " using " + usingHealers.size() + " healers");
//                                }

                                for (Healer h : usingHealers) {
                                    if(req.requestFrom.health > 0) {
                                        if (req.soThatWeCanKill != null) {
                                            if (req.soThatWeCanKill.health > 0) {
                                                Player.gc.overcharge(h.id, req.requestFrom.id);
                                                h.abilityHeat += h.abilityCd;
                                                ((Dpser) req.requestFrom).attack(req.soThatWeCanKill, false);
                                                req.requestFrom.moveHeat = 0;
                                                req.requestFrom.abilityHeat = 0;

                                                req.requestFrom.M.reset();
                                            }
                                        } else {
                                            Debug.lineBreak();
//                                            Debug.log("Saving: " + req.requestFrom.loc + " -> " + req.locationHint);
                                            Debug.lineBreak();
                                            Player.gc.overcharge(h.id, req.requestFrom.id);
                                            h.abilityHeat += h.abilityCd;

                                            req.requestFrom.moveHeat = 0;
                                            req.requestFrom.abilityHeat = 0;
                                            req.requestFrom.attackHeat = 0;

                                            req.requestFrom.M.reset();

                                            if(req.locationHint != null) {
                                                req.requestFrom.M.addSpecialLocation(req.locationHint, 1000);
                                            }

//                                            if(req.locationHint != null && !req.locationHint.containsRobot()  && req.requestFrom.loc.isWithinOffset(req.locationHint,1)){



                                                //                                                Player.QueueMovementAction(req.requestFrom, req.locationHint.getDirectionFrom(req.requestFrom.loc));
//                                                req.requestFrom.moveHeat = req.requestFrom.moveCd;
//                                                req.requestFrom.updateLocation(req.locationHint, true, true);
//                                            }
                                        }
                                    }
                                }

                                req.requestFrom.think();
                            }
                        }
                    }catch (Exception ex){
                        Debug.log(ex);
                    }
                }


            }
        }
    }


    public static void overcharges(){
        if(true)return; //deprecated


//        Debug.log("Overcharge logic");
        if(R.POWERSAVEMODUSHYPER) return;
        if(R.POWERSAVEMODUS && R.turn % 5 != 0) return;

        ArrayList<Healer> overchargers = new ArrayList<>();

        for(Healer r : Map.myHealers){
            if(r.canUseAbility()){
                overchargers.add(r);
            }
        }

        if(overchargers.size() > 0){
            for(Robot r : Map.myRobots){
                if(r.amIDps){
                    double score = getOverchargeScore(r);
                    if(score > 0){
                        ObjectSorting.AddToSort(r,score);
                    }
                }
            }
        }


        ArrayList<Object> sortedTargets = ObjectSorting.SortDescObjects();
//        Debug.log("Overcharge chargers: " + overchargers.size() + " Targets: " + sortedTargets.size());
        if(sortedTargets.size() > 0 ) {
            int overchargeradius = Type.HEALER.activeRange;

            healerloop:
            for (Healer h : overchargers) {
                Loc l = h.loc;
                for (Object o : sortedTargets) {
                    Robot r = (Robot) o;
                    if (l.isWithinSquaredDistance(r, overchargeradius)) {
                        if (getOverchargeScore(r) > 0) {
                            if (Player.UseTargetedAbility(h, Abilities.OVERCHARGE, r.id)) {
//                                Debug.log(h.id + " Overcharge -> " + r.loc);
                                r.M.reset();
                                r.moveHeat = 0;
                                r.abilityHeat = 0;
                                r.attackHeat = 0;
                                r.think();
                                continue healerloop;
                            }
                        }
                    }
                }
            }
        }
    }

    private static double getOverchargeScore(Robot r){
        double score = -200;


        if(R.turn - r.lastSeenEnemy < 2) {
            if (!r.canMove()) {
                score += 50;
                score += Math.abs(Math.min(-1800, r.M.bestmovescore) + 1800) * 0.03;
            }
            if (!r.canAttack()) {
                score += 50;

                for (Robot r2 : r.robotsInApproxAttackRange) {
                    score += 5;
                    if (r2.health < r.damage) {
                        score += 100;
                    }
                }
            }
            score += r.abilityHeat * 0.3;

            score += (r.maxHealth - r.health) * 0.2;

            if(r.amIMage){
                if(Techs.canBlink || r.robotsInApproxAttackRange.size() > 0) {
                    score += 40;
                }
            }
        }


        return score;
    }
}
