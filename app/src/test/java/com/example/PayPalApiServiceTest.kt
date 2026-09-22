package com.example

import com.example.data.api.CaptureOrderResponse
import com.example.data.api.DisburseResponse
import com.example.data.api.MoneyAmount
import com.example.data.api.PayPalApiClient
import com.example.data.api.PayPalApiService
import com.example.data.api.PayPalOrderResponse
import com.example.data.api.RefundResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PayPalApiServiceTest {

    @Test
    fun testBuildDelayedDisbursementOrder_structure() {
        val orderRequest = PayPalApiClient.buildDelayedDisbursementOrder(
            milestoneId = "m_101",
            title = "Backend API & Database Schema",
            amount = 500.0,
            developerMerchantId = "PMR-DEV-9988",
            platformFeePercent = 0.10,
            platformMerchantId = "PMR-PLATFORM-MAIN"
        )

        assertEquals("CAPTURE", orderRequest.intent)
        assertEquals(1, orderRequest.purchaseUnits.size)

        val unit = orderRequest.purchaseUnits[0]
        assertEquals("m_101", unit.referenceId)
        assertEquals("USD", unit.amount.currencyCode)
        assertEquals("500.00", unit.amount.value)
        assertEquals("PMR-DEV-9988", unit.payee?.merchantId)

        val instruction = unit.paymentInstruction
        assertNotNull(instruction)
        assertEquals("DELAYED", instruction?.disbursementMode)
        assertEquals(1, instruction?.platformFees?.size)

        val fee = instruction?.platformFees?.get(0)
        assertEquals("50.00", fee?.amount?.value)
        assertEquals("PMR-PLATFORM-MAIN", fee?.payee?.merchantId)
    }

    @Test
    fun testJsonSerialization_CaptureOrderResponse() {
        val json = """
            {
              "id": "ORDER-991203",
              "status": "COMPLETED",
              "purchase_units": [
                {
                  "reference_id": "m_101",
                  "payments": {
                    "captures": [
                      {
                        "id": "CAP-88123-DELAYED",
                        "status": "COMPLETED",
                        "disbursement_mode": "DELAYED",
                        "amount": {
                          "currency_code": "USD",
                          "value": "500.00"
                        },
                        "seller_receivable_breakdown": {
                          "gross_amount": { "currency_code": "USD", "value": "500.00" },
                          "paypal_fee": { "currency_code": "USD", "value": "15.00" },
                          "net_amount": { "currency_code": "USD", "value": "435.00" },
                          "platform_fees": [
                            {
                              "amount": { "currency_code": "USD", "value": "50.00" }
                            }
                          ]
                        }
                      }
                    ]
                  }
                }
              ]
            }
        """.trimIndent()

        val adapter = PayPalApiClient.moshi.adapter(CaptureOrderResponse::class.java)
        val response = adapter.fromJson(json)

        assertNotNull(response)
        assertEquals("ORDER-991203", response?.id)
        assertEquals("COMPLETED", response?.status)

        val capture = response?.purchaseUnits?.firstOrNull()?.payments?.captures?.firstOrNull()
        assertNotNull(capture)
        assertEquals("CAP-88123-DELAYED", capture?.id)
        assertEquals("DELAYED", capture?.disbursementMode)
        assertEquals("500.00", capture?.amount?.value)
        assertEquals("50.00", capture?.sellerReceivableBreakdown?.platformFees?.firstOrNull()?.amount?.value)
    }

    @Test
    fun testJsonSerialization_DisburseResponse() {
        val json = """
            {
              "id": "CAP-88123-DELAYED",
              "status": "COMPLETED",
              "disbursement_mode": "INSTANT",
              "amount": {
                "currency_code": "USD",
                "value": "450.00"
              }
            }
        """.trimIndent()

        val adapter = PayPalApiClient.moshi.adapter(DisburseResponse::class.java)
        val response = adapter.fromJson(json)

        assertNotNull(response)
        assertEquals("CAP-88123-DELAYED", response?.id)
        assertEquals("COMPLETED", response?.status)
        assertEquals("450.00", response?.amount?.value)
    }

    @Test
    fun testCreateServiceInstance() {
        val service: PayPalApiService = PayPalApiClient.createService(
            baseUrl = PayPalApiClient.SANDBOX_BASE_URL
        )
        assertNotNull(service)
    }
}
