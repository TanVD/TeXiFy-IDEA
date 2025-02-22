package nl.hannahsten.texifyidea.run.linuxpdfviewer

import com.intellij.openapi.util.SystemInfo
import nl.hannahsten.texifyidea.run.linuxpdfviewer.evince.EvinceConversation
import nl.hannahsten.texifyidea.run.linuxpdfviewer.okular.OkularConversation
import nl.hannahsten.texifyidea.run.sumatra.isSumatraAvailable

/**
 * List of supported PDF viewers on Linux.
 *
 * @param viewerCommand The command to call the viewer from the command line.
 * @param conversation The conversation class needed/used to talk to this viewer.
 */
enum class PdfViewer(private val viewerCommand: String,
                     val displayName: String,
                     val conversation: ViewerConversation?) {
    EVINCE("evince", "Evince", EvinceConversation),
    OKULAR("okular", "Okular", OkularConversation),
    SUMATRA("sumatra", "Sumatra", null), // Dummy options to support Windows and Mac.
    OTHER("other", "Custom PDF viewer", null);

    /**
     * Check if the viewer is installed and available from the path.
     */
    fun isAvailable(): Boolean {
        // Using a custom PDF viewer should always be an option.
        return if (this == OTHER) {
            true
        }
        else if (SystemInfo.isWindows && this == SUMATRA) {
            isSumatraAvailable
        }
        // Only support Evince and Okular on Linux, although they can be installed on other systems like Mac.
        else if (SystemInfo.isLinux) {
            // Find out whether the pdf viewer is installed and in PATH, otherwise we can't use it.
            val output = "which ${this.viewerCommand}".runCommand()
            output?.contains("/${this.viewerCommand}") ?: false
        }
        else {
            false
        }
    }

    companion object {
        fun availableSubset(): List<PdfViewer> = values().filter { it.isAvailable() }
        fun firstAvailable(): PdfViewer = availableSubset().first()
    }
}