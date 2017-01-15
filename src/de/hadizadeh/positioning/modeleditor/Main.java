package de.hadizadeh.positioning.modeleditor;

import de.hadizadeh.positioning.modeleditor.controller.Controller;
import de.hadizadeh.positioning.roommodel.FileManager;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Creates the main window
 */
public class Main extends Application {
    private static String parameterLanguage;
    private static File parameterFile;

    private static final int ONE_INSTANCE_CHECK_PORT = 9999;
    private static ServerSocket oneInstanceChecksocket;

    /**
     * Starts the program
     *
     * @param primaryStage starting stage
     * @throws Exception if the stage coul dnot be created
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        Locale locale = new Locale("en", "EN");
        if (!"en".equals(parameterLanguage)) {
            String systemLanguage = System.getProperty("user.language");
            if ("de".equals(parameterLanguage) || "de".equals(systemLanguage)) {
                locale = new Locale("de", "DE");
            }
        }
        final ResourceBundle resourceBundle = ResourceBundle.getBundle("res.language.strings", locale);
        if (!isAppRunning()) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/res/layout/main_layout.fxml"), resourceBundle);
            Parent root = loader.load();

            primaryStage.setTitle(resourceBundle.getString("mainAppName"));
            primaryStage.setMaximized(true);
            Scene scene = new Scene(root, 1100, 950);
            scene.getStylesheets().add("/res/style/style.css");
            primaryStage.setScene(scene);
            primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("/res/img/icon.png")));
            primaryStage.setMinWidth(1028);
            primaryStage.setMinHeight(720);
            final Controller controller = loader.<Controller>getController();

            scene.widthProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
                    controller.windowSizeChanged(scene.getWidth(), scene.getHeight());
                }
            });
            scene.heightProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
                    controller.windowSizeChanged(scene.getWidth(), scene.getHeight());
                }
            });

            scene.setOnDragOver(new EventHandler<DragEvent>() {
                @Override
                public void handle(DragEvent event) {
                    Dragboard db = event.getDragboard();
                    if (db.hasFiles()) {
                        event.acceptTransferModes(TransferMode.COPY);
                    } else {
                        event.consume();
                    }
                }
            });

            scene.setOnDragDropped(new EventHandler<DragEvent>() {
                @Override
                public void handle(DragEvent event) {
                    Dragboard db = event.getDragboard();
                    boolean success = false;
                    if (db.hasFiles()) {
                        success = true;
                        if (controller.getWorkingDirectory() == null || DialogTemplate.showYesNoDialog(resourceBundle.getString("mainCloseFileShort"), resourceBundle.getString("mainCloseFile"), resourceBundle.getString("mainCloseFileText"))) {
                            controller.open(db.getFiles().get(0));
                        }
                    }
                    event.setDropCompleted(success);
                    event.consume();
                }
            });

            primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                public void handle(WindowEvent we) {
                    if (!controller.isSaving()) {
                        we.consume();
                        if (DialogTemplate.showYesNoDialog(resourceBundle.getString("dialogConfirmation"), resourceBundle.getString("mainCloseApp"), resourceBundle.getString("mainCloseAppConfirmation"))) {
                            controller.saveSettings();
                            FileManager.getTmpName("");
                            System.exit(0);
                        }
                    } else {
                        DialogTemplate.showErrorDialog(resourceBundle.getString("mainSaveActiveTitle"), resourceBundle.getString("mainSaveActive"), resourceBundle.getString("mainSaveActiveText"));
                        we.consume();
                    }
                }
            });

            if (parameterFile != null) {
                controller.open(parameterFile);
            }
            primaryStage.show();
        } else {
            DialogTemplate.showErrorDialog(resourceBundle.getString("mainAppRunningShort"), resourceBundle.getString("mainAppRunning"), resourceBundle.getString("mainAppRunningText"));
            System.exit(1);
        }
    }

    private static boolean isAppRunning() {
        try {
            oneInstanceChecksocket = new ServerSocket(ONE_INSTANCE_CHECK_PORT, 0, InetAddress.getByAddress(new byte[]{127, 0, 0, 1}));
        } catch (BindException e) {
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            ;
        }
        return false;
    }

    /**
     * Starting method of the program
     *
     * @param args command line parameter: parameter 0 for file to open directly, parameter 1 for language
     */
    public static void main(String[] args) {
        if (args.length == 1) {
            File file = new File(args[0]);
            if (file.exists()) {
                parameterFile = file;
            } else {
                parameterLanguage = args[0];
            }
        } else if (args.length == 2) {
            parameterLanguage = args[0];
            File file = new File(args[1]);
            if (file.exists()) {
                parameterFile = file;
            }
        }
        launch(args);
    }
}
