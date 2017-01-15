package test.de.hadizadeh.positioning.modeleditor.controller;

import de.hadizadeh.positioning.modeleditor.EditorMap;
import de.hadizadeh.positioning.modeleditor.controller.RoomModelController;
import de.hadizadeh.positioning.roommodel.Map;
import junit.framework.TestCase;

import java.util.Locale;
import java.util.ResourceBundle;

public class RoomModelControllerTest extends TestCase {
    private RoomModelController roomModelController;

    public void setUp() throws Exception {
        super.setUp();
        roomModelController = new RoomModelController();
        roomModelController.load(ResourceBundle.getBundle("res.language.strings", new Locale("en", "EN")), null);
        roomModelController.createMap(new EditorMap(1 * Map.SEGMENTS_PER_METER, 1 * Map.SEGMENTS_PER_METER, 1, (int) 1 * Map.SEGMENTS_PER_METER), 1, 1,1, 1.0);
    }

    public void testGetMaterials() throws Exception {
        assertEquals(8, roomModelController.getMaterials().size());
    }

    public void testResizeMap() throws Exception {
        roomModelController.resizeMap(0.5);
    }

    public void testSelectEraser() throws Exception {
        roomModelController.selectEraser();
    }
}