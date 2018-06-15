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
    private static final float FUEL_USE = 10; //Por Metro e peso
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
                if (this.routeAngle == null)
                    calculateRoute();

                this.controlTower.updateData(this.getId(),this.correntLocation,this.routeAngle,this.velocity,this.getNextLocation());
                AutoPilot autoPilot = new AutoPilot(this);
                autoPilot.start();
                Thread.sleep(SECONDS_STEP * 1000);
                autoPilot.completeSemaphore.acquire();

                if (this.correntLocation == this.baseLocation && this.travelLocations.size() > 1) {
                    this.interrupt();
                    this.cad.interrupt();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void calculateRoute() throws InterruptedException {
        Tuple correntLocation = this.correntLocation;
        Tuple nextLoc = this.getNextLocation();

        List<Tuple> tupleList = this.flyZone.hasClearPath(correntLocation, nextLoc);

        this.runningSemaphore.acquire();

        if (tupleList != null) {
            Collections.reverse(tupleList);
            for (Tuple t : tupleList) {
                this.travelLocations.addFirst(new Location(t, 0));
            }
            nextLoc = this.getNextLocation();
        }

        this.routeAngle = Operations.getRouteAngle(correntLocation, nextLoc);
        if (this.velocity == null)
            this.velocity = Drone.DEFAULT_VELOCITY;

        this.runningSemaphore.release();
    }

    public void overrideDestiny(Tuple maneuveringDestiny) {
        //Adiciona um destino intermedio com o objetivo de realizar uma manobra ou alterar velocidade
    }

    public void overrideVelocity(float maneuveringVelocity, Tuple destiny) throws InterruptedException {
        //Adiciona um destino intermedio com o objetivo de realizar uma manobra ou alterar velocidade

        this.runningSemaphore.acquire();
        if(destiny==this.getNextLocation())
            this.velocity=maneuveringVelocity;
        this.runningSemaphore.release();
    }

    public float getTotalWeight() {
        return this.weight + Drone.DRONE_WEIGHT;
    }

    public Tuple getNextLocation() {
        Location nextLoc = travelLocations.getFirst();
        return nextLoc != null ? travelLocations.getFirst().getLocation() : null;
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
            try {
                calculateNextPosition();
                this.completeSemaphore.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void calculateNextPosition() throws InterruptedException {
            float distance = Operations.getDistanceBetweenTwoPoint(this.drone.correntLocation, this.drone.getNextLocation());

            this.drone.runningSemaphore.acquire();
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
            this.drone.runningSemaphore.release();
        }
    }
}
