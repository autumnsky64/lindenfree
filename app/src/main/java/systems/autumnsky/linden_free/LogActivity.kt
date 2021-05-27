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
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import io.realm.OrderedRealmCollection
import io.realm.Realm
import io.realm.RealmRecyclerViewAdapter
import io.realm.Sort
import io.realm.kotlin.where
import systems.autumnsky.linden_free.model.Action
import systems.autumnsky.linden_free.model.Event
import java.io.BufferedOutputStream
import java.text.DecimalFormat
import java.util.*

class LogActivity : AppCompatActivity() {

    private val onNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_chart -> {
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
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    ActivityCompat.requestPermissions(this, permissions, 1000)
                } else {
                    createCsv()
                }
            }
            R.id.show_chart -> {
                val intent = Intent(applicationContext, ChartActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1000 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            createCsv()
        }
    }

    private val WRITE_REQUEST_CODE: Int = 563

    private fun createCsv() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
            putExtra(Intent.EXTRA_TITLE, "linden_free_log.txt")
        }
        startActivityForResult(intent, WRITE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode != WRITE_REQUEST_CODE || resultCode != Activity.RESULT_OK) {
            super.onActivityResult(requestCode, resultCode, data)
            return
        }

        data?.data?.let { fileUri ->
            contentResolver.openOutputStream(fileUri, "wa")?.let {

                BufferedOutputStream(it, Context.MODE_APPEND).run {
                    val header = "Time\tEvent\tQuantity\n"
                    write(header.toByteArray())

                    Realm.getDefaultInstance().where<Event>()
                        .sort("id", Sort.ASCENDING)
                        .findAll()
                        .forEach { record ->
                            val timeString = DateFormat.format("yyyy/MM/dd kk:mm", record.time)
                            val quantityString =
                                if (record.quantity != null) DecimalFormat("#.##").format(record.quantity!!) else ""

                            write("${timeString}\t${record.name}\t${quantityString}\n".toByteArray())
                        }
                    flush()
                    close()
                }

                Snackbar.make(
                    findViewById(R.id.snack_bar_container),
                    getText(R.string.snackbar_save_file_message),
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logview)

        val layout = LinearLayoutManager(applicationContext)
        val key = arrayOf("time", "id")
        val sort = arrayOf(Sort.DESCENDING, Sort.DESCENDING)
        val eventLog = Realm.getDefaultInstance().where<Event>().findAll().sort(key, sort)

        val logTable = findViewById<RecyclerView>(R.id.log_table_body).apply {
            layoutManager = layout
            adapter = RealmAdapter(eventLog).apply {
                //追記時に最上部にスクロールする
                registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                        findViewById<RecyclerView>(R.id.log_table_body).scrollToPosition(
                            positionStart
                        )
                    }
                })
            }
            addItemDecoration(DividerItemDecoration(applicationContext, layout.orientation))
        }

        //swipe操作
        val helper = ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val timeString = viewHolder.itemView.findViewById<TextView>(R.id.time_cell).text.toString()
                val eventName = viewHolder.itemView.findViewById<TextView>(R.id.event_cell).text.toString()
                val quantityString = viewHolder.itemView.findViewById<TextView>(R.id.qty_cell).text.toString()
                val id = viewHolder.itemView.findViewById<TextView>(R.id.id_cell).text.toString()

                AlertDialog.Builder(this@LogActivity)
                    .setTitle(getText(R.string.title_delete_record))
                    .setMessage("$timeString \n$eventName $quantityString")
                    .setPositiveButton(getText(R.string.dialog_delete)) { _, _ ->

                        id.let { Event().delete(it.toLong()) }

                    }
                    .setNegativeButton(getText(R.string.dialog_cancel)) { _, _ ->
                        //スワイプで行表示が消えたままになるので何も変わってないが再描画
                        logTable.adapter?.notifyDataSetChanged()
                    }
                    .setOnDismissListener {
                        logTable.adapter?.notifyDataSetChanged()
                    }
                    .show()

            }
        })
        helper.attachToRecyclerView(logTable)
        logTable.addItemDecoration(helper)

        //FAB
        findViewById<View>(R.id.insert_event).setOnClickListener {
            val actions = Realm.getDefaultInstance().where<Action>()
                .notEqualTo("name", getString(R.string.dose)).findAll()

            val cal = Calendar.getInstance()
            DatePickerDialog(
                this,
                DatePickerDialog.OnDateSetListener { _, year, month, day ->
                    cal.apply {
                        set(Calendar.YEAR, year)
                        set(Calendar.MONTH, month)
                        set(Calendar.DAY_OF_MONTH, day)
                    }
                    TimePickerDialog(
                        this,
                        TimePickerDialog.OnTimeSetListener { _, hour, min ->
                            cal.apply {
                                set(Calendar.HOUR_OF_DAY, hour)
                                set(Calendar.MINUTE, min)
                            }
                            val actionList = BottomSheetActionList(isDatePicker = false, isTimePicker = false, day = cal.time)
                            actionList.show(supportFragmentManager, actionList.tag)
                        },
                        cal.get(Calendar.HOUR_OF_DAY),
                        cal.get(Calendar.MINUTE),
                        true
                    ).show()
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).apply {
                datePicker.maxDate = cal.timeInMillis
                show()
            }
        }

        //下部ナビゲーション
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.selectedItemId = R.id.navigation_chart
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

    }

    private inner class LogHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val id: TextView = itemView.findViewById(R.id.id_cell)
        val time: TextView = itemView.findViewById(R.id.time_cell)
        val event: TextView = itemView.findViewById(R.id.event_cell)
        val quantity: TextView = itemView.findViewById(R.id.qty_cell)
    }

    private inner class RealmAdapter(private val log: OrderedRealmCollection<Event>) :
        RealmRecyclerViewAdapter<Event, LogHolder>(log, true) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogHolder {
            val row =
                LayoutInflater.from(applicationContext).inflate(R.layout.log_row, parent, false)
            return LogHolder(row)
        }

        override fun onBindViewHolder(holder: LogHolder, position: Int) {
            val record = log[position]
            holder.run {
                id.text = record.id?.toString()
                time.text = record.time?.let { DateFormat.format("yy/MM/dd kk:mm", it) }
                event.text = record.name
                quantity.text = record.quantity?.let { DecimalFormat("#.##").format(it) + "mg" }
            }

            holder.time.setOnClickListener {
                record.name?.let{
                    Event().insertByDatePicker(
                        context = this@LogActivity,
                        action = it,
                        id = record.id,
                        cal = Calendar.getInstance().apply { time = record.time!! }
                    )
                }
            }

            val quantity = record.quantity
            if (quantity != null) {
                holder.quantity.setOnClickListener {
                    EditRecordedQuantityFragment().run {
                        arguments = Bundle().apply {
                            putString("Id", record.id!!.toString())
                            putString("MedicineName", record.name)
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

