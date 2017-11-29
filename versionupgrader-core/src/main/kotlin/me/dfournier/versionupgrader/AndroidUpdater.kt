package me.dfournier.versionupgrader

/**
 * Created by dfournier on 29/11/17.
 */
abstract class AndroidUpdater : Updater {


    override fun getVersionUpgraderData(): Int? {
        return 0
    }

    override fun setVersionUpgraderData(data: Int) {

    }

}

private const val SHARED_PREF_NAME = "sp__versionupgrader"
private const val SHARED_PREF_KEY = "previous_version"