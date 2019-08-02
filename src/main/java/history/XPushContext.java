package history;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class XPushContext extends AnAction {
  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    DataContext context = e.getDataContext();
    Project project = e.getProject();
    Editor editor = CommonDataKeys.EDITOR.getData(context);
    VirtualFile file = CommonDataKeys.VIRTUAL_FILE.getData(context);
    if (editor == null || file == null || project == null) {
      return;
    }
    // handle the case when there are 2 splits: remember both?
    int offset = editor.getCaretModel().getPrimaryCaret().getOffset();
    XManager.getInstance(project).pushContext(new XContext(file, offset));
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    DataContext context = e.getDataContext();
    Project project = e.getProject();
    Editor editor = CommonDataKeys.EDITOR.getData(context);
    VirtualFile file = CommonDataKeys.VIRTUAL_FILE.getData(context);
    e.getPresentation().setEnabled(editor != null && file != null && project != null);
  }
}
