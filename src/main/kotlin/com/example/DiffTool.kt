package com.example

import com.example.models.ChangeType
import com.example.models.ListUpdate
import com.example.models.PropertyUpdate
import java.lang.reflect.Field

class DiffTool {
    fun <T> diff(previous: T, current: T): List<ChangeType> {
        // Start the recursive diffing process
        return recDiff("", previous, current)
    }

    // Walk the objects recursively to determine changes in fields, keeping track of the
    // path.
    private fun <T> recDiff(path: String, previous: T, current: T): List<ChangeType> {
        require(previous != null && current != null) {
            "Neither previous or current values can be null"
        }
        val diffs: MutableList<ChangeType> = ArrayList()
        val fields: Array<Field> = current!!::class.java.getDeclaredFields()
        for (field in fields) {
            val fieldClass = field.type
            val fieldPath = if (path.isEmpty()) field.name else path + "." + field.name
            try {
                field.setAccessible(true)
                val previousValue = field[previous]
                val currentValue = field[current]
                if (previousValue != currentValue) {
                    if(fieldClass.isSimpleType()) {
                        diffs.add(PropertyUpdate(fieldPath, previousValue, currentValue))
                    }
                    else if (previousValue is List<*> && currentValue is List<*>) {
                        diffs.addAll(
                            listDiff(
                                fieldPath,
                                previousValue,
                                currentValue
                            )
                        )
                    }
                    else {
                        diffs.addAll(
                            recDiff(
                                fieldPath,
                                previousValue,
                                currentValue
                            )
                        )
                    }
                }
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }
        }
        return diffs
    }

    // For lists we can use this to determine additions and removals.
    private fun listDiff(path: String, previous: List<*>, current: List<*>): List<ChangeType> {
        val diffs: MutableList<ChangeType> = ArrayList()

        // Take care of the simple types first
        val previousSet = previous.toSet()
        val currentSet = current.toSet()
        val (added, updated) = currentSet.minus(previousSet).partition {
            val item = it!!::class.java
            item.isSimpleType() || !isInBothSets(previousSet, currentSet, it)
        }
        val (removed, _) = previousSet.minus(currentSet).partition {
            val item = it!!::class.java
            item.isSimpleType() || !isInBothSets(previousSet, currentSet, it)
        }
        if (added.isNotEmpty() || removed.isNotEmpty())
            diffs.add(ListUpdate(path, added, removed))

        // Now we can take care of the complex types that were updated
        updated.map {
            val idFieldName = idFieldName(it)
            val aField = it!!::class.java.getDeclaredFieldAccessible(idFieldName)
            val listObjectPath = aField[it]
            val previousItem = findObjectInList(previous, idFieldName, it)
            val fieldPath = if (path.isEmpty()) "[$listObjectPath]" else "$path[$listObjectPath]"
            diffs.addAll(recDiff(fieldPath, previousItem!!, it))
        }
        return diffs
    }

    private fun <T> isInBothSets(previousSet: Set<T>, currentSet: Set<T>, item: T): Boolean {
        val idFieldName = idFieldName(item)
        return findObjectInList(previousSet.toList(), idFieldName, item) != null &&
                findObjectInList(currentSet.toList(), idFieldName, item) != null
    }

    private fun <T> findObjectInList(objectList: List<T>, idFieldName: String, item: T) = objectList.find {
        val first = it!!::class.java.getDeclaredFieldAccessible(idFieldName)
        val second = item!!::class.java.getDeclaredFieldAccessible(idFieldName)
        first[it] == second[item]
    }

    // For a given list object, determine the id field name.
    private fun <T> idFieldName(item: T): String {
        val fields = item!!::class.java.getDeclaredFields()
        val idFields = fields.filter { it.name == "id" || it.isAnnotationPresent(AuditKey::class.java) }.map { it.name }
        when(idFields.size) {
            0 -> throw Exception("No id field found for list object")
            1 -> return idFields[0]
            else -> throw Exception("Ambiguous id fields found for list object")
        }
    }

}

fun Class<*>.getDeclaredFieldAccessible(name: String): Field {
    val field = this.getDeclaredField(name)
    field.setAccessible(true)
    return field
}

fun Class<*>.isSimpleType(): Boolean {
    val primitives: List<String> = mutableListOf(
        "boolean", "Boolean",
        "byte", "Byte",
        "char", "Character",
        "double", "Double",
        "float", "Float",
        "int", "Integer",
        "long", "Long",
        "short", "Short",
        "String",
    )
    return primitives.contains(this.simpleName)
}
