package systems.autumnsky.linden_free

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import io.realm.Realm
import io.realm.Sort
import io.realm.kotlin.where

class InSleepFragment : DialogFragment() {
    override fun onCreateDialog(SavedInstanceState: Bundle?): Dialog {
        return activity?.let {

            val builder = AlertDialog.Builder(it)
            val body = requireActivity().layoutInflater.inflate(R.layout.in_sleep_dialog, null)

            builder.setView(body)
            //入眠時刻を表示
            arguments?.let{
                body.findViewById<TextView>(R.id.in_sleep_time).text = it.getString("InSleepTime")
            }

            //起床ボタンをタップで時刻を入力、長押しでタイムピッカーから入力して終了
            val awakeButton = body.findViewById<Button>(R.id.awake_button)

            awakeButton.setOnClickListener {
                Event().insert( awakeButton.text.toString())
                dismiss()
            }

            awakeButton.setOnLongClickListener { view ->
                Event().insertByTimePicker( awakeButton.text.toString(), view.context )
                dismiss()
                return@setOnLongClickListener true
            }
            builder.setNegativeButton("Cancel", DialogInterface.OnClickListener { _, _ ->
                val key = arrayOf("time", "id")
                val sort = arrayOf( Sort.DESCENDING, Sort.DESCENDING)
                Realm.getDefaultInstance().apply{
                    executeTransaction {
                        where<Event>().equalTo("name", getString(R.string.sleep)).sort(key, sort).findFirst()?.deleteFromRealm()
                    }
                } .also { it.close() }
            })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}