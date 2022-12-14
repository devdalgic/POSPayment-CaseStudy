package com.bano.pospaymentcasestudy.base

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.bano.pospaymentcasestudy.api.OSYService
import com.bano.pospaymentcasestudy.components.DaggerAppComponent
import com.bano.pospaymentcasestudy.db.payment.PaymentRepository
import com.bano.pospaymentcasestudy.modules.DatabaseModule
import com.bano.pospaymentcasestudy.modules.NetworkModule
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import javax.inject.Inject

/**
 * Base view model of the application. Has OSYService and PaymentRepository injected.
 * Can create QRBitmap and makes it possible to observe variables for once
 */
abstract class BaseViewModel : ViewModel() {
    @Inject
    lateinit var osyService: OSYService

    @Inject
    lateinit var paymentRepository: PaymentRepository

    /**
     * Inject necessary variables
     */
    open fun inject(appContext: Context) {
        DaggerAppComponent.builder()
            .networkModule(NetworkModule()).databaseModule(DatabaseModule(appContext))
            .build().inject(this)
    }

    /**
     * Get Bitmap from QR string
     */
    fun getQRBitmap(string: String): Bitmap {
        val size = 512 //pixels
        val bits = QRCodeWriter().encode(string, BarcodeFormat.QR_CODE, size, size)
        return Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565).also {
            for (x in 0 until size) {
                for (y in 0 until size) {
                    it.setPixel(x, y, if (bits[x, y]) Color.BLACK else Color.WHITE)
                }
            }
        }
    }
}

/**
 * Observe a LiveData for once.
 */
fun <T> MutableLiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
    observe(lifecycleOwner, object : Observer<T> {
        override fun onChanged(t: T?) {
            observer.onChanged(t)
            removeObserver(this)
        }
    })
}