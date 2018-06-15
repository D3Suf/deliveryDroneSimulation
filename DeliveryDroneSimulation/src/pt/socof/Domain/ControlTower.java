package pt.socof.Domain;

import pt.socof.Math.Tuple;

import java.util.HashMap;
import java.util.Map;

public class ControlTower {

    public class DroneData {

        public Tuple correntLocation;

        public float routeAngle;

        public float velocity;

        public Tuple nextLocation;

        public boolean maneuvering;

        public DroneData(Tuple correntLocation, float routeAngle, float velocity, Tuple nextLocation) {
            this.correntLocation = correntLocation;
            this.routeAngle = routeAngle;
            this.velocity = velocity;
            this.nextLocation = nextLocation;
            this.maneuvering = false;
        }
    }

    private Map<Long, DroneData> drones;

    public ControlTower() {
        drones = new HashMap<>();
    }

    public synchronized DroneData getDroneData(long id) throws InterruptedException {
        while (!drones.containsKey(id))
            wait();
        return drones.get(id);
    }

    public synchronized void updateData(long id, Tuple correntLocation, float routeAngle, float velocity, Tuple nextLocation) {
        drones.put(id, new DroneData(correntLocation, routeAngle, velocity, nextLocation));
        notifyAll();
    }

    public synchronized boolean maneuveringRequired(long droneId, long avoidColisionWithDroneId) {
        DroneData otherDrone = drones.get(avoidColisionWithDroneId);
        DroneData drone = drones.get(droneId);
        if (!otherDrone.maneuvering && !drone.maneuvering) {
            drone.maneuvering = true;
            drones.put(droneId, drone);
            return true;
        }
        return false;
    }

    public synchronized Map<Long, DroneData> getDronesData(long requestid) {
        Map<Long, DroneData> dronesAux = new HashMap<>(drones);
        dronesAux.remove(requestid);
        return dronesAux;
    }

}
