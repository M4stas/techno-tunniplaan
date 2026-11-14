package io.github.m4stas.technotunniplaan.ui.timetable
import io.github.m4stas.technotunniplaan.Fixtures
import io.github.m4stas.technotunniplaan.schedule.*
import io.github.m4stas.technotunniplaan.store.*
import java.time.LocalTime
import org.junit.Test
import org.junit.Assert.*
class TimetableUiStateTest {
 @Test fun saturdayIsNeverShown() {
     val week = Fixtures.week1()
     val withSaturday = week.copy(lessons = week.lessons + week.lessons.first().copy(day = 5))
     val schedule = Schedule(listOf(withSaturday))
     val home = Settings(home = Fixtures.CLASS_A)
     val state = timetableUiState(schedule, home, Fixtures.CLASS_A, week.monday.plusDays(5), Fixtures.MONDAY.atTime(9, 0))
     assertEquals(5, state.days.size)
     assertEquals(week.monday.plusDays(4), state.selectedDate)
     assertEquals(week.title(), state.weekTitle)
     assertTrue(state.weekTitle.endsWith("13.11"))
 }
 @Test fun buildsDayState() {
 val s = Schedule(listOf(Fixtures.week1())); val mon = Fixtures.MONDAY; val home = Settings(home = Fixtures.CLASS_A)
 val st = timetableUiState(s, home, Fixtures.CLASS_A, null, mon.atTime(9,0))
 assertEquals("K-XX-24A", st.title); assertTrue(st.isHome); assertFalse(st.entityMissing)
 assertEquals(mon, st.selectedDate); assertEquals(5, st.days.size)
 assertEquals("Nädal11 · 9.11–13.11", st.weekTitle); assertFalse(st.weekIsOutdated)
 assertEquals(mon.plusDays(4), timetableUiState(s,home,Fixtures.CLASS_A,null,mon.plusDays(5).atTime(9,0)).selectedDate)
 assertTrue(timetableUiState(s,home,Fixtures.CLASS_A,null,mon.plusDays(8).atTime(9,0)).weekIsOutdated)
 assertTrue(timetableUiState(s,home,EntityRef(EntityKind.CLASS,"gone"),null,mon.atTime(9,0)).entityMissing)
 assertNull(timetableUiState(Schedule(emptyList()),home,Fixtures.CLASS_A,null,mon.atTime(9,0)).selectedDate)
 assertTrue(timetableUiState(s,home.toggleFavourite(Fixtures.MARI),Fixtures.MARI,null,mon.atTime(9,0)).isFavourite)
 assertEquals(SyncError.NETWORK,timetableUiState(s,home.copy(lastSyncError=SyncError.NETWORK),Fixtures.CLASS_A,null,mon.atTime(9,0)).syncError)
 }
 @Test fun buildsSlotsAndTheNextLesson() {
     val s = Schedule(listOf(Fixtures.week1())); val mon = Fixtures.MONDAY; val home = Settings(home = Fixtures.CLASS_A)
     val st = timetableUiState(s, home, Fixtures.CLASS_A, null, mon.atTime(9, 0))
     assertEquals(listOf(1, 2), st.days[0].slots.map { it.lines.size })
     assertEquals(SlotStatus.CURRENT, st.days[0].slots[0].status)
     assertTrue(st.days[1].slots.all { it.status == SlotStatus.UPCOMING })
     assertEquals(NextLessonUi(mon, LocalTime.of(10, 15), 0), st.nextLesson)
     assertEquals(NextLessonUi(mon.plusDays(1), LocalTime.of(12, 45), 1), timetableUiState(s, home, Fixtures.CLASS_A, null, mon.atTime(12, 0)).nextLesson)
     assertNull(timetableUiState(s, home, Fixtures.CLASS_A, null, mon.plusDays(4).atTime(14, 15)).nextLesson)
 }
}
