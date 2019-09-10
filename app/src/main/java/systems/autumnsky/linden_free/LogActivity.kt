package systems.autumnsky.linden_free

import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import android.view.*
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
import java.text.DecimalFormat

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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.log_action_button, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when( item.itemId ){
            R.id.dl_log -> {
                // TODO: ログファイルの保存処理
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logview)

        val layout = LinearLayoutManager(applicationContext)
        val eventLog = Realm.getDefaultInstance().where<EventLog>().findAll()

        findViewById<RecyclerView>(R.id.log_table).run{
            layoutManager = layout
            adapter = RealmAdapter(eventLog)
            addItemDecoration(DividerItemDecoration(applicationContext, layout.orientation))
        }


        val navView: BottomNavigationView = findViewById(R.id.nav_view)
            navView.selectedItemId = R.id.navigation_log
            navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
    }

    private inner class LogHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var logRow: ConstraintLayout
        var time: TextView
        var event: TextView
        var quantity: TextView

        init{
            logRow = itemView.findViewById(R.id.log_row)
            time = itemView.findViewById(R.id.time_cell)
            event = itemView.findViewById(R.id.event_cell)
            quantity = itemView.findViewById(R.id.qty_cell)
        }
    }
    private inner class RealmAdapter(private val log: OrderedRealmCollection<EventLog>)
        : RealmRecyclerViewAdapter<EventLog, LogHolder>(log, true){

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogHolder {
            val row = LayoutInflater.from(applicationContext).inflate(R.layout.log_row, parent, false)
            return LogHolder(row)
        }

        override fun onBindViewHolder(holder: LogHolder, position: Int) {
            val logRow = log[position]
            holder.run {
                time.text =  logRow.time?.let{ DateFormat.format("yy/MM/dd kk:mm", it)}
                event.text = logRow.event_name
                quantity.text = logRow.quantity?.let{ DecimalFormat("#.##").format(it) + "mg" }
            }

        }

        override fun getItemCount(): Int {
            return log.size
        }
    }
}
