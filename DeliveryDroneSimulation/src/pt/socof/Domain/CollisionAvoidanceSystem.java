package pt.socof.Domain;

import pt.socof.Math.Operations;
import pt.socof.Math.Tuple;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class CollisionAvoidanceSystem extends Thread {

    private final Drone drone;

    private final ControlTower controlTower;

    private ExecutorService executor;

    private static final float MANEUVER_VELOCITY = 90; //%

    private static final int COLLISION_PROCESSORS_AWAIT_TIME = 10; //%

    public CollisionAvoidanceSystem(ControlTower controlTower, Drone drone) {
        this.drone = drone;
        this.controlTower = controlTower;
    }

    @Override
    public void run() {
        //Avalia a trajetoria do drone comparando-o com os outros e verifica se irá ocorrer uma colisão.
        //Se ocorrer uma colisão, realiza um override à trajetoria ou velocidade.
        while (!isInterrupted()) {
            try {
                ControlTower.DroneData droneData = controlTower.getDroneData(drone.getId());
                Map<Long, ControlTower.DroneData> dronesInfo = controlTower.getDronesData(this.drone.getId());

                this.executor = Executors.newFixedThreadPool(dronesInfo.size());
                Semaphore onlyOneProcessEnds = new Semaphore(1);
                for (Long id : dronesInfo.keySet()) {
                    ControlTower.DroneData otherDroneData = dronesInfo.get(id);
                    executor.execute(new CollisionProcessor(this, this.drone.getId(), droneData, id, otherDroneData, onlyOneProcessEnds));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void processEminentCollision(long droneId, ControlTower.DroneData droneData, long otherDroneId,
                                        ControlTower.DroneData otherDroneData, Semaphore onlyOneProcessEnds) throws InterruptedException {
        if (!this.drone.isInterrupted()) {
            if (controlTower.maneuveringRequired(droneId, otherDroneId)) {
                this.drone.overrideVelocity(droneData.velocity * (MANEUVER_VELOCITY / 100),
                        droneData.nextLocation);
            }
        }
        this.executor.shutdownNow();
        onlyOneProcessEnds.release();
    }

    private class CollisionProcessor extends Thread {

        private CollisionAvoidanceSystem cad;

        private long droneId;

        private ControlTower.DroneData droneData;

        private long otherDroneId;

        private ControlTower.DroneData otherDroneData;

        private Semaphore onlyOneProcessEnds;

        public CollisionProcessor(CollisionAvoidanceSystem cad, long droneId, ControlTower.DroneData droneData, long otherDroneId,
                                  ControlTower.DroneData otherDroneData, Semaphore onlyOneProcessEnds) {
            this.cad = cad;
            this.droneId = droneId;
            this.droneData = droneData;
            this.otherDroneId = otherDroneId;
            this.otherDroneData = otherDroneData;
            this.onlyOneProcessEnds = onlyOneProcessEnds;
        }

        @Override
        public void run() {
            float time = Operations.timeUntilMeeting(droneData.correntLocation, otherDroneData.correntLocation, droneData.velocity,
                    otherDroneData.velocity, droneData.routeAngle, otherDroneData.routeAngle);
            float timeUntilNextStop = Operations.getDistanceBetweenTwoPoint(droneData.correntLocation,
                    droneData.nextLocation) / droneData.velocity;
            float timeUntilNextStop2 = Operations.getDistanceBetweenTwoPoint(otherDroneData.correntLocation,
                    otherDroneData.nextLocation) / otherDroneData.velocity;

            if (time < timeUntilNextStop && time < timeUntilNextStop2) {
                try {
                    onlyOneProcessEnds.acquire();
                    if (!this.isInterrupted())
                        this.cad.processEminentCollision(droneId, droneData, otherDroneId, otherDroneData,onlyOneProcessEnds);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
