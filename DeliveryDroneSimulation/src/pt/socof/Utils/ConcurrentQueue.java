package pt.socof.Utils;

import java.util.LinkedList;
import java.util.List;

public class ConcurrentQueue<E> {

    private LinkedList<E> list;

    public ConcurrentQueue(){
        list=new LinkedList<>();
    }

    public synchronized E getFirst(){
        return list.getFirst();
    }

    public synchronized List<E> toList(){
        return list;
    }

    public synchronized void addFirst(E element){
        list.addFirst(element);
    }

    public synchronized void removeFirst(){
        list.removeFirst();
    }

    public synchronized void addLast(E element){
        list.addLast(element);
    }

    public synchronized void removeLast(){
        list.removeLast();
    }
}
