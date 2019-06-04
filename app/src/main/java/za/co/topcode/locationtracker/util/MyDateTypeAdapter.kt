package za.co.topcode.locationtracker.util

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Copyright (c) 2019 Topcode All rights reserved.
 * Contact info@topcode.co.za
 * Created by Ndivhuwo Nthambeleni on 2019/01/11.
 */

class MyDateTypeAdapter : TypeAdapter<Date>() {
    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: Date?) {
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
        df.timeZone = TimeZone.getTimeZone("Africa/Johannesburg")
        if (value == null)
            out.nullValue()
        else
            out.value(df.format(value))
    }

    @Throws(IOException::class)
    override fun read(`in`: JsonReader): Date? {
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
        df.timeZone = TimeZone.getTimeZone("Africa/Johannesburg")
        try {
            if (`in`.peek() == JsonToken.NULL) {
                `in`.nextNull()
                return null
            }
            return df.parse(`in`.nextString())
        } catch (e: java.text.ParseException) {
            e.printStackTrace()
            return null
        }

    }
}