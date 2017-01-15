package de.hadizadeh.positioning.modeleditor;

import de.hadizadeh.positioning.roommodel.FileManager;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Optional;

/**
 * Delivers all needed dialog types
 */
public class DialogTemplate {
    private static Image icon;
    private static File initialDirectory;

    /**
     * Shows a yes-no dialog
     *
     * @param title  title of the dialog
     * @param header header text of the dialog
     * @param text   question text of the dialog
     * @return true, if the user excepts, else he does not except
     */
    public static boolean showYesNoDialog(String title, String header, String text) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(getIcon());
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(text);
        Optional<ButtonType> result = alert.showAndWait();
        return (result.get() == ButtonType.OK);
    }

    /**
     * Shows an error dialog
     *
     * @param title  title
     * @param header header text
     * @param text   description text
     */
    public static void showErrorDialog(String title, String header, String text) {
        showSingleButtonDialog(title, header, text, Alert.AlertType.ERROR);
    }

    /**
     * Shows an info dialog
     *
     * @param title  title
     * @param header header text
     * @param text   description text
     */
    public static void showInfoDialog(String title, String header, String text) {
        showSingleButtonDialog(title, header, text, Alert.AlertType.INFORMATION);
    }

    private static void showSingleButtonDialog(String title, String header, String text, Alert.AlertType type) {
        Alert alert = new Alert(type);
        ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(getIcon());
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(text);
        alert.showAndWait();
    }

    /**
     * Shows a dialog for selecting a media file
     *
     * @param title title
     * @param text  description text
     * @param stage stage, where the dialog should be connected to
     * @return selected file
     */
    public static File showSelectMediaDialog(String title, String text, Stage stage) {
        return showFileChooseDialog(false, title, new FileChooser.ExtensionFilter(text, "*.jpg", "*.png", "*.mp3", "*.mp4"), stage);
    }

    /**
     * Shows a dialog for opening a file
     *
     * @param title title
     * @param stage stage, where the dialog should be connected to
     * @return selected file
     */
    public static File showFileOpenDialog(String title, Stage stage) {
        return showFileChooseDialog(false, title, new FileChooser.ExtensionFilter(FileManager.MODEL_EDITOR_FILE_EXTENSION, "*." + FileManager.MODEL_EDITOR_FILE_EXTENSION), stage);
    }

    /**
     * Shows a dialog for saving a file
     *
     * @param title title
     * @param stage stage, where the dialog should be connected to
     * @return saved file path
     */
    public static File showFileSaveDialog(String title, Stage stage) {
        return showFileChooseDialog(true, title, new FileChooser.ExtensionFilter(FileManager.MODEL_EDITOR_FILE_EXTENSION, "*." + FileManager.MODEL_EDITOR_FILE_EXTENSION), stage);
    }

    private static File showFileChooseDialog(boolean save, String title, FileChooser.ExtensionFilter extensionFilter, Stage stage) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.getExtensionFilters().addAll(extensionFilter);
        if (initialDirectory != null) {
            chooser.setInitialDirectory(initialDirectory);
        }
        File selectedDir = null;
        if (save) {
            selectedDir = chooser.showSaveDialog(stage);
        } else {
            selectedDir = chooser.showOpenDialog(stage);
        }
        if (selectedDir != null) {
            initialDirectory = selectedDir.getParentFile();
            return selectedDir;
        }
        return null;
    }

    /**
     * Shows a language chooser dialog
     *
     * @param title  title
     * @param header header text
     * @param text   description text
     * @return Selected language
     */
    public static String showLanguageDialog(String title, String header, String text) {
        TextInputDialog dialog = new TextInputDialog("");
        ((Stage) dialog.getDialogPane().getScene().getWindow()).getIcons().add(getIcon());
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(text);
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String language = result.get();
            if (!"".equals(language) && language.matches("[a-zA-Z]+") && language.length() <= 20) {
                return language.toLowerCase();
            }
        } else {
            return "";
        }
        return null;
    }


    private static Image getIcon() {
        if (icon == null) {
            icon = new Image(Main.class.getResourceAsStream("/res/img/icon.png"));
        }
        return icon;
    }

    /**
     * Returns the initial directory for file choosers
     *
     * @return initial directory
     */
    public static File getInitialDirectory() {
        return initialDirectory;
    }

    /**
     * Sets the initial directory for file choosers
     *
     * @param initialDirectory initial directory
     */
    public static void setInitialDirectory(File initialDirectory) {
        if (initialDirectory != null && initialDirectory.isDirectory()) {
            DialogTemplate.initialDirectory = initialDirectory;
        }
    }
}
