package systems.autumnsky.linden_free.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Medicine(
    @PrimaryKey open var id: String? = null,
    open var name: String? = null,
    open var regular_quantity: Double? = null,
    open var adjustment_step: Double? = null,
    open var is_use_as_needed: Boolean? = null
) : RealmObject()