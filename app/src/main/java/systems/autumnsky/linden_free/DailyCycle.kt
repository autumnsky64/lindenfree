package systems.autumnsky.linden_free

import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.kotlin.createObject
import java.util.*

open class DailyCycle (
    open var day: Date? = null,
    open var stack: RealmList<Cycle>? = null
): RealmObject() {

    fun insert (action: String, time: Calendar){
        Realm.getDefaultInstance().executeTransaction{ realm ->
            time.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
            }

            val cycle = realm.createObject<Cycle>().apply {
                activity = action
                length = 60
            }

            val dailyCycle = realm.createObject<DailyCycle>().apply{
                day = time.time
                stack?.add(cycle)
            }

            realm.copyToRealm(dailyCycle)
            realm.close()
        }
    }
}