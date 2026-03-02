/*
 * Hello Minecraft! Launcher
 * Copyright (C) 2025  huangyuhui <huanghongxun2008@126.com> and contributors
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

import javafx.application.Platform;
import javafx.stage.Stage;
import org.jackhuang.hmcl.Launcher;
import org.jackhuang.hmcl.Metadata;
import org.jackhuang.hmcl.util.i18n.I18n;
import org.jackhuang.hmcl.util.logging.Logger;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.*;

public final class SystemTrayManager {

    private static SystemTrayManager instance;

    private SystemTray tray;
    private TrayIcon trayIcon;
    private Stage stage;
    private boolean initialized = false;
    private JPopupMenu popupMenu;
    private JFrame hiddenFrame;

    private SystemTrayManager() {
    }

    public static synchronized SystemTrayManager getInstance() {
        if (instance == null) {
            instance = new SystemTrayManager();
        }
        return instance;
    }

    public void initialize(Stage stage) {
        if (initialized) {
            return;
        }

        if (!SystemTray.isSupported()) {
            Logger.LOG.info("System tray is not supported on this platform");
            return;
        }

        this.stage = stage;
        this.tray = SystemTray.getSystemTray();

        createHiddenFrame();
        createPopupMenu();

        try {
            Image image = loadTrayIcon();
            trayIcon = new TrayIcon(image, Metadata.FULL_TITLE);
            trayIcon.setImageAutoSize(true);

            trayIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    if (e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3) {
                        showPopupMenu(e.getXOnScreen(), e.getYOnScreen());
                    }
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        showPopupMenu(e.getXOnScreen(), e.getYOnScreen());
                    }
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        Platform.runLater(() -> showStage());
                    }
                }
            });

            tray.add(trayIcon);
            initialized = true;
            Logger.LOG.info("System tray initialized successfully");
        } catch (AWTException e) {
            Logger.LOG.warning("Failed to add tray icon", e);
        } catch (Exception e) {
            Logger.LOG.warning("Failed to initialize system tray", e);
        }
    }

    private void createHiddenFrame() {
        hiddenFrame = new JFrame();
        hiddenFrame.setUndecorated(true);
        hiddenFrame.setSize(1, 1);
        hiddenFrame.setType(Window.Type.UTILITY);
        hiddenFrame.setVisible(true);
    }

    private void createPopupMenu() {
        String showText = getI18nText("system_tray.show");
        String exitText = getI18nText("system_tray.exit");

        popupMenu = new JPopupMenu();
        
        JMenuItem showItem = new JMenuItem(showText);
        showItem.addActionListener(e -> {
            popupMenu.setVisible(false);
            Platform.runLater(this::showStage);
        });
        popupMenu.add(showItem);
        
        popupMenu.addSeparator();
        
        JMenuItem exitItem = new JMenuItem(exitText);
        exitItem.addActionListener(e -> {
            popupMenu.setVisible(false);
            removeTrayIcon();
            Launcher.stopApplication();
        });
        popupMenu.add(exitItem);
    }

    private void showPopupMenu(int x, int y) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice().getDefaultConfiguration();
            Rectangle screenBounds = gc.getBounds();
            
            Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
            int menuHeight = popupMenu.getPreferredSize().height;
            int menuWidth = popupMenu.getPreferredSize().width;
            
            int menuX = x;
            int menuY = y;
            
            boolean isNearBottom = (y + menuHeight) > (screenBounds.y + screenBounds.height - screenInsets.bottom);
            boolean isNearTop = y < (screenBounds.y + screenInsets.top + 50);
            boolean isNearRight = (x + menuWidth) > (screenBounds.x + screenBounds.width - screenInsets.right);
            boolean isNearLeft = x < (screenBounds.x + screenInsets.left + 50);
            
            if (isNearBottom) {
                menuY = y - menuHeight;
            } else if (isNearTop) {
                menuY = y;
            } else {
                menuY = y;
            }
            
            if (isNearRight) {
                menuX = x - menuWidth;
            } else if (isNearLeft) {
                menuX = x;
            } else {
                menuX = x;
            }
            
            menuX = Math.max(screenBounds.x + screenInsets.left, Math.min(menuX, screenBounds.x + screenBounds.width - screenInsets.right - menuWidth));
            menuY = Math.max(screenBounds.y + screenInsets.top, Math.min(menuY, screenBounds.y + screenBounds.height - screenInsets.bottom - menuHeight));
            
            hiddenFrame.setLocation(menuX, menuY);
            hiddenFrame.setVisible(true);
            popupMenu.show(hiddenFrame, 0, 0);
        });
    }

    private Image loadTrayIcon() {
        int trayIconSize = tray.getTrayIconSize().width;
        if (trayIconSize <= 0) {
            trayIconSize = 16;
        }

        String iconPath;
        if (trayIconSize <= 16) {
            iconPath = "/assets/img/icon.png";
        } else if (trayIconSize <= 32) {
            iconPath = "/assets/img/icon@2x.png";
        } else if (trayIconSize <= 64) {
            iconPath = "/assets/img/icon@4x.png";
        } else {
            iconPath = "/assets/img/icon@8x.png";
        }

        try (InputStream is = SystemTrayManager.class.getResourceAsStream(iconPath)) {
            if (is != null) {
                java.awt.image.BufferedImage bufferedImage = javax.imageio.ImageIO.read(is);
                if (bufferedImage != null) {
                    return bufferedImage.getScaledInstance(trayIconSize, trayIconSize, Image.SCALE_SMOOTH);
                }
            }
        } catch (IOException e) {
            Logger.LOG.warning("Failed to load tray icon from " + iconPath, e);
        }

        return createDefaultIcon(trayIconSize);
    }

    private Image createDefaultIcon(int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(java.awt.Color.GREEN);
        g2d.fillRect(0, 0, size, size);
        g2d.dispose();
        return image;
    }

    private String getI18nText(String key) {
        if (Platform.isFxApplicationThread()) {
            String value = I18n.i18n(key);
            Logger.LOG.info("Got i18n text for key " + key + " = " + value + " (FX thread)");
            return value;
        }
        
        AtomicReference<String> result = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            String value = I18n.i18n(key);
            Logger.LOG.info("Got i18n text for key " + key + " = " + value + " (non-FX thread)");
            result.set(value);
            latch.countDown();
        });
        
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return key;
        }
        
        return result.get();
    }

    private void showStage() {
        if (stage != null) {
            stage.show();
            stage.toFront();
            stage.requestFocus();
            if (stage.isIconified()) {
                stage.setIconified(false);
            }
        }
    }

    public void hideStage() {
        if (stage != null) {
            stage.hide();
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void removeTrayIcon() {
        if (tray != null && trayIcon != null) {
            try {
                tray.remove(trayIcon);
            } catch (Exception e) {
                Logger.LOG.warning("Failed to remove tray icon", e);
            }
        }
    }

    public void shutdown() {
        removeTrayIcon();
        if (hiddenFrame != null) {
            hiddenFrame.dispose();
            hiddenFrame = null;
        }
        popupMenu = null;
        initialized = false;
    }
}
