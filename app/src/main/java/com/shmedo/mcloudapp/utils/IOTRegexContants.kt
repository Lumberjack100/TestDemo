package com.shmedo.mcloudapp.utils

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/12/14
 *
 * 描述： TODO
 *
 *
 */
interface IOTRegexContants {
    companion object {
        const val REGEX_MAC_ADDRESS_NO_COLON = "[A-F0-9]{12}"//匹配mac地址,不带冒号
    }
}