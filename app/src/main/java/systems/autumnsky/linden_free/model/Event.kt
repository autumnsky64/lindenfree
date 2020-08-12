package systems.autumnsky.linden_free.model

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
import systems.autumnsky.linden_free.R
import java.util.*

open class Event (
    @PrimaryKey open var id: Long? = null,
    open var time: Date? = null,
    open var name: String? = null,
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
                name = action
                quantity = qty
            }
            realm.copyToRealm(eventLog)
            realm.close()
        }
        DailyCycle().refreshDailyStack(cal)
    }

    private fun update(action: String, oldCal: Calendar, newCal: Calendar, qty: Double? = null ){
        newCal.set( Calendar.SECOND, 0)
        newCal.set( Calendar.MILLISECOND, 0)

        val realm = Realm.getDefaultInstance()
        val found = realm.where<Event>()
                .equalTo("time", oldCal.time)
                .equalTo("name", action)
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

        DailyCycle().refreshDailyStack(newCal)
    }

    fun delete(id :Long){
        Realm.getDefaultInstance().apply{

            val day = Calendar.getInstance()
            where<Event>().equalTo("id", id).findFirst()?.time?.let{
                day.time = it
            }

            executeTransaction {
                where<Event>().equalTo("id", id).findAll().deleteAllFromRealm()
            }

            DailyCycle().refreshDailyStack(day)

        } .also { it.close() }
    }

    fun insertByTimePicker( action: String, context: Context, cal :Calendar = Calendar.getInstance()){
        TimePickerDialog(
            context,
            TimePickerDialog.OnTimeSetListener { _, hour, min ->
                cal.apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, min)
                }
                insert( action, cal)
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            true
        ).show()
    }

    fun insertMedicineLog( medicines: RecyclerView, timing :Calendar? = Calendar.getInstance() ) {
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

    fun insertMedicineLogByTimePicker( medicines: RecyclerView, button: Button, cal: Calendar): Calendar? {
        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, min ->
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, min)

            insertMedicineLog( medicines, cal)

            button.apply {
                text = button.context.getString(R.string.dose).toString() + " " + android.text.format.DateFormat.format("HH:mm" ,cal.time).toString()
                setBackgroundColor(getColor(button.context,
                    R.color.colorPrimary
                ))
                setTextColor(getColor(button.context,
                    R.color.materialLight
                ))
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
        if( oldCal != null) {
            newCal.time = oldCal.time
        }
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

                    button.text = button.context.getString(R.string.dose).toString() + " " + android.text.format.DateFormat.format("HH:mm" ,newCal.time).toString()

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
