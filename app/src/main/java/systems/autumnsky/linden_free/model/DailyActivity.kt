package systems.autumnsky.linden_free.model

import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.Sort
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import java.util.*

open class DailyActivity(
    open var day: Date? = null,
    open var activityStack: RealmList<Activity>? = null,
    open var medicineStack: RealmList<Activity>? = null
) : RealmObject() {

    fun insert(cal: Calendar): DailyActivity? {

        val realm = Realm.getDefaultInstance()

        val lastDay = realm.where<DailyActivity>().maximumDate("day")
                ?: Calendar.getInstance().apply { time = cal.time }.time


        var startDay: Calendar
        var targetDay: Calendar

        if ( lastDay > cal.time ) {
            //既に記録した一番古い日より、古い日付の記録を入れる時
            startDay = Calendar.getInstance().apply{ time = cal.time }
            targetDay = Calendar.getInstance().apply {
                realm.where<DailyActivity>().minimumDate("day").let {
                    time = it
                    add(Calendar.DAY_OF_MONTH, -1)
                }
            }
        } else {
            //前回記録日より記録しようとする日が新しい場合、実際の利用ではほぼこちらのはず
            startDay = Calendar.getInstance().apply{ time = lastDay }
            targetDay = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
        }

        // 現在からlastDayまで日付を遡りつつ1日ずつインサート
        while (startDay.time <= targetDay.time) {
            realm.executeTransaction {
                it.copyToRealm(
                    it.createObject<DailyActivity>().apply {
                        day = startDay.time
                    })
            }
            startDay.add(Calendar.DAY_OF_MONTH, +1)
        }

        realm.close()

        return Realm.getDefaultInstance().where<DailyActivity>().equalTo("day", cal.time) .findFirst()
    }

    fun refreshDailyStack(date: Calendar) {

        val realm = Realm.getDefaultInstance()

        val currentDay = Calendar.getInstance().apply {
            time = date.time
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val currentDayLastSec = Calendar.getInstance().apply {
            time = currentDay.time
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }

        //targetDayがなければインサート グラフ化するイベントがなくても日付レコードは必要
        val targetDay =
            realm.where<DailyActivity>().equalTo("day", currentDay.time).findFirst() ?: insert(
                currentDay
            )

        //sleep/awake イベント時刻を取得 イベントがなければここで終了
        val events = realm.where<Event>()
            .between("time", currentDay.time, currentDayLastSec.time)
            .`in`("name", arrayOf("Sleep", "Awake", "起床", "入眠"))
            .sort(arrayOf("time", "id"), arrayOf(Sort.ASCENDING, Sort.ASCENDING))
            .findAll() ?: return

        realm.beginTransaction()

        var activities = RealmList<Activity>()

        events.forEachIndexed { index, event ->
            val activity = realm.createObject<Activity>()

            activity.name = findActivityName(event.name as String)

            if (index == 0) {
                activity.length = event.time!!.time - currentDay.timeInMillis
                activity.startTime = currentDay.time
            } else {
                val prevTime = events[index - 1]?.time!!
                activity.length = event.time!!.time - prevTime.time
                activity.startTime = prevTime
            }

            activities.add(activity)

            if (index == events.lastIndex) {
                val nextDay = Calendar.getInstance().apply { time = currentDay.time }
                nextDay.add(Calendar.DATE, 1)

                val lastAct = realm.createObject<Activity>()
                lastAct.apply {
                    name = event.name as String
                    length = nextDay.timeInMillis - event.time!!.time
                    startTime = event.time
                }

                activities.add(lastAct)

            }
        }

        targetDay?.activityStack = activities

        realm.commitTransaction()

        //medicineStackの更新
        //登録している薬をArrayに
        val medicines: Array<String> = realm.where<Medicine>().findAll().mapNotNull {
            it.name
        }.toTypedArray()

        //服薬の時刻を取得
        val takenMedicines = realm.where<Event>()

            .between("time", currentDay.time, currentDayLastSec.time)
            .`in`("name", medicines)
            .sort(arrayOf("time", "id"), arrayOf(Sort.ASCENDING, Sort.ASCENDING))
            .findAll() ?: return

        realm.beginTransaction()

        var medicineActivities = RealmList<Activity>()

        takenMedicines.forEach { event ->
            val activity = realm.createObject<Activity>()

            activity.name = event.name
            activity.startTime = event.time

            medicineActivities.add(activity)
        }

        targetDay?.medicineStack = medicineActivities

        realm.commitTransaction()
        realm.close()
    }

    private fun findActivityName(action: String): String? {
        //getStringが使えないのと、4パターンしかないのでWhen構文にしている。汎用性に欠けるが。
        var activityName: String?

        when (action) {
            "Sleep" -> activityName = "Awake"
            "Awake" -> activityName = "Sleep"
            "起床" -> activityName = "睡眠"
            "入眠" -> activityName = "覚醒"
            else -> activityName = null
        }

        return activityName
    }
}