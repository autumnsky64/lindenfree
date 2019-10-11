package systems.autumnsky.linden_free

import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.Intent
import android.icu.text.DateFormat.getDateInstance
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.AppLaunchChecker
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.realm.OrderedRealmCollection
import io.realm.Realm
import io.realm.RealmRecyclerViewAdapter
import io.realm.Sort
import io.realm.kotlin.createObject
import io.realm.kotlin.where
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

    private fun showTutorial() {
        if( (application as LindenFreeApp).isFirstLaunch ){
            if ( Realm.getDefaultInstance().where<Medicine>().findAll().count() == 0 ) {
                //初回起動時はMedicineActivityへ
                startActivity(Intent(applicationContext, MedicineActivity::class.java))
            } else {
                //薬が登録されていれば、調整スピナーやログビューのバルーン表示
                val decorView = this@MainActivity.window.decorView as ViewGroup
                decorView.addView(
                    LayoutInflater.from(this@MainActivity).inflate(R.layout.tutorial_main_activity, null)
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppLaunchChecker.onActivityCreate(this)

        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.selectedItemId = R.id.navigation_home
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        showTutorial()

        // 日付ラベル
        findViewById<TextView>(R.id.date_label).text = DateFormat.format("yyyy/MM/dd", Calendar.getInstance())

        findViewById<Button>(R.id.dose_button).setOnClickListener(SetTime())
        findViewById<Button>(R.id.dose_button).setOnLongClickListener(SetTimeByPicker())

        // 薬のリスト
        val medicineListView = findViewById<RecyclerView>(R.id.medicines_with_spinner)
        val layout = LinearLayoutManager(applicationContext)
        medicineListView.layoutManager = layout

        val realm = Realm.getDefaultInstance()
        val medicineEvents = realm.where<Event>().isNotNull("medicine").findAll()

        medicineListView.adapter = RealmAdapter(medicineEvents)
        medicineListView.addItemDecoration(DividerItemDecoration(applicationContext, layout.orientation))

        // Doseボタンの日時表示
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
        }
        val medicine = realm.where<Medicine>().findFirst()
        val doseTime = realm.where<EventLog>()
                .greaterThanOrEqualTo("time", today.time)
                .equalTo("event_name",medicine?.name)
                .findFirst()?.time

        if( doseTime != null){
            updateButton(findViewById(R.id.dose_button), DateFormat.format( "HH:mm", doseTime).toString())
        }

        // Sleepボタン
        findViewById<Button>(R.id.sleep_button).setOnClickListener{
            insertLog( getString(R.string.sleep), Calendar.getInstance())
            showSleepingDialog()
        }

        // イベントログの最後のレコードが、スリープの時は睡眠中ダイアログを表示
        val lastEvent = realm.where<EventLog>().sort("time", Sort.DESCENDING).findFirst()
        if( lastEvent?.event_name == getString(R.string.sleep)){
            lastEvent.time?.let {
                showSleepingDialog(DateFormat.format("hh:mm", it) as String)
            }
        }
    }

    private fun showSleepingDialog( inSleepTime: String = DateFormat.format("hh:mm", Calendar.getInstance()) as String ) {
        val sleepingDialog = InSleepFragment()
        val bundle = Bundle()

        bundle.putString("InSleepTime", inSleepTime)

        sleepingDialog.isCancelable = false
        sleepingDialog.arguments = bundle
        sleepingDialog.show(supportFragmentManager, "InSleep")
    }

    //薬の一覧
    private inner class MedicineListHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var medicine: ConstraintLayout
        var name: TextView
        var quantitySpinner: Spinner
        var unitLabel: TextView

        init{
            medicine = itemView.findViewById(R.id.medicine_with_spinner)
            name = itemView.findViewById(R.id.medicine_name_with_spinner)
            quantitySpinner = itemView.findViewById(R.id.adjust_spinner)
            unitLabel = itemView.findViewById(R.id.adjust_spinner_unit_label)
        }
    }
    private inner class RealmAdapter(private val medicineEvents: OrderedRealmCollection<Event>)
        : RealmRecyclerViewAdapter<Event, MedicineListHolder>(medicineEvents, false){

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicineListHolder {
            val row = LayoutInflater.from(applicationContext).inflate(R.layout.medicine_contain_spinner, parent, false)
            return MedicineListHolder(row)
        }

        override fun onBindViewHolder(holder: MedicineListHolder, position: Int) {
            val medicineEvent = medicineEvents[position]
            holder.name.text = medicineEvent?.name

            setupSpinner(holder.quantitySpinner, holder.unitLabel, medicineEvent?.medicine?.regular_quantity, medicineEvent?.medicine?.adjustment_step )
        }

        override fun getItemCount(): Int {
            return medicineEvents.size
        }

        private fun setupSpinner(spinner: Spinner, unitLabel: TextView, default: Double?, adjust: Double?){
            val qtyList = mutableListOf<Double>()
            if ( default != null && adjust != null){
                val min = default - (adjust * 2)

                // 増減は上下2ステップ、レンジにstepが使えるのはint/longのみ（言語仕様では0.1刻みも可能らしいが）
                for ( i in 0..4){
                    qtyList.add( min + (adjust * i ))
                }
            } else if (default != null && adjust == null ){
                qtyList.add(default)
            } else {
                spinner.visibility = View.INVISIBLE
                unitLabel.visibility = View.INVISIBLE
            }

            val adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_item, qtyList)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter

            if ( spinner.adapter.count > 2){
                spinner.setSelection(2)
            }
        }
    }

    private inner class SetTime : View.OnClickListener {
        override fun onClick(view: View?) {
            val cal = Calendar.getInstance()
            val button = view as Button
            val labelMap: Map<String, String> = labelAttribute(button)

            if (labelMap["default"] == labelMap["current"]) {
                updateButton(button, DateFormat.format("HH:mm" ,cal.time).toString())
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
        button.setTextColor(getColor(R.color.materialLight))
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

            val labelMap: Map<String, String> =
                labelAttribute(button)
            if (labelMap["default"] == labelMap["current"]){
                insertLog( labelMap["default"], cal )
            }else{
                getDateInstance().parse( findViewById<TextView>(R.id.date_label).text.toString())?.let{ updateLog( labelMap["default"], it, cal) }
            }

            updateButton( button, DateFormat.format("HH:mm", cal.time).toString() )
        }

        TimePickerDialog(
            button.context,
            timeSetListener,
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            true
        ).show()
    }

    fun insertLog(event: String?, cal: Calendar) {

        val realm = Realm.getDefaultInstance()
        var newId: Long = (realm.where<EventLog>().max("id")?.toLong()?:0) + 1

        when (event) {
            getText(R.string.dose) -> {
                val medicineList = findViewById<RecyclerView>(R.id.medicines_with_spinner)

                for (i in 0..medicineList.childCount) {

                    val row: RecyclerView.ViewHolder? = medicineList.findViewHolderForLayoutPosition(i)

                    // 薬リストに登録がない場合をチェック
                    if (row === null) { continue }

                    realm.executeTransaction {
                        val eventLog = realm.createObject<EventLog>(newId).apply{
                            time = cal.time
                            event_name = row.itemView.findViewById<TextView>(R.id.medicine_name_with_spinner).text.toString()
                            quantity = row.itemView.findViewById<Spinner>(R.id.adjust_spinner).selectedItem?.toString()?.toDoubleOrNull()
                        }
                        realm.copyToRealm(eventLog)
                    }
                    newId += 1
                }
            }
            else -> {
                realm.executeTransaction {
                    val eventLog = realm.createObject<EventLog>(newId).apply{
                        time = cal.time
                        event_name = event
                    }
                    realm.copyToRealm(eventLog)
                }
            }
        }
        realm.close()
    }

    private fun  updateLog(event: String?, oldDate: Date, newCal: Calendar) {
        val realm = Realm.getDefaultInstance()
        when (event) {
            getText(R.string.dose) -> {
                val medicineList = findViewById<RecyclerView>(R.id.medicines_with_spinner)

                for (i in 0..medicineList.childCount) {

                    val row: RecyclerView.ViewHolder? = medicineList.findViewHolderForLayoutPosition(i)
                    if (row === null) { continue }

                    val name =  row.itemView.findViewById<TextView>(R.id.medicine_name_with_spinner).text.toString()
                    val targetRecord = realm.where<EventLog>()
                        .greaterThanOrEqualTo("time", oldDate)
                        .equalTo("event_name", name)
                        .findFirst()

                    if ( targetRecord != null) {
                        //update
                        realm.executeTransaction {
                            targetRecord.apply {
                                time = newCal.time
                                quantity = row.itemView.findViewById<Spinner>(R.id.adjust_spinner).selectedItem?.toString()?.toDoubleOrNull()
                            }
                        }
                    } else {
                        //insert
                        realm.executeTransaction {
                            val newId = (realm.where<EventLog>().max("id")?.toLong()?:0) + 1
                            realm.createObject<EventLog>(newId).apply {
                                event_name = name
                                time = newCal.time
                                quantity = row.itemView.findViewById<Spinner>(R.id.adjust_spinner).selectedItem?.toString()?.toDoubleOrNull()
                            }
                        }
                    }
                }

            } else -> {
                realm.executeTransaction{
                    realm.where<EventLog>()
                        .greaterThanOrEqualTo("time", oldDate)
                        .equalTo("event_name", event)
                        .findFirst()?.apply {

                            time = newCal.time

                    }
                }
            }
        }
        realm.close()
    }
}