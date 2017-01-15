package test.de.hadizadeh.positioning.modeleditor;

import de.hadizadeh.positioning.modeleditor.EditorMap;
import de.hadizadeh.positioning.roommodel.model.MapSegment;
import junit.framework.TestCase;

public class EditorMapTest extends TestCase {
    private EditorMap editorMap;

    public void setUp() throws Exception {
        super.setUp();
        editorMap = new EditorMap(1, 1, 1, 1);
    }

    public void testResize() throws Exception {
        editorMap.resize();
    }

    public void testCopyMapSegment() throws Exception {
        MapSegment mapSegment = new MapSegment() {
            @Override
            public void render(Object o, int i, int i1, int i2, int i3) {

            }
        };
        MapSegment newMapSegment = editorMap.copyMapSegment(mapSegment);
        assertEquals(mapSegment.getContent(), newMapSegment.getContent());
        assertEquals(mapSegment.getMaterial(), newMapSegment.getMaterial());
    }
}