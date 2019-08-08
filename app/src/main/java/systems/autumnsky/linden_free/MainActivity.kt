package systems.autumnsky.linden_free

import android.app.TimePickerDialog
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

    private lateinit var textMessage: TextView
    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                textMessage.setText(R.string.title_home)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_log -> {
                textMessage.setText(R.string.title_log)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_medicine -> {
                textMessage.setText(R.string.title_medicine)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        findViewById<TextView>(R.id.date_label).text = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))

        findViewById<Button>(R.id.awake_button).setOnClickListener(SetTime())
        findViewById<Button>(R.id.dose_button).setOnClickListener(SetTime())
        findViewById<Button>(R.id.inbed_button).setOnClickListener(SetTime())
        findViewById<Button>(R.id.sleep_button).setOnClickListener(SetTime())
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
                val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, min ->
                    cal.set(Calendar.HOUR_OF_DAY, hour)
                    cal.set(Calendar.MINUTE, min)

                    //Logテーブル 更新処理

                    // ボタン更新処理
                    val timeString = SimpleDateFormat("HH:mm").format(cal.time)
                    updateButton(button, timeString)

                }

                TimePickerDialog(
                    view.context,
                    timeSetListener,
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    true
                ).show()
            }
        }

        private fun updateButton(button: Button, time: String) {
            val event = labelAttribute(button)["default"]
            val newLabel = "$event  $time"
            button.text = newLabel
            button.setBackgroundColor(getColor(R.color.colorPrimary))
            button.setTextColor(getColor(R.color.primary_material_light))
        }

        private fun labelAttribute(button: Button): Map<String, String> {
            val stringResName = "label_" + resources.getResourceEntryName(button.id)
            val event = getString(resources.getIdentifier(stringResName, "string", packageName))
            return mapOf("default" to event, "current" to button.getText().toString())
        }
    }
}


