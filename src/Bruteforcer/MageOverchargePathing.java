package Bruteforcer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public class MageOverchargePathing {


    public static ArrayList<Robot>[][] hittablesmap;


    public static int totalPathsfound = 0;
    public static int totalAttacks = 0;
    public static int totalOverchargesUsed = 0;
    public static int totalScore = 0;
    public static double totalTime = 0;
    public static int totalIterations = 0;


    public static void Think(int recursion){

        if(R.turn % 100 == 0){
            Debug.log("Plans: " + totalPathsfound + " Attacks: " + totalAttacks + " Score: " + totalScore + " Overcharges: " + totalOverchargesUsed + " Time: " + totalTime + " iters:" + totalIterations);
        }

        int turn = 1;

//        if(R.player1){
//            turn = 2;
//        }

        if(R.POWERSAVEMODUSHYPER) return;
        if(R.turn % 5 != turn) return;

        if(!Techs.canOverload) return;
        if(Map.myHealers.size() == 0) return;
        if(Map.theirRobots.size()  == 0) return;
        if(recursion > 3) return;


        long begintime = System.nanoTime();



        int canMoveMages = 0;
        mageloop:
        for(Mage m : Map.myMages){
            //requiring move/blink to be online for convenience,  already complex enough as is
            for(Loc l : m.adjacentPassableTiles){
                if(!l.containsRobot()   ){
                    canMoveMages++;
                    continue mageloop;
                }
            }
        }
        if(canMoveMages == 0){
            return;
        }


//        Debug.log("Calculating plan");


        int[][] overchargemap = new int[Map.width][Map.height];
        double[][] damagedesiremap = new double[Map.width][Map.height];
        ArrayList<Healer>[][] overchargersmap = new ArrayList[Map.width][Map.height];
        ArrayList<Healer>[][] sortedoverchargersmap = new ArrayList[Map.width][Map.height];
        ArrayList<Healer>[][] movingoverchargersmap = new ArrayList[Map.width][Map.height];
        hittablesmap = new ArrayList[Map.width][Map.height];

        int overchargeSquaredRadius = Type.HEALER.activeRange;
        int overchargePlusMoveSquaredRadius = Type.HEALER.atPlusMoveSquaredApprox;
        int overchargeMaxReach = 6;

        ArrayList<Healer> overchargers = new ArrayList<>();

        //Calcs: Amount of overcharge healers * 121
        for(Healer h  : Map.myHealers){
            if(h.canUseAbility()) {
                overchargers.add(h);
                h.overchargeDamagePotential = 0;
                int x = h.x;
                int y = h.y;
                int maxx = Math.min(x + overchargeMaxReach, Map.widthMinusOne);
                int maxy = Math.min(y + overchargeMaxReach, Map.heightMinusOne);

                for (int x1 = Math.max(x - overchargeMaxReach, 0); x1 <= maxx; x1++) {
                    int dx = x1 - x;

                    for (int y1 = Math.max(y - overchargeMaxReach, 0); y1 <= maxy; y1++) {
                        if (!Map.blocked[x1][y1] && Map.robots[x1][y1] == null) {
                            int dy = y1 - y;
                            if (dx * dx + dy * dy <= overchargeSquaredRadius) {
                                if (overchargersmap[x1][y1] == null) {
                                    overchargersmap[x1][y1] = new ArrayList<>();
                                }
                                overchargersmap[x1][y1].add(h);
                                overchargemap[x1][y1]++;
                            } else if(h.canMove() && dx * dx + dy * dy <= overchargePlusMoveSquaredRadius){

                                for(Loc l : h.adjacentPassableTiles){
                                    if(!l.containsRobot() && l.isWithinSquaredDistance(Map.locations[x1][y1],overchargeSquaredRadius)){
                                        if (movingoverchargersmap[x1][y1] == null) {
                                            movingoverchargersmap[x1][y1] = new ArrayList<>();
                                        }
                                        movingoverchargersmap[x1][y1].add(h);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if(overchargers.size() == 0){
            return;
        }


        int attackdist = Type.MAGE.range;
        int mageMageReach = 5;

        //Calcs: Amount of enemies * 121
        for(Robot r  : Map.theirRobots){
            if(!r.inGarrison) {
                double damagedesire = 10;


                for (Loc l : r.adjacentPassableTiles) {
                    if (l.containsRobot()) {
                        if (l.getRobot().isMine) {
                            damagedesire -= 30;
                        } else {
                            damagedesire += 6;
                        }
                    }
                }

                int x = r.x;
                int y = r.y;
                int maxx = Math.min(x + mageMageReach, Map.widthMinusOne);
                int maxy = Math.min(y + mageMageReach, Map.heightMinusOne);

                for (int x1 = Math.max(x - mageMageReach, 0); x1 <= maxx; x1++) {
                    int dx = x1 - x;
                    for (int y1 = Math.max(y - mageMageReach, 0); y1 <= maxy; y1++) {
                        if (!Map.blocked[x1][y1]) {
                            int dy = y1 - y;
                            if (dx * dx + dy * dy <= attackdist) {
                                if (hittablesmap[x1][y1] == null) {
                                    hittablesmap[x1][y1] = new ArrayList<>();
                                }
                                hittablesmap[x1][y1].add(r);

                                damagedesiremap[x1][y1] += damagedesire;
                            }
                        }
                    }
                }

                r.initialDmgScore = damagedesire;
            }
        }

        //Calcs: Approx: Amount of healers * 91
        for(int x = 0; x < Map.width; x++){
            for(int y = 0; y < Map.height; y++){
                if(overchargemap[x][y] > 0){
                    double damage = damagedesiremap[x][y];
                    if(damage > 0){
                        for(Healer h : overchargersmap[x][y]){
                            h.overchargeDamagePotential += damagedesiremap[x][y];
                        }
                    }
                }
            }
        }


        for(Healer h : overchargers){
            ObjectSorting.AddToSort(h,h.overchargeDamagePotential);
        }
        ArrayList<Object> sortedOverchargers = ObjectSorting.SortAscObjects();

        for(Object o  : sortedOverchargers){
            Healer h = (Healer) o;

            int x = h.x;
            int y = h.y;
            int maxx = Math.min(x + overchargeMaxReach, Map.widthMinusOne);
            int maxy = Math.min(y + overchargeMaxReach, Map.heightMinusOne);

            for (int x1 = Math.max(x - overchargeMaxReach, 0); x1 <= maxx; x1++) {
                int dx = x1 - x;
                for (int y1 = Math.max(y - overchargeMaxReach, 0); y1 <= maxy; y1++) {
                    if (!Map.blocked[x1][y1] && Map.robots[x1][y1] == null) {
                        int dy = y1 - y;
                        if (dx * dx + dy * dy <= overchargeSquaredRadius) {
                            if (sortedoverchargersmap[x1][y1] == null) {
                                sortedoverchargersmap[x1][y1] = new ArrayList<>();
                            }
                            sortedoverchargersmap[x1][y1].add(h);
                        }
                    }
                }
            }
        }


        boolean areweblinking = Techs.canBlink;

        ArrayList<Action> bestplan = null;
        Mage bestmage = null;
        double bestplanscore = 0;
        int fromiteration = -1;

//        Random rand = new Random(PseudoRandom.next(1000000));
//        rand.setSeed(PseudoRandom.next(1000000));

        int maxiterations = 70;
        if(R.POWERSAVEMODUSLIGHT){
            if(R.POWERSAVEMODUS){
                maxiterations = 3;
            }
            else{
                maxiterations = 20;
            }
        }

        for(Mage m : Map.myMages){
            if(!m.inGarrison && !m.isDead) {
                if(m.canMove() || (areweblinking && m.canUseAbility()) ||  sortedoverchargersmap[m.x][m.y] != null) {
                    int dist = 0;
                    for (Robot r : Map.theirRobots) {
                        dist += Map.findDistanceTo(m.loc, r.loc);
                    }
                    ObjectSorting.AddToSort(m, dist);
                }
            }
        }

        ArrayList<Object> sortedMages = ObjectSorting.SortAscObjects();

        for(Object obj : sortedMages) {
            Mage m = (Mage) obj;


            for (int iteration = 0; iteration < maxiterations; iteration++) {

                if(iteration % 5 == 1 && System.nanoTime() - begintime > 200000000) break; //don't want this taking up all our budget. half is plenty

                totalIterations++;

                for (Healer h : overchargers) {
                    h.overchargeSpend = false;
                }
                for (Robot r : Map.theirRobots) {
                    r.expectedRemainingHealthMagePlan = r.health;
                }


                //first, find a path to a spot we can start damaging from

                MainLoc currentspot = m.loc;


                boolean canmove = m.canMove();
                boolean canattack = m.canAttack();
                boolean canblink = m.canUseAbility();

                HashSet<Loc> nowblockedspots = new HashSet<>();


                int donothingcounter = 0;

                ArrayList<Action> plan = new ArrayList<>();
                searchloop:
                while (true) {
                    //will end once we run out of overchargers or locations
                    Loc closestEnemy = null; //the closest enemy is used to be able to score spots at which we can't damage anyone
                    int closestEnemyDist = 30;
                    for (Robot r : Map.theirRobots) {
                        if (r.expectedRemainingHealthMagePlan > 0) {
                            int dist = Map.findDistanceTo(currentspot, r.loc);
                            if (dist < closestEnemyDist) {
                                closestEnemyDist = dist;
                                closestEnemy = r.loc;
                            }
                        }
                    }

                    Healer bestHealer = null;
                    if (closestEnemy != null) {

                        MainLoc startloc = currentspot;
                        MainLoc blinkloc = currentspot;
                        MainLoc finalloc = currentspot;


                        boolean weblinked = false;
                        boolean wemoved = false;

                        boolean applynocontinuationpenalty = iteration <= 0 || PseudoRandom.nextDouble() < 0.9;


                        if (areweblinking && canblink) {
                            double bestScore = -9999;
                            MainLoc bestloc = null;
                            for (MainLoc l : (new Circle(currentspot, 8)).allContainingLocationsOnTheMap()) {
                                if (l.isPassable && !l.containsRobot() && Map.inSight[l.x][l.y]  && !nowblockedspots.contains(l)) {
                                    Healer usedHealer = null;
                                    if (sortedoverchargersmap[l.x][l.y] != null) {
                                        for (Healer h : sortedoverchargersmap[l.x][l.y]) {
                                            if (!h.overchargeSpend) {
                                                usedHealer = h;
                                                break;
                                            }
                                        }
                                    }

                                    int distance = Map.findDistanceTo(l, closestEnemy);
                                    double score = -4 * distance;

                                    if (usedHealer == null) {
                                        if(applynocontinuationpenalty) {
                                            score -= 30;
                                        }
                                        if (movingoverchargersmap[l.x][l.y] != null) {
                                            movingloop:
                                            for (Healer h : movingoverchargersmap[l.x][l.y]) {
                                                if (!h.overchargeSpend) {
                                                    for (MainLoc l2 : h.adjacentPassableTiles) {
                                                        if (l2.isWithinSquaredDistance(l, overchargeSquaredRadius) && !l2.containsRobot && !nowblockedspots.contains(l2)) {
                                                            usedHealer = h;
                                                            break movingloop;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    if (usedHealer == null && applynocontinuationpenalty) {
                                        score -= 30;
                                    }


                                    if (distance <= 1) {
                                        //dont kill yourself nub
                                        score -= 50;
                                    }

                                    double damagestuff = 0;
                                    if (hittablesmap[l.x][l.y] != null) {
                                        for (Robot r : hittablesmap[l.x][l.y]) {
                                            if (r.expectedRemainingHealthMagePlan > 0) {
                                                damagestuff += r.initialDmgScore;
                                            }
                                        }
                                    }
                                    score += Math.min(damagestuff, 65);


                                    if(applynocontinuationpenalty) {
                                        int canContinueCount = 0;
                                        for (MainLoc l2 : l.adjacentPassable) {
                                            if (sortedoverchargersmap[l2.x][l2.y] != null) {
                                                for (Healer h : sortedoverchargersmap[l2.x][l2.y]) {
                                                    if (!h.overchargeSpend && !h.equals(usedHealer)) {
                                                        canContinueCount++;
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                        if (canContinueCount == 0) {
                                            score -= 8;
                                        } else {
                                            score += Math.min(canContinueCount, 3) * (2 + (3 * PseudoRandom.nextDouble()));
                                        }
                                    }


                                    //A bit of randomness so we can try different paths
                                    if (iteration > 0) {
                                        score += PseudoRandom.nextDouble() * 13.0;
                                    }


                                    if (score > bestScore) {
                                        bestScore = score;
                                        bestloc = l;
                                        bestHealer = usedHealer;
                                    }

                                }
                            }

                            if (bestloc != null && !bestloc.equals(currentspot)) {
                                weblinked = true;
                                blinkloc = bestloc;
                                currentspot = bestloc;
                            }
                        }

                        if (canmove) {
                            double bestScore = -9999;
                            MainLoc bestloc = null;
                            for (MainLoc l : currentspot.adjacentPassableIncludingThis) {
                                if (!l.containsRobot && Map.inSight[l.x][l.y] && !nowblockedspots.contains(l)) {
                                    Healer usedHealer = null;
                                    if (sortedoverchargersmap[l.x][l.y] != null) {
                                        for (Healer h : sortedoverchargersmap[l.x][l.y]) {
                                            if (!h.overchargeSpend) {
                                                usedHealer = h;
                                                break;
                                            }
                                        }
                                    }
                                    int distance = Map.findDistanceTo(l, closestEnemy);
                                    double score = -4 * distance;


                                    if (usedHealer == null) {
                                        if(applynocontinuationpenalty) {
                                            score -= 30;
                                        }
                                        if (movingoverchargersmap[l.x][l.y] != null) {
                                            movingloop:
                                            for (Healer h : movingoverchargersmap[l.x][l.y]) {
                                                if (!h.overchargeSpend) {
                                                    for (MainLoc l2 : h.adjacentPassableTiles) {
                                                        if (l2.isWithinSquaredDistance(l, overchargeSquaredRadius) && !l2.containsRobot && !nowblockedspots.contains(l2)) {
                                                            usedHealer = h;
                                                            break movingloop;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }


                                    if (usedHealer == null && applynocontinuationpenalty) {
                                        score -= 30;
                                    }

                                    if (distance <= 1) {
                                        //dont kill yourself nub
                                        score -= 50;
                                    }


                                    if(applynocontinuationpenalty) {
                                        int canContinueCount = 0;
                                        for (MainLoc l2 : l.adjacentPassable) {
                                            if (sortedoverchargersmap[l2.x][l2.y] != null) {
                                                for (Healer h : sortedoverchargersmap[l2.x][l2.y]) {
                                                    if (!h.overchargeSpend && !h.equals(usedHealer)) {
                                                        canContinueCount++;
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                        if (canContinueCount == 0) {
                                            score -= 8;
                                        } else {
                                            score += Math.min(canContinueCount, 3) * (2 + (3 * PseudoRandom.nextDouble()));
                                        }
                                    }


                                    double damagestuff = 0;
                                    if (hittablesmap[l.x][l.y] != null) {
                                        for (Robot r : hittablesmap[l.x][l.y]) {
                                            if (r.expectedRemainingHealthMagePlan > 0) {
                                                damagestuff += r.initialDmgScore;
                                            }
                                        }
                                    }
                                    if(iteration == 0) {
                                        score += Math.min(damagestuff, 65);
                                    }else{
                                        score += PseudoRandom.nextDouble() *  2 + ( iteration * 0.5);
                                        score +=  Math.min(damagestuff, 65) * 2 * PseudoRandom.nextDouble();
                                    }


                                    if (score > bestScore) {
                                        bestScore = score;
                                        bestloc = l;
                                        bestHealer = usedHealer;
                                    }
                                }
                            }


                            if (bestloc != null && !currentspot.equals(bestloc)) {
                                finalloc = bestloc;
                                wemoved = true;
                                currentspot = bestloc;
                            }
                        }
                        MainLoc finalspot = currentspot;

                        boolean shootStart = false;
                        boolean shootAfterBlink = false;
                        boolean shootAfterMove = false;

                        RobotValuation rv1 = null;
                        RobotValuation rv2 = null;
                        RobotValuation rv3 = null;

                        if (canattack) {

                            rv1 = getBestRobotToHit(startloc);
                            if (weblinked) {
                                rv2 = getBestRobotToHit(blinkloc);
                            }
                            if (wemoved) {
                                rv3 = getBestRobotToHit(finalspot);
                            }

                            if (weblinked && rv2.score > rv1.score) {
                                if (wemoved && rv3.score > rv2.score) {
                                    if (rv3.score > 0) {
                                        shootAfterMove = true;
                                    }
                                } else {
                                    if (rv2.score > 0) {
                                        shootAfterBlink = true;
                                    }
                                }
                            } else {
                                if (wemoved && rv3.score > rv1.score) {
                                    if (rv3.score > 0) {
                                        shootAfterMove = true;
                                    }
                                } else {
                                    if (rv1.score > 0) {
                                        shootStart = true;
                                    }
                                }
                            }
                        }

                        if (shootStart) {
                            double damage = doPredictedDamage(rv1.r);
                            plan.add(new AttackAction(rv1.r, damage));
                            canattack = false;

                        }
                        if (weblinked) {
                            plan.add(new BlinkAction(blinkloc));
                            canblink = false;
                        }
                        if (shootAfterBlink) {
                            double damage = doPredictedDamage(rv2.r);
                            plan.add(new AttackAction(rv2.r, damage));
                            canattack = false;
                        }
                        if (wemoved) {
                            plan.add(new MoveAction(finalloc));
                            canmove = false;
                        }
                        if (shootAfterMove) {
                            double damage = doPredictedDamage(rv3.r);
                            plan.add(new AttackAction(rv3.r, damage));
                            canattack = false;
                        }
                    }
//                        else{
//                            Debug.log("Shutting down cause no nearby units");
//                        }

                    if (bestHealer == null) {
                        //End of the line
                        break searchloop;
                    } else {


                        if((!canmove) || (!canattack) || (!canblink)) {

                            if (!currentspot.isWithinSquaredDistance(bestHealer, overchargeSquaredRadius)) {
                                boolean foundspot = false;
                                for (MainLoc l : bestHealer.adjacentPassableTiles) {
                                    if (!l.containsRobot && l.isWithinSquaredDistance(currentspot, overchargeSquaredRadius) && !nowblockedspots.contains(l)) {
                                        plan.add(new HealerMoveAction(bestHealer, l));
                                        foundspot = true;
                                        nowblockedspots.add(l);
//                                        Debug.log("HEALER MOVEMENT IN PLAN "  +  bestHealer.loc + " -> " + l );
                                        break;
                                    }
                                }

                                if (!foundspot) {
                                    Debug.log("Couldnt find move spot?!??!");
                                    break searchloop;
                                }
                            }


                            plan.add(new OverChargeAction(bestHealer));
                            bestHealer.overchargeSpend = true;
                            canattack = true;
                            canblink = true;
                            canmove = true;
                        }else{
                            if(donothingcounter++ > 30){
                                break;
                            }
                        }
                    }
                }


                //TODO: remove unneccessary sections,  like a move, overcharge, move, overcharge, move. where a  move, overcharge, move would've gotten us to the same spot




                //now prune the list, to find the best length of the list. Don't want to keep moving/jumping/overcharging after the attacks
                double runningplanscore = 0;
                double max = -100;
                int best = 0;

                for (int i = 0; i < plan.size(); i++) {
                    runningplanscore += plan.get(i).score;

                    if (runningplanscore > max) {
                        max = runningplanscore;
                        best = i + 1;
                    }
                }

//                Debug.log("Prune: " + plan.size() + " score: " + runningplanscore + " max: " + max + " to size: " + best);
//                for(Action a : plan){
//                    Debug.log(a.toString());
//                }



//                if(R.turn == 235){
//                    Debug.lineBreak();
//                    Debug.log(plan);
//                }


                if (max > bestplanscore) {
                    //Is our plan the best?
//                        Debug.lineBreak();
//                        Debug.log("old plan:");
//                        Debug.log(plan);

                    bestplan = new ArrayList<>();
                    for (int i = 0; i < best; i++) {
                        bestplan.add(plan.get(i));
                    }

//                        Debug.log("new plan:");
//                        Debug.log(bestplan);
//                        Debug.lineBreak();
                    bestplanscore = max;
                    bestmage = m;
                    fromiteration = iteration;
                }
            }

        }


        if(bestplan != null && bestplanscore > 300){
            int mageid = bestmage.id;
            Loc pastloc = bestmage.loc;
            Loc nextloc;
            int attacks = 0;
            for(Action a : bestplan){
//                Debug.log(a.toString());
                try{
                     if(a instanceof MoveAction){
                         nextloc = ((MoveAction)a).l;

                         Player.gc.moveRobot(mageid, nextloc.getDirectionFrom(pastloc));
                         pastloc = nextloc;
                         bestmage.moveHeat += bestmage.moveCd;

                     }
                     else if(a instanceof BlinkAction){
                         nextloc = ((BlinkAction)a).l;

                         Player.gc.blink(mageid, nextloc.toMapLocation());
                         pastloc = nextloc;
                         bestmage.abilityHeat += bestmage.abilityCd;

                     } else if(a instanceof AttackAction){
                         bestmage.attack(((AttackAction)a).target,false);
                         attacks++;
                     } else if(a instanceof OverChargeAction){
                         Healer h = ((OverChargeAction)a).healer;
                         Player.gc.overcharge(h.id,mageid);
                         h.abilityHeat += h.abilityCd;
                         bestmage.moveHeat = 0;
                         bestmage.attackHeat = 0;
                         bestmage.abilityHeat = 0;
                         totalOverchargesUsed++;
                     }else if(a instanceof HealerMoveAction){
                         Healer h = ((HealerMoveAction)a).healer;
                         Loc l = ((HealerMoveAction)a).l;
                         Player.gc.moveRobot(h.id,l.getDirectionFrom(h.loc));
                         h.updateLocation(l,true,true);
                         h.moveHeat += h.moveCd;
                         h.M.reset();
//                         Debug.log("ASDMDSAFSAJFDAJFDAJFDJFDJJFDJFDSJFDSJSFDJFDSJFDSJJ");

                     }
                }catch (Exception ex){
                    Debug.log(ex);
                }


            }
            Debug.log("FOUND A PLAN: " + bestplanscore + " iter: " +  fromiteration  + " from: " + bestmage.loc + "->" + pastloc + " attacks: " + attacks);

            totalAttacks += attacks;
            totalPathsfound++;
            totalScore += bestplanscore;





            bestmage.updateLocation(pastloc,true,true);
            bestmage.M.reset();
        }
//        else{
//            Debug.log("No good plan found " + bestplanscore);
//        }

        totalTime += ((System.nanoTime()) - begintime) / 1000000.0;


         if(R.turn <= 231 && Map.myTotalUnitCount < 300){
             MageOverchargePathing.Think(recursion + 1);
         }



//        Debug.log("Time: "  +  ((double)((System.nanoTime()) - begintime)) / 1000000.0 );


    }


    public static RobotValuation getBestRobotToHit(Loc l){
        double bestscore = 0;
        Robot bestRobot = null;
        if(hittablesmap[l.x][l.y] != null) {
            for (Robot r : hittablesmap[l.x][l.y]) {

                if (r.expectedRemainingHealthMagePlan > 0) {
                    double score = 10;

                    if (r.expectedRemainingHealthMagePlan < Mage.myMageDamage) {
                        score += 8;
                    }

                    for(Loc l2 : r.loc.adjacentPassable){
                        if(Map.inSight[l2.x][l2.y]) {
                            Robot r2 = l2.getRobot();
                            if (r2 != null) {
                                if (r2.expectedRemainingHealthMagePlan > 0) {
                                    if (r2.isMine) {
                                        score -= 30;
                                    } else {
                                        score += 5;
                                        if (r2.expectedRemainingHealthMagePlan < Mage.myMageDamage) {
                                            score += 3;
                                        }
                                    }
                                }
                            }
                        }else{
                            score += 1; //frequently contains enemies
                        }
                    }


                    if (score > bestscore) {
                        bestscore = score;
                        bestRobot = r;
                    }
                }
            }
        }

        RobotValuation rv = new RobotValuation();
        if(bestRobot != null) {
            rv.r = bestRobot;
            rv.score = bestscore;
        }else{
            rv.score = -10000;
        }

        return rv;
    }

    public static double doPredictedDamage(Robot r){

        int damage = 0;
        r.expectedRemainingHealthMagePlan -= Mage.myMageDamage;
        if (r.expectedRemainingHealthMagePlan <= 0) {
            damage += 500; //highly valuing kills
        }

        if(r.amIKnight && !r.isMine){
            r.expectedRemainingHealthMagePlan += Knight.theirKnightDefense;
        }

        for(Loc l2 : r.loc.adjacentPassable) {
            if (Map.inSight[l2.x][l2.y]) {
                Robot r2 = l2.getRobot();
                if(r2 != null) {
                    r2.expectedRemainingHealthMagePlan -= Mage.myMageDamage;

                    if(r2.amIKnight && !r2.isMine){
                        r2.expectedRemainingHealthMagePlan += Knight.theirKnightDefense;
                    }

                    if (r2.isMine) {
                        damage -= Mage.myMageDamage * 3;
                    } else {
                        if(r2.amIWorker){
                            damage += Mage.myMageDamage * 0.3;
                            if (r2.expectedRemainingHealthMagePlan <= 0) {
                                damage += 50;
                            }
                        }
                        else if(r2.amIStructure){
                            damage += Mage.myMageDamage * 1.3;
                            if (r2.expectedRemainingHealthMagePlan <= 0) {
                                damage += 800 + ((Structure)r2).garrisonedWithin.size() * 400;
                            }
                        }
                        else {
                            damage += Mage.myMageDamage;

                            if (r2.expectedRemainingHealthMagePlan <= 0) {
                                damage += 500;
                            }
                        }
                    }
                }
            }else{
                //Bit of extra damage to make them prefer splashing on unseen tiles
                damage += 5;
            }
        }
        return damage * 0.3 + Mage.myMageDamage;
    }


    public static class Action{
        public double score = 0;
    }

    public static class MoveAction extends Action{
        public Loc l;
        public MoveAction(Loc l){this.l = l; this.score = -0.1;}

        @Override
        public String toString(){
            return "Move: " + l;
        }
    }
    public static class BlinkAction extends Action{
        public Loc l;
        public BlinkAction(Loc l){this.l = l; this.score = -0.2;}

        @Override
        public String toString(){
            return "Blink: " + l;
        }
    }
    public static class AttackAction extends Action{
        public Robot target;
        public AttackAction(Robot r, double score){this.target = r; this.score = score;}

        @Override
        public String toString(){
            return "Attack: " + target.loc;
        }
    }


    public static class HealerMoveAction extends Action{
        public Healer healer = null;
        public Loc l = null;
        public HealerMoveAction(Healer healer, Loc l){this.healer = healer; this.l = l; this.score = -40;}

        @Override
        public String toString(){
            return "Healer move: " + healer.loc + " ->" + l;
        }
    }

    public static class OverChargeAction extends Action{
        public Healer healer = null;
        public OverChargeAction(Healer healer){this.healer = healer; this.score = -15;}

        @Override
        public String toString(){
            return "Overcharge from: " + healer.loc;
        }
    }

    public static class RobotValuation{
        public Robot r;
        public double score;


    }


}
