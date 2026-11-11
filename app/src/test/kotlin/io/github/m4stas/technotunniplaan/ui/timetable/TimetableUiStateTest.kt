package io.github.m4stas.technotunniplaan.ui.timetable
import io.github.m4stas.technotunniplaan.Fixtures
import io.github.m4stas.technotunniplaan.schedule.*
import io.github.m4stas.technotunniplaan.store.*
import org.junit.Test
import org.junit.Assert.*
class TimetableUiStateTest {
 @Test fun buildsDayState() {
 val s = Schedule(listOf(Fixtures.week1())); val mon = Fixtures.MONDAY; val home = Settings(home = Fixtures.CLASS_A)
 val st = timetableUiState(s, home, Fixtures.CLASS_A, null, mon.atTime(9,0))
 assertEquals("K-XX-24A", st.title); assertTrue(st.isHome); assertFalse(st.entityMissing)
 assertEquals(mon, st.selectedDate); assertEquals(5, st.days.size)
 assertEquals(listOf(1,2), st.days[0].rows.map { it.size })
 assertEquals("Matemaatika", st.days[0].highlight!!.subject); assertNull(st.days[1].highlight)
 assertEquals("Nädal11 · 9.11–13.11", st.weekTitle); assertFalse(st.weekIsOutdated)
 assertEquals(mon.plusDays(4), timetableUiState(s,home,Fixtures.CLASS_A,null,mon.plusDays(5).atTime(9,0)).selectedDate)
 assertTrue(timetableUiState(s,home,Fixtures.CLASS_A,null,mon.plusDays(8).atTime(9,0)).weekIsOutdated)
 assertTrue(timetableUiState(s,home,EntityRef(EntityKind.CLASS,"gone"),null,mon.atTime(9,0)).entityMissing)
 assertNull(timetableUiState(Schedule(emptyList()),home,Fixtures.CLASS_A,null,mon.atTime(9,0)).selectedDate)
 assertTrue(timetableUiState(s,home.toggleFavourite(Fixtures.MARI),Fixtures.MARI,null,mon.atTime(9,0)).isFavourite)
 assertEquals(SyncError.NETWORK,timetableUiState(s,home.copy(lastSyncError=SyncError.NETWORK),Fixtures.CLASS_A,null,mon.atTime(9,0)).syncError)
 }
}
