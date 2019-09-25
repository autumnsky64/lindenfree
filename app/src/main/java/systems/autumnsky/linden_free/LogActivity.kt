package systems.autumnsky.linden_free

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.format.DateFormat
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import io.realm.OrderedRealmCollection
import io.realm.Realm
import io.realm.RealmRecyclerViewAdapter
import io.realm.Sort
import io.realm.kotlin.where
import java.io.BufferedOutputStream
import java.text.DecimalFormat
import java.util.*

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
        when (item.itemId) {
            R.id.dl_log -> {
                if (ActivityCompat.checkSelfPermission( this, Manifest.permission.WRITE_EXTERNAL_STORAGE )
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    ActivityCompat.requestPermissions(this, permissions, 1000)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult( requestCode: Int, permissions: Array<out String>, grantResults: IntArray ) {
        if (requestCode == 1000 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            createCsv()
        }
    }

    private val WRITE_REQUEST_CODE: Int = 563
    private fun createCsv(){
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
            putExtra(Intent.EXTRA_TITLE, "linden_free_log.txt")
        }
        startActivityForResult(intent, WRITE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == WRITE_REQUEST_CODE && resultCode == Activity.RESULT_OK ){
            data?.data?.let{ fileUri ->
                val stream = contentResolver.openOutputStream( fileUri, "wa" )
                stream?.let{
                    val header = "Time\tEvent\tQuantity\n"
                    val buffer = BufferedOutputStream(it, Context.MODE_APPEND)
                    buffer.write(header.toByteArray())

                    Realm.getDefaultInstance().where<EventLog>().sort("id", Sort.ASCENDING).findAll().forEach { record ->
                        val timeString = DateFormat.format("yyyy/MM/dd kk:mm", record.time )
                        val quantityString = if( record.quantity != null ) DecimalFormat("#.##").format(record.quantity!!) else ""

                        buffer.write("${timeString}\t${record.event_name}\t${quantityString}\n".toByteArray())
                    }

                    buffer.flush()
                    buffer.close()

                    Snackbar.make(findViewById(R.id.snack_bar_container), "Log text file has been saved. ", Snackbar.LENGTH_LONG).show()
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logview)

        val layout = LinearLayoutManager(applicationContext)
        val eventLog = Realm.getDefaultInstance().where<EventLog>().findAll()

        findViewById<RecyclerView>(R.id.log_table_body).run {
            layoutManager = layout
            adapter = RealmAdapter(eventLog)
            addItemDecoration(DividerItemDecoration(applicationContext, layout.orientation))
        }


        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.selectedItemId = R.id.navigation_log
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
    }

    private inner class LogHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var logRow: ConstraintLayout
        var time: TextView
        var event: TextView
        var quantity: TextView

        init {
            logRow = itemView.findViewById(R.id.log_row)
            time = itemView.findViewById(R.id.time_cell)
            event = itemView.findViewById(R.id.event_cell)
            quantity = itemView.findViewById(R.id.qty_cell)
        }
    }

    private inner class RealmAdapter(private val log: OrderedRealmCollection<EventLog>) :
        RealmRecyclerViewAdapter<EventLog, LogHolder>(log, true) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogHolder {
            val row = LayoutInflater.from(applicationContext).inflate(R.layout.log_row, parent, false)
            return LogHolder(row)
        }

        override fun onBindViewHolder(holder: LogHolder, position: Int) {
            val logRecord = log[position]
            holder.run {
                time.text = logRecord.time?.let { DateFormat.format("yy/MM/dd kk:mm", it) }
                event.text = logRecord.event_name
                quantity.text = logRecord.quantity?.let { DecimalFormat("#.##").format(it) + "mg" }
            }

            holder.logRow.setOnLongClickListener {
                AlertDialog.Builder(this@LogActivity).apply {
                    setTitle("Delete this record?")
                    setMessage("${holder.time.text} ${holder.event.text} ${holder.quantity.text}")
                    setPositiveButton("Yes") { _, _ ->

                        // log tableからの削除
                        val realm = Realm.getDefaultInstance()
                        realm.executeTransaction {
                            val targetLog = realm.where<EventLog>().equalTo("id", logRecord?.id).findAll()
                            targetLog.deleteAllFromRealm()
                        }
                        realm.close()
                    }
                    setNegativeButton("No", null)
                    show()
                }
                return@setOnLongClickListener true
            }

            holder.time.setOnClickListener {
                val cal = Calendar.getInstance().apply { time = logRecord.time!! }

                //DatePickerで日付セット -> TimePickerで日付セット -> DB Update
                DatePickerDialog(
                    this@LogActivity,
                    DatePickerDialog.OnDateSetListener { _, year, month, day ->
                        cal.apply {
                            set(Calendar.YEAR, year)
                            set(Calendar.MONTH, month)
                            set(Calendar.DAY_OF_MONTH, day)
                        }
                        TimePickerDialog(
                            this@LogActivity,
                            TimePickerDialog.OnTimeSetListener { _, hour, min ->
                                cal.apply {
                                    set(Calendar.HOUR_OF_DAY, hour)
                                    set(Calendar.MINUTE, min)
                                }
                                val realm = Realm.getDefaultInstance()
                                realm.executeTransaction {
                                    logRecord.time = cal.time
                                }
                                realm.close()

                            },
                            cal.get(Calendar.HOUR_OF_DAY),
                            cal.get(Calendar.MINUTE),
                            true
                        ).show()
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).show()
            }

            val quantity = logRecord.quantity
            if (quantity != null) {
                holder.quantity.setOnClickListener {
                    EditQuantityLogFragment().run {
                        arguments = Bundle().apply {
                            putString("Id", logRecord.id!!.toString())
                            putString("MedicineName", logRecord.event_name)
                            putString("Quantity", quantity.toString())
                        }
                        show(supportFragmentManager, "EditQuantity")
                    }
                }
            }
        }

        override fun getItemCount(): Int {
            return log.size
        }
    }
}
