package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kunminx.architecture.domain.message.MutableResult
import com.kunminx.architecture.domain.message.Result
import com.shmedo.core.data.mapper.asDomain
import com.shmedo.core.data.mapper.asEntity
import com.shmedo.core.data.repository.BuiltinCommandRepository
import com.shmedo.core.model.BuiltinCommandInfo
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2025/1/13
 * 描述：内置指令选择器ViewModel
 */
class BuiltinCommandSelectorViewModel(
    private val builtinCommandRepository: BuiltinCommandRepository
) : ViewModel() {

    // 是否为选择模式
    val isSelectionMode = NonNullObservableField(false)

    // 全选状态（适配布局中的 allChecked）
    val allChecked = NonNullObservableField(false)

    // 选中数量（适配布局中的 checkedCount）
    val checkedCount = NonNullObservableField(0)

    // 内置指令列表
    private val _commandList = MutableResult<List<BuiltinCommandInfo>>()
    val commandList: Result<List<BuiltinCommandInfo>> = _commandList

    // 导入结果
    private val _importResult = MutableResult<ImportResult>()
    val importResult: Result<ImportResult> = _importResult


    init {
        initializeDefaultCommands()
        loadCommands()
    }

    /**
     * 加载所有指令
     */
    fun loadCommands() {
        viewModelScope.launch {
            try {
                val commands = builtinCommandRepository.getAllCommands()
                _commandList.postValue(commands.asDomain())

                // 加载分类
//                val categories = builtinCommandRepository.getAllCategories()
//                _categoryList.postValue(categories)

                Timber.d("加载指令成功，共 ${commands.size} 条")
            } catch (e: Exception) {
                Timber.e(e, "加载指令失败")
                _commandList.postValue(emptyList())
            }
        }
    }

    /**
     * 初始化默认指令（首次使用时）
     */
    private fun initializeDefaultCommands() {
        viewModelScope.launch {
            try {
                builtinCommandRepository.initializeDefaultCommands()
            } catch (e: Exception) {
                Timber.e(e, "初始化默认指令失败")
            }
        }
    }


    /**
     * 保存指令
     */
    fun saveCommand(command: BuiltinCommandInfo, isAdd: Boolean = true) {
        viewModelScope.launch {
            try {
                if (isAdd) {
                    // 新增
                    builtinCommandRepository.saveCommand(command.asEntity())
                } else {
                    // 更新
                    builtinCommandRepository.updateCommand(command.asEntity())
                }
                loadCommands() // 重新加载列表
                Timber.d("保存指令成功: ${command.name}")
            } catch (e: Exception) {
                Timber.e(e, "保存指令失败")
            }
        }
    }

    /**
     * 删除多个指令
     */
    fun deleteCommands(commands: List<BuiltinCommandInfo>) {
        viewModelScope.launch {
            try {
                builtinCommandRepository.deleteCommands(commands.asEntity())

                // 清除选择状态
                clearSelection()
                loadCommands() // 重新加载列表

                Timber.d("批量删除指令成功，共 ${commands.size} 条")
            } catch (e: Exception) {
                Timber.e(e, "批量删除指令失败")
            }
        }
    }

    /**
     * 删除指令
     */
    fun deleteCommand(command: BuiltinCommandInfo) {
        viewModelScope.launch {
            try {
                builtinCommandRepository.deleteCommand(command.asEntity())
                loadCommands() // 重新加载列表
                Timber.d("删除指令成功: ${command.name}")
            } catch (e: Exception) {
                Timber.e(e, "删除指令失败")
            }
        }
    }

    /**
     * 导入指令
     */
    fun importCommands(commands: List<BuiltinCommandInfo>) {
        viewModelScope.launch {
            try {
                val result = builtinCommandRepository.importCommands(commands.asEntity())
                if (result.isSuccess) {
                    _importResult.postValue(ImportResult.Success(commands.size))
                    loadCommands() // 重新加载列表
                } else {
                    _importResult.postValue(
                        ImportResult.Error(
                            result.exceptionOrNull()?.message ?: "导入失败"
                        )
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "导入指令失败")
                _importResult.postValue(ImportResult.Error(e.message ?: "导入失败"))
            }
        }
    }


    /**
     * 取消选择模式
     */
    fun cancelSelectionMode() {
        clearSelection()
    }


    /**
     * 清除选择
     */
    fun clearSelection() {
        checkedCount.set(0)
        allChecked.set(false)
        isSelectionMode.set(false)
    }


    /**
     * 导入结果密封类
     */
    sealed class ImportResult {
        data class Success(val count: Int) : ImportResult()
        data class Error(val message: String) : ImportResult()
    }
} 