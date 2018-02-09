package Bruteforcer;

public class OverchargeRequest {

    public Robot requestFrom;
    public Robot soThatWeCanKill = null;
    public Loc locationHint = null;
    public int shotsReq;

    public OverchargeRequest(Robot requester, Robot forTarget){
        this.requestFrom = requester;
        this.soThatWeCanKill = forTarget;

    }
    public OverchargeRequest(Robot requester, Loc hint){
        this.requestFrom = requester;
        this.locationHint = hint;

    }

    public void updateShotsRequired(){
        if(soThatWeCanKill != null) {
            int damage;

            if (requestFrom.amIMage) {
                damage = Mage.myMageDamage;
            } else {
                damage = requestFrom.damage;
            }

            if (soThatWeCanKill.amIKnight) {
                shotsReq = 1 + ((soThatWeCanKill.health - 1) / (damage - Knight.theirKnightDefense));
            } else {
                shotsReq = 1 + ((soThatWeCanKill.health - 1) / damage);
            }
        }
    }

}
