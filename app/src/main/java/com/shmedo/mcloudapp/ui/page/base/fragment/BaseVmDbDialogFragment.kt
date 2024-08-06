package com.shmedo.mcloudapp.ui.page.base.fragment

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.mcloudapp.R


/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2022/11/28 <br></br>
 * 描述：     TODO
 */
abstract class BaseVmDbDialogFragment : DialogFragment() {
    protected lateinit var mActivity: AppCompatActivity

    //该类绑定的ViewDataBinding
    private var _binding: ViewDataBinding? = null
    val mDatabind: ViewDataBinding get() = _binding!!

    //是否第一次加载
    protected var isFirst = true

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = context as AppCompatActivity
    }

    protected abstract val dataBindingConfig: DataBindingConfig

    /**
     * 初始化view
     */
    protected abstract fun initView(savedInstanceState: Bundle?)

    /**
     * 创建观察者
     */
    protected fun createObserver() {}

    /**
     * Fragment执行onCreate后触发的方法
     */
    protected open fun initData() {}

    protected open fun setWindowStyle(gravity: Int = Gravity.BOTTOM) {
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.window?.let {
            //无标题  必须放在setContextView之前调用
            it.requestFeature(Window.FEATURE_NO_TITLE)
            //Sets whether this dialog is cancelable with the BACK key.
            isCancelable = false
            it.setWindowAnimations(R.style.DialogFragmentAnimation)
            it.setBackgroundDrawableResource(android.R.color.transparent)
            it.decorView.setPadding(0, 0, 0, 0)
            val wlp = it.attributes
            wlp.gravity = gravity
            wlp.width = WindowManager.LayoutParams.MATCH_PARENT
            wlp.height = WindowManager.LayoutParams.WRAP_CONTENT
            it.attributes = wlp
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setWindowStyle()
        _binding = DataBindingUtil.inflate<ViewDataBinding>(
            inflater,
            dataBindingConfig.layout,
            container,
            false
        )
        mDatabind.lifecycleOwner = viewLifecycleOwner
        mDatabind.setVariable(dataBindingConfig.vmVariableId, dataBindingConfig.stateViewModel)
        val bindingParams = dataBindingConfig.bindingParams
        var i = 0
        while (i < bindingParams.size()) {
            mDatabind.setVariable(bindingParams.keyAt(i), bindingParams.valueAt(i))
            i++
        }
        return mDatabind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(savedInstanceState)
        createObserver()
        initData()
    }

    override fun onPause() {
        super.onPause()
        if (isFirst) {
            isFirst = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dismissListener?.onDismiss(dialog)
    }

    interface OnDialogFragmentDismissListener {
        /**
         * This method will be invoked when the dialog is dismissed.
         *
         * @param dialog the dialog that was dismissed will be passed into the
         * method
         */
        fun onDismiss(dialog: DialogInterface)
    }

    private var dismissListener: OnDialogFragmentDismissListener? = null
    fun setOnDialogFragmentDismissListener(listener: OnDialogFragmentDismissListener?) {
        dismissListener = listener
    }
}
