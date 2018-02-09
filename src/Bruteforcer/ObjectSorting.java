package Bruteforcer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

//Bit of a cheapish and easy to use hack class to do sorting of a list based on a value
//Don't use this with threads or anything, because it uses a single static list for items
//It also always assumes one of the sort methods is called, otherwise the list doesn't reset
public class ObjectSorting {
    public Object internalobj;
    public double score;

    public ObjectSorting(Object o, double score){
        internalobj = o;
        this.score = score;
    }



    private static ArrayList<ObjectSorting> list = new ArrayList<>();


    public static void AddToSort(Object r, double score){
        list.add(new ObjectSorting(r,score));
    }
    public static ArrayList<ObjectSorting> SortAsc(){
        list.sort(new CompAsc());
        ArrayList<ObjectSorting> returnVal = new ArrayList<>();
        returnVal.addAll(list);
        list.clear();
        return returnVal;
    }
    public static ArrayList<ObjectSorting> SortDesc(){
        list.sort(new CompDesc());
        ArrayList<ObjectSorting> returnVal = new ArrayList<>();
        returnVal.addAll(list);
        list.clear();
        return returnVal;
    }

    public static ArrayList<Object> SortDescObjects(){
        list.sort(new CompDesc());
        ArrayList<Object> returnVal = new ArrayList<>();
        for(ObjectSorting o : list){
            returnVal.add(o.internalobj);
        }
        list.clear();
        return returnVal;
    }

    public static ArrayList<Object> SortAscObjects(){
        list.sort(new CompAsc());
        ArrayList<Object> returnVal = new ArrayList<>();
        for(ObjectSorting o : list){
            returnVal.add(o.internalobj);
        }
        list.clear();
        return returnVal;
    }
    private static class CompAsc implements Comparator<ObjectSorting>{
        public int compare(ObjectSorting o1, ObjectSorting o2 ) {
            return Double.compare(o1.score,o2.score);
        }
    }
    private static class CompDesc implements Comparator<ObjectSorting>{
        public int compare(ObjectSorting o1, ObjectSorting o2 ) {
            return  Double.compare(o2.score,o1.score);
        }
    }
}


