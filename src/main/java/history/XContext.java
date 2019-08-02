package history;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class XContext {
  final VirtualFile myLeftFile;
  final int myLeftOffset;
  final VirtualFile myRightFile;
  final int myRightOffset;

  XContext(@NotNull VirtualFile path, int offset) {
    this(path, offset, null, 0);
  }

  XContext(@NotNull VirtualFile leftPath, int leftOffset, @Nullable VirtualFile rightPath, int rightOffset) {
    myLeftFile = leftPath;
    myLeftOffset = leftOffset;
    myRightFile = rightPath;
    myRightOffset = rightOffset;
  }
}
