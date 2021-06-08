package systems.autumnsky.linden_free

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.realm.OrderedRealmCollection
import io.realm.Realm
import io.realm.kotlin.where
import systems.autumnsky.linden_free.model.Action
import systems.autumnsky.linden_free.model.Event
import systems.autumnsky.linden_free.model.Medicine
import java.text.DecimalFormat
import java.util.*

class BottomSheetActionList(
    isDatePicker: Boolean = true,
    isTimePicker: Boolean = false,
    day: Date? = null
) : BottomSheetDialogFragment() {

    val isDatePicker = isDatePicker
    val isTimePicker = isTimePicker
    val date = day

    val realm: Realm= Realm.getDefaultInstance()
    val actions = realm.where<Action>().isNull("medicine").findAll()
    val medicine = realm.where<Medicine>().equalTo("is_use_as_needed", true).findAll()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_action_list, container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<RecyclerView>(R.id.action_list).run {
            adapter = ActionListAdapter(actions)
            layoutManager = GridLayoutManager(activity, 2)
        }
        view.findViewById<RecyclerView>(R.id.use_as_need_medicine_list).run {
            adapter = MedicineListAdapter(medicine)
            layoutManager = GridLayoutManager(activity, 1)
        }
    }

    private inner class ActionListHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val action: ConstraintLayout = itemView.findViewById(R.id.action_row_in_bottom_sheet)
        val name: TextView = itemView.findViewById(R.id.action_name_in_bottom_sheet)
    }

    private inner class ActionListAdapter(private val actionList: OrderedRealmCollection<Action>) :
        RecyclerView.Adapter<ActionListHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActionListHolder {
            val row = LayoutInflater.from(context)
                .inflate(R.layout.action_row_in_bottom_sheet, parent, false)
            return ActionListHolder(row)
        }

        override fun onBindViewHolder(holder: ActionListHolder, position: Int) {

            val name = actionList[position]?.name ?: return

            val cal = Calendar.getInstance()
            if (this@BottomSheetActionList.date != null) {
                cal.time = date
            }

            holder.name.text = name
            holder.action.setOnClickListener { view ->

                when {
                    isDatePicker -> {
                        Event().insertByDatePicker(name, view.context, cal)
                    }
                    isTimePicker -> {
                        Event().insertByTimePicker(name, view.context, cal)
                    }
                    else -> {
                        Event().insert(name, cal)
                    }
                }

                dismiss()
            }

            if (!isDatePicker) {
                holder.action.setOnLongClickListener { view ->
                    Event().insertByTimePicker(name, view.context, cal)
                    dismiss()
                    return@setOnLongClickListener false
                }
            }
        }

        override fun getItemCount(): Int {
            return actionList.size
        }
    }

    private inner class MedicineListHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val action: ConstraintLayout = itemView.findViewById(R.id.medicine_row_for_card)
        val name: TextView = itemView.findViewById(R.id.medicine_name_inner_card)
        val quantity: TextView = itemView.findViewById(R.id.medicine_quantity_inner_card)
        val unitLabel: TextView = itemView.findViewById(R.id.medicine_unit_label_inner_card)
    }

    private inner class MedicineListAdapter(private val medicineList: OrderedRealmCollection<Medicine>) :
        RecyclerView.Adapter<MedicineListHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicineListHolder {
            val row = LayoutInflater.from(context)
                .inflate(R.layout.card_medicine_row, parent, false)
            return MedicineListHolder(row)
        }

        override fun onBindViewHolder(holder: MedicineListHolder, position: Int) {

            val name = medicineList[position]?.name ?: return

            val cal = Calendar.getInstance()
            if (this@BottomSheetActionList.date != null) {
                cal.time = date
            }

            val layout = holder.name.layoutParams as ViewGroup.MarginLayoutParams
            layout.topMargin = 24

            holder.name.text = name

            val qty = medicineList[position]?.regular_quantity
            qty?.let{
                holder.apply {
                    unitLabel.visibility = View.VISIBLE
                    quantity.visibility = View.VISIBLE
                    quantity.text = DecimalFormat("#.##").format(it)
                }
            }

            holder.action.setOnClickListener { view ->

                when {
                    isDatePicker -> {
                        Event().insertByDatePicker(name, view.context, cal, qty )
                    }
                    isTimePicker -> {
                        Event().insertByTimePicker(name, view.context, cal, qty)
                    }
                    else -> {
                        Event().insert(name, cal, qty)
                    }
                }

                dismiss()
            }

            if (!isDatePicker) {
                holder.action.setOnLongClickListener { view ->
                    Event().insertByTimePicker(name, view.context, cal, qty)
                    dismiss()
                    return@setOnLongClickListener false
                }
            }
        }

        override fun getItemCount(): Int {
            return medicineList.size
        }
    }
}
