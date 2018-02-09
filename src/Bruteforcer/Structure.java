package Bruteforcer;


import bc.Direction;
import bc.VecUnit;
import bc.VecUnitID;

import java.util.ArrayList;
import java.util.HashSet;

public class Structure extends Robot {

    public ArrayList<Robot> garrisonedWithin = new ArrayList<>();

public HashSet<Worker> alreadyComingInToBuild = new HashSet<>();
public HashSet<Worker> alreadyComingInToBuildLast = new HashSet<>();



    public ArrayList<MainLoc> rangerShootSpots = null;
    public ArrayList<MainLoc> mageShootSpots = null;


    public Structure(Type type, int id, boolean isMine){
        super(type,id,isMine);
        amIStructure = true;




    }


    public void initstructure(){
        rangerShootSpots = new ArrayList<>();
        mageShootSpots = new ArrayList<>();
        for(MainLoc l :   (new Circle(loc,Type.RANGER.range)).allContainingLocationsOnTheMap()){
            if(l.isPassable){
                if(!loc.isWithinSquaredDistance(l,37)){
                    rangerShootSpots.add(l);
                }
            }
        }

        for(MainLoc l :   (new Circle(loc,Type.MAGE.range)).allContainingLocationsOnTheMap()){
            if(l.isPassable){
                if(!loc.isWithinSquaredDistance(l,20)){
                    mageShootSpots.add(l);
                }
            }
        }
    }


    public void eject(){



        boolean shouldIUnload = garrisonedWithin.size() > 0;
//        boolean forceEject = garrisonedWithin.size() > 1;
        boolean forceEject = true;


//        Robot removebot = null;
//        outerloop:
//        for (Loc l : adjacentTiles) {
//            if (l.isEmpty()) {
//                Direction d = l.getDirectionFrom(loc);
//                for (Robot r : garrisonedWithin) {
//                    if (Player.gc.canUnload(id, d)) {
//                        Player.gc.unload(id, d);
//
//                        r.updateLocation(l, false, true);
//                        Map.robots[l.x][l.y] = r;
//
//                        removebot = r;
//                        r.inGarrison = false;
//                        r.moveHeat = 10;
//                        break outerloop;
//                    }
//                }
//            }
//        }

        if(amIRocket && R.amIEarth)return;

        VecUnitID unitsInside = Player.gc.unit(id).structureGarrison();

        int pleasemoveoutmates = -100;

        if(unitsInside.size() > 3){
            pleasemoveoutmates = -1000 * (int)unitsInside.size();
        }

//        Debug.log("Contains: ");
        for(int i = 0; i < unitsInside.size(); i++){
            Robot r = Map.robotsById.get(unitsInside.get(i));
            r.M.addSpecialLocation(loc,pleasemoveoutmates);
            r.think();
            r.justUnloaded = true;
            if(r.loc.equals(loc)) break;//cant unload if one doesnt wanna go out
//            Debug.log(r);
        }




//        Robot removebot = null;
//
//        for (Robot r : garrisonedWithin) {
//            Loc l = r.whereShouldIUnloadYou(forceEject);
//            if(l != null){
//                Direction d = l.getDirectionFrom(loc);
//                if (Player.gc.canUnload(id, d)) {
//                    Player.gc.unload(id, d);
//                    r.moveHeat = r.moveCd;
//                    r.updateLocation(l, false, true);
//                    Map.robots[l.x][l.y] = r;
//                    removebot = r;
//                    r.inGarrison = false;
//
//                    Map.botsJustUnloaded.add(r);
////                    break;
//                }
//            }
//        }


//        if (removebot != null) {
//            garrisonedWithin.remove(removebot);
//        }


    }
}
