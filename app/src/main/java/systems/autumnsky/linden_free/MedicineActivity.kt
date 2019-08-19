package systems.autumnsky.linden_free

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.realm.OrderedRealmCollection
import io.realm.Realm
import io.realm.RealmRecyclerViewAdapter

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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when( item.itemId ){
            R.id.add_medicine -> {
                val dialog = EditMedicineFragment()
                dialog.show(supportFragmentManager,"medicine")
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medicine)

        var medicines = Realm.getDefaultInstance()

        // 登録している薬一覧を表示
        val medicineListView = findViewById<RecyclerView>(R.id.medicine_list)

        val layout = LinearLayoutManager(applicationContext)
        medicineListView.layoutManager = layout

        val allMedicines = medicines.where(Medicine::class.java).findAll()

        medicineListView.adapter = RealmAdapter(medicineListView, allMedicines, autoUpdate = true)
        medicineListView.addItemDecoration(DividerItemDecoration(applicationContext, layout.orientation))

        // 下部ナビゲーション
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.selectedItemId = R.id.navigation_medicine
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
    }

    // 薬テーブルの一覧表示
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

    private inner class RealmAdapter( private val view:View, private val medicines: OrderedRealmCollection<Medicine>, private val autoUpdate: Boolean)
        : RealmRecyclerViewAdapter<Medicine, MedicineListHolder>(medicines, autoUpdate) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicineListHolder {

            val row = LayoutInflater.from(applicationContext).inflate(R.layout.medicine_row, parent, false)
            return MedicineListHolder(row)
        }

        override fun onBindViewHolder(holder: MedicineListHolder, position: Int) {
            val medicine = medicines[position]
            holder.name.text = medicine?.name
            holder.quantity.text = medicine?.regular_quantity?.toString()?:"0"
            holder.step.text = medicine?.adjustment_step?.toString()?:"0"

            // リスナー
            holder.medicine.setOnClickListener{
                // 薬情報の編集はEditMedicineFragmentダイアログで行う
                val bundle = Bundle()
                bundle.putString("MedicineId", medicine?.id)
                bundle.putString("Name", medicine?.name)
                bundle.putDouble("Quantity", medicine?.regular_quantity?:0.0)
                bundle.putDouble("Step", medicine?.adjustment_step?:0.0)

                val dialog = EditMedicineFragment()
                dialog.arguments = bundle

                dialog.show(supportFragmentManager,"medicine")
            }

            holder.medicine.setOnLongClickListener {
                 // 薬の削除は長押しでAlertDialogを表示してから
                AlertDialog.Builder(this@MedicineActivity)
                    .setTitle("Delete " + medicine?.name + "?")
                    .setPositiveButton("Yes", DialogInterface.OnClickListener { _: DialogInterface, _: Int ->
                        // medicine tableからの削除
                        val realm = Realm.getDefaultInstance()

                        val targetMedicine = realm.where(Medicine::class.java).equalTo("id",medicine?.id).findFirst()
                        val targetEvent = realm.where(Event::class.java).equalTo("medicine.id", targetMedicine?.id).findAll()

                        realm.executeTransaction {
                            targetEvent?.deleteAllFromRealm()
                            targetMedicine?.deleteFromRealm()
                        }
                        realm.close()
                    })
                    .setNegativeButton("No", null)
                    .show()
                return@setOnLongClickListener true
            }

        }

        override fun getItemCount(): Int {
            return medicines.size
        }
    }
}
