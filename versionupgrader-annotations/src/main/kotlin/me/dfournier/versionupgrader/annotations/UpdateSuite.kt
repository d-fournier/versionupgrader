package me.dfournier.versionupgrader.annotations


/**
 * Indicate to the Version Upgrader that the current class can apply updates
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class UpdateSuite