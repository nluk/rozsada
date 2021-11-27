package me.nluk.rozsada1.lib

val Int.idOdd: Boolean
    get() = this % 2 == 1

val Int.isEven: Boolean
    get() = !this.idOdd