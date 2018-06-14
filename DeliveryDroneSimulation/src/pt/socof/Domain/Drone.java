package pt.socof.Domain;

import pt.socof.Math.Operations;
import pt.socof.Math.Tuple;
import pt.socof.Utils.ConcurrentQueue;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

public class Drone extends Thread {

    private float fuel;

    private float weight;

    private Float routeAngle;

    private Float velocity;

    private final Tuple baseLocation;

    private Tuple correntLocation;

    private ConcurrentQueue<Location> travelLocations;

    private Semaphore runningSemaphore;

    private final CollisionAvoidanceSystem cad;
    private final FlyZone flyZone;
    private final ControlTower controlTower;

    private static final float MAX_FUEL = 200;
    private static final float DEFAULT_VELOCITY = 10; //Metros por segundo
    private static final float FUEL_USE = 10; //Por Metro e peso - FUEL_USE*weight*DEFAULT_VELOCITY
    private static final int SECONDS_STEP = 2;
    private static final float DRONE_WEIGHT = 10;

    public Drone(ControlTower controlTower, FlyZone flyZone, Tuple baseLocation, ConcurrentQueue<Location> locations) {
        this.flyZone = flyZone;
        this.controlTower = controlTower;
        this.correntLocation = baseLocation;
        this.baseLocation = baseLocation;
        locations.addLast(new Location(baseLocation, 0));
        this.travelLocations = locations;

        List<Location> loc = locations.toList();
        this.weight = 0;
        for (Location l : loc) {
            this.weight += l.getUnloadWeight();
        }
        this.fuel = MAX_FUEL;
        this.cad = new CollisionAvoidanceSystem(this.controlTower, this);
        this.routeAngle = null;
        this.velocity = null;
        this.runningSemaphore = new Semaphore(1);
    }

    @Override
    public void run() {
        this.cad.start();
        try {
            while (!isInterrupted()) {
                this.runningSemaphore.acquire();
                this.controlTower.updateData(this.getId(),this.correntLocation,this.routeAngle,this.velocity);
                AutoPilot autoPilot = new AutoPilot(this);
                autoPilot.start();
                Thread.sleep(SECONDS_STEP * 1000);

                autoPilot.completeSemaphore.acquire();
                if (this.correntLocation == this.baseLocation && this.travelLocations.size() > 1) {
                    this.interrupt();
                    this.cad.interrupt();
                }
                this.runningSemaphore.release();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void overrideDestiny(Tuple maneuveringDestiny) {
        //Adiciona um destino intermedio com o objetivo de realizar uma manobra ou alterar velocidade
    }

    public void overrideVelocity(Tuple maneuveringDestiny) {
        //Adiciona um destino intermedio com o objetivo de realizar uma manobra ou alterar velocidade
    }

    public float getTotalWeight() {
        return this.weight + Drone.DRONE_WEIGHT;
    }

    public Tuple getNextLocation() {
        Location nextLoc = travelLocations.getFirst();
        return nextLoc != null ? travelLocations.getFirst().getLocation() : null;
    }

    public Semaphore getRunningSemaphore() {
        return this.runningSemaphore;
    }

    private class AutoPilot extends Thread {

        private Drone drone;

        private Semaphore completeSemaphore;

        public AutoPilot(Drone drone) {
            this.drone = drone;
            this.completeSemaphore = new Semaphore(0);
        }

        @Override
        public void run() {
            if (this.drone.routeAngle == null)
                calculateRoute();
            calculateNextPosition();
            this.completeSemaphore.release();
        }

        private void calculateRoute() {
            Tuple correntLocation = this.drone.correntLocation;
            Tuple nextLoc = this.drone.getNextLocation();

            List<Tuple> tupleList = this.drone.flyZone.hasClearPath(correntLocation, nextLoc);
            if (tupleList != null) {
                Collections.reverse(tupleList);
                for (Tuple t : tupleList) {
                    this.drone.travelLocations.addFirst(new Location(t, 0));
                }
                nextLoc = this.drone.getNextLocation();
            }

            this.drone.routeAngle = Operations.getRouteAngle(correntLocation, nextLoc);
            if (this.drone.velocity == null)
                this.drone.velocity = Drone.DEFAULT_VELOCITY;
        }

        private void calculateNextPosition() {
            float distance = Operations.getDistanceBetweenTwoPoint(this.drone.correntLocation, this.drone.getNextLocation());
            float maxDistance = this.drone.velocity * Drone.SECONDS_STEP;
            if (distance <= maxDistance) {
                Location location = this.drone.travelLocations.removeFirst();
                this.drone.correntLocation = location.getLocation();
                this.drone.fuel -= this.drone.getTotalWeight() * Drone.FUEL_USE * distance;
                this.drone.weight -= location.getUnloadWeight();
                this.drone.routeAngle = null;
                this.drone.velocity = null;
            } else {
                this.drone.correntLocation = Operations.nextPosition(this.drone.correntLocation, this.drone.velocity, this.drone.routeAngle);
                this.drone.fuel -= this.drone.getTotalWeight() * Drone.FUEL_USE * maxDistance;
            }
        }
    }
}
