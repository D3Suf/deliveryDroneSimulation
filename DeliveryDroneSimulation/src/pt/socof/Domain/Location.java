package pt.socof.Domain;

import pt.socof.Math.Tuple;

public class Location {

    private Tuple location;

    private float unloadWeight;

    public Location(Tuple location,float unloadWeight) {
        this.setLocation(location);
        this.setUnloadWeight(unloadWeight);
    }

    public Tuple getLocation() {
        return location;
    }

    public void setLocation(Tuple location) {
        this.location = location;
    }

    public float getUnloadWeight() {
        return unloadWeight;
    }

    public void setUnloadWeight(float unloadWeight) {
        this.unloadWeight = unloadWeight;
    }
}
