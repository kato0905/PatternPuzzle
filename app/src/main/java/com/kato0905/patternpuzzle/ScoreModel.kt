package com.kato0905.patternpuzzle

import io.realm.RealmObject

open class ScoreModel(
        open var id : Int = 0,
        open var name : String = "name",
        open var score1 : Int = 0,
        open var score2 : Int = 0,
        open var score3 : Int = 0,
        open var score4 : Int = 0,
        open var score5 : Int = 0,
        open var score6 : Int = 0,
        open var score7 : Int = 0,
        open var score8 : Int = 0,
        open var score9 : Int = 0,
        open var score10 : Int = 0
) : RealmObject() {}