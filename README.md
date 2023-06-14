# Alphalyr Marketing Studio Kotlin SDK

## Getting started

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

```swift
AlphalyrMarketingStudioSdk.setCustomerId(customerId: String)
AlphalyrMarketingStudioSdk.setGdprConsent(consent: Bool)
```

### Track a transaction

```swift
AlphalyrMarketingStudioSdk.trackTransaction(
    totalPrice: Double, 
    totalPriceWithTax: Double, 
    reference: String, 
    new: Bool,
    currency: String,
    discountCode: String?, 
    discountAmount: Double?, 
    products: List<Triple<String, Int, Double>>
)
```


