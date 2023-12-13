package com.shmedo.lib.network.parser

import com.shmedo.lib.network.response.CloudPlatformApiResponse
import com.shmedo.lib.network.response.PageList
import rxhttp.wrapper.annotation.Parser
import rxhttp.wrapper.exception.ParseException
import rxhttp.wrapper.parse.TypeParser
import rxhttp.wrapper.utils.convertTo
import java.io.IOException
import java.lang.reflect.Type

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/12/12
 *
 * 描述： TODO
 *
 *
 */
@Parser(name = "Response", wrappers = [PageList::class])
open class CloudPlatformApiResponseParser<T> : TypeParser<T> {
    /**
     * 此构造方法适用于任意Class对象，但更多用于带泛型的Class对象，
     *
     * 如:List&lt;Student&gt;
     *
     * 用法:
     * Java: .asParser(new ResponseParser&lt;List&lt;Student&gt;&gt;(){})
     *
     * Kotlin: .asParser(object : ResponseParser&lt;List&lt;Student&gt;&gt;() {})
     *
     * 注：此构造方法一定要用protected关键字修饰，否则调用此构造方法将拿不到泛型类型
     */
    protected constructor() : super()

    /**
     * 此构造方法仅适用于不带泛型的Class对象，如: Student.class
     *
     * 用法
     * Java: .asParser(new ResponseParser<>(Student.class))   或者  .asResponse(Student.class)
     * Kotlin: .asParser(ResponseParser(Student::class.java)) 或者  .asResponse<Student>()
     */
    constructor(type: Type) : super(type)

    @Throws(IOException::class)
    override fun onParse(response: okhttp3.Response): T {
        val apiResponse: CloudPlatformApiResponse<T> = response.convertTo(CloudPlatformApiResponse::class, *types)
        var data = apiResponse.data //获取data字段
        if (data == null && types[0] === String::class.java) {
            /*
             * 考虑到有些时候服务端会返回：{"errorCode":0,"errorMsg":"关注成功"}  类似没有data的数据
             * 此时code正确，但是data字段为空，直接返回data的话，会报空指针错误，
             * 所以，判断泛型为String类型时，重新赋值，并确保赋值不为null
             */
            @Suppress("UNCHECKED_CAST")
            data = apiResponse.errCode?.errMessage as T
        }

        if ((apiResponse.errCode?.code != 0)) { //code不等于200，说明数据不正确，抛出异常
            throw ParseException(
                apiResponse.errCode?.code.toString(),
                apiResponse.errCode?.errMessage,
                response
            )
        }
        return data!!
    }
}