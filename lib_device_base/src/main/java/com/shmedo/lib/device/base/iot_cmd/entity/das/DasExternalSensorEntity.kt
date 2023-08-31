import com.shmedo.lib.device.base.Validater

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/4/21 <br></br>
 * 描述：     生成扩展传感器参数拼接指令
 */
class DasExternalSensorEntity : Validater {
    var index //传感器接入顺序（第一支、第二支...）
            : String? = null
    var type //传感器类型
            : String? = null
    var addr //传感器地址/通道
            : String? = null
    var threshold //触发值
            : String? = null
    var corrval //修正值
            : String? = null
    var spacing //测段长
            : String? = null
    var holenum //测孔编号
            : String? = null
    var tubealti //安装高程
            : String? = null
    var ropelen //安装绳长
            : String? = null
    var poly_a //多项式系数A
            : String? = null
    var poly_b //多项式系数B
            : String? = null
    var poly_c //多项式系数C
            : String? = null
    var temp_k //温度系数K
            : String? = null
    var temp_t0 //初始温度T0
            : String? = null
    var sens_k //灵敏度K
            : String? = null
    var temp_b //温度系数b
            : String? = null
    var referval_f //基准值F
            : String? = null
    var elastic_mod //膨胀系数(应力计)
            : String? = null

    //量水堰计特有
    var lsycsds //初始读数
            : String? = null
    var lsyysst //初始堰上水头
            : String? = null

    //倾角仪特有
    var initvalx //X轴初始值
            : String? = null
    var initvaly //Y轴初始值
            : String? = null
    var initvalz //Z轴初始值
            : String? = null
    var child_type //子传感器类型
            : String? = null

    //阵列测斜仪特有
    var datatype // 解算方式 0:顶部  1: 底部
            : String? = null
    var measinval // 测量间隔
            : String? = null
    var model_type // 模型切换 0:坐标模型  1: ADME 模型
            : String? = null


    override fun validate() {}
    override fun toString(): String {
        val stringBuilder = StringBuilder()
        try {
            stringBuilder.append("index")
            stringBuilder.append("=")
            stringBuilder.append(index)
            stringBuilder.append("&")
            stringBuilder.append("type")
            stringBuilder.append("=")
            stringBuilder.append(type)
            stringBuilder.append("&")
            stringBuilder.append("addr")
            stringBuilder.append("=")
            stringBuilder.append(addr)
            stringBuilder.append("&")
            for (f in javaClass.declaredFields) {
                val value = f[this]
                if (value != null && f.name != "index" && f.name != "type" && f.name != "addr" && value != "NullKey") {
                    stringBuilder.append(f.name)
                    stringBuilder.append("=")
                    stringBuilder.append(value)
                    stringBuilder.append("&")
                }
            }
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        if (stringBuilder.toString().endsWith("&")) {
            stringBuilder.delete(stringBuilder.length - 1, stringBuilder.length)
        }
        return stringBuilder.toString()
    }
}