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
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import java.text.DecimalFormat
import java.util.*


class EditMedicineFragment : DialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.edit_medicine, container, false)

        // 薬の一覧から呼び出されたら、薬情報を予め入力しておく
        arguments?.let{
            view.findViewById<TextView>(R.id.medicine_id).text = it.getString("MedicineId")
            view.findViewById<EditText>(R.id.input_medicine_name).setText(it.getString("Name"))

            val quantity: Double? = it.getDouble("Quantity")
            val step: Double? = it.getDouble("Step")

            if ( quantity != 0.0 ){ view.findViewById<EditText>(R.id.input_regular_quantity).setText(DecimalFormat("#.##").format(quantity))}
            if ( step != 0.0 ){ view.findViewById<EditText>(R.id.input_adjustment_step).setText(DecimalFormat("#.##").format(step)) }
            }

        // 下部のCancel/Saveボタン
        // Todo: builderでsetPositive/cancelに変更する
        view.findViewById<Button>(R.id.save_medicine).setOnClickListener(AddMedicine())
        view.findViewById<Button>(R.id.cancel_medicine).setOnClickListener{ dismiss() }

        return view
    }
    private inner class AddMedicine: View.OnClickListener {
        override fun onClick(view: View?) {
            val medicineDialog = view?.parent as View
            val id = medicineDialog.findViewById<TextView>(R.id.medicine_id).text?.toString()
            val editedName = medicineDialog.findViewById<TextView>(R.id.input_medicine_name).text?.toString()
            val editedQuantity = medicineDialog.findViewById<EditText>(R.id.input_regular_quantity).text?.toString()?.toDoubleOrNull()
            val editedStep = medicineDialog.findViewById<EditText>(R.id.input_adjustment_step).text?.toString()?.toDoubleOrNull()

            // 薬名の空欄チェック
            if( editedName == "" ) { return }

            val realm = Realm.getDefaultInstance()

            when ( id ) {
                // idが初期値の時はアクションバーの追加ボタンから呼ばれた → Insert
                "MedicineId" -> {
                    realm.executeTransaction{
                        val editedMedicine = realm.createObject<Medicine>(UUID.randomUUID().toString()).apply{
                            name = editedName
                            regular_quantity = editedQuantity
                            adjustment_step = editedStep
                        }
                        realm.copyToRealm(editedMedicine)

                        val event = realm.createObject<Event>(UUID.randomUUID().toString()).apply {
                            name = editedName
                            medicine = editedMedicine
                        }
                        realm.copyToRealm(event)

                    }
                }
                else -> {
                    // そうでない場合はIdに一致するレコードを更新
                    realm.executeTransaction{
                        val targetMedicine = realm.where<Medicine>().equalTo("id",id).findFirst()
                        targetMedicine?.apply {
                            name = editedName
                            regular_quantity = editedQuantity
                            adjustment_step = editedStep
                            }

                        realm.where<Event>().equalTo("medicine.id",id).findFirst()?.apply {
                            medicine = targetMedicine
                            name = editedName
                        }
                    }
                }
            }

            realm.close()

            dismiss()
        }

    }
}
