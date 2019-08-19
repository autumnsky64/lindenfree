package systems.autumnsky.linden_free

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class Log (
    @PrimaryKey open var id: String? = null,
    open var time: Calendar? = null,
    open var event_name: String? = null,
    open var quantity: Double? = null
): RealmObject()