package com.example.mobile_spotter.presentation.base

import android.app.Dialog
import android.content.res.Configuration
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
import com.example.mobile_spotter.ext.dpToPx
import com.example.mobile_spotter.ext.setMargins
import com.example.mobile_spotter.ext.showSnackbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.view_rfid_dialog.*
import kotlinx.android.synthetic.main.view_rfid_dialog.view.*
import kotlinx.coroutines.*


abstract class BaseFragment(@LayoutRes layout: Int) : Fragment(layout), KeyboardShowListener {

    private var readRfidCardDialog: Dialog? = null
    private var rfidDialogView: View? = null

    var initialized = false

    open val showFloatingActionButton: Boolean
        get() = (parentFragment as? BaseFragment)?.showFloatingActionButton == true

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        this.setKeyboardListener()
        setupRFIDButtonVisibility()
        setRFIDButtonElevation()
        onSetupLayout(savedInstanceState)
        onBindViewModel()

        if (!initialized) {
            observeOperations()
            initialized = true
        }
    }

    open fun setRFIDButtonElevation(value: Int = 16) {
        val fab = activity?.findViewById<FloatingActionButton>(R.id.fab)
        fab?.setMargins(16, 16, 16, value)
    }

    abstract fun logoutTimerEvent()

    /**
     * For observing livedata values from viewmodel
     */
    abstract fun observeOperations()

    /**
     * Layout, OnClickListener, RecyclerView.Adapter/LayoutManager, etc handling
     */
    abstract fun onSetupLayout(savedInstanceState: Bundle?)

    /**
     * livedata/viewmodel operations handling
     */
    abstract fun onBindViewModel()

    abstract fun onCodeRecognized(code: String)

    private fun setupRFIDButtonVisibility() {
        val fab = activity?.findViewById<FloatingActionButton>(R.id.fab)
        fab?.isVisible = showFloatingActionButton && checkKeyboardAvailability()
        fab?.setOnClickListener {
            createRFIDDialog()?.show()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setupRFIDButtonVisibility()
    }

    override fun onDestroy() {
        super.onDestroy()
        readRfidCardDialog?.dismiss()
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

    private fun checkKeyboardAvailability(): Boolean {
        return activity?.resources?.configuration?.keyboard == Configuration.KEYBOARD_QWERTY
    }

    open fun showRFIDMessage(message: String) {
        rfidDialogView?.let {
            it.dialogMessage.text = message
            GlobalScope.launch {
                delay(2500)
                withContext(Dispatchers.Main) {
                    it.dialogMessage.text = ""
                    // readRfidCardDialog?.dismiss()
                }
            }
        }
    }

    private fun createRFIDDialog(): Dialog? {
        activity?.let { context ->
            if (readRfidCardDialog == null) {
                rfidDialogView = LayoutInflater.from(context)
                    .inflate(R.layout.view_rfid_dialog, null)
                rfidDialogView?.let {
                    with(it) {
                        editTextRfidListener.requestFocus()
                        editTextRfidListener.setOnKeyListener { view, i, keyEvent ->
                            if (keyEvent.keyCode == KEYCODE_ENTER && editTextRfidListener.text.toString().trim().isNotEmpty()) {
                                onCodeRecognized(editTextRfidListener.text.toString().trim())
                                editTextRfidListener.setText("")
                                val anim = (imageViewRfidIcon.drawable as TransitionDrawable)
                                anim.startTransition(100)

                                GlobalScope.launch {
                                    delay(2500)
                                    withContext(Dispatchers.Main) {
                                        anim.resetTransition()
                                        // readRfidCardDialog?.dismiss()
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
                            setContentView(it)
                        }
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
