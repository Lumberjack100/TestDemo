package com.shmedo.lib.device.base.md_cmd.parser.das


import com.shmedo.lib.device.base.iot_cmd.enums.IOTSensorType
import com.shmedo.lib.device.base.iot_cmd.model.das.DasExternalSensorInfo
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants
import com.shmedo.lib.device.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.device.base.md_cmd.interfaces.MDCommandParser

/**
 * 创建者：gonghe
 * 创建时间：2024/4/17
 * 描述： TODO
 */
class MDDasExternalSensorInfoParser : MDCommandParser<DasExternalSensorInfo> {
    override fun parseInstance(values: List<String>): DasExternalSensorInfo {
        val model = values[0].substring(5, 7)
        return DasExternalSensorInfo().apply {
            collectorModel = if (model.startsWith("0") && model.length > 1) model.substring(
                1
            ) else model

            addr = if (values[1].startsWith("0") && values[1].length > 1) values[1].substring(
                1
            ) else values[1]

            type = if (values[2].startsWith("0") && values[2].length > 1) values[2].substring(
                1
            ) else values[2]

            when (IOTSensorType.value(type)) {
                IOTSensorType.KANG_PERCOLATE -> {//基康渗压计
                    threshold = (values.getOrNull(3) ?: threshold).replace("nan", "0")
                    poly_a = (values.getOrNull(4) ?: poly_a).replace("nan", "0")
                    poly_b = (values.getOrNull(5) ?: poly_b).replace("nan", "0")
                    poly_c = (values.getOrNull(6) ?: poly_c).replace("nan", "0")
                    temp_k =
                        (values.getOrNull(7) ?: temp_k).replace("nan", "0")
                    temp_t0 =
                        (values.getOrNull(8) ?: temp_t0).replace("nan", "0")
                    corrval = (values.getOrNull(9) ?: corrval).replace("nan", "0")
                    ropelen = (values.getOrNull(10) ?: ropelen).replace("nan", "0")
                    tubealti =
                        (values.getOrNull(11) ?: tubealti).replace("nan", "0")
                }

                IOTSensorType.GUDAN_PERCOLATE -> {//葛南渗压计
                    threshold = (values.getOrNull(3) ?: threshold).replace("nan", "0")
                    sens_k = (values.getOrNull(4) ?: sens_k).replace("nan", "0")
                    temp_b =
                        (values.getOrNull(5) ?: temp_b).replace("nan", "0")
                    referval_f = (values.getOrNull(6) ?: referval_f).replace("nan", "0")
                    temp_t0 =
                        (values.getOrNull(7) ?: temp_t0).replace("nan", "0")
                    corrval = (values.getOrNull(8) ?: corrval).replace("nan", "0")
                    ropelen = (values.getOrNull(9) ?: ropelen).replace("nan", "0")
                    tubealti =
                        (values.getOrNull(10) ?: tubealti).replace("nan", "0")
                }

                IOTSensorType.GUDAN_SOIL_PRESSURE -> {//葛南土压力计
                    threshold = (values.getOrNull(3) ?: threshold).replace("nan", "0")
                    sens_k = (values.getOrNull(4) ?: sens_k).replace("nan", "0")
                    temp_b =
                        (values.getOrNull(5) ?: temp_b).replace("nan", "0")
                    referval_f = (values.getOrNull(6) ?: referval_f).replace("nan", "0")
                    temp_t0 =
                        (values.getOrNull(7) ?: temp_t0).replace("nan", "0")
                    corrval = (values.getOrNull(8) ?: corrval).replace("nan", "0")
                }

                IOTSensorType.GUDAN_STRESS,//葛南应力计
                IOTSensorType.GUDAN_NOT_STRESS -> {//葛南无应力计
                    threshold = (values.getOrNull(3) ?: threshold).replace("nan", "0")
                    sens_k = (values.getOrNull(4) ?: sens_k).replace("nan", "0")
                    temp_b =
                        (values.getOrNull(5) ?: temp_b).replace("nan", "0")
                    elastic_mod =
                        (values.getOrNull(6) ?: elastic_mod).replace("nan", "0")
                    referval_f = (values.getOrNull(7) ?: referval_f).replace("nan", "0")
                    temp_t0 =
                        (values.getOrNull(8) ?: temp_t0).replace("nan", "0")
                    corrval = (values.getOrNull(9) ?: corrval).replace("nan", "0")
                }

                IOTSensorType.GUDAN_DISPLACEMENT_METER -> {//葛南位移计
                    threshold = (values.getOrNull(3) ?: threshold).replace("nan", "0")
                    sens_k = (values.getOrNull(4) ?: sens_k).replace("nan", "0")
                    temp_b =
                        (values.getOrNull(5) ?: temp_b).replace("nan", "0")
                    referval_f = (values.getOrNull(6) ?: referval_f).replace("nan", "0")
                    temp_t0 =
                        (values.getOrNull(7) ?: temp_t0).replace("nan", "0")
                    corrval = (values.getOrNull(8) ?: corrval).replace("nan", "0")
                }

                IOTSensorType.JUNXING_ZLJ_300T -> {//轴力计
                    threshold = (values.getOrNull(3) ?: threshold).replace("nan", "0")
                    sens_k = (values.getOrNull(4) ?: sens_k).replace("nan", "0")
                    referval_f = (values.getOrNull(5) ?: referval_f).replace("nan", "0")
                    corrval = (values.getOrNull(6) ?: corrval).replace("nan", "0")
                    temp_b =
                        (values.getOrNull(7) ?: temp_b).replace("nan", "0")
                    temp_t0 =
                        (values.getOrNull(8) ?: temp_t0).replace("nan", "0")
                }

                IOTSensorType.INCLINOMETER -> {//固定测斜仪
                    threshold = (values.getOrNull(3) ?: threshold).replace("nan", "0")
                    spacing =
                        (values.getOrNull(4) ?: spacing).replace("nan", "0")
                    corrval = (values.getOrNull(5) ?: corrval).replace("nan", "0")
                }

                else -> {
                    threshold = (values.getOrNull(3) ?: threshold).replace("nan", "0")
                    corrval = (values.getOrNull(4) ?: corrval).replace("nan", "0")
                    model_type = IOTConstants.NULL_KEY
                    datatype = IOTConstants.NULL_KEY
                    measinval = IOTConstants.NULL_KEY
                    caddr = IOTConstants.NULL_KEY

                    when (IOTSensorType.value(type)) {
                        IOTSensorType.LUYAN_INCLINOMETER -> {//倾角仪
                            initvalx = corrval
                            initvaly = (values.getOrNull(5) ?: initvaly).replace("nan", "0")
                            initvalz = (values.getOrNull(6) ?: initvalz).replace("nan", "0")
                        }

                        IOTSensorType.WEIR -> {//量水堰计
                            lsycsds =
                                (values.getOrNull(5) ?: IOTConstants.NULL_KEY).replace("nan", "0")
                            lsyysst =
                                (values.getOrNull(6) ?: IOTConstants.NULL_KEY).replace("nan", "0")
                        }

                        IOTSensorType.STATIC_LEVEL,//静力水准
                        IOTSensorType.SEDIMENTATION_METER,//沉降仪
                        -> {
                            initval = (values.getOrNull(5) ?: initval).replace("nan", "0")
                        }

                        IOTSensorType.VERTICAL_COORDINATE,//垂线坐标仪
                        -> {
                            initvalx = corrval
                            initvaly = (values.getOrNull(5) ?: tubealti).replace("nan", "0")
                        }

                        IOTSensorType.DIGITAL_WATER_LEVEL_GAUGE -> {//数字式水位计
                            tubealti = (values.getOrNull(5) ?: tubealti).replace("nan", "0")
                            ropelen = (values.getOrNull(6) ?: ropelen).replace("nan", "0")
                        }

                        else -> {

                        }
                    }
                }
            }
        }
    }


    override fun commandType(): MDCommandType = MDCommandType.COLLECTOR_CHANNEL_SENSOR_PARAMETER
}