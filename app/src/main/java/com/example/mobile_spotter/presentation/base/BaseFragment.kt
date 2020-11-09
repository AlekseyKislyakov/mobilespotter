package com.example.mobile_spotter.presentation.base

import android.app.Dialog
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.view.KeyEvent.KEYCODE_ENTER
import android.view.LayoutInflater
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

    var initialized = false

    open val showFloatingActionButton: Boolean
        get() = (parentFragment as? BaseFragment)?.showFloatingActionButton ?: false

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        this.setKeyboardListener()
        setupRFIDButtonVisibility()
        onSetupLayout(savedInstanceState)
        onBindViewModel()

        if(!initialized) {
            observeOperations()
            initialized = true
        }
    }

    /**
     * For observing livedata values from viewmodel
     */
    abstract fun observeOperations()

    /**
     * Layout, OnClickListener, RecyclerView.Adapter/LayoutManager, etc handling
     */
    abstract fun onSetupLayout(savedInstanceState: Bundle?)

    /**
     * livedata/viewmodel operations handling у ViewModel
     */
    abstract fun onBindViewModel()

    abstract fun onCodeRecognized(code: String)

    private fun setupRFIDButtonVisibility() {
        val fab = activity?.findViewById<FloatingActionButton>(R.id.fab)
        fab?.isVisible = showFloatingActionButton
        fab?.setOnClickListener {
            createRFIDDialog()?.show()
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

    private fun createRFIDDialog(): Dialog? {
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

    protected infix fun <T> LiveData<T>.observe(block: (T) -> Unit) {
        observe(this@BaseFragment.viewLifecycleOwner, { t -> block.invoke(t) })
    }

}
