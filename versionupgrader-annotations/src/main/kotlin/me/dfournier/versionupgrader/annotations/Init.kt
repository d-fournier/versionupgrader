package me.dfournier.versionupgrader.annotations


/**
 * Execute the annotated method if the upgrader is executed for the first time<br/>
 * The annotated method must be contained in a [UpdateSuite] to be executed
 *
 * @param version The version since when the update must me applied
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Init