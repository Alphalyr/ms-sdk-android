package fr.alphalyr.marketingstudiosdk

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.URL


class AlphalyrMarketingStudioSdk {
    companion object {private var gdprConsent: Boolean = false
        private var customerId: String = ""
        private var universalLinkingUrl: String? = null

        private val deviceId: String = ""
        private val deviceType: String = if (Build.USER.equals("pad", ignoreCase = true)) "t" else if (Build.USER.equals("phone", ignoreCase = true)) "m" else "u"
        private var aid: String = ""
        private var blacklistedParams: Array<String> = arrayOf()

        public fun configure(aid: String, blacklistedParams: Array<String>?) {
            this.aid = aid
            if (blacklistedParams != null) {
                this.blacklistedParams = blacklistedParams
            }
        }

        public fun onNewIntent(intent: Intent) {
            val appLinkData: Uri? = intent.data
            if (appLinkData != null) {
                universalLinkingUrl = appLinkData.toString()
                trackLandingHit()
            }
        }

        public fun setGdprConsent(newValue: Boolean) {
            gdprConsent = newValue
        }

        public fun setCustomerId(newValue: String) {
            customerId = newValue
        }

        fun trackTransaction(
            totalPrice: Double,
            totalPriceWithTax: Double,
            reference: String,
            new: Boolean,
            currency: String,
            discountCode: String,
            discountAmount: Double,
            products: List<Triple<String, Int, Double>>
        ) {
            val transactionQueryParams = "totalPrice=$totalPrice&totalPriceWithTax=$totalPriceWithTax&reference=$reference&new=${if (new) "1" else "0"}&currency=$currency&discountCode=$discountCode&discountAmount=$discountAmount&products=${stringifyProducts(products)}"

            val queryParams = listOfNotNull(commonQueryParams(), transactionQueryParams).joinToString("&")
            requestApi("track/store", queryParams)
        }

        private fun trackLandingHit() {
            val queryParams = listOfNotNull(commonQueryParams(), universalLinkingQueryParams()).joinToString("&")

            requestApi("tag/store", queryParams)
        }

        private fun universalLinkingQueryParams(): String? {
            if (universalLinkingUrl == null) {
                return null
            }
            val url = URL(universalLinkingUrl)
            val queryParams = getUniversalLinkingQueryParams()

            return "path=${url.path}&utm_source=${queryParams["utm_source"]}&utm_medium=${queryParams["utm_medium"]}&utm_campaign=${queryParams["utm_campaign"]}&referrer=${queryParams["referrer"]}"
        }

        private fun getUniversalLinkingQueryParams(): Map<String, String> {
            val components = Uri.parse(universalLinkingUrl.toString())
            val queryParams: MutableMap<String, String> = mutableMapOf()

            components.queryParameterNames.forEach { name ->
                queryParams[name] = components.getQueryParameter(name) ?: ""
            }

            return queryParams
        }

        private fun commonQueryParams(): String {
            return "aid=$aid&device_type=$deviceType&uuid=$deviceId&gdpr_consent=${if (gdprConsent) "1" else "0"}&cid=$customerId"
        }

        private fun stringifyProducts(products: List<Triple<String, Int, Double>>): String {
            return products.joinToString(";") { "${it.first}:${it.second}:${it.third}" }
        }

        private fun requestApi(path: String, queryParams: String) {
            GlobalScope.launch(Dispatchers.IO) {
                val fullUrl =
                    "https://webhook.site/4bc1a57c-09ab-40fb-b3cd-76b12d7a2f71?$queryParams"
                val url = URL(fullUrl)

                with(url.openConnection() as HttpURLConnection) {
                    requestMethod = "GET"

                    val response = try {
                        BufferedReader(InputStreamReader(inputStream)).use {
                            val responseBuilder = StringBuilder()
                            var inputLine: String?
                            while (it.readLine().also { inputLine = it } != null) {
                                responseBuilder.append(inputLine)
                            }
                            responseBuilder.toString()
                        }
                    } catch (e: IOException) {
                        // Handle network request failure
                        ""
                    }

                    // Handle API response
                    if (response.isNotEmpty()) {
                        // Process the response
                    }
                }
            }
        }
    }
}
