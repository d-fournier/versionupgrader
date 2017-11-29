package me.dfournier.versionupgrader.annotations

/**
 * Execute the annotated method if the upgrader notice that the
 * software has been updated and migration scripts must be applied.<br/>
 * The annotated method must be contained in a [UpdateSuite] to be executed
 *
 * @param version The version since when the update must me applied
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Update(
        val version: Int
)