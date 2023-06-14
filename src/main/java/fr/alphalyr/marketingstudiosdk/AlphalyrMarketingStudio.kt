package fr.alphalyr.marketingstudiosdk

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.content.res.Configuration
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
    companion object {
        private var gdprConsent: Boolean = false
        private var customerId: String? = null
        private var universalLinkingUrl: String? = null
        private var isConfigured: Boolean = false
        private var deviceId: String? = null
        private var deviceType: String = "u"
        private var aid: String? = null
        private var excludedUniversalLinkingParams: Array<String> = emptyArray()

        fun configure(aid: String, applicationContext: Context, excludedUniversalLinkingParams: Array<String>?) {
            this.aid = aid
            excludedUniversalLinkingParams?.let { this.excludedUniversalLinkingParams = it }
            setDeviceId(applicationContext)
            setDeviceType(applicationContext)
            isConfigured = true
        }

        private fun setDeviceId(applicationContext: Context) {
            deviceId = Settings.Secure.getString(applicationContext.contentResolver, Settings.Secure.ANDROID_ID)
        }

        private fun setDeviceType(applicationContext: Context) {
            val configuration = applicationContext.resources.configuration
            val isTablet = configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
            deviceType = if (isTablet) "t" else "m"
        }

        private fun checkIfConfigured() {
            if (!isConfigured) {
                throw Error("AlphalyrMarketingStudioSDK has not been configured yet")
            }
        }

        fun onNewIntent(intent: Intent) {
            checkIfConfigured()
            val appLinkData: Uri? = intent.data
            if (appLinkData != null) {
                universalLinkingUrl = appLinkData.toString()
                trackLandingHit()
            }
        }

        fun setGdprConsent(newValue: Boolean) {
            gdprConsent = newValue
        }

        fun setCustomerId(newValue: String) {
            customerId = newValue
        }

        fun trackScreenChange(newScreen: String) {
            val queryParams = listOfNotNull(commonQueryParams(), "referrer=self&path=$newScreen").joinToString("&")

            requestApi("tag/store", queryParams)
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
            checkIfConfigured()
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

            return arrayOf("path=${url.path}", getFilteredUniversalLinkingQueryParams()).joinToString("&")
        }

        private fun getFilteredUniversalLinkingQueryParams(): String {
            val components = Uri.parse(universalLinkingUrl.toString())
            val queryParams = mutableListOf<String>()

            components.queryParameterNames.forEach { name ->
                if (name !in excludedUniversalLinkingParams) {
                    queryParams.add("${name}=${components.getQueryParameter(name) ?: ""}")
                }
            }

            return queryParams.joinToString("&")
        }

        private fun commonQueryParams(): String {
            return "aid=$aid&device_type=$deviceType&uuid=$deviceId&gdpr_consent=${if (gdprConsent) "1" else "0"}&cid=$customerId"
        }

        private fun stringifyProducts(products: List<Triple<String, Int, Double>>): String {
            return products.joinToString(";") { "${it.first}:${it.second}:${it.third}" }
        }

        private fun requestApi(path: String, queryParams: String) {
            GlobalScope.launch(Dispatchers.IO) {
                val fullUrl = " https://tck.elitrack.com/$path?$queryParams"
                val url = URL(fullUrl)

                with(url.openConnection() as HttpURLConnection) {
                    requestMethod = "GET"

                    try {
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
                        Log.e("AlphalyrMarketingStudioSDK", e.toString())
                    }
                }
            }
        }
    }
}
