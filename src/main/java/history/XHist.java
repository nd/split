package history;

import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class XHist<T> {
  private final ArrayList<T> myPlaces = new ArrayList<>();
  // we are always between myPlaces[myNextIdx-1] and myPlaces[myNextIdx]
  private int myNextIdx = 0; // always >=0

  boolean canForward() {
    return myNextIdx < myPlaces.size();
  }

  boolean canBackward() {
    return myNextIdx > 0;
  }

  void pushPlace(@NotNull T t) {
    T prevPlace = getPrevPlace();
    if (areEqual(prevPlace, t)) {
      return;
    }
    if (myNextIdx - 1 >= 0 && shouldBeJoined(prevPlace, t)) {
      myPlaces.set(myNextIdx - 1, t);
      return;
    }
    if (areEqual(getNextPlace(), t)) {
      return;
    }
    while (myNextIdx < myPlaces.size()) {
      myPlaces.remove(myPlaces.size() - 1);
    }
    myPlaces.add(t);
    myNextIdx++;
  }

  void insertPlace(@NotNull T t) {
    if (areEqual(getPrevPlace(), t)) {
      return;
    }
    if (areEqual(getNextPlace(), t)) {
      return;
    }
    myPlaces.add(myNextIdx, t);
    myNextIdx++;
  }

  void setNextPlace(int nextIndex) {
    if (nextIndex < myPlaces.size()) {
      myNextIdx = nextIndex;
    }
  }

  boolean back() {
    return backFromPlace(getPrevPlace());
  }

  boolean backFromPlace(@Nullable T fromPlace) {
    if (myNextIdx > 0) {
      // position:
      // [..., prev-1, prev,  next, ...]
      //                    ^
      // If we are at prev=next-1, then move to prev-1.
      // Otherwise, we reached prev and moved somewhere.
      // Add the current position and don't change index,
      // so that getPrev place still returns prev:
      // [..., prev-1, prev,  current, next, ...]
      //                    ^
      T backPlace = myPlaces.get(myNextIdx - 1);
      if (areEqual(backPlace, fromPlace)) {
        myNextIdx--;
      } else {
        myPlaces.add(myNextIdx, fromPlace);
      }
      return true;
    }
    return false;
  }

  boolean forward() {
    return forwardFromPlace(getPrevPlace());
  }

  boolean forwardFromPlace(@Nullable T fromPlace) {
    if (myNextIdx < myPlaces.size()) {
      int nextIdx = myNextIdx;
      while (nextIdx < myPlaces.size()) {
        T nextPlace = myPlaces.get(nextIdx);
        if (!areEqual(fromPlace, nextPlace)) {
          myNextIdx = nextIdx + 1;
          return true;
        } else {
          nextIdx++;
        }
      }
      return false;
    }
    return false;
  }

  boolean areEqual(@Nullable T p1, @Nullable T p2) {
    return Objects.equals(p1, p2);
  }

  boolean shouldBeJoined(@Nullable T p1, @Nullable T p2) {
    return Objects.equals(p1, p2);
  }

  @Nullable T getNextPlace() {
    return myNextIdx < myPlaces.size() ? myPlaces.get(myNextIdx) : null;
  }

  int getNextIdx() {
    return myNextIdx;
  }

  @Nullable T getPrevPlace() {
    return myNextIdx > 0 ? myPlaces.get(myNextIdx - 1) : null;
  }

  @NotNull Pair<T, T> getPosition() {
    return Pair.create(getPrevPlace(), getNextPlace());
  }

  @NotNull List<T> getPlaces() {
    return myPlaces;
  }
}