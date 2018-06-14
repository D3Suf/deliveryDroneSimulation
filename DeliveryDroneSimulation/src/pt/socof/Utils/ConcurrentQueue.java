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

    public synchronized E removeFirst(){
        return list.removeFirst();
    }

    public synchronized void addLast(E element){
        list.addLast(element);
    }

    public synchronized E removeLast(){
        return list.removeLast();
    }

    public synchronized boolean isEmpty() {
        return list.isEmpty();
    }

    public synchronized int size() {
        return list.size();
    }
}
