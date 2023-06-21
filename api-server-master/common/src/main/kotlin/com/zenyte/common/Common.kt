package com.zenyte.common

import com.google.gson.Gson
import com.google.gson.GsonBuilder

/**
 * @author Corey
 * @since 30/10/2019
 */

val gson: Gson = GsonBuilder().disableHtmlEscaping().create()

const val ZENYTE_USER_MEMBER_ID = 1272