package me.elhizazi.Castix

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import me.elhizazi.Castix.privilege.domain.model.EngineType
import me.elhizazi.Castix.privilege.domain.model.PlaybackOptimizationTask
import me.elhizazi.Castix.privilege.domain.repository.PrivilegeEngineRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Castix", appName)
  }

  @Test
  fun `verify privilege engine strategy repository initialization`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repository = PrivilegeEngineRepository()

    val shizuku = repository.getEngine(EngineType.SHIZUKU)
    val dhizuku = repository.getEngine(EngineType.DHIZUKU)
    val root = repository.getEngine(EngineType.ROOT)
    val lsposed = repository.getEngine(EngineType.LSPOSED)

    assertEquals(EngineType.SHIZUKU, shizuku.engineType)
    assertEquals(EngineType.DHIZUKU, dhizuku.engineType)
    assertEquals(EngineType.ROOT, root.engineType)
    assertEquals(EngineType.LSPOSED, lsposed.engineType)

    val healthMap = repository.queryAllEngineHealth(context)
    assertEquals(6, healthMap.size)
    assertNotNull(healthMap[EngineType.SHIZUKU])
    assertNotNull(healthMap[EngineType.DHIZUKU])
    assertNotNull(healthMap[EngineType.ROOT])
    assertNotNull(healthMap[EngineType.LSPOSED])
    assertNotNull(healthMap[EngineType.LSPATCH])
    assertNotNull(healthMap[EngineType.ACCESSIBILITY])
  }

  @Test
  fun `verify playback optimization task execution`() = runBlocking {
    val repository = PrivilegeEngineRepository()
    val result = repository.executePlaybackTask(
      EngineType.SHIZUKU,
      PlaybackOptimizationTask.BATTERY_WHITELIST,
      "me.elhizazi.Castix.test"
    )

    assertNotNull(result)
    assertTrue(result.command.contains("dumpsys deviceidle whitelist"))
  }

  @Test
  fun `verify target apps repository whitelist persistence`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = me.elhizazi.Castix.privilege.domain.repository.TargetAppsRepository(context)

    val initialSelected = repo.getSelectedPackages()
    assertTrue(initialSelected.contains("com.google.android.youtube"))

    val testSet = setOf("com.google.android.youtube", "org.schabi.newpipe")
    repo.saveSelectedPackages(testSet)
    assertEquals(testSet, repo.getSelectedPackages())

    val apps = repo.getCombinedTargetApps()
    assertTrue(apps.isNotEmpty())
    assertTrue(apps.any { it.packageName == "com.google.android.youtube" && it.isSelected })
  }

  @Test
  fun `verify floating button mode preference`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = me.elhizazi.Castix.privilege.domain.repository.TargetAppsRepository(context)

    repo.setFloatingButtonMode(me.elhizazi.Castix.privilege.domain.model.FloatingButtonMode.ALWAYS_VISIBLE)
    assertEquals(
      me.elhizazi.Castix.privilege.domain.model.FloatingButtonMode.ALWAYS_VISIBLE,
      repo.getFloatingButtonMode()
    )

    repo.setFloatingButtonMode(me.elhizazi.Castix.privilege.domain.model.FloatingButtonMode.ONLY_SELECTED_APPS)
    assertEquals(
      me.elhizazi.Castix.privilege.domain.model.FloatingButtonMode.ONLY_SELECTED_APPS,
      repo.getFloatingButtonMode()
    )
  }
}

