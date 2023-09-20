package com.shmedo.lib.ble.scanner.repository

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanCallback
import no.nordicsemi.android.support.v18.scanner.ScanResult
import no.nordicsemi.android.support.v18.scanner.ScanSettings
import timber.log.Timber

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/9/5
 *
 * 描述： TODO
 *
 *
 */
class ScannerRepository internal constructor() {
    fun getScannerState(): Flow<ScanningState> =
        callbackFlow {
            val scanCallback: ScanCallback = object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult) {
                    if (result.isConnectable) {
                        DevicesDataStore.instance.addNewDevice(result)

                        trySend(ScanningState.DevicesDiscovered(DevicesDataStore.instance.devices))
                    }
                }

                override fun onBatchScanResults(results: List<ScanResult>) {
                    val newResults = results.filter { it.isConnectable }
                    newResults.forEach {
                        DevicesDataStore.instance.addNewDevice(it)
                    }
                    if (newResults.isNotEmpty()) {
                        trySend(ScanningState.DevicesDiscovered(DevicesDataStore.instance.devices))
                    }
                }

                override fun onScanFailed(errorCode: Int) {
                    trySend(ScanningState.Error(errorCode))
                }
            }
            Timber.i("scannerViewModel Start Scanning")
            trySend(ScanningState.Loading)

            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setLegacy(false)
                .setReportDelay(500)
                .setUseHardwareBatchingIfSupported(false)
                .build()
            val scanner = BluetoothLeScannerCompat.getScanner()
            scanner.startScan(null, settings, scanCallback)

            awaitClose {
                Timber.i("scannerViewModel awaitClose")
                scanner.stopScan(scanCallback)
            }
        }

    fun clear() {
        DevicesDataStore.instance.clear()
    }

    companion object {
        val instance = ScannerRepository()
    }
}