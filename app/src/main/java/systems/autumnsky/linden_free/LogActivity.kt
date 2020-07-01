package systems.autumnsky.linden_free

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
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
import androidx.recyclerview.widget.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import io.realm.OrderedRealmCollection
import io.realm.Realm
import io.realm.RealmRecyclerViewAdapter
import io.realm.Sort
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.log_row.view.*
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
                }else{
                    createCsv()
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

        if (requestCode != WRITE_REQUEST_CODE || resultCode != Activity.RESULT_OK ) {
            super.onActivityResult(requestCode, resultCode, data)
            return
        }

        data?.data?.let{ fileUri ->
            contentResolver.openOutputStream( fileUri, "wa" )?.let{

                BufferedOutputStream(it, Context.MODE_APPEND).run {
                    val header = "Time\tEvent\tQuantity\n"
                    write(header.toByteArray())

                    Realm.getDefaultInstance().where<Event>()
                        .sort("id", Sort.ASCENDING)
                        .findAll()
                        .forEach { record ->
                            val timeString = DateFormat.format("yyyy/MM/dd kk:mm", record.time )
                            val quantityString = if( record.quantity != null ) DecimalFormat("#.##").format(record.quantity!!) else ""

                            write("${timeString}\t${record.event_name}\t${quantityString}\n".toByteArray())
                        }
                    flush()
                    close()
                }

                Snackbar.make(findViewById(R.id.snack_bar_container), getText(R.string.snackbar_save_file_message), Snackbar.LENGTH_LONG).show()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logview)

        val layout = LinearLayoutManager(applicationContext)
        val key = arrayOf("time", "id")
        val sort = arrayOf( Sort.DESCENDING, Sort.DESCENDING)
        val eventLog = Realm.getDefaultInstance().where<Event>().findAll().sort(key, sort)

        val logTable = findViewById<RecyclerView>(R.id.log_table_body).apply {
            layoutManager = layout
            adapter = RealmAdapter(eventLog)
            addItemDecoration(DividerItemDecoration(applicationContext, layout.orientation))
        }

        //swipe操作
        val helper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback( 0, ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT){
            override fun onMove( recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val timeString = viewHolder.itemView.time_cell.text.toString()
                val eventName = viewHolder.itemView.event_cell.text.toString()
                val quantityString = viewHolder.itemView.qty_cell.text.toString()

                AlertDialog.Builder(this@LogActivity)
                    .setTitle(getText(R.string.title_delete_record))
                    .setMessage("$timeString \n$eventName $quantityString")
                    .setPositiveButton(getText(R.string.dialog_delete)){ _, _ ->
                        // medicine tableからの削除
                        val id = viewHolder.itemView.log_id.text?.toString()?.toLong()
                        Realm.getDefaultInstance().apply{
                            executeTransaction {
                                where<Event>().equalTo("id", id).findAll().deleteAllFromRealm()
                                }
                        } .also { it.close() }
                    }
                    .setNegativeButton(getText(R.string.dialog_cancel)){ _ , _ ->
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
            val actionList = ActionList()
            actionList.show(supportFragmentManager, actionList.tag )
        }

        //下部ナビゲーション
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.selectedItemId = R.id.navigation_log
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        showTutorial()
    }

    private fun showTutorial() {
        if( (application as LindenFreeApp).isFirstLaunch){
            val decorView = this@LogActivity.window.decorView as ViewGroup
            decorView.addView(
                LayoutInflater.from(this@LogActivity).inflate(R.layout.tutorial_log_activity, null)
            )
        }
    }

    private inner class LogHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val id :TextView = itemView.findViewById(R.id.log_id)
        val time :TextView = itemView.findViewById(R.id.time_cell)
        val event :TextView = itemView.findViewById(R.id.event_cell)
        val quantity :TextView = itemView.findViewById(R.id.qty_cell)
    }

    private inner class RealmAdapter(private val log: OrderedRealmCollection<Event>) :
        RealmRecyclerViewAdapter<Event, LogHolder>(log, true) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogHolder {
            val row = LayoutInflater.from(applicationContext).inflate(R.layout.log_row, parent, false)
            return LogHolder(row)
        }

        override fun onBindViewHolder(holder: LogHolder, position: Int) {
            val logRecord = log[position]
            holder.run {
                id.text = logRecord.id?.toString()
                time.text = logRecord.time?.let { DateFormat.format("yy/MM/dd kk:mm", it) }
                event.text = logRecord.event_name
                quantity.text = logRecord.quantity?.let { DecimalFormat("#.##").format(it) + "mg" }
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
                                    cal.set( Calendar.SECOND, 0)
                                    cal.set( Calendar.MILLISECOND, 0)
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

class ActionList : BottomSheetDialogFragment() {
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        val view = View.inflate(context, R.layout.bottom_sheet_action_list, null)
        dialog.setContentView( view )

        val realm = Realm.getDefaultInstance()
        val layout = GridLayoutManager( activity, 2 )
        val actionList: RecyclerView = view.findViewById(R.id.action_list)

        realm.where<Action>().isNull("medicine").notEqualTo("name", getString(R.string.dose)).findAll()?.let {
            actionList.run {
                layoutManager = layout
                adapter = RealmAdapter(it)
            }
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_action_list, container, false)
    }

    private inner class ActionListHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val action :ConstraintLayout = itemView.findViewById(R.id.action_row_in_bottom_sheet)
        val name :TextView = itemView.findViewById(R.id.action_name_in_bottom_sheet)
    }

    private inner class RealmAdapter(private val actionList: OrderedRealmCollection<Action>)
        : RealmRecyclerViewAdapter<Action, ActionListHolder>(actionList, true) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActionListHolder {
            val row = LayoutInflater.from(context)
                .inflate(R.layout.action_row_in_bottom_sheet, parent, false)
            return ActionListHolder(row)
        }

        override fun onBindViewHolder(holder: ActionListHolder, position: Int) {
            holder.name.text = actionList[position]?.name
            holder.action.setOnClickListener { view ->
                Event().insertByTimePicker( holder.name.text.toString(), view.context)
                dismiss()
            }
        }

        override fun getItemCount(): Int {
            return actionList.size
        }
    }
}
