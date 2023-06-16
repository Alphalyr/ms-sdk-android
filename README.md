# Alphalyr Marketing Studio Kotlin SDK

## Getting started

### Pre-requisites
This SDK relies on Universal Links. You may refer to this [guide](https://developer.android.com/training/app-links/deep-linking) to setup your mobile app with a domain name.

### Install the SDK

### Initialize the SDK in your app

```kotlin
// Import the SDK
import fr.alphalyr.marketingstudiosdk.AlphalyrMarketingStudioSdk

class MainActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Configure the SDK
        AlphalyrMarketingStudioSdk.configure(
            "MY_ALPHALYR_AID",
            applicationContext,
            excludedUniversalLinkingParams = arrayOf("secret", "params"),
        )
        // Add a listener for universal linking
        AlphalyrMarketingStudioSdk.onNewIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Add a listener for universal linking
        AlphalyrMarketingStudioSdk.onNewIntent(intent)
    }
}
```

### Register your customer preferences

```kotlin
AlphalyrMarketingStudioSdk.setCustomerId(customerId: String) // usually SHA256 of email address
AlphalyrMarketingStudioSdk.setGdprConsent(consent: Bool)
```

### Track a transaction

```kotlin
AlphalyrMarketingStudioSdk.trackTransaction(
    totalPrice: Double, // amount without taxes, without shipping costs 
    totalPriceWithTax: Double, // amount with taxes included
    reference: String, // order id
    new: Bool, // true if new customer, false if returning customer
    currency: String, // currency ISO-4217 code (i.e: EUR)
    discountCode: String?, // coupon code
    discountAmount: Double?,
    products: List<Triple<String, Int, Double>>
)
```
