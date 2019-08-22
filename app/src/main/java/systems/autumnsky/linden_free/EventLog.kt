package systems.autumnsky.linden_free

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class EventLog (
    @PrimaryKey open var id: Long? = null,
    open var time: Date? = null,
    open var event_name: String? = null,
    open var quantity: Double? = null
): RealmObject()