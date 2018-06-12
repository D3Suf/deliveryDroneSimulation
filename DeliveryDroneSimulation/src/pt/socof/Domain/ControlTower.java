package pt.socof.Domain;

import pt.socof.Math.Tuple;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ControlTower {

    public class DroneData {
        public Tuple correntLocation;

        public float routeAngle;

        public float velocity;

        public DroneData(Tuple correntLocation, float routeAngle, float velocity){
            this.correntLocation=correntLocation;
            this.routeAngle=routeAngle;
            this.velocity=velocity;
        }
    }

    private Map<Long, DroneData> drones;

    public ControlTower() {
        drones = new HashMap<>();
    }

    public synchronized void updateData(long id, Tuple correntLocation, float routeAngle, float velocity) {
        drones.put(id,new DroneData(correntLocation,routeAngle,velocity));
    }

    public synchronized Iterator<DroneData> getDronesData(long requestid){
        Map<Long, DroneData> dronesAux=new HashMap<>(drones);
        dronesAux.remove(requestid);
        return dronesAux.values().iterator();
    }

}
