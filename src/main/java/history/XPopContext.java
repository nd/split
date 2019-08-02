package history;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.fileEditor.impl.EditorWindow;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class XPopContext extends AnAction {
  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    Project project = e.getProject();
    if (project == null) {
      return;
    }
    XManager contextManager = XManager.getInstance(project);
    XContext ctx = contextManager.popContext();
    if (ctx != null) {
      FileEditorManagerEx manager = FileEditorManagerEx.getInstanceEx(project);
      CommandProcessor.getInstance().executeCommand(project, () -> {
        if (ctx.myRightFile == null) {
          if (ctx.myLeftFile.isValid()) {
            manager.unsplitAllWindow();
            new OpenFileDescriptor(project, ctx.myLeftFile, ctx.myLeftOffset).navigate(true);
          }
        } else {
          if (ctx.myLeftFile.isValid() && ctx.myRightFile.isValid()) {
            EditorWindow currentWindow = manager.getCurrentWindow();
            if (currentWindow != null) {
              manager.unsplitAllWindow();
              currentWindow.closeAllExcept(null);
              new OpenFileDescriptor(project, ctx.myLeftFile, ctx.myLeftOffset).navigate(true);
              currentWindow.split(SwingConstants.VERTICAL, true, null, true);
              new OpenFileDescriptor(project, ctx.myRightFile, ctx.myRightOffset).navigate(false);
            }
          }
        }
      }, "XPopContext", null);
    }
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    XContext context = XManager.getCurrentContext(e.getProject());
    e.getPresentation().setEnabled(context != null);
  }
}
