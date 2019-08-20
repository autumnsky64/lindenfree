package systems.autumnsky.linden_free

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

class LogActivity : AppCompatActivity() {

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
        setContentView(R.layout.activity_logview)

        val logTable = findViewById<RecyclerView>(R.id.log_table)
        val layout = LinearLayoutManager(applicationContext)
        logTable.layoutManager = layout

        var realm = Realm.getDefaultInstance()
        val log = realm.where<Log>().findAll()

        logTable.adapter = RealmAdapter(logTable, log, autoUpdate = false)
        logTable.addItemDecoration(DividerItemDecoration(applicationContext, layout.orientation))

        val navView: BottomNavigationView = findViewById(R.id.nav_view)
            navView.selectedItemId = R.id.navigation_log
            navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
    }

    private inner class LogHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var logRow: ConstraintLayout
        var time: TextView
        var event: TextView

        init{
            logRow = itemView.findViewById(R.id.log_row)
            time = itemView.findViewById(R.id.time_cell)
            event = itemView.findViewById(R.id.event_cell)
        }
    }
    private inner class RealmAdapter(private val view: View, private val log: OrderedRealmCollection<Log>, private val autoUpdate: Boolean)
        : RealmRecyclerViewAdapter<Log, LogHolder>(log, autoUpdate){

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogHolder {
            val row = LayoutInflater.from(applicationContext).inflate(R.layout.log_row, parent, false)
            return LogHolder(row)
        }

        override fun onBindViewHolder(holder: LogHolder, position: Int) {
            val logRow = log[position]

            val date = logRow.time
            if( date != null){
                holder.time.text =  SimpleDateFormat("yy/MM/dd HH:mm").format(date)
            }

            holder.event.text = logRow.event_name

        }

        override fun getItemCount(): Int {
            return log.size
        }
    }
}
