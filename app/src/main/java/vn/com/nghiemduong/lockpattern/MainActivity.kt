package vn.com.nghiemduong.lockpattern

import android.annotation.SuppressLint
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import vn.com.nghiemduong.lockpattern.customview.LockPatternView
import vn.com.nghiemduong.lockpattern.utils.PrefUtils

class MainActivity : AppCompatActivity() {

    private lateinit var lpvLogin: LockPatternView
    private lateinit var tvClear: TextView
    private lateinit var tvAction: TextView
    private lateinit var tvSuggestDraw: TextView
    private lateinit var tvScreenLock: TextView
    private var mPassword = ""
    private var mPasswordAgain = ""
    private var mSate = -1
    private lateinit var mToast: Toast

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        init()
        onEvents()
    }

    private fun init() {
        mPassword = PrefUtils.fetchLockPattern(this)
        if (mPassword.isNotEmpty()) { // Có password rồi
            tvClear.visibility = View.INVISIBLE
            tvAction.visibility = View.INVISIBLE
            mSate = LockPatternView.UNLOCK_PASSWORD
            tvSuggestDraw.text = getString(R.string.draw_pattern_to_unlock)
            tvScreenLock.text = getString(R.string.screen_lock)
        } else { // null chưa có password
            tvClear.visibility = View.INVISIBLE
            tvAction.text = getString(R.string.next)
            tvAction.visibility = View.INVISIBLE
            tvSuggestDraw.text = getString(R.string.draw_an_unlock_pattern)
            tvScreenLock.text = getString(R.string.set_screen_lock)
            mSate = LockPatternView.SET_PASSWORD
        }
    }

    private fun onEvents() {
        lpvLogin.setOnClickPasswordListener(object : LockPatternView.OnClickPasswordListener {
            override fun onPassword(password: String) {
                when (mSate) {
                    LockPatternView.SET_PASSWORD -> {
                        if (password.length > 3) {
                            mPasswordAgain = password
                            lpvLogin.redrawPassword(LockPatternView.SET_PASSWORD)
                            tvAction.visibility = View.VISIBLE
                            tvClear.visibility = View.VISIBLE
                            lpvLogin.isClickAction = false
                        } else {
                            lpvLogin.clear()
                            mToast = Toast.makeText(
                                applicationContext, "Connect at least 4 dots", Toast.LENGTH_SHORT
                            )
                            mToast.show()
                        }
                    }

                    LockPatternView.CONFIRM_PASSWORD -> {
                        if (mPasswordAgain == password) {
                            // password again chính xác
                            lpvLogin.redrawPassword(LockPatternView.SUCCESS)
                            tvAction.visibility = View.VISIBLE
                            lpvLogin.isClickAction = false
                            tvClear.visibility = View.VISIBLE
                        } else {
                            // password nhập lại ko chính xác
                            lpvLogin.redrawPassword(LockPatternView.FAIL)
                            lpvLogin.isClickAction = false
                            tvAction.visibility = View.INVISIBLE
                            tvClear.visibility = View.VISIBLE
                            mToast = Toast.makeText(
                                this@MainActivity, "Password again incorrect",
                                Toast.LENGTH_SHORT
                            )
                            mToast.show()
                        }
                    }

                    LockPatternView.UNLOCK_PASSWORD -> {
                        if (mPassword == password) {
                            lpvLogin.redrawPassword(LockPatternView.SUCCESS)
                            tvSuggestDraw.text = getString(R.string.correct_password)
                        } else {
                            lpvLogin.redrawPassword(LockPatternView.FAIL)
                            tvSuggestDraw.text = getString(R.string.incorrect_password)
                        }
                    }
                }
            }
        })

        tvClear.setOnClickListener {
            when (mSate) {
                LockPatternView.SET_PASSWORD -> {
                    mPasswordAgain = ""
                }
            }

            lpvLogin.clear()
            tvClear.visibility = View.INVISIBLE
            tvAction.visibility = View.INVISIBLE
            lpvLogin.isClickAction = true
        }

        tvAction.setOnClickListener {
            when (tvAction.text) {
                getString(R.string.next) -> {
                    lpvLogin.clear()
                    tvClear.visibility = View.INVISIBLE
                    tvAction.visibility = View.INVISIBLE
                    tvAction.text = getString(R.string.confirm)
                    tvSuggestDraw.text = getString(R.string.draw_pattern_again_to_confirm)
                    mSate = LockPatternView.CONFIRM_PASSWORD
                    lpvLogin.isClickAction = true
                }

                getString(R.string.confirm) -> {
                    mPassword = mPasswordAgain
                    lpvLogin.clear()
                    PrefUtils.saveLockPattern(mPasswordAgain, this)
                    tvClear.visibility = View.INVISIBLE
                    tvAction.visibility = View.INVISIBLE
                    tvSuggestDraw.text = getString(R.string.draw_an_unlock_pattern)
                    tvScreenLock.text = getString(R.string.screen_lock)
                    mSate = LockPatternView.UNLOCK_PASSWORD
                    lpvLogin.isClickAction = true
                }
            }
        }
    }

    private fun initViews() {
        lpvLogin = findViewById(R.id.lpvLogin)
        tvClear = findViewById(R.id.tvClear)
        tvAction = findViewById(R.id.tvAction)
        tvSuggestDraw = findViewById(R.id.tvSuggestDraw)
        tvScreenLock = findViewById(R.id.tvScreenLock)
    }
}