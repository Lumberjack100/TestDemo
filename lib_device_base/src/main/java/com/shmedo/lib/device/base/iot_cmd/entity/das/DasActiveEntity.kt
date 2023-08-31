import com.shmedo.lib.device.base.Validater

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  4/12/21 <br></br>
 * 描述：    生成DAS  激活参数拼接指令
 */
class DasActiveEntity : Validater {
    var mode //0表示待机，1表示激活
            : String? = null

    override fun validate() {}

    override fun toString(): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append("mode=$mode")
        return stringBuilder.toString()
    }
}