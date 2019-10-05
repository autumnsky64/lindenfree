package systems.autumnsky.linden_free


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
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
            view.findViewById<EditText>(R.id.input_medicine_name).setText(it.getString("Name"))

            val quantity: Double? = it.getDouble("Quantity")
            val step: Double? = it.getDouble("Step")

            if ( quantity != 0.0 ){ view.findViewById<EditText>(R.id.input_regular_quantity).setText(DecimalFormat("#.##").format(quantity))}
            if ( step != 0.0 ){ view.findViewById<EditText>(R.id.input_adjustment_step).setText(DecimalFormat("#.##").format(step)) }

            }

        // 下部のCancel/Saveボタン
        view.findViewById<Button>(R.id.save_medicine).setOnClickListener(AddMedicine(arguments?.getString("MedicineId")))
        view.findViewById<Button>(R.id.cancel_medicine).setOnClickListener{ dismiss() }

        return view
    }
    private inner class AddMedicine( targetId: String? ): View.OnClickListener {
        val targetId: String? = targetId
        override fun onClick(view: View?) {
            val medicineDialog = view?.parent as View
            val editedName = medicineDialog.findViewById<TextView>(R.id.input_medicine_name).text?.toString()
            val editedQuantity = medicineDialog.findViewById<EditText>(R.id.input_regular_quantity).text?.toString()?.toDoubleOrNull()
            val editedStep = medicineDialog.findViewById<EditText>(R.id.input_adjustment_step).text?.toString()?.toDoubleOrNull()

            // 薬名の空欄チェック
            if( editedName == "" ) { return }

            val realm = Realm.getDefaultInstance()

            if ( targetId != null ) {
                realm.executeTransaction{
                    val targetMedicine = realm.where<Medicine>().equalTo("id", targetId).findFirst()
                    targetMedicine?.apply {
                        name = editedName
                        regular_quantity = editedQuantity
                        adjustment_step = editedStep
                    }

                    realm.where<Event>().equalTo("medicine.id", targetId.toString()).findFirst()?.apply {
                        medicine = targetMedicine
                        name = editedName
                    }
                }
            } else {
                // idがnullならaddボタンからの追加 → Insert
                realm.executeTransaction {
                    val editedMedicine =
                        realm.createObject<Medicine>(UUID.randomUUID().toString()).apply {
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
            realm.close()

            if ( (activity?.application as LindenFreeApp).isFirstLaunch ) {
                showMedicineRowTutorial()
            }

            dismiss()
        }

        //初回チュートリアル用バルーン
        private fun showMedicineRowTutorial() {
            activity?.apply{
                if ( Realm.getDefaultInstance().where<Medicine>().findAll().count() == 1) {
                    //薬が一つ登録されている時は、リサイクラービューの操作チップを表示
                    findViewById<TextView>(R.id.description_medicine_row)?.visibility = View.VISIBLE
                    findViewById<ImageView>(R.id.arrow_medicine_row)?.visibility = View.VISIBLE
                    findViewById<TextView>(R.id.description_to_home)?.visibility = View.VISIBLE
                    findViewById<ImageView>(R.id.arrow_to_home)?.visibility = View.VISIBLE
                } else {
                    //薬が2つ以上なら非表示、バルーンが2行目に被る
                    findViewById<TextView>(R.id.description_medicine_row)?.let{ it.visibility = View.INVISIBLE }
                    findViewById<ImageView>(R.id.arrow_medicine_row)?.let{ it.visibility = View.INVISIBLE }
                }
            }
        }
    }
}
