package history

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class XProjectActivity: ProjectActivity {
    override suspend fun execute(project: Project) {
        XManager.getInstance(project)
    }
}