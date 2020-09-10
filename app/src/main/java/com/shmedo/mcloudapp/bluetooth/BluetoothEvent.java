package com.shmedo.mcloudapp.bluetooth;

import com.shmedo.core.event.MessageEvent;

/**
 * Created by Liudongdong on 18/2/1.
 */

public class BluetoothEvent extends MessageEvent {
    private BluetoothEventType eventType;
    private Object eventData;

    public BluetoothEvent(BluetoothEventType eventType, Object eventData) {
        this.eventType = eventType;
        this.eventData = eventData;
    }

    public BluetoothEventType getEventType() {
        return eventType;
    }

    public void setEventType(BluetoothEventType eventType) {
        this.eventType = eventType;
    }

    public Object getEventData() {
        return eventData;
    }

    public void setEventData(Object eventData) {
        this.eventData = eventData;
    }

    public static BluetoothEvent withEventType(BluetoothEventType eventType) {
        return new BluetoothEvent(eventType, null);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private BluetoothEventType eventType;
        private Object eventData;

        public Builder setEventType(BluetoothEventType eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder setEventData(Object eventData) {
            this.eventData = eventData;
            return this;
        }

        public BluetoothEvent build() {
            return new BluetoothEvent(this.eventType, this.eventData);
        }
    }
}
