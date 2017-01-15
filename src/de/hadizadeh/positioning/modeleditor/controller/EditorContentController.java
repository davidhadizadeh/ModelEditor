package de.hadizadeh.positioning.modeleditor.controller;


import de.hadizadeh.positioning.content.exceptions.ContentPersistenceException;
import de.hadizadeh.positioning.modeleditor.ContentListVisualiser;
import de.hadizadeh.positioning.modeleditor.DialogTemplate;
import de.hadizadeh.positioning.roommodel.ContentController;
import de.hadizadeh.positioning.roommodel.FileManager;
import de.hadizadeh.positioning.roommodel.model.ContentElement;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the contents
 */
public class EditorContentController extends ContentController implements Initializable {
    private static final String CONTENT_ICON_PATH = "/res/img/contents/";
    @FXML
    ListView contents_lv;
    @FXML
    TextField title_et;
    @FXML
    TextField description_et;
    @FXML
    TextField url_et;
    @FXML
    Label play_video_lb;
    @FXML
    TextArea fulltext_et;
    @FXML
    ImageView image_iv;
    @FXML
    ImageView audio_play_iv;
    @FXML
    Slider audio_slider_sb;
    @FXML
    Label audio_player_minutes_played;
    @FXML
    Label audio_player_minutes_total;
    @FXML
    Pane player_group;
    @FXML
    ComboBox language_cb;
    @FXML
    Pane create_content_btns_hb;
    @FXML
    Pane main_pane_left;
    @FXML
    RowConstraints createDiscardRc;
    @FXML
    ImageView removeImageIv;
    @FXML
    ImageView removeAudioIv;
    @FXML
    ImageView removeVideoIv;
    @FXML
    ImageView copyLanguageIv;
    @FXML
    ImageView removeLanguageIv;
    @FXML
    ImageView createContentIv;
    @FXML
    ImageView removeContentIv;
    @FXML
    ImageView selectMediaFileIv;
    @FXML
    Button discard_content_btn;

    private RoomModelController roomModelController;
    private MediaPlayer mediaPlayer;
    private boolean playing;
    private File mediaFileChooser;
    private File audioTmpFile;
    private boolean dataLoading = false;
    private Image contentIconText;
    private Image contentIconImage;
    private Image contentIconAudio;
    private Image contentIconVideo;
    private Label statusLabel;
    private ResourceBundle resourceBundle;
    private boolean noContentCreated;
    private Image createImage;
    private Image createImageDisabled;
    private Image removeImage;
    private Image removeImageDisabled;
    private Image copyImage;
    private Image copyImageDisabled;
    private boolean creatingContent;

    /**
     * Initializes the controller and loads needed resources
     *
     * @param url            url (not needed)
     * @param resourceBundle resource bundle for data
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        contentIconText = Controller.loadImage(CONTENT_ICON_PATH + "text.png");
        contentIconImage = Controller.loadImage(CONTENT_ICON_PATH + "image.png");
        contentIconAudio = Controller.loadImage(CONTENT_ICON_PATH + "audio.png");
        contentIconVideo = Controller.loadImage(CONTENT_ICON_PATH + "video.png");

        createImage = new Image(Controller.class.getResourceAsStream("/res/img/create.png"));
        createImageDisabled = new Image(Controller.class.getResourceAsStream("/res/img/create_disabled.png"));
        removeImage = new Image(Controller.class.getResourceAsStream("/res/img/delete.png"));
        removeImageDisabled = new Image(Controller.class.getResourceAsStream("/res/img/delete_disabled.png"));
        copyImage = new Image(Controller.class.getResourceAsStream("/res/img/copy.png"));
        copyImageDisabled = new Image(Controller.class.getResourceAsStream("/res/img/copy_disabled.png"));
    }

    /**
     * Sets the references of the resources
     *
     * @param resourceBundle resource bundle
     * @param statusLabel    status label
     */
    public void load(ResourceBundle resourceBundle, Label statusLabel) {
        this.resourceBundle = resourceBundle;
        this.statusLabel = statusLabel;
    }

    /**
     * Sets the room model controller reference
     *
     * @param roomModelController room model controller reference
     */
    public void setRoomModelController(RoomModelController roomModelController) {
        this.roomModelController = roomModelController;
    }

    /**
     * Opens the content files
     *
     * @throws ContentPersistenceException if the content could not be loaded
     */
    public void open() throws ContentPersistenceException {
        dataLoading = true;
        currentLanguage = defaultLanguage;
        clearForm();
        refreshLanguages();

        language_cb.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String t, String t1) {
                if (t1 != null) {
                    currentLanguage = t1;
                    int lastSelectedContentIndex = currentContentIndex;
                    refreshContentsLv();
                    currentContentIndex = lastSelectedContentIndex;
                    contents_lv.getSelectionModel().select(currentContentIndex);
                }
            }
        });

        language_cb.getSelectionModel().select(0);

        if (contentElements.size() > 0) {
            noContentCreated = false;
        } else {
            noContentCreated = true;
        }


        refreshContentsLv();

        contents_lv.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observableValue, Object o, Object t1) {
                int selectedIndex = contents_lv.getSelectionModel().getSelectedIndex();
                if (selectedIndex >= 0) {
                    currentContentIndex = selectedIndex;
                    loadContentData(currentContentIndex);
                }
            }
        });

        audio_slider_sb.valueProperty().addListener(new InvalidationListener() {
            public void invalidated(Observable ov) {
                if (audio_slider_sb.isValueChanging()) {
                    mediaPlayer.seek(Duration.seconds(audio_slider_sb.getValue()));
                }
            }
        });

        audio_slider_sb.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                if (Math.abs(old_val.intValue() - new_val.intValue()) > 2) {
                    mediaPlayer.seek(Duration.seconds(new_val.intValue()));
                }
            }
        });


        title_et.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                                String oldValue, String newValue) {
                if (!dataLoading) {
                    contentElements.get(currentLanguage).get(currentContentIndex).setTitle(newValue.trim());
                    if (!creatingContent) {
                        refreshContentsLv();
                    }
                }
            }
        });

        title_et.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> arg0, Boolean lastFocus, Boolean newFocus) {
                if (!creatingContent && lastFocus && !newFocus && !checkContentTitle()) {
                    title_et.requestFocus();
                    title_et.selectAll();
                }
            }
        });

        description_et.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                                String oldValue, String newValue) {
                if (!dataLoading) {
                    contentElements.get(currentLanguage).get(currentContentIndex).setDescription(newValue.trim());
                }
            }
        });

        url_et.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                                String oldValue, String newValue) {
                if (!dataLoading) {
                    contentElements.get(currentLanguage).get(currentContentIndex).setUrl(newValue.trim());
                }
            }
        });

        url_et.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> arg0, Boolean lastFocus, Boolean newFocus) {
                if (!newFocus && lastFocus && !"".equals(url_et.getText())) {
                    try {
                        new URL(url_et.getText());
                    } catch (MalformedURLException malformedURLException) {
                        url_et.setText("");
                        DialogTemplate.showErrorDialog(resourceBundle.getString("contentUrlErrorShort"), resourceBundle.getString("contentUrlError"), resourceBundle.getString("contentUrlErrorText"));
                        url_et.requestFocus();
                    }
                }
            }
        });

        fulltext_et.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                                String oldValue, String newValue) {
                if (!dataLoading) {
                    contentElements.get(currentLanguage).get(currentContentIndex).setFullText(newValue);
                }
            }
        });

        if (contentElements.size() > 0) {
            creatingContent = false;
            setContentFieldsFocus(false);
            contents_lv.getSelectionModel().selectFirst();
        } else {
            actionCreateNewContent();
        }
        audioPause();
        dataLoading = false;

        Tooltip.install(copyLanguageIv, new Tooltip(resourceBundle.getString("contentCopyLanguage")));
        Tooltip.install(removeLanguageIv, new Tooltip(resourceBundle.getString("contentRemoveLanguage")));
        Tooltip.install(createContentIv, new Tooltip(resourceBundle.getString("contentStatusCreateContent")));
        Tooltip.install(removeContentIv, new Tooltip(resourceBundle.getString("contentStatusRemoveContent")));
        Tooltip.install(selectMediaFileIv, new Tooltip(resourceBundle.getString("contentSelectMediaFile")));
        Tooltip.install(removeImageIv, new Tooltip(resourceBundle.getString("contentStatusRemoveImage")));
        Tooltip.install(removeVideoIv, new Tooltip(resourceBundle.getString("contentStatusRemoveAudio")));
        Tooltip.install(removeAudioIv, new Tooltip(resourceBundle.getString("contentStatusRemoveVideo")));
        refreshIconButtons();
    }

    /**
     * Selects the content in the dropdown
     */
    public void reselectContent() {
        if (contents_lv != null) {
            contents_lv.getSelectionModel().select(currentContentIndex);
        }
    }

    private void refreshLanguages() {
        language_cb.getItems().clear();
        for (String language : languages) {
            language_cb.getItems().add(language);
        }
        refreshContentsLv();
    }

    /**
     * Reloads the contents list
     */
    public void refreshContentsLv() {
        if (contentElements != null && !noContentCreated) {
            contents_lv.getItems().clear();
            contents_lv.setCellFactory(ContentListVisualiser.getContentListCallback());
            List<ContentListVisualiser.ContentListItem> loadedContents = new ArrayList<>();
            if (contentElements.get(currentLanguage) != null) {
                for (ContentElement contentElement : contentElements.get(currentLanguage)) {
                    Image image = null;
                    if (contentElement.getVideoFile() != null && contentElement.getVideoFile().exists()) {
                        image = contentIconVideo;
                    } else if (contentElement.getAudioFile() != null && contentElement.getAudioFile().exists()) {
                        image = contentIconAudio;
                    } else if (contentElement.getImageFile() != null && contentElement.getImageFile().exists()) {
                        image = contentIconImage;
                    } else {
                        image = contentIconText;
                    }
                    ContentListVisualiser.ContentListItem contentListItem = new ContentListVisualiser.ContentListItem(contentElement.getTitle(), image);
                    contents_lv.getItems().add(contentListItem);
                    loadedContents.add(contentListItem);
                }
            }
            roomModelController.refreshContentBox(loadedContents);
        }
    }

    private void loadContentData(int selectedIndex) {
        boolean dataLoadingBefore = dataLoading;
        dataLoading = true;
        audioPause();
        audio_slider_sb.setValue(0);
        clearForm();
        ContentElement contentElement = contentElements.get(currentLanguage).get(selectedIndex);
        title_et.setText(contentElement.getTitle());
        description_et.setText(contentElement.getDescription());
        url_et.setText(contentElement.getUrl());

        setAudioFile(contentElement.getAudioFile());

        File videoFile = contentElement.getVideoFile();
        contentElements.get(currentLanguage).get(selectedIndex).setVideoFile(videoFile);
        if (videoFile != null) {
            boolean videoExists = videoFile.exists();
            play_video_lb.setVisible(videoExists);
            removeVideoIv.setVisible(videoExists);
        }

        if (contentElement.getImageFile() != null && contentElement.getImageFile().exists()) {
            try {
                FileInputStream fis = new FileInputStream(contentElement.getImageFile().getAbsolutePath());
                image_iv.setImage(new Image(fis));
                fis.close();
                image_iv.setVisible(true);
                removeImageIv.setVisible(true);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            removeImageIv.setVisible(false);
            image_iv.setVisible(false);
        }

        fulltext_et.setText(contentElement.getFullText());
        dataLoading = dataLoadingBefore;
    }

    private void clearForm() {
        boolean dataLoadingBefore = dataLoading;
        dataLoading = true;
        play_video_lb.setVisible(false);
        title_et.setText("");
        description_et.setText("");
        url_et.setText("");
        fulltext_et.setText("");
        image_iv.setImage(null);
        image_iv.setVisible(false);
        player_group.setVisible(false);
        dataLoading = dataLoadingBefore;
        removeImageIv.setVisible(false);
        removeAudioIv.setVisible(false);
        removeVideoIv.setVisible(false);
    }

    /**
     * Chooses a new media file and adds it to the list
     */
    @FXML
    protected void actionSelectMediaFile() {
        File selectedFile = DialogTemplate.showSelectMediaDialog(resourceBundle.getString("contentSelectMediaFile"), resourceBundle.getString("contentFileChooserMediaFile"), (Stage) main_pane_left.getScene().getWindow());
        if (selectedFile != null) {
            String extension = "";
            int i = selectedFile.getName().lastIndexOf('.');
            if (i >= 0) {
                extension = selectedFile.getName().toLowerCase().substring(i + 1);
            }

            if ("jpg".equals(extension) || "png".equals(extension)) {
                contentElements.get(currentLanguage).get(currentContentIndex).setImageUpdated(true);
                contentElements.get(currentLanguage).get(currentContentIndex).setImageFile(selectedFile);
                try {
                    FileInputStream fis = new FileInputStream(selectedFile.getAbsolutePath());
                    image_iv.setImage(new Image(fis));
                    fis.close();
                    image_iv.setVisible(true);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                removeImageIv.setVisible(true);
            } else if ("mp3".equals(extension)) {
                contentElements.get(currentLanguage).get(currentContentIndex).setAudioUpdated(true);
                contentElements.get(currentLanguage).get(currentContentIndex).setAudioFile(selectedFile);
                audio_slider_sb.setValue(0);
                player_group.setVisible(true);
                setAudioFile(selectedFile);

            } else if ("mp4".equals(extension)) {
                contentElements.get(currentLanguage).get(currentContentIndex).setVideoUpdated(true);
                contentElements.get(currentLanguage).get(currentContentIndex).setVideoFile(selectedFile);
                play_video_lb.setVisible(true);
                removeVideoIv.setVisible(true);
            }
            mediaFileChooser = selectedFile;
            refreshContentsLv();
        }
    }

    /**
     * Shows an image in an external program
     */
    @FXML
    protected void actionShowImage() {
        try {
            Desktop.getDesktop().open(contentElements.get(currentLanguage).get(currentContentIndex).getImageFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Shows a video in an external program
     */
    @FXML
    protected void actionPlayVideo() {
        try {
            Desktop.getDesktop().open(contentElements.get(currentLanguage).get(currentContentIndex).getVideoFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Removes a content
     */
    @FXML
    protected void actionRemoveContent() {
        if (DialogTemplate.showYesNoDialog(resourceBundle.getString("dialogConfirmation"), resourceBundle.getString("contentDeleteContent"), resourceBundle.getString("contentDeleteContentQuestion"))) {
            audioPause();
            int index = currentContentIndex;
            for (String language : languages) {
                contentElements.get(language).get(currentContentIndex).setTitle(null);
                contentElements.get(language).remove(currentContentIndex);
            }
            refreshContentsLv();
            if (index < contentElements.get(currentLanguage).size()) {
                contents_lv.getSelectionModel().select(index);
            } else {
                contents_lv.getSelectionModel().select(index - 1);
            }
            if (contentElements.get(currentLanguage).size() == 0) {
                noContentCreated = true;
                actionCreateNewContent();
            }
            refreshIconButtons();
        }
    }

    private void setContentFieldsFocus(boolean contentFocused) {
        //main_pane_left.setVisible(!contentFocused);
        create_content_btns_hb.setVisible(contentFocused);
        if (contentFocused) {
            createDiscardRc.setPrefHeight(100);
        } else {
            createDiscardRc.setPrefHeight(0);
        }
    }

    /**
     * Creates a new content
     */
    @FXML
    protected void actionCreateNewContent() {
        clearForm();
        setContentFieldsFocus(true);
        creatingContent = true;
        ContentElement contentElement = new ContentElement(getNewContentNumber());
        if (contentElements.get(currentLanguage) == null) {
            contentElements.put(currentLanguage, new ArrayList<>());
        }
        contentElements.get(currentLanguage).add(contentElement);
        currentContentIndex = contentElements.get(currentLanguage).size() - 1;
        discard_content_btn.setDisable(noContentCreated);
        refreshIconButtons();
    }

    /**
     * Removes the image
     */
    @FXML
    protected void actionRemoveImage() {
        contentElements.get(currentLanguage).get(currentContentIndex).setImageFile(null);
        loadContentData(currentContentIndex);
    }

    /**
     * Removes the audio file
     */
    @FXML
    protected void actionRemoveAudio() {
        contentElements.get(currentLanguage).get(currentContentIndex).setAudioFile(null);
        loadContentData(currentContentIndex);
    }

    /**
     * Removes the video file
     */
    @FXML
    protected void actionRemoveVideo() {
        contentElements.get(currentLanguage).get(currentContentIndex).setVideoFile(null);
        loadContentData(currentContentIndex);
    }

    /**
     * Sets the status bar text to the remove image text
     */
    @FXML
    protected void actionStatusRemoveImage() {
        setContextStatus(resourceBundle.getString("contentStatusRemoveImage"));
    }

    /**
     * Sets the status bar text to the remove audio text
     */
    @FXML
    protected void actionStatusRemoveAudio() {
        setContextStatus(resourceBundle.getString("contentStatusRemoveAudio"));
    }

    /**
     * Sets the status bar text to the remove video text
     */
    @FXML
    protected void actionStatusRemoveVideo() {
        setContextStatus(resourceBundle.getString("contentStatusRemoveVideo"));
    }

    /**
     * Sets the status bar text to the create content text
     */
    @FXML
    protected void actionStatusCreateContent() {
        setContextStatus(resourceBundle.getString("contentStatusCreateContent"));
    }

    /**
     * Sets the status bar text to the remove content text
     */
    @FXML
    protected void actionStatusRemoveContent() {
        setContextStatus(resourceBundle.getString("contentStatusRemoveContent"));
    }

    /**
     * Sets the status bar text to the add media file text
     */
    @FXML
    protected void actionStatusSelectMediaFile() {
        setContextStatus(resourceBundle.getString("contentStatusAddMediaFile"));
    }

    /**
     * Clears the status bar text
     */
    @FXML
    protected void actionStatusClear() {
        setContextStatus("");
    }

    /**
     * Adds a content
     */
    @FXML
    protected void actionAddContent() {
        if (checkContentTitle()) {
            for (String language : languages) {
                if (!currentLanguage.equals(language)) {
                    contentElements.get(language).add(new ContentElement(contentElements.get(currentLanguage).get(currentContentIndex)));
                }
            }
            creatingContent = false;
            noContentCreated = false;
            setContentFieldsFocus(false);
            refreshContentsLv();
            contents_lv.getSelectionModel().select(contents_lv.getItems().size() - 1);
            refreshIconButtons();
        } else {
            title_et.requestFocus();
        }
    }

    private boolean checkContentTitle() {
        if ("".equals(title_et.getText().trim())) {
            title_et.setText(resourceBundle.getString("contentTitleBlankText"));
            DialogTemplate.showErrorDialog(resourceBundle.getString("contentCreateErrorShort"), resourceBundle.getString("contentCreateError"), resourceBundle.getString("contentCreateErrorText"));
            return false;
        }
        return true;
    }

    /**
     * Removes inserted data for new content
     */
    @FXML
    protected void actionDiscardContent() {
        contentElements.get(currentLanguage).remove(contentElements.get(currentLanguage).size() - 1);
        setContentFieldsFocus(false);
        creatingContent = false;
        contents_lv.getSelectionModel().select(0);
        loadContentData(0);
        refreshIconButtons();
    }

    /**
     * Starts or stops playing the audio file
     */
    @FXML
    protected void audioBtnClicked() {
        if (!playing) {
            audioPlay();
        } else {
            audioPause();
        }
    }

    /**
     * Copies all content of a language to a new one
     */
    @FXML
    protected void actionCopyLanguage() {
        String newLanguage = DialogTemplate.showLanguageDialog(resourceBundle.getString("contentCreateLanguage"), resourceBundle.getString("contentCreateLanguageText"), resourceBundle.getString("contentEnterLanguage"));
        if (!languages.contains(newLanguage) && newLanguage != null && !"".equals(newLanguage)) {
            languages.add(newLanguage);
            contentElements.put(newLanguage, new ArrayList<>());
            for (ContentElement contentElement : contentElements.get(currentLanguage)) {
                ContentElement copyElement = new ContentElement(contentElement);
                if (copyElement.getImageFile() != null && copyElement.getImageFile().exists()) {
                    copyElement.setImageUpdated(true);
                }
                if (copyElement.getAudioFile() != null && copyElement.getAudioFile().exists()) {
                    copyElement.setAudioUpdated(true);
                }
                if (copyElement.getVideoFile() != null && copyElement.getVideoFile().exists()) {
                    copyElement.setVideoUpdated(true);
                }
                contentElements.get(newLanguage).add(copyElement);
            }
            refreshLanguages();
            language_cb.getSelectionModel().select(newLanguage);
            refreshIconButtons();
        } else if (languages.contains(newLanguage) || !"".equals(newLanguage)) {
            DialogTemplate.showErrorDialog(resourceBundle.getString("contentCreateLanguage"), resourceBundle.getString("contentWrongInput"), resourceBundle.getString("contentLanguageUniqueWarning"));
        }
    }

    /**
     * Sets the status bar text to the copylanguage text
     */
    @FXML
    protected void actionStatusCopyLanguage() {
        setContextStatus(resourceBundle.getString("contentCopyLanguage"));
    }

    /**
     * Removes a language
     */
    @FXML
    protected void actionRemoveLanguage() {
        if (DialogTemplate.showYesNoDialog(resourceBundle.getString("dialogConfirmation"), resourceBundle.getString("contentRemoveLanguage"), resourceBundle.getString("contentRemoveLanguageConfirmation"))) {
            languages.remove(currentLanguage);
            contentElements.remove(currentLanguage);
            refreshLanguages();
            refreshIconButtons();
            language_cb.getSelectionModel().select(0);
        }
    }

    /**
     * Sets the status bar text to the remove language text
     */
    @FXML
    protected void actionStatusRemoveLanguage() {
        setContextStatus(resourceBundle.getString("contentRemoveLanguage"));
    }

    private void audioPlay() {
        audio_play_iv.setImage(new Image(Controller.class.getResourceAsStream("/res/img/audio_pause.png")));
        playing = true;
        try {
            mediaPlayer.play();
        } catch (Exception e) {
        }
    }

    private void audioPause() {
        audio_play_iv.setImage(new Image(Controller.class.getResourceAsStream("/res/img/audio_play.png")));
        playing = false;
        try {
            mediaPlayer.pause();
        } catch (Exception e) {
        }
    }

    private void setAudioFile(File file) {
        if (file != null && file.exists()) {
            try {
                File tmpDir = FileManager.getTmpName("audio");
                File f = File.createTempFile("audio", null, tmpDir);
                f.delete();
                audioTmpFile = new File(f.getAbsolutePath());
                Files.copy(file.toPath(), audioTmpFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                Media media = new Media(audioTmpFile.toURI().toString());
                mediaPlayer = null;
                mediaPlayer = new MediaPlayer(media);

                mediaPlayer.setOnReady(new Runnable() {

                    @Override
                    public void run() {
                        final double totalDuration = mediaPlayer.getTotalDuration().toSeconds();
                        audio_slider_sb.setMax(totalDuration);
                        audio_player_minutes_total.setText(getFormattedTime((int) totalDuration));
                        player_group.setVisible(true);
                        removeAudioIv.setVisible(true);
                        mediaPlayer.currentTimeProperty().addListener(new ChangeListener<Duration>() {
                            @Override
                            public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
                                new Runnable() {
                                    public void run() {
                                        double seconds = mediaPlayer.getCurrentTime().toSeconds();
                                        if (!audio_slider_sb.isDisabled() && !audio_slider_sb.isValueChanging() && seconds < audio_slider_sb.getMax()) {
                                            audio_slider_sb.setValue(seconds);
                                        }
                                        if (!audio_slider_sb.isDisabled() && seconds <= totalDuration) {
                                            audio_player_minutes_played.setText(getFormattedTime((int) seconds));
                                        }
                                    }
                                }.run();
                            }
                        });
                    }
                });
                mediaPlayer.setOnEndOfMedia(new Runnable() {
                    @Override
                    public void run() {
                        mediaPlayer.stop();
                        audioPause();
                        mediaPlayer.seek(Duration.seconds(0));
                        audio_slider_sb.setValue(0);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            player_group.setVisible(false);
            removeAudioIv.setVisible(false);
        }
    }

    private void setContextStatus(String text) {
        if (!statusLabel.getStyleClass().contains("status-important")) {
            statusLabel.setText(text);
        }
    }

    private void refreshIconButtons() {
        if (languages.size() > 1 && !creatingContent) {
            removeLanguageIv.setImage(removeImage);
            removeLanguageIv.setDisable(false);
            language_cb.setDisable(false);
        } else {
            removeLanguageIv.setImage(removeImageDisabled);
            removeLanguageIv.setDisable(true);
            language_cb.setDisable(true);
        }
        if (!creatingContent) {
            copyLanguageIv.setImage(copyImage);
            copyLanguageIv.setDisable(false);
        } else {
            copyLanguageIv.setImage(copyImageDisabled);
            copyLanguageIv.setDisable(true);
        }

        if (!creatingContent) {
            createContentIv.setImage(createImage);
            removeContentIv.setImage(removeImage);
            createContentIv.setDisable(false);
            removeContentIv.setDisable(false);
            contents_lv.setDisable(false);
        } else {
            createContentIv.setImage(createImageDisabled);
            removeContentIv.setImage(removeImageDisabled);
            createContentIv.setDisable(true);
            removeContentIv.setDisable(true);
            contents_lv.setDisable(true);
        }
    }

    /**
     * Cel for image and text in the listview
     */
    public static class LvImageTextCell extends ListCell<Label> {
        /**
         * Creates the cell
         */
        public LvImageTextCell() {
            super();
        }

        /**
         * Updates an item
         *
         * @param item  item
         * @param empty true, if it is empty, else it is not empty
         */
        @Override
        protected void updateItem(Label item, boolean empty) {
            super.updateItem(item, empty);
            setGraphic(item);
        }
    }
}
