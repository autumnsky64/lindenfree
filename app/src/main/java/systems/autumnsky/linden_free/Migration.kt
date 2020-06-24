package systems.autumnsky.linden_free

import io.realm.DynamicRealm
import io.realm.RealmMigration

class Migration : RealmMigration {
    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        val scheme = realm.schema
        if( oldVersion == 1L) {
            scheme.rename("Event", "Action")
            scheme.rename("EventLog", "Event")
        }
    }
}