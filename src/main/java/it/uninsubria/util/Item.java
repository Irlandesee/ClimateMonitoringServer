package it.uninsubria.util;

public class Item {

    private String itemID;
    private int val;

    public Item(){
        this.itemID = IDGenerator.generateID();
    }

    public Item(String id){
        this.itemID = id;
    }

    public Item(String id, int val){
        this.itemID = id;
        this.val = val;
    }

    public String getID(){return this.itemID;}
    public synchronized int getVal(){
        return this.val;
    }

    public synchronized void setVal(int val){
        this.val = val;
    }

    public String toString(){
        return this.itemID + ":" + this.val;
    }

}
