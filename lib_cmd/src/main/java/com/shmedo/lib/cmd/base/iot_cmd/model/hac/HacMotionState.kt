package com.shmedo.lib.cmd.base.iot_cmd.model.hac

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  12/28/20 <br></br>
 * 描述：     ADME HAC的电机运动状态
 */
@Parcelize
data class HacMotionState(
    var abndiasis: String = "", //设备异常诊断 0：正常
    var measmode: String = "", //测量模式 (0：正测  1：反测)
    //电机运动信息(0：上拉至管口 1：测斜仪配对,设置参数 2：测斜仪下放 3：管底等待 4：测点测量 5：磁开关触发，测量结束 6：测斜仪配对,读取数据 7：数据上传 8：数据上传完成等待下次测量 9:等待反测 10：测量失败
    //注：单次测量过程 0、1、2、3、4、5、6、7、8
    //   正反测量过程 0、1、2、3、4、5、6、9—->1、2、3、4、5、6、7、8
    var motorinfo: String = "",
    var measpoint: String = "", //测点信息 (1|20.5  表示第一个测量点：20.5米)
    var waittime: String = "", //等待时间
    var incvoltage: String = "", //测斜仪电压
    var driveinputv: String = "", //驱动器输入电压
) : Parcelable
