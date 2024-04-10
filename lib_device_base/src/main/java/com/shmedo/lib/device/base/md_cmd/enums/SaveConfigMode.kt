package com.shmedo.lib.device.base.md_cmd.enums

/**
 * 项目名：  mCloudapp <br></br>
 * 包名：    com.shmedo.core.enums <br></br>
 * 创建者:   gonghe <br></br>
 * 创建时间:  2020/4/23 <br></br>
 * 描述：    保存设备配置参数方式
 */
enum class SaveConfigMode(private val model: Int) {
    //保存并重启
    SAVE_REBOOT(1),

    //保存
    SAVE_NO_REBOOT(2);

    fun toInt(): Int {
        return model
    }

    companion object {
        fun value(model: Int): SaveConfigMode {
            return when (model) {
                2 -> SAVE_NO_REBOOT
                else -> SAVE_REBOOT
            }
        }
    }
}
