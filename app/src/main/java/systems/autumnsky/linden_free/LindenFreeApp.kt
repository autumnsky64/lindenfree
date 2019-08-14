package systems.autumnsky.linden_free

import android.app.Application
import io.realm.Realm


class LindenFreeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
    }
}