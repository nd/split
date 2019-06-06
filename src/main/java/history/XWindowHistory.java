package history;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.fileEditor.impl.EditorWindow;
import com.intellij.openapi.fileEditor.impl.IdeDocumentHistoryImpl;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/*
 History per window split.

 History maintains a stack of places. Places are filled in `addPlace` via the listener registered
 in IdeDocumentHistoryImpl.

 MyIndex is the index of the place for the next back action or -1 if there is no back places.
 The stack of places only grows and never shrinks. When one navigates back we decrease the index,
 but don't remove elements after the index so it is possible to return forward to places one left.
 However when a new place is added all elements after the current index become invalid,
 it is not possible to reach them anymore. This is handled by myMaxIndex: we don't access elements
 located at positions greater than myMaxIndex.

 During the back action we check if we are opening the last place. If that's the case we add
 the current location to the stack of places to make it possible to reach it via forward action.
 */
class XWindowHistory {
  private final Project myProject;
  private final List<IdeDocumentHistoryImpl.PlaceInfo> myPlaces = new ArrayList<>();
  private int myIndex = -1;
  private int myMaxIndex = -1;
  private volatile boolean myNavigationInProgress;

  public XWindowHistory(@NotNull Project project) {
    myProject = project;
  }

  synchronized boolean addPlace(@NotNull IdeDocumentHistoryImpl.PlaceInfo place) {
    if (myNavigationInProgress) {
      return false;
    }
    if (myIndex >= 0 && sameLine(myPlaces.get(myIndex), place)) {
      return false;
    }
    myIndex++;
    if (myIndex < myPlaces.size()) {
      myPlaces.set(myIndex, place);
      myMaxIndex = myIndex;
    } else {
      myPlaces.add(place);
      myMaxIndex++;
    }
    return true;
  }

  private static boolean sameLine(@NotNull IdeDocumentHistoryImpl.PlaceInfo place1,
                                  @NotNull IdeDocumentHistoryImpl.PlaceInfo place2) {
    if (place1.getFile().getPath().equals(place2.getFile().getPath())) {
      RangeMarker pos1 = place1.getCaretPosition();
      RangeMarker pos2 = place2.getCaretPosition();
      if (pos1 != null && pos2 != null) {
        Document doc1 = pos1.getDocument();
        Document doc2 = pos2.getDocument();
        return doc1.getLineNumber(pos1.getStartOffset()) == doc2.getLineNumber(pos2.getStartOffset());
      }
    }
    return false;
  }

  private static boolean sameOffset(@NotNull IdeDocumentHistoryImpl.PlaceInfo place1,
                                    @NotNull IdeDocumentHistoryImpl.PlaceInfo place2) {
    if (place1.getFile().getPath().equals(place2.getFile().getPath())) {
      RangeMarker pos1 = place1.getCaretPosition();
      RangeMarker pos2 = place2.getCaretPosition();
      return pos1 != null && pos2 != null && pos1.getStartOffset() == pos2.getStartOffset();
    }
    return false;
  }

  synchronized void back() {
    if (canBack()) {
      IdeDocumentHistoryImpl.PlaceInfo currentPlace = XManager.getCurrentPlaceInfo(myProject);
      if (currentPlace == null) {
        return;
      }
      if (myIndex == myMaxIndex) {
        if (addPlace(currentPlace)) {
          myIndex--;
        }
      }
      IdeDocumentHistoryImpl.PlaceInfo place = null;
      do {
        place = myPlaces.get(myIndex--);
      } while (myIndex>= 0 && place != null && sameOffset(place, currentPlace));
      if (place != null) {
        gotoPlace(place);
      }
    }
  }

  synchronized void forward() {
    if (canForward()) {
      IdeDocumentHistoryImpl.PlaceInfo place = myPlaces.get(getForwardIndex());
      myIndex++;
      gotoPlace(place);
    }
  }

  private int getForwardIndex() {
    // Forward action is possible only after one or more back action was executed.
    // Before the first back action stack looks like this:
    //
    //   [p_0, p_1, ..., p_n-1, p_n], myIndex=n
    //
    // during back we add current location at p_n+1 position, retrieve place
    // and decrement myIndex:
    //
    //   [p_0, p_1, ..., p_n-1, p_n, p_n+1], myIndex=n-1, place p_n is opened in the editor
    //
    // To go forward to the place p_n+1 we have to use myIndex+2.
    return myIndex + 2;
  }

  private synchronized void gotoPlace(@NotNull IdeDocumentHistoryImpl.PlaceInfo place) {
    try {
      myNavigationInProgress = true;
      IdeDocumentHistoryImpl.getInstance(myProject).gotoPlaceInfo(place);
    } finally {
      myNavigationInProgress = false;
    }
  }

  synchronized boolean canBack() {
    return 0 <= myIndex && myIndex <= myMaxIndex;
  }

  synchronized boolean canForward() {
    int forwardIndex = getForwardIndex();
    return forwardIndex <= myMaxIndex && myPlaces.get(forwardIndex) != null;
  }

  @NotNull
  synchronized XWindowHistory copyForWindow(@NotNull EditorWindow window) {
    XWindowHistory copy = new XWindowHistory(myProject);
    for (IdeDocumentHistoryImpl.PlaceInfo place : myPlaces) {
      copy.addPlace(new IdeDocumentHistoryImpl.PlaceInfo(place.getFile(), place.getNavigationState(),
              place.getEditorTypeId(), window, place.getCaretPosition()));
    }
    return copy;
  }
}
