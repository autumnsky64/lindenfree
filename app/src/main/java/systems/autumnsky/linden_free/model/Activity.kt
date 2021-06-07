package systems.autumnsky.linden_free.model

import io.realm.RealmList
import io.realm.RealmObject
import java.util.*

open class Activity(
    open var name: String? = null,
    open var length: Long? = null,
    open var startTime: Date? = null,
    open var endTime: Date? = null,

    open var startEvent: Event? = null,
    open var endEvent: Event? = null,

    open var medicines: RealmList<TakenMedicine>? = null
) : RealmObject()

