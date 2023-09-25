package com.example.models

import com.example.AuditKey

data class Thing(
    val name: String,
    val bar: Bar,
    val age: Int = 25,
    val phone: String = "8675309")

data class Bar(
    val description: String,
    val number: Int,
    val foo: Foo,
)

data class Foo(
    val colors: List<String>,
//    val sillyList: List<ListObj1>
)

data class RootState(
    val a: Int,
    val b: SubState,
    val c: List<Any>
)

data class SubState(
    val d: Int,
    val e: Int
)

sealed interface ListObject
data class IdListObject(val id: String, val x: Int) : ListObject {
    override fun toString(): String {
        return """{"id": "$id", "x": $x}"""
    }
}

data class AuditIdListObject(@field:AuditKey val aid: String, val x: Int) : ListObject {
    override fun toString(): String {
        return """{"aid": "$aid", "x": $x}"""
    }
}

data class NoIdListObject(val y: String, val z: String) : ListObject {
    override fun toString(): String {
        return """{"y": "$y", "z": "$z"}"""
    }
}

