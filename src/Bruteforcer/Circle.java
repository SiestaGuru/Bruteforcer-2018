package Bruteforcer;

import java.util.ArrayList;

public class Circle extends Loc {

    public final int radiusSquared;

    public final static Circle nullCircle = new Circle(0,0,0);

    public Circle(int x, int y, int radiusSquared){
        super(x,y);
        this.radiusSquared = radiusSquared;
    }

    public Circle(Loc l, int radiusSquared) {
        super(l.x,l.y);
        this.radiusSquared = radiusSquared;
    }

    public double getRadius(){
        return LookupSqrt.sqrt[radiusSquared];
    }

    public boolean equals(Circle l){
        return x == l.x && y == l.y && l.radiusSquared == radiusSquared;
    }


    public String toString(){
        return "[" + x + "," + y +", r:" + radiusSquared + "]";
    }

    public boolean overlaps(Circle loc){
        return Fast.distanceTo(x,y,loc.x,loc.y) <= (LookupSqrt.sqrt[radiusSquared] + LookupSqrt.sqrt[loc.radiusSquared]);
    }

    public boolean contains(Loc l){
        int dy =  l.y-y;
        int dx =  l.x-x;
        return dx * dx + dy * dy <= radiusSquared;
    }

    public boolean contains(Robot r){
        int dy = r.y-y;
        int dx =  r.x-x;
        return dx * dx + dy * dy <= radiusSquared;
    }

    public boolean containsNullable(Loc l) {
        if(l == null)return false;
        int dy = l.y-y;
        int dx =  l.x-x;
        return dx * dx + dy * dy <= radiusSquared;
    }
    public boolean containsNullable(Robot r) {
        if(r == null)return false;
        int dy = r.y-y;
        int dx =  r.x-x;
        return dx * dx + dy * dy <= radiusSquared;
    }

    public boolean contains(int x1, int y1){
        int dy =  x1-y;
        int dx =  y1-x;
        return dx * dx + dy * dy <= radiusSquared;
    }

    public ArrayList<Loc> allContainingLocations() {
        ArrayList<Loc> locs = new ArrayList<>(radiusSquared);
        int radius = ((int) LookupSqrt.sqrt[radiusSquared]);
        for (int x1 = x - radius; x1 <= x + radius; x1++) {
            int dx = x1 - x;
            int h = (int) LookupSqrt.sqrt[radiusSquared - dx * dx];
            for (int y1 = y - h; y1 <= y + h; y1++) {
                locs.add(new Loc(x1,y1));
            }
        }
        return locs;
    }

    public int getInvisibleTiles(){
        int count = 0;
        int radius = ((int) LookupSqrt.sqrt[radiusSquared]);
        for (int x1 = Math.max(x - radius,0); x1 <= Math.min(x + radius,Map.widthMinusOne); x1++) {
            int dx = x1 - x;
            int  h = (int) LookupSqrt.sqrt[radiusSquared - dx * dx];
            int until = Math.min(y + h,Map.heightMinusOne);
            for (int y1 = Math.max(y - h,0); y1 <= until; y1++) {
                if(!Map.inSight[x1][y1]){
                    count++;
                }
            }
        }
        return count;
    }
    public int getVisibleTiles(){
        int count = 0;

        int radius = ((int) LookupSqrt.sqrt[radiusSquared]);

        for (int x1 = Math.max(x - radius,0); x1 <= Math.min(x + radius,Map.widthMinusOne); x1++) {
            int dx = x1 - x;
            int  h = (int) LookupSqrt.sqrt[radiusSquared - dx * dx];
            int until = Math.min(y + h,Map.heightMinusOne);
            for (int y1 = Math.max(y - h,0); y1 <= until; y1++) {
                if(Map.inSight[x1][y1]){
                    count++;
                }
            }
        }

        return count;
    }




    public ArrayList<MainLoc> allContainingLocationsOnTheMap() {
        int radius = ((int) LookupSqrt.sqrt[radiusSquared]);
        ArrayList<MainLoc> locs = new ArrayList<>(radiusSquared);

        for (int x1 = Math.max(x - radius,0); x1 <= Math.min(x + radius,Map.widthMinusOne); x1++) {
            int dx = x1 - x;
            int  h = (int) LookupSqrt.sqrt[radiusSquared - dx * dx];
            int until = Math.min(y + h,Map.heightMinusOne);
            for (int y1 = Math.max(y - h,0); y1 <= until; y1++) {
                locs.add(Map.locations[x1][y1]);
            }
        }
        return locs;
    }

    public ArrayList<MainLoc> allContainingPassableLocations() {
        int radius = ((int) LookupSqrt.sqrt[radiusSquared]);
        ArrayList<MainLoc> locs = new ArrayList<>(radiusSquared);

        for (int x1 = Math.max(x - radius,0); x1 <= Math.min(x + radius,Map.widthMinusOne); x1++) {
            int dx = x1 - x;
            int  h = (int) LookupSqrt.sqrt[radiusSquared - dx * dx];
            int until = Math.min(y + h,Map.heightMinusOne);
            for (int y1 = Math.max(y - h,0); y1 <= until; y1++) {
                if(!Map.blocked[x1][y1]) {
                    locs.add(Map.locations[x1][y1]);
                }
            }
        }
        return locs;
    }

    public ArrayList<MainLoc> allContainingLocationsOnTheMapOld() {
        ArrayList<MainLoc> locs = new ArrayList<>();
        int radius = ((int) LookupSqrt.sqrt[radiusSquared]) + 1;

        for (int x1 = Math.max(x - radius,0); x1 <= Math.min(x + radius,Map.widthMinusOne); x1++) {
            int dx = x1 - x;
            for (int y1 = Math.max(y - radius,0); y1 <= Math.min(y + radius,Map.heightMinusOne); y1++) {
                int dy = y1 - y;
                if (dx * dx + dy * dy <= radiusSquared) {
                    locs.add(Map.locations[x1][y1]);
                }
            }
        }
        return locs;
    }

    public ArrayList<MainLoc> allSpotsAfterMove() {
        ArrayList<MainLoc> locs = new ArrayList<>();
        int radius = ((int) LookupSqrt.sqrt[radiusSquared]) + 2;

        for (int x1 = x - radius; x1 <= x + radius; x1++) {
            for (int y1 = y - radius; y1 <= y + radius; y1++) {
                if(Map.isOnTheMap(x1,y1) && canReachWithStep(x1,y1,radiusSquared)){
                    locs.add(Map.locations[x1][y1]);
                }
            }
        }
        return locs;

    }


    public ArrayList<Robot> containingRobots(){
        return Map.getRobotsInRange(this);
    }
    public ArrayList<Robot> containingEnemyRobots(){
        return Map.getEnemiesInRange(this);
    }
    public ArrayList<Robot> containingAlliedRobots(){
        return Map.getAlliesInRange(this);
    }
    public ArrayList<Robot> containingRobots(Type type){
        return Map.getRobotsInRange(this,type);
    }
    public ArrayList<Robot> containingEnemyRobots(Type type){
        return Map.getEnemiesInRange(this,type);
    }
    public ArrayList<Robot> containingAlliedRobots(Type type){
        return Map.getAlliesInRange(this,type);
    }
}
