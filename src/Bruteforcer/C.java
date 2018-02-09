package Bruteforcer;

import java.util.ArrayList;

public class C {


    public static double Clamp(double val, double min, double max){
        return Math.min(Math.max(val,min),max);
    }

    public static int Clamp(int val, int min, int max){
        return Math.min(Math.max(val,min),max);
    }



    public static ArrayList<Robot>  expandToIncludeGarrisoned(ArrayList<Robot> list){
        ArrayList<Robot> newlist = new ArrayList<>();

        for(Robot r : list){
            newlist.add(r);
            if(r.amIStructure){
                newlist.addAll(((Structure)r).garrisonedWithin);
            }

        }
        return newlist;
    }


}
