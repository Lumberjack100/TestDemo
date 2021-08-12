package com.shmedo.configlibrary.at;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/8/11 <br/>
 * 描述：     TODO
 */
public class ATCommand {
    public final static String NEWLINE_CRLF = "\r\n";
    public final static String NEWLINE_CR = "\r";
    public final static String NEWLINE_LF = "\n";

    public static final String COMMAND_HEADER = "AT+";
    public static final String COMMAND_RESULT_HEADER = "+";
    public static final String DELIMITER_COLON = ":";


    public static final String QUERY_FLAG = "?";
    public static final String OK_FLAG = "\r\nOK\r\n";
    public static final String ERROR_FLAG = "ERR";


}
