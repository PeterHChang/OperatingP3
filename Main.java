import java.io.*;
import java.util.LinkedList;
import java.util.Queue;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Collections;
import java.util.HashMap;
import classes.Process;
import classes.Entry;

public class Main {

    public static void main(String[] args) 
    {
        try {

            //read in the files 

            Scanner in = new Scanner(System.in);
            System.out.println("Input process name (include '.pf'): ");
            String pfFile = in.nextLine();

            System.out.println("Input scheduling name (include '.sf'): ");
            String scheduleType = in.nextLine();

            Scanner fromSched;

            //initiate the files for type schedule  
            File type = new File(scheduleType);
            fromSched = new Scanner(type);

            //read in all lines
            if (fromSched.hasNext()) 
            {
                String schedulingAlg = fromSched.nextLine();

                //switch command to figure out what type of algorithm this is.

                switch (schedulingAlg) {

                    //done
                    case "FCFS":
                        FCFS(pfFile);
                        break;

                    //if it is round robin, store the quantum. done
                    case "RR": 
                    {
                        String quantumString = fromSched.nextLine();
                        quantumString = quantumString.replaceAll("[^0-9]", "");

                        int quantum = Integer.valueOf(quantumString);
                        RR(pfFile, quantum);
                    }
                        break;

                    case "SPN": 
                    {
                        boolean serviceGiven;
                        String readString = fromSched.nextLine().substring(14);
                        if (readString.equals("true")) {
                            serviceGiven = true;
                        } else {
                            serviceGiven = false;
                        }
                        readString = fromSched.nextLine().substring(6);
                        double alpha = Double.parseDouble(readString);
                        SPN(pfFile, serviceGiven, alpha);
                    }
                        break;

                    // add alpha

                    case "HRRN": 
                    {
                        boolean serviceGiven;
                        String readString = fromSched.nextLine().substring(14);
                        if (readString.equals("true")) 
                        {
                            serviceGiven = true;
                        } 
                        else 
                        {
                            serviceGiven = false;
                        }
                        readString = fromSched.nextLine().substring(6);
                        double alpha = Double.parseDouble(readString);
                        HRRN(pfFile, serviceGiven, alpha);
                    }
                        break;

                    //not done

                    case "FEEDBACK": 
                    {
                        System.out.println("FEEDBACK");
                        String quantumString = fromSched.nextLine();
                        quantumString = quantumString.replaceAll("[^0-9]", "");
                        int quantum = Integer.valueOf(quantumString);

                        System.out.println(quantum);
                        quantumString = fromSched.nextLine();
                        quantumString = quantumString.replaceAll("[^0-9]", "");
                        int numPriorites = Integer.valueOf(quantumString);

                        System.out.println(numPriorites);
                        break;
                    }
                }
            }
        } 
        catch (FileNotFoundException e) 
        {
            System.out.println("Unable to find named file");
        }

    }

    public static void FCFS(String pfFile) {
        System.out.println("FCFS");
        Scanner fromProcess;
        String command = "";
        int programCount = 0;

        HashMap<Integer, Process> processes = new HashMap<>();
        PriorityQueue<Process> events = new PriorityQueue<>();

        Queue<Integer> readyQueue = new LinkedList<>();
        boolean processorIdle = true;

        int runningProcess = 0;

        String event = "";
        int finishProg = 0; // number of finished programs
        int time = 0;

        File process = new File(pfFile);
        try {
            fromProcess = new Scanner(process);
            
            while (fromProcess.hasNextLine()) 
            {
                command = fromProcess.nextLine();
                String commandArray[] = command.split(" ");
                processes.put(programCount, new Process(programCount, Integer.parseInt(commandArray[0])));
                for (int i = 2; i < commandArray.length; i = i + 2) 
                {
                    processes.get(programCount).updateQueue(commandArray[i]);
                }
                programCount++;
            }
            for (int i = 0; i < programCount; i++) {
                processes.get(i).setEvent("ARRIVE");
                processes.get(i).setEventTime(processes.get(i).getArrival());
                events.add(processes.get(i));
            }
            while (finishProg != programCount) {
                Process processingEvent = events.remove();
                event = processingEvent.getEvent();
                time = processingEvent.getEventTime();
                switch (event) {
                    case "ARRIVE":
                        if (processorIdle == true) {
                            processingEvent.updateResponseTime(0);
                            // System.out.println("WAITED " + 0);
                            if (processingEvent.getStart() == -1) {
                                processingEvent.setStart(time);
                            }
                            processorIdle = false;
                            runningProcess = processingEvent.getProcessID();
                            if (processingEvent.isLast()) {
                                processingEvent.setEvent("EXIT");
                            } else {
                                processingEvent.setEvent("BLOCK");
                            }
                            int duration = Integer.parseInt(processingEvent.popQueue());
                            processingEvent.setService(processingEvent.getService() + duration);
                            processingEvent.setEventTime(time + duration);
                            events.add(processingEvent);
                        } else {
                            readyQueue.add(processingEvent.getProcessID());
                            processingEvent.setWait(time);
                        }
                        break;
                    case "BLOCK":
                        processingEvent.setEvent("UNBLOCK");
                        processingEvent.setEventTime(time + Integer.parseInt(processingEvent.popQueue()));
                        
                        events.add(processingEvent);
                        if (!readyQueue.isEmpty()) {
                            Process removedProcess = processes.get(readyQueue.remove());
                            removedProcess.updateResponseTime(time - removedProcess.getWait());
                            // System.out.println("WAITED " + (time-removedProcess.getWait()));
                            if (removedProcess.getStart() == -1) {
                                removedProcess.setStart(time);
                            }
                            runningProcess = removedProcess.getProcessID();
                            if (removedProcess.isLast()) {
                                removedProcess.setEvent("EXIT");
                            } else {
                                removedProcess.setEvent("BLOCK");
                            }
                            int duration = Integer.parseInt(removedProcess.popQueue());
                            removedProcess.setService(removedProcess.getService() + duration);
                            removedProcess.setEventTime(time + duration);
                            events.add(removedProcess);
                        } else {
                            processorIdle = true;
                        }
                        break;
                    case "EXIT":
                        processingEvent.setFinish(time);
                        processingEvent.setTurnaround(processingEvent.getFinish() - processingEvent.getArrival());
                        finishProg++;

                        if (!readyQueue.isEmpty()) {
                            Process removedProcess = processes.get(readyQueue.remove());
                            removedProcess.updateResponseTime(time - removedProcess.getWait());
                            // System.out.println("WAITED " + (time-removedProcess.getWait()));
                            if (removedProcess.getStart() == -1) {
                                removedProcess.setStart(time);
                            }
                            runningProcess = removedProcess.getProcessID();
                            if (removedProcess.isLast()) {
                                removedProcess.setEvent("EXIT");
                            } else {
                                removedProcess.setEvent("BLOCK");
                            }
                            int duration = Integer.parseInt(removedProcess.popQueue());
                            removedProcess.setService(removedProcess.getService() + duration);
                            removedProcess.setEventTime(time + duration);
                            events.add(removedProcess);
                        } else {
                            processorIdle = true;
                        }
                        break;
                    case "UNBLOCK":
                        if (processorIdle == true) {
                            processorIdle = false;
                            runningProcess = processingEvent.getProcessID();
                            processingEvent.updateResponseTime(0);
                            // System.out.println("WAITED " + 0);
                            if (processingEvent.getStart() == -1) {
                                processingEvent.setStart(time);
                            }
                            if (processingEvent.isLast()) {
                                processingEvent.setEvent("EXIT");
                            } else {
                                processingEvent.setEvent("BLOCK");
                            }
                            int duration = Integer.parseInt(processingEvent.popQueue());
                            processingEvent.setService(processingEvent.getService() + duration);
                            processingEvent.setEventTime(time + duration);
                            events.add(processingEvent);
                        } else {
                            processingEvent.setWait(time);
                            readyQueue.add(processingEvent.getProcessID());
                        }
                        break;
                    case "TIMEOUT":
                        System.out.println("TIMEOUT");
                        break;
                }
            }

            result(processes, programCount); 

        } catch (FileNotFoundException e) {
            System.out.println("Unable to find named file");
        }

    }

    public static void RR(String pfFile, int quantum) {
        System.out.println("RR");
        Scanner fromProcess;
        String command = "";
        int programCount = 0;
        HashMap<Integer, Process> processes = new HashMap<>();
        PriorityQueue<Process> events = new PriorityQueue<>();
        Queue<Integer> readyQueue = new LinkedList<>();
        boolean processorIdle = true;
        int runningProcess;
        String event = "";
        int finishProg = 0; // number of finished programs
        int time = 0;

        File process = new File(pfFile);
        try 
        {
            fromProcess = new Scanner(process);
            while (fromProcess.hasNextLine()) 
            {
                command = fromProcess.nextLine();
                String commandArray[] = command.split(" ");
                processes.put(programCount, new Process(programCount, Integer.parseInt(commandArray[0])));

                for (int i = 2; i < commandArray.length; i = i + 2) 
                {
                    processes.get(programCount).updateQueue(commandArray[i]);
                }

                programCount++;
            }
            for (int i = 0; i < programCount; i++) 
            {
                processes.get(i).setEvent("ARRIVE");
                processes.get(i).setEventTime(processes.get(i).getArrival());
                events.add(processes.get(i));
            }

            while (finishProg != programCount) 
            {
                Process processingEvent = events.remove();
                event = processingEvent.getEvent();
                time = processingEvent.getEventTime();
                switch (event) {
                    case "ARRIVE":
                        if (processorIdle == true) {
                            processingEvent.updateResponseTime(0);
                            // System.out.println("WAITED " + 0);
                            if (processingEvent.getStart() == -1) {
                                processingEvent.setStart(time);
                            }
                            processorIdle = false;
                            runningProcess = processingEvent.getProcessID();
                            if (processingEvent.isLast()) {
                                processingEvent.setEvent("EXIT");
                            } else {
                                processingEvent.setEvent("BLOCK");
                            }
                            int duration = Integer.parseInt(processingEvent.popQueue());
                            if (quantum < duration) {
                                processingEvent.setEvent("TIMEOUT");
                                processingEvent.setService(processingEvent.getService() + quantum);
                                processingEvent.setEventTime(time + quantum);
                                processingEvent.frontAdd(duration - quantum);
                            } else {
                                processingEvent.setService(processingEvent.getService() + duration);
                                processingEvent.setEventTime(time + duration);
                            }
                            events.add(processingEvent);
                        } else {
                            readyQueue.add(processingEvent.getProcessID());
                            processingEvent.setWait(time);
                        }
                        break;
                    case "BLOCK":
                        processingEvent.setEvent("UNBLOCK");
                        processingEvent.setEventTime(time + Integer.parseInt(processingEvent.popQueue()));
                        events.add(processingEvent);
                        if (!readyQueue.isEmpty()) {
                            Process removedProcess = processes.get(readyQueue.remove());
                            removedProcess.updateResponseTime(time - removedProcess.getWait());
                            // System.out.println("WAITED " + (time-removedProcess.getWait()));
                            if (removedProcess.getStart() == -1) {
                                removedProcess.setStart(time);
                            }
                            runningProcess = removedProcess.getProcessID();
                            if (removedProcess.isLast()) {
                                removedProcess.setEvent("EXIT");
                            } else {
                                removedProcess.setEvent("BLOCK");
                            }
                            int duration = Integer.parseInt(removedProcess.popQueue());
                            if (quantum < duration) {
                                removedProcess.setEvent("TIMEOUT");
                                removedProcess.setService(removedProcess.getService() + quantum);
                                removedProcess.setEventTime(time + quantum);
                                removedProcess.frontAdd(duration - quantum);
                            } else {
                                removedProcess.setService(removedProcess.getService() + duration);
                                removedProcess.setEventTime(time + duration);
                            }
                            events.add(removedProcess);
                        } else {
                            processorIdle = true;
                        }
                        break;
                    case "EXIT":
                        processingEvent.setFinish(time);
                        processingEvent.setTurnaround(processingEvent.getFinish() - processingEvent.getArrival());
                        finishProg++;

                        if (!readyQueue.isEmpty()) {
                            Process removedProcess = processes.get(readyQueue.remove());
                            removedProcess.updateResponseTime(time - removedProcess.getWait());
                            // System.out.println("WAITED " + (time-removedProcess.getWait()));
                            if (removedProcess.getStart() == -1) {
                                removedProcess.setStart(time);
                            }
                            runningProcess = removedProcess.getProcessID();
                            if (removedProcess.isLast()) {
                                removedProcess.setEvent("EXIT");
                            } else {
                                removedProcess.setEvent("BLOCK");
                            }
                            int duration = Integer.parseInt(removedProcess.popQueue());
                            if (quantum < duration) {
                                removedProcess.setEvent("TIMEOUT");
                                removedProcess.setService(removedProcess.getService() + quantum);
                                removedProcess.setEventTime(time + quantum);
                                removedProcess.frontAdd(duration - quantum);
                            } else {
                                removedProcess.setService(removedProcess.getService() + duration);
                                removedProcess.setEventTime(time + duration);
                            }
                            events.add(removedProcess);
                        } else {
                            processorIdle = true;
                        }
                        break;
                    case "UNBLOCK":
                        if (processorIdle == true) {
                            processorIdle = false;
                            runningProcess = processingEvent.getProcessID();
                            processingEvent.updateResponseTime(0);
                            // System.out.println("WAITED " + 0);
                            if (processingEvent.getStart() == -1) {
                                processingEvent.setStart(time);
                            }
                            if (processingEvent.isLast()) {
                                processingEvent.setEvent("EXIT");
                            } else {
                                processingEvent.setEvent("BLOCK");
                            }
                            int duration = Integer.parseInt(processingEvent.popQueue());
                            if (quantum < duration) {
                                processingEvent.setEvent("TIMEOUT");
                                processingEvent.setService(processingEvent.getService() + quantum);
                                processingEvent.setEventTime(time + quantum);
                                processingEvent.frontAdd(duration - quantum);
                            } else {
                                processingEvent.setService(processingEvent.getService() + duration);
                                processingEvent.setEventTime(time + duration);
                            }
                            events.add(processingEvent);
                        } else {
                            processingEvent.setWait(time);
                            readyQueue.add(processingEvent.getProcessID());
                        }
                        break;
                    case "TIMEOUT":
                        if (!readyQueue.isEmpty()) {
                            processingEvent.setWait(time);
                            readyQueue.add(processingEvent.getProcessID());
                            Process removedProcess = processes.get(readyQueue.remove());
                            removedProcess.updateResponseTime(time - removedProcess.getWait());
                            // System.out.println("WAITED " + (time-removedProcess.getWait()));
                            if (removedProcess.getStart() == -1) {
                                removedProcess.setStart(time);
                            }
                            runningProcess = removedProcess.getProcessID();
                            if (removedProcess.isLast()) {
                                removedProcess.setEvent("EXIT");
                            } else {
                                removedProcess.setEvent("BLOCK");
                            }
                            int duration = Integer.parseInt(removedProcess.popQueue());
                            if (quantum < duration) {
                                removedProcess.setEvent("TIMEOUT");
                                removedProcess.setService(removedProcess.getService() + quantum);
                                removedProcess.setEventTime(time + quantum);
                                removedProcess.frontAdd(duration - quantum);
                            } else {
                                removedProcess.setService(removedProcess.getService() + duration);
                                removedProcess.setEventTime(time + duration);
                            }
                            events.add(removedProcess);
                        } else {
                            runningProcess = processingEvent.getProcessID();
                            processingEvent.updateResponseTime(0);
                            // System.out.println("WAITED " + 0);
                            if (processingEvent.getStart() == -1) {
                                processingEvent.setStart(time);
                            }
                            if (processingEvent.isLast()) {
                                processingEvent.setEvent("EXIT");
                            } else {
                                processingEvent.setEvent("BLOCK");
                            }
                            int duration = Integer.parseInt(processingEvent.popQueue());
                            if (quantum < duration) {
                                processingEvent.setEvent("TIMEOUT");
                                processingEvent.setService(processingEvent.getService() + quantum);
                                processingEvent.setEventTime(time + quantum);
                                processingEvent.frontAdd(duration - quantum);
                            } else {
                                processingEvent.setService(processingEvent.getService() + duration);
                                processingEvent.setEventTime(time + duration);
                            }
                            events.add(processingEvent);
                        }
                        break;
                }
            }

            result(processes, programCount); 

        } catch (FileNotFoundException e) {
            System.out.println("Unable to find named file");
        }

    }

    public static void SPN(String pfFile, boolean serviceGiven, double alpha) 
    {
        System.out.println("SPN");
        Scanner fromProcess;
        String command = "";
        int programCount = 0;
        HashMap<Integer, Process> processes = new HashMap<>();
        PriorityQueue<Process> events = new PriorityQueue<>();
        PriorityQueue<Entry> readyQueue = new PriorityQueue<>();
        boolean processorIdle = true;
        int runningProcess = -1;
        String event = "";
        int finishProg = 0; // number of finished programs
        int time = 0;

        File process = new File(pfFile);
        try {
            fromProcess = new Scanner(process);
            while (fromProcess.hasNextLine()) {
                command = fromProcess.nextLine();
                String commandArray[] = command.split(" ");
                processes.put(programCount, new Process(programCount, Integer.parseInt(commandArray[0])));
                for (int i = 2; i < commandArray.length; i = i + 2) {
                    processes.get(programCount).updateQueue(commandArray[i]);
                }
                programCount++;
            }

            for (int i = 0; i < programCount; i++) 
            {
                processes.get(i).setEvent("ARRIVE");
                processes.get(i).setEventTime(processes.get(i).getArrival());
                processes.get(i).setNextTime();
                events.add(processes.get(i));
            }
            
            while (finishProg != programCount) 
            {
                Process processingEvent = events.remove();
                event = processingEvent.getEvent();
                time = processingEvent.getEventTime();
                switch (event) 
                {
                    case "ARRIVE":
                        if (processorIdle == true) {
                            processingEvent.updateResponseTime(0);
                            if (processingEvent.getStart() == -1) {
                                processingEvent.setStart(time);
                            }
                            processorIdle = false;
                            runningProcess = processingEvent.getProcessID();
                            if (processingEvent.isLast()) {
                                processingEvent.setEvent("EXIT");
                            } else {
                                processingEvent.setEvent("BLOCK");
                            }
                            int duration = Integer.parseInt(processingEvent.popQueue());
                            processingEvent.setService(processingEvent.getService() + duration);
                            processingEvent.setEventTime(time + duration);
                            events.add(processingEvent);
                        } else {
                            readyQueue.add(processingEvent.getEntry());
                            processingEvent.setWait(time);
                        }
                        break;

                    case "BLOCK":
                        processingEvent.setEvent("UNBLOCK");
                        processingEvent.setEventTime(time + Integer.parseInt(processingEvent.popQueue()));
                        events.add(processingEvent);
                        if (!readyQueue.isEmpty()) {
                            Process removedProcess = processes.get(readyQueue.remove().getKey());
                            removedProcess.updateResponseTime(time - removedProcess.getWait());
                            // System.out.println("WAITED " + (time-removedProcess.getWait()));
                            if (removedProcess.getStart() == -1) {
                                removedProcess.setStart(time);
                            }
                            runningProcess = removedProcess.getProcessID();
                            if (removedProcess.isLast()) {
                                removedProcess.setEvent("EXIT");
                            } else {
                                removedProcess.setEvent("BLOCK");
                            }

                            int duration = Integer.parseInt(removedProcess.popQueue());

                            if(serviceGiven == false){
                                removedProcess.predictNextTime(duration, alpha);
                            }
                            removedProcess.setService(removedProcess.getService() + duration);
                            removedProcess.setEventTime(time + duration);
                            events.add(removedProcess);
                        } else {
                            processorIdle = true;
                        }
                        break;

                    case "EXIT":
                        processingEvent.setFinish(time);
                        processingEvent.setTurnaround(processingEvent.getFinish() - processingEvent.getArrival());
                        finishProg++;

                        if (!readyQueue.isEmpty()) 
                        {
                            Process removedProcess = processes.get(readyQueue.remove().getKey());
                            removedProcess.updateResponseTime(time - removedProcess.getWait());
                            // System.out.println("WAITED " + (time-removedProcess.getWait()));
                            if (removedProcess.getStart() == -1) {
                                removedProcess.setStart(time);
                            }
                            runningProcess = removedProcess.getProcessID();
                            if (removedProcess.isLast()) 
                            {
                                removedProcess.setEvent("EXIT");
                            } 
                            else 
                            {
                                removedProcess.setEvent("BLOCK");
                            }

                            int duration = Integer.parseInt(removedProcess.popQueue());
                            if(serviceGiven == false){
                                removedProcess.predictNextTime(duration, alpha);
                            }
                            removedProcess.setService(removedProcess.getService() + duration);
                            removedProcess.setEventTime(time + duration);
                            events.add(removedProcess);
                        } 
                        else 
                        {
                            processorIdle = true;
                        }
                        break;

                    case "UNBLOCK":
                        if (processorIdle == true) 
                        {
                            processorIdle = false;
                            runningProcess = processingEvent.getProcessID();
                            processingEvent.updateResponseTime(0);
                            // System.out.println("WAITED " + 0);
                            if (processingEvent.getStart() == -1) 
                            {
                                processingEvent.setStart(time);
                            }
                            if (processingEvent.isLast()) 
                            {
                                processingEvent.setEvent("EXIT");
                            } 
                            else 
                            {
                                processingEvent.setEvent("BLOCK");
                            }

                            int duration = Integer.parseInt(processingEvent.popQueue());
                            processingEvent.setService(processingEvent.getService() + duration);
                            processingEvent.setEventTime(time + duration);
                            events.add(processingEvent);

                        } 
                        else 
                        {
                            processingEvent.setWait(time);

                            if(serviceGiven == true)
                            {
                                processingEvent.setNextTime();
                            }
                            readyQueue.add(processingEvent.getEntry());
                        }
                        break;
                        
                    case "TIMEOUT":
                        System.out.println("TIMEOUT");
                        break;
                }
            }



            result(processes, programCount); 

        } catch (FileNotFoundException e) {
            System.out.println("Unable to find named file");
        }

    }

    public static void HRRN(String pfFile, boolean serviceGiven, double alpha) 
    {
        System.out.println("HRRN");
        Scanner fromProcess;
        String command = "";
        int programCount = 0;
        HashMap<Integer, Process> processes = new HashMap<>();
        PriorityQueue<Process> events = new PriorityQueue<>();
        PriorityQueue<Entry> readyQueue = new PriorityQueue<>(Collections.reverseOrder());
        boolean processorIdle = true;
        int runningProcess = -1;
        String event = "";
        int finishProg = 0; // number of finished programs
        int time = 0;

        File process = new File(pfFile);
        try {
            fromProcess = new Scanner(process);
            while (fromProcess.hasNextLine()) {
                command = fromProcess.nextLine();
                String commandArray[] = command.split(" ");
                processes.put(programCount, new Process(programCount, Integer.parseInt(commandArray[0])));
                for (int i = 2; i < commandArray.length; i = i + 2) {
                    processes.get(programCount).updateQueue(commandArray[i]);
                }
                programCount++;
            }
            for (int i = 0; i < programCount; i++) {
                processes.get(i).setEvent("ARRIVE");
                processes.get(i).setEventTime(processes.get(i).getArrival());
                processes.get(i).setNextTime();
                events.add(processes.get(i));
            }
            
            while (finishProg != programCount) {
                Process processingEvent = events.remove();
                event = processingEvent.getEvent();
                time = processingEvent.getEventTime();
                switch (event) {
                    case "ARRIVE":
                        if (processorIdle == true) {
                            processingEvent.updateResponseTime(0);
                            // System.out.println("WAITED " + 0);
                            if (processingEvent.getStart() == -1) {
                                processingEvent.setStart(time);
                            }
                            processorIdle = false;
                            runningProcess = processingEvent.getProcessID();
                            if (processingEvent.isLast()) {
                                processingEvent.setEvent("EXIT");
                            } else {
                                processingEvent.setEvent("BLOCK");
                            }
                            int duration = Integer.parseInt(processingEvent.popQueue());
                            processingEvent.setService(processingEvent.getService() + duration);
                            processingEvent.setEventTime(time + duration);
                            events.add(processingEvent);
                        } else {
                            readyQueue.add(processingEvent.getEntry());
                            processingEvent.setWait(time);
                        }
                        break;
                    case "BLOCK":
                        processingEvent.setEvent("UNBLOCK");
                        processingEvent.setEventTime(time + Integer.parseInt(processingEvent.popQueue()));
                        events.add(processingEvent);
                        if (!readyQueue.isEmpty()) {
                            for(Entry cur : readyQueue){
                                processes.get(cur.getKey()).factorHRRN(time - processes.get(cur.getKey()).getWait());

                                System.out.println((cur.getKey()) + " " + (processes.get(cur.getKey()).getEntry().getStored()));
                                System.out.println(processes.get(cur.getKey()).getEntry().getValue());
                            }
                            Process removedProcess = processes.get(readyQueue.remove().getKey());
                            removedProcess.updateResponseTime(time - removedProcess.getWait());
                            // System.out.println("WAITED " + (time-removedProcess.getWait()));
                            if (removedProcess.getStart() == -1) {
                                removedProcess.setStart(time);
                            }
                            runningProcess = removedProcess.getProcessID();
                            if (removedProcess.isLast()) {
                                removedProcess.setEvent("EXIT");
                            } else {
                                removedProcess.setEvent("BLOCK");
                            }
                            int duration = Integer.parseInt(removedProcess.popQueue());
                            if(serviceGiven == false){
                            removedProcess.predictNextTime(duration, alpha);
                            }
                            removedProcess.setService(removedProcess.getService() + duration);
                            removedProcess.setEventTime(time + duration);
                            events.add(removedProcess);
                        } else {
                            processorIdle = true;
                        }
                        break;
                    case "EXIT":
                        processingEvent.setFinish(time);
                        processingEvent.setTurnaround(processingEvent.getFinish() - processingEvent.getArrival());
                        finishProg++;

                        if (!readyQueue.isEmpty()) {
                            for(Entry cur : readyQueue){
                                processes.get(cur.getKey()).factorHRRN(time - processes.get(cur.getKey()).getWait());
                                System.out.println(processes.get(cur.getKey()));
                                System.out.println(processes.get(cur.getKey()).getEntry().getValue());
                            }
                            Process removedProcess = processes.get(readyQueue.remove().getKey());
                            removedProcess.updateResponseTime(time - removedProcess.getWait());
                            // System.out.println("WAITED " + (time-removedProcess.getWait()));
                            if (removedProcess.getStart() == -1) {
                                removedProcess.setStart(time);
                            }
                            runningProcess = removedProcess.getProcessID();
                            if (removedProcess.isLast()) {
                                removedProcess.setEvent("EXIT");
                            } else {
                                removedProcess.setEvent("BLOCK");
                            }
                            int duration = Integer.parseInt(removedProcess.popQueue());

                            if(serviceGiven == false){
                                removedProcess.predictNextTime(duration, alpha);
                            }
                            removedProcess.setService(removedProcess.getService() + duration);
                            removedProcess.setEventTime(time + duration);
                            events.add(removedProcess);
                        } else {
                            processorIdle = true;
                        }
                        break;
                    case "UNBLOCK":
                        if (processorIdle == true) 
                        {
                            processorIdle = false;
                            runningProcess = processingEvent.getProcessID();
                            processingEvent.updateResponseTime(0);
                            // System.out.println("WAITED " + 0);
                            if (processingEvent.getStart() == -1) 
                            {
                                processingEvent.setStart(time);
                            }
                            if (processingEvent.isLast()) 
                            {
                                processingEvent.setEvent("EXIT");
                            } 

                            else 
                            {
                                processingEvent.setEvent("BLOCK");
                            }

                            int duration = Integer.parseInt(processingEvent.popQueue());
                            processingEvent.setService(processingEvent.getService() + duration);
                            processingEvent.setEventTime(time + duration);
                            events.add(processingEvent);
                        } 
                        else 
                        {
                            processingEvent.setWait(time);
                            if(serviceGiven == true)
                            {
                                processingEvent.setNextTime();
                            }

                            readyQueue.add(processingEvent.getEntry());
                        }
                        break;
                    case "TIMEOUT":
                        System.out.println("TIMEOUT");
                        break;
                }
            }

            result(processes, programCount); 
        } 
        catch (FileNotFoundException e) 
        {
            System.out.println("Unable to find named file");
        }
    }


    public static void result(HashMap<Integer, Process> processes, int programCount) 
    {
        int sumTurnaround = 0;
        double sumNormalizedTurnaround = 0;
        int sumResponse = 0;
        for (int i = 0; i < programCount; i++) 
        {
            Process selected = processes.get(i);
            System.out.println("Process " + selected.getProcessID() + " :");
            System.out.println("\tArrive = " + selected.getArrival());
            System.out.println("\tService = " + selected.getService());
            System.out.println("\tStart = " + selected.getStart());
            System.out.println("\tFinish = " + selected.getFinish());
            System.out.println("\tTurnaround = " + selected.getTurnaround());

            sumTurnaround += selected.getTurnaround();

            System.out.println("\tNormalized Turnaround = " + (double) selected.getTurnaround() / selected.getService());

            sumNormalizedTurnaround += (double) selected.getTurnaround() / selected.getService();

            System.out.println("\tAverage Response Time = " + selected.getResponseTime());

            sumResponse += selected.getResponseTime();
        }

        System.out.println("Mean Turnaround: " + ((double) sumTurnaround / programCount));
        System.out.println("Mean Normalized Turnaround: " + (sumNormalizedTurnaround / programCount));
        System.out.println("Mean Average Response Time: " + ((double) sumResponse / programCount));
    }
}


