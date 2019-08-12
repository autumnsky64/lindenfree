package systems.autumnsky.linden_free


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.*


class EditMedicineFragment : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val mmid = arguments?.getInt("MedicineId")
        Log.d( "MedicineId" , mmid.toString())
        return inflater.inflate(R.layout.edit_medicine, container, false)
    }
}
