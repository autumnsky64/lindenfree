package systems.autumnsky.linden_free

import android.app.DatePickerDialog
import android.app.Dialog
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
import io.realm.RealmRecyclerViewAdapter
import io.realm.RealmResults
import java.util.*

class BottomSheetActionList (actions : RealmResults<Action>, isDatePicker :Boolean = false ): BottomSheetDialogFragment() {
    private val isDatePicker = isDatePicker
    private val targetActions = actions

    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        val view = View.inflate(context, R.layout.bottom_sheet_action_list, null)
        dialog.setContentView( view )

        val layout = GridLayoutManager( activity, 2 )
        val actionList: RecyclerView = view.findViewById(R.id.action_list)

        actionList.run {
            layoutManager = layout
            adapter = RealmAdapter(targetActions)
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_action_list, container, false)
    }

    private inner class ActionListHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val action : ConstraintLayout = itemView.findViewById(R.id.action_row_in_bottom_sheet)
        val name : TextView = itemView.findViewById(R.id.action_name_in_bottom_sheet)
    }

    private inner class RealmAdapter(private val actionList: OrderedRealmCollection<Action>)
        : RealmRecyclerViewAdapter<Action, ActionListHolder>(actionList, true) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActionListHolder {
            val row = LayoutInflater.from(context)
                .inflate(R.layout.action_row_in_bottom_sheet, parent, false)
            return ActionListHolder(row)
        }

        override fun onBindViewHolder(holder: ActionListHolder, position: Int) {
            holder.name.text = actionList[position]?.name
            holder.action.setOnClickListener { view ->
                //DatePickerで日付セット -> TimePickerで日付セット -> DB Update
                val cal = Calendar.getInstance()
                if( isDatePicker ){
                    DatePickerDialog(
                        view.context,
                        DatePickerDialog.OnDateSetListener { _, year, month, day ->
                            cal.apply {
                                set(Calendar.YEAR, year)
                                set(Calendar.MONTH, month)
                                set(Calendar.DAY_OF_MONTH, day)
                            }
                            Event().insertByTimePicker(holder.name.text.toString(), view.context, cal)
                            dismiss()
                        },
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                    ).show()
                } else {
                    Event().insertByTimePicker(holder.name.text.toString(), view.context, cal)
                    dismiss()
                }
            }
        }

        override fun getItemCount(): Int {
            return actionList.size
        }
    }
}
