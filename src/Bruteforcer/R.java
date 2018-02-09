package Bruteforcer;

import bc.Planet;
import bc.Team;

import java.util.ArrayList;
import java.util.Random;

public class R {

    public static int turn;
    public static Planet myPlanet;
    public static Planet oppositePlanet;
    public static Team myTeam;
    public static boolean player1;

    public static int karbonite;
    public static boolean weGainedKarboniteThisTurn;

    public static double beBallsyRating = 0;

    public static boolean amIEarth;
    public static boolean amIMars;

    private static int turnsExperienced = 0;

    public static boolean earthIsFullyConnected;
    public static boolean earthIsFullyDisconnected;

    public static int nextSnipeVolley = -1;

    public static boolean SPREAD = false;

    public static boolean POWERSAVEMODUSLIGHT = false;
    public static boolean POWERSAVEMODUS = false;
    public static boolean POWERSAVEMODUSHYPER = false;


    public static boolean KNIGHTGAME = false;


    public static double shortestRush = 9999999999.0;
    public static double longestRush = -999999999.0;

    public static boolean heavilystuck = false;


    public static boolean hyperVigilanceHasBeenTriggered = false;

    public static ArrayList<OverchargeRequest> overchargeReqs = new ArrayList<>();
    public static ArrayList<Healer> unusedHealers = new ArrayList<>();



    public static boolean CrawlMode = false;
    public static boolean DefensiveStance = false;



    public static boolean SUICIDERUSH = false;
    public static boolean SUICIDERUSHWORKERS = false;


    public static void initGame(){
        Debug.init();
        Player.ParseMapInitial();




        player1 = myTeam.name().equals("Red");


        earthIsFullyConnected = true;
        earthIsFullyDisconnected = true;


        if(Debug.release){
            //Rarely allows different outcomes on the same map (pseudorandom isn't actually used much)
            Random r = new Random();
            PseudoRandom.seed = 1000 + r.nextInt(50000);
        }



        if(amIEarth) {
//            Debug.log("hi " + Map.theirStartingSpots.size() + " " + Map.myStartingSpots.size());
            for (Loc l : Map.theirStartingSpots) {
                for (Loc l2 : Map.myStartingSpots) {
                    int dist = Map.findDistanceTo(l2, l);
                    if (dist > 1000) {
                        earthIsFullyConnected = false;
                    }else{
                        earthIsFullyDisconnected = false;
                    }
                    if(dist < shortestRush){
                        shortestRush = dist;
                    }
                    if(dist > longestRush){
                        longestRush = dist;
                    }
                }
            }
        }




//        if(!player1){
//            GrandStrategy.strategy = GrandStrategy.STDRANGER;
        if(earthIsFullyDisconnected && R.amIEarth){
            Debug.log("Disconnected map");
            GrandStrategy.strategy = GrandStrategy.EARLYROCKETS;

        }else {

            boolean shouldknightrush = false;
            if(earthIsFullyConnected && longestRush < 35 && (Map.width + Map.height) < 70 ){
                double score = 150 -  (Map.width + Map.height) * 1.1;
//                score += Map.totalSuspectedReachableKarbonite * 0.05;
                score -= longestRush;
                score -= shortestRush * 2;

                score -= Map.theirStartingSpots.size() * 10;

                if(Map.myRobots.size() > 0 ){
                    score +=  (MapSections.sectionOpenRatio[Map.myRobots.get(0).loc.sectionId]  - 0.85) * 60; //open is better for knight rushes
                }


                //Todo: estimate map-section openness

                Debug.log("Knight rush score: " + score +  " longest: " + longestRush);

                if(score > 0){
                    shouldknightrush = true;
                }
            }else{
                Debug.log("Not even looking at knight rush " + longestRush);
            }

//            if(!R.player1){
//                shouldknightrush = true;
//            }

            if(shouldknightrush){
                GrandStrategy.strategy = GrandStrategy.KNIGHTRUSH;
            }else{
                GrandStrategy.strategy = GrandStrategy.ADAPTIVERANGER;
            }


//            if(R.player1) {
////                GrandStrategy.strategy = GrandStrategy.STDRANGER;
//                GrandStrategy.strategy = GrandStrategy.KNIGHTRUSH;
//            }else{
//                GrandStrategy.strategy = GrandStrategy.KNIGHTRUSH;

//            }
        }
//        }else{
//            GrandStrategy.strategy = GrandStrategy.LATEMAGE;
//        }


        if(GrandStrategy.strategy == GrandStrategy.KNIGHTRUSH || GrandStrategy.strategy == GrandStrategy.RANDOM){
            KNIGHTGAME = true;
        }
    }



    public static void doTurn(long turn, long timeleft) {
        long l = 0;
//        for(int i =0; i <200000; i++){
//            hello = new Hello(hello);
//        }

//        MapLocation m = Map.locations[0][0].toMapLocation();
//        for(int i =0 ; i < 10000000; i++){
//            Player.gc.my();
//            Player.gc.karboniteAt(m);
//            if(i % 1000 == 0) {
//                System.gc();
//            }
//        }

//        Debug.log("Turns: " + turnsExperienced++ + "  " + turn + l);
//        if(true)return;

//        Debug.log("");

//        Debug.log(GrandStrategy.strategy);


        R.turn = (int) turn;

        //Reduces chance of crashbots
        if (R.amIEarth && R.turn > 750) return;

        overchargeReqs.clear();

        if(timeleft < 7000) {

            POWERSAVEMODUSLIGHT = true;

            if (timeleft < 4000) {
                if (!POWERSAVEMODUS) {
                    System.out.println("Switching to power save modus");
                }
                POWERSAVEMODUS = true;
                if (timeleft < 1000) {
                    if (!POWERSAVEMODUSHYPER) {
                        System.out.println("Switching to strong power save modus");
                    }
                    POWERSAVEMODUSHYPER = true;
                } else {
                    POWERSAVEMODUSHYPER = false;
                }
            }
            else{
                POWERSAVEMODUS = false;
                POWERSAVEMODUSHYPER = false;
            }
        }
        else {
            POWERSAVEMODUS = false;
            POWERSAVEMODUSHYPER = false;
            POWERSAVEMODUSLIGHT = false;
        }

//        if(false) {
//            if (turn > 200 && turn < 226) {
//                if (R.player1 && Techs.techHealer >= 2 && Map.myUnitCounts[Type.HEALER.typeId] > 15 && Map.myUnitCounts[Type.MAGE.typeId] >= 2 && Map.myTotalUnitCount >= 40) {
//                    if (turn > 210) {
//                        SUICIDERUSH = true;
//                        SUICIDERUSHWORKERS = true;
//                    } else {
//                        SUICIDERUSHWORKERS = true;
//                    }
//                }
//            } else {
//                SUICIDERUSH = false;
//                SUICIDERUSHWORKERS = false;
//            }
//        }




//        SPREAD = R.player1 && R.turn > 325 && R.turn < 620;
        SPREAD = !POWERSAVEMODUSHYPER && (EnemyStrategy.Strategy == EnemyStrategy.MAGEHEAVY || Techs.enemyTechHealers >= 3 || Techs.enemyTechMages >= 4 || Techs.enemyTechRangers >= 3); //(R.player1 || Map.theirUnitCounts[Type.MAGE.typeId] > 5) && R.turn > 400;

        weGainedKarboniteThisTurn = false;

        Debug.beginClock(0);
        try {
            Player.ParseTurn();
        } catch (Exception ex) {
            Debug.log(ex);
        }
        Debug.endClock(0);

        try{
            pruneEnemyFactoryList();
            if(R.turn % 20 == 1){
                MapSections.SetCanReach();
            }
        }catch (Exception ex){
            Debug.log(ex);
        }



        Debug.beginClock(1);
        try {
            MapMeta.UpdateDangerMap();
            analyzeFactoriesUnderThreat();

        } catch (Exception ex) {
            Debug.log(ex);
        }
        Debug.endClock(1);


        try {
            WorkerKarboSearch.think();
        }catch (Exception ex){
            Debug.log(ex);
        }

        Debug.beginClock(2);
        try {
            Techs.DoThinking();
        } catch (Exception ex) {
            Debug.log(ex);
        }
        try {
            Construction.wantToMakeUnits = false;
            Construction.DoThinking(0, false, false, false,false);
        } catch (Exception ex) {
            Debug.log(ex);
        }
        Debug.endClock(2);


        try{
            if(Techs.enemyCanOverCharge) {
                R.IdentifySacrificialLambs();
            }
        }catch (Exception ex){
            Debug.log(ex);
        }

        try {
            determineHeavilyStuck();
        }catch (Exception ex){
            Debug.log(ex);
        }


        try {
            MageOverchargePathing.Think(0);
        } catch (Exception ex) {
            Debug.log(ex);
        }



        if(R.turn % 10 == 0) {
            try {
                AnalyzeEnemyStrategy();
            } catch (Exception ex) {
                Debug.log(ex);
            }
        }
        if(amIMars){
            try {
                MarsStuff.avoidRocketLandings();
            }catch (Exception ex) {
                Debug.log(ex);
            }
        }

        if(Map.theirUnitCounts[Type.RANGER.typeId] > 10){
            CrawlMode = true;
        }else{
            CrawlMode = false;
        }
        if(GrandStrategy.strategy == GrandStrategy.ADAPTIVERANGER && R.turn < 140){
            DefensiveStance = true;
        }else{
            DefensiveStance = false;
        }


//        if(R.turn % 20 == 0){
//            System.out.println("T" + R.turn + " karb "  + Map.totalKarboHarvested + " / " + Map.initialTotalKarbo);
//        }


//        print dangermap (or damage)
//        if(R.turn == 400 || R.turn == 420 || R.turn == 440){
//
//             Debug.log("Suspicious");
//            for (int y = MapMeta.controlRows - 1; y >= 0; y--) {
//                String s = "";
//                for (int x = 0; x < MapMeta.controlColumns; x++) {
//                    s += (int)(((MapMeta.controlSuspectedEnemyPresence[x][y] / 80.0) + 1) % 10);
//                }
//                Debug.log(s);
//            }
//            Debug.lineBreak();
//            Debug.lineBreak();


//            Debug.log("Danger");
//            for(int y =Map.height - 1; y >= 0; y--) {
//                String s = "";
//                for (int x = 0; x < Map.width; x++) {
//                    s += (int)((MapMeta.dangerArray[x][y] / 200.0) % 10);
//                }
//                Debug.log(s);
//            }
//            Debug.lineBreak();
//            Debug.lineBreak();
//            Debug.log("Healer");
//            for(int y =Map.height - 1; y >= 0; y--) {
//                String s = "";
//                for (int x = 0; x < Map.width; x++) {
//                    s += (int)((MapMeta.healerSupportArray[x][y] / 2.0) % 10);
//                }
//                Debug.log(s);
//            }
//            Debug.lineBreak();
//            Debug.lineBreak();
//            Debug.log("AttackRange");
//            for(int y =Map.height - 1; y >= 0; y--) {
//                String s = "";
//                for (int x = 0; x < Map.width; x++) {
//                    s += (int)((MapMeta.withinAttackRangeArray[x][y] / 10.0) % 10);
//                }
//                Debug.log(s);
//            }
//        }
//        if(R.turn == 1){
//            for(int y =Map.height - 1; y >= 0; y--) {
//                String s = "";
//                for (int x = 0; x < Map.width; x++) {
//                   if(Map.blocked[x][y]){
//                       s += "X";
//                   }else{
//                       s += "-";
//                   }
//                }
//                Debug.log(s);
//            }
//        }
//        if (R.turn == 10 || R.turn == 140) {
//            Debug.log("karbo:");
//            for (int y = MapMeta.controlRows - 1; y >= 0; y--) {
//                String s = "";
//                for (int x = 0; x < MapMeta.controlColumns; x++) {
//                    s += ((int)(MapMeta.controlKarbonite[x][y] / 20.0)) % 10;
//
//                }
//                Debug.log(s);
//            }
//        }

//
//        if(R.turn == 125){
//            Debug.log("control activity:");
//            for (int y = MapMeta.controlRows - 1; y >= 0; y--) {
//
//                String s = "";
//                for (int x = 0; x < MapMeta.controlColumns; x++) {
////                    s += ((int)(MapMeta.controlRollingActivity[x][y] / 8.0)) % 10;
//                    s += ((int)(MapMeta.controlMap[x][y] / 20.0) + 10) % 10;
//
//                }
//                Debug.log(s);
//            }
//
//            for (int y = MapMeta.controlRows - 1; y >= 0; y--) {
//                for (int x = 0; x < MapMeta.controlColumns; x++) {
//                    Debug.log("[" + x + "," + y + "] " +  MapMeta.controlRollingActivity[x][y]);
//
//                }
//            }
//        }

        beBallsyRating = Math.max(Map.myRobots.size() / (5.0 * (Map.width + Map.height)) , 1);
        Debug.beginClock(3);

        for (Robot r : Map.myRobots) {
            try {
                r.basePreTurn();
            } catch (Exception ex) {
                Debug.log(ex);
            }
        }
        for (Robot r : Map.myRobots) {
            try {
                r.preturn();
            } catch (Exception ex) {
                Debug.log(ex);
            }
        }
        for (Robot r : Map.myRobots) {
            try {
                r.baseThink();
            } catch (Exception ex) {
                Debug.log(ex);
            }
        }

//        for (Robot r : Map.myRobots) {
//            try {
//                if (!r.inGarrison) {
//                    r.considerLoadingIn();
//                }
//            } catch (Exception ex) {
//                Debug.log(ex);
//            }
//        }
        Dpser.doBloodlustCalcs();
        Debug.endClock(3);


        Debug.beginClock(4);
        ArrayList<Object> orderedBots = getOrderedBots();

        int counter = 0;
        int max = orderedBots.size();
        if(POWERSAVEMODUSHYPER){
            max = 30;
        } else if(POWERSAVEMODUS){
            max = 70;
        }

        //Unit turn updates
        for (Object o : orderedBots) {
            try {
                Robot r = (Robot)o;
                if(!r.justUnloaded && !r.inGarrison && !r.hasDoneTurn) {
                    r.think();
                    r.hasDoneTurn = true;
                    if (counter++ > max) {
                        break;
                    }
                }
//                else{
//                    Debug.log("s: " + o.score);
//                }
            } catch (Exception ex) {
                Debug.log(ex);
            }
        }
        Debug.endClock(4);

        for(Robot r: Map.postponedBots){
            try {
                r.think();
            }catch (Exception ex){
                Debug.log(ex);
            }
        }





        for (Robot r : Map.latecomers) {
            try {
                r.basePreTurn();
                r.preturn();
                r.baseThink();
                r.think();
            } catch (Exception ex) {
                Debug.log(ex);
            }
        }



        for (Robot r : Map.myRobots) {
            if (r.amIStructure && !r.amIBlueprint) {
                ((Structure) r).eject();
            }
        }

        for(Healer h : unusedHealers){
            try{
                h.tryHealing(h.canMove());
            }catch (Exception ex){
                Debug.log(ex);
            }
        }

        if(Techs.canOverload){
            Healer.fulfilloverchargeRequests();
        }


        Debug.beginClock(5);
        if (Techs.canSnipe && R.turn >= R.nextSnipeVolley) {
            R.nextSnipeVolley += 5;
            Ranger.SnipeVolley();
        }

        for (Rocket r : Map.launchingRockets) {
            r.launch();
        }


        if (!R.POWERSAVEMODUS && weGainedKarboniteThisTurn) {

            Map.latecomers.clear();

            Construction.DoThinking(0, false, false, false,false);

            for (Robot r : Map.latecomers) {
                try {
                    r.basePreTurn();
                    r.preturn();
                    r.baseThink();
                    r.think();
                } catch (Exception ex) {
                    Debug.log(ex);
                }
            }
        }
        Debug.endClock(5);


        //Send moves?

//        if(turn % 40 == 0){
//            Debug.printclocks();
//        }
//

        EndOfturn();

    }







    public static void determineHeavilyStuck(){

        int tracker = 0;
        for(Robot r: Map.myRobots){
            if(!r.amIStructure){
                tracker +=   (R.turn - r.lastMoved) - 4;
            }
        }
        heavilystuck = tracker > 0;
//        Debug.log("Stuck tracker: " + tracker);

    }




    public static ArrayList<Object> getOrderedBots(){
        int workers = 0;
        for(Robot r : Map.myRobots){
            if(!r.inGarrison && !r.inSpace){
                double goFirstDesire = 0;

                if(r.amIStructure){
                    goFirstDesire -= 500;
                }
                else{
                    if(r.amIWorker){
                        if(workers < 10 && (R.turn > 600 || !MapMeta.anyActivityDetected) && R.amIEarth && Map.myCompletedRockets.size() < 5){
                            goFirstDesire += 10;
                            workers++;
                        }else {
                            goFirstDesire -= 5;
                        }
                    } else if(r.amIMage){
                        goFirstDesire += 2;
                    } else if(r.amIKnight && ((Knight)r).bloodlust){
                        if(r.health < r.maxHealth && MapMeta.healerSupportArray[r.x][r.y] > 0) {
                            goFirstDesire -= 30; //give healers a chance to heal first before you go running off
                        }else{
                            goFirstDesire += 20;
                        }
                    } else if(r.amIHealer){
                        goFirstDesire -= 5;
                    }
                    for(Loc l : r.adjacentPassableTiles){
                        if(!l.containsRobot()){
                            goFirstDesire--;
                        }
                    }

                    goFirstDesire += r.scaryEnemyRobotsNearSize * 7;
                    goFirstDesire += (double)(r.maxHealth - r.health) / 40.0;
                    if(r.rocketCaller != null){
                        goFirstDesire += 40;
                    }
                }



                ObjectSorting.AddToSort(r,goFirstDesire);
            }
        }
        return ObjectSorting.SortDescObjects();
    }



    public static void AnalyzeEnemyStrategy(){
        double[] truth = new double[5];

        double total = 0;

        for(int i =0; i < 5; i++){
            if(i != Type.WORKER.typeId){
                total+= Map.theirRollingUnitCounts[i];
            }
        }


        EnemyStrategy strat = EnemyStrategy.UNKNOWN;


        if(total > 4) {
            for (int i = 0; i < 5; i++) {
                truth[i] = ((double) Map.theirRollingUnitCounts[i]) / total;
            }


            double bestDist = 9999;


            double[] fullRangerSuspected = new double[5];
            fullRangerSuspected[Type.RANGER.typeId] = 0.9;
            fullRangerSuspected[Type.HEALER.typeId] = 0.05;
            fullRangerSuspected[Type.KNIGHT.typeId] = 0.05;
            double rangerDist = Distance(truth, fullRangerSuspected);
            if (rangerDist < bestDist) {
                bestDist = rangerDist;
                strat = EnemyStrategy.FULLRANGER;
            }


            double[] RANGERHEALERSuspected = new double[5];
            RANGERHEALERSuspected[Type.RANGER.typeId] = 0.7;
            RANGERHEALERSuspected[Type.HEALER.typeId] = 0.3;

            double RANGERHEALERDist = Distance(truth, RANGERHEALERSuspected);
            if (RANGERHEALERDist < bestDist) {
                bestDist = RANGERHEALERDist;
                strat = EnemyStrategy.RANGERHEALER;
            }

            double[] FULLKNIGHTSuspected = new double[5];
            FULLKNIGHTSuspected[Type.KNIGHT.typeId] = 0.9;
            FULLKNIGHTSuspected[Type.HEALER.typeId] = 0.05;
            FULLKNIGHTSuspected[Type.RANGER.typeId] = 0.05;

            double FULLKNIGHTDist = Distance(truth, FULLKNIGHTSuspected);
            if (FULLKNIGHTDist < bestDist) {
                bestDist = FULLKNIGHTDist;
                strat = EnemyStrategy.FULLKNIGHT;
            }


            double[] KNIGHTHEALERSuspected = new double[5];
            KNIGHTHEALERSuspected[Type.KNIGHT.typeId] = 0.65;
            KNIGHTHEALERSuspected[Type.HEALER.typeId] = 0.3;
            KNIGHTHEALERSuspected[Type.RANGER.typeId] = 0.05;

            double KNIGHTHEALERDist = Distance(truth, KNIGHTHEALERSuspected);
            if (KNIGHTHEALERDist < bestDist) {
                bestDist = KNIGHTHEALERDist;
                strat = EnemyStrategy.KNIGHTHEALER;
            }


            double[] MAGEHEAVYSuspected = new double[5];
            MAGEHEAVYSuspected[Type.MAGE.typeId] = 0.7;
            MAGEHEAVYSuspected[Type.RANGER.typeId] = 0.1;
            MAGEHEAVYSuspected[Type.HEALER.typeId] = 0.1;
            MAGEHEAVYSuspected[Type.KNIGHT.typeId] = 0.1;

            double MAGEHEAVYDist = Distance(truth, MAGEHEAVYSuspected);
            if (MAGEHEAVYDist < bestDist) {
                bestDist = MAGEHEAVYDist;
                strat = EnemyStrategy.MAGEHEAVY;
            }


            double[] KNIGHTRANGERSuspected = new double[5];
            KNIGHTRANGERSuspected[Type.RANGER.typeId] = 0.45;
            KNIGHTRANGERSuspected[Type.KNIGHT.typeId] = 0.45;
            KNIGHTRANGERSuspected[Type.HEALER.typeId] = 0.1;
            double KNIGHTRANGERDist = Distance(truth, KNIGHTRANGERSuspected);
            if (KNIGHTRANGERDist < bestDist) {
                bestDist = KNIGHTRANGERDist;
                strat = EnemyStrategy.KNIGHTRANGER;
            }


            double[] MAGERANGERSuspected = new double[5];
            MAGERANGERSuspected[Type.RANGER.typeId] = 0.4;
            MAGERANGERSuspected[Type.MAGE.typeId] = 0.4;
            MAGERANGERSuspected[Type.HEALER.typeId] = 0.2;
            double MAGERANGERDist = Distance(truth, MAGERANGERSuspected);
            if (MAGERANGERDist < bestDist) {
                bestDist = MAGERANGERDist;
                strat = EnemyStrategy.MAGERANGER;
            }
        }

        if(strat != EnemyStrategy.Strategy){
            Debug.log("Enemy strat is now: " + strat);
        }

        EnemyStrategy.Strategy = strat;

    }

    public static double Distance(double[] truth, double[] suspected){
        double total = 0;

        for(int i = 0; i < suspected.length; i++){
            total += Math.abs(suspected[i] - truth[i]);
        }
        return total;
    }

    public static void pruneEnemyFactoryList(){

        ArrayList<Factory> removethese = new ArrayList<>();

        for(Factory f : Map.theirFactoriesById.values()){
            if(f.health <= 0 ||   (R.turn - f.turnLastSeen)> 250  ||  Map.inSight[f.x][f.y] && (R.turn - f.turnLastSeen)> 3 ){
                removethese.add(f);
            }
        }
        //Yeah this can be done neater I'm sure, but priorities..
        for(Factory f : removethese){
//            Debug.log("pruning factory " + f);
            Map.theirFactoriesById.remove(f.id);
        }

    }

    public static void analyzeFactoriesUnderThreat(){

        for(Factory f : Map.myFactories){
            double threatscore = 0;

            for(Robot r : Map.theirDps){
                int dist = Map.findDistanceTo(f.loc,r.loc);
                if(dist < 15){
                    threatscore +=  r.damage - dist;
                }
            }
            f.threatScore = threatscore;
        }
    }


    public static void EndOfturn(){

        ArrayList<Robot> removethese = new ArrayList<>();

        for(Robot r : Map.theirGhostImages.values()){
            if(r.health <= 0 ||   (R.turn - r.turnLastSeen)> 5  ||  Map.inSight[r.x][r.y] &&  R.turn != r.turnLastSeen ){
                removethese.add(r);
            }
        }
        //Yeah this can be done neater I'm sure, but priorities..
        for(Robot r : removethese){
//            Debug.log("pruning factory " + f);
            Map.theirGhostImages.remove(r.id);
        }





        for(Robot r : Map.theirRobots){
            if(!r.isDead){
                Map.theirGhostImages.put(r.id,r);
            }
        }

        try {
            for (Worker w : Map.myWorkers) {
                if (w.goKillYourself > 30) {
                    Debug.log("Disintegrating worker " + w);
                    Player.gc.disintegrateUnit(w.id);
                    w.isDead = true;
                    w.health = 0;

                }
            }
        }catch (Exception ex){
            Debug.log(ex);
        }


    }




    public static void IdentifySacrificialLambs(){
        //We're going to dangle a single unit in front of an enemy overcharging mage and hope they catch the bait, if not, it still makes it at least
        //harder to do major damage due to tactical spreading
        for(Mage m : Map.theirMages){
            int closestdistance = 11;
            Robot closest = null;

            for(Robot r : Map.myRobots){
                int dist = Map.findDistanceTo(r.loc,m.loc);

                if(dist < closestdistance){
                    closestdistance = dist;
                    closest = r;
                }

            }

            if(closest != null){
//                Debug.log("Sacrificing " + closest.loc);
                Circle avoidcircle = new Circle(closest.loc,2);
                for(Robot r : (new Circle(closest.loc,8).containingAlliedRobots())){
                    r.M.addCircle(avoidcircle,-150);
                    closest.M.addCircle(new Circle(r.loc,2),-80);
                }
                closest.M.addGoal(m.loc,5);
            }
        }



    }

}
