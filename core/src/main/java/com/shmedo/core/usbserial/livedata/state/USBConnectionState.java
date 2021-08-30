package com.shmedo.core.usbserial.livedata.state;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/8/11 <br/>
 * 描述：     TODO
 */
public class USBConnectionState {
    public enum State {
        CONNECTING,
        READY,
        DISCONNECTED
    }

    public static final class Connecting extends USBConnectionState {
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        public static Connecting INSTANCE = new Connecting();

        private Connecting() {
            super(State.CONNECTING);
        }
    }

    public static final class Ready extends USBConnectionState {
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        public static Ready INSTANCE = new Ready();

        private Ready() {
            super(State.READY);
        }
    }

    public static final class Disconnected extends USBConnectionState {
        private final String reason;

        @RestrictTo(RestrictTo.Scope.LIBRARY)
        public Disconnected( final String reason) {
            super(State.DISCONNECTED);
            this.reason = reason;
        }

        public String getReason() {
            return reason;
        }
    }

    protected final State state;

    private USBConnectionState(@NonNull final State state) {
        this.state = state;
    }

    /**
     * The connection state. This can be used in <i>switch</i> in Java.
     */
    public final State getState() {
        return state;
    }

    /**
     * Whether the target device is connected, or not.
     */
    public final boolean isConnected() {
        return state == State.READY;
    }

    /**
     * Whether the target device is ready to use.
     */
    public final boolean isReady() {
        return state == State.READY;
    }

}
