package com.shmedo.core.cmd;

import com.shmedo.core.enums.CommandType;
import com.shmedo.core.interfaces.Validater;

/**
 * Created by Liudongdong on 17/12/12.
 */
public class CommandManager {
    private static final CommandManager instance = new CommandManager();

    private CommandManager() {
    }

    public <T extends Validater> String getCommand(CommandType commandType, T parameter) {
        Command cmd = new Command(commandType, parameter);
        return cmd.toString();
    }

    public static CommandManager getInstance() {
        return instance;
    }
}
