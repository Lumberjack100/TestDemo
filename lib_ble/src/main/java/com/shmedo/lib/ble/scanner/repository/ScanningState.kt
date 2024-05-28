package com.shmedo.lib.ble.scanner.repository

import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/9/5
 *
 * 描述： TODO
 *
 *
 */
sealed class ScanningState {

    data object Loading : ScanningState()

    data class Error(val errorCode: Int) : ScanningState()

    data class DevicesDiscovered(val devices: List<DiscoveredBluetoothDevice>) : ScanningState() {
        val bonded: List<DiscoveredBluetoothDevice> = devices.filter { it.isBonded }

        val notBonded: List<DiscoveredBluetoothDevice> = devices.filter { !it.isBonded }

        fun size(): Int = bonded.size + notBonded.size

        fun isEmpty(): Boolean = devices.isEmpty()
    }

    fun isRunning(): Boolean {
        return this is Loading || this is DevicesDiscovered
    }
}