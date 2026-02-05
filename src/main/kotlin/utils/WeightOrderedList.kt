package me.clcondorcet.boiteaoutils.utils

import java.util.LinkedList
import kotlin.random.Random

class WeightOrderedList<T> {

    data class WeightedObject<T>(val item: T, val weight: Int)

    val list: LinkedList<WeightedObject<T>> = LinkedList()

    fun addAll(items: Collection<T>, weightGetter: (T) -> Int) {
        list.addAll(items.map { WeightedObject(it, weightGetter(it)) })
    }

    val totalWeight: Int
        get() { return list.sumOf { it.weight }}

    fun order() {
        list.sortByDescending { it.weight }
    }

    fun random(): T {
        order()
        var randomIndex = Random(System.currentTimeMillis()).nextInt(totalWeight)
        list.forEach { item ->
            if (randomIndex < item.weight) return item.item
            randomIndex -= item.weight
        }
        throw IndexOutOfBoundsException()
    }
}