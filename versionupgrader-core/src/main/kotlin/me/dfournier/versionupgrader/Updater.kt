package me.dfournier.versionupgrader

interface Updater {
    /**
     * Return the current version
     */
    fun getCurrentVersion(): Int

    /**
     * Return the data needed for the version upgrader
     * @return The stored data from the last execution. Can be null if the versionUpgrader is executing for the first time
     */
    fun getVersionUpgraderData(): Int?

    /**
     * Return the data needed for the version upgrader
     * @param data The data stored by the library to process the following updates
     */
    fun setVersionUpgraderData(data: Int)
}