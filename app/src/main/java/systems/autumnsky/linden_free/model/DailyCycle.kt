package systems.autumnsky.linden_free.model

import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.Sort
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import java.util.*

open class DailyCycle (
    open var day: Date? = null,
    open var cycleStack: RealmList<Cycle>? = null,
    open var medicineStack: RealmList<Cycle>? = null
): RealmObject() {

    fun insert (cal: Calendar) : DailyCycle? {

        val realm = Realm.getDefaultInstance()

        var lastDay = realm.where<DailyCycle>().lessThan("day", cal.time).sort("day", Sort.DESCENDING) .findFirst()?.day
                ?: Calendar.getInstance().apply { time = cal.time }.time

        // 現在からlastDayまで日付を遡りつつ1日ずつインサート
        var currentDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        while (lastDay <= currentDay.time) {
            realm.executeTransaction { realm ->
                realm.copyToRealm(
                    realm.createObject<DailyCycle>().apply {
                        day = currentDay.time
                    })
            }
            currentDay.add(Calendar.DAY_OF_MONTH, -1)
        }

        realm.close()

        return Realm.getDefaultInstance().where<DailyCycle>().equalTo("day", cal.time).findFirst()
    }

    fun refreshDailyStack(date: Calendar){

        val realm = Realm.getDefaultInstance()

        val day = Calendar.getInstance().apply{
            time = date.time
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val currentDayLastSec = Calendar.getInstance().apply {
            time = day.time
            set( Calendar.HOUR_OF_DAY, 23)
            set( Calendar.MINUTE, 59)
            set( Calendar.SECOND, 59)
        }

        //targetDayがなければインサート グラフ化するイベントがなくても日付レコードは必要
        val targetDay = realm.where<DailyCycle>().equalTo("day", day.time).findFirst() ?: insert(day)

        //sleep/awake イベント時刻を取得 イベントがなければここで終了
        val events = realm.where<Event>()
            .between("time", day.time, currentDayLastSec.time)
            .`in`("name", arrayOf("Sleep","Awake","起床","入眠"))
            .sort(arrayOf("time","id"), arrayOf(Sort.ASCENDING, Sort.ASCENDING))
            .findAll() ?: return

        realm.beginTransaction()

        var cycles = RealmList<Cycle>()

        events.mapIndexed { index, event ->
            val cycle = realm.createObject<Cycle>()

            cycle.activity = convertActivityName(event.name as String)

            if (index == 0) {
                cycle.length = event.time!!.time - day.timeInMillis
                cycle.startTime = day.time
            } else {
                val prevTime = events[index - 1]?.time!!
                cycle.length = event.time!!.time - prevTime.time
                cycle.startTime = prevTime
            }

            cycles.add(cycle)

            if (index == events.lastIndex) {
                val nextDay = Calendar.getInstance().apply { time = day.time }
                nextDay.add(Calendar.DATE, 1)

                val lastCycle = realm.createObject<Cycle>()
                lastCycle.apply{
                    activity = event.name as String
                    length = nextDay.timeInMillis - event.time!!.time
                    startTime = event.time
                }

                cycles.add(lastCycle)

                }
            }

        targetDay?.cycleStack = cycles

        realm.commitTransaction()

        //medicineStackの更新
        //登録している薬をArrayに
        val medicines :Array<String> = realm.where<Medicine>().findAll().map {
            it.name
        }.filterNotNull().toTypedArray()

        //服薬の時刻を取得
        val medicineEvents = realm.where<Event>()
            .between("time", day.time, currentDayLastSec.time)
            .`in`("name", medicines)
            .sort(arrayOf("time","id"), arrayOf(Sort.ASCENDING, Sort.ASCENDING))
            .findAll() ?: return

        realm.beginTransaction()

        var medicineCycles = RealmList<Cycle>()

        medicineEvents.forEach { event ->
            val cycle = realm.createObject<Cycle>()

            cycle.activity = event.name
            cycle.startTime = event.time

            medicineCycles.add(cycle)
        }

        targetDay?.medicineStack = medicineCycles

        realm.commitTransaction()
        realm.close()
    }

    private fun convertActivityName( action: String ) :String?{
        //getStringが使えないのと、4パターンしかないのでWhen構文にしている。汎用性に欠けるが。
        var activityName :String?

        when (action){
            "Sleep" -> activityName = "Awake"
            "Awake" -> activityName = "Sleep"
            "起床" -> activityName = "睡眠"
            "入眠" -> activityName = "覚醒"
            else -> activityName = null
        }

        return activityName
    }
}