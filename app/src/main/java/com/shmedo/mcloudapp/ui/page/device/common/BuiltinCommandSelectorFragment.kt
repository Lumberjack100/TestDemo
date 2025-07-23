package com.shmedo.mcloudapp.ui.page.device.common

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.BindingAdapter
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.core.model.BuiltinCommandInfo
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentBuiltinCommandSelectorBinding
import com.shmedo.mcloudapp.databinding.ItemBuiltinCommandBinding
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showMessage
import com.shmedo.mcloudapp.ui.dialog.BuiltinCommandEditDialog
import com.shmedo.mcloudapp.ui.page.base.fragment.BaseFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.BuiltinCommandSelectorViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.ui.widget.recyclerview.RecycleViewDivider
import com.shmedo.mcloudapp.utils.BuiltinCommandCsvParser
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.util.UUID


/**
 * 创建者：gonghe
 * 创建时间：2025/1/13
 * 描述：内置指令选择器Fragment
 */
class BuiltinCommandSelectorFragment : BaseFragment() {
    private lateinit var binding: FragmentBuiltinCommandSelectorBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: BuiltinCommandSelectorViewModel by viewModel()

    companion object {
        private const val REQUEST_CODE_IMPORT_CSV = 1001
        private const val ARG_PRODUCT_TYPE = "product_type"
        private const val ARG_COMMUNICATE_WAY = "communicate_way"
        private const val ARG_DEVICE_INFO = "device_info"
        private const val ARG_BLE_DEVICE = "ble_device"

        fun newBundleArguments(
            productType: String,
            communicateWay: String,
            deviceInfo: String,
            bleDevice: String?
        ): Bundle {
            return Bundle().apply {
                putString(ARG_PRODUCT_TYPE, productType)
                putString(ARG_COMMUNICATE_WAY, communicateWay)
                putString(ARG_DEVICE_INFO, deviceInfo)
                putString(ARG_BLE_DEVICE, bleDevice)
            }
        }
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_builtin_command_selector,
            BR.toolbarVM, toolbarViewModel
        )
            .addBindingParam(BR.stateVM, mStates)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentBuiltinCommandSelectorBinding
        binding.llToolbar.toolbar.title = "快捷发送"
        binding.llToolbar.toolbar.setNavigationOnClickListener {
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
            nav().navigateUp()
        }
        initRecyclerView()
    }

    private fun initRecyclerView() {
        binding.recyclerView.linear().setup { rv ->
            rv.addItemDecoration(
                RecycleViewDivider(
                    LinearLayoutManager.VERTICAL, ConvertUtils.dp2px(8f), ColorUtils.getColor(
                        R.color.transparent
                    )
                )
            )
            addType<BuiltinCommandInfo>(R.layout.item_builtin_command)
            onBind {
                val itemBinding = getBinding<ItemBuiltinCommandBinding>()
                val item = getModel<BuiltinCommandInfo>()

            }
            // 设置点击事件
            setupItemClickListeners()
        }
    }

    private fun BindingAdapter.setupItemClickListeners() {
        // 长按显示操作菜单
        onLongClick(R.id.item) {
            if (!toggleMode) {
                mStates.isSelectionMode.set(true)
                toggle()//触发 onToggle 回调
                setChecked(layoutPosition, true)//触发 onChecked 回调，选中当前项
            }
        }

        //切换模式事件回调
        onToggle { position, toggleMode, end ->
            //刷新列表显示选择选择框
            val info = getModel<BuiltinCommandInfo>(position)
            info.refreshCbVisibility(toggleMode)
        }

        // 复选框、发送按钮点击事件
        onClick(R.id.item, R.id.cb_select, R.id.btn_send) { viewId ->
            val info = getModel<BuiltinCommandInfo>()

            when (viewId) {
                R.id.item -> {//整体点击事件
                    //如果当前未处于选择模式下,点击事件处理
                    if (!toggleMode && viewId == R.id.item) {
                        showEditDialog(info) // 编辑指令
                        return@onClick
                    }

                    //如果当前处于选择模式下,点击事件处理
                    setChecked(layoutPosition, !info.checked)
                }

                R.id.cb_select -> {//复选框点击事件
                    setChecked(layoutPosition, !info.checked)
                }

                R.id.btn_send -> {//发送按钮点击事件
                    selectCommand(info)
                }
            }
        }

        //条目选中事件回调
        onChecked { position, checked, allChecked ->
            val info = getModel<BuiltinCommandInfo>(position)
            info.refreshChecked(checked)

            //刷新已选择计数器
            mStates.checkedCount.set(checkedCount)
            mStates.allChecked.set(allChecked)
        }
    }

    override fun createObserver() {
        super.createObserver()
        observeViewModel()
    }

    private fun observeViewModel() {
        // 监听指令列表变化
        mStates.commandList.observe(viewLifecycleOwner) { commands ->
            binding.recyclerView.models = commands
//            handleEmptyState(commands.isEmpty())
        }

        // 监听导入结果
        mStates.importResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is BuiltinCommandSelectorViewModel.ImportResult.Success -> {
                    showMessage("导入成功，共导入 ${result.count} 条指令")
                }

                is BuiltinCommandSelectorViewModel.ImportResult.Error -> {
                    showMessage("导入失败：${result.message}")
                }
            }
        }
    }

    inner class ClickProxy : BaseClickProxy() {
        fun onMenuClick() {
            importFromCSV()
        }

        // 添加新指令
        fun onAddClick() {
            showEditDialog(null)
        }

        // 全选/取消全选
        fun onSelectAllClick() {
            binding.recyclerView.bindingAdapter.checkedAll(mStates.allChecked.get())
        }

        // 删除选中的指令
        fun onDeleteSelectedClick() {
            val checkedList =
                binding.recyclerView.bindingAdapter.getCheckedModels<BuiltinCommandInfo>()
            if (checkedList.isEmpty()) {
                showMessage("请先选择要删除的指令")
                return
            }
            mStates.deleteCommands(checkedList)
            resetUnCheckMode()
        }

        // 取消选择模式
        fun onCancelClick() {
            mStates.cancelSelectionMode()
            resetUnCheckMode()
        }
    }

    private fun resetUnCheckMode() {
        binding.recyclerView.bindingAdapter.toggle(false)
        binding.recyclerView.bindingAdapter.checkedAll(false)
    }

    /**
     * 选择指令并返回结果
     */
    private fun selectCommand(command: BuiltinCommandInfo) {
        val resultBundle = Bundle().apply {
            putString("command_content", command.content)
            putString("command_name", command.name)
        }
        setFragmentResult(
            BleCustomCommandLogPrintFragment.FRAGMENT_BUILTIN_COMMAND_SELECTED_REQUEST_KEY,
            resultBundle
        )
        Toaster.show("指令已发送")
//        nav().navigateUp()
    }

    /**
     * 显示编辑弹框
     */
    private fun showEditDialog(command: BuiltinCommandInfo?) {
        val popupView = BuiltinCommandEditDialog(
            context = requireContext(),
            command = command,
            onSave = { editedCommand ->
                if (command == null) {
                    // 新增
                    val newCommand = editedCommand.copy(
                        id = UUID.randomUUID().toString(),
                        createTime = System.currentTimeMillis(),
                        updateTime = System.currentTimeMillis()
                    )
                    mStates.saveCommand(newCommand)
                } else {
                    // 更新
                    val updatedCommand = editedCommand.copy(
                        updateTime = System.currentTimeMillis()
                    )
                    mStates.saveCommand(updatedCommand, false)
                }
            }
        )
        XPopup.Builder(context)
            .dismissOnBackPressed(false) // 按返回键是否关闭弹窗，默认为true
            .dismissOnTouchOutside(false)// 点击外部是否关闭弹窗，默认为true
            .enableDrag(false)
            .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
            .asCustom(popupView)
            .show()
    }

    /**
     * 导入CSV文件
     */
    private fun importFromCSV() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "text/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("text/csv", "text/plain"))
        }

        try {
            startActivityForResult(intent, REQUEST_CODE_IMPORT_CSV)
        } catch (e: Exception) {
            showMessage("无法打开文件选择器")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_IMPORT_CSV && resultCode == android.app.Activity.RESULT_OK) {
            data?.data?.let { uri ->
                importCSVFile(uri)
            }
        }
    }

    /**
     * 导入CSV文件
     */
    private fun importCSVFile(uri: Uri) {
        lifecycleScope.launch {
            try {
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                if (inputStream == null) {
                    showMessage("无法读取文件")
                    return@launch
                }

                val commands = BuiltinCommandCsvParser.parseFromInputStream(inputStream)
                if (commands.isEmpty()) {
                    showMessage("文件中没有有效的指令数据")
                    return@launch
                }

                // 导入指令
                mStates.importCommands(commands)

            } catch (e: Exception) {
                Timber.e(e, "导入CSV文件失败")
                showMessage("导入失败：${e.message}")
            }
        }
    }

    /**
     * 处理空状态
     */
    private fun handleEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            showMessage("暂无内置指令，点击右下角+号添加或右上角导入")
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar, isKeyboardEnable = true)
    }
} 