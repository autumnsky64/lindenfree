package systems.autumnsky.linden_free

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Medicine (
    @PrimaryKey var id: Int? = null,
    var name: String? = null,
    var regular_quantity: Double? = null,
    var adjustment_step: Double? = null
): RealmObject()