package systems.autumnsky.linden_free

import android.app.TimePickerDialog
import android.content.Context
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.ContextCompat.getColor
import androidx.recyclerview.widget.RecyclerView
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

    private fun update(action: String, oldCal: Calendar, newCal: Calendar, qty: Double? = null ){
        newCal.set( Calendar.SECOND, 0)
        newCal.set( Calendar.MILLISECOND, 0)

        val realm = Realm.getDefaultInstance()
        val found = realm.where<Event>()
                .equalTo("time", oldCal.time)
                .equalTo("event_name", action)
                .findFirst()

        if ( found == null){
            insert(action, newCal, qty)
        } else {
            realm.executeTransaction {
                found.apply {
                    time = newCal.time
                    quantity = qty
                }
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

            button.apply {
                text = button.context.getString( R.string.dose ).toString() + " " + android.text.format.DateFormat.format("HH:mm" ,cal.time).toString()
                setBackgroundColor(getColor(button.context, R.color.colorPrimary))
                setTextColor(getColor(button.context, R.color.materialLight))
            }
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

    fun updateMedicineLog( medicines: RecyclerView, button: Button, oldCal: Calendar? ) :Calendar?{
        val newCal = Calendar.getInstance()
        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, min ->
            newCal.set(Calendar.HOUR_OF_DAY, hour)
            newCal.set(Calendar.MINUTE, min)

            for (i in 0..medicines.childCount) {
                medicines.findViewHolderForLayoutPosition(i)?.let {
                    val medicine =
                        it.itemView.findViewById<TextView>(R.id.medicine_name_with_spinner).text.toString()
                    val quantity =
                        it.itemView.findViewById<Spinner>(R.id.adjust_spinner).selectedItem?.toString()
                            ?.toDoubleOrNull()

                    if( oldCal != null) {
                        update(medicine, oldCal, newCal, quantity)
                    }else{
                        insert(medicine, newCal, quantity)
                    }

                    button.text = button.context.getString( R.string.dose ).toString() + " " + android.text.format.DateFormat.format("HH:mm" ,newCal.time).toString()

                }
            }
        }

        TimePickerDialog(
            button.context,
            timeSetListener,
            newCal.get(Calendar.HOUR_OF_DAY),
            newCal.get(Calendar.MINUTE),
            true
        ).show()

        return newCal
    }

}
