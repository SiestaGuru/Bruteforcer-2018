package Bruteforcer;

import java.util.ArrayList;

public class MainLoc extends Loc {


    public MainLoc[] adjacent = null;
    public MainLoc[] adjacentIncludingThis = null;
    public MainLoc[] adjacentPassableIncludingThis = null;
    public MainLoc[] adjacentPassable = null;


    public MainLoc[] rangerAtMoveTiles = null;

    public int passableNeighboursCount;

    public int nodeId;

    public int sectionId = -1;

    public boolean isPassable;
    public boolean containsRobot;


    public int pathfindingStepsReq = 0;
    public MainLoc pathfindingFirstStep = null;



    public MainLoc(int x, int y){
        super(x,y);
    }

    public void initAdjacent(){
        adjacent = calcAdjacentTiles();
        adjacentIncludingThis = calcAdjacentIncludingThis();
        adjacentPassable = calcPassableAdjacent();
        adjacentPassableIncludingThis = calcPassableAdjacentIncludingThis();
        passableNeighboursCount = adjacentPassable.length;


        ArrayList<MainLoc> tiles = new Circle(this,Type.RANGER.atPlusMoveSquaredApprox).allContainingPassableLocations();
        rangerAtMoveTiles = tiles.toArray(new MainLoc[tiles.size()]);

    }

    @Override
    public boolean isOnTheMap(){
        return true;
    }

    @Override
    public boolean isPassable(){
        return isPassable;
    }

    @Override
    public boolean containsRobot(){
        return containsRobot;
    }

    private MainLoc[] calcAdjacentTiles(){
        if(x <= 0){
            if(y <= 0){
                return new MainLoc[]{
                        Map.locations[x][y+1],
                        Map.locations[x+1][y],
                        Map.locations[x+1][y+1]
                };
            }else if(y >= Map.height - 1){
                return new MainLoc[]{
                        Map.locations[x][y-1],
                        Map.locations[x+1][y],
                        Map.locations[x+1][y-1]
                };
            } else{
                return new MainLoc[]{
                        Map.locations[x][y+1],
                        Map.locations[x][y-1],
                        Map.locations[x+1][y],
                        Map.locations[x+1][y+1],
                        Map.locations[x+1][y-1]
                };
            }
        }else if(x >= Map.width - 1){
            if(y <= 0){
                return new MainLoc[]{
                        Map.locations[x][y+1],
                        Map.locations[x-1][y],
                        Map.locations[x-1][y+1]
                };
            }else if(y >= Map.height - 1){
                return new MainLoc[]{
                        Map.locations[x][y-1],
                        Map.locations[x-1][y],
                        Map.locations[x-1][y-1]
                };
            } else{
                return new MainLoc[]{
                        Map.locations[x][y+1],
                        Map.locations[x][y-1],
                        Map.locations[x-1][y],
                        Map.locations[x-1][y+1],
                        Map.locations[x-1][y-1]
                };
            }
        } else{
            if(y <= 0){
                return new MainLoc[]{
                        Map.locations[x][y+1],
                        Map.locations[x+1][y],
                        Map.locations[x-1][y],
                        Map.locations[x+1][y+1],
                        Map.locations[x-1][y+1]
                };
            }else if(y >= Map.height - 1){
                return new MainLoc[]{
                        Map.locations[x][y-1],
                        Map.locations[x+1][y],
                        Map.locations[x-1][y],
                        Map.locations[x+1][y-1],
                        Map.locations[x-1][y-1]
                };
            } else{
                return new MainLoc[]{
                        Map.locations[x][y+1],
                        Map.locations[x][y-1],
                        Map.locations[x+1][y],
                        Map.locations[x-1][y],
                        Map.locations[x+1][y+1],
                        Map.locations[x+1][y-1],
                        Map.locations[x-1][y+1],
                        Map.locations[x-1][y-1]
                };
            }
        }
    }
    private MainLoc[] calcAdjacentIncludingThis(){
        if(x <= 0){
            if(y <= 0){
                return new MainLoc[]{
                        this,
                        Map.locations[x][y+1],
                        Map.locations[x+1][y],
                        Map.locations[x+1][y+1],

                };
            }else if(y >= Map.height - 1){
                return new MainLoc[]{
                        this,
                        Map.locations[x][y-1],
                        Map.locations[x+1][y],
                        Map.locations[x+1][y-1]
                };
            } else{
                return new MainLoc[]{
                        this,
                        Map.locations[x][y+1],
                        Map.locations[x][y-1],
                        Map.locations[x+1][y],
                        Map.locations[x+1][y+1],
                        Map.locations[x+1][y-1]
                };
            }
        }else if(x >= Map.width - 1){
            if(y <= 0){
                return new MainLoc[]{
                        this,
                        Map.locations[x][y+1],
                        Map.locations[x-1][y],
                        Map.locations[x-1][y+1]
                };
            }else if(y >= Map.height - 1){
                return new MainLoc[]{
                        this,
                        Map.locations[x][y-1],
                        Map.locations[x-1][y],
                        Map.locations[x-1][y-1]
                };
            } else{
                return new MainLoc[]{
                        this,
                        Map.locations[x][y+1],
                        Map.locations[x][y-1],
                        Map.locations[x-1][y],
                        Map.locations[x-1][y+1],
                        Map.locations[x-1][y-1]
                };
            }
        } else{
            if(y <= 0){
                return new MainLoc[]{
                        this,
                        Map.locations[x][y+1],
                        Map.locations[x+1][y],
                        Map.locations[x-1][y],
                        Map.locations[x+1][y+1],
                        Map.locations[x-1][y+1]
                };
            }else if(y >= Map.height - 1){
                return new MainLoc[]{
                        this,
                        Map.locations[x][y-1],
                        Map.locations[x+1][y],
                        Map.locations[x-1][y],
                        Map.locations[x+1][y-1],
                        Map.locations[x-1][y-1]
                };
            } else{
                return new MainLoc[]{
                        this,
                        Map.locations[x][y+1],
                        Map.locations[x][y-1],
                        Map.locations[x+1][y],
                        Map.locations[x-1][y],
                        Map.locations[x+1][y+1],
                        Map.locations[x+1][y-1],
                        Map.locations[x-1][y+1],
                        Map.locations[x-1][y-1]
                };
            }
        }
    }
    private MainLoc[] calcPassableAdjacentIncludingThis(){
        ArrayList<MainLoc> passables = new ArrayList<>();

        for(MainLoc l : adjacentIncludingThis){
            if(l.isPassable()){
                passables.add(l);
            }
        }
        return passables.toArray(new MainLoc[passables.size()]);
    }
    private MainLoc[] calcPassableAdjacent(){
        ArrayList<MainLoc> passables = new ArrayList<>();

        for(MainLoc l : adjacent){
            if(l.isPassable()){
                passables.add(l);
            }
        }
        return passables.toArray(new MainLoc[passables.size()]);
    }

    @Override
    public MainLoc[] adjacentTiles(){
        return adjacent;
    }
    @Override
    public MainLoc[] adjacentTilesIncludingThis(){
        return adjacentIncludingThis;
    }
    @Override
    public MainLoc[] adjacentPassableTiles(){
        return adjacentPassable;
    }
    @Override
    public MainLoc[] adjacentPassableTilesIncludingThis(){
        return adjacentPassableIncludingThis;
    }




    public int GetInvisibleRangerTiles(){
        int count = 0;
        for(int i = 0 ; i < rangerAtMoveTiles.length; i++){
            if(!Map.inSight[rangerAtMoveTiles[i].x][rangerAtMoveTiles[i].y]){
                count++;
            }
        }
//        Debug.log("Inivisble tiles: " + count);
        return count;
    }


    public static ArrayList<MainLoc> getPassableMainLocs(ArrayList<Loc> locs){
         ArrayList<MainLoc> newlocs = new ArrayList<>(locs.size());
         for(Loc l : locs){
             if(l.isOnTheMap()){
                 MainLoc ml = Map.locations[l.x][l.y];
                 if(ml.isPassable){
                     newlocs.add(ml);
                 }
             }
         }
         return newlocs;
    }


    //Obsolete and incomplete
    private static Loc[] getNewCircleTiles(int radius, Dir direction){
        //TODO: other radii

        if(direction == Dir.CENTER) return new Loc[0];


        switch (radius){
            default:
                return new Loc[0]; //unimportant?
            case 70:  //std ranger sight

                if(direction.diagonal){
                    return new Loc[]{
                            new Loc(9 * direction.x,0),
                            new Loc(9 * direction.x,direction.y),
                            new Loc(9 * direction.x,2 * direction.y),
                            new Loc(9 * direction.x,3 * direction.y),
                            new Loc(8 * direction.x,3 * direction.y),
                            new Loc(8 * direction.x,4 * direction.y),
                            new Loc(8 * direction.x,5 * direction.y),
                            new Loc(7 * direction.x,5 * direction.y),
                            new Loc(7 * direction.x,6 * direction.y),
                            new Loc(6 * direction.x,6 * direction.y),
                            new Loc(6 * direction.x,7 * direction.y),
                            new Loc(5 * direction.x,7 * direction.y),
                            new Loc(5 * direction.x,8 * direction.y),
                            new Loc(4 * direction.x,8 * direction.y),
                            new Loc(3 * direction.x,8 * direction.y),
                            new Loc(3 * direction.x,9 * direction.y),
                            new Loc(2 * direction.x,9 * direction.y),
                            new Loc(direction.x,9 * direction.y),
                            new Loc(0,9 * direction.y)
                    };
                }else if(direction.x != 0){
                    return new Loc[]{
                            new Loc(9 * direction.x, 0),
                            new Loc(9 * direction.x, 1),
                            new Loc(9 * direction.x, 2),
                            new Loc(9 * direction.x, -1),
                            new Loc(9 * direction.x, -2),
                            new Loc(8 * direction.x, -3),
                            new Loc(8 * direction.x, -4),
                            new Loc(8 * direction.x, 3),
                            new Loc(8 * direction.x, 4),
                            new Loc(7 * direction.x, 5),
                            new Loc(7 * direction.x, -5),
                            new Loc(6 * direction.x, 6),
                            new Loc(6 * direction.x, -6),

                            new Loc(5 * direction.x, -7),
                            new Loc(5 * direction.x, 7),
                            new Loc(3 * direction.x, 8),
                            new Loc(3 * direction.x, -8)
                    };
                }else{
                    return new Loc[]{
                            new Loc( 0,9 * direction.y),
                            new Loc( 1,9 * direction.y),
                            new Loc(2,9 * direction.y),
                            new Loc( -1,9 * direction.y),
                            new Loc( -2,9 * direction.y),

                            new Loc( -3,8 * direction.y),
                            new Loc( -4,8 * direction.y),
                            new Loc( 3,8 * direction.y),
                            new Loc( 4,8 * direction.y),

                            new Loc( 5,7 * direction.y),
                            new Loc(-5,7 * direction.y),
                            new Loc( 6,6 * direction.y),
                            new Loc(-6,6 * direction.y),

                            new Loc( -7,5 * direction.y),
                            new Loc( 7,5 * direction.y),
                            new Loc( 8,3 * direction.y),
                            new Loc( -8,3 * direction.y)
                    };
                }
            case 50:
                if(direction.diagonal){
                    return new Loc[]{
                            new Loc(6 * direction.x,0),
                            new Loc(6 * direction.x,direction.y),
                            new Loc(6 * direction.x,2 * direction.y),
                            new Loc(6 * direction.x,3 * direction.y),


                            new Loc(5 * direction.x,3 * direction.y),
                            new Loc(5 * direction.x,4 * direction.y),

                            new Loc(4 * direction.x,4 * direction.y),

                            new Loc(4 * direction.x,5 * direction.y),

                            new Loc(3 * direction.x,5 * direction.y),
                            new Loc(3 * direction.x,6 * direction.y),

                            new Loc(2 * direction.x,6 * direction.y),
                            new Loc(direction.x,6 * direction.y),
                            new Loc(direction.x,6 * direction.y),

                    };
                }else if(direction.x != 0){
                    return new Loc[]{
                            new Loc(6 * direction.x, 0),
                            new Loc(6 * direction.x, 1),
                            new Loc(6 * direction.x, -1),
                            new Loc(6 * direction.x, 2),
                            new Loc(6 * direction.x, -2),
                            new Loc(5 * direction.x, 3),
                            new Loc(5 * direction.x, -3),
                            new Loc(4 * direction.x, 4),
                            new Loc(4 * direction.x, -4),
                            new Loc(3 * direction.x, -5),
                            new Loc(3 * direction.x, 5),
                    };
                }else{
                    return new Loc[]{
                            new Loc(0,6 * direction.y),
                            new Loc(1,6 * direction.y),
                            new Loc(-1,6 * direction.y),
                            new Loc(-2,6 * direction.y),
                            new Loc(2,6 * direction.y),
                            new Loc(3,5 * direction.y),
                            new Loc(-3,5 * direction.y),
                            new Loc(4,4 * direction.y),
                            new Loc(-4,4 * direction.y),
                            new Loc(-5,3 * direction.y),
                            new Loc(5,3 * direction.y),
                    };

                }

        }

    }


}
