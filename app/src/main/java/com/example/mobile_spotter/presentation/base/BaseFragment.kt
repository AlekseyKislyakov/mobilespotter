package com.example.mobile_spotter.presentation.base

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import com.example.mobile_spotter.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

abstract class BaseFragment(@LayoutRes layout: Int): Fragment(layout) {

    open val showBottomNavigationView: Boolean
        get() = (parentFragment as? BaseFragment)?.showBottomNavigationView ?: false

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
        setupBottomNavigationVisibility()
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

    private fun setupBottomNavigationVisibility() {
        val bottomNavigationView = activity?.findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView?.isVisible = showBottomNavigationView
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
