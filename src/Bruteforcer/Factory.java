package Bruteforcer;

public class Factory  extends Structure {


    public boolean canProduceSomething = false;

    private static int startbuildpath = 0;

    public int turnsLeftProduction = -1;
    public double threatScore = -1;


    public double antiClusterIntensity = 0;

    public Factory( int id, boolean isMine){
        super(Type.FACTORY,id,isMine);
        amIProducer = true;
        amIFactory = true;
        amIBlueprint = true;
    }

    @Override
    public void init(){}
    @Override
    public void preturn(){

        if(garrisonedWithin.size() > 0) {

            int free = 0;
            int robots = 0;

            for (Loc l : adjacentPassableTiles) {
                if(!l.containsRobot()){
                    free++;
                }else{
                    robots++;
                }
            }

            if(free < 2){
                if(robots > 0) {
                    for(Loc l: adjacentPassableTiles){
                        Map.globalDesire[l.x][l.y] -= 500;
                    }
                    antiClusterIntensity++;
                    if (free < 1) {
                        Movement.addGlobalAntiClusterGoal(loc, -15 - antiClusterIntensity);

                    } else {
                        Movement.addGlobalAntiClusterGoal(loc, -10 - (antiClusterIntensity * 0.5));
                    }
                }else{
                    antiClusterIntensity = 0;
                }

                if(garrisonedWithin.size() > 2){
                    R.heavilystuck = true;
                }
            }else{
                antiClusterIntensity = 0;
            }
        }
    }
    @Override
    public void think(){

    }



    public void buildUnit(){
        if (R.karbonite >= 40) {
            int unit, rand;
            switch (GrandStrategy.strategy) {
                case EARLYROCKETS:
                case ADAPTIVERANGER:
                    if(EnemyStrategy.Strategy == EnemyStrategy.FULLKNIGHT || EnemyStrategy.Strategy == EnemyStrategy.KNIGHTHEALER){
                        if((Map.myUnitCounts[Type.MAGE.typeId]) < Map.myUnitCounts[Type.RANGER.typeId] * 0.35){
                            unit = Type.MAGE.typeId;
                        }else{
                            unit = getStdRangerbuild();
                        }

                    } else if(EnemyStrategy.Strategy == EnemyStrategy.KNIGHTRANGER){
                        if((1 + Map.myUnitCounts[Type.MAGE.typeId]) < Map.myUnitCounts[Type.RANGER.typeId] * 0.2){
                            unit = Type.MAGE.typeId;
                        }else{
                            unit = getStdRangerbuild();
                        }
                    } else{
                        unit = getStdRangerbuild();
                    }



                    break;
                case STDRANGER:
//                    rand = PseudoRandom.next(6);

                    unit = getStdRangerbuild();



//                    if(( (Map.myUnitCounts[Type.WORKER.typeId] <= 8 && R.turn > 550) ||  Map.myUnitCounts[Type.WORKER.typeId] <= 2) && scaryEnemyRobotsNear.size() == 0){
//                        unit = Type.WORKER.typeId;
//                    }
                    break;

                case LATEMAGE:
                    rand = PseudoRandom.next(6);
                    if (rand == 0 && Map.myUnitCounts[Type.HEALER.typeId] < 20 && Map.myUnitCounts[Type.RANGER.typeId] > 3) {
                        unit = Type.HEALER.typeId;
                    } else if (rand < 4 || !Techs.canBlink) {
                        unit = Type.RANGER.typeId;
                    } else {
                        unit = Type.MAGE.typeId;
                    }
//                    if(( (Map.myUnitCounts[Type.WORKER.typeId] <= 8 && R.turn > 550) ||  Map.myUnitCounts[Type.WORKER.typeId] <= 2) && scaryEnemyRobotsNear.size() == 0){
//                        unit = Type.WORKER.typeId;
//                    }
                    break;
                case MAGERUSH:
                    if((Map.myUnitCounts[Type.HEALER.typeId] + 1)  <   Map.myUnitCounts[Type.MAGE.typeId] * 0.1){
                        unit = Type.HEALER.typeId;
                    }
                    else if(R.turn > 45 &&  Map.myUnitCounts[Type.RANGER.typeId] < 1 + (Map.myUnitCounts[Type.MAGE.typeId] * 0.3)){
                        unit = Type.RANGER.typeId;
                    }
                    else {
                        unit = Type.MAGE.typeId;
                    }
                    break;

                case KNIGHTRUSH:


                    if(R.turn > 100  || EnemyStrategy.Strategy == EnemyStrategy.MAGEHEAVY || EnemyStrategy.Strategy == EnemyStrategy.MAGERANGER  ||  ( R.hyperVigilanceHasBeenTriggered && Map.myFactories.size() <= 4)){
                        unit = getStdRangerbuild();
                    }else {

                        if(Map.myTotalUnitsBuilt <= 2){

                            if(startbuildpath == 0){
                                int shortest = 999;
                                for(Factory f : Map.theirFactories){
                                    int dist = Map.findDistanceTo(loc,f.loc);
                                    if( dist < shortest ){
                                        shortest = dist;
                                    }
                                }
                                for(Loc l : Map.theirStartingSpots){
                                    int dist = Map.findDistanceTo(loc,l);
                                    if( dist < shortest ){
                                        shortest = dist;
                                    }
                                }

                                if( shortest < 20 || Map.getEnemiesInRange(new Circle(loc,50)).size() >= 2){
                                    startbuildpath = 1;
                                }else{
                                    startbuildpath = 2;
                                }
                            }
                            if(startbuildpath == 1){
                                if (Map.myTotalUnitsBuilt == 0) {
                                    unit = Type.KNIGHT.typeId;
                                } else if (Map.myTotalUnitsBuilt == 1) {
                                    unit = Type.MAGE.typeId;
                                } else{
                                    unit = Type.RANGER.typeId;
                                }
                            }else {
                                if (Map.myTotalUnitsBuilt == 0) {
                                    unit = Type.MAGE.typeId;
                                } else if (Map.myTotalUnitsBuilt == 1) {
                                    unit = Type.RANGER.typeId;
                                } else{
                                    unit = Type.HEALER.typeId;
                                }
                            }
                        }
                        else {
                            if ((1 + Map.myNonGarrisonedUnitCounts[Type.HEALER.typeId]) * 3 < Map.myUsableTotalDpsCount && scaryEnemyRobotsNearSize == 0) {
                                unit = Type.HEALER.typeId;
                            } else if (Map.myTotalDpsBuilt > 3 && Map.myNonGarrisonedUnitCounts[Type.RANGER.typeId] * 7 < Map.myUsableTotalDpsCount) {
                                unit = Type.RANGER.typeId;
                            } else if (Map.theirUnitCounts[Type.KNIGHT.typeId] > 2 && (Map.myNonGarrisonedUnitCounts[Type.MAGE.typeId] + 1 + (Map.startedBuildCount[Type.MAGE.typeId] - Map.finishedUnitCounts[Type.MAGE.typeId])) * 3 < Map.myUsableTotalDpsCount) {
                                unit = Type.MAGE.typeId;
                            } else {
                                unit = Type.KNIGHT.typeId;
                            }
                        }
                    }



//                    if ((1 + Map.myUnitCounts[Type.HEALER.typeId]) * 2.5 < Map.myUnitCounts[Type.KNIGHT.typeId] && scaryEnemyRobotsNearSize == 0) {
//                        unit = Type.HEALER.typeId;
//                    }
//                    else if(R.turn > 45 &&  Map.myUnitCounts[Type.RANGER.typeId] < 1 + (Map.myUnitCounts[Type.KNIGHT.typeId] * 0.1)){
//                        unit = Type.RANGER.typeId;
//                    }
//                    else {
//                        unit = Type.KNIGHT.typeId;
//                    }

                    break;
                case RANDOM:
                default:
                    unit = PseudoRandom.next(5);


//                        if(unit == Type.RANGER.typeId) unit = Type.KNIGHT.typeId;
                    break;
            }
            if(R.karbonite < Type.fromId(unit).cost ) return;

            try {
                if (Player.gc.canProduceRobot(id, Type.fromId(unit).toUnitType())) {
                    Player.gc.produceRobot(id, Type.fromId(unit).toUnitType());

//                    Debug.log("Factory: " + id + " building");


                    Map.startedBuildCount[unit]++;

                    R.karbonite -= Type.fromId(unit).cost;
                    canProduceSomething = false;

                    Map.myTotalUnitsBuilt++;
                    if(unit == Type.KNIGHT.typeId){
                        Map.myTotalKnightsBuilt++;
                        Map.myTotalDpsBuilt++;
                    }
                    else if(unit == Type.MAGE.typeId){
                        Map.myTotalDpsBuilt++;
                        Map.myTotalMagesBuilt++;

                    }
                    else if(unit == Type.RANGER.typeId){
                        Map.myTotalDpsBuilt++;
                        Map.myTotalRangersBuilt++;
                    }
                    else if(unit == Type.HEALER.typeId){
                        Map.myTotalHealersBuilt++;
                    }

                } else {
                    Player.gc.produceRobot(id, Type.fromId(unit).toUnitType());
                }
            }catch (Exception ex){
                Debug.log("Cant build Type: " + unit + " KArbo: " + R.karbonite + "  playerkar  " + Player.gc.karbonite());
                Debug.log(ex);

            }
        }
    }
    public void buildWorker(){
        if (R.karbonite >= Type.WORKER.cost) {
            if (Player.gc.canProduceRobot(id, Type.WORKER.toUnitType())) {
                Player.gc.produceRobot(id, Type.WORKER.toUnitType());
                R.karbonite -= Type.WORKER.cost;
                canProduceSomething = false;
            }else{
                Player.gc.produceRobot(id, Type.WORKER.toUnitType());
            }
        }
    }

    public int getStdRangerbuild(){
        int unit = -1;


        if (Map.myUnitCounts[Type.KNIGHT.typeId] < 2) {

            Circle near = new Circle(loc, 80);

            double nearby = 0;
            int totalcount = 0;

            for (Robot r : Map.getEnemiesInRange(near)) {
                int dist = Map.findDistanceTo(loc,r.loc);
                if(dist < 14) {
                    if (r.amIFactory) {
                        nearby += 10;
                    } else if (r.amIDps || r.amIHealer) {
                        nearby += 1;
                    } else {
                        nearby += 0.6;
                    }
                    if(dist < 10) {
                        totalcount++;
                    }
                }
            }
            if(totalcount > 15 && Map.myUnitCounts[Type.MAGE.typeId] < 4){
                unit = Type.MAGE.typeId;
            }
            else if (nearby > 7) {
                unit = Type.KNIGHT.typeId;
            }
            if(R.turn < 100 && Map.myTotalKnightsBuilt == 0){
                int shortest = 999;
                for(Factory f : Map.theirFactories){
                    int dist = Map.findDistanceTo(loc,f.loc);
                    if( dist < shortest ){
                        shortest = dist;
                    }
                }
                for(Loc l : Map.theirStartingSpots){
                    int dist = Map.findDistanceTo(loc,l);
                    if( dist < shortest ){
                        shortest = dist;
                    }
                }

                if(shortest < 20){
                    unit = Type.KNIGHT.typeId;
                }
            }
        }

        if(R.turn > 700){
            if(Map.myUnitCounts[Type.MAGE.typeId] < Map.myRockets.size() * 0.5){
                //To clear mars of enemy workers
                unit = Type.MAGE.typeId;
            }
        }


        if(unit == -1) {

            double healerfactor = 1.6;

            if(MapSections.sectionOpenRatio[loc.sectionId] < 0.75){
                healerfactor -= 0.3;
            }


            if(Techs.techHealer >= 2 && R.turn > 175){ //get ready for some overcharge madness

                int magesproducing =  Map.startedBuildCount[Type.MAGE.typeId] - Map.finishedUnitCounts[Type.MAGE.typeId];

                if (Map.myNonGarrisonedUnitCounts[Type.MAGE.typeId]  + magesproducing <= 2 || ( Techs.canOverload && Map.myNonGarrisonedUnitCounts[Type.MAGE.typeId] < Map.myUsableTotalDpsCount * 0.08) ){
                    unit = Type.MAGE.typeId;
                }
                else if ((1 + Map.myNonGarrisonedUnitCounts[Type.HEALER.typeId]) * (healerfactor - 0.4) < Map.myNonGarrisonedUnitCounts[Type.RANGER.typeId]   || ( Map.myUsableTotalDpsCount > 6 &&  (Map.myTotalHealersBuilt + 1) * 4 < Map.myTotalDpsBuilt) ) {
                    unit = Type.HEALER.typeId;
                } else {
                    unit = Type.RANGER.typeId;
                }
            }else{

//                if(R.player1) {

//                    if (Map.myTotalMagesBuilt == 0 && R.shortestRush < 30 && Map.myTotalRangersBuilt > 0) {
//                        unit = Type.MAGE.typeId;
//                    } else if (((1 + Map.myNonGarrisonedUnitCounts[Type.HEALER.typeId]) * 1.9 < Map.myNonGarrisonedUnitCounts[Type.RANGER.typeId] || (Map.myTotalRangersBuilt >= 2 && Map.myTotalHealersBuilt <= 1)) && scaryEnemyRobotsNearSize == 0) {
//                        unit = Type.HEALER.typeId;
//                    } else {
//                        unit = Type.RANGER.typeId;
//                    }

//                }else{
//



                    if (Map.myTotalMagesBuilt == 0 && R.shortestRush < 30 && Map.myTotalRangersBuilt > 0) {
                        unit = Type.MAGE.typeId;
                    } else if (((1 + Map.myNonGarrisonedUnitCounts[Type.HEALER.typeId]) * healerfactor < Map.myNonGarrisonedUnitCounts[Type.RANGER.typeId] || (( Map.myUsableTotalDpsCount > 6 &&  (Map.myTotalHealersBuilt + 1) * 2.5 < Map.myTotalDpsBuilt)) || (Map.myTotalRangersBuilt >= 2 && Map.myTotalHealersBuilt <= 1))) {
                        unit = Type.HEALER.typeId;
                    } else {
                        unit = Type.RANGER.typeId;
                    }
//                }
            }


        }


        return unit;
    }
}