package at.happynev.mwoscoreboardhelper;

import at.happynev.mwoscoreboardhelper.tracer.*;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Created by Nev on 15.01.2017.
 */
public class PreviewTabController {
    @FXML
    Button buttonLoadScreenshot;
    @FXML
    ImageView imagePreview;
    @FXML
    Pane paneResults;
    @FXML
    Button buttonPerformTrace;

    @FXML
    private void initialize() {
        buttonLoadScreenshot.setOnAction(event -> loadImage());
        buttonPerformTrace.setOnAction(event -> performTrace());
    }

    private void performTrace() {
        try {
            BufferedImage img = SwingFXUtils.fromFXImage(imagePreview.getImage(), null);
            if (false) {
                BufferedImage result = TraceHelpers.extractSpecificColor(img, new int[]{200, 160, 55}, new int[]{242, 186, 84});
                imagePreview.setImage(SwingFXUtils.toFXImage(result, null));
            } else {

                ScreenshotType type = ScreenshotType.identifyType(img);
                HBox matchInfo = new HBox();
                matchInfo.setSpacing(5);
                paneResults.getChildren().clear();

                paneResults.getChildren().add(new Label("Screenshot identified as " + type));
                Offsets off = Offsets.getInstance(type, img);
                if (type == ScreenshotType.QP_1PREPARATION) {
                } else if (type == ScreenshotType.QP_3SUMMARY) {
                } else {
                    for (TraceableImage tr : ScreenshotType.getLastCheck()) {
                        paneResults.getChildren().add(new HBox(new ImageView(SwingFXUtils.toFXImage(tr.getImage(), null)), new Label(" --> " + tr.getValue())));
                    }
                    return;
                }
                MatchInfoTracer match = new MatchInfoTracer(img, off);
                paneResults.getChildren().add(matchInfo);
                Label labelMap = new Label(match.getMap());
                Label labelGameMode = new Label(match.getGameMode());
                Label labelBattleTime = new Label(match.getBattleTime());
                Label labelServer = new Label(match.getServer());
                Label labelWinningTeam = new Label(match.getWinningTeam());
                Label labelLosingTeam = new Label(match.getLosingTeam());
                Label labelMatchResult = new Label(match.getMatchResult());
                labelMap.textProperty().bind(match.progressProperty());
                match.finishedProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue) {
                        labelMap.textProperty().unbind();
                        labelMap.setText(match.getMap());
                        labelGameMode.setText(match.getGameMode());
                        labelBattleTime.setText(match.getBattleTime());
                        labelServer.setText(match.getServer());
                        labelWinningTeam.setText(match.getWinningTeam());
                        labelLosingTeam.setText(match.getLosingTeam());
                        labelMatchResult.setText(match.getMatchResult());
                        paneResults.getChildren().add(0, new Label("map:" + match.getMapImage().getTraceDuration() + "ms"));
                        paneResults.getChildren().add(0, new Label("time:" + match.getBattleTimeImage().getTraceDuration() + "ms"));
                    }
                });
                matchInfo.getChildren().add(new VBox(new ImageView(SwingFXUtils.toFXImage(match.getMapImage().getImage(), null)), labelMap));
                matchInfo.getChildren().add(new VBox(new ImageView(SwingFXUtils.toFXImage(match.getGameModeImage().getImage(), null)), labelGameMode));
                if (match.getServerImage() != null) {
                    matchInfo.getChildren().add(new VBox(new ImageView(SwingFXUtils.toFXImage(match.getServerImage().getImage(), null)), labelServer));
                }
                if (match.getBattleTimeImage() != null) {
                    matchInfo.getChildren().add(new VBox(new ImageView(SwingFXUtils.toFXImage(match.getBattleTimeImage().getImage(), null)), labelBattleTime));
                }
                if (match.getWinningTeamImage() != null) {
                    matchInfo.getChildren().add(new VBox(new ImageView(SwingFXUtils.toFXImage(match.getWinningTeamImage().getImage(), null)), labelWinningTeam));
                }
                if (match.getLosingTeamImage() != null) {
                    matchInfo.getChildren().add(new VBox(new ImageView(SwingFXUtils.toFXImage(match.getLosingTeamImage().getImage(), null)), labelLosingTeam));
                }
                if (match.getMatchResultImage() != null) {
                    matchInfo.getChildren().add(new VBox(new ImageView(SwingFXUtils.toFXImage(match.getMatchResultImage().getImage(), null)), labelMatchResult));
                }
                for (int i = 0; i < 24; i++) {
                    PlayerInfoTracer pi = new PlayerInfoTracer(img, i, off);
                    HBox playerInfo = new HBox();
                    playerInfo.setSpacing(5);
                    paneResults.getChildren().add(playerInfo);
                    Label labelUnit = new Label();
                    Label labelPilotName = new Label();
                    Label labelMech = new Label();
                    Label labelStatus = new Label();
                    Label labelMatchScore = new Label();
                    Label labelKills = new Label();
                    Label labelAssists = new Label();
                    Label labelDamage = new Label();
                    Label labelPing = new Label();
                    labelPilotName.textProperty().bind(pi.progressProperty());
                    pi.finishedProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue) {
                            labelPilotName.textProperty().unbind();
                            labelUnit.setText(pi.getUnitTag());
                            labelPilotName.setText(pi.getPilotName());
                            labelMech.setText(pi.getMech());
                            labelStatus.setText(pi.getStatus());
                            labelMatchScore.setText("" + pi.getMatchScore());
                            labelKills.setText("" + pi.getKills());
                            labelAssists.setText("" + pi.getAssists());
                            labelDamage.setText("" + pi.getDamage());
                            labelPing.setText("" + pi.getPing());
                        }
                    });
                    if (pi.getUnitTagImage() != null) {
                        playerInfo.getChildren().add(new VBox(new ImageView(SwingFXUtils.toFXImage(pi.getUnitTagImage().getImage(), null)), labelUnit));
                    }
                    if (pi.getPilotNameImage() != null) {
                        playerInfo.getChildren().add(new VBox(new ImageView(SwingFXUtils.toFXImage(pi.getPilotNameImage().getImage(), null)), labelPilotName));
                    }
                    if (pi.getMechImage() != null) {
                        playerInfo.getChildren().add(new VBox(new ImageView(SwingFXUtils.toFXImage(pi.getMechImage().getImage(), null)), labelMech));
                    }
                    if (pi.getStatusImage() != null) {
                        playerInfo.getChildren().add(new VBox(new ImageView(SwingFXUtils.toFXImage(pi.getStatusImage().getImage(), null)), labelStatus));
                    }
                    if (pi.getMatchScoreImage() != null) {
                        playerInfo.getChildren().add(new VBox(new ImageView(SwingFXUtils.toFXImage(pi.getMatchScoreImage().getImage(), null)), labelMatchScore));
                    }
                    if (pi.getKillsImage() != null) {
                        playerInfo.getChildren().add(new VBox(new ImageView(SwingFXUtils.toFXImage(pi.getKillsImage().getImage(), null)), labelKills));
                    }
                    if (pi.getAssistsImage() != null) {
                        playerInfo.getChildren().add(new VBox(new ImageView(SwingFXUtils.toFXImage(pi.getAssistsImage().getImage(), null)), labelAssists));
                    }
                    if (pi.getDamageImage() != null) {
                        playerInfo.getChildren().add(new VBox(new ImageView(SwingFXUtils.toFXImage(pi.getDamageImage().getImage(), null)), labelDamage));
                    }
                    if (pi.getPingImage() != null) {
                        playerInfo.getChildren().add(new VBox(new ImageView(SwingFXUtils.toFXImage(pi.getPingImage().getImage(), null)), labelPing));
                    }
                }
            }
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    private void loadImage() {
        FileChooser fc = new FileChooser();
        fc.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("jpg", "jpeg", "png", "bmp"));
        fc.setInitialDirectory(new File("."));
        File result = fc.showOpenDialog(null);
        try {
            Image screenshot = new Image(result.toURI().toString());

            imagePreview.setImage(screenshot);
        } catch (Exception e) {
            Logger.error(e);
        }
    }
}
