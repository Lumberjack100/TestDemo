import com.shmedo.lib.device.base.Validater

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/4/15 <br></br>
 * 描述：     获取服务器(数据中心)编号参数
 */
class IndexEntity(private val index: Int) : Validater {
    override fun validate() {}
    override fun toString(): String {
        return "index=$index"
    }
}