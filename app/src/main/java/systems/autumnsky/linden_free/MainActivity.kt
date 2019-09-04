package systems.autumnsky.linden_free

import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.realm.OrderedRealmCollection
import io.realm.Realm
import io.realm.RealmRecyclerViewAdapter
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class MainActivity : AppCompatActivity() {

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_log -> {
                val intent = Intent(applicationContext, LogActivity::class.java)
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

        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.selectedItemId = R.id.navigation_home
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        // 日付ラベル
        findViewById<TextView>(R.id.date_label).text = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))

        findViewById<Button>(R.id.awake_button).setOnClickListener(SetTime())
        findViewById<Button>(R.id.dose_button).setOnClickListener(SetTime())
        findViewById<Button>(R.id.in_bed_button).setOnClickListener(SetTime())
        findViewById<Button>(R.id.sleep_button).setOnClickListener(SetTime())

        findViewById<Button>(R.id.awake_button).setOnLongClickListener(SetTimeByPicker())
        findViewById<Button>(R.id.dose_button).setOnLongClickListener(SetTimeByPicker())
        findViewById<Button>(R.id.in_bed_button).setOnLongClickListener(SetTimeByPicker())
        findViewById<Button>(R.id.sleep_button).setOnLongClickListener(SetTimeByPicker())

        // 薬のリスト
        val medicineListView = findViewById<RecyclerView>(R.id.medicines_with_spinner)
        val layout = LinearLayoutManager(applicationContext)
        medicineListView.layoutManager = layout

        var realm = Realm.getDefaultInstance()
        val medicineEvents = realm.where<Event>().isNotNull("medicine").findAll()

        medicineListView.adapter = RealmAdapter(medicineListView, medicineEvents, autoUpdate = false)
        medicineListView.addItemDecoration(DividerItemDecoration(applicationContext, layout.orientation))

        // 既にDBに時刻が登録済みなら、ボタンのラベルなど書換
        // TODO: ボタン個別の実装をしない
        // TODO: null判定をスマートに
        // TODO: 日付判定が数日前にさかのぼるとできなくなると思う
        val currentDate = SimpleDateFormat("yyyy/MM/dd").parse(findViewById<TextView>(R.id.date_label).text.toString())
        if (currentDate != null){
            val eventLog = realm.where<EventLog>().greaterThanOrEqualTo("time", currentDate)

            val awake = eventLog.equalTo("event_name", getString(R.string.awake)).findFirst()
            val awakeTime = awake?.time
            if ( awakeTime != null ){
                updateButton(findViewById<Button>(R.id.awake_button), SimpleDateFormat("HH:mm").format(awakeTime))
            }

            val medicine = realm.where<Medicine>().isNotNull("id").findFirst()
            val dose = realm.where<EventLog>()
                .greaterThanOrEqualTo("time", currentDate)
                .equalTo("event_name",medicine?.name)
                .findFirst()
            val doseTime = dose?.time

            if ( doseTime != null ){
                updateButton(findViewById<Button>(R.id.dose_button), SimpleDateFormat("HH:mm").format(doseTime))
            }

            val inBed = eventLog.equalTo("event_name", getString(R.string.in_bed)).findFirst()
            val inBedTime = inBed?.time
            if ( inBedTime != null ){
                updateButton(findViewById<Button>(R.id.in_bed_button), SimpleDateFormat("HH:mm").format(inBedTime))
            }

            val sleep = eventLog.equalTo("event_name",getString(R.string.sleep)).findFirst()
            val sleepTime = sleep?.time
            if ( sleepTime != null ){
                updateButton(findViewById<Button>(R.id.sleep_button), SimpleDateFormat("HH:mm").format(sleepTime))
            }
        }

    }

    private inner class MedicineListHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var medicine: ConstraintLayout
        var name: TextView
        var quantitySpinner: Spinner

        init{
                medicine = itemView.findViewById(R.id.medicine_with_spinner)
                name = itemView.findViewById(R.id.medicine_name_with_spinner)
                quantitySpinner = itemView.findViewById(R.id.adjust_spinner)
        }
    }
    private inner class RealmAdapter(private val view:View, private val medicineEvents: OrderedRealmCollection<Event>, private val autoUpdate: Boolean)
        : RealmRecyclerViewAdapter<Event, MedicineListHolder>(medicineEvents, autoUpdate){

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicineListHolder {
            val row = LayoutInflater.from(applicationContext).inflate(R.layout.medicine_contain_spinner, parent, false)
            return MedicineListHolder(row)
        }

        override fun onBindViewHolder(holder: MedicineListHolder, position: Int) {
            val medicineEvent = medicineEvents[position]
            holder.name.text = medicineEvent?.name
            setupSpinner(holder.quantitySpinner, medicineEvent?.medicine?.regular_quantity, medicineEvent?.medicine?.adjustment_step )
        }

        override fun getItemCount(): Int {
            return medicineEvents.size
        }

        private fun setupSpinner(spinner: Spinner, default: Double?, adjust: Double?){
            val qtyList = mutableListOf<Double>()
            if ( default != null && adjust != null){
                val min = default - (adjust * 2)

                // 増減は上下2ステップ、レンジにstepが使えるのはint/longのみ（言語仕様では0.1刻みも可能らしいが）
                for ( i in 0..4){
                    qtyList.add( min + (adjust * i ))
                }
            } else if (default != null){
                qtyList.add(default)
                }

            val adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_item, qtyList)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
            spinner.setSelection(2)
        }
    }

    private inner class SetTime : View.OnClickListener {
        override fun onClick(view: View?) {
            val cal = Calendar.getInstance()
            val button = view as Button
            val labelMap: Map<String, String> = labelAttribute(button)

            if (labelMap["default"] == labelMap["current"]) {
                updateButton(button, SimpleDateFormat("HH:mm").format(cal.time))
                insertLog(labelMap["default"]?:"some event", cal)
            } else {
                timePicker(button)
            }
        }
    }

    private inner class SetTimeByPicker : View.OnLongClickListener {
        override fun onLongClick(view: View?): Boolean {
            val button = view as Button
            timePicker(button)
            return true
        }
    }

    private fun updateButton(button: Button, time: String) {
        val event = labelAttribute(button)["default"]
        val newLabel = "$event  $time"
        button.text = newLabel
        button.setBackgroundColor(getColor(R.color.colorPrimary))
        button.setTextColor(getColor(R.color.primary_material_light))
    }

    //buttonのIDから、初期のラベルを取得
    private fun labelAttribute(button: Button): Map<String, String> {
        val stringResName = resources.getResourceEntryName(button.id).replace("_button","")
        val event = getString(resources.getIdentifier(stringResName, "string", packageName))
        return mapOf("default" to event, "current" to button.text.toString())
    }

    //タイムピッカーで指定した時刻でボタンを更新
    private fun timePicker( button: Button ) {
        val cal = Calendar.getInstance()
        val timeSetListener = OnTimeSetListener { _, hour, min ->

            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, min)

            val labelMap: Map<String, String> = labelAttribute(button)
            if (labelMap["default"] == labelMap["current"]){
                insertLog( labelMap["default"], cal )
            }else{
                val timeString = labelMap["current"]?.replace("${labelMap["default"]}", " ")
                val dateString = findViewById<TextView>(R.id.date_label).text
                val oldDate: Date? = SimpleDateFormat("yyyy/MM/dd hh:mm").parse("$dateString $timeString")
                if (oldDate != null){ updateLog( labelMap["default"], oldDate, cal  ) }
            }

            val timeString = SimpleDateFormat("HH:mm").format(cal.time)
            updateButton(button, timeString)
        }

        TimePickerDialog(
            button.context,
            timeSetListener,
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun insertLog(event: String?, cal: Calendar) {

        val realm = Realm.getDefaultInstance()
        var id = realm.where<EventLog>().count() + 1
        var time: Date = cal.time
        when (event) {
            "Dose" -> {
                val medicineEvents = realm.where<Event>().isNotNull("medicine").findAll()
                val qtyList = findViewById<RecyclerView>(R.id.medicines_with_spinner)

                // リサイクルビューのポジションカウンタ
                var j = 0
                medicineEvents.forEach {

                    realm.beginTransaction()
                    val eventLog = realm.createObject<EventLog>(id)
                    eventLog.time = time
                    eventLog.event_name = it.name

                    val viewHolder: RecyclerView.ViewHolder? = qtyList.findViewHolderForLayoutPosition(j)
                    if( viewHolder != null){
                        val qty = viewHolder.itemView.findViewById<Spinner>(R.id.adjust_spinner).selectedItem.toString()
                        eventLog.quantity = qty.toDoubleOrNull()
                        Log.d("qty",qty)
                    }
                    j++

                    realm.copyToRealm(eventLog)
                    realm.commitTransaction()
                    id += 1
                }
            } else -> {
                realm.beginTransaction()
                val log = realm.createObject<EventLog>(id)
                log.time = time
                log.event_name = event

                realm.copyToRealm(log)
                realm.commitTransaction()
            }
        }

    }

    private fun  updateLog(event: String?, oldDate: Date, newCal: Calendar) {
        val realm = Realm.getDefaultInstance()
        var time: Date = newCal.time
        when (event) {
            "Dose" -> {
                val medicineEvents = realm.where<EventLog>().greaterThanOrEqualTo("time", oldDate)
            } else -> {
                realm.beginTransaction()
                val log = realm.where<EventLog>().greaterThanOrEqualTo("time", oldDate).equalTo("event_name", event).findFirst()
                log?.time = time
                realm.commitTransaction()
            }
        }

    }

}


