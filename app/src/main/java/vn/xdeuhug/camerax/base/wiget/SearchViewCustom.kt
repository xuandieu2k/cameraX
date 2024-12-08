package vn.xdeuhug.camerax.base.wiget

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import androidx.appcompat.widget.SearchView
import java.util.Timer
import java.util.TimerTask

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 27 / 10 / 2024
 */
class SearchViewCustom: SearchView {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    fun queryAfterTextChanged(
        delay: Long = 500,
        onTextChangedDelayed: (text: String) -> Unit
    ) = doOnQueryTextListener(delay, onTextChangedDelayed)

    /**
     * Add an action which will be invoked after the text changed.
     *
     * @return the [SearchView.OnQueryTextListener] added to the [SearchView]
     */
    fun doOnQueryTextListener(
        delay: Long,
        onTextChangedDelayed: (text: String) -> Unit
    ): OnQueryTextListener {
        val queryListener = object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                handlerPostDelayed(delay) { onTextChangedDelayed.invoke(newText ?: "") }
                return true
            }
        }
        this.setOnQueryTextListener(queryListener)
        return queryListener
    }

    fun doOnSubmitTextListener(
        searchView: SearchView, delay: Long, onTextChangedDelayed: (text: String) -> Unit
    ): OnQueryTextListener {
        val queryListener = object : OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                handlerPostDelayed(delay) { onTextChangedDelayed.invoke(newText ?: "") }
                return true
            }
        }
        this.setOnQueryTextListener(queryListener)
        return queryListener
    }


    private var handlerDelayTimer: Timer = Timer()

    fun handlerPostDelayed(delay: Long, onSuccess: () -> Unit) {
        handlerDelayTimer.cancel()
        handlerDelayTimer = Timer()
        handlerDelayTimer.schedule(object : TimerTask() {
            override fun run() {
                Handler(Looper.getMainLooper()).post {
                    onSuccess.invoke()
                }
            }
        }, delay)
    }

    fun doOnQueryTextListener(
        delay: Long,
        onQueryTextListener: OnQueryTextListener
    ) {
        val queryListener = object : OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                onQueryTextListener.onQueryTextSubmit(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                handlerPostDelayed(delay) { onQueryTextListener.onQueryTextChange(newText) }
                return true
            }
        }
        this.setOnQueryTextListener(queryListener)
    }
}