package de.dimskiy.waypoints.platform.storage

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class TestAppDatabaseRule(private val testClassInstance: Any) : TestRule {

    lateinit var dbInstance: AppDatabase

    override fun apply(
        base: Statement,
        description: Description
    ): Statement? = object : Statement() {

        override fun evaluate() {
            dbInstance = Room.inMemoryDatabaseBuilder(
                context = InstrumentationRegistry.getInstrumentation().context,
                klass = AppDatabase::class.java
            ).build()

            testClassInstance.injectDatabaseIntoFields(description)

            try {
                base.evaluate()
            } finally {
                dbInstance.close()
            }
        }
    }

    private fun Any.injectDatabaseIntoFields(description: Description) {
        for (field in description.testClass.declaredFields) {
            field.isAccessible = true

            when (field.type) {
                AppDatabase::class.java -> field.set(this, dbInstance)

                WaypointsDao::class.java -> field.set(this, dbInstance.waypointDao())
            }
        }
    }
}