package systems.autumnsky.linden_free

import android.content.Intent
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView

class RegisterMedicine : AppCompatActivity() {

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_log -> {
                val intent = Intent(applicationContext, LogView::class.java)
                startActivity(intent)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_home -> {
                val intent = Intent(applicationContext, MainActivity::class.java)
                startActivity(intent)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_medicine -> {
                val intent = Intent(applicationContext, RegisterMedicine::class.java)
                startActivity(intent)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medicine)

        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.selectedItemId = R.id.navigation_medicine
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
    }
}
