package systems.autumnsky.linden_free

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
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
        val event = awakeButton.text.toString()

        awakeButton.setOnClickListener {
            MainActivity().insertLog( event, Calendar.getInstance() )

            Toast.makeText(
                context,
                "Good Morning! Recorded awake time.",
                Toast.LENGTH_LONG
            ).show()

            dismiss()
        }


        awakeButton.setOnLongClickListener { _ ->
            val cal = Calendar.getInstance()
            val timeSetListener = TimePickerDialog.OnTimeSetListener { TimePicker, hour, min ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, min)

                MainActivity().insertLog( event, cal )
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