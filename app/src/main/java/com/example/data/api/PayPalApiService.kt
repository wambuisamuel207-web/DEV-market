package com.example.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Retrofit Service Interface for PayPal Commerce Platform (PPCP) & Orders v2 API.
 * Handles OAuth2 authentication, multiparty order creation with delayed disbursement,
 * payment capture, delayed funds release/disbursement, refunds, and partner onboarding.
 */
interface PayPalApiService {

    /**
     * Authenticate and request an OAuth2 Bearer Access Token using client credentials.
     *
     * @param basicAuth Base64 encoded `Basic <client_id:client_secret>`
     * @param grantType Typically `"client_credentials"`
     */
    @FormUrlEncoded
    @POST("v1/oauth2/token")
    suspend fun getAccessToken(
        @Header("Authorization") basicAuth: String,
        @Field("grant_type") grantType: String = "client_credentials"
    ): PayPalTokenResponse

    /**
     * Create an order with `intent = "CAPTURE"`.
     * In multiparty escrow workflows, specify `payment_instruction.disbursement_mode = "DELAYED"`
     * and define `platform_fees` for the marketplace commission split.
     *
     * @param requestId Unique idempotency key (e.g., UUID)
     * @param prefer `"return=representation"` or `"return=minimal"`
     * @param partnerAttributionId Platform BN code (optional)
     * @param request The order definition with purchase units and delayed disbursement instructions
     */
    @POST("v2/checkout/orders")
    suspend fun createOrder(
        @Header("PayPal-Request-Id") requestId: String? = null,
        @Header("Prefer") prefer: String? = "return=representation",
        @Header("PayPal-Partner-Attribution-Id") partnerAttributionId: String? = null,
        @Body request: CreateOrderRequest
    ): PayPalOrderResponse

    /**
     * Retrieve current order status and details by order ID.
     */
    @GET("v2/checkout/orders/{orderId}")
    suspend fun getOrderDetails(
        @Path("orderId") orderId: String
    ): PayPalOrderResponse

    /**
     * Capture payment for an approved order.
     * When `disbursement_mode = "DELAYED"`, the funds are captured from the buyer and held in
     * escrow rather than immediately disbursed to the seller's balance.
     *
     * @param orderId The PayPal Order ID
     * @param requestId Unique idempotency key
     * @param prefer `"return=representation"`
     */
    @POST("v2/checkout/orders/{orderId}/capture")
    suspend fun captureOrderPayment(
        @Path("orderId") orderId: String,
        @Header("PayPal-Request-Id") requestId: String? = null,
        @Header("Prefer") prefer: String? = "return=representation",
        @Body emptyBody: Map<String, String> = emptyMap()
    ): CaptureOrderResponse

    /**
     * Retrieve captured payment details, including current status (`COMPLETED`, `PENDING`),
     * `disbursement_mode` (`DELAYED`), and seller receivable breakdowns.
     *
     * @param captureId The PayPal Capture ID (e.g., `"CAP-..."`)
     */
    @GET("v2/payments/captures/{captureId}")
    suspend fun getCaptureDetails(
        @Path("captureId") captureId: String
    ): CapturePayment

    /**
     * Disburse delayed funds from a captured payment.
     * This releases the escrowed funds to the developer's PayPal account after milestone deliverables
     * are inspected and approved by the client or mediated by the platform admin.
     *
     * Endpoint: `POST /v2/payments/captures/{capture_id}/disburse`
     *
     * @param captureId The ID of the delayed capture to disburse
     * @param requestId Idempotency key for disbursement execution
     * @param prefer `"return=representation"`
     */
    @POST("v2/payments/captures/{captureId}/disburse")
    suspend fun disburseDelayedFunds(
        @Path("captureId") captureId: String,
        @Header("PayPal-Request-Id") requestId: String? = null,
        @Header("Prefer") prefer: String? = "return=representation",
        @Body emptyBody: Map<String, String> = emptyMap()
    ): DisburseResponse

    /**
     * Refund a captured payment back to the client/buyer.
     * Used when an escrow dispute is decided in favor of the client, or upon contract cancellation.
     *
     * Endpoint: `POST /v2/payments/captures/{capture_id}/refund`
     *
     * @param captureId The ID of the capture to refund
     * @param requestId Idempotency key
     * @param prefer `"return=representation"`
     * @param request Partial or full refund details and notes
     */
    @POST("v2/payments/captures/{captureId}/refund")
    suspend fun refundCapture(
        @Path("captureId") captureId: String,
        @Header("PayPal-Request-Id") requestId: String? = null,
        @Header("Prefer") prefer: String? = "return=representation",
        @Body request: RefundRequest = RefundRequest()
    ): RefundResponse

    /**
     * Create a Partner Referral to onboard a developer with the `DELAY_FUNDS_DISBURSEMENT` capability.
     *
     * Endpoint: `POST /v2/customer/partner-referrals`
     */
    @POST("v2/customer/partner-referrals")
    suspend fun createPartnerReferral(
        @Body request: PartnerReferralRequest
    ): PartnerReferralResponse

    /**
     * Verify whether an onboarded developer's PayPal account has payments receivable and email confirmed.
     *
     * Endpoint: `GET /v1/customer/partners/{partner_id}/merchant-integrations/{merchant_id}`
     */
    @GET("v1/customer/partners/{partnerId}/merchant-integrations/{merchantId}")
    suspend fun getMerchantIntegration(
        @Path("partnerId") partnerId: String,
        @Path("merchantId") merchantId: String
    ): MerchantIntegrationResponse
}
