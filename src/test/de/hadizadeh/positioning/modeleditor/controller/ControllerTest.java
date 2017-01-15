package test.de.hadizadeh.positioning.modeleditor.controller;

import de.hadizadeh.positioning.modeleditor.controller.Controller;
import junit.framework.TestCase;

import java.io.File;

class ControllerTest extends TestCase {

    private Controller controller;

    public void setUp() throws Exception {
        super.setUp();
        controller = new Controller();
        controller.setWorkingFile(new File("filename"));
    }

    void testOpen() {
        controller.open(new File(""));
    }

    void testSetWorkingFile() {
        controller.setWorkingFile(new File("newFilename"));
        assertEquals("newFilename", controller.getWorkingDirectory());
    }

    void testIsSaving() {
        assertFalse(controller.isSaving());
    }

    void testGetWorkingDirectory() {
        assertEquals("filename", controller.getWorkingDirectory());
    }
}
