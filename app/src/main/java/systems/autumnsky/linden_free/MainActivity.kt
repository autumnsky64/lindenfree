package systems.autumnsky.linden_free

import android.content.Intent
import android.icu.text.SimpleDateFormat
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

        // 薬 ボタン リサイクルビューの薬リストを一括でDBに書き込む
        findViewById<Button>(R.id.dose_button).setOnClickListener { view ->
            val button = view as Button
            val labelMap: Map<String, String> = labelAttribute(button)
            val medicineList = findViewById<RecyclerView>(R.id.medicines_with_spinner)

            // ボタンのラベルがデフォルトならインサート 時刻が入ってればアップデート
            if (labelMap["default"] == labelMap["current"]) {
                Event().insertMedicineLog( medicineList )
                updateButton( button, Calendar.getInstance() )
            } else {
                Event().updateMedicineLog( medicineList, button, buildCalendarByLabel( button ) )
            }
        }

        findViewById<Button>(R.id.dose_button).setOnLongClickListener { view ->
            val button = view as Button
            val labelMap: Map<String, String> = labelAttribute(button)
            val medicineList = findViewById<RecyclerView>(R.id.medicines_with_spinner)

            if(labelMap["default"] == labelMap["current"]) {
                Event().insertMedicineLogByTimePicker( medicineList, button )
            } else {
                Event().updateMedicineLog( medicineList, button, buildCalendarByLabel( button ))
            }
            return@setOnLongClickListener false
        }

        val realm = Realm.getDefaultInstance()

        // ボタンなどの日時表示
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
        }
        val medicine = realm.where<Medicine>().findFirst()
        val doseTime = realm.where<Event>()
            .greaterThanOrEqualTo("time", today.time)
            .equalTo("event_name",medicine?.name)
            .findFirst()?.time

        if( doseTime != null){
            val cal = Calendar.getInstance()
            cal.time = doseTime
            updateButton(findViewById(R.id.dose_button), cal )
        }

        // 薬のリスト
        val medicineListView = findViewById<RecyclerView>(R.id.medicines_with_spinner)
        val layout = LinearLayoutManager(applicationContext)
        medicineListView.layoutManager = layout

        val medicines = realm.where<Action>().isNotNull("medicine").findAll()

        medicineListView.adapter = RealmAdapter(medicines)
        medicineListView.addItemDecoration(DividerItemDecoration(applicationContext, layout.orientation))

        // Sleepボタン
        findViewById<Button>(R.id.sleep_button).setOnClickListener{
            Event().insert( getString(R.string.sleep) )
            showSleepingDialog()
        }

        // イベントログの最後のレコードが、スリープの時は睡眠中ダイアログを表示
        val lastEvent = realm.where<Event>().sort("time", Sort.DESCENDING).findFirst()
        if( lastEvent?.event_name == getString(R.string.sleep)){
            lastEvent?.time?.let {
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
    private inner class RealmAdapter(private val medicineList: OrderedRealmCollection<Action>)
        : RealmRecyclerViewAdapter<Action, MedicineListHolder>(medicineList, false){

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicineListHolder {
            val row = LayoutInflater.from(applicationContext).inflate(R.layout.medicine_contain_spinner, parent, false)
            return MedicineListHolder(row)
        }

        override fun onBindViewHolder(holder: MedicineListHolder, position: Int) {
            holder.name.text = medicineList[position]?.name

            setupSpinner(holder.quantitySpinner, holder.unitLabel, medicineList[position]?.medicine?.regular_quantity, medicineList[position].medicine?.adjustment_step )
            //TODO ログから容量をセットする
        }

        override fun getItemCount(): Int {
            return medicineList.size
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

    //buttonのIDから、初期のラベルを取得
    private fun labelAttribute(button: Button): Map<String, String> {
        val stringResName = resources.getResourceEntryName(button.id).replace("_button","")
        val event = getString(resources.getIdentifier(stringResName, "string", packageName))
        return mapOf("default" to event, "current" to button.text.toString())
    }

    private fun updateButton(button: Button, calendar: Calendar) {
        val time = DateFormat.format("HH:mm" ,calendar.time).toString()
        val event = labelAttribute(button)["default"]
        val newLabel = "$event  $time"
        button.text = newLabel
        button.setBackgroundColor(getColor(R.color.colorPrimary))
        button.setTextColor(getColor(R.color.materialLight))
    }

    private fun buildCalendarByLabel( button :Button): Calendar? {
        val labelMap = labelAttribute( button )
        val date = findViewById<TextView>(R.id.date_label).text.toString() + labelMap["current"]?.replace( labelMap["default"].toString(), "")
        val cal = Calendar.getInstance()
        cal.time = SimpleDateFormat("yyyy/MM/dd  HH:mm").parse( date )
        return cal
    }
}