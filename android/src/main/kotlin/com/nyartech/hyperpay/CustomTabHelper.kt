package com.nyartech.hyperpay


import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.browser.customtabs.*


/**
 * Created by sanjeev on 17/3/17.
 */
open class CustomTabHelper {
    private var customTabsClient: CustomTabsClient? = null
    var customTabsSession: CustomTabsSession? = null

    fun bindCustomTabsService(activity: Activity?, url: Uri?) {
        if (customTabsClient != null) {
            return
        }
        val mConnection: CustomTabsServiceConnection = object : CustomTabsServiceConnection() {
            override fun onCustomTabsServiceConnected(
                componentName: ComponentName,
                customTabsClient: CustomTabsClient
            ) {
                this@CustomTabHelper.customTabsClient = customTabsClient
                this@CustomTabHelper.customTabsClient!!.warmup(0L)
                customTabsSession = this@CustomTabHelper.customTabsClient!!.newSession(null)
                customTabsSession!!.mayLaunchUrl(url, null, null)
            }

            override fun onServiceDisconnected(name: ComponentName) {
                customTabsClient = null
            }
        }
        val packageNameToUse = getPackageName(activity!!);
        val chromePackage = CustomTabsClient.getPackageName(
            activity,
            packageNameToUse, false
        )
        val ok =
            CustomTabsClient.bindCustomTabsService(activity, chromePackage, mConnection)

        Log.d(
            "CustomTabHelper",
            "bindCustomTabsService : isOk = $ok ,CustomTabsClient = $customTabsClient ,chromePackage = $chromePackage ,packageNameToUse = $packageNameToUse"
        );
    }

    /**
     * Code from https://developer.chrome.com/multidevice/android/customtabs
     * For more information see
     * http://stackoverflow.com/a/33281092/137744
     * https://medium.com/google-developers/best-practices-for-custom-tabs-5700e55143ee
     */
    private fun getPackageName(context: Context): List<String> {
        // Get default VIEW intent handler that can view a web url.
        val activityIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.test-url.com"))

        // Get all apps that can handle VIEW intents.
        val pm = context.packageManager
        val resolvedActivityList = pm.queryIntentActivities(activityIntent, 0)
        val packagesSupportingCustomTabs: MutableList<String> = ArrayList()
        for (info in resolvedActivityList) {
            val serviceIntent = Intent()
            serviceIntent.action = CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION
            serviceIntent.setPackage(info.activityInfo.packageName)
            if (pm.resolveService(serviceIntent, 0) != null) {
                packagesSupportingCustomTabs.add(info.activityInfo.packageName)
            }
        }
        return packagesSupportingCustomTabs
    }

    companion object {
        private const val CUSTOM_TAB_PACKAGE_NAME = "com.android.chrome"
    }
}