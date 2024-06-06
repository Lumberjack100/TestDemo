package com.shmedo.lib.device.base.md_cmd.parser.das


import com.shmedo.lib.device.base.iot_cmd.enums.IOTSensorType
import com.shmedo.lib.device.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.device.base.md_cmd.interfaces.MDCommandParser
import com.shmedo.lib.device.base.md_cmd.model.das.MDDasExternalSensorInfo

/**
 * 创建者：gonghe
 * 创建时间：2024/4/17
 * 描述： TODO
 */
class MDDasExternalSensorInfoParser : MDCommandParser<MDDasExternalSensorInfo> {
    override fun parseInstance(values: List<String>): MDDasExternalSensorInfo {
        val model = values[0].substring(5, 7)
        return MDDasExternalSensorInfo().apply {
            collectorModel = if (model.startsWith("0") && model.length > 1) model.substring(
                1
            ) else model

            sensorAddress = if (values[1].startsWith("0") && values[1].length > 1) values[1].substring(
                1
            ) else values[1]

            sensorType = if (values[2].startsWith("0") && values[2].length > 1) values[2].substring(
                1
            ) else values[2]

            when (IOTSensorType.value(sensorType)) {
                IOTSensorType.KANG_PERCOLATE -> {//基康渗压计
                    triggerThreshold = (values.getOrNull(3) ?: triggerThreshold).replace("nan", "0")
                    polynomialRatioA = (values.getOrNull(4) ?: polynomialRatioA).replace("nan", "0")
                    polynomialRatioB = (values.getOrNull(5) ?: polynomialRatioB).replace("nan", "0")
                    polynomialRatioC = (values.getOrNull(6) ?: polynomialRatioC).replace("nan", "0")
                    temperatureCoefficientK =
                        (values.getOrNull(7) ?: temperatureCoefficientK).replace("nan", "0")
                    temperatureCoefficientT0 =
                        (values.getOrNull(8) ?: temperatureCoefficientT0).replace("nan", "0")
                    correctionValue = (values.getOrNull(9) ?: correctionValue).replace("nan", "0")
                    wireRopeLength = (values.getOrNull(10) ?: wireRopeLength).replace("nan", "0")
                    installElevation =
                        (values.getOrNull(11) ?: installElevation).replace("nan", "0")
                }

                IOTSensorType.GUDAN_PERCOLATE -> {//葛南渗压计
                    triggerThreshold = (values.getOrNull(3) ?: triggerThreshold).replace("nan", "0")
                    sensitivityK = (values.getOrNull(4) ?: sensitivityK).replace("nan", "0")
                    temperatureCoefficientB =
                        (values.getOrNull(5) ?: temperatureCoefficientB).replace("nan", "0")
                    referenceValueF = (values.getOrNull(6) ?: referenceValueF).replace("nan", "0")
                    temperatureCoefficientT0 =
                        (values.getOrNull(7) ?: temperatureCoefficientT0).replace("nan", "0")
                    correctionValue = (values.getOrNull(8) ?: correctionValue).replace("nan", "0")
                    wireRopeLength = (values.getOrNull(9) ?: wireRopeLength).replace("nan", "0")
                    installElevation =
                        (values.getOrNull(10) ?: installElevation).replace("nan", "0")
                }

                IOTSensorType.GUDAN_SOIL_PRESSURE -> {//葛南土压力计
                    triggerThreshold = (values.getOrNull(3) ?: triggerThreshold).replace("nan", "0")
                    sensitivityK = (values.getOrNull(4) ?: sensitivityK).replace("nan", "0")
                    temperatureCoefficientB =
                        (values.getOrNull(5) ?: temperatureCoefficientB).replace("nan", "0")
                    referenceValueF = (values.getOrNull(6) ?: referenceValueF).replace("nan", "0")
                    temperatureCoefficientT0 =
                        (values.getOrNull(7) ?: temperatureCoefficientT0).replace("nan", "0")
                    correctionValue = (values.getOrNull(8) ?: correctionValue).replace("nan", "0")
                }

                IOTSensorType.GUDAN_STRESS,//葛南应力计
                IOTSensorType.GUDAN_NOT_STRESS -> {//葛南无应力计
                    triggerThreshold = (values.getOrNull(3) ?: triggerThreshold).replace("nan", "0")
                    sensitivityK = (values.getOrNull(4) ?: sensitivityK).replace("nan", "0")
                    temperatureCoefficientB =
                        (values.getOrNull(5) ?: temperatureCoefficientB).replace("nan", "0")
                    expansionCoefficient =
                        (values.getOrNull(6) ?: expansionCoefficient).replace("nan", "0")
                    referenceValueF = (values.getOrNull(7) ?: referenceValueF).replace("nan", "0")
                    temperatureCoefficientT0 =
                        (values.getOrNull(8) ?: temperatureCoefficientT0).replace("nan", "0")
                    correctionValue = (values.getOrNull(9) ?: correctionValue).replace("nan", "0")
                }

                IOTSensorType.GUDAN_DISPLACEMENT_METER -> {//葛南位移计
                    triggerThreshold = (values.getOrNull(3) ?: triggerThreshold).replace("nan", "0")
                    sensitivityK = (values.getOrNull(4) ?: sensitivityK).replace("nan", "0")
                    temperatureCoefficientB =
                        (values.getOrNull(5) ?: temperatureCoefficientB).replace("nan", "0")
                    referenceValueF = (values.getOrNull(6) ?: referenceValueF).replace("nan", "0")
                    temperatureCoefficientT0 =
                        (values.getOrNull(7) ?: temperatureCoefficientT0).replace("nan", "0")
                    correctionValue = (values.getOrNull(8) ?: correctionValue).replace("nan", "0")
                }

                IOTSensorType.JUNXING_ZLJ_300T -> {//轴力计
                    triggerThreshold = (values.getOrNull(3) ?: triggerThreshold).replace("nan", "0")
                    sensitivityK = (values.getOrNull(4) ?: sensitivityK).replace("nan", "0")
                    referenceValueF = (values.getOrNull(5) ?: referenceValueF).replace("nan", "0")
                    correctionValue = (values.getOrNull(6) ?: correctionValue).replace("nan", "0")
                    temperatureCoefficientB =
                        (values.getOrNull(7) ?: temperatureCoefficientB).replace("nan", "0")
                    temperatureCoefficientT0 =
                        (values.getOrNull(8) ?: temperatureCoefficientT0).replace("nan", "0")
                }

                IOTSensorType.INCLINOMETER -> {//固定测斜仪
                    triggerThreshold = (values.getOrNull(3) ?: triggerThreshold).replace("nan", "0")
                    measuringSectionLength =
                        (values.getOrNull(4) ?: measuringSectionLength).replace("nan", "0")
                    correctionValue = (values.getOrNull(5) ?: correctionValue).replace("nan", "0")
                }

                else -> {
                    triggerThreshold = (values.getOrNull(3) ?: triggerThreshold).replace("nan", "0")
                    correctionValue = (values.getOrNull(4) ?: correctionValue).replace("nan", "0")

                    if (IOTSensorType.value(sensorType) == IOTSensorType.LUYAN_INCLINOMETER) {//倾角仪
                        initvalx = (values.getOrNull(5) ?: initvalx).replace("nan", "0")
                        initvaly = (values.getOrNull(6) ?: initvaly).replace("nan", "0")
                        initvalz = (values.getOrNull(7) ?: initvalz).replace("nan", "0")
                    } else if (IOTSensorType.value(sensorType) == IOTSensorType.WEIR) {//量水堰计
                        lsycsds = (values.getOrNull(5) ?: lsycsds).replace("nan", "0")
                        lsyysst = (values.getOrNull(6) ?: lsyysst).replace("nan", "0")
                    } else if (IOTSensorType.value(sensorType) == IOTSensorType.STATIC_LEVEL) {//量水堰计

                    }

                    when (IOTSensorType.value(sensorType)) {
                        IOTSensorType.LUYAN_INCLINOMETER -> {//倾角仪
                            initvalx = (values.getOrNull(5) ?: initvalx).replace("nan", "0")
                            initvaly = (values.getOrNull(6) ?: initvaly).replace("nan", "0")
                            initvalz = (values.getOrNull(7) ?: initvalz).replace("nan", "0")
                        }
                        IOTSensorType.WEIR -> {//量水堰计
                            lsycsds = (values.getOrNull(5) ?: lsycsds).replace("nan", "0")
                            lsyysst = (values.getOrNull(6) ?: lsyysst).replace("nan", "0")
                        }
                        IOTSensorType.STATIC_LEVEL,//静力水准
                        IOTSensorType.SEDIMENTATION_METER ,//沉降仪
                        IOTSensorType.VERTICAL_COORDINATE ,//垂线坐标仪
                        -> {
                            initialValue = (values.getOrNull(5) ?: initialValue).replace("nan", "0")
                        }
                        IOTSensorType.DIGITAL_WATER_LEVEL_GAUGE -> {//数字式水位计
                            installElevation= (values.getOrNull(5) ?: installElevation).replace("nan", "0")
                            wireRopeLength= (values.getOrNull(6) ?: wireRopeLength).replace("nan", "0")
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