package com.shmedo.lib.device.base.iot_cmd.model.adme

import android.text.TextUtils

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  12/28/20 <br></br>
 * 描述：     ADME的运行状态
 */
class AdmeMotionState {
    var motionstate //运动状态(0:管口停止，1:管底停止，2:管口测量，3:管口测试，4:上拉测量，5:上拉测试，6:下放测量，7:下放测试)
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var inctiondis //测斜仪运动距离或实时下放距离或管底等待时的距离
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var measmode //测量模式 (0：`正测  1：反测)
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field

    //电机运动信息(0：磁开关触发 1：测斜仪配对,设置参数 2：测斜仪下放 3：管底等待 4：测点测量 5：磁开关触发，测量结束 6：测斜仪配对,读取数据  7：数据上传  8：周期等待)
    //注：单次测量过程 0、1、2、3、4、5、6、7、8
    //   正反测量过程 0、1、2、3、4、5、6、8—》0、1、2、3、4、5、6、8
    var motorinfo //电机运动信息
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var measpoint //测点信息 (1|20.5  表示第一个测量点：20.5米)
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
    var waittime //管底等待时间
            : String? = null
        get() = if (TextUtils.isEmpty(field)) "" else field
}