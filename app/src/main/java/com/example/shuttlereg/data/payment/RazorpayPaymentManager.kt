package com.example.shuttlereg.data.payment

import android.app.Activity
import com.example.shuttlereg.domain.model.PaymentData
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RazorpayPaymentManager @Inject constructor() {

    private var paymentCallback: PaymentCallback? = null

    interface PaymentCallback {
        fun onPaymentSuccess(paymentData: PaymentData)
        fun onPaymentFailure(error: String)
    }

    fun initiatePayment(
        activity: Activity,
        amount: Double,
        orderId: String,
        description: String,
        userEmail: String,
        userPhone: String,
        userName: String,
        callback: PaymentCallback
    ) {
        this.paymentCallback = callback
        
        try {
            val checkout = Checkout()
            
            // Set your Razorpay API Key
            checkout.setKeyID("rzp_test_your_key_id") // Replace with actual key
            
            val options = JSONObject()
            
            // Order details
            options.put("name", "ShuttleReg")
            options.put("description", description)
            options.put("image", "https://your-logo-url.com/logo.png") // Optional
            options.put("order_id", orderId)
            options.put("currency", "INR")
            options.put("amount", (amount * 100).toInt()) // Razorpay expects amount in paise
            
            // Customer details
            val prefill = JSONObject()
            prefill.put("email", userEmail)
            prefill.put("contact", userPhone)
            prefill.put("name", userName)
            options.put("prefill", prefill)
            
            // Theme
            val theme = JSONObject()
            theme.put("color", "#2196F3") // Primary color
            options.put("theme", theme)
            
            // Payment methods
            val method = JSONObject()
            method.put("netbanking", true)
            method.put("card", true)
            method.put("upi", true)
            method.put("wallet", true)
            options.put("method", method)
            
            // Retry settings
            val retry = JSONObject()
            retry.put("enabled", true)
            retry.put("max_count", 3)
            options.put("retry", retry)
            
            // Notes for tracking
            val notes = JSONObject()
            notes.put("purpose", "Tournament Registration")
            notes.put("user_email", userEmail)
            options.put("notes", notes)
            
            // Open Razorpay checkout
            checkout.open(activity, options)
            
        } catch (e: Exception) {
            callback.onPaymentFailure("Payment initialization failed: ${e.message}")
        }
    }

    fun handlePaymentResult(
        paymentId: String?,
        orderId: String?,
        signature: String?
    ) {
        if (paymentId != null && orderId != null) {
            // Payment successful
            val paymentData = PaymentData(
                paymentId = paymentId,
                orderId = orderId,
                amount = 0.0, // Will be set by the calling code
                currency = "INR",
                method = "razorpay"
            )
            paymentCallback?.onPaymentSuccess(paymentData)
        } else {
            paymentCallback?.onPaymentFailure("Payment verification failed")
        }
    }

    fun handlePaymentError(error: String) {
        paymentCallback?.onPaymentFailure(error)
    }
}

// Extension functions for easier integration
class RazorpayActivity : Activity(), PaymentResultListener {

    private val paymentManager = RazorpayPaymentManager()

    override fun onPaymentSuccess(razorpayPaymentID: String?) {
        paymentManager.handlePaymentResult(
            paymentId = razorpayPaymentID,
            orderId = null, // Extract from your order tracking
            signature = null
        )
    }

    override fun onPaymentError(code: Int, response: String?) {
        paymentManager.handlePaymentError(response ?: "Payment failed with code: $code")
    }
}