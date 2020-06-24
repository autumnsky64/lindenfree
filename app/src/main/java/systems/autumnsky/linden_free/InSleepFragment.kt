package systems.autumnsky.linden_free

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import java.util.*

class InSleepFragment : DialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val sleepingDialog = inflater.inflate(R.layout.in_sleep_dialog, container, false)

        //入眠時刻を表示
        arguments?.let{
            sleepingDialog.findViewById<TextView>(R.id.in_sleep_time).text = it.getString("InSleepTime")
        }

        //起床ボタンをタップで時刻を入力、長押しでタイムピッカーから入力して終了
        val awakeButton = sleepingDialog.findViewById<Button>(R.id.awake_button)

        awakeButton.setOnClickListener {
            val realm = Realm.getDefaultInstance()
            val newId: Long = (realm.where<Event>().max("id")?.toLong()?:0) + 1

            realm.executeTransaction{
                val newRecord = realm.createObject<Event>(newId).apply {
                    time = Calendar.getInstance().time
                    event_name = awakeButton.text.toString()
                }
                realm.copyToRealm(newRecord)
            }
            realm.close()
            dismiss()
        }

        awakeButton.setOnLongClickListener { _ ->
            val cal = Calendar.getInstance()
            val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, min ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, min)

                val realm = Realm.getDefaultInstance()
                val newId: Long = (realm.where<Event>().max("id")?.toLong()?:0) + 1

                realm.executeTransaction{
                    val newRecord = realm.createObject<Event>(newId).apply {
                        time = cal.time
                        event_name = awakeButton.text.toString()
                    }
                    realm.copyToRealm(newRecord)
                }
                realm.close()
            }

            TimePickerDialog(
                activity,
                timeSetListener,
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
            ).show()
            dismiss()

            return@setOnLongClickListener true
        }

        return sleepingDialog
    }
}