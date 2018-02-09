package Bruteforcer;

import java.util.ArrayList;

public class Ranger extends Dpser {


    public int beganSnipe = -10000;
    private boolean someoneInAttackRange;
    private boolean healthbelow3hits;
    private boolean healthbelow2hits;
    private boolean healthbelow1hit;

    public Ranger(int id, boolean isMine){
        super(Type.RANGER,id,isMine);
        amIRanger = true;
    }

    @Override
    public void init(){}
    @Override
    public void preturn(){}
    @Override
    public void think(){
       // Movement.addGoal(new Loc(R.random.nextInt(Map.width), R.random.nextInt(Map.height)), 5);


        if(R.turn - beganSnipe <= 5){
            return;
        }


        someoneInAttackRange = false;

        if(canMove()) {

            boolean crawlMode = scaryEnemyRobotsNearSize > 0 && MapMeta.healerSupportArray[x][y] > 0 && (R.CrawlMode  || attackCircle.containingAlliedRobots(Type.RANGER).size() > 5);

            boolean forcedintocorner = false;
            int buddies = 0;
            for (Robot r : Map.getAlliesInRange(new Circle(loc, 50))) {
                if (r.amIDps) {
                    buddies++;
                } else if (r.amIFactory && scaryEnemyRobotsNearSize >= 2 && Map.myUnitCounts[Type.FACTORY.typeId] < 5) {
                    if (loc.isWithinSquaredDistance(r, 20)) {
                        forcedintocorner = true;
                    }
                }
            }
            if (forcedintocorner && !crawlMode && buddies > 3) {
                crawlMode = true;
            }



            if (canAttack()) {
                for (Robot r : enemyRobotsInAttackRange) {
                    if (!r.isDead && (r.amIDps || r.amIHealer)) {
                        someoneInAttackRange = true;
                    }
                }
            }




            boolean lookingForhealer = false;
            if (!R.POWERSAVEMODUS) {
                if (health < maxHealth && health <= Type.RANGER.damage * 5) {

                    if(bloodlust) {
                        lookingForhealer = addFindHealerGoal(4 + ((double) (maxHealth - health) / 10.0), false);
                    }else{
                        lookingForhealer = addFindHealerGoal(8 + ((double) (maxHealth - health) / 5.0), R.turn < 120);
                    }
                    if (health <= Type.RANGER.damage * 3) {
                        healthbelow3hits = true;
                        if (health <= Type.RANGER.damage * 2) {
                            healthbelow2hits = true;

                            healthbelow1hit = health <= Type.RANGER.damage;
                        }
                    }
                }
            }


            if(R.SUICIDERUSH){
                Debug.log("BALAAARGHHAFDDSSFDDS");
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
                double weightstrife = 2;
                double basereespaceWeight = 0.6;
                if (bloodlust) {
                    weightstrife += 2;
                }
                if (lookingForhealer) {
                    weightstrife -= 3;
                }
                if (R.DefensiveStance) {
                    weightstrife -= 1;
                    basereespaceWeight -= 0.8;
                }


                if (EnemyStrategy.Strategy == EnemyStrategy.FULLKNIGHT || EnemyStrategy.Strategy == EnemyStrategy.KNIGHTHEALER || EnemyStrategy.Strategy == EnemyStrategy.KNIGHTRANGER) {
                    M.addGoal(getControlMapGoal(weightstrife, 0.6, basereespaceWeight, 1), 20 + 30 * R.beBallsyRating);
                } else if (EnemyStrategy.Strategy == EnemyStrategy.MAGEHEAVY || EnemyStrategy.Strategy == EnemyStrategy.MAGERANGER) {
                    M.addGoal(getControlMapGoal(weightstrife, 1, basereespaceWeight + 1, 1), 20 + 30 * R.beBallsyRating);
                } else {
                    if(MapMeta.anyActivityDetected) {
                        M.addGoal(getControlMapGoal(weightstrife - 0.3, 1, basereespaceWeight + 0.4, 1), 20 + 30 * R.beBallsyRating);
                    }else{
                        //Scout a bit more, make sure they're not hiding anywhere
                        M.addGoal(getControlMapGoal(weightstrife - 0.3, 1, basereespaceWeight + 0.8, 0.6), 30 + 30 * R.beBallsyRating);
                    }
                }
            }

            if(R.DefensiveStance){
                double bestfactoryscore = 0;
                Factory bestfactory = null;
                for(Factory f : Map.myFactories){
                    double score= (f.threatScore * 0.25) - Map.findDistanceTo(loc,f.loc);
                    if(score > bestfactoryscore){
                        bestfactoryscore = score;
                        bestfactory = f;
                    }
                }
                if(bestfactory != null){
                    M.addGoal(bestfactory.loc,8);
//                    Debug.log("Defending: " + bestfactory.loc);
                }
            }




            healthbelow1hit = false;
            healthbelow2hits = false;
            healthbelow3hits = false;



            if (!R.POWERSAVEMODUSHYPER) {
                boolean healerbackup = MapMeta.healerSupportArray[x][y] > 0;

                for (Loc l : adjacentPassableTilesIncludingThis) {
                    double canbeHitDamage = MapMeta.enemyDamageArray[l.x][l.y];

                    double canBeHitWithoutEnemyMove = 0;

                    for (Robot r : scaryEnemyRobotsNear) {
                        if (r.attackCircle.contains(l)) {
                            canBeHitWithoutEnemyMove += r.damage;
                        }
                    }

                    double scary = canbeHitDamage * 0.3 + canBeHitWithoutEnemyMove * 0.7;

                    if (healerbackup) {
                        scary /= 2.0;
                    }
                    if (healthbelow2hits) {
                        if (canBeHitWithoutEnemyMove > 0) {
                            scary += 200.0;
                        } else {
                            scary += 100.0;
                        }

                        if (healthbelow1hit) {
                            if (canBeHitWithoutEnemyMove > 0) {
                                scary += 500.0;
                            } else {
                                scary += 300.0;
                            }
                        }
                    }
                    if(bloodlust){
                        scary /= 4;
                    }
                    M.addSpecialLocation(l, -scary);
                }
            }


            //Debug.log("size: " + robotsInSight.size());
//            if (R.SPREAD) {
//                M.addSpecialLocation(loc, -30); //to dodge snipe etc.
//            }






            if(inGarrison){
                M.addSpecialLocation(loc,-50);
            }
            else if(crawlMode){
                //Not moving is often a good idea to conserve the ability to move next turn
                M.addSpecialLocation(loc,  Math.min(15 * (scaryEnemyRobotsNearSize + 1),80));
            }
            else{
                M.addSpecialLocation(loc,Math.min(5 * (1 + scaryEnemyRobotsNearSize ),25));
            }


            int cutoff = health + 1 + Type.RANGER.damage;




            double visionextra =  Math.min(0, -1 + ((double)health / (double)maxHealth)  + R.beBallsyRating);



            double dangerextra = 0;



            if(bloodlust){
                addDangerDesire((1.5 - R.beBallsyRating) * Math.min(-0.5,-1.0 + dangerextra), cutoff,-0.2 + visionextra);
            }else if(crawlMode) {
                addDangerDesire((1.5 - R.beBallsyRating) * (-3.0 + dangerextra), cutoff,-1 + visionextra);
            }else{
                addDangerDesire((1.5 - R.beBallsyRating) * (-5.0 + dangerextra), cutoff,-0.4 + visionextra);
            }



            if(R.POWERSAVEMODUSHYPER){
                for (Robot r : robotsInSight) {
                    if (!r.isMine) {
                        M.addEnemyZone(r.approxAttackPlusMoveCircle, r.attackRange, attackRange, 500, -500);
                    }
                }
            }else {
                Loc factoryShootSpot = null;
                int bestdist = 15;
                for(Factory f : Map.theirFactoriesById.values()){
                    for(Loc l : f.rangerShootSpots){
                        int dist = Map.findDistanceTo(loc,l);
                        if(dist < bestdist){
                            bestdist = dist;
                            factoryShootSpot = l;
                        }
                    }
                }
                if(factoryShootSpot != null){
//                Debug.log("FOUND a FACTORY shoot spot " + factoryShootSpot);
                    M.addGoal(factoryShootSpot,10);
                    M.addSpecialLocation(factoryShootSpot,40);
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


                for (Robot r : C.expandToIncludeGarrisoned(robotsInSight)) {
                    if (!r.isMine) {
                        if (!r.isDead) {


                            double scaryfactor = 1;
                            double defendfriendsfactor = 1;

                            if(r.amIDps) {
                                double count = 0;

                                for(Robot r2 : r.robotsInApproxAttackRange){
                                    if(r2.isMine && r2.id != id ){
                                        if((r2.amIStructure || r2.amIHealer)) {
                                            defendfriendsfactor *= 1.4;
                                        }
                                        double factor = 1.0;
                                        if(r2.amIWorker) {
                                            factor *= 0.3;
                                        }
                                        if(!r.loc.isWithinSquaredDistance(r,r.attackRange)){
                                            factor *= 0.5;
                                        }
                                        count+=factor;
                                    }
                                }
                                //This makes a robot less scary if it already has a bunch of other targets it can fire on
                                //No need to have 10 robots all whimper in fear because of one enemy ranger
                                if(r.amIMage) {
                                    scaryfactor = 0.7 + 0.3 * (1.0 / (count + 1));
                                }else{
                                    scaryfactor = 0.4 + 0.6 * (1.0 / (count + 1));
                                }
                            }

                            if(bloodlust || forcedintocorner){
                                M.addVector(r.loc, (20 + 5 * R.beBallsyRating)*defendfriendsfactor);
                                M.addGoal(r.loc, 20 * defendfriendsfactor);
                            }else {
                                M.addVector(r.loc, 5 * R.beBallsyRating * defendfriendsfactor);
                            }

                            double damagethem = 350 + (r.maxHealth - r.health);
                            if (r.health < damage * 2) {
                                damagethem += 20;
                                if (r.health < damage) {
                                    damagethem += 200;
                                }
                            }

                            damagethem += R.beBallsyRating * 100;

                            if(bloodlust){
                                damagethem += 400;
                            }

                            if(r.inGarrison){
                                damagethem = 0;
                            }

                            if(canAttack()){
                                r.updateTheoreticalDamage();
                                damagethem += r.theoreticalDamage * 2;
                                if(r.health <= r.theoreticalDamage){
                                    damagethem += 250;
                                }
                                else if (r.health <= r.theoreticalDamageIncludingNextTurn - 30) {
                                    damagethem += 90;
                                }
                                else if(r.health <= r.theoreticalDamageIncludingNextTurn){
                                    damagethem += 40;
                                }

                                if(someoneInAttackRange){
                                    if(crawlMode){
                                        damagethem *= 0.2;
                                    }else {
                                        damagethem *= 0.4;
                                    }
                                }
                            }else{
                                if(crawlMode){
                                    //dont back up too much in crawl mode
                                    damagethem *= 0.75;
                                }else {
                                    //step out of range please
                                    damagethem *= 0.15;
                                }
                            }

                            damagethem *= defendfriendsfactor;

                            if (r.amIRanger) {
                                damagethem *= 0.8;

                                double extraScary = 0;
                                if(health < maxHealth) {
                                    extraScary += 50;
                                    if (healthbelow3hits) {
                                        extraScary += 200;
                                        if (healthbelow2hits) {
                                            extraScary += 400;
                                            if (healthbelow1hit) {
                                                extraScary += 8000;
                                            }
                                        }
                                    }
                                }
                                extraScary += Math.min(scaryEnemyRobotsNearSize,6) * 30;

                                double scary = ((-500 * (1.5 - R.beBallsyRating)) - extraScary) * 0.35 * scaryfactor;

                                if(r.attackHeat < 10){
                                    scary *= 1.2;
                                }else{
                                    scary *= 0.8;
                                }

                                if(r.moveHeat >= 20){
                                    scary *= 0.8;
                                    damagethem *= 1.2;
                                } else if(r.moveHeat >= 10){
                                    scary *= 0.95;
                                    damagethem *= 1.05;
                                }

                                if(buddies == 0){
                                    scary *= 1.4;

                                    if(health < r.health && MapMeta.healerSupportArray[x][y] == 0){
                                        scary *= 1.5;
                                    }
                                }

                                if(bloodlust){
                                    scary *= 0.6;
                                    scary = Math.max(40,scary - 100);
                                }


                                if(crawlMode){
                                    damagethem *= 2.5;
                                    scary *= 1.5;
                                }



                                M.addEnemyZone(r.approxAttackPlusMoveCircle, r.attackRange, attackRange, damagethem, scary);

                                if(damagethem - scary < 500){
                                    //In most cases, we want to get too close
                                    M.addVectorCircle(r.loc,36,-400,-100);
                                }

                                M.addVector(r.loc,(scary + damagethem) * 0.2);

                                if(health < maxHealth * 0.8){
                                    M.addCircle(r.loc,36,-800); //try not to step on a spot it takes more than a single move to escape from
                                }

                            } else if (r.amIDps) {
                                if (r.amIKnight) {

                                    boolean cantheyRushUs = Map.findDistanceTo(loc,r.loc) <= 6;

                                    damagethem *= 0.3;
                                    if(cantheyRushUs) {
                                        M.addVectorCircle(new Circle(r.loc, 34), -500, -50);
                                    }

                                    M.addEnemyZone(r.approxAttackPlusMoveCircle, r.attackRange, attackRange, damagethem, -1000 * scaryfactor);

                                    if(cantheyRushUs && buddies == 0 && r.scaryEnemyRobotsNearSize < 2){
                                        //Try to step out of their vision range
                                        M.addVector(r.loc,-100);
                                        M.addEnemyZone(new Circle(r.loc,50),50,0,0,-1500);
                                    }

                                } else {
                                    damagethem *= 0.8;
                                    M.addEnemyZone(r.approxAttackPlusMoveCircle, r.attackRange, attackRange, damagethem, -750 * scaryfactor);
                                }
                                M.addVectorCircle(new Circle(r.loc, 17), -500, -30); //If theyre in this circle, they can move to within our non-attack range
                            } else {

                                if(r.amIFactory){
                                    damagethem *= (3 + ((Structure)r).garrisonedWithin.size()) ;

                                } else if(r.amIRocket){
                                    if(R.amIEarth){
                                        damagethem *= (0.6 + ((Structure)r).garrisonedWithin.size());
                                    }else{
                                        damagethem *= (0.2 + ((Structure)r).garrisonedWithin.size());
                                    }
                                } else if(r.amIWorker){
                                    damagethem *= 0.25;
                                }

                                M.addEnemyZone(attackCircle, r.attackRange, attackRange, damagethem, 0);
                            }

                            if(!r.amIWorker) {
                                M.addVectorCircle(new Circle(r.loc, 20), -500, -50); //Our non-attack range (Plus move)
                                M.addCircle(new Circle(r.loc, 10), -200); //Our non-attack range
                            }
                        }
                    } else {


                        if(GrandStrategy.strategy == GrandStrategy.KNIGHTRUSH && R.turn < 150  ){
                            if(r.amIMage) {
                                //Be a good vision buddy
                                M.addGoal(r.loc, 10);
                                M.addVector(r.loc, 3);
                                M.addCircle(r.loc, 5, 100);
                            }else if(r.amIKnight){
                                M.addGoal(r.loc, 6);
                                M.addVector(r.loc, 2);
                                M.addCircle(r.loc, 5, 50);
                            }

                        }
                        else if(R.DefensiveStance){
                            if(r.amIDps || r.amIHealer){
                                M.addVector(r.loc,2);
                                M.addCircle(r.loc, 4,30);
                            }
                        }
                        else {
                            M.addVector(r.loc, -3);
                        }


                    }
                }

                if(crawlMode && buddies > 10 &&  R.turn > 220 && Worker.nonsuicidalWorkersCount > 10 && !R.hyperVigilanceHasBeenTriggered){
                    for(Robot r : loc.getAdjacentFriendlyRobots(Type.WORKER)){
                        ((Worker) r).goKillYourself += 1.0;
                        if(--Worker.nonsuicidalWorkersCount <= 10){
                            break;
                        }
                    }
                }

            }
        }




        attackAndMove();

        stayAliveOverchargeRequests();


//        if(!R.player1) {
//            if (Techs.canSnipe && canUseAbility()) {
//
//                if(Map.theirRobots.size() > 0) {
//                    Loc loc = Map.theirRobots.get(PseudoRandom.next(Map.theirRobots.size())).loc;
//                    Debug.log("Sniping");
//                    if(Player.UseLocationAbility(this, Abilities.SNIPE, loc)){
//
//                        beganSnipe = R.turn;
//                    }else{
//                        Debug.log("Failed?");
//                    }
//                }
//
//            }
//        }

    }


    public void Snipe(Loc l) {
        if (Player.UseLocationAbility(this, Abilities.SNIPE, l)) {
//            Debug.log(this + " is sniping: " + l);
            beganSnipe = R.turn;
        }
//        else {
//            Debug.log("Failed?");
//        }
    }






    public static void SnipeVolley(){
        ArrayList<Ranger> participants = new ArrayList<>();


        if(R.POWERSAVEMODUSHYPER){
            int theirsize = Map.theirRobots.size();
            if(theirsize > 0) {
                for (Ranger r : Map.myRangers) {
                    if (!r.inGarrison && (R.turn - r.lastSeenEnemy > 3 || R.turn - r.lastShotAtEnemy > 10) && r.canUseAbility() && MapMeta.getControlOf(r.loc) > 0) {
                        r.Snipe(Map.theirRobots.get(PseudoRandom.next(theirsize)).loc);
                    }
                }
            }
            return;
        }


        for (Ranger r : Map.myRangers) {
            if (!r.inGarrison && (R.turn - r.lastSeenEnemy > 3 || R.turn - r.lastShotAtEnemy > 10) && r.canUseAbility() && MapMeta.getControlOf(r.loc) > 0) {
                participants.add(r);
            }
        }


        boolean shouldFocusHealers = Map.theirUnitCounts[Type.HEALER.typeId] < Map.theirUnitCounts[Type.RANGER.typeId] * 2;




//        Debug.log("Starting snipe volley, participants: " + participants.size() + "  out of: " + Map.myRangers.size());

        if(participants.size() == 0) return;

        Ranger[] participantsArray = participants.toArray(new Ranger[participants.size()]);

        for(Robot r : Map.theirRobots){
            double score =  Math.min(10,R.turn - r.lastMoved) * 10;

//            Debug.log("This target moved: " + (R.turn - r.lastMoved) + " turns ago");

            if(r.amIHealer){
                if(shouldFocusHealers) {
                    score += 50;
                }else{
                    score += 25;
                }
            } else if(r.amIFactory || r.amIRocket){
                score += 80;
            } else if(r.amIRanger){
                score += 35;
            } else if(r.amIMage){
                score += 30;
            }

            for(Loc l : r.adjacentTiles){
                if(!l.isPassable()){
                    score += 5; //No ability to run
                }
                else if(!Map.inSight[l.x][l.y]){
                    score += 8; //Rather likely to contain a unit
                }
                else if(Map.robots[l.x][l.y] != null){
                    if(Map.robots[l.x][l.y].isMine){
                        score -= 30;
                    }else{
                        score += 10;
                    }
                }
            }

            score += 5 * (8 - r.adjacentTiles.length); //off the map

//            score -= (r.maxHealth - r.health) * 0.02; //it's actually somewhat better to shoot high health targets, because we'll know they won't get healed up while we're sniping so we miss out on a kill
            score -= r.scaryEnemyRobotsNearSize * 5; //we're already killing this

            ObjectSorting.AddToSort(r,score);
            r.expectedHealthAfterVolley = r.health;
        }


//        ArrayList<Object> sortedBots = ObjectSorting.SortDescObjects();
        ArrayList<ObjectSorting> sortedBots = ObjectSorting.SortDesc();

        int participantCounter = 0;

        outerloop:
        for(ObjectSorting target : sortedBots){

            Robot r = ((Robot) target.internalobj);
            while(r.expectedHealthAfterVolley > 0){
//                Debug.log(participantsArray[participantCounter] + " -> " + r);
                participantsArray[participantCounter++].Snipe(r.loc);

                r.expectedHealthAfterVolley -= Type.RANGER.damage;
                if(participantCounter >= participantsArray.length ){
                    break outerloop;
                }
            }
//            if(r.expectedHealthAfterVolley <= 0){
//                Debug.log("Expecting unit at: " + r.loc + " to die soon, score: " + target.score);
//            }
        }

    }
}
