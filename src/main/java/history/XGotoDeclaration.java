package history;

import com.intellij.codeInsight.navigation.actions.GotoDeclarationAction;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class XGotoDeclaration extends AnAction implements DumbAware {
  private final AnAction myDelegate = ActionManager.getInstance().getAction(IdeActions.ACTION_GOTO_DECLARATION);

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    Project project = e.getProject();
    Editor editor = project != null ? FileEditorManagerEx.getInstanceEx(project).getSelectedTextEditor() : null;
    if (editor == null) {
      return;
    }
    PsiElement[] elements = GotoDeclarationAction.findAllTargetElements(project, editor, editor.getCaretModel().getOffset());
    setUseCurrentWindow(elements, true);
    try {
      myDelegate.actionPerformed(e);
    } finally {
      setUseCurrentWindow(elements, null);
    }
  }

  private void setUseCurrentWindow(PsiElement[] targetElements, @Nullable Boolean value) {
    for (PsiElement element : targetElements) {
      element.putUserData(FileEditorManager.USE_CURRENT_WINDOW, value);
    }
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    myDelegate.update(e);
  }
}
