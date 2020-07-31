package systems.autumnsky.linden_free

import io.realm.DynamicRealm
import io.realm.Realm
import io.realm.RealmMigration
import io.realm.Sort
import io.realm.kotlin.where
import systems.autumnsky.linden_free.model.DailyCycle
import systems.autumnsky.linden_free.model.Event
import java.util.*

class Migration : RealmMigration {
    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        val scheme = realm.schema
        var verCount = oldVersion
        if( verCount == 1L) {
            scheme.apply {
                rename("Event", "Action")
                rename("EventLog", "Event")
                get("Medicine")?.addField("is_use_as_needed", Boolean::class.java)?.setNullable("is_use_as_needed",true)
                get("Event")?.renameField("event_name", "name")
            }
            verCount++
        }
        if ( verCount == 2L ){
            scheme.apply {
                create("Cycle").apply {
                    addField("activity", String::class.java)
                    addField("length", Long::class.java).setNullable("length", true)
                    addField("startTime", Date::class.java)
                }
            }
            scheme.apply{
                create("DailyCycle").apply {
                    addField("day", Date::class.java)
                    addRealmListField("stack", get("Cycle")!!)
                }
            }

            //既存のイベントデータから、DailyCycleテーブルに日ごとのデータを入れる
            val lastDay = Calendar.getInstance().apply{
                time = Realm.getDefaultInstance().where<Event>()
                    .isNotEmpty("time").sort("time", Sort.DESCENDING).findFirst()?.time
            }

            var currentDay = Calendar.getInstance().apply {
                time = Realm.getDefaultInstance().where<Event>()
                    .isNotEmpty("time").sort("time", Sort.ASCENDING).findFirst()?.time
                set(Calendar.MINUTE, 0)
                set(Calendar.HOUR, 0)
            }

            while ( currentDay < lastDay ){
                DailyCycle().refreshDailyStack( currentDay )
                currentDay.add(Calendar.DATE, 1)
            }

        }
    }
}