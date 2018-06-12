package pt.socof.Domain;

import pt.socof.Math.Tuple;

public class NoFlyZone {

    private Tuple topLeft;

    private Tuple topRight;

    private Tuple downLeft;

    private Tuple downRight;

    public NoFlyZone(Tuple topLeft,Tuple topRight,Tuple downLeft,Tuple downRight){
        this.topLeft=topLeft;
        this.topRight=topRight;
        this.downLeft=downLeft;
        this.downRight=downRight;
    }

}
