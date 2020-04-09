package at.mikemitterer.catshostel.model

class Cat {
    /**
     * Wird bei insert automatisch von iBatis gesetzt
     */
    var ID = 0L

    var name: String? = null

    var age: Int = 0

    /** Für iBatis  */
    constructor() {}

    constructor(name: String, age: Int) {
        this.name = name
        this.age = age
    }
}