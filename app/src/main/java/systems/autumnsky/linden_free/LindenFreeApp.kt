package systems.autumnsky.linden_free

import android.app.Application
import androidx.core.app.AppLaunchChecker
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import systems.autumnsky.linden_free.model.Action
import java.util.*


class LindenFreeApp : Application() {

    var isFirstLaunch: Boolean = false

    override fun onCreate() {
        super.onCreate()

        isFirstLaunch = !AppLaunchChecker.hasStartedFromLauncher(applicationContext)

        Realm.init(this)
        val builder = RealmConfiguration.Builder()
        builder.schemaVersion(3).migration(Migration())
        val config = builder.build()
        Realm.setDefaultConfiguration(config)

        if (isFirstLaunch){
            insertDefaultActions()
        }
    }

    private fun insertDefaultActions() {
        val defaultEvents = mutableListOf(
            getString(R.string.awake),
            getString(R.string.dose),
            getString(R.string.in_bed),
            getString(R.string.sleep)
        )

        val realm = Realm.getDefaultInstance()

        defaultEvents.forEach { actionName ->
            if( realm.where<Action>().equalTo("name", actionName ).findAll().count()== 0 ){
                realm.executeTransaction {
                    val action = realm.createObject<Action>(UUID.randomUUID().toString())
                    action.name = actionName
                    realm.copyToRealm(action)
                }
            }
        }
        realm.close()
    }
}