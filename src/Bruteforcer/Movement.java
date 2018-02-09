package Bruteforcer;

/**
 * Created by Hermen on 10/6/2017.
 */
public class Movement {

    public  double moveVectorX = 0;
    public  double moveVectorY = 0;

    public  Loc[] specialLocations = new Loc[60];
    public  int specialLocationsCount  = 0;
    public  double[] extraPoints = new double[60];


    public Circle[] circleZones = new Circle[60];
    public int circleCount = 0;
    public double[] circleForces = new double[60];


    public Circle[] enemyZonesMoveAndShootMe = new Circle[60];
    public int[] enemyZoneShootMeDist = new int[60];
    public int[] enemyZoneShootThemDist = new int[60];
    public double[] enemyZoneShootThemForce = new double[60];
    public double[] enemyZoneShootMeForce = new double[60];
    public int enemyZoneCount = 0;



    public Circle[] vectorCircleZones = new Circle[60];
    public int vectorCircleCount = 0;
    public double[] vectorCircleBaseForces = new double[60];
    public double[] vectorCircleDistForces = new double[60];


    public int[] goalNodes = new int[60];
    public double[] goalForces = new double[60];
    public int goalCount = 0;



    public static int[] globalAntiClusterGoals = new int[120];
    public static double[] globalAntiClusterGoalForces = new double[120];
    public static int globalAntiClusterGoalCount = 0;



    public int myX;
    public int myY;
    public Loc myLocation;
    public Circle myMoveCircle;
    public Robot myRobot;

    public int maxShapes;


    public double dangerDesireFactor = 0;
    public double healerDesireFactor = 0;
    public double dangerCutOff = 0;
    public double deathPenalty = 0;
    public double invisibleTileRangerRadius = 0;


    public static float[] V_EndDropOff = new float[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0.95f,0,8f,0.6f,0.35f,0.1f,0f};
    public static float[] V_LinearDropOff = new float[]{1,0.95f,0.9f,0.85f,0.8f,0.75f,0.7f,0.65f,0.6f,0.55f,0.5f,0.45f,0.4f,0.35f,0.3f,0.25f,0.2f,0.15f,0.1f,0.05f,0f};
    public static float[] V_ExpoCenter = new float[]{1,0.8f,0.65f,0.53f,0.43f,0.35f,0.29f,0.25f,0.21f,0.17f, 0.14f, 0.11f,0.08f,0.05f,0.02f,0f};

    public double bestmovescore;


    public Movement(Robot r){
        myRobot = r;
    }

    public void reset(){
        myLocation = myRobot.loc;
        myMoveCircle = new Circle(myLocation,2);
        myX = myLocation.x;
        myY = myLocation.y;
        specialLocationsCount = 0;
        circleCount = 0;
        vectorCircleCount = 0;
        dangerDesireFactor = 0;
        dangerCutOff = 0;
        goalCount = 0;
        enemyZoneCount = 0;
        invisibleTileRangerRadius = 0;

        if(R.POWERSAVEMODUSHYPER){
            maxShapes = 4;
        } else if(R.POWERSAVEMODUS){
            maxShapes = 15;
        } else{
            maxShapes = 40;
        }
    }


    //Adds a vector with a force proportional to what is found in a lookup table of floats
    //It picks the entry that corresponds to the distance/distanceBase * arraylength.
    //So it picks the first entry of the table if distance/distancebase = 0, the last if it's 1 (and picks the closest match in all cases)
    public void addVectorDistanceArray(Loc m, float force, float distanceBase, float[] array){
        int l = array.length -1;
        int deltaX = m.x - myX;
        int deltaY = m.y - myY;
        double factor = force * array[Math.min(l, (int)Math.round(m.distanceTo(myLocation) / distanceBase))] / LookupSqrt.sqrt[deltaX*deltaX+deltaY*deltaY];
        moveVectorX += factor * deltaX;
        moveVectorY += factor * deltaY;
    }




    public  void addVector(Loc m, double force){
        if(myLocation.equals(m)){
            addSpecialLocation(myLocation,force);
            return;
        }
        int deltaX = m.x - myX;
        int deltaY = m.y - myY;
        double factor = force / LookupSqrt.sqrt[deltaX*deltaX+deltaY*deltaY];

        moveVectorX += factor * deltaX;
        moveVectorY += factor * deltaY;
    }


    public void addCircle(Circle c, double force ){
        if( circleCount < maxShapes ) {
            if (c.overlaps(myMoveCircle)) {
                circleZones[circleCount] = c;
                circleForces[circleCount++] = force;
            }
        }
    }
    public void addCircle(Loc l, int distsquared, double force){
        if( circleCount <maxShapes) {
            Circle c = new Circle(l, distsquared);
            if (c.overlaps(myMoveCircle)) {
                circleZones[circleCount] = c;
                circleForces[circleCount++] = force;
            }
        }
    }

    public void addVectorCircle(Circle c, double baseforce, double distforce){
        if( vectorCircleCount < maxShapes ) {
            if (c.overlaps(myMoveCircle)) {
                vectorCircleZones[vectorCircleCount] = c;
                vectorCircleBaseForces[vectorCircleCount] = baseforce + (distforce * c.getRadius());
                vectorCircleDistForces[vectorCircleCount++] = -distforce;
            }
        }
    }
    public void addVectorCircle(Loc l, int distsquared,  double baseforce, double distforce){
        if( vectorCircleCount < maxShapes ) {
            Circle c = new Circle(l, distsquared);
            if (c.overlaps(myMoveCircle)) {

                vectorCircleZones[vectorCircleCount] = c;
                vectorCircleBaseForces[vectorCircleCount] = baseforce + (distforce * c.getRadius());
                vectorCircleDistForces[vectorCircleCount++] = -distforce;
            }
        }
    }

    public void addEnemyZone(Circle theirShootPlusMoveCircle, int theirShoot, int myAttackRange, double attackThemScore, double attackMeScore){
        if( enemyZoneCount < maxShapes ) {
            if (theirShootPlusMoveCircle.overlaps(myMoveCircle) ||  (myAttackRange > theirShoot &&  (new Circle(theirShootPlusMoveCircle,myAttackRange)).overlaps(myMoveCircle)  )  ) {
                enemyZonesMoveAndShootMe[enemyZoneCount] = theirShootPlusMoveCircle;
                enemyZoneShootMeDist[enemyZoneCount] = theirShoot;
                enemyZoneShootThemDist[enemyZoneCount] = myAttackRange;
                enemyZoneShootThemForce[enemyZoneCount] = attackThemScore;
                enemyZoneShootMeForce[enemyZoneCount++] = attackMeScore;
            }
        }
    }



    public void addGoal(Loc m, double force){


        goalForces[goalCount] = force * 0.7;
        goalNodes[goalCount++] = Map.nodeIds[m.x][m.y];

        addVector(m,force * 0.3);


//        if(m.isWithinOffset(myLocation,1)){
//            addCircle(new Circle(m,2),force);
//
//        }else {
//            Loc stepTo = Map.findPathToNextStep(myLocation, m);
//            Loc furtherMovement = Map.findPathAhead(myLocation, m);
//
////            if(myRobot.amIWorker){
////                Debug.log(myLocation + " -> " + m + "  first: " + stepTo +"   further: " + furtherMovement);
////            }
//
//            if (stepTo != null && stepTo.canBeMovedToFrom(myLocation)) {
//                addSpecialLocation(stepTo, force * 0.5);
//                addVector(furtherMovement, force * 0.5);
//
//            } else if (furtherMovement != null) {
//                addVector(furtherMovement, force);
//            } else {
//                addVector(m, force);
//            }
//        }


    }

    public static void addGlobalAntiClusterGoal(Loc m, double force){
        globalAntiClusterGoalForces[globalAntiClusterGoalCount] = force;
        globalAntiClusterGoals[globalAntiClusterGoalCount++] = Map.nodeIds[m.x][m.y];
    }

    public void addSpecialLocation(Loc m, double force){

        if(m.isWithinOffset(myLocation,1)) {
            if (specialLocationsCount < maxShapes) {
                extraPoints[specialLocationsCount] = force;
                specialLocations[specialLocationsCount++] = m;
            }
        }
        //Debug.log("special: " + m.toString());
    }




    public void calcBestMove(){
        if(myRobot.canMove()) {
            bestmovescore = -999999999;
            for (MainLoc l : myRobot.adjacentPassableTilesIncludingThis) {
                if (!R.POWERSAVEMODUSHYPER || l.isWithinSquaredDistance(myLocation, 1)) { //Only diagonals when we have to save energy like crazy
                    Robot r = Map.robots[l.x][l.y];
                    boolean free = r == null;
                    boolean toStructure = !free && (!myRobot.inGarrison || myLocation.equals(l)) && r.amIStructure && r.isMine && !r.amIBlueprint && ((Structure) r).garrisonedWithin.size() < 6;
                    if (free || l.equals(myLocation) || toStructure) {
                        double score = evaluateSpot(l, toStructure);
                        if (score > bestmovescore) {
                            bestmovescore = score;
                            myRobot.bestMove = l;
                        }


//                        if(myRobot.id == 25477){
//                            Debug.log(   l.getDirFrom(myLocation) + ":  " + score );
//                        }
                    }
                }
            }
        }

    }



    public double evaluateSpot(MainLoc l, boolean intoStructure) {

//        boolean debug = myRobot.id == 58062 && R.turn > 130 && R.turn < 150;
        double debugtracker = 0;


//        if(debug){
//            Debug.lineBreak();
//            Debug.log("Score for: " + l);
//        }

        int x = l.x;
        int y = l.y;
        //The engine of the system. Everything comes together to allow this spot evaluation
        double score = -l.distanceTo(moveVectorX, moveVectorY);

//        if(debug && score!=debugtracker){
//            Debug.log((score - debugtracker) + "  vector");
//            debugtracker = score;
//        }

        for (int i = 0; i < specialLocationsCount; i++) {
            if (specialLocations[i].equals(l)) {
                score += extraPoints[i];
            }
        }

//        if(debug && score!=debugtracker){
//            Debug.log((score - debugtracker) + "  speciallocs");
//            debugtracker = score;
//        }

        for (int i = 0; i < circleCount; i++) {
            if (circleZones[i].contains(l)) {
                score += circleForces[i];
            }
        }

//        if(debug && score!=debugtracker){
//            Debug.log((score - debugtracker) + "  circles");
//            debugtracker = score;
//        }

        for (int i = 0; i < vectorCircleCount; i++) {
            if (vectorCircleZones[i].contains(l)) {
                score += vectorCircleBaseForces[i] + l.distanceTo(vectorCircleZones[i]) * vectorCircleDistForces[i];
            }
        }

//        if(debug && score!=debugtracker){
//            Debug.log((score - debugtracker) + "  vectorcircles");
//            debugtracker = score;
//        }

        if(!intoStructure) {
            double attackThemBest = 0;

            for (int i = 0; i < enemyZoneCount; i++) {
                if (enemyZonesMoveAndShootMe[i].contains(l)) {
                    if (enemyZonesMoveAndShootMe[i].isWithinSquaredDistance(l, enemyZoneShootMeDist[i])) {
                        score += enemyZoneShootMeForce[i];
                    } else if(!R.POWERSAVEMODUSLIGHT) {
                        //check if they can realistically shoot us, if not, decrease impact
                        //note, it's the responsibility of the one calling the method to supply move/attack heat info somehow
                        boolean foundPassable = false;
                        boolean foundEmpty = false;

                        for(Loc l2 : enemyZonesMoveAndShootMe[i].adjacentPassableTiles()){
                            if(l2.isWithinSquaredDistance(l,enemyZoneShootMeDist[i])) {
                                foundPassable = true;
                                if (!l2.containsRobot()) {
                                    foundEmpty = true;
                                    break;
                                }
                            }
                        }
                        if(foundEmpty){
                            //still not the full impact, a move costs resources and is dangerous
                            score += enemyZoneShootMeForce[i] * 0.75;
                        } else if(foundPassable){
                            //Significant reduction. Both a move and a deplacement is not something most bots manage
                            score += enemyZoneShootMeForce[i] * 0.4;
                        }

                    }else{
                        score += enemyZoneShootMeForce[i] * 0.75;
                    }
                }
                if (l.isWithinSquaredDistance(enemyZonesMoveAndShootMe[i], enemyZoneShootThemDist[i])) {
                    attackThemBest = Math.max(attackThemBest, enemyZoneShootThemForce[i]);
                }
            }

//            if(debug && score!=debugtracker){
//                Debug.log((score - debugtracker) + "  ezone - bad");
//                debugtracker = score;
//            }

            score += attackThemBest;

//            if(debug && score!=debugtracker){
//                Debug.log((score - debugtracker) + "  ezone - good");
//                debugtracker = score;
//            }

        }

        int thisNode = Map.nodeIds[x][y];
        for (int i = 0; i < goalCount; i++) {
            score -= ((double) Map.findDistanceTo(thisNode, goalNodes[i])) * goalForces[i];
        }

//        if(debug && score!=debugtracker){
//            Debug.log((score - debugtracker) + "  goal");
//            debugtracker = score;
//        }

        for (int i = 0; i < globalAntiClusterGoalCount; i++) {



            Loc loc = Map.findPathToNextStep(thisNode,globalAntiClusterGoals[i]);
            if(loc != null && loc.containsRobot()){
                int freecount = 0;
                for(Loc l3 : myRobot.loc.adjacentPassable){
                    if(!l3.containsRobot()){
                        freecount++;
                    }
                }
                if(freecount < 5) {
                    score -=  Math.max(15.0,(double) Map.findDistanceTo(thisNode, globalAntiClusterGoals[i]))   * globalAntiClusterGoalForces[i];
                }
            }
        }

//        if(debug && score!=debugtracker){
//            Debug.log((score - debugtracker) + "  anticluster");
//            debugtracker = score;
//        }

        if (!intoStructure) {

            if(invisibleTileRangerRadius != 0){
                score += l.GetInvisibleRangerTiles() * invisibleTileRangerRadius;
            }

            //        if(debug && score!=debugtracker){
//            Debug.log((score - debugtracker) + "  vision");
//            debugtracker = score;
//        }

            double possibleDamage = MapMeta.enemyDamageArrayPlusGhost[x][y];

            if(myRobot.amIRanger){
                possibleDamage *= 2; // expect to get hit twice by everyone
            }

            if(possibleDamage >= dangerCutOff) {
                double dmg = MapMeta.enemyDamageArray[x][y];
                double realDmg = MapMeta.enemyDamageArrayReal[x][y];
                double dmgWithoutMove = MapMeta.enemyDamageArrayRealWithoutMovement[x][y];
                double dmgRestrictive = MapMeta.enemyDamageArrayRealRestrictive[x][y];
                double dmgRealisticallyInsta = MapMeta.enemyDamageArrayRealisticallyInsta[x][y];
                double dmgRealisticallyNext = MapMeta.enemyDamageArrayRealisticallyNext[x][y];
                double healing = (MapMeta.healerSupportArray[x][y] * Healer.healerHealing);

                double factor = 1.0;

                //Some factors that make this spot a little more appealing.
                //If we can survive through healing or through their lack of movement, then our risk is much lower
                //Yeah, it's still dangerous, but our one possible way out.
                //These reductions are also supposed to stop bots from just walking straight back in ranger lines combat
                if(dmgRealisticallyInsta - (healing * 0.5) < myRobot.health){
                    if(dmgRealisticallyInsta < myRobot.maxHealth) {
                        factor *= 0.3;
                    }
                } else if(dmgRealisticallyNext - (healing * 1.5) < myRobot.health ){
                    if(dmgRealisticallyNext < myRobot.maxHealth) {
                        factor *= 0.3;
                    }
                }



                if(possibleDamage - healing < dangerCutOff){
                    factor *= 0.95;
                }
                else if(possibleDamage - healing < myRobot.health){
                    factor *= 0.95;
                }

                if(possibleDamage < dangerCutOff){
                    factor *= 0.85;
                }else if(possibleDamage < myRobot.health){
                    factor *= 0.85;
                }


                if(dmg < dangerCutOff){
                    factor *= 0.95;
                }

                if(realDmg - healing < dangerCutOff){
                    factor *= 0.8;
                    if(realDmg  < dangerCutOff){
                        factor *= 0.8;
                    }
                }
                else if(realDmg - healing < myRobot.health){
                    factor *= 0.8;
                }

                if(dmgWithoutMove - healing < dangerCutOff){
                    factor *= 0.8;
                    if(dmgWithoutMove  < dangerCutOff){
                        factor *= 0.8;
                    }
                }
                else if(dmgWithoutMove - healing < myRobot.health){
                    factor *= 0.8;
                }

                if(dmgRestrictive - healing < dangerCutOff){
                    factor *= 0.8;
                    if(dmgRestrictive  < dangerCutOff){
                        factor *= 0.8;
                    }
                }
                else if(dmgRestrictive - healing < myRobot.health){
                    factor *= 0.8;
                }


                score += (1.5 - (R.beBallsyRating * 0.5)) * deathPenalty * factor;

//                if(debug){
//                    Debug.log("Factor: " + factor);
//                }
            }
        }

//        if(debug && score!=debugtracker){
//            Debug.log((score - debugtracker) + "  damage/death");
//            debugtracker = score;
//        }

        double danger = MapMeta.dangerArray[x][y];

        if(intoStructure) {
            score += danger * dangerDesireFactor * 0.5;
        }else{
            score += danger * dangerDesireFactor;
        }

//        if(debug && score!=debugtracker){
//            Debug.log((score - debugtracker) + "  danger");
//            debugtracker = score;
//        }

        score += MapMeta.healerSupportArray[x][y] * healerDesireFactor;

//        if(debug && score!=debugtracker){
//            Debug.log((score - debugtracker) + "  healer");
//            debugtracker = score;
//        }

        score += Map.globalDesire[x][y];

//        if(debug && score!=debugtracker){
//            Debug.log((score - debugtracker) + "  global");
//            debugtracker = score;
//        }

        if(intoStructure){
            if(myRobot.scaryEnemyRobotsNearSize > 0) {
                score -= 150;
                if(myRobot.amIRanger){
                    score -= 50;//cant shoot as much because of low movespeed
                }
            }else{
                score -= (80   -    Math.min(80,(R.turn - myRobot.lastMoved) * 4) );
            }
            if(Map.robots[x][y].health < Math.max(  100,Map.robots[x][y].scaryEnemyRobotsNearSize * 60)){
                //if factory is low health, it's a bad idea to sit inside
                score -= 10000;
            }
            if(R.turn - myRobot.lastConstructed < 3){
                //Stops workers from entering
                score -= 10000;
            }
            if(Map.robots[x][y].amIRocket && R.amIEarth && R.turn > 746){
                score += 100000;
            }


        }
        score += MapMeta.passableTilesAround[x][y] * 0.2;


//        if(debug && score!=debugtracker){
//            Debug.log((score - debugtracker) + "  passable - FINAL");
//        }


//        if(myRobot.id == 15310 && R.turn > 115 && R.turn < 125)

        return score;
    }




}
