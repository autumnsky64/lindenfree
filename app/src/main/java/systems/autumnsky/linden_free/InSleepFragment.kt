package systems.autumnsky.linden_free

import android.app.Dialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.edit_medicine.view.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class InSleepFragment : DialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val sleepingDialog = inflater.inflate(R.layout.in_sleep_dialog, container, false)

        //入眠時刻を表示
        if( arguments != null ) {
            sleepingDialog.findViewById<TextView>(R.id.in_sleep_time).text = arguments?.getString("InSleepTime")
        }

        //起床ボタンをタップで時刻を入力、長押しでタイムピッカーから入力して終了
        val awakeButton = sleepingDialog.findViewById<Button>(R.id.awake_button)

        awakeButton.setOnClickListener {
            val realm = Realm.getDefaultInstance()

            realm.executeTransaction {
                var id = realm.where<EventLog>().count() + 1
                val log = realm.createObject<EventLog>(id)

                log.event_name = getString(R.string.awake)
                log.time = Calendar.getInstance().time

                realm.copyToRealmOrUpdate(log)
            }

            realm.close()

            Toast.makeText(
                context,
                "Good Morning! Recorded awake time.",
                Toast.LENGTH_LONG
            ).show()

            dismiss()
        }

        return sleepingDialog

        // TODO:ダイアログフラグメントからダイアログフラグメントは直接は呼び出せない
/*        awakeButton.setOnLongClickListener { view: View ->
            val cal = Calendar.getInstance()
            val timeSetListener = TimePickerDialog.OnTimeSetListener { TimePicker, hour, min ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, min)

                val realm = Realm.getDefaultInstance()
                var id = realm.where<EventLog>().count() + 1
                val log = realm.createObject<EventLog>(id)

                realm.executeTransaction{
                    log.event_name = getString(R.string.awake)
                    log.time = Calendar.getInstance().time
                }
            }

            TimePickerDialog(
            view.context,
            timeSetListener,
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),true).show(),
                )

        }*/
    }
}