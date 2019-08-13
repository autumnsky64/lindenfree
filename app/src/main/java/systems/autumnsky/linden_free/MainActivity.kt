package systems.autumnsky.linden_free

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
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
        findViewById<Button>(R.id.inbed_button).setOnClickListener(SetTime())
        findViewById<Button>(R.id.sleep_button).setOnClickListener(SetTime())

        findViewById<Button>(R.id.awake_button).setOnLongClickListener(SetTimeByPicker())
        findViewById<Button>(R.id.dose_button).setOnLongClickListener(SetTimeByPicker())
        findViewById<Button>(R.id.inbed_button).setOnLongClickListener(SetTimeByPicker())
        findViewById<Button>(R.id.sleep_button).setOnLongClickListener(SetTimeByPicker())
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
        val stringResName = "label_" + resources.getResourceEntryName(button.id)
        val event = getString(resources.getIdentifier(stringResName, "string", packageName))
        return mapOf("default" to event, "current" to button.getText().toString())
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


