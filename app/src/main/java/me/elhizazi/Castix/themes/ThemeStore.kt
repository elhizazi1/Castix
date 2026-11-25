package me.elhizazi.Castix.themes

import android.content.Context
import android.net.Uri
import org.json.JSONObject
import java.io.File
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import java.util.zip.ZipEntry

internal data class CastixTheme(val id: String, val name: String, val entry: String, val folder: File)
internal class ThemeStore(private val context: Context) {
    private val root = File(context.filesDir, "amoled_themes").apply { mkdirs() }
    private fun read(folder: File): CastixTheme {
        val json = JSONObject(File(folder, "plugins.json").readText())
        val id = json.getString("id")
        require(id.matches(Regex("[a-zA-Z0-9][a-zA-Z0-9._-]{0,79}")))
        require(json.getInt("apiVersion") == 1 && json.getString("type") == "amoled-theme")
        val entry = json.getString("entry")
        require(entry.endsWith(".html") && safeFile(folder, entry).isFile)
        val name = json.getString("name"); require(name.isNotBlank() && name.length <= 100)
        return CastixTheme(id, name, entry, folder)
    }
    fun list(): List<CastixTheme> = root.listFiles().orEmpty().filter { it.isDirectory && !it.name.startsWith(".") }.mapNotNull { runCatching { read(it).takeIf { theme -> theme.id == it.name } }.getOrNull() }.sortedBy { it.name }
    fun find(id: String?): CastixTheme? = list().firstOrNull { it.id == id }
    fun delete(id: String) { find(id)?.folder?.deleteRecursively() }
    private fun unpack(uri: Uri, action: (CastixTheme) -> Unit) {
        val stage = java.nio.file.Files.createTempDirectory(root.toPath(), ".import-").toFile()
        try {
            var total = 0L; var count = 0
            val seen = mutableSetOf<String>()
            ZipInputStream(requireNotNull(context.contentResolver.openInputStream(uri))).use { zip ->
                while (true) {
                    val entry = zip.nextEntry ?: break
                    require(++count <= 512)
                    val file = safeFile(stage, entry.name.trimEnd('/'))
                    require(seen.add(file.canonicalPath))
                    if (entry.isDirectory) file.mkdirs() else {
                        file.parentFile?.mkdirs()
                        file.outputStream().use { out ->
                            val buffer = ByteArray(8192)
                            while (true) {
                                val n = zip.read(buffer); if (n < 0) break
                                total += n; require(total <= 16L * 1024 * 1024)
                                out.write(buffer, 0, n)
                            }
                        }
                    }
                    zip.closeEntry()
                }
            }
            require(File(stage, "plugins.json").length() <= 65536)
            action(read(stage))
        } finally { stage.deleteRecursively() }
    }
    fun install(uri: Uri) = unpack(uri) { theme ->
        val target = File(root, theme.id)
        require(!target.exists())
        check(theme.folder.renameTo(target))
    }
    fun convert(source: Uri, destination: Uri) = unpack(source) { theme ->
        ZipOutputStream(requireNotNull(context.contentResolver.openOutputStream(destination, "wt"))).use { zip ->
            theme.folder.walkTopDown().filter { it.isFile }.forEach { file ->
                zip.putNextEntry(ZipEntry(file.relativeTo(theme.folder).invariantSeparatorsPath)); file.inputStream().use { it.copyTo(zip) }; zip.closeEntry()
            }
        }
    }
    companion object {
        fun safeFile(root: File, name: String): File {
            require(name.isNotBlank() && !name.startsWith('/') && !name.contains('\\') && name.split('/').none { it == ".." || it == "." || it.isEmpty() })
            return File(root, name).canonicalFile.also { require(it.path.startsWith(root.canonicalPath + File.separator)) }
        }
    }
}
