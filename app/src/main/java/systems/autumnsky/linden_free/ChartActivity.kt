package systems.autumnsky.linden_free

import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.realm.OrderedRealmCollection
import io.realm.Realm
import io.realm.RealmRecyclerViewAdapter
import io.realm.Sort
import io.realm.kotlin.where
import systems.autumnsky.linden_free.model.Action
import systems.autumnsky.linden_free.model.DailyCycle

class ChartActivity : AppCompatActivity() {

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_chart -> {
                val intent = Intent(applicationContext, ChartActivity::class.java)
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
        menuInflater.inflate(R.menu.chart_action_button, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.dl_chart -> { }
            R.id.show_table -> {
                val intent = Intent(applicationContext, LogActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart)

        findViewById<RecyclerView>(R.id.cycle_chart).apply {
            layoutManager = LinearLayoutManager(applicationContext)
            adapter = RealmAdapter(
                Realm.getDefaultInstance().where<DailyCycle>().findAll().sort("day", Sort.DESCENDING))
        }

        //FAB
        findViewById<View>(R.id.insert_event).setOnClickListener {
            val actions = Realm.getDefaultInstance().where<Action>().notEqualTo("name", getString(R.string.dose)).findAll()
            val actionList = BottomSheetActionList( actions, isDatePicker = true )
            actionList.show(supportFragmentManager, actionList.tag )
        }

        //下部ナビゲーション
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.selectedItemId = R.id.navigation_chart
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
    }


    private inner class Element(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val date :TextView = itemView.findViewById(R.id.time_cell)
        val event :TextView = itemView.findViewById(R.id.event_cell)
    }

    private inner class RealmAdapter(private val days: OrderedRealmCollection<DailyCycle>) :
        RealmRecyclerViewAdapter<DailyCycle, Element>(days, true) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Element {
            val row =
                LayoutInflater.from(applicationContext).inflate(R.layout.log_row, parent, false)
            return Element(row)
        }

        override fun onBindViewHolder(element: Element, position: Int) {
            val current = days[position]
            element.run {
                date.text = current.day?.let { DateFormat.format("yy/MM/dd kk:mm", it) }
                Log.d ( "Day:" , current.day?.toString())
                current.stack?.forEach { cycle ->
                    Log.d ("cycle:", cycle.activity + "," + DateFormat.format("yy/MM/dd kk:mm", cycle.startTime) + "," + cycle.length.toString())
                }
            }
        }

        override fun getItemCount(): Int {
            return days.size
        }
    }
}
