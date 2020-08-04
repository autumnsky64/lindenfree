package systems.autumnsky.linden_free

import android.content.Intent
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.realm.*
import io.realm.kotlin.where
import systems.autumnsky.linden_free.model.Action
import systems.autumnsky.linden_free.model.Cycle
import systems.autumnsky.linden_free.model.DailyCycle
import java.util.*

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
        val drawArea : ImageView = itemView.findViewById(R.id.draw_area)
    }

    private inner class RealmAdapter(private val days: OrderedRealmCollection<DailyCycle>) :
        RealmRecyclerViewAdapter<DailyCycle, Element>(days, true) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Element {
            return Element(
                LayoutInflater.from(applicationContext).inflate(R.layout.cycle_chart_element, parent, false)
            )
        }

        override fun onBindViewHolder(element: Element, position: Int) {
            val day = days[position].day
            val cycles = days[position].stack
            element.drawArea.setImageDrawable(DrawCycle(day, cycles))
        }

        override fun getItemCount(): Int {
            return days.size
        }
    }

    class DrawCycle(
        day : Date?,
        stack : RealmList<Cycle>?
    ): Drawable(){

        //1日分のグラフ描画
        override fun draw(canvas: Canvas) {
            val paint = Paint().apply {
                color = Color.rgb(101, 202, 239)
            }
            val width = (0..1000).random().toFloat()
            canvas.drawRect(0F, 0F, width, canvas.maximumBitmapHeight.toFloat(), paint)
        }

        override fun setAlpha(alpha: Int) {
            this.alpha = alpha
        }

        override fun setColorFilter(filter: ColorFilter?) {
            this.colorFilter = filter
        }

        override fun getOpacity(): Int =
            PixelFormat.OPAQUE

    }
}
