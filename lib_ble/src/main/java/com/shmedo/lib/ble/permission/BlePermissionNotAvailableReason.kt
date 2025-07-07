package com.shmedo.lib.ble.permission

/**
 * 创建者：gonghe
 * 创建时间：2025/7/1
 * 描述： The reason why the BLE permission is not available.
 */

/**
 * The reason why the BLE permission is not available.
 */
enum class BlePermissionNotAvailableReason {
    /** Bluetooth Scan permission is required. */
    PERMISSION_REQUIRED,
    /** Bluetooth is not available on this device. */
    NOT_AVAILABLE,
    /** Bluetooth is disabled. */
    DISABLED,
}