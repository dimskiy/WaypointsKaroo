package de.dimskiy.waypoints.platform.storage.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = WaypointEntity.TABLE_NAME)
data class WaypointEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = COLUMN_ID) val id: Int = 0,
    @ColumnInfo(name = COLUMN_SERVER_TYPE) val serverType: ServerType = ServerType.PHOTON,
    @ColumnInfo(name = COLUMN_SERVER_ID) val serverId: String,
    @ColumnInfo(name = COLUMN_NAME) val name: String,
    @ColumnInfo(name = "country") val country: String?,
    @ColumnInfo(name = "city") val city: String?,
    @ColumnInfo(name = "zip") val zip: String?,
    @ColumnInfo(name = "street") val street: String?,
    @ColumnInfo(name = "qualifier1") val qualifier1: String?,
    @ColumnInfo(name = "qualifier2") val qualifier2: String?,
    @ColumnInfo(name = "latitude") val latitude: Double,
    @ColumnInfo(name = "longitude") val longitude: Double
) {
    enum class ServerType {
        PHOTON
    }

    companion object {
        const val TABLE_NAME = "waypoints"
        const val COLUMN_ID = "id"
        const val COLUMN_SERVER_ID = "serverId"
        const val COLUMN_SERVER_TYPE = "serverType"
        const val COLUMN_NAME = "name"
    }
}