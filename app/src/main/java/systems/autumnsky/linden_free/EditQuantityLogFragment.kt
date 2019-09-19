package systems.autumnsky.linden_free

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import io.realm.Realm
import io.realm.kotlin.where

class EditQuantityLogFragment : DialogFragment() {
    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = requireActivity().layoutInflater.inflate(R.layout.edit_log_quantity, null)

        arguments?.let{
            view.findViewById<TextView>(R.id.target_log_id).text = it.getString("Id")
            view.findViewById<TextView>(R.id.edit_quantity_medicine_label).text = it.getString("MedicineName")
            view.findViewById<EditText>(R.id.input_fixed_quantity).setText( it.getString("Quantity") )
        }

        val builder = AlertDialog.Builder(activity)
        builder
            .setView(view)
            .setPositiveButton("SAVE"){ _, _ ->
                val logId = view.findViewById<TextView>(R.id.target_log_id).text.toString().toLong()
                val realm = Realm.getDefaultInstance()
                val logRecord = realm.where<EventLog>().equalTo("id", logId).findFirst()
                realm.executeTransaction{
                        logRecord?.quantity = view.findViewById<EditText>(R.id.input_fixed_quantity).text.toString().toDouble()
                    }
                realm.close()
                }
            .setNegativeButton("CANCEL"){ _, _ ->
                dismiss()
                }
            .create()

        return builder.show()
    }
}

