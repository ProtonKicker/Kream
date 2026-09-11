package ru.ytkab0bp.beamklipper.utils

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Process
import android.provider.MediaStore
import android.util.Log
import ru.ytkab0bp.beamklipper.KlipperInstance
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Collects the diagnostic logs the user is most likely to need when something
 * won't start: the app / WebService process (via logcat), and the per-instance
 * Klipper and Moonraker log files (which those services write themselves, since
 * they run in separate processes).
 *
 * Everything here is best-effort and never throws to the caller.
 */
object BeamLogs {

    /** How much of any single source we keep in memory / show / share. */
    private const val MAX_CHARS = 240_000

    data class Source(
        val id: String,
        /** Short human label for the picker. */
        val label: String,
        /** Producer of the (already tail-trimmed) text. Runs off the main thread. */
        val load: () -> String,
    )

    fun crashFile(ctx: Context): File =
        File(ctx.getExternalFilesDir(null) ?: ctx.filesDir, "last_crash.txt")

    /**
     * Build the list of log sources available right now. The instance list is
     * whatever exists; a not-yet-created log file just shows an explanatory line.
     */
    fun sources(ctx: Context): List<Source> {
        val list = ArrayList<Source>()

        list += Source("app", "App / Web") { readLogcat() }

        val crash = crashFile(ctx)
        if (crash.exists() && crash.length() > 0) {
            list += Source("crash", "Última falha") { tail(readTextOrEmpty(crash)) }
        }

        for (inst in KlipperInstance.getInstances()) {
            val name = inst.name.ifBlank { inst.id ?: "?" }
            val logs = File(inst.publicDirectory, "logs")
            list += Source("klippy_${inst.id}", "Klipper · $name") {
                readInstanceLog(File(logs, "klippy.log"))
            }
            list += Source("moonraker_${inst.id}", "Moonraker · $name") {
                readInstanceLog(File(logs, "moonraker.log"))
            }
        }
        return list
    }

    /** One combined blob of every source, for the Share button. */
    fun combined(ctx: Context): String = buildString {
        append("=== Kocoa Beam — diagnóstico ===\n")
        append("device: ").append(Build.MANUFACTURER).append(' ').append(Build.MODEL)
            .append("  sdk=").append(Build.VERSION.SDK_INT)
            .append("  abi=").append(Build.SUPPORTED_ABIS.joinToString(",")).append('\n')
        append("time: ").append(System.currentTimeMillis()).append("\n\n")
        for (s in sources(ctx)) {
            append("\n########## ").append(s.label).append(" ##########\n")
            append(runCatching(s.load).getOrElse { "<falha ao ler: ${it.message}>" })
            append('\n')
        }
    }.let { if (it.length > MAX_CHARS) it.substring(it.length - MAX_CHARS) else it }

    /**
     * Write [combined] to a file the user can find with a file manager and
     * returns a human-readable location, or null on failure.
     *
     * API 29+: the public Downloads collection (no permission needed).
     * Older: the app's external files dir (Android/data/<pkg>/files/logs/),
     * also visible without any permission.
     */
    fun saveForSharing(ctx: Context): String? {
        val name = "kocoa-beam-logs-" +
                SimpleDateFormat("yyyyMMdd-HHmmss", Locale.ROOT).format(Date()) + ".txt"
        val body = combined(ctx).toByteArray()
        return try {
            if (Build.VERSION.SDK_INT >= 29) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, name)
                    put(MediaStore.Downloads.MIME_TYPE, "text/plain")
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                val resolver = ctx.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    ?: return null
                resolver.openOutputStream(uri)?.use { it.write(body) } ?: return null
                values.clear()
                values.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
                "Downloads/$name"
            } else {
                val dir = File(ctx.getExternalFilesDir(null) ?: ctx.filesDir, "logs")
                    .apply { mkdirs() }
                val f = File(dir, name)
                f.writeBytes(body)
                f.absolutePath
            }
        } catch (t: Throwable) {
            Log.w("BeamLogs", "saveForSharing failed", t)
            null
        }
    }

    // --- internals -----------------------------------------------------------

    private fun readInstanceLog(f: File): String {
        if (!f.exists()) return "(${f.name} ainda não foi criado — o serviço não chegou a rodar nesta sessão)"
        val txt = readTextOrEmpty(f)
        return if (txt.isBlank()) "(${f.name} está vazio)" else tail(txt)
    }

    /** Dump this process's own logcat (main process → app + WebService + instance orchestration). */
    private fun readLogcat(): String {
        return try {
            val pid = Process.myPid()
            val cmd = if (Build.VERSION.SDK_INT >= 24)
                arrayOf("logcat", "-d", "-v", "time", "--pid=$pid")
            else
                arrayOf("logcat", "-d", "-v", "time")
            val p = ProcessBuilder(*cmd).redirectErrorStream(true).start()
            val out = p.inputStream.bufferedReader().use { it.readText() }
            p.waitFor()
            val filtered = if (Build.VERSION.SDK_INT >= 24) out else out.lines()
                .filter { it.contains(" $pid ") || it.contains("beam_") || it.contains("WebService")
                        || it.contains("moonraker_") || it.contains("KlipperInstance") }
                .joinToString("\n")
            if (filtered.isBlank()) "(logcat vazio — o buffer do sistema pode ter sido limpo)" else tail(filtered)
        } catch (t: Throwable) {
            Log.w("BeamLogs", "logcat failed", t)
            "(não consegui ler o logcat neste aparelho: ${t.message})"
        }
    }

    private fun readTextOrEmpty(f: File): String =
        try { f.readText() } catch (t: Throwable) { "<erro ao ler ${f.name}: ${t.message}>" }

    private fun tail(s: String): String =
        if (s.length <= MAX_CHARS) s
        else "…(início cortado, mostrando os últimos ${MAX_CHARS / 1000} KB)…\n" +
                s.substring(s.length - MAX_CHARS)
}
