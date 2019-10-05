package systems.autumnsky.linden_free

import android.app.Application
import androidx.core.app.AppLaunchChecker
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.kotlin.createObject
import java.util.*


class LindenFreeApp : Application() {

    var isFirstLaunch: Boolean = false

    override fun onCreate() {
        super.onCreate()

        isFirstLaunch = !AppLaunchChecker.hasStartedFromLauncher(applicationContext)

        Realm.init(this)
        val config = RealmConfiguration.Builder().schemaVersion(1).build()
        Realm.setDefaultConfiguration(config)

        //薬以外の必須イベントをEventモデルに追加
        val defaultEvents = mutableListOf(
            getString(R.string.awake),
            getString(R.string.dose),
            getString(R.string.in_bed),
            getString(R.string.sleep)
        )

        val realm = Realm.getDefaultInstance()

        defaultEvents.forEach {
            val name = it
            realm.executeTransaction {
                val event = realm.createObject<Event>(UUID.randomUUID().toString())
                event.name = name
                realm.copyToRealm(event)
            }
        }

        realm.close()
    }
}