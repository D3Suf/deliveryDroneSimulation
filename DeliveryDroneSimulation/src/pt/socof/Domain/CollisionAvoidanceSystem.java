package pt.socof.Domain;

import pt.socof.Math.Tuple;

public class CollisionAvoidanceSystem extends Thread {

    private final Drone drone;

    private final ControlTower controlTower;

    public CollisionAvoidanceSystem(ControlTower controlTower, Drone drone) {
        this.drone = drone;
        this.controlTower = controlTower;
    }

    @Override
    public void run() {
        //Avalia a trajetoria do drone comparando-o com os outros e verifica se irá ocorrer uma colisão.
        //Se ocorrer uma colisão, realiza um override à trajetoria ou velocidade.
        while (!isInterrupted()) {
            controlTower.getDronesData(this.drone.getId());
            try {




                this.drone.getRunningSemaphore().acquire();
                if (!this.drone.isInterrupted()) {
                    this.drone.overrideDestiny(new Tuple(0, 0));
                }
                this.drone.getRunningSemaphore().release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
