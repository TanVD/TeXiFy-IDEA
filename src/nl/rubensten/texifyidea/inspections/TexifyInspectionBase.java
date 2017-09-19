package nl.rubensten.texifyidea.inspections;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.psi.PsiFile;
import nl.rubensten.texifyidea.file.LatexFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Sten Wessel
 */
public abstract class TexifyInspectionBase extends LocalInspectionTool {

    @NotNull
    public abstract InspectionGroup getInspectionGroup();

    @NotNull
    public abstract String getInspectionId();

    @Nls
    @NotNull
    @Override
    public abstract String getDisplayName();

    @NotNull
    public abstract List<ProblemDescriptor> inspectFile(@NotNull PsiFile file, @NotNull InspectionManager manager, boolean isOntheFly);

    @NotNull
    @Override
    public String getShortName() {
        return getInspectionGroup().getPrefix() + getInspectionId();
    }

    @Nls
    @NotNull
    @Override
    public String getGroupDisplayName() {
        return getInspectionGroup().getDisplayName();
    }

    @Nullable
    @Override
    public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull InspectionManager manager, boolean isOnTheFly) {
        if (!(file instanceof LatexFile)) {
            return null;
        }

        List<ProblemDescriptor> descriptors = inspectFile(file, manager, isOnTheFly);
        return descriptors.toArray(new ProblemDescriptor[descriptors.size()]);
    }
}
