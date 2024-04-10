package com.shmedo.lib.device.base.md_cmd.enums

/**
 * 项目名：  mCloudapp <br></br>
 * 包名：    com.shmedo.core.enums <br></br>
 * 创建者:   gonghe <br></br>
 * 创建时间:  2020/4/23 <br></br>
 * 描述：   日志输出状态
 */
enum class LogOutputStatus(//打开
    private val status: Int
) {
    CLOSE(0),

    //关闭
    OPEN(1);

    fun toInt(): Int {
        return status
    }

    companion object {
        fun value(status: Int): LogOutputStatus {
            return when (status) {
                0 -> CLOSE
                1 -> OPEN
                else -> CLOSE
            }
        }
    }
}
