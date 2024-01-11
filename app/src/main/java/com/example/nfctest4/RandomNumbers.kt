package com.example.nfctest4

import kotlin.random.Random

object Numbers {
    private val randomNumbers = (0..2).shuffled(Random(System.currentTimeMillis())).toMutableList()
    private var currentIndex = 0

    fun getNextNumber(): Int {
        if (currentIndex >= randomNumbers.size) {
            // リスト内のすべての数値を返し終えた場合、リストをシャッフルして再度始める
            currentIndex = 0
            randomNumbers.shuffle()
        }

        val nextNumber = randomNumbers[currentIndex]
        currentIndex++
        return nextNumber
    }
}











