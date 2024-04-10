package com.shmedo.lib.device.base.md_cmd.enums

/**
 * Created by adu on 2017/12/11.
 * 工作模式
 */
enum class WorkModel(//info模式
    private val model: Int
) {
    INITIALZE(0),

    //初始化模式
    WORK(1),

    //工作模式
    DEBUG(2),

    //debug模式
    INFO(3);

    fun toInt(): Int {
        return model
    }

    companion object {
        fun value(model: Int): WorkModel {
            return when (model) {
                0 -> INITIALZE
                1 -> WORK
                2 -> DEBUG
                3 -> INFO
                else -> INITIALZE
            }
        }

        fun isValidMode(value: Int): Boolean {
            val allModes: MutableList<Int> = ArrayList()
            for (model in entries) {
                allModes.add(model.toInt())
            }
            return allModes.contains(value)
        }
    }
}
