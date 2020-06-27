package systems.autumnsky.linden_free

import android.app.Application
import android.app.TimePickerDialog
import android.content.Context
import android.icu.text.MessageFormat.format
import android.text.format.DateFormat.format
import android.text.format.DateUtils
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import io.realm.Realm
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import java.text.DateFormat
import java.util.*

open class Event (
    @PrimaryKey open var id: Long? = null,
    open var time: Date? = null,
    open var event_name: String? = null,
    open var quantity: Double? = null
): RealmObject() {

    fun insert(timing: Calendar, name :String, qty :Double? = null){
        val realm = Realm.getDefaultInstance()
        val newId: Long = (realm.where<Event>().max("id")?.toLong()?:0) + 1

        timing.set( Calendar.SECOND, 0)
        timing.set( Calendar.MILLISECOND, 0)
        realm.executeTransaction {
            val eventLog = realm.createObject<Event>(newId).apply{
                time = timing.time
                event_name = name
                quantity = qty
            }
            realm.copyToRealm(eventLog)
        }
        realm.close()
    }

    fun update(event: String, oldCal: Calendar, newCal: Calendar, qty: Double? = null ){
        newCal.set( Calendar.SECOND, 0)
        newCal.set( Calendar.MILLISECOND, 0)

        val realm = Realm.getDefaultInstance()
        realm.executeTransaction{
            realm.where<Event>()
                .equalTo("time", oldCal.time)
                .equalTo("event_name", event)
                .findFirst()?.apply {
                    time = newCal.time
                    quantity = qty
                }
            }
        realm.close()
    }

    fun insertByTimePicker( action: String, context: Context){
        val cal = Calendar.getInstance()
        TimePickerDialog(
            context,
            TimePickerDialog.OnTimeSetListener { _, hour, min ->
                cal.apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, min)
                }
                insert( cal, action )
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            true
        ).show()
    }
}
