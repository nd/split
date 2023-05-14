package history;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.fileEditor.impl.EditorWindow;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class XOpenPreviousFile extends AnAction implements DumbAware {
  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    Project project = e.getProject();
    Editor editor = project != null ? FileEditorManagerEx.getInstanceEx(project).getSelectedTextEditor() : null;
    EditorWindow currentWindow = project != null ? FileEditorManagerEx.getInstanceEx(project).getCurrentWindow() : null;
    if (editor != null && currentWindow != null) {
      XWindowHistory history = XManager.getInstance(project).getHistory(currentWindow);
      if (history != null) {
        if (!history.gotoToPrevFile()) {
          FileEditorManager.getInstance(project).closeFile(((EditorImpl) editor).getVirtualFile());
        }
      }
    }
  }

  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }
}
