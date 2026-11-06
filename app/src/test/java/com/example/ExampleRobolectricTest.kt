package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.Project
import com.example.engine.builder.AndroidApkBuilder
import com.example.engine.gguf.DeviceMemoryMonitor
import com.example.engine.skills.SkillsRepository
import org.junit.Assert.*
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
    assertEquals("AI Studio", appName)
  }

  @Test
  fun `verify installed skills catalog`() {
    val skills = SkillsRepository.getInstalledSkills()
    assertTrue(skills.isNotEmpty())
    assertTrue(skills.any { it.name.contains("Compose", ignoreCase = true) })
    assertTrue(skills.any { it.category == com.example.data.model.SkillCategory.REVERSE_ENGINEERING || it.name.contains("APK", true) })
    assertTrue(skills.any { it.name.contains("Gradle", ignoreCase = true) })
  }

  @Test
  fun `verify on-device APK generation and file existence`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val testProject = Project(
      id = "test-proj",
      name = "Test App",
      description = "Unit test project",
      packageName = "com.aistudio.testapp"
    )

    val apkFile = AndroidApkBuilder.generatePhysicalApkFile(context, testProject, false)
    assertTrue(apkFile.exists())
    assertTrue(apkFile.length() > 0)
    assertTrue(apkFile.name.endsWith(".apk"))
  }

  @Test
  fun `verify device memory monitoring`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val mem = DeviceMemoryMonitor.getMemoryStatus(context)
    assertTrue(mem.totalRamMb > 0)
  }
}
