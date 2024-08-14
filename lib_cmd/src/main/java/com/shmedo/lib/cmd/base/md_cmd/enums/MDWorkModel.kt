package com.shmedo.lib.cmd.base.md_cmd.enums

/**
 * Created by adu on 2017/12/11.
 * 工作模式
 */
enum class MDWorkModel(//info模式
    private val model: String
) {
    INITIALZE("0"),

    //初始化模式
    WORK("1"),

    //工作模式
    DEBUG("2"),

    //debug模式
    INFO("3");

    override fun toString(): String {
        return model
    }

    companion object {
        fun value(model: String): MDWorkModel {
            return when (model) {
                "0" -> INITIALZE
                "1" -> WORK
                "2" -> DEBUG
                "3" -> INFO
                else -> INITIALZE
            }
        }
    }
}
