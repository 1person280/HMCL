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
package org.jackhuang.hmcl.download.neoforge;

import org.jackhuang.hmcl.game.Artifact;
import org.jackhuang.hmcl.game.Library;
import org.jackhuang.hmcl.util.gson.JsonMap;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ForgeNewInstallProfile {
    private final List<Processor> processors;
    private final List<Library> libraries;
    private final String json;
    private final String minecraft;
    private final String version;
    private final String profile;
    private final String path;
    private final String filePath;
    private final String logoFile;
    private final String mirrorList;
    private final JsonMap<String, String> data;

    public ForgeNewInstallProfile(List<Processor> processors, List<Library> libraries, String json, String minecraft, String version, String profile, String path, String filePath, String logoFile, String mirrorList, JsonMap<String, String> data) {
        this.processors = processors;
        this.libraries = libraries;
        this.json = json;
        this.minecraft = minecraft;
        this.version = version;
        this.profile = profile;
        this.path = path;
        this.filePath = filePath;
        this.logoFile = logoFile;
        this.mirrorList = mirrorList;
        this.data = data;
    }

    public List<Processor> getProcessors() {
        return processors;
    }

    public List<Library> getLibraries() {
        return libraries;
    }

    public String getJson() {
        return json;
    }

    public String getMinecraft() {
        return minecraft;
    }

    public String getVersion() {
        return version;
    }

    public String getProfile() {
        return profile;
    }

    public Optional<Artifact> getPath() {
            if (path == null) return Optional.empty();
            return Optional.of(Artifact.fromDescriptor(path));
        }

    public String getFilePath() {
        return filePath;
    }

    public String getLogoFile() {
        return logoFile;
    }

    public String getMirrorList() {
        return mirrorList;
    }

    public Map<String, String> getData() {
        return data;
    }

    public static final class Processor {
        private final Artifact jar;
        private final List<Artifact> classpath;
        private final List<String> args;
        private final Map<String, String> outputs;
        private final List<String> sides;

        public Processor(Artifact jar, List<Artifact> classpath, List<String> args, Map<String, String> outputs, @Nullable List<String> sides) {
            this.jar = jar;
            this.classpath = classpath;
            this.args = args;
            this.outputs = outputs;
            this.sides = sides;
        }

        public Artifact getJar() {
            return jar;
        }

        public List<Artifact> getClasspath() {
            return classpath;
        }

        public List<String> getArgs() {
            return args;
        }

        public Map<String, String> getOutputs() {
            return outputs;
        }

        public List<String> getSides() {
            return sides;
        }
    }
}
