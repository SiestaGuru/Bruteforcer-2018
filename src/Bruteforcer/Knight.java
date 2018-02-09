package Bruteforcer;

import java.util.ArrayList;

public class Knight extends Dpser {

    public static int myKnightDefense = 5;
    public static int theirKnightDefense = 5;


    public Knight( int id, boolean isMine){
        super(Type.KNIGHT,id,isMine);
        amIDps = true;
        amIKnight = true;
    }





//
//
//
//                if(!bloodlust) {
//                    if (scaryEnemyRobotsNearSize > 0) {
//                        bloodlust = true;
//                        Debug.log("BLOODLUST RAGE");
//                    }
//                    if (!bloodlust) {
//                        for (Robot r : Map.getRobotsInRangeFilterId(new Circle(loc, 12), id)) {
//                            if (r.isMine && r.amIKnight && ((Knight) r).bloodlust) {
//                                bloodlust = true;
//                                Debug.log("BLOODLUST INFECTED");
//                                break;
//                            }
//                        }
//                    }
//                }else{
//                    if(R.turn - lastSeenEnemy > 20){
//                        bloodlust = false;
//                        Debug.log("Back to peaceful");
//                    }
//                }








    @Override
    public void init(){}
    @Override
    public void preturn(){}
    @Override
    public void think(){



        //Movement.addGoal(new Loc(R.random.nextInt(Map.width), R.random.nextInt(Map.height)), 5);

        if(health < Type.MAGE.damage){
            bloodlust = false;
        }


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

        }

        if(bloodlust){
            addDangerDesire(-1,health + 1,0);
            M.addGoal(getControlMapGoal(6,1,0.1,1),10);


            int closest = 999;
            Robot closestrob = null;
            for(Robot r : Map.theirRobots){
                if(!r.amIWorker){
                    int dist = Map.findDistanceTo(loc,r.loc);
                    if(r.amIFactory){
                        dist -= 4;
                    }
                    if(dist < closest){
                        closest = dist;
                        closestrob = r;
                    }
                }
            }
            if(closestrob != null){
                M.addGoal(closestrob.loc,5);
            }

        }else {
            addDangerDesire(-5,health + 1,-0.3);
            M.addGoal(getControlMapGoal(3,1,1,1),10);

        }


        boolean someoneInAttackRange = false;

        for(Robot r : enemyRobotsInAttackRange){
            if(!r.isDead && !r.amIWorker){
//                Debug.log(this + " has " + r);
                someoneInAttackRange = true;
            }
        }


        boolean shouldHyperFocusFactories = Map.theirUnitCounts[Type.FACTORY.typeId] < 4 && R.turn < 130;


        if(shouldHyperFocusFactories) {
            double bestfactoryscore = 0;
            Factory bestfactory = null;
            for (Factory f : Map.theirFactories) {

                double score = 100 - Map.findDistanceTo(loc, f.loc);

                if (f.amIBlueprint) {
                    score += 3; //easier to kill
                }

                double control = MapMeta.getControlOf(f.loc);

                if (control > 10) {
                    score -= 5; //already killing it?
                } else if (control < -10) {
                    score -= 10; //too hard
                }

                if (score > bestfactoryscore) {
                    bestfactoryscore = score;
                    bestfactory = f;
                }
            }
            if (bestfactory != null) {
                M.addGoal(bestfactory.loc, 20);
            }
        }

        boolean dodge = !canAttack() || someoneInAttackRange;

        if(canMove()) {
            for (Robot r : Map.getEnemiesInRange(new Circle(loc, Type.RANGER.atPlusMoveSquaredApprox))) {

                int count = r.robotsInAttackRange.size();
                if (r.robotsInAttackRange.contains(this)) {
                    count--;
                }
                double scaryfactor = 0.5 + 0.5 * (1.0 / (count + 1));


                double desire = 0;
                if (r.amIRanger) {
                    desire += 400;
                } else if (r.amIHealer) {
                    desire += 400;
                } else if (r.amIFactory) {
                    if (shouldHyperFocusFactories) {
                        desire += 700;
                    } else {
                        desire += 500;
                    }
                } else if (r.amIMage) {
                    desire += 1000;
                } else if (r.amIWorker) {
                    desire += 10;
                } else if (r.amIKnight) {

                    if (r.health < health || (r.health + Knight.theirKnightDefense) < damage ) {
                        desire += 400;
                    } else {
                        desire += 50;
                        scaryfactor += 0.25;
                    }

                    if (dodge) {
                        desire *= 0.25;
                        scaryfactor += 1;
                    }

                }

                if (!r.amIWorker) {
                    if (r.health < damage) {
                        desire += 400;
                    }
                    if (r.health < 200) {
                        r.updateTheoreticalDamage();
                        if (r.health <= r.theoreticalDamage) {
                            desire += 200;
                        }
                    }
                }

                int realdist = Map.findDistanceTo(loc, r.loc);
                if (bloodlust) {
                    desire += (r.maxHealth - r.health) * 5.0;
                    if (realdist < 8 || (realdist < 2 * (11 - r.scaryEnemyRobotsNearSize))) {
                        M.addGoal(r.loc, desire * 0.8);
                    }
                    if (r.amIMage) {
                        scaryfactor *= 2.5;
                    } else if(r.amIKnight){
                        scaryfactor *= 1.5;
                    }
                    M.addEnemyZone(r.approxAttackPlusMoveCircle, r.attackRange, attackRange, desire * 3, scaryfactor * (Math.max(0,r.damage) * -2));


                } else {
                    desire += (r.maxHealth - r.health) * 2.0;
                    if (realdist < 5 || (scaryEnemyRobotsNearSize < 2 && realdist < 7)) {
                        M.addGoal(r.loc, desire * 0.3);
                    }
                    M.addEnemyZone(r.approxAttackPlusMoveCircle, r.attackRange, attackRange, desire * 2, scaryfactor * (Math.max(0,r.damage) * -6));
                }

                if(r.amIKnight){
                    if(health + myKnightDefense < Type.KNIGHT.damage){
                        if(r.health + theirKnightDefense < Type.KNIGHT.damage && canAttack() && Map.findDistanceTo(loc,r.loc) <= 2){
                            M.addCircle(r.loc,2,5000);
                        }else{
                            M.addVectorCircle(r.loc,8,-5000,-200);
                        }
                    }
                }
            }


            boolean enemymagenear = false;
            for (Robot r : Map.getEnemiesInRange(new Circle(loc, 50))) {
                if (r.amIMage) {
                    enemymagenear = true;
                }
            }
            if (enemymagenear || R.SPREAD) {
                double amount = -20;
                if (enemymagenear) {
                    amount = -100;
                }
                for (Robot r : Map.getAlliesInRange(new Circle(loc, 8))) {
                    M.addVector(r.loc, -5);
                    M.addCircle(r.loc, 1, amount);
                }
            }
        }



        attackAndMove();


        if(Techs.canJavelin){
            for(Robot r : Map.getEnemiesInRange(new Circle(loc,10))){
                if(Player.UseTargetedAbility(this, Abilities.JAVELIN,r.id)){
//                  Debug.log("Javelin");
                    break;
                }
            }
        }

        stayAliveOverchargeRequests();

    }

}
