package za.co.topcode.locationtracker.util

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.IOException

/**
 * Copyright (c) 2019 Topcode All rights reserved.
 * Contact info@topcode.co.za
 * Created by Ndivhuwo Nthambeleni on 2019/01/11.
 */

class IntegerTypeAdapter : TypeAdapter<Int>() {
    @Throws(IOException::class)
    override fun write(jsonWriter: JsonWriter, number: Int?) {
        if (number == null) {
            jsonWriter.nullValue()
            return
        }
        jsonWriter.value(number)
    }

    @Throws(IOException::class)
    override fun read(jsonReader: JsonReader): Int? {
        if (jsonReader.peek() == JsonToken.NULL) {
            jsonReader.nextNull()
            return null
        }
        var value: Int
        try {
            //String value = jsonReader.nextString();
            value = jsonReader.nextInt()
        } catch (e: NumberFormatException) {
            //throw new JsonSyntaxException(e);
            value = 0
        }

        return value
    }

}