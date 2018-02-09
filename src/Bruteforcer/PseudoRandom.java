package Bruteforcer;

public class PseudoRandom {


    public static int seed = 43124;
    public static long longseed = 43593;
    public static int timescalled = 0;



    public static int next(int bound){
        //I don't even know why I made this.. the normal random is probably better..
        if (bound <=0)return  0;

        if(seed < 100000) seed *= 211;

        seed += R.karbonite *499 + Map.totalUnitCount * 157 + R.turn * 919 + 823 * bound  + R.karbonite * Map.totalUnitCount + (timescalled++);

        switch (seed % 7){
            case 0:
                seed /= 3;
                break;
            case 1:
                seed /= 7;
                break;
            case 2:
                break;
            case 3:
                seed /= 2;
                break;
            case 4:
                seed /= 5;
                break;
            case 5:
                seed /= 4;
                break;
            case 6:
                seed /= 9;
                break;
        }
        return seed % bound;
    }


    public static double nextDouble(){
        longseed = (longseed * 0x5DEECE66DL + 0xBL + R.karbonite) & ((1L << 48) - 1);
        return Math.abs(longseed * 0.0000001) % 1.0;
    }

    public static boolean nextBool(){

        if(seed < 100000) seed *= 211;

        seed += R.karbonite *499 + Map.totalUnitCount * 157 + R.turn * 919 + 433024223 + R.karbonite * Map.totalUnitCount;

        switch (seed % 7){
            case 0:
                seed /= 3;
                break;
            case 1:
                seed /= 7;
                break;
            case 2:
                break;
            case 3:
                seed /= 2;
                break;
            case 4:
                seed /= 5;
                break;
            case 5:
                seed /= 4;
                break;
            case 6:
                seed /= 9;
                break;
        }
        return seed % 2 == 0;
    }
}
