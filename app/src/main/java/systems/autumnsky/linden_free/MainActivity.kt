package systems.autumnsky.linden_free

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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

        findViewById<TextView>(R.id.date_label).text = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))

        findViewById<Button>(R.id.awake_button).setOnClickListener(SetTime())
        findViewById<Button>(R.id.dose_button).setOnClickListener(SetTime())
        findViewById<Button>(R.id.in_bed_button).setOnClickListener(SetTime())
        findViewById<Button>(R.id.sleep_button).setOnClickListener(SetTime())

        findViewById<Button>(R.id.awake_button).setOnLongClickListener(SetTimeByPicker())
        findViewById<Button>(R.id.dose_button).setOnLongClickListener(SetTimeByPicker())
        findViewById<Button>(R.id.in_bed_button).setOnLongClickListener(SetTimeByPicker())
        findViewById<Button>(R.id.sleep_button).setOnLongClickListener(SetTimeByPicker())

        val medicineListView = findViewById<RecyclerView>(R.id.medicines_with_spinner)
        val layout = LinearLayoutManager(applicationContext)
        medicineListView.layoutManager = layout

        var realm = Realm.getDefaultInstance()
        val medicineEvents = realm.where<Event>().isNotNull("medicine").findAll()

        medicineListView.adapter = RealmAdapter(medicineListView, medicineEvents, autoUpdate = true)
        medicineListView.addItemDecoration(DividerItemDecoration(applicationContext, layout.orientation))
    }

    private inner class MedicineListHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var medicine: ConstraintLayout
        var name: TextView

        init{
                medicine = itemView.findViewById(R.id.medicine_with_spinner)
                name = itemView.findViewById(R.id.medicine_name_with_spinner)
        }
    }
    private inner class RealmAdapter(private val view:View, private val medicineEvents: OrderedRealmCollection<Event>, private val autoUpdate: Boolean)
        : RealmRecyclerViewAdapter<Event, MedicineListHolder>(medicineEvents, autoUpdate){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicineListHolder {
            val row = LayoutInflater.from(applicationContext).inflate(R.layout.medicine_contain_spinner, parent, false)
            return MedicineListHolder(row)
        }

        override fun onBindViewHolder(holder: MedicineListHolder, position: Int) {
            val medicine = medicineEvents[position]
            holder.name.text = medicine?.name
        }

        override fun getItemCount(): Int {
            return medicineEvents.size
        }
    }

    private inner class SetTime : View.OnClickListener {
        override fun onClick(view: View?) {
            val cal = Calendar.getInstance()
            val button = view as Button
            val labelMap: Map<String, String> = labelAttribute(button)

            if (labelMap["default"] == labelMap["current"]) {
                //Logテーブルに insert

                updateButton(button, SimpleDateFormat("HH:mm").format(cal.time))
            } else {
                //TimePickerからセットするのは時刻入力済みの時のみ
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
        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, min ->

            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, min)

            //Logテーブル 更新処理

            // ボタン更新処理
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
}


