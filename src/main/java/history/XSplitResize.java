package history;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.fileEditor.impl.EditorTabbedContainer;
import com.intellij.openapi.fileEditor.impl.EditorWindow;
import com.intellij.openapi.fileEditor.impl.EditorsSplitters;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Splitter;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayDeque;
import java.util.Deque;

public class XSplitResize extends AnAction {
  private final float myDelta;

  public XSplitResize(boolean increase) {
    myDelta = increase ? 0.1f : -0.1f;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    Project project = e.getProject();
    if (project == null) {
      return;
    }
    EditorWindow currentWindow = FileEditorManagerEx.getInstanceEx(project).getCurrentWindow();
    EditorTabbedContainer tabbedPane = currentWindow.getTabbedPane();
    JComponent currentWindowComponent = tabbedPane != null ? tabbedPane.getComponent() : null;
    if (currentWindowComponent == null) {
      return;
    }
    Splitter splitter = findSplitter(currentWindow.getOwner(), currentWindowComponent);
    if (splitter != null) {
      float delta = splitter.getFirstComponent().getComponent(0) == currentWindowComponent ? myDelta : - myDelta;
      float proportion = splitter.getProportion();
      float newProportion = Math.min(proportion + delta, 1.0f);
      if (0 < newProportion && newProportion < 1) {
        splitter.setProportion(newProportion);
      }
    }
  }

  @Nullable
  private static Splitter findSplitter(EditorsSplitters splitters, JComponent currentWindowComponent) {
    Deque<Component> queue = new ArrayDeque<>();
    ContainerUtil.addAll(queue, splitters.getComponents());
    while (!queue.isEmpty()) {
      Component c = queue.removeFirst();
      Splitter splitter = ObjectUtils.tryCast(c, Splitter.class);
      if (splitter != null) {
        if (splitter.getFirstComponent().getComponent(0) == currentWindowComponent || splitter.getSecondComponent().getComponent(0) == currentWindowComponent) {
          return splitter;
        }
      }
      JComponent jc = ObjectUtils.tryCast(c, JComponent.class);
      if (jc != null) {
        for (Component component : jc.getComponents()) {
          queue.addLast(component);
        }
      }
    }
    return null;
  }

  @Override
  public boolean isDumbAware() {
    return true;
  }
}
