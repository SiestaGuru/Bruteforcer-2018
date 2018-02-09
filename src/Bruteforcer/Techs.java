package Bruteforcer;

import bc.ResearchInfo;
import bc.UnitType;

public class Techs {

    private enum TechType {
        WORKER, KNIGHT, RANGER, MAGE, HEALER, ROCKET;
        public UnitType toUnitType(){
            switch (this){
                case MAGE:
                    return UnitType.Mage;
                case HEALER:
                    return UnitType.Healer;
                case RANGER:
                    return UnitType.Ranger;
                case ROCKET:
                    return UnitType.Rocket;
                case WORKER:
                    return UnitType.Worker;
                case KNIGHT:
                    return UnitType.Knight;
            }
            return null;
        }
    }



    public static int enemyTechKnights = 0;
    public static int enemyTechRangers = 0;
    public static int enemyTechMages = 0;
    public static int enemyTechHealers = 0;

    public static boolean enemyCanOverCharge = false ;




    private static TechType[] SnipelessRanger = new TechType[]{TechType.RANGER,TechType.HEALER, TechType.HEALER,TechType.HEALER, TechType.ROCKET, TechType.RANGER, TechType.ROCKET, TechType.MAGE,TechType.MAGE,TechType.MAGE,TechType.MAGE};
    private static TechType[] RangerSchedule = new TechType[]{TechType.RANGER,TechType.HEALER,TechType.RANGER, TechType.RANGER, TechType.ROCKET, TechType.HEALER, TechType.HEALER, TechType.KNIGHT, TechType.MAGE,TechType.MAGE,TechType.MAGE,TechType.MAGE};
    private static TechType[] RangerFocusOverchargeSchedule = new TechType[]{TechType.RANGER,TechType.HEALER, TechType.HEALER, TechType.HEALER,TechType.RANGER, TechType.ROCKET, TechType.RANGER, TechType.ROCKET, TechType.MAGE,TechType.MAGE,TechType.MAGE,TechType.MAGE};
    private static TechType[] RocketSchedule = new TechType[]{TechType.RANGER,TechType.HEALER, TechType.ROCKET,TechType.RANGER, TechType.RANGER, TechType.HEALER, TechType.HEALER, TechType.MAGE,TechType.MAGE,TechType.MAGE,TechType.MAGE};


    private static TechType[] KnightSchedule = new TechType[]{TechType.KNIGHT,TechType.HEALER, TechType.KNIGHT, TechType.KNIGHT,  TechType.HEALER,  TechType.HEALER, TechType.ROCKET, TechType.RANGER, TechType.RANGER, TechType.RANGER, TechType.MAGE, TechType.MAGE,TechType.MAGE,TechType.MAGE};
    private static TechType[] KnightRushSchedule = new TechType[]{TechType.KNIGHT, TechType.MAGE, TechType.HEALER, TechType.HEALER, TechType.HEALER,  TechType.RANGER,  TechType.ROCKET, TechType.RANGER, TechType.RANGER, TechType.MAGE, TechType.MAGE, TechType.MAGE,TechType.MAGE,TechType.MAGE};
    private static TechType[] MageSchedule = new TechType[]{TechType.RANGER,TechType.MAGE,TechType.MAGE,TechType.MAGE,TechType.MAGE,TechType.HEALER,TechType.HEALER,TechType.ROCKET,TechType.RANGER,TechType.RANGER,TechType.WORKER,TechType.WORKER};
    private static TechType[] RandomSchedule = new TechType[]{TechType.WORKER,TechType.HEALER,TechType.HEALER,TechType.HEALER,TechType.ROCKET,TechType.RANGER,TechType.RANGER,TechType.RANGER,TechType.RANGER,TechType.MAGE,TechType.MAGE,TechType.MAGE};

    private static TechType[] MageRushSchedule = new TechType[]{TechType.MAGE,TechType.MAGE,TechType.MAGE,TechType.MAGE,TechType.HEALER,TechType.HEALER,TechType.ROCKET,TechType.RANGER,TechType.RANGER,TechType.RANGER,TechType.WORKER,TechType.WORKER};

    private static TechType[] KnightMageSchedule = new TechType[]{TechType.KNIGHT,TechType.KNIGHT,TechType.KNIGHT,TechType.MAGE,TechType.MAGE,TechType.MAGE,TechType.MAGE,TechType.RANGER,TechType.RANGER,TechType.RANGER,TechType.RANGER,TechType.RANGER};
    private static TechType[] KArboniteRangerSchedule = new TechType[]{TechType.WORKER, TechType.RANGER,TechType.HEALER,TechType.RANGER, TechType.RANGER, TechType.ROCKET, TechType.HEALER, TechType.HEALER, TechType.KNIGHT, TechType.MAGE,TechType.MAGE,TechType.MAGE,TechType.MAGE};

//    private static TechType[] AllRushSchedule = new TechType[]{TechType.KNIGHT,TechType.MAGE,TechType.RANGER,TechType.KNIGHT,TechType.KNIGHT,TechType.HEALER, TechType.HEALER,TechType.HEALER,TechType.MAGE,TechType.MAGE,TechType.MAGE, TechType.RANGER,TechType.RANGER};
    private static TechType[] AllRushSchedule = new TechType[]{TechType.KNIGHT,TechType.MAGE,TechType.RANGER,TechType.HEALER, TechType.RANGER, TechType.HEALER,TechType.HEALER,TechType.ROCKET,TechType.MAGE,TechType.MAGE,TechType.MAGE, TechType.RANGER,TechType.KNIGHT,TechType.KNIGHT};


    private static TechType[] rangerOvercharge = new TechType[]{TechType.RANGER,TechType.HEALER, TechType.HEALER, TechType.HEALER,TechType.MAGE, TechType.ROCKET, TechType.MAGE, TechType.MAGE, TechType.MAGE,TechType.RANGER,TechType.RANGER,TechType.KNIGHT,TechType.KNIGHT,TechType.KNIGHT};

    private static TechType[] rushOverCharge = new TechType[]{TechType.HEALER, TechType.HEALER, TechType.HEALER,TechType.MAGE,TechType.MAGE,  TechType.MAGE, TechType.MAGE, TechType.ROCKET,TechType.RANGER,TechType.RANGER,TechType.RANGER,TechType.KNIGHT,TechType.KNIGHT,TechType.KNIGHT};




//    The Worker’s Tree
//    25:  Gimme some of that Black Stuff: Workers may harvest an additional 1 Karbonite from a deposit at a time.
//    75:  Time is of the Essence: Workers add 1 more health when repairing or constructing a building.
//    75:  Time is of the Essence II: Workers add another 1 more health (2 more total) when repairing or constructing a building.
//    75:  Time is of the Essence III: Workers add another 3 more health (5 more total) when repairing or constructing a building.
//
//    The Knight’s Tree
//    Description
//    25: Armor: Decreases the strength of an attack on a Knight by an additional 5HP.
//    75: Even More Armor: Decreases the strength of an attack on a Knight by another 5HP (10HP more total).
//    150: Javelin: Unlocks Javelin for Knights.
//
//
//    The Ranger’s Tree
//    25:  Get in Fast: Decreases a Ranger’s movement cooldown by 5.
//    100: Scopes: Increases a Ranger’s vision range by 30.
//    200: Snipe: Unlocks Snipe for Rangers.
//
//
//    The Mage’s Tree
//    25:  Glass Cannon: Increases standard attack damage by 15HP.
//    75: Glass Cannon II: Increases standard attack damage by another 15HP (30HP more total).
//    100: Glass Cannon III: Increases standard attack damage by another 15HP (45HP more total).
//    200: Blink: Unlocks Blink for Mages.
//
//
//    The Healer’s Tree
//    25: Spirit Water: Increases Healer’s healing ability by 2HP.
//    100: Spirit Water II: Increases Healer’s healing ability by an another 5HP (7HP more total).
//    200: Overcharge: Unlocks Overcharge for Healers.
//
//    The Rocket’s Tree
//    100: Rocketry: Unlocks rocket technology. Workers can now blueprint and build rockets.
//    100: Rocket Boosters: Reduces rocket travel time by 20 rounds compared to the travel time determined by the orbit of the planets.
//    100: Increased Capacity: Allows rockets to garrison 4 more units per rocket.




    static  boolean queued = false;


    public static int techWorker = 0;
    public static int techMage = 0;
    public static int techHealer = 0;
    public static int techRanger = 0;
    public static int techRocket = 0;
    public static int techKnight = 0;


    public static boolean canSnipe = false;
    public static boolean canJavelin = false;
    public static boolean canBlink = false;
    public static boolean canBuildRocket = false;
    public static boolean canOverload = false;


    public static boolean enemyIsGoingForOvercharge = false;

    public static void DoThinking(){
        if(!queued) {

            if(R.amIEarth) {
                TechType[] queue;
                switch (GrandStrategy.strategy) {
                    case ADAPTIVERANGER:
//                        if(!R.player1) {
//                            queue = RangerSchedule;
//                        }else{
                           // queue = RangerFocusOverchargeSchedule;
//                        if(R.player1) {
//                            queue = rangerOvercharge;
//                        }else{
                            queue = rushOverCharge;
//                        }
//                            queue = KArboniteRangerSchedule;
//                        }
                        break;
                    case STDRANGER:
//                            if(R.player1) {
                            queue = RangerSchedule;
//                        }else{
//                            queue = RangerFocusOverchargeSchedule;
//                        }
//                    queue = SnipelessRanger;
                        break;

                    case LATEMAGE:
                        queue = MageSchedule;
                        break;
                    case EARLYROCKETS:
                        queue = RocketSchedule;
                        break;
                    case KNIGHTRUSH:
//                        queue = KnightSchedule;

                        queue = AllRushSchedule;

                        break;
                    case MAGERUSH:
                        queue = MageRushSchedule;
                        break;
                    case RANDOM:
                        queue = KnightMageSchedule;
                        break;
                    default:
                        queue = KnightSchedule;
                        break;
                }

//            for(TechType t : RangerSchedule) {

                for (TechType t : queue) {
                    Player.gc.queueResearch(t.toUnitType());
                }
            }
            queued = true;

        }

        ResearchInfo info = Player.gc.researchInfo();

        techWorker = (int)info.getLevel(TechType.WORKER.toUnitType());
        techMage = (int)info.getLevel(TechType.MAGE.toUnitType());
        techHealer = (int)info.getLevel(TechType.HEALER.toUnitType());
        techRanger = (int)info.getLevel(TechType.RANGER.toUnitType());
        techRocket = (int)info.getLevel(TechType.ROCKET.toUnitType());
        techKnight = (int)info.getLevel(TechType.KNIGHT.toUnitType());




//        Debug.log("Worker:" + techWorker + " Mage: " + techMage + " Healer: " + techHealer + " ranger: " + techRanger + " rocket: " + techRocket + " knight: " + techKnight);


        if(techRanger >= 3){
            if(!canSnipe){
                R.nextSnipeVolley = R.turn;
                canSnipe = true;
            }
        }


        if(techKnight == 1){
            Knight.myKnightDefense = 10;
        } else if(techKnight == 2){
            Knight.myKnightDefense = 15;
        }
        else if(techKnight >= 3){
            Knight.myKnightDefense = 15;
            canJavelin = true;
        }


        if(techMage == 1){
            Mage.myMageDamage = Type.MAGE.damage + 15;
        } else if(techMage == 2){
            Mage.myMageDamage = Type.MAGE.damage + 30;
        } else if(techMage == 3){
            Mage.myMageDamage = Type.MAGE.damage + 45;
        }
        else if(techMage >= 4){
            Mage.myMageDamage = Type.MAGE.damage + 45;
            canBlink = true;
        }


        if(techRocket >= 1){
            canBuildRocket = true;
        }
        if(techHealer >= 3){
            canOverload = true;
        }


//        if(R.turn % 10 == 0) {
//            Debug.log("Enemy techs,  Ranger: " + enemyTechRangers + " Mage: " + enemyTechMages + " Knight " + enemyTechKnights + " Healer " + enemyTechHealers);
//        }
    }


}
