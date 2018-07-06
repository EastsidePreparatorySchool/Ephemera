/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package gamelog;

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

    GameLog(GameLogState state) {
        rlock = rwl.readLock();
        wlock = rwl.writeLock();
        this.state = state;
    }

    void addLogEntries(List<GameLogEntry> list) {
        wlock.lock();
        try {
            log.addAll(list);
            end += list.size();
        } finally {
            wlock.unlock();
        }
    }

    GameLogObserver addObserver(Object client) {
        GameLogObserver obs;
        synchronized (observers) {
            obs = new GameLogObserver(this);
            observers.put(client, obs);
        }
        return obs;
    }

    void removeObserver(Object client) {
        synchronized (observers) {
            observers.remove(client);
        }
    }

    void collapseRead() {
        wlock.lock();
        try {
            // process all read items into state log, 
            for (int i = 0; i < minRead - start; i++) {
                state.addEntry(log.get(i));
            }

            // take the sublist of unread items, make it the new list, 
            ArrayList<GameLogEntry> newLog = new ArrayList<>();
            newLog.addAll(log.subList(minRead - start, end - start));
            log = newLog;

            // and adjust the "start" offset
            start -= end - minRead;
        } finally {
            wlock.unlock();
        }
    }

    // needs rewrite with concept of observers
    ArrayList<GameLogEntry> getNewItems(GameLogObserver obs) {
        ArrayList<GameLogEntry> result = new ArrayList<>();

        rlock.lock();
        try {
            // copy the new items to the result
            result.addAll(log.subList(obs.maxRead - start, end - start));

            // update maxRead, and possibly minRead
            // need to lock this, multiple threads might want to do it
            synchronized (observers) {
                // if we were at minRead and have new items, we might need to move it
                if (obs.maxRead == minRead && end > minRead) {
                    int currentMin = end;

                    for (GameLogObserver o : observers.values()) {
                        if (o.maxRead < currentMin) {
                            currentMin = o.maxRead;
                        }
                    }
                    // record new minimum
                    minRead = currentMin;
                }
                // set our new maxRead
                obs.maxRead = end;
            }

        } finally {
            rlock.unlock();
        }

        return result;
    }

    GameLogState getNewGameLogState() {
        GameLogState result = null;
        wlock.lock();
        try {
            result = state.clone();
        } finally {
            wlock.unlock();
        }
        return result;
    }
}
