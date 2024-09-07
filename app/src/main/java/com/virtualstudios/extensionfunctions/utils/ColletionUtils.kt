package com.virtualstudios.extensionfunctions.utils

fun <T> nonNull(list: List<T>?): List<T> {
    return list ?: ArrayList()
}

fun <T> clearAndAddAll(to: MutableList<T>, from: List<T>?) {
    to.clear()
    if (from != null) to.addAll(from)
}

fun <K, V> clearAndPutAll(to: MutableMap<K, V>, from: Map<K, V>?) {
    to.clear()
    if (from != null) to.putAll(from)
}

fun <E> nonNull(data: ArrayList<E>?): ArrayList<E> {
    return data ?: ArrayList()
}

fun isNotEmpty(collection: Collection<*>?): Boolean {
    return collection != null && collection.size > 0
}