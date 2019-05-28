package history;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.fileEditor.impl.EditorWindow;
import com.intellij.openapi.project.DumbAware;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;

public class XSplit extends AnAction implements DumbAware {
  private final AnAction myDelegate;

  public XSplit(@NotNull AnAction delegate) {
    myDelegate = delegate;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    FileEditorManagerEx editorManager = FileEditorManagerEx.getInstanceEx(e.getProject());
    EditorWindow currentWindow = editorManager.getCurrentWindow();
    if (currentWindow == null) {
      return;
    }
    EditorWindow[] windowsBefore = editorManager.getWindows();
    myDelegate.actionPerformed(e);
    EditorWindow[] windowsAfter = editorManager.getWindows();
    EditorWindow created = null;
    for (EditorWindow window : windowsAfter) {
      if (ArrayUtil.find(windowsBefore, window) < 0) {
        created = window;
        break;
      }
    }
    if (created != null) {
      XManager.getInstance(e.getProject()).copyHistory(currentWindow, created);
    }
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    myDelegate.update(e);
  }
}
