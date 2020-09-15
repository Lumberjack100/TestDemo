package com.shmedo.configlibrary.ble.cmd;

import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * Created by Liudongdong on 17/12/12.
 */
public class CommandManager {
    private static final CommandManager instance = new CommandManager();

    public static CommandManager getInstance() {
        return instance;
    }

    private CommandManager() {
    }

    public String getCommand(CommandType commandType) {
        Command cmd = new Command(commandType);
        return cmd.toString();
    }

    public <T extends Validater> String getCommand(CommandType commandType, T parameter) {
        Command cmd = new Command(commandType, parameter);
        return cmd.toString();
    }


}
