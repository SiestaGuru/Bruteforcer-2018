package Bruteforcer;

public class Fast {
    //Methods optimized for speed in terms of execution time, not bytecode


    public static boolean isWithinDistance(int x1, int y1, int x2, int y2, double distance){
        //x1 and y1 become deltax and deltay (prevents needing new variable)
        x1 -= x2;
        y1 -= y2;
        return x1 * x1 + y1 * y1 < distance * distance;
    }

    public static boolean isWithinSquaredDistance(int x1, int y1, int x2, int y2, int squaredDistance){
        //x1 and y1 become deltax and deltay (prevents needing new variable)
        x1 -= x2;
        y1 -= y2;
        return x1 * x1 + y1 * y1 < squaredDistance;
    }

    public static double squaredDistanceTo(int x1, int y1, int x2, int y2){
        x1 -= x2;
        y1 -= y2;
        return x1 * x1 + y1 * y1;
    }

    public static double distanceTo(int x1, int y1, int x2, int y2){
        x1 -= x2;
        y1 -= y2;
        return LookupSqrt.sqrt[x1 * x1 + y1 * y1];
    }

    public static double distanceTo(double x1, double y1, double x2, double y2){
        x1 -= x2;
        y1 -= y2;
        return Math.sqrt(x1 * x1 + y1 * y1);
    }


    //Can actually be a faster way of checking  x >= bound1 && x <= bound2  if the bounds can be reused.
    // double center = (bound1 + bound2) / 2;
    // double tolerance = center-bound1;
    // if(Float.intBitsToFloat((Float.floatToIntBits(x-center) & 0b01111111111111111111111111111111)) <= tolerance)
    public static  boolean withinTolerance(double f1, double f2, double tolerance){
        return Float.intBitsToFloat((Float.floatToIntBits((float)(f1-f2)) & 0b01111111111111111111111111111111)) <= tolerance;
    }
    public static  boolean withinTolerance(float f1, float f2, float tolerance){
        return Float.intBitsToFloat((Float.floatToIntBits((f1-f2)) & 0b01111111111111111111111111111111)) <= tolerance;
    }


    //About 60% of the speed of the regular version, but waaay worse precision
    //These are based on the original fast inveserse sqrt used in Quake Arena
    public static double invSqrtFastest(double x) {
        return Float.intBitsToFloat(0x5f3759df - (Float.floatToIntBits((float)x) >> 1)); //it's some kind of magic
    }
    //Only slightly faster than the regular version, at worse precision
    public static double invSqrtFaster(double x) {
        double xhalf = 0.5f * x;
        x = Float.intBitsToFloat(0x5f3759df - (Float.floatToIntBits((float)x) >> 1)); //it's some kind of magic
        return x * (1.5f - xhalf * x * x);
    }
    //Only slightly faster than the default. Good precision for a lot of common values, but bad precision for others
    public static double invSqrtFast(double x) {
        if(x >= 100f){
            return 1f/ Math.sqrt(x);
        }else{
            return LookupSqrtDoubles.invSqrt[(int)(x*36f + 0.5f)];
        }
    }
    public static double invSqrt(double x){
        //Just inline this
        return 1.0 / Math.sqrt(x);
    }



    //Somewhat faster than the standard version, way worse precision
    public static double sqrtFastest(double x) {
        return (Float.intBitsToFloat(0x1fbd1df5 + (Float.floatToIntBits((float)x) >> 1))); //it's some kind of magic
    }
    //Terrible precision for some variables, perfect for a lot of neat variables like integers
    public static double sqrtFast(double x) {
        if(x >= 100f){
            return Math.sqrt(x);
        }else{
            return LookupSqrtDoubles.sqrt[(int)(x*36f + 0.5f)];
        }
    }



    public static double sqrt(int x){
        return LookupSqrt.sqrt[x];
    }




    public static boolean contains(double[] ar, double value){
        for(int i = ar.length -1; i>=0; i--){
            if(ar[i] == value) return true;
        }
        return false;
    }
    public static boolean containedInSortedArray(double[] ar, double value){
        //Inlined from Arrays.binarysearch without the NaN stuff
        int low = 0;
        int high = ar.length - 1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            double midVal = ar[mid];
            if (midVal < value)
                low = mid + 1;
            else if (midVal > value)
                high = mid - 1;
            else {
                return true;
            }
        }
        return false;
    }





    public static final double PI = Math.PI;
    public static final double PIOVER4 = PI / 4f;

    public static final double PITHREEQUARTERS = 3f * PIOVER4;
    public static final double TAU = PI * 2f;
    public static final double HALFPI = PI / 2f;
    private static final double HALFPI_NEG = -HALFPI;

    public static final double TWOTHIRDS = 2f/3f;
    public static final double ONETHIRD = 1f/3f;





}
