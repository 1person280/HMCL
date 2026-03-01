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
package org.jackhuang.hmcl.download;

import org.jackhuang.hmcl.game.*;
import org.jackhuang.hmcl.task.Task;
import org.jackhuang.hmcl.util.SimpleMultimap;
import org.jackhuang.hmcl.util.StringUtils;
import org.jackhuang.hmcl.util.gson.JsonUtils;
import org.jackhuang.hmcl.util.versioning.VersionNumber;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.jackhuang.hmcl.download.LibraryAnalyzer.LibraryType.*;
import static org.jackhuang.hmcl.util.logging.Logger.LOG;

public class MaintainTask extends Task<Version> {
    private final GameRepository repository;
    private final Version version;

    public MaintainTask(GameRepository repository, Version version) {
        this.repository = repository;
        this.version = version;

        if (version.getInheritsFrom() != null)
            throw new IllegalArgumentException("MaintainTask requires independent game version");
    }

    @Override
    public void execute() {
        setResult(maintain(repository, version));
    }

    public static Version maintain(GameRepository repository, Version version) {
        if (version.getInheritsFrom() != null)
            throw new IllegalArgumentException("MaintainTask requires independent game version");

        String mainClass = version.resolve(null).getMainClass();

        if (mainClass != null && mainClass.equals(LibraryAnalyzer.LAUNCH_WRAPPER_MAIN)) {
            version = maintainOptiFineLibrary(repository, maintainGameWithLaunchWrapper(repository, unique(version), true), false);
        } else if (mainClass != null && mainClass.equals(LibraryAnalyzer.BOOTSTRAP_LAUNCHER_MAIN)) {
            version = maintainGameWithCpwBoostrapLauncher(repository, unique(version));
        } else {
            version = maintainOptiFineLibrary(repository, unique(version), false);
        }

        List<Library> libraries = version.getLibraries();
        if (!libraries.isEmpty()) {
            Library library = libraries.get(0);
            if ("org.glavo".equals(library.getGroupId())
                    && ("log4j-patch".equals(library.getArtifactId()) || "log4j-patch-beta9".equals(library.getArtifactId()))
                    && "1.0".equals(library.getVersion())
                    && library.getDownload() == null) {
                version = version.setLibraries(libraries.subList(1, libraries.size()));
            }
        }

        return version;
    }

    public static Version maintainPreservingPatches(GameRepository repository, Version version) {
        if (!version.isResolvedPreservingPatches())
            throw new IllegalArgumentException("MaintainTask requires independent game version");
        Version newVersion = maintain(repository, version.resolve(repository));
        return newVersion.setPatches(version.getPatches()).markAsUnresolved();
    }

    private static Version maintainGameWithLaunchWrapper(GameRepository repository, Version version, boolean reorderTweakClass) {
        LibraryAnalyzer libraryAnalyzer = LibraryAnalyzer.analyze(version, null);
        VersionLibraryBuilder builder = new VersionLibraryBuilder(version);

        if (libraryAnalyzer.has(OPTIFINE)) {
            if (builder.hasTweakClass(LibraryAnalyzer.OPTIFINE_TWEAKERS[1])) {
                builder.replaceTweakClass(LibraryAnalyzer.OPTIFINE_TWEAKERS[1], LibraryAnalyzer.OPTIFINE_TWEAKERS[0], !reorderTweakClass, reorderTweakClass);
            }
        } else {
            for (String optiFineTweaker : LibraryAnalyzer.OPTIFINE_TWEAKERS) {
                builder.removeTweakClass(optiFineTweaker);
            }
        }

        return builder.build();
    }

    private static String updateIgnoreList(GameRepository repository, Version version, String ignoreList) {
        String[] ignores = ignoreList.split(",");
        List<String> newIgnoreList = new ArrayList<>();

        newIgnoreList.add("${primary_jar}");

        Path libraryDirectory = repository.getLibrariesDirectory(version).toAbsolutePath().normalize();

        for (String classpathName : repository.getClasspath(version)) {
            Path classpathFile = Paths.get(classpathName).toAbsolutePath();
            String fileName = classpathFile.getFileName().toString();
            if (Stream.of(ignores).anyMatch(fileName::contains)) {
                String absolutePath;
                if (classpathFile.startsWith(libraryDirectory)) {
                    absolutePath = "${library_directory}${file_separator}" + libraryDirectory.relativize(classpathFile).toString().replace(File.separator, "${file_separator}");
                } else {
                    absolutePath = classpathFile.toString();
                }
                newIgnoreList.add(StringUtils.substringBefore(absolutePath, ","));
            }
        }
        return String.join(",", newIgnoreList);
    }

    private static Version maintainGameWithCpwBoostrapLauncher(GameRepository repository, Version version) {
        LibraryAnalyzer libraryAnalyzer = LibraryAnalyzer.analyze(version, null);
        VersionLibraryBuilder builder = new VersionLibraryBuilder(version);

        if (!libraryAnalyzer.has(NEO_FORGE)) return version;

        Optional<String> bslVersion = libraryAnalyzer.getVersion(BOOTSTRAP_LAUNCHER);

        if (bslVersion.isPresent()) {
            if (VersionNumber.compare(bslVersion.get(), "0.1.17") < 0) {
                List<Argument> jvm = builder.getMutableJvmArguments();
                for (int i = 0; i < jvm.size(); i++) {
                    Argument jvmArg = jvm.get(i);
                    if (jvmArg instanceof StringArgument) {
                        String jvmArgStr = jvmArg.toString();
                        if (jvmArgStr.startsWith("-DignoreList=")) {
                            jvm.set(i, new StringArgument("-DignoreList=" + updateIgnoreList(repository, version, jvmArgStr.substring("-DignoreList=".length()))));
                        }
                    }
                }
            } else {
                List<Argument> jvm = builder.getMutableJvmArguments();
                for (int i = 0; i < jvm.size(); i++) {
                    Argument jvmArg = jvm.get(i);
                    if (jvmArg instanceof StringArgument) {
                        String jvmArgStr = jvmArg.toString();
                        if (jvmArgStr.startsWith("-DignoreList=")) {
                            jvm.set(i, new StringArgument(jvmArgStr + ",${primary_jar_name}"));
                        }
                    }
                }
            }
        }

        return builder.build();
    }

    private static Version maintainOptiFineLibrary(GameRepository repository, Version version, boolean remove) {
        return version;
    }

    public static Version unique(Version version) {
        List<Library> libraries = new ArrayList<>();

        SimpleMultimap<String, Integer, List<Integer>> multimap = new SimpleMultimap<>(HashMap::new, ArrayList::new);

        for (Library library : version.getLibraries()) {
            String id = library.getGroupId() + ":" + library.getArtifactId();
            VersionNumber number = VersionNumber.asVersion(library.getVersion());
            String serialized = JsonUtils.GSON.toJson(library);

            if (multimap.containsKey(id)) {
                boolean duplicate = false;
                for (int otherLibraryIndex : multimap.get(id)) {
                    Library otherLibrary = libraries.get(otherLibraryIndex);
                    VersionNumber otherNumber = VersionNumber.asVersion(otherLibrary.getVersion());
                    if (CompatibilityRule.equals(library.getRules(), otherLibrary.getRules())) {
                        boolean flag = true;
                        if (number.compareTo(otherNumber) > 0) {
                            libraries.set(otherLibraryIndex, library);
                        } else if (number.compareTo(otherNumber) == 0) {
                            if (library.equals(otherLibrary)) {
                                String otherSerialized = JsonUtils.GSON.toJson(otherLibrary);
                                if (serialized.length() > otherSerialized.length()) {
                                    libraries.set(otherLibraryIndex, library);
                                }
                            } else {
                                flag = false;
                            }
                        }
                        if (flag) {
                            duplicate = true;
                            break;
                        }
                    }
                }

                if (!duplicate) {
                    multimap.put(id, libraries.size());
                    libraries.add(library);
                }
            } else {
                multimap.put(id, libraries.size());
                libraries.add(library);
            }
        }

        return version.setLibraries(libraries);
    }
}
