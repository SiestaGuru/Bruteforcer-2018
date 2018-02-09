package Bruteforcer;

import bc.Direction;
import bc.MapLocation;
import bc.Planet;
import bc.UnitType;

import java.util.ArrayList;

//Immutable
public class Loc {

    public final int x;
    public final int y;
    


    

    public static final Loc ZERO_LOCATION = new Loc(0,0);

    public Loc(int x, int y){
        this.x = x;
        this.y = y;

    }
    public strictfp int hashCode() {
        return this.x + this.y * 535;
    }

    public String toString(){
        return "[" + x + "," + y +"]";
    }

    public boolean equals(Loc l){
        if(l == null)return false;
        return x == l.x && y == l.y;
    }

    public boolean isWithinOffset(Loc l, int offset){
        return Math.abs(l.x-x) <= offset && Math.abs(l.y-y) <= offset;


    }

    public Loc add(Loc loc){
        return new Loc(x+loc.x,y+loc.y);
    }

    public Loc add(int x1, int y1) {
        return new Loc(x + x1, y + y1);
    }

    public Loc add(Dir d){
        switch (d){
            case N:
                return new Loc(x,y+1);
            case E:
                return new Loc(x+1,y);
            case S:
                return new Loc(x,y-1);
            case W:
                return new Loc(x-1,y);
            case NE:
                return new Loc(x+1,y+1);
            case NW:
                return new Loc(x-1,y+1);
            case SE:
                return new Loc(x+1,y-1);
            case SW:
                return new Loc(x-1,y-1);
            default:
                return this;

        }
    }

    public Loc add(Dir d, int tiles){
        switch (d){
            case N:
                return new Loc(x,y+tiles);
            case E:
                return new Loc(x+tiles,y);
            case S:
                return new Loc(x,y-tiles);
            case W:
                return new Loc(x-tiles,y);
            case NE:
                return new Loc(x+tiles,y+tiles);
            case NW:
                return new Loc(x-tiles,y+tiles);
            case SE:
                return new Loc(x+tiles,y-tiles);
            case SW:
                return new Loc(x-tiles,y-tiles);
            default:
                return this;

        }
    }






    public MainLoc[] adjacentTiles(){
        return Map.locations[x][y].adjacent;
    }

    public MainLoc[] adjacentTilesIncludingThis(){
        return Map.locations[x][y].adjacentIncludingThis;
    }

    public MainLoc[] adjacentPassableTiles(){
        return Map.locations[x][y].adjacentPassable;
    }
    public MainLoc[] adjacentPassableTilesIncludingThis(){
        return Map.locations[x][y].adjacentPassableIncludingThis;
    }

    //Can be used for the other planet
    public ArrayList<Loc> adjacentLocsWithin(int left, int right, int bottom, int top) {
        ArrayList<Loc> locs = new ArrayList<>();

        for (int x1 = Math.max(x - 1, left); x1 <= Math.min(x + 1, right -1); x1++) {
            for (int y1 = Math.max(y - 1, bottom); y1 <= Math.min(y + 1, top -1); y1++) {
                if (x1 != x || y1 != y) {
                    locs.add(new Loc(x1, y1));
                }
            }
        }
        return locs;
    }


    public boolean isOnTheMap(){
        return  x >= 0 && x < Map.width && y >= 0 && y < Map.height;
    }

    public Loc subtract(Loc loc){
        return new Loc(x-loc.x,y-loc.y);
    }
    public Loc subtract(int x1, int y1){
        return new Loc(x-x1,y-y1);
    }



    public boolean isPassable(){
        return !Map.blocked[x][y];
    }
    public boolean isEmpty(){
        return isOnTheMap() && !Map.blocked[x][y] && Map.robots[x][y] == null;
    }
    public boolean containsRobot(){
        return Map.robots[x][y] != null;
    }
    public Robot getRobot(){
        return Map.robots[x][y];
    }
    public boolean canBeMovedToFrom(Loc location){
        return isEmpty() && isWithinOffset(location,1);
    }





    public Loc towards(Loc l, double dist){
        int dy =  l.y-y;
        int dx =  l.x-x;
        double linedist =  LookupSqrt.sqrt[dx*dx+dy*dy];
        if(linedist == 0) return this;
        double fraction =  dist / linedist;
        return new Loc((int)(x + 0.5 + fraction* dx) ,(int)(y + 0.5 + fraction * dy));
    }
    public Loc towards(int x1, int y1, double dist){
        int dy =  y1-y;
        int dx =  x1-x;
        double linedist =  LookupSqrt.sqrt[dx*dx+dy*dy];
        if(linedist == 0) return this;
        double fraction =  dist / linedist;
        return new Loc((int)(x + 0.5 + fraction* dx) ,(int)(y + 0.5 + fraction * dy));
    }


    public Loc away(Loc l, double dist){
        int dy =  l.y-y;
        int dx =  l.x-x;
        double linedist =  LookupSqrt.sqrt[dx*dx+dy*dy];
        if(linedist == 0) return this;
        double fraction =  dist / linedist;
        return new Loc((int)(x + 0.5 - fraction* dx) ,(int)(y + 0.5 - fraction * dy));
    }
    public Loc away(int x1, int y1, double dist){
        int dy =  y1-y;
        int dx =  x1-x;
        double linedist =  LookupSqrt.sqrt[dx*dx+dy*dy];
        if(linedist == 0) return this;
        double fraction =  dist / linedist;
        return new Loc((int)(x + 0.5 - fraction* dx) ,(int)(y + 0.5 - fraction * dy));
    }


    public boolean isWithinDistance(Loc loc, double distance){
        double deltax = loc.x - x;
        double deltay = loc.y - y;
        return deltax * deltax + deltay * deltay < distance * distance;
    }
    public boolean isWithinDistance(Robot r, double distance){
        double deltax = r.x - x;
        double deltay = r.y - y;
        return deltax * deltax + deltay * deltay < distance * distance;
    }

    public boolean isWithinDistance(double x1, double y1, double distance){
        x1 -= x;
        y1 -= y;
        return x1 * x1 + y1 * y1 < distance * distance;
    }

    public boolean isFurtherThan(Loc loc, double distance){
        double deltax = loc.x - x;
        double deltay = loc.y - y;
        return deltax * deltax + deltay * deltay > distance * distance;
    }
    public boolean isFurtherThan(double x1, double y1, double distance){
        x1 -= x;
        y1 -= y;
        return x1 * x1 + y1 * y1 > distance * distance;
    }


    public boolean isWithinSquaredDistance(Loc loc, int distance){
        double deltax = loc.x - x;
        double deltay = loc.y - y;
        return deltax * deltax + deltay * deltay <= distance;
    }
    public boolean isWithinSquaredDistance(Robot r, int distance){
        double deltax = r.x - x;
        double deltay = r.y - y;
        return deltax * deltax + deltay * deltay <= distance;
    }
    public boolean isWithinSquaredDistance(double x1, double y1, int distance){
        x1 -= x;
        y1 -= y;
        return x1 * x1 + y1 * y1 <= distance;
    }

    public boolean isFurtherThanSquared(Loc loc, int distance){
        double deltax = loc.x - x;
        double deltay = loc.y - y;
        return deltax * deltax + deltay * deltay > distance;
    }
    public boolean isFurtherThanSquared(double x1, double y1, int distance){
        x1 -= x;
        y1 -= y;
        return x1 * x1 + y1 * y1 > distance;
    }

    public int distanceSquared(Loc loc){
        int deltax = loc.x - x;
        int deltay = loc.y - y;
        return deltax * deltax + deltay * deltay;
    }
    public double distanceSquared(int x1, int y1){
        x1 -= x;
        y1 -= y;
        return x1 * x1 + y1 * y1;
    }

    public double distanceTo(Loc loc){
        return Fast.distanceTo(x,y,loc.x,loc.y);
    }
    public double distanceTo(int x1,int y1){
        return Fast.distanceTo(x,y,x1,y1);
    }
    public double distanceTo(double x1,double y1){
        return Fast.distanceTo((double)x,(double)y,x1,y1);
    }


    public boolean isWithin(Circle loc){
        int deltax = loc.x - x;
        int deltay = loc.y - y;
        return deltax * deltax + deltay * deltay <= loc.radiusSquared;
    }
    public boolean isOutside(Circle loc){
        int deltax = loc.x - x;
        int deltay = loc.y - y;
        return deltax * deltax + deltay * deltay > loc.radiusSquared;
    }
    public boolean isOnBorder(Circle loc){
        int deltax = loc.x - x;
        int deltay = loc.y - y;
        return deltax * deltax + deltay * deltay == loc.radiusSquared;
    }

    public boolean canReachWithStepAndAttack(Robot r){

        int deltax;
        int deltay;

        if(!r.isMine || r.canMove()){
            deltax = Math.abs(r.loc.x - x) - 1;
            deltay = Math.abs(r.loc.y - y) - 1;
        }else{
            deltax = r.loc.x - x;
            deltay = r.loc.y - y;
        }

        return deltax * deltax + deltay * deltay <= r.attackRange;
    }

    public boolean canReachWithStep(Circle c){
        int deltax = Math.abs(c.x - x) - 1;
        int deltay = Math.abs(c.y - y) - 1;
        return deltax * deltax + deltay * deltay <= c.radiusSquared;
    }
    public boolean canReachWithStep(int x1, int y1, int distSquared){
        int deltax = Math.abs(x1 - x) - 1;
        int deltay = Math.abs(y1 - y) - 1;
        return deltax * deltax + deltay * deltay <= distSquared;
    }


    public MapLocation toMapLocation(){
        return new MapLocation(R.myPlanet,x,y);
    }

    public MapLocation toEarthMapLocation(){
        return new MapLocation(Planet.Earth,x,y);
    }
    public MapLocation toMarsMapLocation(){
        return new MapLocation(Planet.Mars,x,y);
    }


    public static Loc fromMapLocation(MapLocation m){
        return new Loc(m.getX(),m.getY());
    }

    
    public Direction getDirectionFrom(Loc l){

        if(equals(l)) return Direction.Center;

        Loc directional = subtract(l);
        if(directional.x == -1){
            if(directional.y == -1){
                return Direction.Southwest;
            } else if(directional.y == 0){
                return Direction.West;
            } else if(directional.y == 1){
                return Direction.Northwest;
            }
        } else if(directional.x == 0){
            if(directional.y == -1){
                return Direction.South;
            } else if(directional.y == 0){
                return Direction.Center;
            } else if(directional.y == 1){

                return Direction.North;
            }
        } else if(directional.x == 1){
            if(directional.y == -1){
                return Direction.Southeast;
            } else if(directional.y == 0){
                return Direction.East;
            } else if(directional.y == 1){
                return Direction.Northeast;
            }
        }
        return null;
    }

    public Dir getDirFrom(Loc l){
        Loc directional = subtract(l);
        if(directional.x == -1){
            if(directional.y == -1){
                return Dir.SW;
            } else if(directional.y == 0){
                return Dir.W;
            } else if(directional.y == 1){
                return Dir.NW;
            }
        } else if(directional.x == 0){
            if(directional.y == -1){
                return Dir.S;
            } else if(directional.y == 0){
                return Dir.CENTER;
            } else if(directional.y == 1){
                return Dir.N;
            }
        } else if(directional.x == 1){
            if(directional.y == -1){
                return Dir.SE;
            } else if(directional.y == 0){
                return Dir.E;
            } else if(directional.y == 1){
                return Dir.NE;
            }
        }
        return Dir.NONE;
    }
    

    public ArrayList<Robot> getAdjacentRobots(){
        ArrayList<Robot> robots = new ArrayList<>(8);
        for(MainLoc l : adjacentPassableTiles()){
            if(l.containsRobot){
                robots.add(Map.robots[l.x][l.y]);
            }
        }
        return robots;
    }
    public ArrayList<Robot> getAdjacentRobots(Type type){
        ArrayList<Robot> robots = new ArrayList<>(8);
        for(MainLoc l : adjacentPassableTiles()){
            if(l.containsRobot && Map.robots[l.x][l.y].type == type){
                robots.add(Map.robots[l.x][l.y]);
            }
        }
        return robots;
    }


    public ArrayList<Robot> getAdjacentFriendlyRobots(){
        ArrayList<Robot> robots = new ArrayList<>(8);
        for(MainLoc l : adjacentPassableTiles()){
            if(l.containsRobot && Map.robots[l.x][l.y].isMine){
                robots.add(Map.robots[l.x][l.y]);
            }
        }
        return robots;
    }
    public ArrayList<Robot> getAdjacentFriendlyRobots(Type type){
        ArrayList<Robot> robots = new ArrayList<>(8);
        for(MainLoc l : adjacentPassableTiles()){
            if(l.containsRobot && Map.robots[l.x][l.y].isMine && Map.robots[l.x][l.y].type == type){
                robots.add(Map.robots[l.x][l.y]);
            }
        }
        return robots;
    }


    public ArrayList<Robot> getAdjacentEnemyRobots(){
        ArrayList<Robot> robots = new ArrayList<>(8);
        for(MainLoc l : adjacentPassableTiles()){
            if(l.containsRobot && !Map.robots[l.x][l.y].isMine){
                robots.add(Map.robots[l.x][l.y]);
            }
        }
        return robots;
    }

    public ArrayList<Robot> getAdjacentEnemyRobots(Type type){
        ArrayList<Robot> robots = new ArrayList<>(8);
        for(MainLoc l : adjacentPassableTiles()){
            if(l.containsRobot && !Map.robots[l.x][l.y].isMine && Map.robots[l.x][l.y].type == type){
                robots.add(Map.robots[l.x][l.y]);
            }
        }
        return robots;
    }

}
