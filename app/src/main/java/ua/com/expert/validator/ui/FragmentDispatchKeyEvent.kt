package ua.com.expert.validator.ui

import android.view.KeyEvent

interface FragmentDispatchKeyEvent {

    fun dispatchKeyEvent(event: KeyEvent): Boolean

}