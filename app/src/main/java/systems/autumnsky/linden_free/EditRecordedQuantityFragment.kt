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
import systems.autumnsky.linden_free.model.Event

class EditRecordedQuantityFragment : DialogFragment() {
    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = requireActivity().layoutInflater.inflate(R.layout.edit_log_quantity, null)
        val builder = AlertDialog.Builder(activity)

        arguments?.let {
            view.findViewById<TextView>(R.id.edit_quantity_medicine_label).text =
                it.getString("MedicineName")
            view.findViewById<EditText>(R.id.input_fixed_quantity).setText(it.getString("Quantity"))

            builder.apply {
                setView(view)
                setPositiveButton("SAVE") { _, _ ->
                    val realm = Realm.getDefaultInstance()
                    val logRecord =
                        realm.where<Event>().equalTo("id", it.getString("Id")!!.toInt()).findFirst()

                    realm.executeTransaction {
                        logRecord?.quantity =
                            view.findViewById<EditText>(R.id.input_fixed_quantity).text.toString()
                                .toDouble()
                    }

                    realm.close()
                }

                setNegativeButton("CANCEL") { _, _ ->
                    dismiss()
                }
                create()
            }
        }
        return builder.show()
    }
}


