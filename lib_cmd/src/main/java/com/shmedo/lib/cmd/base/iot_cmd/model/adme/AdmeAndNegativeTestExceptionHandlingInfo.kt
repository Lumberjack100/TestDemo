package com.shmedo.lib.cmd.base.iot_cmd.model.adme

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  1/10/21 <br></br>
 * 描述：      ADME 正反测异常智能处理
 */
data class AdmeAndNegativeTestExceptionHandlingInfo (
    var mode: String = "",//工作模式(0:关，1:开)
)