package com.shmedo.core.cmd;

import com.shmedo.core.interfaces.Validater;

/**
 * Created by Liudongdong on 17/12/12.
 */
public class Entity implements Validater {
    private static final Entity empty = new Entity();

    @Override
    public void validate() {
        //just do nothing is ok
    }

    @Override
    public String toString() {
        return "";
    }

    public static Entity empty() {
        return empty;
    }
}
