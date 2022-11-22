package com.shmedo.configlibrary.at

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/8/11 <br></br>
 * 描述：     TODO
 */
object ATCommand {
    const val NEWLINE_CRLF = "\r\n"
    const val NEWLINE_CR = "\r"
    const val NEWLINE_LF = "\n"
    const val COMMAND_HEADER = "AT+"
    const val COMMAND_RESULT_HEADER = "+"
    const val DELIMITER_COLON = ":"
    const val QUERY_FLAG = "?"
    const val OK_FLAG = "\r\nOK\r\n"
    const val ERROR_FLAG = "ERR"
}