package systems.autumnsky.linden_free

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.realm.OrderedRealmCollection
import io.realm.Realm
import io.realm.RealmRecyclerViewAdapter
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.medicine_row.view.*
import java.text.DecimalFormat

class MedicineActivity : AppCompatActivity() {

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_log -> {
                val intent = Intent(applicationContext, LogActivity::class.java)
                startActivity(intent)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_home -> {
                //初回起動時のヒント
                findViewById<ConstraintLayout>(R.id.tutorial_medicine)?.let{
                    it.visibility = View.INVISIBLE
                }

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

                EditMedicineFragment().show(supportFragmentManager,"medicine")

                //初回起動時のツールチップを消す
                findViewById<TextView>(R.id.description_add_medicine)?.let{ it.visibility = View.INVISIBLE }
                findViewById<ImageView>(R.id.arrow_add_medicine)?.let{ it.visibility = View.INVISIBLE }

            }

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medicine)

        val realm = Realm.getDefaultInstance()

        // 登録している薬一覧を表示
        val medicineList = findViewById<RecyclerView>(R.id.medicine_list)
        val layout = LinearLayoutManager(applicationContext)
        realm.where<Medicine>().findAll()?.let {
            medicineList.run {
                layoutManager = layout
                adapter = RealmAdapter(it)
                addItemDecoration(DividerItemDecoration(applicationContext, layout.orientation))
            }
        }

        val helper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback( 0, ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT){
            override fun onMove( recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                AlertDialog.Builder(this@MedicineActivity).run{
                    setCancelable(false)
                    setTitle(getString(R.string.title_delete_medicine, viewHolder.itemView.medicine_name.text.toString()))
                    setPositiveButton(getText(R.string.dialog_delete)) { _, _ ->
                        // medicine tableからの削除
                        val id = viewHolder.itemView.medicine_id.text.toString()

                        Realm.getDefaultInstance().apply {
                            val targetMedicine = where<Medicine>().equalTo("id", id).findFirst()
                            val targetEvent = where<Action>().equalTo("medicine.id", targetMedicine?.id).findAll()

                            executeTransaction {
                                targetEvent?.deleteAllFromRealm()
                                targetMedicine?.deleteFromRealm()
                            }
                            close()
                        }
                    }
                    setNegativeButton(getText(R.string.dialog_cancel)) { _, _ ->
                        //スワイプで行表示が消えたままになるので何も変わってないが再描画
                        medicineList.adapter?.notifyDataSetChanged()
                    }
                    show()
                }
            }
        })
        helper.attachToRecyclerView(medicineList)
        medicineList.addItemDecoration(helper)

        // 下部ナビゲーション
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.selectedItemId = R.id.navigation_medicine
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        //初回起動時のツールチップ表示
        showTutorial()
    }

    private fun showTutorial(){
        if( (application as LindenFreeApp).isFirstLaunch
            && Realm.getDefaultInstance().where<Medicine>().findAll().count() == 0 ) {
                val decorView = this@MedicineActivity.window.decorView as ViewGroup
                decorView.addView(
                    LayoutInflater.from(this@MedicineActivity).inflate(R.layout.tutorial_medicine_activity, null)
                )
        }
    }
    // 薬テーブルの一覧表示
    private inner class MedicineListHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val medicine: ConstraintLayout = itemView.findViewById(R.id.medicine)
        val medicine_id: TextView = itemView.findViewById(R.id.medicine_id)
        val name: TextView = itemView.findViewById(R.id.medicine_name)
        val quantity: TextView = itemView.findViewById(R.id.regular_quantity)
        val step: TextView = itemView.findViewById(R.id.adjustment_step)

        val regularUnitLabel: TextView = itemView.findViewById(R.id.regular_unit_label)
        val adjustmentLabel: TextView = itemView.findViewById(R.id.adjustment_label)
        val adjustmentUnitLabel: TextView = itemView.findViewById(R.id.adjustment_unit)
        val useAsNeededLabel: TextView = itemView.findViewById(R.id.is_use_as_needed)
    }

    private inner class RealmAdapter(private val medicines: OrderedRealmCollection<Medicine>)
        : RealmRecyclerViewAdapter<Medicine, MedicineListHolder>(medicines, true) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicineListHolder {

            val row = LayoutInflater.from(applicationContext).inflate(R.layout.medicine_row, parent, false)
            return MedicineListHolder(row)
        }

        override fun onBindViewHolder(holder: MedicineListHolder, position: Int) {
            val medicine = medicines[position]
            holder.medicine_id.text = medicine?.id
            holder.name.text = medicine?.name

            // 量の表示 0の時はラベルとかも非表示
            val regQty = medicine?.regular_quantity
            if ( regQty != null){
                holder.run{
                    quantity.text = DecimalFormat("#.##").format(regQty)
                    regularUnitLabel.visibility = View.VISIBLE
                }

            } else {
                holder.run{
                    quantity.text = ""
                    regularUnitLabel.visibility = View.INVISIBLE
                }
            }

            val stepQty = medicine?.adjustment_step
            if ( stepQty != null ){
                holder.run{
                    step.text = DecimalFormat("#.##").format(stepQty)
                    adjustmentLabel.visibility = View.VISIBLE
                    adjustmentUnitLabel.visibility = View.VISIBLE
                }
            } else {
                holder.run{
                    step.text = ""
                    adjustmentLabel.visibility = View.INVISIBLE
                    adjustmentUnitLabel.visibility = View.INVISIBLE
                }
            }

            val isUseAsNeeded = medicine?.is_use_as_needed
            if( isUseAsNeeded != null && isUseAsNeeded ){ holder.useAsNeededLabel.visibility = View.VISIBLE }

            // タップで編集
            holder.medicine.setOnClickListener{
                // 薬情報の編集はEditMedicineFragmentダイアログで行う
                EditMedicineFragment().run {
                    arguments = Bundle().apply {
                        putString("MedicineId", medicine?.id)
                        putString("Name", medicine?.name)
                        putDouble("Quantity", medicine?.regular_quantity?:0.0)
                        putDouble("Step", medicine?.adjustment_step?:0.0)
                        putBoolean("IsUseAsNeeded", medicine?.is_use_as_needed?:false)
                    }
                    show(supportFragmentManager,"medicine")
                }
            }
        }

        override fun getItemCount(): Int {
            return medicines.size
        }
    }

}
