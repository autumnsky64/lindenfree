package systems.autumnsky.linden_free

import android.content.Intent
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.format.DateFormat
import android.view.*
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
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
            }
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
                Realm.getDefaultInstance().where<DailyActivity>().findAll()
                    .sort("day", Sort.DESCENDING)
            )
            background = RuledLine()
        }

        //FAB
        findViewById<View>(R.id.insert_event).setOnClickListener {
            val actions = Realm.getDefaultInstance().where<Action>()
                .notEqualTo("name", getString(R.string.dose)).findAll()
            val actionList = BottomSheetActionList(actions)
            actionList.show(supportFragmentManager, actionList.tag)
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
            element.drawArea.setImageDrawable(DrawPattern(day, cycles, takenMedicines))
        }

        override fun getItemCount(): Int {
            return days.size
        }
    }

    class DrawPattern(
        day: Date?,
        activityStack: RealmList<Activity>?,
        medicineStack: RealmList<Activity>?
    ) : Drawable() {

        val day = day
        val activityStack = activityStack
        val medicineStack = medicineStack

        //1日分のグラフ描画
        override fun draw(canvas: Canvas) {
            val barPaint = Paint().apply { color = Color.rgb(101, 202, 239) }
            val dotPaint = Paint().apply { color = Color.rgb(255, 193, 7) }

            activityStack?.forEach { cycle ->
                if (cycle.name != "Sleep" && cycle.name != "睡眠") {
                    return@forEach
                }
                canvas.drawRect(bar(cycle, canvas), barPaint)
            }

            medicineStack?.forEach { medicine ->
                val timing: Float = ((medicine.startTime!!.time - day!!.time) / 1000).toFloat()
                val x: Float = ratioOfDay(timing) * canvas.width
                val y: Float = (canvas.height * 0.5).toFloat()
                val r: Float = (canvas.height * 0.2).toFloat()
                canvas.drawCircle(x, y, r, dotPaint)
            }

            day?.let{
                addDayLabel(day, canvas)
            }
        }

        private fun bar(activity: Activity, canvas: Canvas): Rect {
            val startSec: Float = ((activity.startTime!!.time - day!!.time) / 1000).toFloat()
            val left = (ratioOfDay(startSec) * canvas.width).toInt()

            val length: Float = (activity.length!! / 1000).toFloat()
            var right = ((ratioOfDay(length) * canvas.width) + left).toInt()

            //5分以上でないと1px以上にならないため
            if ((right - left) < 1) {
                right += 1
            }

            return Rect(left, 0, right, canvas.height)
        }

        private fun addDayLabel(day: Date, canvas: Canvas){
            when (DateFormat.format("d", day) as String) {
                "1","10","20" -> {
                    val size :Float = ( canvas.height * 0.66 ).toFloat()
                    val padding :Float = (( canvas.height - size ) * 0.25 ).toFloat()
                    val dayLabel = DateFormat.format("M/d", day) as String
                    val paint = Paint().apply {
                        color = Color.rgb(100, 100, 100)
                        textSize = size
                        isAntiAlias = true }

                    canvas.drawText( dayLabel, 10f, size + padding, paint )
                }
            }
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


