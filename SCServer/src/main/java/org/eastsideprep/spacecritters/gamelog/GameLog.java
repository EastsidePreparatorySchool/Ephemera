/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package org.eastsideprep.spacecritters.gamelog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * @author gmein
 */
public class GameLog {

    private final ReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock rlock;
    private final Lock wlock;

    private final GameLogState state;
    private ArrayList<GameLogEntry> log = new ArrayList<>();
    private final HashMap<Object, GameLogObserver> observers = new HashMap<>();

    private int start = 0;
    private int end = 0;
    private int minRead = 0;
    private final int COLLAPSE_THRESHOLD = 1000;

    public GameLog(GameLogState state) {
        rlock = rwl.readLock();
        wlock = rwl.writeLock();
        this.state = state;
    }

    public void addLogEntry(GameLogEntry item) {
        wlock.lock();
        try {
            log.add(item);
            end++;
        } finally {
            wlock.unlock();
        }
//        printLogInfo("AE");
    }

    public void addLogEntries(List<GameLogEntry> list) {
        wlock.lock();
        try {
            log.addAll(list);
            end += list.size();
        } finally {
            wlock.unlock();
        }
//        printLogInfo("AES");
    }

    public GameLogObserver addObserver(Object client) {
        collapseRead();
        GameLogObserver obs;
        synchronized (observers) {
            obs = new GameLogObserver(this);
            observers.put(client, obs);
        }
        printLogInfo("AO");
        return obs;
    }

    public void removeObserver(Object client) {
        synchronized (observers) {
            observers.remove(client);
            updateMinRead();
        }
        printLogInfo("RO");
        collapseRead();
    }

    public void collapseRead() {
        if (minRead - start <= COLLAPSE_THRESHOLD) {
            return;
        }

        wlock.lock();
        try {
            printLogInfo("CR1");
            System.out.println("Log: compacting, state entries:" + state.getEntryCount());
            // process all read items into state log, 
            for (int i = 0; i < minRead - start; i++) {
                state.addEntry(log.get(i));
            }

            // take the sublist of unread items, make it the new list, 
            ArrayList<GameLogEntry> newLog = new ArrayList<>();
            newLog.addAll(log.subList(minRead - start, end - start));
            log = newLog;

            // and adjust the "start" offset
            start = minRead;
            printLogInfo("CR2");
            System.out.println("Log: compacted, state entries:" + state.getEntryCount());
        } finally {
            wlock.unlock();
        }
    }

    // needs rewrite with concept of observers
    public ArrayList<GameLogEntry> getNewItems(GameLogObserver obs) {
        ArrayList<GameLogEntry> result = new ArrayList<>();

        rlock.lock();
        int oldMinRead = minRead;
        try {
            printLogInfo("GNI1", obs);
            int items = end - obs.maxRead;
            if (items > 0) {
                // copy the new items to the result
                result.addAll(log.subList(obs.maxRead - start, end - start));

                // update maxRead, and possibly minRead
                // need to lock this, multiple threads might want to do it
                synchronized (observers) {
                    int oldMax = obs.maxRead;
                    obs.maxRead = end;

                    // if we were at minRead we might need to move it
                    if (oldMax == minRead) {
                        updateMinRead();
                    }
                }
                printLogInfo("GNI2", obs);

            }

        } finally {
            rlock.unlock();
        }

        collapseRead();

        return result;
    }

    public GameLogState getNewGameLogState() {
        GameLogState result = null;
        wlock.lock();
        try {
            result = state.copy();
        } finally {
            wlock.unlock();
        }
        return result;
    }

    // only call this when already synchronized on observers
    private void updateMinRead() {
        int currentMin = end;

        for (GameLogObserver o : observers.values()) {
            if (o.maxRead < currentMin) {
                currentMin = o.maxRead;
            }
        }
        // record new minimum
        minRead = currentMin;
    }

    private void printLogInfo(String op) {
        if (end < start || minRead < start || minRead > end) {
            System.out.println("---- log corrupt");
        }
        System.out.println("Log" + op + ": array size:" + log.size() + ", start:" + start + ", end:" + end + ", minRead:" + minRead + ", state:" + state.getEntryCount());
    }

    private void printLogInfo(String op, GameLogObserver obs) {
        printLogInfo(op);
        if (obs.maxRead < start || obs.maxRead > end) {
            System.out.println("---- obs data corrupt");
        }
        System.out.println("  obs:" + Integer.toHexString(obs.hashCode()) + ", maxRead:" + obs.maxRead);
    }
}
