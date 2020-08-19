package systems.autumnsky.linden_free

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
import systems.autumnsky.linden_free.model.Action
import systems.autumnsky.linden_free.model.Event
import java.util.*

class BottomSheetActionList(
    actions: RealmResults<Action>,
    isDatePicker: Boolean = true,
    isTimePicker: Boolean = false,
    day: Date? = null
) : BottomSheetDialogFragment() {

    private val isDatePicker = isDatePicker
    private val isTimePicker = isTimePicker
    private val targetActions = actions
    private val date = day

    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)

        val view = View.inflate(context, R.layout.bottom_sheet_action_list, null)
        dialog.setContentView(view)

        val layout = GridLayoutManager(activity, 2)
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

    private inner class ActionListHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val action: ConstraintLayout = itemView.findViewById(R.id.action_row_in_bottom_sheet)
        val name: TextView = itemView.findViewById(R.id.action_name_in_bottom_sheet)
    }

    private inner class RealmAdapter(private val actionList: OrderedRealmCollection<Action>) :
        RealmRecyclerViewAdapter<Action, ActionListHolder>(actionList, true) {

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
}
