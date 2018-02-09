package Bruteforcer;

import bc.Direction;

public enum Dir {
    N(false,0,1),
    S(false,0,-1),
    E(false,1,0),
    W(false,-1,0),
    NE(true,1,1),
    NW(true,-1,1),
    SE(true,1,-1),
    SW(true,-1,-1),
    NONE(false,0,0),
    CENTER(false,0,0);

    public boolean diagonal;
    public int x;
    public int y;

    private Dir(boolean diagonal, int x, int y){
        this.diagonal = diagonal;
        this.x = x;
        this.y = y;
    }

    public static Direction toDirection(Dir d){
        switch (d){
            case E:
                return Direction.East;
            case N:
                return Direction.North;
            case S:
                return Direction.South;
            case W:
                return Direction.West;
            case NW:
                return Direction.Northwest;
            case NE:
                return Direction.Northeast;
            case SE:
                return Direction.Southeast;
            case SW:
                return Direction.Southeast;

            default:
                return Direction.Center;

        }

    }


}
