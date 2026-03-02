/*
 * Hello Minecraft! Launcher
 * Copyright (C) 2020  huangyuhui <huanghongxun2008@126.com> and contributors
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
package org.jackhuang.hmcl.upgrade;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.jackhuang.hmcl.Metadata;
import org.jackhuang.hmcl.task.FileDownloadTask.IntegrityCheck;
import org.jackhuang.hmcl.util.gson.JsonUtils;
import org.jackhuang.hmcl.util.io.NetworkUtils;
import org.jackhuang.hmcl.util.platform.OperatingSystem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class RemoteVersion {

    public static RemoteVersion fetch(UpdateChannel channel, boolean preview, String url) throws IOException {
        try {
            JsonObject response = JsonUtils.fromNonNullJson(NetworkUtils.doGet(url), JsonObject.class);
            String version = parseVersionFromTagName(response.get("tag_name").getAsString());
            String changelog = Optional.ofNullable(response.get("body")).map(JsonElement::getAsString).orElse("");
            
            List<DownloadInfo> downloads = new ArrayList<>();
            JsonArray assets = response.getAsJsonArray("assets");
            if (assets != null) {
                for (JsonElement assetElement : assets) {
                    JsonObject asset = assetElement.getAsJsonObject();
                    String name = asset.get("name").getAsString();
                    String downloadUrl = asset.get("browser_download_url").getAsString();
                    
                    if (name.endsWith(".jar") && !name.contains("sources") && !name.contains("javadoc")) {
                        downloads.add(new DownloadInfo(name, downloadUrl, Type.JAR));
                    } else if (name.endsWith(".exe") && !name.contains("sha1") && !name.contains("sha256")) {
                        downloads.add(new DownloadInfo(name, downloadUrl, Type.EXE));
                    }
                }
            }
            
            if (downloads.isEmpty()) {
                throw new IOException("No download url is available");
            }
            
            return new RemoteVersion(channel, version, downloads, changelog, preview, false);
        } catch (JsonParseException e) {
            throw new IOException("Malformed response", e);
        }
    }
    
    private static String parseVersionFromTagName(String tagName) {
        if (tagName.startsWith("v")) {
            return tagName.substring(1);
        }
        return tagName;
    }

    private final UpdateChannel channel;
    private final String version;
    private final List<DownloadInfo> downloads;
    private final String changelog;
    private final boolean preview;
    private final boolean force;

    public RemoteVersion(UpdateChannel channel, String version, List<DownloadInfo> downloads, String changelog, boolean preview, boolean force) {
        this.channel = channel;
        this.version = version;
        this.downloads = downloads;
        this.changelog = changelog;
        this.preview = preview;
        this.force = force;
    }

    public UpdateChannel getChannel() {
        return channel;
    }

    public String getVersion() {
        return version;
    }
    
    public String getChangelog() {
        return changelog;
    }
    
    public List<DownloadInfo> getDownloads() {
        return downloads;
    }
    
    public DownloadInfo getPreferredDownload() {
        if (OperatingSystem.CURRENT_OS == OperatingSystem.WINDOWS) {
            for (DownloadInfo download : downloads) {
                if (download.getType() == Type.EXE) {
                    return download;
                }
            }
        }
        for (DownloadInfo download : downloads) {
            if (download.getType() == Type.JAR) {
                return download;
            }
        }
        return downloads.get(0);
    }
    
    public DownloadInfo getJarDownload() {
        for (DownloadInfo download : downloads) {
            if (download.getType() == Type.JAR) {
                return download;
            }
        }
        return null;
    }
    
    public DownloadInfo getExeDownload() {
        for (DownloadInfo download : downloads) {
            if (download.getType() == Type.EXE) {
                return download;
            }
        }
        return null;
    }
    
    public boolean hasExeDownload() {
        return getExeDownload() != null;
    }

    public boolean isPreview() {
        return preview;
    }

    public boolean isForce() {
        return force;
    }

    @Override
    public String toString() {
        return "[" + version + " from GitHub Releases]";
    }
    
    public static class DownloadInfo {
        private final String name;
        private final String url;
        private final Type type;
        
        public DownloadInfo(String name, String url, Type type) {
            this.name = name;
            this.url = url;
            this.type = type;
        }
        
        public String getName() {
            return name;
        }
        
        public String getUrl() {
            return url;
        }
        
        public Type getType() {
            return type;
        }
    }

    public enum Type {
        JAR,
        EXE
    }
}
