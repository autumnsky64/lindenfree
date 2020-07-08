package systems.autumnsky.linden_free

import io.realm.DynamicRealm
import io.realm.RealmMigration

class Migration : RealmMigration {
    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        val scheme = realm.schema
        var verCount = oldVersion
        if( verCount == 1L) {
            scheme.apply {
                rename("Event", "Action")
                rename("EventLog", "Event")
                get("Medicine")?.addField("is_use_as_needed", Boolean::class.java)
                get("Event")?.renameField("event_name", "name")
            }
        }
    }
}