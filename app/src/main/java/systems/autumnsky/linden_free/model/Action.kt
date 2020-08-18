package systems.autumnsky.linden_free.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Action(
    @PrimaryKey open var id: String? = null,
    open var name: String? = null,
    open var medicine: Medicine? = null
) : RealmObject()