package pt.socof.Domain;

import pt.socof.Math.Tuple;

import java.util.List;

public class FlyZone {

    private List<NoFlyZone> noFlyZoneList;

    public FlyZone(List<NoFlyZone> noFlyZoneList){
        this.noFlyZoneList=noFlyZoneList;
    }

    public boolean isValidLocation(Tuple coordenates){
        //Verifica se as coordenadas recebidas não fazem parte de uma NoFlyZone
        return true;
    }

    public List<Tuple> hasClearPath(Tuple beginingCoordenates,Tuple destinyCoordenates){
        //Verifica se existe alguma NoFlyZone que impessa o voo direto do ponto de inicio até o destino
        //Retorna null se não houver obstacolos, caso contrario retorna um caminho que permite conturna-lo
        return null;
    }

}
