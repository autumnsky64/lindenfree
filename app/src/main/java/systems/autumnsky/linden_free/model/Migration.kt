package systems.autumnsky.linden_free.model

import io.realm.DynamicRealm
import io.realm.RealmMigration
import java.util.*

class Migration : RealmMigration {
    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        val scheme = realm.schema
        var verCount = oldVersion
        if (verCount == 1L) {
            scheme.apply {
                rename("Event", "Action")
                rename("EventLog", "Event")
                get("Medicine")?.addField("is_use_as_needed", Boolean::class.java)
                    ?.setNullable("is_use_as_needed", true)
                get("Event")?.renameField("event_name", "name")
            }
            verCount++
        }
        if (verCount == 2L) {
            scheme.apply {
                create("Activity").apply {
                    addField("name", String::class.java)
                    addField("length", Long::class.java).setNullable("length", true)
                    addField("startTime", Date::class.java)
                }
            }
            scheme.apply {
                create("DailyActivity").apply {
                    addField("day", Date::class.java)
                    addRealmListField("activityStack", get("Activity")!!)
                    addRealmListField("medicineStack", get("Activity")!!)
                }
            }
            verCount++
        }
        if (verCount == 3L) {
            scheme.create("TakenMedicine").apply {
                addField("name", String::class.java)
                addField("quantity", Double::class.java).setNullable("quantity", true)
            }
            scheme.apply{
                get("Activity")?.apply {
                    addField("endTime", Date::class.java)
                    addRealmListField("medicines", get("TakenMedicine")!!)
                }
                get("DailyActivity")?.removeField("medicineStack")
            }
        }
    }
}