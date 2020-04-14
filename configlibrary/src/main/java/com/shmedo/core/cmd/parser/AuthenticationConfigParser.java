package com.shmedo.core.cmd.parser;


import com.shmedo.core.enums.CommandType;
import com.shmedo.core.interfaces.ResultParser;
import com.shmedo.core.model.AuthenticationConfigInfo;
import com.shmedo.core.annotations.Parser;

/**
 * Created by adu on 2018/1/10.
 */
@Parser
public class AuthenticationConfigParser implements ResultParser<AuthenticationConfigInfo> {
    @Override
    public AuthenticationConfigInfo parse(String result) {
        AuthenticationConfigInfo info = new AuthenticationConfigInfo();

        return null;
    }

    @Override
    public void validate(String result) {

    }

    @Override
    public CommandType commandType() {
        return CommandType.AUTHENTICATION_CONFIG;
    }
}
