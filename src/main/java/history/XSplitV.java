package history;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import org.jetbrains.annotations.NotNull;

public class XSplitV extends XSplit {
  public XSplitV() {
    super(ActionManager.getInstance().getAction("SplitVertically"));
  }
}
