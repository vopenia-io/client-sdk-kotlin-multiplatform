package com.vopenia.sdk

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.pm.ProviderInfo
import android.net.Uri

object Sdk {
    internal lateinit var applicationContext: Context
}

class HoldApplicationContext : ContentProvider() {
    override fun onCreate(): Boolean {
        return true
    }

    override fun attachInfo(context: Context?, info: ProviderInfo?) {
        super.attachInfo(context, info)

        context?.let {
            Sdk.applicationContext = it
        }
    }

    override fun query(
        p0: Uri,
        p1: Array<out String>?,
        p2: String?,
        p3: Array<out String>?,
        p4: String?
    ) = null

    override fun getType(p0: Uri) = null

    override fun insert(p0: Uri, p1: ContentValues?) = null

    override fun delete(p0: Uri, p1: String?, p2: Array<out String>?) = 0

    override fun update(p0: Uri, p1: ContentValues?, p2: String?, p3: Array<out String>?) = 0
}
