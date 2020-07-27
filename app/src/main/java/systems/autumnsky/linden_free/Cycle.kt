package systems.autumnsky.linden_free

import io.realm.RealmObject
import java.util.*

open class Cycle (
    open var activity: String? = null,
    open var length: Long? = null,
    open var startTime: Date? = null
): RealmObject(){

}