package com.example.data.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Client factory and payload builder utilities for PayPal Commerce Platform (PPCP) integration.
 */
object PayPalApiClient {

    const val SANDBOX_BASE_URL = "https://api-m.sandbox.paypal.com/"
    const val LIVE_BASE_URL = "https://api-m.paypal.com/"

    /**
     * Build a configured Moshi instance
     */
    val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    /**
     * Create an OkHttpClient configured for PayPal API requests.
     *
     * @param authTokenProvider Optional lambda returning the current Bearer token if known
     * @param partnerAttributionId Optional BN code for the marketplace platform
     */
    fun createOkHttpClient(
        authTokenProvider: (() -> String?)? = null,
        partnerAttributionId: String? = "DevMarket_Platform_Escrow"
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val headerInterceptor = Interceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")

            partnerAttributionId?.let {
                requestBuilder.header("PayPal-Partner-Attribution-Id", it)
            }

            val token = authTokenProvider?.invoke()
            if (!token.isNullOrBlank() && original.header("Authorization") == null) {
                requestBuilder.header("Authorization", "Bearer $token")
            }

            chain.proceed(requestBuilder.build())
        }

        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(headerInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    /**
     * Create an instance of [PayPalApiService]
     *
     * @param baseUrl Defaults to PayPal Sandbox environment
     * @param client Custom OkHttpClient (defaults to [createOkHttpClient])
     */
    fun createService(
        baseUrl: String = SANDBOX_BASE_URL,
        client: OkHttpClient = createOkHttpClient()
    ): PayPalApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        return retrofit.create(PayPalApiService::class.java)
    }

    /**
     * Convenience helper to construct a [CreateOrderRequest] configured for
     * Milestone Escrow with Delayed Disbursement.
     *
     * @param milestoneId Identifier of the milestone
     * @param title Title or deliverable name
     * @param amount Total milestone budget
     * @param developerMerchantId PayPal merchant ID of the developer (payee)
     * @param platformFeePercent Marketplace take rate (default 10%)
     * @param platformMerchantId Platform payee ID for the commission
     */
    fun buildDelayedDisbursementOrder(
        milestoneId: String,
        title: String,
        amount: Double,
        developerMerchantId: String,
        platformFeePercent: Double = 0.10,
        platformMerchantId: String? = null
    ): CreateOrderRequest {
        val commissionAmount = amount * platformFeePercent
        val formattedTotal = String.format(Locale.US, "%.2f", amount)
        val formattedCommission = String.format(Locale.US, "%.2f", commissionAmount)

        val platformFeesList = listOf(
            PlatformFee(
                amount = MoneyAmount(currencyCode = "USD", value = formattedCommission),
                payee = platformMerchantId?.let { Payee(merchantId = it) }
            )
        )

        val paymentInstruction = PaymentInstruction(
            disbursementMode = "DELAYED",
            platformFees = platformFeesList
        )

        val purchaseUnit = PurchaseUnitRequest(
            referenceId = milestoneId,
            description = "Milestone Escrow: $title",
            customId = milestoneId,
            invoiceId = "INV-${milestoneId.uppercase()}-${System.currentTimeMillis().toString().takeLast(6)}",
            amount = MoneyAmount(currencyCode = "USD", value = formattedTotal),
            payee = Payee(merchantId = developerMerchantId),
            paymentInstruction = paymentInstruction
        )

        return CreateOrderRequest(
            intent = "CAPTURE",
            purchaseUnits = listOf(purchaseUnit)
        )
    }
}
