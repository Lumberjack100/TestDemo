package com.shmedo.iot.parser;

import android.text.TextUtils;

import com.shmedo.iot.IOTCommandResult;
import com.shmedo.iot.enums.IOTCommandType;
import com.shmedo.iot.interfaces.IOTResultParser;
import com.shmedo.iot.utils.IOTStringUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/8/31 <br/>
 * 描述：     TODO
 */
public class IOTParseManager {
    private static final IOTParseManager ourInstance = new IOTParseManager();

    private Map<IOTCommandType, IOTResultParser> parserMap = new HashMap<>();

    public static IOTParseManager getInstance() {
        if (ourInstance.parserMap == null || ourInstance.parserMap.size() <= 0) {
            ourInstance.registerParse();
        }
        return ourInstance;
    }

    private IOTParseManager() {
        registerParse();
    }


    public <T> T parse(String result) {
        baseValidate(result);
        IOTCommandType cmdType = IOTStringUtil.extractCommandType(result);
        IOTResultParser parser = parserMap.get(cmdType);
        if (parser == null)
            throw new RuntimeException("未找到命令：" + cmdType + "的解析器");

        parser.validate(result);
        T data = (T) parser.parse(result);

        return data;
    }

    /**
     * 这个方式只执行初级的格式校验，具体的逻辑校验由Validater接口和StringValidater接口执行
     *
     * @param result
     */
    private void baseValidate(String result) {
        if (TextUtils.isEmpty(result))
            throw new IllegalArgumentException("result为空或者null");
        if (result.length() < IOTCommandResult.RESULT_MIN_LENGTH)
            throw new IllegalArgumentException("result长度过短:" + result);
        if (!result.startsWith(IOTCommandResult.COMMAND_HEADER))
            throw new IllegalArgumentException("result格式错误:" + result);
    }


    /**
     * 将本包下的Parse注册到parserMap中
     */
    private void registerParse() {
        List<Class> clazzes = Arrays.asList(new Class[]{
                TerminalTimeParser.class,
                DeviceCurrentStateParser.class});

        registerWithClass(clazzes);
    }

    private void registerWithClass(List<Class> classes) {
        try {
            for (int i = 0; i < classes.size(); ++i) {
                IOTResultParser resultParser = (IOTResultParser) classes.get(i).newInstance();
                if (!parserMap.containsKey(resultParser.commandType())) {
                    parserMap.put(resultParser.commandType(), resultParser);
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
