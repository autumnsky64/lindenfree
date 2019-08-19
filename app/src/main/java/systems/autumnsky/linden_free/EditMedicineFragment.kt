package systems.autumnsky.linden_free


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import io.realm.Realm
import java.util.*


class EditMedicineFragment : DialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.edit_medicine, container, false)

        // 薬の一覧から呼び出されたら、薬情報を予め入力しておく
        if( arguments != null ) {
            view.findViewById<TextView>(R.id.id_container).text = arguments?.getString("MedicineId")
            view.findViewById<EditText>(R.id.input_medicine_name).setText(arguments?.getString("Name"))

            val quantity: Double? = arguments?.getDouble("Quantity")?:0.0
            val step: Double? = arguments?.getDouble("Step")?:0.0
            view.findViewById<EditText>(R.id.input_regular_quantity).setText(quantity.toString())
            view.findViewById<EditText>(R.id.input_adjustment_quantity).setText(step.toString())
        }

        // 下部のCancel/Saveボタン
        view.findViewById<Button>(R.id.save_medicine).setOnClickListener(insertMedicine())
        view.findViewById<Button>(R.id.cancel_medicine).setOnClickListener{ dismiss() }

        return view
    }
    private inner class insertMedicine: View.OnClickListener {
        override fun onClick(view: View?) {
            val medicineDialog = view?.parent as View

            val id : String =  medicineDialog.findViewById<TextView>(R.id.id_container).text.toString()
            val name: String? = medicineDialog.findViewById<EditText>(R.id.input_medicine_name).text?.toString()
            val quantity : Double? = medicineDialog.findViewById<EditText>(R.id.input_regular_quantity).text?.toString()?.toDoubleOrNull()
            val step : Double? = medicineDialog.findViewById<EditText>(R.id.input_adjustment_quantity).text?.toString()?.toDoubleOrNull()

            // 薬名の空欄チェック
            if( name.equals("") ) { return }

            val realm = Realm.getDefaultInstance()

            realm.beginTransaction()

            when (id) {
                // idが初期値の時はアクションバーの追加ボタンから呼ばれた → 追加
                "MedicineId" -> {
                    val medicine = realm.createObject(Medicine::class.java, UUID.randomUUID().toString())
                    medicine.name = name
                    medicine.regular_quantity = quantity
                    medicine.adjustment_step = step
                    realm.copyToRealm(medicine)

                    val event = realm.createObject(Event::class.java, UUID.randomUUID().toString())
                    event.name = name
                    event.medicine = medicine
                    realm.copyToRealm(event)
                }
                else -> {
                    // そうでない場合はIdに一致するレコードを更新
                    val medicine = realm.where(Medicine::class.java).equalTo("id",id).findFirst()
                    medicine?.name = name
                    medicine?.regular_quantity = quantity
                    medicine?.adjustment_step = step
                }
            }

            realm.commitTransaction()
            realm.close()

            dismiss()
        }

    }
}
