package systems.autumnsky.linden_free.model

import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.Sort
import io.realm.kotlin.createObject
import io.realm.kotlin.oneOf
import io.realm.kotlin.where
import java.util.*

open class DailyActivity(
    open var day: Date? = null,
    open var activityStack: RealmList<Activity>? = null,
) : RealmObject() {

    fun insert(cal: Calendar): DailyActivity? {

        val realm = Realm.getDefaultInstance()

        val lastDay = realm.where<DailyActivity>().maximumDate("day")
                ?: Calendar.getInstance().apply { time = cal.time }.time


        var startDay: Calendar
        var targetDay: Calendar

        if ( lastDay < cal.time ) {
            //前回記録日より記録しようとする日が新しい場合、実際の利用ではほぼこちらのはず
            startDay = Calendar.getInstance().apply{
                time = lastDay
                add( Calendar.DAY_OF_MONTH, +1)
            }
            targetDay = Calendar.getInstance().apply {
                time = cal.time
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
        } else {
            //既に記録した一番古い日より、古い日付の記録を入れる時
            startDay = Calendar.getInstance().apply { time = cal.time }
            targetDay = Calendar.getInstance().apply {
                realm.where<DailyActivity>().minimumDate("day")?.let { day ->
                    time = day
                    add(Calendar.DAY_OF_MONTH, -1)
                }
            }
        }

        while (startDay.time <= targetDay.time) {
            realm.executeTransaction {
                it.copyToRealm(
                    it.createObject<DailyActivity>().apply {
                        day = startDay.time
                    })
            }
            startDay.add(Calendar.DAY_OF_MONTH, +1)
        }

        val result = realm.where<DailyActivity>().equalTo("day", cal.time) .findFirst()
        realm.close()

        return result
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
            .oneOf("name", arrayOf("Sleep", "Awake", "起床", "入眠"))
            .sort("time", Sort.ASCENDING)
            .findAll() ?: return

        realm.beginTransaction()

        var activities = RealmList<Activity>()

        events.forEachIndexed { index, event ->
            if (index == 0) {
                if (event.name == "Awake" || event.name == "起床") {
                    activities.add(
                        realm.createObject<Activity>().apply {
                            name = findActivityName(event.name as String)
                            length = event.time!!.time - currentDay.timeInMillis
                            startTime = currentDay.time
                            endTime = event.time!!
                        })
                }
            } else {
                val preventEvent = events[index - 1]?.name
                if ( ( preventEvent == "Awake" ) xor ( event.name == "Sleep")) {
                    activities.add(
                        realm.createObject<Activity>().apply {
                            name = findActivityName(event.name as String)
                            length = event.time!!.time - events[index - 1]?.time!!.time
                            startTime = events[index - 1]?.time
                            endTime = event.time
                    })
                }
            }
            if (index == events.size - 1 && event.name == "Sleep") {
                val nextDay = Calendar.getInstance().apply { time = currentDay.time }
                nextDay.add(Calendar.DAY_OF_MONTH, 1)

                val lastAct = realm.createObject<Activity>()
                lastAct.apply {
                    name = event.name as String
                    length = nextDay.timeInMillis - event.time!!.time
                    startTime = event.time
                    endTime = currentDayLastSec.time
                }
                activities.add(lastAct)
            }
        }

        //登録している薬をArrayに
        val registeredMedicines: Array<String> =
            realm.where<Medicine>().findAll().mapNotNull { it.name }.toTypedArray()

        //服薬の時刻を取得
        val loggedMedicines = realm.where<Event>()
            .between("time", currentDay.time, currentDayLastSec.time)
            .oneOf("name", registeredMedicines)
            .sort(arrayOf("time", "id"), arrayOf(Sort.ASCENDING, Sort.ASCENDING))
            .findAll()

        if( loggedMedicines != null) {
            val initialActivity = realm.createObject<Activity>()
            loggedMedicines.fold(initialActivity, { acc, event ->
                when (acc.startTime) {
                    null -> {
                        acc.name = "TookMedicine"
                        acc.startTime = event.time
                        acc.endTime =event.time
                        val medicine = realm.createObject<TakenMedicine>().apply {
                            name = event.name
                            quantity = event.quantity
                        }
                        acc.medicines = RealmList<TakenMedicine>().apply{ add(medicine)}

                        activities.add(acc)

                        return@fold activities.last()!!
                    }
                    event.time -> {
                        val medicine = realm.createObject<TakenMedicine>().apply {
                            name = event.name
                            quantity = event.quantity
                        }
                        activities.last()?.medicines?.add(medicine)
                        return@fold acc
                    }
                    else -> {
                        val activity = prepareMedicineActivity(
                            event.name!!,
                            event.time!!,
                            event.quantity!!
                        )
                        activities.add(activity)
                        return@fold  activities.last()!!
                    }
                }
            })
        }

        targetDay?.activityStack = activities

        realm.commitTransaction()
        realm.close()
    }

    private fun prepareMedicineActivity( argName: String?, argTime: Date?, argQuantity: Double?): Activity{
        val realm = Realm.getDefaultInstance()
        val medicine = realm.createObject<TakenMedicine>().apply {
                name = argName
                quantity = argQuantity
            }

        val takenMedicines = RealmList<TakenMedicine>().apply { add(medicine) }

        return realm.createObject<Activity>().apply{
            name = "TookMedicine"
            startTime = argTime
            endTime = argTime
            medicines = takenMedicines
        }
    }

    private fun findActivityName(action: String): String? {
        //getStringが使えないのと、4パターンしかないのでWhen構文にしている。汎用性に欠けるが。
        return when (action) {
            "Sleep" ->  "Awake"
            "Awake" ->  "Sleep"
            "起床" ->  "睡眠"
            "入眠" ->  "覚醒"
            else ->  null
        }
    }
}