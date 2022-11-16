package classes;

import java.util.LinkedList;
import java.util.Deque;

public class Process implements Comparable<Process> {
    int processID;
    String currentEvent;
    int eventTime;
    int arrival;
    int service;
    int start = -1;
    int finish;
    int turnaround;
    int responses = 0;
    Entry entry;
    double responseTime;
    int wait;
    Deque<String> commands = new LinkedList<>();

    public Process(int processID, int arrival) {
        this.arrival = arrival;
        this.processID = processID;
        entry = new Entry(processID, -1);
    }

    public void setService(int service) {
        this.service = service;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public void setFinish(int finish) {
        this.finish = finish;
    }

    public void setTurnaround(int turnaround) {
        this.turnaround = turnaround;
    }

    public void setEvent(String event) {
        this.currentEvent = event;
    }

    public void setWait(int wait){
        this.wait = wait;
    }

    public void updateResponseTime(int responseTime) {
        if (responses == 0) {
            this.responseTime = responseTime;
        } else {
            this.responseTime = ((this.responseTime * responses) + responseTime) / (responses+1);
        }
        responses++;
    }

    public void updateQueue(String command) {
        this.commands.add(command);
    }

    public void setEventTime(int time) {
        this.eventTime = time;
    }

    public void frontAdd(int duration){
        commands.addFirst(Integer.toString(duration));
    }

    public void setNextTime(){
        entry.setValue(Integer.parseInt(commands.peek()));
        entry.setStored(Integer.parseInt(commands.peek()));
    }

    public void predictNextTime(int actualTime, double alpha)
    {
        entry.setValue((alpha)*actualTime + ((1-alpha) * entry.getStored()));
        entry.setStored((alpha)*actualTime + ((1-alpha) * entry.getStored()));
    }

    public void factorHRRN(int waitTime){
        entry.setValue((entry.getStored()+waitTime)/entry.getStored());
    }

    public void loadValue(){
        entry.loadValue();
    }

    public boolean isLast() {
        return (commands.size() == 1);
    }

    public int getProcessID() {
        return processID;
    }

    public int getArrival() {
        return arrival;
    }

    public int getService() {
        return service;
    }

    public int getStart() {
        return start;
    }

    public int getFinish() {
        return finish;
    }

    public int getTurnaround() {
        return turnaround;
    }

    public double getResponseTime() {
        return responseTime;
    }

    public String getEvent() {
        return currentEvent;
    }

    public int getWait() {
        return wait;
    }

    public Deque<String> getQueue() {
        return getQueue();
    }

    public String popQueue() {
        return commands.remove();
    }

    public boolean isEmpty() {
        return commands.isEmpty();
    }

    public int getEventTime() {
        return eventTime;
    }

    public int getNextTime(){
        return (Integer.parseInt(commands.peek()));
    }

    public Entry getEntry(){
        return entry;
    }

    @Override
    public int compareTo(Process other) {
        return (Integer.valueOf(this.getEventTime()).compareTo(Integer.valueOf(other.getEventTime())));
    }

}