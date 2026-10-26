package com.dd3boh.outertune.db

import org.junit.Assert.assertEquals
import org.junit.Test

class AlbumSavedOrderSqlTest {
    private fun java.sql.Connection.add(id: String, imported: Long, position: Long?, saved: Boolean = true) {
        createStatement().use { it.execute("INSERT INTO album(id,title,songCount,duration,lastUpdateTime,bookmarkedAt,savedOrder) VALUES ('$id','$id',0,0,0,${if (saved) imported else "NULL"},${position ?: "NULL"})") }
    }
    private fun java.sql.Connection.ids(order: String): List<String> = createStatement().use { st ->
        st.executeQuery("SELECT id FROM album WHERE bookmarkedAt IS NOT NULL ORDER BY $order").use { rs ->
            buildList { while (rs.next()) add(rs.getString(1)) }
        }
    }
    @Test fun `remote history wins over insertion and import timestamps`() {
        SchemaDb.open().use { db ->
            db.add("middle", 9000, 1)
            db.add("newest", 1000, 2)
            db.add("oldest", 12000, 0)
            assertEquals(listOf("newest", "middle", "oldest"), db.ids(AlbumSavedOrderSql.DESC))
            assertEquals(listOf("oldest", "middle", "newest"), db.ids(AlbumSavedOrderSql.ASC))
        }
    }
    @Test fun `new app saves stay ahead until the next remote snapshot and unsaved albums stay out`() {
        SchemaDb.open().use { db ->
            db.add("remote", 999999, 100)
            db.add("localOld", 1000, null)
            db.add("localNew", 2000, null)
            db.add("removed", 3000, null, saved = false)
            assertEquals(listOf("localNew", "localOld", "remote"), db.ids(AlbumSavedOrderSql.DESC))
            db.createStatement().use { it.execute("UPDATE album SET savedOrder=101 WHERE id='localOld'") }
            assertEquals(listOf("localNew", "localOld", "remote"), db.ids(AlbumSavedOrderSql.DESC))
            db.createStatement().use { it.execute("UPDATE album SET savedOrder=102 WHERE id='localNew'") }
            assertEquals(listOf("localNew", "localOld", "remote"), db.ids(AlbumSavedOrderSql.DESC))
        }
    }
    @Test fun `home uses the same saved order and limits after sorting`() {
        SchemaDb.open().use { db ->
            (0..30).shuffled(java.util.Random(42)).forEach { db.add("a$it", (100-it).toLong(), it.toLong()) }
            val expected = db.ids(AlbumSavedOrderSql.DESC).take(24)
            val actual = db.createStatement().use { st ->
                st.executeQuery(AlbumSavedOrderSql.RECENT.replace(":limit", "24")).use { rs ->
                    buildList { while (rs.next()) add(rs.getString("id")) }
                }
            }
            assertEquals(expected, actual)
        }
    }
}
