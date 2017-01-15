package test.de.hadizadeh.positioning.modeleditor.controller;

import de.hadizadeh.positioning.modeleditor.controller.EditorContentController;
import junit.framework.TestCase;

public class EditorContentControllerTest extends TestCase {

    private EditorContentController editorContentController;

    public void setUp() throws Exception {
        super.setUp();
        editorContentController = new EditorContentController();

        editorContentController.initialize(null, null);
    }

    public void testReselectContent() throws Exception {
        editorContentController.reselectContent();
    }
}
