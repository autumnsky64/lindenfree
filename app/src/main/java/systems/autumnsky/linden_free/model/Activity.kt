package systems.autumnsky.linden_free.model

import io.realm.RealmObject
import java.util.*

open class Activity (
    open var name: String? = null,
    open var length: Long? = null,
    open var startTime: Date? = null
): RealmObject()

