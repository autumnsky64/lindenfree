package systems.autumnsky.linden_free

import io.realm.Realm
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import java.util.*

open class Event (
    @PrimaryKey open var id: Long? = null,
    open var time: Date? = null,
    open var event_name: String? = null,
    open var quantity: Double? = null
): RealmObject() {

    fun insert(event: String?, cal: Calendar){
        val realm = Realm.getDefaultInstance()
        val newId: Long = (realm.where<Event>().max("id")?.toLong()?:0) + 1

        realm.executeTransaction {
            val eventLog = realm.createObject<Event>(newId).apply{
                time = cal.time
                event_name = event
            }
            realm.copyToRealm(eventLog)
        }
        realm.close()
    }

    fun update(event: String?, oldDate: Date, newCal: Calendar){
        val realm = Realm.getDefaultInstance()

        realm.executeTransaction{
            realm.where<Event>()
                .greaterThanOrEqualTo("time", oldDate)
                .equalTo("event_name", event)
                .findFirst()?.apply {

                    time = newCal.time

                }
        }

        realm.close()
    }
}
