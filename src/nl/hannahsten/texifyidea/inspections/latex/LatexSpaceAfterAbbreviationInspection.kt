package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import nl.hannahsten.texifyidea.insight.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.psi.LatexNormalText
import nl.hannahsten.texifyidea.util.Magic
import nl.hannahsten.texifyidea.util.childrenOfType
import nl.hannahsten.texifyidea.util.files.document
import nl.hannahsten.texifyidea.util.inMathContext

/**
 * @author Hannah Schellekens
 */
open class LatexSpaceAfterAbbreviationInspection : TexifyInspectionBase() {

    override val inspectionGroup = InsightGroup.LATEX

    override fun getDisplayName() = "Normal space after abbreviation"

    override val inspectionId = "SpaceAfterAbbreviation"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): MutableList<ProblemDescriptor> {
        val descriptors = descriptorList()

        val texts = file.childrenOfType(LatexNormalText::class)
        for (text in texts) {
            if (text.inMathContext()) {
                continue
            }

            // We find ranges where comments start and end, to check if matches are in there
            val commentMatcher = Magic.Pattern.comments.matcher(text.text)
            val commentParts = arrayListOf<IntRange>()
            while (commentMatcher.find()) {
                commentParts.add(commentMatcher.start()..commentMatcher.end())
            }

            val matcher = Magic.Pattern.abbreviation.matcher(text.text)
            while (matcher.find()) {
                val matchRange = matcher.start()..matcher.end()

                if (!isFollowedByWhitespace(text, matchRange) || text.text.length < matcher.end() + 1) {
                    continue
                }

                // If the match is inside a comment, ignore it
                if (commentParts.any { it.first < matchRange.first && it.last > matchRange.last }) {
                    continue
                }

                descriptors.add(manager.createProblemDescriptor(
                        text,
                        TextRange(matchRange.last - 1, matchRange.last + 1),
                        "Abbreviation is not followed by a normal space",
                        ProblemHighlightType.WEAK_WARNING,
                        isOntheFly,
                        NormalSpaceFix(matchRange)
                ))
            }
        }

        return descriptors
    }

    private fun isFollowedByWhitespace(text: LatexNormalText, matchRange: IntRange): Boolean {
        // Whitespace followed in the Normal Text.
        val string = text.text
        if (text.text.length > matchRange.last) {
            val spaceMaybe = string.substring(matchRange.last, matchRange.last + 1)
            if (matchRange.last < string.length && spaceMaybe.matches(Regex("\\s+"))) {
                return true
            }
        }

        // Whitespace as PsiWhitespace
        val content = text.parent?.parent ?: return false
        return content.nextSibling is PsiWhiteSpace
    }

    /**
     * @author Hannah Schellekens
     */
    private open class NormalSpaceFix(val whitespaceRange: IntRange) : LocalQuickFix {

        override fun getFamilyName() = "Insert normal space"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val element = descriptor.psiElement as LatexNormalText
            val file = element.containingFile
            val document = file.document() ?: return

            replaceNormalText(document, element)
        }

        private fun replaceNormalText(document: Document, normalText: LatexNormalText) {
            val start = normalText.textOffset + whitespaceRange.last
            val end = normalText.textOffset + whitespaceRange.last + 1
            document.replaceString(start, end, "\\ ")
        }
    }
}