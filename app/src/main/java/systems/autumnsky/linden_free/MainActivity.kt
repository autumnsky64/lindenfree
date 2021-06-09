package systems.autumnsky.linden_free

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.AppLaunchChecker
import androidx.recyclerview.widget.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.realm.OrderedRealmCollection
import io.realm.Realm
import io.realm.RealmRecyclerViewAdapter
import io.realm.Sort
import io.realm.kotlin.where
import systems.autumnsky.linden_free.model.*
import java.text.DecimalFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private val onNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_chart -> {
                    val intent = Intent(applicationContext, ChartActivity::class.java)
                    startActivity(intent)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_home -> {
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    startActivity(intent)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_medicine -> {
                    val intent = Intent(applicationContext, MedicineActivity::class.java)
                    startActivity(intent)
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppLaunchChecker.onActivityCreate(this)

        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.selectedItemId = R.id.navigation_home
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        //初回起動で薬が登録されてなければMedicine Activityへ
        if ((application as LindenFreeApp).isFirstLaunch
            && Realm.getDefaultInstance().where<Medicine>().findAll().count() == 0
        ) {
            startActivity(Intent(applicationContext, MedicineActivity::class.java))
        }

        // 日付ラベル
        val day = intent.getStringExtra("Day")
        if (day != null) {
            findViewById<TextView>(R.id.date_label).text = day
        } else {
            findViewById<TextView>(R.id.date_label).text =
                DateFormat.format("yyyy/MM/dd", Calendar.getInstance())
        }

        val currentDay =
            SimpleDateFormat("yyyy/MM/dd").parse(findViewById<TextView>(R.id.date_label).text.toString())
        val calToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        var isTodayDaily: Boolean
        isTodayDaily = calToday.compareTo(Calendar.getInstance().apply { time = currentDay }) == 0

        // 薬 ボタン: リサイクルビューの薬リストを一括でDBに書き込む
        findViewById<Button>(R.id.dose_button).setOnClickListener { view ->
            val button = view as Button
            val labelMap: Map<String, String> = labelAttribute(button)
            val medicineList = findViewById<RecyclerView>(R.id.medicines_with_spinner)

            // ボタンのラベルがデフォルトならインサート 時刻が入ってればアップデート
            if (labelMap["default"] == labelMap["current"]) {
                if (isTodayDaily) {
                    Event().insertMedicineLog(medicineList)
                    updateButton(button, Calendar.getInstance())
                } else {
                    val argCal = Calendar.getInstance().apply { time = currentDay }
                    Event()
                        .insertMedicineLogByTimePicker(medicineList, button, argCal)
                    updateButton(button, argCal)
                }
            } else {
                Event()
                    .updateMedicineLog(medicineList, button, buildCalendarByLabel(button))
            }
        }

        findViewById<Button>(R.id.dose_button).setOnLongClickListener { view ->
            val button = view as Button
            val labelMap: Map<String, String> = labelAttribute(button)
            val medicineList = findViewById<RecyclerView>(R.id.medicines_with_spinner)
            val argCal = Calendar.getInstance().apply { time = currentDay }

            if (labelMap["default"] == labelMap["current"]) {
                Event()
                    .insertMedicineLogByTimePicker(medicineList, button, argCal)
            } else {
                Event()
                    .updateMedicineLog(medicineList, button, buildCalendarByLabel(button))
            }
            return@setOnLongClickListener false
        }

        val realm = Realm.getDefaultInstance()

        // ボタンなどの日時表示
        val currentDayLastSec = Calendar.getInstance().apply {
            time = currentDay
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }

        val medicine = realm.where<Medicine>().findFirst()
        val doseTime = realm.where<Event>()
            .between("time", currentDay, currentDayLastSec.time)
            .equalTo("name", medicine?.name)
            .findFirst()?.time

        if (doseTime != null) {
            val cal = Calendar.getInstance()
            cal.time = doseTime
            updateButton(findViewById(R.id.dose_button), cal)
        }

        // 薬のリスト
        val medicineLayout = LinearLayoutManager(applicationContext)
        val medicineListView = findViewById<RecyclerView>(R.id.medicines_with_spinner)
        medicineListView.layoutManager = medicineLayout

        realm.where<Action>().isNotNull("medicine").equalTo("medicine.is_use_as_needed", false)
            .findAll()?.let {
            medicineListView.apply {
                adapter = MedicineAdapter(it)
                addItemDecoration(
                    DividerItemDecoration(
                        applicationContext,
                        medicineLayout.orientation
                    )
                )
            }
        }

        //その日のイベントリスト
        val dailyEventView = findViewById<RecyclerView>(R.id.todays_sleeping_log)
        realm.where<DailyActivity>().equalTo("day", currentDay).findFirst()?.activityStack?.let {
            dailyEventView.apply {
                layoutManager =
                    GridLayoutManager(applicationContext, 1, GridLayoutManager.VERTICAL, false)
                val activities = it as OrderedRealmCollection<Activity>
                adapter = EventAdapter(activities.sort("endTime", Sort.ASCENDING), currentDay)
            }
        }

        //LogActivityからスワイプ処理、コピペ
        val helper = ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                AlertDialog.Builder(this@MainActivity)
                    .setTitle(getText(R.string.title_delete_record))
                    .setPositiveButton(getText(R.string.dialog_delete)) { _, _ ->

                        DailyActivity().deleteActivity(currentDay, viewHolder.adapterPosition )
                    }
                    .setNegativeButton(getText(R.string.dialog_cancel)) { _, _ ->
                        //スワイプで行表示が消えたままになるので何も変わってないが再描画
                        dailyEventView.adapter?.notifyDataSetChanged()
                    }
                    .setOnDismissListener {
                        dailyEventView.adapter?.notifyDataSetChanged()
                    }
                    .show()
            }
        })
        helper.attachToRecyclerView(dailyEventView)

        //日付移動
        val prevDay = Calendar.getInstance().apply {
            time = currentDay
            add(Calendar.DAY_OF_MONTH, -1)
        }

        val nextDay = Calendar.getInstance().apply {
            time = currentDay
            add(Calendar.DAY_OF_MONTH, +1)
        }

        findViewById<ImageButton>(R.id.move_previous_day).setOnClickListener {
            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.putExtra("Day", DateFormat.format("yyyy/MM/dd", prevDay))
            startActivity(intent)
        }

        if (nextDay < Calendar.getInstance()) {
            findViewById<ImageButton>(R.id.move_next_day).apply {
                visibility = View.VISIBLE
                setOnClickListener {
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    intent.putExtra("Day", DateFormat.format("yyyy/MM/dd", nextDay))
                    startActivity(intent)
                }
            }
        }

        //FAB
        findViewById<View>(R.id.daily_insert_event).setOnClickListener {
            if (isTodayDaily) {
                val actionList = BottomSheetActionList(isDatePicker = false)
                actionList.show(supportFragmentManager, actionList.tag)
            } else {
                val actionList = BottomSheetActionList(
                    isDatePicker = false,
                    isTimePicker = true,
                    day = currentDay
                )
                actionList.show(supportFragmentManager, actionList.tag)
            }
        }

        // Sleepボタン
        val sleepButton = findViewById<Button>(R.id.sleep_button)
        if (isTodayDaily) {
            sleepButton.setOnClickListener {
                Event().insert(getString(R.string.sleep))
                showSleepingDialog()
            }
        } else {
            sleepButton.visibility = View.GONE
        }

        // イベントログの最後のレコードが、スリープの時は睡眠中ダイアログを表示
        val key = arrayOf("time", "id")
        val sortDescend = arrayOf(Sort.DESCENDING, Sort.DESCENDING)
        val lastEvent = realm.where<Event>().sort(key, sortDescend).findFirst()
        if (lastEvent?.name == getString(R.string.sleep)) {
            lastEvent.time?.let {
                showSleepingDialog(DateFormat.format("hh:mm", it) as String)
            }
        }
    }

    private fun showSleepingDialog(
        inSleepTime: String = DateFormat.format(
            "hh:mm",
            Calendar.getInstance()
        ) as String
    ) {
        InSleepFragment().apply {
            isCancelable = false
            arguments = Bundle().apply {
                putString("InSleepTime", inSleepTime)
            }
            show(supportFragmentManager, "InSleep")
        }
    }

    //薬の一覧
    private inner class MedicineListHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val id: TextView = itemView.findViewById(R.id.id_cell_medicine)
        val name: TextView = itemView.findViewById(R.id.medicine_name_with_spinner)
        val quantitySpinner: Spinner = itemView.findViewById(R.id.adjust_spinner)
        val unitLabel: TextView = itemView.findViewById(R.id.adjust_spinner_unit_label)
    }

    private inner class MedicineAdapter(private val medicineList: OrderedRealmCollection<Action>) :
        RealmRecyclerViewAdapter<Action, MedicineListHolder>(medicineList, false) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicineListHolder {
            val row = LayoutInflater.from(applicationContext)
                .inflate(R.layout.medicine_row_contain_spinner, parent, false)
            return MedicineListHolder(row)
        }

        override fun onBindViewHolder(holder: MedicineListHolder, position: Int) {
            val medicineName = medicineList[position]?.name
            holder.name.text = medicineName

            val todaysFirstSec = Calendar.getInstance().apply {
                time =
                    SimpleDateFormat("yyyy/MM/dd").parse(findViewById<TextView>(R.id.date_label).text.toString())
            }
            val recordedMedicine = Realm.getDefaultInstance().where<Event>().greaterThan("time", todaysFirstSec.time)
                    .equalTo("name", medicineName).sort("time", Sort.ASCENDING)
                    .findFirst()

            holder.id.text = recordedMedicine?.id.toString()

            val recordedQuantity = recordedMedicine?.quantity
            setupSpinner(
                holder.quantitySpinner,
                holder.unitLabel,
                medicineList[position]?.medicine,
                recordedQuantity
            )

        }

        override fun getItemCount(): Int {
            return medicineList.size
        }

        private fun setupSpinner(
            spinner: Spinner,
            unitLabel: TextView,
            medicine: Medicine?,
            quantity: Double?
        ) {
            val qtyList = mutableListOf<Double>()
            val default = medicine?.regular_quantity
            val adjust = medicine?.adjustment_step

            if (default != null && adjust != null) {
                val min = default - (adjust * 2)

                // 増減は上下2ステップ、レンジにstepが使えるのはint/longのみ（言語仕様では0.1刻みも可能らしいが）
                for (i in 0..4) {
                    qtyList.add(min + (adjust * i))
                }
            } else if (default != null && adjust == null) {
                qtyList.add(default)
            } else {
                spinner.visibility = View.INVISIBLE
                unitLabel.visibility = View.INVISIBLE
            }

            val adapter =
                ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_item, qtyList)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter

            if (spinner.adapter.count > 2) {
                val found = qtyList.indexOf(quantity)
                if (found == -1) {
                    spinner.setSelection(2)
                } else {
                    spinner.setSelection(found)
                }
            }
        }
    }

    //イベントの一覧
    private inner class EventAdapter(private val todaysActivities: OrderedRealmCollection<Activity>, private val day: Date) :
        RealmRecyclerViewAdapter<Activity, RecyclerView.ViewHolder>(todaysActivities, true) {

        val midnight: Date? = Calendar.getInstance().apply {
            time = day
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val lastSec: Date? = Calendar.getInstance().apply {
            time = day
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.time

        override fun getItemViewType(position: Int): Int {
            val activity = todaysActivities[position]
            return if ( activity.medicines!!.isNotEmpty() ){
                3
            } else if ( activity.length == null || activity.startTime == midnight || activity.endTime == lastSec ){
                1
            } else {
                2
            }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = LayoutInflater.from(parent.context)

            return when (viewType) {
                1 -> {
                    SingleRowCard(inflater.inflate(R.layout.card_activity_single_row, parent, false))
                }
                2 -> {
                    WithLengthCard(inflater.inflate(R.layout.card_activity_has_length, parent, false))
                }
                3-> {
                    TakenMedicineCard(inflater.inflate(R.layout.card_activity_medicines, parent, false))
                }
                else -> {
                    error("No activity type")
                }
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val activity = todaysActivities[position]
            when ( getItemViewType(position) ) {
                1 -> {
                    val name = if( position == 0 && activity.startEvent == null ){ getString(R.string.awake) } else { activity.name }
                    val card = holder as SingleRowCard
                    val time = if( activity.startTime == midnight ) activity.endTime else activity.startTime
                    card.run{
                        timeCell.text = DateFormat.format("HH:mm", time) as String
                        nameCell.text = name
                        timeCell.setOnClickListener { updateEventTime(activity.endEvent!!) }
                    }
                }
                2 -> {
                    val card = holder as WithLengthCard
                    card.run{
                        startTimeCell.run {
                            text = DateFormat.format("HH:mm", activity.startTime) as String
                            setOnClickListener { updateEventTime( activity.startEvent!! ) }
                        }
                        endTimeCell.run {
                            text = activity.endTime?.let { DateFormat.format("HH:mm", it) as String }
                            setOnClickListener { updateEventTime( activity.endEvent!! ) }
                        }
                        nameCell.text = activity.name
                    }
                }
                3 -> {
                    val card = holder as TakenMedicineCard
                    card.run {
                        timeCell.run {
                            text = DateFormat.format("HH:mm", activity.startTime) as String
                            setOnClickListener {
                                val cal = Calendar.getInstance()
                                cal.time = activity.startTime

                                TimePickerDialog(
                                    this@MainActivity,
                                    TimePickerDialog.OnTimeSetListener { _, hour, min ->
                                        cal.apply {
                                            set(Calendar.HOUR_OF_DAY, hour)
                                            set(Calendar.MINUTE, min)
                                        }
                                        Realm.getDefaultInstance().executeTransaction {
                                            activity.medicines!!.forEach{
                                                it.time = cal.time
                                            }
                                        }
                                        DailyActivity().refreshDailyStack(cal)
                                    },
                                    cal.get(Calendar.HOUR_OF_DAY),
                                    cal.get(Calendar.MINUTE),
                                    true
                                ).show()
                            }

                            medicines.apply {
                                layoutManager = GridLayoutManager( applicationContext, 1 )
                                adapter = TakenMedicinesAdapter( activity.medicines as OrderedRealmCollection<Event> )
                            }
                        }
                    }
                }
            }

    }

        private fun updateEventTime(event: Event) {
            val cal = Calendar.getInstance()
            cal.time = event.time

            TimePickerDialog(
                this@MainActivity,
                TimePickerDialog.OnTimeSetListener { _, hour, min ->
                    cal.apply {
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, min)
                    }
                    Realm.getDefaultInstance().executeTransaction {
                        event.time = cal.time
                    }
                    DailyActivity().refreshDailyStack(cal)
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
            ).show()
        }

        override fun getItemCount(): Int {
            return todaysActivities.size
        }
    }

    private inner class SingleRowCard(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val idCell: TextView = itemView.findViewById(R.id.card_single_id)
        val nameCell: TextView = itemView.findViewById(R.id.card_single_name)
        val timeCell: TextView = itemView.findViewById(R.id.card_single_time)
    }

    private inner class WithLengthCard(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameCell: TextView = itemView.findViewById(R.id.card_with_length_name)
        val startTimeCell: TextView = itemView.findViewById(R.id.card_with_length_start_time)
        val endTimeCell: TextView = itemView.findViewById(R.id.card_with_length_end_time)
    }

    private inner class TakenMedicineCard(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val timeCell: TextView = itemView.findViewById(R.id.card_medicine_time)
        val medicines: RecyclerView = itemView.findViewById(R.id.card_medicines)
    }

    // Medicineカード内のリサイクラービュー
    private inner class TakenMedicinesAdapter( private val takenMedicines: OrderedRealmCollection<Event>):
        RealmRecyclerViewAdapter<Event, MedicineInnerCard>(takenMedicines, true){

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicineInnerCard {
            return MedicineInnerCard(LayoutInflater.from(parent.context).inflate(R.layout.card_medicine_row, parent,false))
        }

        override fun onBindViewHolder(holder: MedicineInnerCard, position: Int) {
            val medicine = takenMedicines[position]
            holder.apply {
                name.text = medicine.name as String
                medicine.quantity?.let {
                    unitLabel.visibility = View.VISIBLE
                    quantity.visibility = View.VISIBLE
                    quantity.text = DecimalFormat("#.##").format(medicine.quantity)
                }
                quantity.setOnClickListener {
                    EditRecordedQuantityFragment().run {
                        arguments = Bundle().apply {
                            putString("Id", medicine.id!!.toString())
                            putString("MedicineName", medicine.name as String)
                            putString("Quantity", medicine.quantity.toString())
                        }
                        show(supportFragmentManager, "EditQuantity")
                    }
                }
            }
        }
    }

    private inner class MedicineInnerCard(itemView: View) : RecyclerView.ViewHolder(itemView){
        val name: TextView = itemView.findViewById(R.id.medicine_name_inner_card)
        val quantity: TextView = itemView.findViewById(R.id.medicine_quantity_inner_card)
        val unitLabel: TextView = itemView.findViewById(R.id.medicine_unit_label_inner_card)
    }

    //buttonのIDから、初期のラベルを取得
    private fun labelAttribute(button: Button): Map<String, String> {
        val stringResName = resources.getResourceEntryName(button.id).replace("_button", "")
        val event = getString(resources.getIdentifier(stringResName, "string", packageName))
        return mapOf("default" to event, "current" to button.text.toString())
    }

    private fun updateButton(button: Button, calendar: Calendar) {
        val time = DateFormat.format("HH:mm", calendar.time).toString()
        val event = labelAttribute(button)["default"]
        val newLabel = "$event  $time"
        button.text = newLabel
        button.setBackgroundColor(getColor(R.color.colorPrimary))
        button.setTextColor(getColor(R.color.materialLight))
    }

    private fun buildCalendarByLabel(button: Button): Calendar? {
        val labelMap = labelAttribute(button)
        val date =
            findViewById<TextView>(R.id.date_label).text.toString() + labelMap["current"]?.replace(
                labelMap["default"].toString(),
                ""
            )
        val cal = Calendar.getInstance()
        cal.time = SimpleDateFormat("yyyy/MM/dd  HH:mm").parse(date)
        return cal
    }
}