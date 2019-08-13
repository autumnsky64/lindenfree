package systems.autumnsky.linden_free

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.edit_medicine.*

class MedicineActivity : AppCompatActivity() {

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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.medicine_action_button, menu)
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medicine)

        // 登録している薬一覧
        val medicineListView = findViewById<RecyclerView>(R.id.medicine_list)
        val layout = LinearLayoutManager(applicationContext)
        medicineListView.layoutManager = layout
        medicineListView.adapter = rvAdapter(createMedicineList())
        medicineListView.addItemDecoration(DividerItemDecoration(applicationContext, layout.orientation))

        // 下部ナビゲーション
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.selectedItemId = R.id.navigation_medicine
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
    }

    // 薬テーブルの一覧表示
    private fun createMedicineList(): MutableList<MutableMap<String, Any>> {
        val medicineList: MutableList<MutableMap<String, Any>> = mutableListOf()
        var medicine = mutableMapOf<String, Any>("ID" to 1 ,"Name" to "ロヒプノール", "RegularQuantity" to 1.5 ,"AdjustmentStep" to 0.5)
        medicineList.add(medicine)

        medicine = mutableMapOf<String, Any>("ID" to 4, "Name" to "ロラメット", "RegularQuantity" to 2 ,"AdjustmentStep" to 1)
        medicineList.add(medicine)
        medicine = mutableMapOf<String, Any>("ID" to 9, "Name" to "サインバルタ", "RegularQuantity" to 30 ,"AdjustmentStep" to 0.5)
        medicineList.add(medicine)

        return medicineList
    }

    private inner class MedicineListHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var medicine: ConstraintLayout
        var name: TextView
        var quantity: TextView
        var step: TextView

        init {
            medicine = itemView.findViewById(R.id.medicine)
            name = itemView.findViewById(R.id.medicine_name)
            quantity = itemView.findViewById(R.id.regular_quantity)
            step = itemView.findViewById(R.id.adjustment_step)
        }
    }

    private inner class rvAdapter( private val _listData: MutableList<MutableMap<String, Any>>): RecyclerView.Adapter<MedicineListHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicineListHolder {

            val row = LayoutInflater.from(applicationContext).inflate(R.layout.medicine_row, parent, false)
            return MedicineListHolder(row)
        }

        override fun onBindViewHolder(holder: MedicineListHolder, position: Int) {
            val medicine = _listData[position]

            holder.name.text = medicine["Name"] as String
            holder.quantity.text = medicine["RegularQuantity"].toString()
            holder.step.text = medicine["AdjustmentStep"].toString()

            // リスナー
            holder.medicine.setOnClickListener{
                // 薬情報の編集はEditMedicineFragmentダイアログで行う
                val bundle = Bundle()
                bundle.putInt("MedicineId", medicine["ID"] as Int)
                bundle.putString("Name", medicine["Name"] as String)

                // 暫定でMap<String, Any>を使っているので文字列→ 倍精度とキャスト
                val regularQuantity = medicine["RegularQuantity"].toString().toDouble()
                val stepQuantity = medicine["AdjustmentStep"].toString().toDouble()
                bundle.putDoubleArray("Quantity", doubleArrayOf(regularQuantity, stepQuantity))

                val dialog = EditMedicineFragment()
                dialog.arguments = bundle

                dialog.show(supportFragmentManager,"medicine")
            }

            holder.medicine.setOnLongClickListener {
                 // 薬の削除は長押しでAlertDialogを表示してから
                AlertDialog.Builder(this@MedicineActivity)
                    .setTitle("Delete " + medicine["Name"] + "?")
                    .setPositiveButton("Yes", DialogInterface.OnClickListener { _: DialogInterface, _: Int ->
                        // medicine tableからの削除処理

                    })
                    .setNegativeButton("No", null)
                    .show()
                return@setOnLongClickListener true
            }

        }

        override fun getItemCount(): Int {
            return _listData.size
        }
    }
}
