package history;

import com.intellij.openapi.actionSystem.ActionManager;

public class XSplitV extends XSplit {
  public XSplitV() {
    super(ActionManager.getInstance().getAction("SplitVertically"));
  }
}
