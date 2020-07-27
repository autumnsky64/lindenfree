package systems.autumnsky.linden_free

import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.Sort
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import java.util.*

open class DailyCycle (
    open var day: Date? = null,
    open var stack: RealmList<Cycle>? = null
): RealmObject() {

    fun insert (cal: Calendar) : DailyCycle? {
        Realm.getDefaultInstance().executeTransaction{ realm ->
            realm.copyToRealm(
                realm.createObject<DailyCycle>().apply{
                    day = cal.time
            })
            realm.close()
        }
        return realm.where<DailyCycle>().equalTo("day", cal.time).findFirst()
    }

    fun refreshDailyStack(day: Calendar){

        val realm = Realm.getDefaultInstance()

        day.apply{
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
        }

        val currentDayLastSec = Calendar.getInstance().apply {
            time = day.time
            set( Calendar.HOUR_OF_DAY, 23)
            set( Calendar.MINUTE, 59)
            set( Calendar.SECOND, 59)
        }

        //sleep/awake イベント時刻を取得
        val events = realm.where<Event>()
            .between("time", day.time, currentDayLastSec.time)
            .`in`("name", arrayOf("Sleep","Awake"))
            .sort(arrayOf("time","id"), arrayOf(Sort.ASCENDING, Sort.ASCENDING))
            .findAll()

        realm.beginTransaction()

        var cycles = RealmList<Cycle>()

        events.mapIndexed { index, event ->
            val cycle = realm.createObject<Cycle>()

            if (index == 0) {
                cycle.length = event.time!!.time - day.timeInMillis
            } else {
                val prevTime = events[index - 1]?.time!!.time
                cycle.length = event.time!!.time - prevTime
            }

            cycles.add(cycle)

            if (index == events.lastIndex) {
                val nextDay = Calendar.getInstance().apply { time = day.time }
                nextDay.add(Calendar.DATE, 1)

                val lastCycle = realm.createObject<Cycle>()
                lastCycle.length = nextDay.timeInMillis - event.time!!.time

                cycles.add(lastCycle)
            }
        }

        //targetDayがなければインサート
        val targetDay = realm.where<DailyCycle>().equalTo("day", day.time).findFirst()?.let{
            insert(day)
        }

        targetDay?.stack = cycles

        realm.commitTransaction()
        realm.close()
    }
}