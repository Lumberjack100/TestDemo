package com.shmedo.lib.device.base.iot_cmd.model.das

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/4/15 <br></br>
 * 描述：      DAS 状态页面数据中心状态
 */
class DasNetStatusInfo {
    var index //中心编号
            = 0
    var errno //错误码
            = 0
    var send //已发送
            = 0
    var unsend //未发生
            = 0
    var rate //在线率
            = 0f
}