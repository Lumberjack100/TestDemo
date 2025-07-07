package com.shmedo.lib.ble.communicate.spec

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import java.util.UUID

/**
 * 蓝牙设备规格基类
 * 统一管理不同设备的服务和特征值 UUID
 */
sealed class BleDeviceSpec(
    val deviceName: String,
    val serviceUuid: UUID,
    val notifyCharacteristicUuid: UUID,
    val writeCharacteristicUuid: UUID
) {

    //WRITE、WRITE_NO_RESPONSE
    object ESP32A : BleDeviceSpec(
        deviceName = "ESP32A",
        serviceUuid = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb"),
        notifyCharacteristicUuid = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb"),
        writeCharacteristicUuid = UUID.fromString("0000fff3-0000-1000-8000-00805f9b34fb")
    )

    object ESP32B : BleDeviceSpec(
        deviceName = "ESP32B",
        serviceUuid = UUID.fromString("0000a002-0000-1000-8000-00805f9b34fb"),
        notifyCharacteristicUuid = UUID.fromString("0000c305-0000-1000-8000-00805f9b34fb"),
        writeCharacteristicUuid = UUID.fromString("0000c303-0000-1000-8000-00805f9b34fb")
    )

    object GOC400 : BleDeviceSpec(
        deviceName = "GOC400",
        serviceUuid = UUID.fromString("0000ff00-0000-1000-8000-00805f9b34fb"),
        notifyCharacteristicUuid = UUID.fromString("0000ff01-0000-1000-8000-00805f9b34fb"),
        writeCharacteristicUuid = UUID.fromString("0000ff02-0000-1000-8000-00805f9b34fb")
    )

    object GOCW91200 : BleDeviceSpec(
        deviceName = "GOCW91200",
        serviceUuid = UUID.fromString("00001910-0000-1000-8000-00805f9b34fb"),
        notifyCharacteristicUuid = UUID.fromString("0000fff5-0000-1000-8000-00805f9b34fb"),
        writeCharacteristicUuid = UUID.fromString("0000fff4-0000-1000-8000-00805f9b34fb")
    )

    //WRITE、WRITE_NO_RESPONSE
    object MS52SF1 : BleDeviceSpec(
        deviceName = "MS52SF1",
        serviceUuid = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb"),
        notifyCharacteristicUuid = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb"),
        writeCharacteristicUuid = UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb")
    )

    //济南有人物联网公司低功耗蓝牙模块服务和特征值  WRITE、WRITE_NO_RESPONSE
    object USR : BleDeviceSpec(
        deviceName = "USR",
        serviceUuid = UUID.fromString("0003cdd0-0000-1000-8000-00805f9b0131"),
        notifyCharacteristicUuid = UUID.fromString("0003cdd1-0000-1000-8000-00805f9b0131"),
        writeCharacteristicUuid = UUID.fromString("0003cdd2-0000-1000-8000-00805f9b0131")
    )

    /**
     * 验证设备是否支持写操作
     */
    fun isWriteSupported(characteristic: BluetoothGattCharacteristic?): Boolean {
        characteristic ?: return false
        val properties = characteristic.properties
        return (properties and BluetoothGattCharacteristic.PROPERTY_WRITE > 0) ||
                (properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE > 0)
    }
}

/**
 * 设备发现结果
 */
data class DeviceDiscoveryResult(
    val spec: BleDeviceSpec,
    val notifyCharacteristic: BluetoothGattCharacteristic,
    val writeCharacteristic: BluetoothGattCharacteristic
)

/**
 * 蓝牙设备规格管理器
 * 负责识别和管理不同类型的蓝牙设备
 */
class BleDeviceSpecManager {

    companion object {
        private val ALL_SPECS = listOf(
            BleDeviceSpec.ESP32A,
            BleDeviceSpec.ESP32B,
            BleDeviceSpec.GOC400,
            BleDeviceSpec.GOCW91200,
            BleDeviceSpec.MS52SF1,
            BleDeviceSpec.USR
        )
    }

    /**
     * 识别设备并返回相应的规格和特征值
     *
     * @param gatt 蓝牙GATT连接
     * @return 设备发现结果，如果未找到支持的设备则返回null
     */
    fun identifyDevice(gatt: BluetoothGatt): DeviceDiscoveryResult? {
        for (spec in ALL_SPECS) {
            val service = gatt.getService(spec.serviceUuid) ?: continue

            val notifyCharacteristic = service.getCharacteristic(spec.notifyCharacteristicUuid)
            var writeCharacteristic = service.getCharacteristic(spec.writeCharacteristicUuid)

            // 特殊处理：ESP32A和MS52SF1共享相同的服务UUID但写特征值不同
            if (spec == BleDeviceSpec.ESP32A && writeCharacteristic == null) {
                writeCharacteristic =
                    service.getCharacteristic(BleDeviceSpec.MS52SF1.writeCharacteristicUuid)
            }

            if (notifyCharacteristic != null && writeCharacteristic != null && spec.isWriteSupported(
                    writeCharacteristic
                )
            ) {
                return DeviceDiscoveryResult(spec, notifyCharacteristic, writeCharacteristic)
            }
        }

        return null
    }

    /**
     * 获取所有支持的设备规格
     */
    fun getAllSpecs(): List<BleDeviceSpec> = ALL_SPECS.toList()
} 