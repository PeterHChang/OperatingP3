package classes;

public class Entry implements Comparable<Entry> {
    private int key;
    private double value = 0;
    private double stored = 0;

    public Entry(int key, double value) {
        this.key = key;
        this.value = value;
    }

    public int getKey(){
        return key;
    }

    public double getValue(){
        return value;
    }

    public double getStored(){
        return value;
    }

    public void setStored(double value){
        this.stored = value;
    }

    public void loadValue(){
        value = stored;
    }

    public void setValue(double value){
        this.value = value;
    }
 
    @Override
    public int compareTo(Entry other) {
        return (Double.valueOf(this.getValue()).compareTo(Double.valueOf(other.getValue())));
    }
}
