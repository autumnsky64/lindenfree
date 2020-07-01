package systems.autumnsky.linden_free

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment

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
            Event().insert( awakeButton.text.toString())
            dismiss()
        }

        awakeButton.setOnLongClickListener { view ->
            Event().insertByTimePicker( awakeButton.text.toString(), view.context )
            dismiss()
            return@setOnLongClickListener true
        }

        return sleepingDialog
    }
}