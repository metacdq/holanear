package com.cindaku.holanear.extension

import com.google.gson.reflect.TypeToken

inline fun <reified T> genericType() = object: TypeToken<T>() {}.type