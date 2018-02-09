package Bruteforcer;

import bc.UnitType;

public enum Type {
    WORKER(50,100,0,0,50,20,0,500,2,0,"Worker",0),
    KNIGHT(40,250,60,2,50,15,20,100,10,1,"Knight",5),
    RANGER(40,200,30,50,70,30,20,0,0,2,"Ranger",72),
    MAGE(40,80,60,30,30,20,20,250,5,3,"Mage",45),
    HEALER(40,100,-10,30,50,25,10,100,30,4,"Healer",45),
    FACTORY(200,300,0,0,2,0,0,0,0,5,"Factory",0),
    ROCKET(150,200,0,0,2,0,0,0,0,6,"Rocket",0),
    NONE(-1,-1,-1,-1,-1,-1,-1,-1,-1,7,"NONETYPE",0);


    public static int MageBlinkAttackRange = 58;
    public static int MageBlinkMoveAttackRange = 81;


    public final int cost;
    public final int maxHealth;
    public final int damage;
    public final int range;
    public final int sight;
    public final int moveCd;
    public final int attackCd;
    public final int activeCd;
    public final int activeRange;
    public final int typeId;
    public final String string;

    public final int atPlusMoveSquaredApprox; //not exact for all ranges

    private Type(int cost, int maxHealth,int damage,int range,int sight,int moveCd,int attackCd,int activeCd,int activeRange,int typeId, String s, int atMove) {
        this.cost = cost;
        this.maxHealth = maxHealth;
        this.damage = damage;
        this.range = range;
        this.sight = sight;
        this.moveCd = moveCd;
        this.attackCd = attackCd;
        this.activeCd = activeCd;
        this.activeRange = activeRange;
        this.typeId = typeId;
        this.string = s;
        this.atPlusMoveSquaredApprox = atMove;
    }

    @Override
    public String toString() {
        return string;
    }

    public static Type fromId(int id) {
        switch (id) {
            case 0:
                return WORKER;
            case 1:
                return KNIGHT;
            case 2:
                return RANGER;
            case 3:
                return MAGE;
            case 4:
                return HEALER;
            case 5:
                return FACTORY;
            case 6:
                return ROCKET;
            default:
                return NONE;
        }

    }

    public UnitType toUnitType(){
       switch (this){
           case HEALER:
               return UnitType.Healer;
           case WORKER:
               return UnitType.Worker;
           case KNIGHT:
               return UnitType.Knight;
           case RANGER:
               return UnitType.Ranger;
           case MAGE:
               return UnitType.Mage;
           case ROCKET:
               return UnitType.Rocket;
           case FACTORY:
               return UnitType.Factory;
           default:
               return null;
       }
    }
}