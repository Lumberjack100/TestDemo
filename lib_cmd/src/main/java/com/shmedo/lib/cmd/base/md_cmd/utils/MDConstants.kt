package com.shmedo.lib.cmd.base.md_cmd.utils

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/22 <br/>
 * 描述：     TODO
 */
interface MDConstants {

    companion object {
        const val SEND_COMMAND_HEADER = "##"
        const val RESPONSE_COMMAND_HEADER = "\$\$"
        const val COMMAND_SPLICER_COMMA = ","
        const val COMMAND_SPLICER_WHITESPACE = " "
        const val COMMAND_FOOTER = "\r\n"
        const val RESULT_MIN_LENGTH = 5
        const val ERROR_FLAG = "e"
    }
}