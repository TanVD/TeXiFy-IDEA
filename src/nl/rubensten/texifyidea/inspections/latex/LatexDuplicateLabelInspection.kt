package nl.rubensten.texifyidea.inspections.latex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.inspections.InspectionGroup
import nl.rubensten.texifyidea.inspections.TexifyInspectionBase
import nl.rubensten.texifyidea.util.commandsInFile
import nl.rubensten.texifyidea.util.commandsInFileSet
import nl.rubensten.texifyidea.util.requiredParameter
import kotlin.reflect.jvm.internal.impl.utils.SmartList

/**
 * @author Ruben Schellekens
 */
open class LatexDuplicateLabelInspection : TexifyInspectionBase() {

    companion object {
        val NO_FIX: LocalQuickFix? = null
    }

    override fun getInspectionGroup() = InspectionGroup.LATEX

    override fun getDisplayName() = "Duplicate labels"

    override fun getInspectionId(): String = "DuplicateLabel"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = SmartList<ProblemDescriptor>()

        // Fill up a set of labels.
        val labels: MutableSet<String> = HashSet()
        for (cmd in file.commandsInFileSet()) {
            if (cmd.containingFile == file) {
                continue
            }

            if (cmd.name == "\\label" || cmd.name == "\\bibitem") {
                labels.add(cmd.requiredParameter(0) ?: continue)
            }
        }

        // Check labels in file.
        for (cmd in file.commandsInFile()) {
            if (cmd.name != "\\label" && cmd.name != "\\bibitem") {
                continue
            }

            val labelName = cmd.requiredParameter(0) ?: continue
            if (labelName in labels) {
                descriptors.add(manager.createProblemDescriptor(
                        cmd,
                        TextRange.from(cmd.commandToken.textLength + 1, labelName.length),
                        "Duplicate label",
                        ProblemHighlightType.GENERIC_ERROR,
                        isOntheFly
                ))
            }
        }

        return descriptors
    }
}