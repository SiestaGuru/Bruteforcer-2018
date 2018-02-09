package Testing;

import Bruteforcer.*;
import Bruteforcer.Robot;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Random;

public class Testing {

    @Test
    public void SortTest(){
//        ObjectSorting.AddToSort("hi1",1);
//        ObjectSorting.AddToSort("hi2",2);
//        ObjectSorting.AddToSort("hi3",3);
//        ObjectSorting.AddToSort("hi7",7);
//        ObjectSorting.AddToSort("hi3",3);
//
//        ArrayList<Object> o1 = ObjectSorting.SortAsc();
//
//
//        ObjectSorting.AddToSort("hi1",1);
//        ObjectSorting.AddToSort("hi2",2);
//        ObjectSorting.AddToSort("hi3",3);
//        ObjectSorting.AddToSort("hi7",7);
//        ObjectSorting.AddToSort("hi3",3);
//
//        ArrayList<Object> o2 = ObjectSorting.SortDesc();
//
//
//        for(Object o : o1){
//            System.out.println(o);
//        }
//        Debug.lineBreak();
//        for(Object o : o2){
//            System.out.println(o);
//        }
    }


    @Test
    public void containingmaplocs(){

        Map.init(50,50);


        for(int x = 0 ; x < 50; x++){
            for(int y = 0 ; y < 50; y++){
                for(int r = 0 ; r < 100; r++) {
                    Circle c = new Circle(x,y,r);
                    ArrayList list1 = c.allContainingLocationsOnTheMap();
                    ArrayList list2 = c.allContainingLocationsOnTheMapFast();

                    if(list1.size() != list2.size()){
                        Debug.log("BAD");
                    }
                }
            }
        }



    }

    @Test
    public void map(){

        int width = 50;
        int height = 50;
        Map.init(width,height);


        Loc startloc = new Loc(8,8);
        Map.myStartingSpots.add(startloc);
//        R.myPlanet = Planet.Earth;

        Loc[] nearlocs = new Loc[8];

        nearlocs[0] = startloc.add(0,1);
        nearlocs[1] = startloc.add(0,-1);
        nearlocs[2] = startloc.add(1,0);
        nearlocs[3] = startloc.add(-1,0);
        nearlocs[4] = startloc.add(1,1);
        nearlocs[5] = startloc.add(1,-1);
        nearlocs[6] = startloc.add(-1,1);
        nearlocs[7] = startloc.add(-1,-1);


        Random r = new Random();


        for(int x =0; x < width; x++){
            for(int y =0; y < height; y++){
                if(x != startloc.x || y != startloc.y) {
                    if (r.nextInt(4) == 0) {

                        Map.blocked[x][y] = true;
                    }
                }
            }

        }


        startStopwatch();
        for(int y =0; y < height; y++){
            for(int x =0; x < width; x++){
                Map.findPathToNextStep(new Loc(x,y),startloc);
            }
        }

        stopStopwatch("Time:");



        Map.theirRobots = new ArrayList<>();
        Map.myRobots = new ArrayList<>();

        int id = 0;
        for(int i =0 ;i < 8; i++) {
            Map.theirRobots.add(new Ranger(id++, false));
            Map.myRobots.add(new Ranger(id++, true));
            Map.theirRobots.add(new Knight( id++, false));
            Map.myRobots.add(new Knight(id++, true));
            Map.theirRobots.add(new Mage( id++, false));
            Map.myRobots.add(new Mage(id++, true));

            Map.theirRobots.add(new Healer( id++, false));
            Map.myRobots.add(new Healer(id++, true));


        }
        for(Robot rter : Map.theirRobots){

            Loc l= new Loc(r.nextInt(width/2) + (width/2),r.nextInt(height));


            while(Map.robots[l.x][l.y] != null && Map.blocked[l.x][l.y]){
                l = new Loc((r.nextInt(r.nextInt(9999999)) % (width /2)) + (width / 2),r.nextInt(r.nextInt(9999999)) % height);
            }
            rter.turnUpdate(l, 100, 0,0,0);
            Map.robots[l.x][l.y] = rter;

        }
        for(Robot rter : Map.myRobots){
            Loc l =  new Loc(r.nextInt(width/2),r.nextInt(height));

            while(Map.robots[l.x][l.y] != null && Map.blocked[l.x][l.y]){
                l = new Loc(r.nextInt(r.nextInt(9999999)) % (width /2),r.nextInt(r.nextInt(9999999)) % height);
            }
            rter.turnUpdate(l, 100, 0,0,0);
            Map.robots[l.x][l.y] = rter;
        }


//        for(int y =0; y < hegith; y++){
//            for(int x =0; x < width; x++){
//                double dist = Map.findDistanceTo(startloc,new Loc(x,y));
//                System.out.println(x + "," + y + ": " + dist);
//            }
//
//        }
        startStopwatch();
        MapMeta.UpdateDangerMap();

        stopStopwatch("Danger map:");


//        Robot robot = new Robot(Type.RANGER,0,true);
//        robot.turnUpdate(startloc,100,0,0,0);
//        robot.Movement.addGoal(new Loc(0,0),10);
//        robot.Movement.addSpecialLocation(robot.loc.add(Dir.N),999);
//        robot.Movement.calcBestMove();

//        Debug.log(robot.loc);
//        Debug.log(robot.bestMove);
//
//        Debug.log("range: " + robot.attackRange);
//        Debug.log("Size circle: " +  robot.attackCircle.allContainingLocationsOnTheMap().size());


        Debug.lineBreak();
        Debug.log("HasRobot");
        Debug.lineBreak();

        for(int y =height - 1; y >= 0; y--) {
            String s = "";
            for (int x = 0; x < width; x++) {
               if(Map.robots[x][y] == null){
                   s += "-";
               } else if(Map.robots[x][y].isMine){
                   s += "1";
               } else{
                   s += "2";
               }
            }
            Debug.log(s);
        }



        Debug.lineBreak();
        Debug.log("Danger");
        Debug.lineBreak();

        for(int y =height - 1; y >= 0; y--) {
            String s = "";
            for (int x = 0; x < width; x++) {
                s += (int)((MapMeta.dangerArray[x][y] / 30.0) % 10);
            }
            Debug.log(s);
        }

        Debug.lineBreak();
        Debug.log("Attackrange");
        Debug.lineBreak();

        for(int y =height - 1; y >= 0; y--) {
            String s = "";
            for (int x = 0; x < width; x++) {
                s += MapMeta.withinAttackRangeArray[x][y] % 10;
            }
            Debug.log(s);
        }


        Debug.lineBreak();
        Debug.log("Healer");
        Debug.lineBreak();


        for(int y =height - 1; y >= 0; y--) {
            String s = "";
            for (int x = 0; x < width; x++) {
                s += MapMeta.healerSupportArray[x][y] % 10;
            }
            Debug.log(s);
        }


        Debug.lineBreak();
        Debug.log("Sight");
        Debug.lineBreak();

        for(int y =height - 1; y >= 0; y--) {
            String s = "";
            for (int x = 0; x < width; x++) {
                if(Map.inSight[x][y]){
                    s += "O";
                }else{
                    s += "-";
                }
            }
            Debug.log(s);
        }




        Debug.lineBreak();
        Debug.lineBreak();
        Debug.seperator();
        Debug.lineBreak();
        Debug.lineBreak();



//        for(int y =height - 1; y >= 0; y--){
//            String s = "";
//            for(int x =0; x < width; x++){
//                Loc loc = new Loc(x,y);
//                if(robot.bestMove.equals(loc)){
//                    s += "+";
//                }else {
//
//                    Loc firsttep = Map.findPathToNextStep(startloc, new Loc(x, y));
//
//                    if (firsttep == null) {
//                        s += "-";
//                    } else {
//
//                        boolean found = false;
//                        for (int i = 0; i < 8; i++) {
//                            if (firsttep.equals(nearlocs[i])) {
//                                s += i;
//                                found = true;
//                                break;
//                            }
//                        }
//                        if (!found) {
//                            s += "X";
//                        }
//                    }
//                }
//
//
//            }
//            System.out.println(s);
//        }



        Debug.lineBreak();
        Debug.lineBreak();
        Debug.seperator();
        Debug.lineBreak();
        Debug.lineBreak();





        System.out.println("Control map,  Width: " + MapMeta.controlColumns + " Height: " + MapMeta.controlRows);

        for(int x = 0; x < MapMeta.controlColumns; x++){
            String s = "";
            for(int y = 0; y < MapMeta.controlRows; y++){
                s += MapMeta.controlLocations[x][y];

            }
            Debug.log(s);
        }

        Debug.lineBreak();
        Debug.lineBreak();

        for(int x = 0; x < MapMeta.controlColumns; x++){
            String s = "";
            for(int y = 0; y < MapMeta.controlRows; y++){
                if(MapMeta.controlIsReachable[x][y]){
                    s += "O";
                }else{
                    s += "-";
                }

            }
            Debug.log(s);
        }


        Debug.lineBreak();
        Debug.lineBreak();
        for(int y = MapMeta.controlRows -1; y >= 0 ; y--){
            String s = "";
            for(int x = 0; x < MapMeta.controlColumns; x++){
                s += (int)(5 + (MapMeta.controlMap[x][y] / 50.0)) % 10;
               // s += MapMeta.controlMap[x][y] + ",";
            }
            Debug.log(s);
        }
        Debug.lineBreak();
        Debug.lineBreak();

        for(int y = MapMeta.controlRows -1; y >= 0 ; y--){
            for(int x = 0; x < MapMeta.controlColumns; x++){

                Debug.log(x + "," + y + ": " +   MapMeta.controlMap[x][y] + ",");
            }
        }

        System.out.println("");


        Debug.lineBreak();
        Debug.lineBreak();
        Debug.lineBreak();
        Debug.lineBreak();


        int count1= 0;
        int count2= 0;
        for(int x= 0; x < Map.width; x++){
            for(int y= 0;y < Map.height; y++){
                if(Map.robots[x][y] != null){
                    count1++;
                }
            }
        }
        Debug.log(count1);
        Debug.log(Map.myRobots.size() + Map.theirRobots.size());



        int error = 0;
        int shift = 0;
        for(Robot rob: Map.myRobots){

            int count = 0;


            for(Robot rob2: Map.myRobots){
                if(!rob2.equals(rob)){
                    if(rob.loc.isWithinSquaredDistance(rob2.loc,rob.sightRange)) {
                        count++;
                        System.out.print(rob2.loc);
                    }
                }
            }
            for(Robot rob2: Map.theirRobots){
                if(!rob2.equals(rob)){
                    if(rob.loc.isWithinSquaredDistance(rob2.loc,rob.sightRange)) {
                        count++;
                        System.out.print(rob2.loc);
                    }
                }
            }
            Debug.lineBreak();
            Debug.log("nextup " + rob.sightCircle  + "  " + rob.sightRange);

            int countold = Map.getRobotsInRangeFilterId(rob.sightCircle,rob.id).size();

            Debug.log(Map.getRobotsInRangeFilterId(rob.sightCircle,rob.id));

            if(count != countold){
                error++;
                shift += (countold - count);
            }

            Debug.lineBreak();Debug.lineBreak();Debug.lineBreak();
        }

        Debug.log("Error: " + error);
        Debug.log("Shift: " + shift);




        startStopwatch();
        MapMeta.CalcControlMapPathing();
        stopStopwatch("controlpathing");

        for(int column1 = 0; column1 < MapMeta.controlColumns; column1++) {
            for (int row1 = 0; row1 < MapMeta.controlRows; row1++) {
                for (int column2 = 0; column2 < MapMeta.controlColumns; column2++) {
                    for (int row2 = 0; row2 < MapMeta.controlRows; row2++) {

                        String s = column1 + "," + row1 + " -> " + column2 + "," + row2 + "   ";
                        for (int x = 0; x < MapMeta.controlZonePaths[column1][row1][column2][row2].length; x++) {
                            s += MapMeta.controlZonePaths[column1][row1][column2][row2][x];
                        }
                        System.out.println(s);

                    }
                }
            }
        }


    }


    @Test
    public void pseudorandom(){

        R.turn = 0;
        R.karbonite = 100;
        Map.totalUnitCount= 0;

        Random r = new Random();

        int[] counts = new int[128];

        for(int i = 0 ; i < 10000;i++){


            if(i % 70 == 0){
                R.turn++;
                R.karbonite = r.nextInt(256);
                Map.totalUnitCount = r.nextInt(512);
                Debug.lineBreak();

            }
            int rand = PseudoRandom.next(128);
            System.out.print(rand + ", ");
            counts[rand]++;

        }
        Debug.lineBreak();
        Debug.log(counts);


    }


    @Test
    public void hello(){




        Random r = new Random();


        int errors = 0;



        for(int i = 0; i < 1000000; i++){

            Circle c1 = new Circle(r.nextInt(50),r.nextInt(50),r.nextInt(100));
            Circle c2 = new Circle(r.nextInt(50),r.nextInt(50),r.nextInt(100));

            boolean overlapsMethod = c1.overlaps(c2);
            boolean overlapsBruteforce = false;
            outerloop:
            for(int x = 0; x < 50; x++){
                for(int y = 0; y < 50; y++){
                    if(c1.contains(x,y) && c2.contains(x,y)){
                        overlapsBruteforce = true;
                        break outerloop;
                    }
                }
            }

            if(overlapsMethod != overlapsBruteforce){
//                errors++;
//                System.out.println("Error");
//                System.out.println(c1.toString());
//                System.out.println(c2.toString());
            }

        }

        System.out.println("er" + errors);


    }



    long nanosecs = 0;

    public void startStopwatch() {
        nanosecs = System.nanoTime();
    }

    public long stopStopwatch(String print) {
        long newtime = System.nanoTime();
        System.out.println(print + " ns: " + (newtime - nanosecs)   +  "  ms: " + ((float)(newtime - nanosecs)) / 1000000f  );
        return (newtime - nanosecs);
    }
    public long stopStopwatchAvg(String print,float items) {
        long newtime = System.nanoTime();
        System.out.println(print + " AVERAGE: ns: " +  (float)(newtime - nanosecs) / items    +  "  ms: " + ((float)(newtime - nanosecs)) / (1000000f * items)  );
        return (newtime - nanosecs);
    }
}
