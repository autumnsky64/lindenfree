package systems.autumnsky.linden_free


import android.content.Context
import android.os.Bundle

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.activity_main.*


class EditMedicineFragment : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.edit_medicine, container, false)
        val quantity: DoubleArray? = arguments?.getDoubleArray("Quantity")

        view.findViewById<EditText>(R.id.input_medicine_name).setText(arguments?.getString("Name"))
        view.findViewById<EditText>(R.id.input_regular_quantity).setText(quantity!![0].toString())
        view.findViewById<EditText>(R.id.input_adjustment_quantity
        ).setText(quantity!![1].toString())

        view.findViewById<Button>(R.id.save_medicine).setOnClickListener(InsertMedicine())
        view.findViewById<Button>(R.id.cancel_medicine).setOnClickListener( View.OnClickListener { dismiss() })
        return view
    }

    inner class InsertMedicine() : View.OnClickListener{
        override fun onClick(view: View?) {

            val medicine = view?.parent as View

            // Medicineテーブルへ書込
            val id = arguments?.getInt("MedicineId")

            try {
                val name = medicine.findViewById<EditText>(R.id.input_medicine_name)!!.text.toString()
                val quantity = medicine.findViewById<EditText>(R.id.input_regular_quantity)!!.text.toString()
                val step = medicine.findViewById<EditText>(R.id.input_adjustment_quantity)!!.text.toString()
                Log.d("Medicine", "$id $name $quantity mg adjust step $step mg")
                dismiss()
            } catch ( e:NullPointerException) {
                Log.d( "Medicine", "どれかのEditTextが空")
            }
        }
    }

}
