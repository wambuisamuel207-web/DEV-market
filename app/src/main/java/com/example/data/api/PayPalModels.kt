package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * PayPal API OAuth2 Token Response
 */
@JsonClass(generateAdapter = true)
data class PayPalTokenResponse(
    @Json(name = "scope") val scope: String? = null,
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "token_type") val tokenType: String = "Bearer",
    @Json(name = "app_id") val appId: String? = null,
    @Json(name = "expires_in") val expiresIn: Long = 32400,
    @Json(name = "nonce") val nonce: String? = null
)

/**
 * Currency amount pair
 */
@JsonClass(generateAdapter = true)
data class MoneyAmount(
    @Json(name = "currency_code") val currencyCode: String = "USD",
    @Json(name = "value") val value: String
)

/**
 * Recipient / merchant information
 */
@JsonClass(generateAdapter = true)
data class Payee(
    @Json(name = "merchant_id") val merchantId: String? = null,
    @Json(name = "email_address") val emailAddress: String? = null
)

/**
 * Platform fee specification for multiparty marketplace split
 */
@JsonClass(generateAdapter = true)
data class PlatformFee(
    @Json(name = "amount") val amount: MoneyAmount,
    @Json(name = "payee") val payee: Payee? = null
)

/**
 * Payment instruction controlling delayed disbursement in PayPal Commerce Platform.
 * Setting disbursement_mode to "DELAYED" holds funds in escrow until an explicit disbursement call.
 */
@JsonClass(generateAdapter = true)
data class PaymentInstruction(
    @Json(name = "disbursement_mode") val disbursementMode: String = "DELAYED",
    @Json(name = "platform_fees") val platformFees: List<PlatformFee>? = null,
    @Json(name = "payee_pricing_tier_id") val payeePricingTierId: String? = null
)

/**
 * Purchase unit representing a project milestone contract
 */
@JsonClass(generateAdapter = true)
data class PurchaseUnitRequest(
    @Json(name = "reference_id") val referenceId: String,
    @Json(name = "description") val description: String? = null,
    @Json(name = "custom_id") val customId: String? = null,
    @Json(name = "invoice_id") val invoiceId: String? = null,
    @Json(name = "amount") val amount: MoneyAmount,
    @Json(name = "payee") val payee: Payee? = null,
    @Json(name = "payment_instruction") val paymentInstruction: PaymentInstruction? = null
)

/**
 * Request payload for creating a PayPal checkout order
 */
@JsonClass(generateAdapter = true)
data class CreateOrderRequest(
    @Json(name = "intent") val intent: String = "CAPTURE",
    @Json(name = "purchase_units") val purchaseUnits: List<PurchaseUnitRequest>
)

/**
 * HATEOAS Link description
 */
@JsonClass(generateAdapter = true)
data class LinkDescription(
    @Json(name = "href") val href: String,
    @Json(name = "rel") val rel: String,
    @Json(name = "method") val method: String? = null
)

/**
 * PayPal Order details response
 */
@JsonClass(generateAdapter = true)
data class PayPalOrderResponse(
    @Json(name = "id") val id: String,
    @Json(name = "status") val status: String, // CREATED, SAVED, APPROVED, VOIDED, COMPLETED, PAYER_ACTION_REQUIRED
    @Json(name = "intent") val intent: String? = null,
    @Json(name = "purchase_units") val purchaseUnits: List<PurchaseUnitResponse>? = null,
    @Json(name = "create_time") val createTime: String? = null,
    @Json(name = "update_time") val updateTime: String? = null,
    @Json(name = "links") val links: List<LinkDescription>? = null
)

@JsonClass(generateAdapter = true)
data class PurchaseUnitResponse(
    @Json(name = "reference_id") val referenceId: String? = null,
    @Json(name = "amount") val amount: MoneyAmount? = null,
    @Json(name = "payee") val payee: Payee? = null,
    @Json(name = "payment_instruction") val paymentInstruction: PaymentInstruction? = null,
    @Json(name = "payments") val payments: PaymentsCollection? = null
)

/**
 * Capture payment entity inside payments collection
 */
@JsonClass(generateAdapter = true)
data class CapturePayment(
    @Json(name = "id") val id: String,
    @Json(name = "status") val status: String, // COMPLETED, PENDING, FAILED, DECLINED
    @Json(name = "disbursement_mode") val disbursementMode: String? = null, // DELAYED, INSTANT
    @Json(name = "amount") val amount: MoneyAmount,
    @Json(name = "final_capture") val finalCapture: Boolean = true,
    @Json(name = "seller_receivable_breakdown") val sellerReceivableBreakdown: SellerReceivableBreakdown? = null,
    @Json(name = "seller_protection") val sellerProtection: Map<String, Any?>? = null,
    @Json(name = "create_time") val createTime: String? = null,
    @Json(name = "update_time") val updateTime: String? = null,
    @Json(name = "links") val links: List<LinkDescription>? = null
)

@JsonClass(generateAdapter = true)
data class SellerReceivableBreakdown(
    @Json(name = "gross_amount") val grossAmount: MoneyAmount,
    @Json(name = "paypal_fee") val paypalFee: MoneyAmount? = null,
    @Json(name = "net_amount") val netAmount: MoneyAmount? = null,
    @Json(name = "platform_fees") val platformFees: List<PlatformFeeBreakdown>? = null
)

@JsonClass(generateAdapter = true)
data class PlatformFeeBreakdown(
    @Json(name = "amount") val amount: MoneyAmount,
    @Json(name = "payee") val payee: Payee? = null
)

@JsonClass(generateAdapter = true)
data class PaymentsCollection(
    @Json(name = "captures") val captures: List<CapturePayment>? = null,
    @Json(name = "refunds") val refunds: List<RefundResponse>? = null
)

/**
 * Response after capturing an authorized PayPal order
 */
@JsonClass(generateAdapter = true)
data class CaptureOrderResponse(
    @Json(name = "id") val id: String,
    @Json(name = "status") val status: String, // COMPLETED, INSTRUMENT_DECLINED
    @Json(name = "purchase_units") val purchaseUnits: List<PurchaseUnitResponse>? = null,
    @Json(name = "links") val links: List<LinkDescription>? = null
)

/**
 * Response after executing delayed disbursement
 * POST /v2/payments/captures/{capture_id}/disburse
 */
@JsonClass(generateAdapter = true)
data class DisburseResponse(
    @Json(name = "id") val id: String,
    @Json(name = "status") val status: String, // COMPLETED, PENDING
    @Json(name = "disbursement_mode") val disbursementMode: String? = null,
    @Json(name = "amount") val amount: MoneyAmount? = null,
    @Json(name = "links") val links: List<LinkDescription>? = null
)

/**
 * Refund request for captured payments
 */
@JsonClass(generateAdapter = true)
data class RefundRequest(
    @Json(name = "amount") val amount: MoneyAmount? = null,
    @Json(name = "invoice_id") val invoiceId: String? = null,
    @Json(name = "note_to_payer") val noteToPayer: String? = null
)

/**
 * Refund response details
 */
@JsonClass(generateAdapter = true)
data class RefundResponse(
    @Json(name = "id") val id: String,
    @Json(name = "status") val status: String, // COMPLETED, PENDING, FAILED
    @Json(name = "amount") val amount: MoneyAmount? = null,
    @Json(name = "note_to_payer") val noteToPayer: String? = null,
    @Json(name = "create_time") val createTime: String? = null,
    @Json(name = "update_time") val updateTime: String? = null,
    @Json(name = "links") val links: List<LinkDescription>? = null
)

/**
 * Partner referral request for seller onboarding with DELAY_FUNDS_DISBURSEMENT
 */
@JsonClass(generateAdapter = true)
data class PartnerReferralRequest(
    @Json(name = "tracking_id") val trackingId: String,
    @Json(name = "operations") val operations: List<PartnerReferralOperation> = listOf(PartnerReferralOperation()),
    @Json(name = "products") val products: List<String> = listOf("EXPRESS_CHECKOUT")
)

@JsonClass(generateAdapter = true)
data class PartnerReferralOperation(
    @Json(name = "operation") val operation: String = "DELAY_FUNDS_DISBURSEMENT"
)

@JsonClass(generateAdapter = true)
data class PartnerReferralResponse(
    @Json(name = "links") val links: List<LinkDescription>? = null
)

/**
 * Check integration capability and seller readiness
 */
@JsonClass(generateAdapter = true)
data class MerchantIntegrationResponse(
    @Json(name = "merchant_id") val merchantId: String? = null,
    @Json(name = "tracking_id") val trackingId: String? = null,
    @Json(name = "payments_receivable") val paymentsReceivable: Boolean? = null,
    @Json(name = "primary_email_confirmed") val primaryEmailConfirmed: Boolean? = null
)
