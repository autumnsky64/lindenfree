package systems.autumnsky.linden_free.model

import io.realm.RealmObject

open class TakenMedicine(
    open var name: String? = null,
    open var quantity: Double? = null,
    open var medicineEvent: Event? = null
): RealmObject()
