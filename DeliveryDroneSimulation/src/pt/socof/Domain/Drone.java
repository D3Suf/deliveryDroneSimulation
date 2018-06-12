package pt.socof.Domain;

import pt.socof.Math.Tuple;
import pt.socof.Utils.ConcurrentQueue;

import java.util.List;

public class Drone extends Thread {

    private float fuel;

    private float weight;

    private ConcurrentQueue<Location> travelLocations;

    private Tuple baseLocation;

    private Tuple correntLocation;

    private float routeAngle;

    private float velocity;

    private final CollisionAvoidanceSystem cad;
    private final FlyZone flyZone;
    private final ControlTower controlTower;

    private static final float MAX_FUEL=200;
    private static final float DEFAULT_VELOCITY=10; //Metros por segundo
    private static final float FUEL_USE=10; //Por Metro e peso - FUEL_USE*weight*DEFAULT_VELOCITY

    public Drone(ControlTower controlTower,FlyZone flyZone,Tuple baseLocation,ConcurrentQueue<Location> locations){
        this.flyZone=flyZone;
        this.controlTower=controlTower;
        this.correntLocation=baseLocation;
        this.baseLocation=baseLocation;
        this.travelLocations=locations;

        List<Location> loc=locations.toList();
        this.weight=0;
        for (Location l: loc) {
            this.weight+=l.getUnloadWeight();
        }
        this.fuel=MAX_FUEL;
        this.cad=new CollisionAvoidanceSystem(this.controlTower,this);
    }

    @Override
    public void run(){
        this.cad.start();
        while (!isInterrupted()){
            autoPilot();
        }
    }

    public void autoPilot(){
        //Identificar proximo destino
        //Calcular a proxima localização segundo a velocidade e proxima localização
        //Definir nova localização e stado de combustivel

        //Se chegou a destino, atualizar carda e resetar predefinições de voo
    }

    public void overrideDestiny(Tuple maneuveringDestiny){
        //Adiciona um destino intermedio com o objetivo de realizar uma manobra ou alterar velocidade
    }

    public void overrideVelocity(Tuple maneuveringDestiny){
        //Adiciona um destino intermedio com o objetivo de realizar uma manobra ou alterar velocidade
    }

    public Tuple getNextLocation() {
        return travelLocations.getFirst().getLocation();
    }
}
