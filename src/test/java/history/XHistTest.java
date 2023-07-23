package history;

import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;


public class XHistTest {
  @Test
  public void setPlace() {
    XHist<String> h = new XHist<>();
    h.pushPlace("1");
    h.pushPlace("2");
    h.pushPlace("3");
    h.setNextPlace(1);
    assertPlace(h, "1", "2");
  }

  @Test
  public void joinOnPush() {
    XHist<String> h = new XHist<>() {
      @Override
      boolean shouldBeJoined(@Nullable String p1, @Nullable String p2) {
        return "1".equals(p1) && "1a".equals(p2);
      }
    };
    h.pushPlace("1");
    h.pushPlace("2");
    h.back();
    h.pushPlace("1a");
    assertPlace(h, "1a", "2");
    assertEquals(List.of("1a", "2"), h.getPlaces());
  }

  @Test
  public void insertPlace() {
    XHist<String> h = new XHist<>();
    h.insertPlace("1");
    assertPlace(h, "1", null);
    assertEquals(List.of("1"), h.getPlaces());

    h.insertPlace("2");
    assertPlace(h, "2", null);
    assertEquals(List.of("1", "2"), h.getPlaces());

    h.insertPlace("3");
    assertPlace(h, "3", null);
    assertEquals(List.of("1", "2", "3"), h.getPlaces());

    h.back();
    h.back();
    assertPlace(h, "1", "2");

    h.insertPlace("1");
    // nothing happens because it is the same as prev:
    assertPlace(h, "1", "2");
    assertEquals(List.of("1", "2", "3"), h.getPlaces());

    h.insertPlace("2");
    // nothing happens because it is the same as next:
    assertPlace(h, "1", "2");
    assertEquals(List.of("1", "2", "3"), h.getPlaces());

    h.insertPlace("4");
    assertPlace(h, "4", "2");
    assertEquals(List.of("1", "4", "2", "3"), h.getPlaces());
  }

  @Test
  public void radials() {
    XHist<String> h = new XHist<>();
    h.pushPlace("1");
    assertPlace(h, "1", null);

    // move to 2
    h.backFromPlace("2");
    assertPlace(h, "1", "2");
    assertEquals(List.of("1", "2"), h.getPlaces());

    // move to 3
    h.backFromPlace("3");
    assertPlace(h, "1", "3");
    assertEquals(List.of("1", "3", "2"), h.getPlaces());

    // move to 4
    h.backFromPlace("4");
    assertPlace(h, "1", "4");

    assertEquals(List.of("1", "4", "3", "2"), h.getPlaces());

    h.forward();
    h.back();
    assertEquals(List.of("1", "4", "3", "2"), h.getPlaces());
  }

  @Test
  public void forwardFromPlace() {
    XHist<String> h = new XHist<>();
    h.pushPlace("1");
    h.pushPlace("2");
    h.back();
    h.back();
    assertPlace(h, null, "1");

    h.forwardFromPlace("1");
    assertPlace(h, "2", null);

    h.back();
    h.back();
    assertPlace(h, null, "1");

    h.forwardFromPlace("3");
    assertPlace(h, "1", "2");
    assertEquals(List.of("1", "2"), h.getPlaces());
  }

  @Test
  public void backFromPlace() {
    XHist<String> h = new XHist<>();
    h.pushPlace("1");
    h.pushPlace("2");
    h.back();
    assertPlace(h, "1", "2");

    h.backFromPlace("3");
    assertPlace(h, "1", "3");
    assertEquals(List.of("1", "3", "2"), h.getPlaces());
  }

  @Test
  public void pushPlaceMiddle() {
    XHist<String> h = new XHist<>();
    h.pushPlace("1");
    h.pushPlace("2");
    h.pushPlace("3");
    h.back();
    h.back();
    assertPlace(h, "1", "2");

    h.pushPlace("4");
    assertPlace(h, "4", null);
    assertEquals(List.of("1", "4"), h.getPlaces());

    h.back();
    h.back();
    assertPlace(h, null, "1");
    h.pushPlace("5");
    assertPlace(h, "5", null);
    assertEquals(List.of("5"), h.getPlaces());
  }

  @Test
  public void pushPlace() {
    XHist<String> h = new XHist<>();
    assertPlace(h, null, null);
    h.pushPlace("1");
    assertPlace(h, "1", null);
    assertTrue(h.canBackward());
    assertFalse(h.canForward());
    h.pushPlace("2");
    h.pushPlace("3");
    assertEquals(List.of("1", "2", "3"), h.getPlaces());
    assertPlace(h, "3", null);

    h.pushPlace("3");
    // ^no op since 3 is the same as the last place
    assertEquals(List.of("1", "2", "3"), h.getPlaces());
    assertPlace(h, "3", null);

    h.back();
    assertPlace(h, "2", "3");
    assertTrue(h.canForward());
    assertTrue(h.canBackward());

    h.back();
    assertPlace(h, "1", "2");
    assertTrue(h.canForward());
    assertTrue(h.canBackward());

    h.back();
    assertPlace(h, null, "1");
    assertFalse(h.back());
    assertPlace(h, null, "1");
    assertTrue(h.canForward());
    assertFalse(h.canBackward());

    h.forward();
    assertPlace(h, "1", "2");
    assertTrue(h.canBackward());

    h.forward();
    assertPlace(h, "2", "3");

    h.forward();
    assertPlace(h, "3", null);

    assertFalse(h.forward());
  }

  @Test
  public void testEmpty() {
    XHist<String> h = new XHist<>();
    assertFalse(h.canForward());
    assertFalse(h.canBackward());
  }

  private void assertPlace(XHist<String> h, String p1, String p2) {
    Pair<String, String> currentPlace = h.getPosition();
    assertEquals(Pair.create(p1, p2), currentPlace);
  }
}
