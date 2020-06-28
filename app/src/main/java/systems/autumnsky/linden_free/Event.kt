package systems.autumnsky.linden_free

import android.app.Application
import android.app.TimePickerDialog
import android.content.Context
import android.icu.text.MessageFormat.format
import android.text.format.DateFormat.format
import android.text.format.DateUtils
import android.util.Log
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.core.view.forEach
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

    fun insert(action: String, timing: Calendar? = null, qty :Double? = null){
        val cal = when ( timing ){
            null -> Calendar.getInstance()
            else -> timing
        }

        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        Realm.getDefaultInstance().executeTransaction { realm ->
            val newId: Long = (realm.where<Event>().max("id")?.toLong()?:0) + 1
            val eventLog = realm.createObject<Event>(newId).apply{
                time = cal.time
                event_name = action
                quantity = qty
            }
            realm.copyToRealm(eventLog)
            realm.close()
        }
    }

    fun update(action: String, oldCal: Calendar, newCal: Calendar, qty: Double? = null ){
        newCal.set( Calendar.SECOND, 0)
        newCal.set( Calendar.MILLISECOND, 0)

         Realm.getDefaultInstance().executeTransaction { realm ->
            realm.where<Event>()
                .equalTo("time", oldCal.time)
                .equalTo("event_name", action)
                .findFirst()?.apply {
                    time = newCal.time
                    quantity = qty
                }
             realm.close()
            }
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
                insert( action )
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            true
        ).show()
    }

    fun insertMedicineLog( medicines: RecyclerView, timing :Calendar? = null ) {
        for (i in 0..medicines.childCount) {
            medicines.findViewHolderForLayoutPosition(i)?.let {
                val medicine =
                    it.itemView.findViewById<TextView>(R.id.medicine_name_with_spinner).text.toString()
                val quantity =
                    it.itemView.findViewById<Spinner>(R.id.adjust_spinner).selectedItem?.toString()
                        ?.toDoubleOrNull()

                insert(  action = medicine, timing = timing, qty = quantity)
            }
        }
    }

    fun insertMedicineLogByTimePicker( medicines: RecyclerView, button: Button): Calendar? {
        val cal = Calendar.getInstance()
        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, min ->
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, min)

            insertMedicineLog( medicines, cal)
        }

        TimePickerDialog(
            button.context,
            timeSetListener,
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            true
        ).show()
        return cal
    }

    fun updateMedicineLog( medicines: RecyclerView: button: Button, oldCal: Calendar) :Calendar?{

        return cal
    }

}
