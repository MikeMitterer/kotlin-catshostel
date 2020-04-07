package at.mikemitterer.catshostel.model

class Cat {
    var ID = 0

    var name: String? = null

    var age: Int = 0

    /** Für iBatis  */
    constructor() {}

    constructor(role: String, age: Int) {
        this.name = role
        this.age = age
    }
}