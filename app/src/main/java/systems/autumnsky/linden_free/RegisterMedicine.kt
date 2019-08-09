package systems.autumnsky.linden_free

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class RegisterMedicine : AppCompatActivity() {

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_log -> {
                val intent = Intent(applicationContext, LogView::class.java)
                startActivity(intent)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_home -> {
                val intent = Intent(applicationContext, MainActivity::class.java)
                startActivity(intent)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_medicine -> {
                val intent = Intent(applicationContext, RegisterMedicine::class.java)
                startActivity(intent)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medicine)

        val medicineListView = findViewById<RecyclerView>(R.id.medicine_list)
        val layout = LinearLayoutManager(applicationContext)
        medicineListView.layoutManager = layout
        medicineListView.adapter = rvAdapter(createMedicineList())
        medicineListView.addItemDecoration(DividerItemDecoration(applicationContext, layout.orientation))

        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.selectedItemId = R.id.navigation_medicine
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
    }

    private fun createMedicineList(): MutableList<MutableMap<String, Any>> {
        val medicineList: MutableList<MutableMap<String, Any>> = mutableListOf()
        var medicine = mutableMapOf<String, Any>("Name" to "ロヒプノール", "RegularQuantity" to 1.5 ,"AdjustmentStep" to 0.5)
        medicineList.add(medicine)

        medicine = mutableMapOf<String, Any>("Name" to "ロラメット", "RegularQuantity" to 2 ,"AdjustmentStep" to 1)
        medicineList.add(medicine)
        medicine = mutableMapOf<String, Any>("Name" to "サインバルタ", "RegularQuantity" to 30 ,"AdjustmentStep" to 0.5)
        medicineList.add(medicine)

        return medicineList
    }

    private inner class MedicineListHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: TextView
        var quantity: TextView
        var step: TextView

        init {
            name = itemView.findViewById(R.id.medicine_name)
            quantity = itemView.findViewById(R.id.regular_quantity)
            step = itemView.findViewById(R.id.adjustment_step)
        }
    }

    private inner class rvAdapter( private val _listData: MutableList<MutableMap<String, Any>>): RecyclerView.Adapter<MedicineListHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicineListHolder {
            val inflater = LayoutInflater.from(applicationContext)
            val row = inflater.inflate(R.layout.medicine_row, parent, false)
            val holder = MedicineListHolder(row)
            return holder
        }

        override fun onBindViewHolder(holder: MedicineListHolder, position: Int) {
            val medicine = _listData[position]

            holder.name.text = medicine["Name"] as String
            holder.quantity.text = medicine["RegularQuantity"].toString()
            holder.step.text = medicine["AdjustmentStep"].toString()
        }

        override fun getItemCount(): Int {
            return _listData.size
        }
    }
}
