package Bruteforcer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Debug extends R {

    public enum Modes {NONE, ALL, MOVEMENT, TASKS, MAP}

    public static Modes LOG_MODE = Modes.MAP;





    public static int exceptionsPrinted = 0;
    private static final int TIMECHANNELS = 20;


    public static boolean release = true;

    //public static String identifier;

    public static void init() {
        timeChannel = new long[TIMECHANNELS];
        countChannel = new long[TIMECHANNELS];
        timeChannelSums = new long[TIMECHANNELS];
        //identifier = R.myType.name() + "-" + rc.getID() + ": ";
    }


    public static void log(Object[] arr, Modes mode) {
        if(release)return;
        if (mode.equals(LOG_MODE) || LOG_MODE.equals(Modes.ALL)) {
            String str = "[";
            for (int i = 0; i < arr.length; i++) {
                str += arr[i].toString();
            }

            System.out.println(str + "]");
        }
    }

    public static void log(Object[] arr) {
        if(release)return;
        String str = "[";
        for (int i = 0; i < arr.length; i++) {
            str += arr[i].toString();
        }

        System.out.println(str + "]");
    }

    public static void log(Object o) {
        if(release)return;
        if(o == null) System.out.println("null");else
        System.out.println(o.toString());
    }

    public static void log(Iterable o) {
        if(release)return;

        String str = "[";
        for(Object o2 : o){
            str += o2.toString() + ", ";
        }
        System.out.println(str + "]");
    }

    public static void log(int[] arr, Modes mode) {
        if(release)return;
        if (mode.equals(LOG_MODE) || LOG_MODE.equals(Modes.ALL)) {
            String str = "[";

            for (int i = 0; i < arr.length - 1; i++) {
                str = str + arr[i] + ",  ";
            }
            if (arr.length != 0) {
                str = str + arr[arr.length - 1];
            }
            System.out.println(str + "]");
        }
    }

    public static void log(int[] arr) {
        if(release)return;
        String str = "[";

        for (int i = 0; i < arr.length - 1; i++) {
            str = str + arr[i] + ",  ";
        }
        if (arr.length != 0) {
            str = str + arr[arr.length - 1];
        }
        System.out.println(str + "]");
    }



    public static void log(float[] arr, Modes mode) {
        if(release)return;
        if (mode.equals(LOG_MODE) || LOG_MODE.equals(Modes.ALL)) {
            String str = "[";

            for (int i = 0; i < arr.length - 1; i++) {
                str = str + ((float) (Math.round(arr[i] * 100))) / 100f + ",  ";
            }
            if (arr.length != 0) {
                str = str + ((float) (Math.round(arr[arr.length - 1] * 100))) / 100f;
            }
            System.out.println(str + "]");
        }
    }

    public static void log(float[] arr) {
        if(release)return;
        String str = "[";

        for (int i = 0; i < arr.length - 1; i++) {
            str = str + ((float) (Math.round(arr[i] * 100))) / 100f + ",  ";
        }
        if (arr.length != 0) {
            str = str + ((float) (Math.round(arr[arr.length - 1] * 100))) / 100f;
        }
        System.out.println(str + "]");
    }

    public static void log(double[] arr) {
        if(release)return;
        String str = "[";

        for (int i = 0; i < arr.length - 1; i++) {
            str = str + ((float) (Math.round(arr[i] * 100))) / 100f + ",  ";
        }
        if (arr.length != 0) {
            str = str + ((float) (Math.round(arr[arr.length - 1] * 100))) / 100f;
        }
        System.out.println(str + "]");
    }

    public static void logPrecision(float[] arr) {
        if(release)return;
        String str = "[";

        for (int i = 0; i < arr.length - 1; i++) {
            str = str + arr[i] + ",  ";
        }
        if (arr.length != 0) {
            str = str + arr[arr.length - 1];
        }
        System.out.println(str + "]");
    }



    public static void log(String s, Modes mode) {
        if(release)return;
        if (mode.equals(LOG_MODE) || LOG_MODE.equals(Modes.ALL)) {
            System.out.println(s);
        }
    }

    public static void log(String s) {
        if(release)return;
        System.out.println("T" + R.turn + "  " + s);
    }


    public static void log(List<Object> list){
        if(release)return;
       String s = "[";
        for(Object o : list){
            s+=o.toString() + ", ";
        }

        System.out.println("T" + R.turn + "  " + s + "]");
    }

    public static void lineBreak(){
        System.out.println("");
    }
    public static void seperator(){
        System.out.println("________________________");
    }



    public static void log(Exception e) {
        exceptionsPrinted++;

        if(release || exceptionsPrinted < 10 || exceptionsPrinted % 20 == 0) {
            System.out.println("T" + R.turn + "   Exception ("+exceptionsPrinted+") : ");
            System.out.println(e.toString());
            e.printStackTrace(System.out);

            if (e.getStackTrace().length > 2) {
                System.out.println(e.getStackTrace()[0]);
                System.out.println(e.getStackTrace()[1]);
                System.out.println(e.getStackTrace()[2]);
            } else if (e.getStackTrace().length == 2) {
                System.out.println(e.getStackTrace()[0]);
                System.out.println(e.getStackTrace()[1]);
            } else if (e.getStackTrace().length == 1) {
                System.out.println(e.getStackTrace()[0]);
            }
        }
    }

    public static void logCurrentStracktrace() {
        try {
            throw new Exception("stacktrace exception");
        } catch (Exception e) {
            log(e);
        }
    }


    public static void beginClock(int channel){
        timeChannel[channel] = System.nanoTime();
    }
    public static void endClock(int channel){
        countChannel[channel]++;
        timeChannelSums[channel] += System.nanoTime() - timeChannel[channel];
    }


    public static long[] timeChannel;
    public static long[] timeChannelSums;
    public static long[] countChannel;
    public static void printclocks(){
        Debug.log("Clocks:");
        for(int i = 0; i < TIMECHANNELS; i++){
           if(countChannel[i] > 0){
               double totalTime = (double)timeChannelSums[i];

               double average = totalTime / (double)countChannel[i];

               System.out.println("(" + i + ") Tot: " +  totalTime / 1000000.0 + "ms AVG: " + average / 1000000.0 + "ms Count:" + countChannel[i] );
           }
        }

    }
}
