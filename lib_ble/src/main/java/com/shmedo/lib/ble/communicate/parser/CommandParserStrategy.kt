package com.shmedo.lib.ble.communicate.parser

/**
 * 指令解析策略接口
 * 
 * 创建者：gonghe
 * 创建时间：2023/9/6
 * 描述：使用策略模式处理不同类型的指令解析
 */
interface CommandParserStrategy {
    /**
     * 判断是否可以解析该内容
     */
    fun canParse(content: String): Boolean
    
    /**
     * 解析内容并返回指令列表
     */
    fun parse(content: String): List<String>
}

/**
 * 物联网指令解析策略 - 处理包含 $$ 的指令
 */
class IoTCommandParser : CommandParserStrategy {
    override fun canParse(content: String): Boolean = content.contains("$$")
    
    override fun parse(content: String): List<String> {
        return content.split("\r\n".toRegex())
            .filter { it.isNotEmpty() }
            .mapNotNull { tempCmd ->
                val index = tempCmd.lastIndexOf("$$")
                if (index != -1) tempCmd.substring(index) else null
            }
    }
}

/**
 * 标准指令解析策略 - 处理包含 $cmd 的指令
 */
class StandardCommandParser : CommandParserStrategy {
    override fun canParse(content: String): Boolean = content.contains("\$cmd")
    
    override fun parse(content: String): List<String> {
        val index = content.lastIndexOf("\$cmd")
        return if (index != -1) {
            listOf(content.substring(index))
        } else {
            emptyList()
        }
    }
}

/**
 * 默认解析策略 - 处理其他所有内容
 */
class DefaultCommandParser : CommandParserStrategy {
    override fun canParse(content: String): Boolean = true
    override fun parse(content: String): List<String> = listOf(content)
} 