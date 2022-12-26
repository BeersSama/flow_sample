package com.beerus.flow_code_learn.models

data class ComparablePair<A: Comparable<A>, B: Comparable<B>>(
    val first: A,
    val second: B
) : Comparable<ComparablePair<A, B>> {
    override fun compareTo(other: ComparablePair<A, B>): Int {
        val firstComp = first.compareTo(other.first)
        if (firstComp != 0) { return firstComp }
        return second.compareTo(other.second)
    }
}
