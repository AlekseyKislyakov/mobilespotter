package com.example.mobile_spotter.presentation.base

import android.app.Dialog
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.view.KeyEvent.KEYCODE_ENTER
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.LayoutRes
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import com.example.mobile_spotter.R
import com.example.mobile_spotter.ext.KeyboardShowListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.view_rfid_dialog.view.*
import kotlinx.coroutines.*


abstract class BaseFragment(@LayoutRes layout: Int) : Fragment(layout), KeyboardShowListener {

    private var readRfidCardDialog: Dialog? = null

    open val showFloatingActionButton: Boolean
        get() = (parentFragment as? BaseFragment)?.showFloatingActionButton ?: false


    // private val plugins = mutableListOf<BasePlugin>()

//    override fun onAttach(context: Context) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            AndroidSupportInjection.inject(this)
//        }
//        super.onAttach(context)
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // initPlugins()
        super.onCreate(savedInstanceState)
        // callOperations()
    }

    override fun onResume() {
        // dispatchEventToPlugins(LifecycleEvent.OnResume)
        super.onResume()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // Timber.d("onActivityCreated $this")
        // dispatchEventToPlugins(LifecycleEvent.OnActivityCreated(savedInstanceState))
        this.setKeyboardListener()
        setupRFIDButtonVisibility()
        onSetupLayout(savedInstanceState)
        onBindViewModel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //dispatchEventToPlugins(LifecycleEvent.OnViewCreated(view, savedInstanceState))
    }

    override fun onPause() {
        //dispatchEventToPlugins(LifecycleEvent.OnPause)
        super.onPause()
    }

    override fun onStart() {
        super.onStart()
        //dispatchEventToPlugins(LifecycleEvent.OnStart)
    }

    override fun onStop() {
        //dispatchEventToPlugins(LifecycleEvent.OnStop(this))
        super.onStop()
    }

    override fun onDestroyView() {
        //dispatchEventToPlugins(LifecycleEvent.OnDestroyView(this))
        //Timber.d("onDestroyView $this")
        super.onDestroyView()
    }

    override fun onDestroy() {
        //dispatchEventToPlugins(LifecycleEvent.OnDestroy)
        //Timber.d("onDestroy $this")
        //releasePlugins()
        super.onDestroy()
    }

    /**
     * Вызывать методы вью модели, которые получают данные из репозиториев
     */
    abstract fun callOperations()

    /**
     * В этом методе производить все настройки вью фрагмента
     * установка OnClickListener, RecyclerView.Adapter/LayoutManager, etc
     */
    abstract fun onSetupLayout(savedInstanceState: Bundle?)

    /**
     * В этом методе производить подписки на изменения значений в лайвдатах у ViewModel
     */
    abstract fun onBindViewModel()

    abstract fun onCodeRecognized(code: String)

    private fun setupRFIDButtonVisibility() {
        val fab = activity?.findViewById<FloatingActionButton>(R.id.fab)
        fab?.isVisible = showFloatingActionButton
        fab?.setOnClickListener {
            createChooseResolutionDialog()?.show()
        }
    }

    fun hideBottomNavigation() {
//        val bottomNavigationView = activity?.findViewById<BottomNavigationView>(R.id.bottomNavigationView)
//        bottomNavigationView?.isVisible = false
    }

    fun showBottomNavigation() {
//        val bottomNavigationView = activity?.findViewById<BottomNavigationView>(R.id.bottomNavigationView)
//        bottomNavigationView?.isVisible = true
    }


    override fun onKeyboardHeightChanged(value: Int) {
        if (value != -1) {
            hideBottomNavigation()
        } else {
            showBottomNavigation()
        }
    }

    private fun createChooseResolutionDialog(): Dialog? {
        activity?.let { context ->
            if (readRfidCardDialog == null) {
                val rfidDialogView = LayoutInflater.from(context)
                        .inflate(R.layout.view_rfid_dialog, null)

                with(rfidDialogView) {
                    editTextRfidListener.requestFocus()
                    editTextRfidListener.setOnKeyListener { view, i, keyEvent ->
                        if (keyEvent.keyCode == KEYCODE_ENTER && editTextRfidListener.text.toString().trim().isNotEmpty()) {
                            onCodeRecognized(editTextRfidListener.text.toString().trim())
                            editTextRfidListener.setText("")
                            val anim = (imageViewRfidIcon.drawable as TransitionDrawable)
                                    anim.startTransition(100)

                            GlobalScope.launch {
                                delay(500)
                                withContext(Dispatchers.Main) {
                                    anim.resetTransition()
                                    readRfidCardDialog?.dismiss()
                                }
                            }
                        }

                        false
                    }
                    buttonDiscard.setOnClickListener {
                        readRfidCardDialog?.dismiss()
                    }

                    readRfidCardDialog = Dialog(context, R.style.DialogTheme).apply {
                        setCancelable(true)
                        setCanceledOnTouchOutside(true)
                        setContentView(rfidDialogView)
                    }
                }
            }
        }

        return readRfidCardDialog
    }

    // region Plugins
//    @CallSuper
//    protected open fun initPlugins() {
//        addPlugin(HideKeyboardPlugin())
//        addPlugin(StatusBarIconColorsPlugin(requireActivity(), this))
//    }

//    protected fun addPlugin(plugin: BasePlugin) {
//        plugins.add(plugin)
//    }
//
//    private fun dispatchEventToPlugins(event: LifecycleEvent) {
//        plugins.forEach { it.onLifecycleEvent(event) }
//    }
//
//    private fun releasePlugins() {
//        plugins.clear()
//    }

    // end region Plugins

    protected infix fun <T> LiveData<T>.observe(block: (T) -> Unit) {
        observe(this@BaseFragment.viewLifecycleOwner, { t -> block.invoke(t) })
    }

}
