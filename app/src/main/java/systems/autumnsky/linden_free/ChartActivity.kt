package systems.autumnsky.linden_free

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.MediaStore
import android.text.format.DateFormat
import android.view.*
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.drawToBitmap
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import io.realm.*
import io.realm.kotlin.where
import systems.autumnsky.linden_free.model.Action
import systems.autumnsky.linden_free.model.Activity
import systems.autumnsky.linden_free.model.DailyActivity
import java.util.*

class ChartActivity : AppCompatActivity() {

    private val onNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
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
            R.id.dl_chart -> {
                saveChartPng()
            }
            R.id.show_table -> {
                val intent = Intent(applicationContext, LogActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun saveChartPng(){
        val dateStr = DateFormat.format("yyMMdd-HHmm", Calendar.getInstance())
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "sleep-medicine-chart-${dateStr}.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.IS_PENDING, "1")
        }

        val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        contentResolver.run {
            insert(contentUri, contentValues)?.let { uri ->

                openOutputStream(uri)?.let { stream ->
                    val chartView = findViewById<ConstraintLayout>(R.id.chart_area)
                    chartView.drawToBitmap().compress(Bitmap.CompressFormat.PNG, 100, stream)
                    stream.close()

                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    update( uri, contentValues, null, null)
                }

                Snackbar.make(
                    findViewById(R.id.snack_bar_container),
                    getText(R.string.snackbar_save_png_message),
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart)

        findViewById<RecyclerView>(R.id.cycle_chart).apply {
            layoutManager = LinearLayoutManager(applicationContext)
            adapter = RealmAdapter(
                Realm.getDefaultInstance().where<DailyActivity>().findAll()
                    .sort("day", Sort.DESCENDING)
            )
            background = RuledLine()
        }

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
                            val actionList = BottomSheetActionList(actions, isDatePicker = false, isTimePicker = false, day = cal.time)
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


    private inner class Element(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val drawArea: ImageView = itemView.findViewById(R.id.draw_area)
    }

    private inner class RealmAdapter(private val days: OrderedRealmCollection<DailyActivity>) :
        RealmRecyclerViewAdapter<DailyActivity, Element>(days, true) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Element {
            return Element(
                LayoutInflater.from(applicationContext)
                    .inflate(R.layout.cycle_chart_element, parent, false)
            )
        }

        override fun onBindViewHolder(element: Element, position: Int) {
            val day = days[position].day
            val cycles = days[position].activityStack
            val takenMedicines = days[position].medicineStack

            if( position > 0){
                element.drawArea.setImageDrawable(DrawPattern(day, cycles, takenMedicines))
            } else {
                element.drawArea.setImageDrawable(DrawPattern(day, cycles, takenMedicines, isFirstRow = true))
            }

            element.itemView.setOnClickListener {
                val intent = Intent(applicationContext, MainActivity::class.java)
                intent.putExtra("Day", DateFormat.format("yyyy/MM/dd", day))
                startActivity(intent)
            }
        }

        override fun getItemCount(): Int {
            return days.size
        }
    }

    class DrawPattern(
        day: Date?,
        activityStack: RealmList<Activity>?,
        medicineStack: RealmList<Activity>?,
        isFirstRow: Boolean = false
    ) : Drawable() {

        val day = day
        val activityStack = activityStack
        val medicineStack = medicineStack
        val isfirstRow = isFirstRow

        //1日分のグラフ描画
        override fun draw(canvas: Canvas) {
            val barPaint = Paint().apply { color = Color.argb(200, 80, 190, 230) }
            val dotPaint = Paint().apply {
                color = Color.argb(200, 255, 193, 7)
                isAntiAlias = true
            }

            activityStack?.filter { activity ->
                    arrayOf("Sleep", "睡眠", "入眠").contains(activity.name)
                }?.forEachIndexed { index, activity ->
                    if ( index != activityStack.lastIndex) {
                        canvas.drawRect(bar(activity, canvas), barPaint)
                    } else {
                        canvas.drawRect(bar(activity, canvas, isLastOfDay = true), barPaint)
                    }
            }

            medicineStack?.forEach { medicine ->
                val timing: Float = ((medicine.startTime!!.time - day!!.time) / 1000).toFloat()
                val x: Float = ratioOfDay(timing) * bounds.right
                val y: Float = bounds.exactCenterY()
                val r: Float = (bounds.height() * 0.2).toFloat()
                canvas.drawCircle(x, y, r, dotPaint)
            }

            when (DateFormat.format("d", day) as String) {
                "1","10","20" -> {
                    addDayLabel(day!!, canvas)
                }
                else -> {
                    if ( isfirstRow ) {
                        addDayLabel(day!!, canvas)
                    }
                }
            }
        }

        private fun bar(activity: Activity, canvas: Canvas, isLastOfDay: Boolean = false): Rect {
            val startSec: Float = ((activity.startTime!!.time - day!!.time) / 1000).toFloat()
            val left = (ratioOfDay(startSec) * canvas.width).toInt()

            var right: Int
            val length: Float = (activity.length!! / 1000).toFloat()
            when ( isLastOfDay ) {
                true -> right = bounds.width()
                false -> right = ((ratioOfDay(length) * canvas.width) + left).toInt()
            }
            //5分以上でないと1px以上にならないため
            if ((right - left) < 1) {
                right += 1
            }

            return Rect(left, 0, right, canvas.height)
        }

        private fun addDayLabel(day: Date, canvas: Canvas){

            val size :Float = ( bounds.height() * 0.66 ).toFloat()
            val padding :Float = (( bounds.height() - size ) * 0.25 ).toFloat()
            val dayLabel = DateFormat.format("M/d", day) as String
            val paint = Paint().apply {
                color = Color.rgb(90, 100, 100)
                textSize = size
                isAntiAlias = true
            }

            canvas.drawText( dayLabel, 10f, size + padding, paint )
        }

        private fun ratioOfDay(second: Float): Float {
            return second / (24 * 60 * 60)
        }

        override fun setAlpha(alpha: Int) {
            this.alpha = alpha
        }

        override fun setColorFilter(filter: ColorFilter?) {
            this.colorFilter = filter
        }

        override fun getOpacity(): Int = PixelFormat.OPAQUE
    }

    class RuledLine : Drawable() {
        override fun draw(canvas: Canvas) {
            val dayOfHour = 24
            val spaceEvery1Hour = bounds.right / dayOfHour

            val hourLine = Paint().apply { color = Color.rgb(192, 192, 192) }
            val sixHourLine = Paint().apply { color = Color.rgb(32, 32, 32) }

            for (i in 1 until (dayOfHour)) {
                val startX = (i * spaceEvery1Hour).toFloat()
                val startY = 0F
                val endX = startX
                val endY = bounds.bottom.toFloat()

                if (i.rem(6) == 0) {
                    canvas.drawLine(startX, startY, endX, endY, sixHourLine)
                } else {
                    canvas.drawLine(startX, startY, endX, endY, hourLine)
                }
            }
        }

        override fun setAlpha(alpha: Int) {
            this.alpha = alpha
        }

        override fun setColorFilter(filter: ColorFilter?) {
            this.colorFilter = filter
        }

        override fun getOpacity(): Int = PixelFormat.OPAQUE
    }
}


