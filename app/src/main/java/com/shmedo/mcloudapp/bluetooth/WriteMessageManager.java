package com.shmedo.mcloudapp.bluetooth;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Liudongdong on 18/2/5.
 */

public class WriteMessageManager {
    private static WriteMessageManager instance = new WriteMessageManager();

    public static WriteMessageManager getInstance() {
        return instance;
    }

    private WriteMessageManager() {
    }

    private List<Message> messages = new LinkedList<>();

    public synchronized void addMessage(Message m) {
        messages.add(m);
    }

    public synchronized void addMessage(List<Message> msgs) {
        messages.addAll(msgs);
    }

    public synchronized Message getFront() {
        if (messages.size() <= 0) {
            return null;
        }
        return messages.get(0);
    }

    public synchronized void removeFront() {
        if (messages.size() <= 0) {
            return;
        }
        messages.remove(0);
    }

    public synchronized void clear() {
        messages.clear();
    }

    public synchronized int size() {
        return messages.size();
    }
}
