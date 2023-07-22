package history;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.fileEditor.impl.EditorWindow;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class XSaveCurrentPlace extends AnAction implements DumbAware {

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    Project project = e.getProject();
    FileEditorManagerEx manager = FileEditorManagerEx.getInstanceEx(project);
    XManager xmanager = XManager.getInstance(project);
    EditorWindow window = manager.getCurrentWindow();
    if (window == null) {
      return;
    }
    xmanager.addCurrentPlace(window);
  }
}
