/*
 * Hello Minecraft! Launcher
 * Copyright (C) 2021  huangyuhui <huanghongxun2008@126.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.jackhuang.hmcl.ui;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXRadioButton;
import com.jfoenix.controls.JFXScrollPane;
import com.jfoenix.controls.JFXSpinner;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import org.jackhuang.hmcl.Metadata;
import org.jackhuang.hmcl.upgrade.RemoteVersion;
import org.jackhuang.hmcl.ui.construct.DialogCloseEvent;
import org.jackhuang.hmcl.ui.construct.JFXHyperlink;
import org.jackhuang.hmcl.util.platform.OperatingSystem;

import static org.jackhuang.hmcl.ui.FXUtils.onEscPressed;
import static org.jackhuang.hmcl.util.i18n.I18n.i18n;
import static org.jackhuang.hmcl.util.logging.Logger.LOG;

public final class UpgradeDialog extends JFXDialogLayout {

    public UpgradeDialog(RemoteVersion remoteVersion, java.util.function.Consumer<RemoteVersion.DownloadInfo> updateCallback) {
        maxWidthProperty().bind(Controllers.getScene().widthProperty().multiply(0.7));
        maxHeightProperty().bind(Controllers.getScene().heightProperty().multiply(0.7));

        setHeading(new Label(i18n("update.changelog") + " - " + i18n("update.newest_version", remoteVersion.getVersion())));
        
        VBox contentBox = new VBox(16);
        contentBox.setPadding(new Insets(16));
        
        JFXHyperlink openInBrowser = new JFXHyperlink(i18n("web.view_in_browser"));
        openInBrowser.setExternalLink(Metadata.GITHUB_RELEASES_URL);
        
        ScrollPane changelogPane = new ScrollPane();
        changelogPane.setFitToWidth(true);
        changelogPane.setFitToHeight(true);
        changelogPane.setPrefHeight(300);
        JFXScrollPane.smoothScrolling(changelogPane);
        
        String changelog = remoteVersion.getChangelog();
        if (changelog != null && !changelog.isEmpty()) {
            Label changelogLabel = new Label(changelog);
            changelogLabel.setWrapText(true);
            changelogLabel.setStyle("-fx-font-family: monospace; -fx-font-size: 12px;");
            changelogPane.setContent(changelogLabel);
        } else {
            changelogPane.setContent(new Label(i18n("update.changelog.unavailable")));
        }
        
        contentBox.getChildren().add(changelogPane);
        
        VBox downloadOptionsBox = new VBox(8);
        downloadOptionsBox.setPadding(new Insets(8, 0, 0, 0));
        
        boolean isWindows = OperatingSystem.CURRENT_OS == OperatingSystem.WINDOWS;
        boolean hasExe = remoteVersion.hasExeDownload();
        
        ToggleGroup downloadGroup = new ToggleGroup();
        JFXRadioButton jarRadio = new JFXRadioButton(i18n("update.download.jar"));
        jarRadio.setToggleGroup(downloadGroup);
        jarRadio.setUserData(remoteVersion.getJarDownload());
        
        if (isWindows && hasExe) {
            JFXRadioButton exeRadio = new JFXRadioButton(i18n("update.download.exe"));
            exeRadio.setToggleGroup(downloadGroup);
            exeRadio.setUserData(remoteVersion.getExeDownload());
            exeRadio.setSelected(true);
            
            downloadOptionsBox.getChildren().addAll(
                new Label(i18n("update.download.choose")),
                exeRadio,
                jarRadio
            );
        } else {
            jarRadio.setSelected(true);
            downloadOptionsBox.getChildren().add(jarRadio);
        }
        
        contentBox.getChildren().add(downloadOptionsBox);
        setBody(contentBox);

        JFXButton updateButton = new JFXButton(i18n("update.accept"));
        updateButton.getStyleClass().add("dialog-accept");
        updateButton.setOnAction(e -> {
            RemoteVersion.DownloadInfo selectedDownload = (RemoteVersion.DownloadInfo) downloadGroup.getSelectedToggle().getUserData();
            if (selectedDownload != null) {
                updateCallback.accept(selectedDownload);
            }
        });

        JFXButton cancelButton = new JFXButton(i18n("button.cancel"));
        cancelButton.getStyleClass().add("dialog-cancel");
        cancelButton.setOnAction(e -> fireEvent(new DialogCloseEvent()));

        setActions(openInBrowser, updateButton, cancelButton);
        onEscPressed(this, cancelButton::fire);
    }
}
