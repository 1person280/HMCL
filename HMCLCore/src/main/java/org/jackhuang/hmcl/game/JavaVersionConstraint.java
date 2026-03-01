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
package org.jackhuang.hmcl.game;

import org.jackhuang.hmcl.download.LibraryAnalyzer;
import org.jackhuang.hmcl.java.JavaRuntime;
import org.jackhuang.hmcl.util.Lang;
import org.jackhuang.hmcl.util.platform.Architecture;
import org.jackhuang.hmcl.util.platform.OperatingSystem;
import org.jackhuang.hmcl.util.versioning.GameVersionNumber;
import org.jackhuang.hmcl.util.versioning.VersionNumber;
import org.jackhuang.hmcl.util.versioning.VersionRange;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

import static org.jackhuang.hmcl.download.LibraryAnalyzer.LAUNCH_WRAPPER_MAIN;

public enum JavaVersionConstraint {
    VANILLA(true, VersionRange.all(), VersionRange.all()) {
        @Override
        protected boolean appliesToVersionImpl(GameVersionNumber gameVersionNumber, @Nullable Version version, @Nullable JavaRuntime java, @Nullable LibraryAnalyzer analyzer) {
            return version == null || version.getJavaVersion() == null;
        }

        @Override
        public boolean checkJava(GameVersionNumber gameVersionNumber, Version version, JavaRuntime java) {
            GameJavaVersion minimumJavaVersion = GameJavaVersion.getMinimumJavaVersion(gameVersionNumber);
            return minimumJavaVersion == null || java.getParsedVersion() >= minimumJavaVersion.majorVersion();
        }
    },
    GAME_JSON(true, VersionRange.all(), VersionRange.all()) {
        @Override
        protected boolean appliesToVersionImpl(GameVersionNumber gameVersionNumber, @Nullable Version version,
                                               @Nullable JavaRuntime java, @Nullable LibraryAnalyzer analyzer) {
            if (version == null) return false;
            return gameVersionNumber.compareTo("1.7.10") >= 0 && version.getJavaVersion() != null;
        }

        @Override
        public VersionRange<VersionNumber> getJavaVersionRange(Version version) {
            String javaVersion;
            if (Objects.requireNonNull(version.getJavaVersion()).majorVersion() >= 9) {
                javaVersion = "" + version.getJavaVersion().majorVersion();
            } else {
                javaVersion = "1." + version.getJavaVersion().majorVersion();
            }
            return VersionNumber.atLeast(javaVersion);
        }
    },
    MODDED_JAVA_17(false, GameVersionNumber.between("1.18", "1.20.4"), VersionNumber.between("17", "17.999")) {
        @Override
        protected boolean appliesToVersionImpl(GameVersionNumber gameVersionNumber, @Nullable Version version,
                                               @Nullable JavaRuntime java, @Nullable LibraryAnalyzer analyzer) {
            return analyzer != null && analyzer.has(LibraryAnalyzer.LibraryType.NEO_FORGE)
                    && super.appliesToVersionImpl(gameVersionNumber, version, java, analyzer);
        }
    },
    MODDED_JAVA_21(false, GameVersionNumber.atLeast("1.20.5"), VersionNumber.between("21", "21.999")) {
        @Override
        protected boolean appliesToVersionImpl(GameVersionNumber gameVersionNumber, @Nullable Version version,
                                               @Nullable JavaRuntime java, @Nullable LibraryAnalyzer analyzer) {
            return analyzer != null && analyzer.has(LibraryAnalyzer.LibraryType.NEO_FORGE)
                    && super.appliesToVersionImpl(gameVersionNumber, version, java, analyzer);
        }
    },
    LAUNCH_WRAPPER(true, GameVersionNumber.atMost("1.12.999"), VersionNumber.atMost("1.8.999")) {
        @Override
        protected boolean appliesToVersionImpl(GameVersionNumber gameVersionNumber, @Nullable Version version,
                                               @Nullable JavaRuntime java, @Nullable LibraryAnalyzer analyzer) {
            if (version == null) return false;
            return super.appliesToVersionImpl(gameVersionNumber, version, java, analyzer) && LAUNCH_WRAPPER_MAIN.equals(version.getMainClass()) &&
                    version.getLibraries().stream()
                            .filter(library -> "launchwrapper".equals(library.getArtifactId()))
                            .anyMatch(library -> VersionNumber.asVersion(library.getVersion()).compareTo(VersionNumber.asVersion("1.13")) < 0);
        }
    },
    VANILLA_JAVA_8_51(false, GameVersionNumber.atLeast("1.13"), VersionNumber.atLeast("1.8.0_51")),
    VANILLA_LINUX_JAVA_8(true, GameVersionNumber.atMost("1.12.999"), VersionNumber.atMost("1.8.999")) {
        @Override
        protected boolean appliesToVersionImpl(GameVersionNumber gameVersionNumber, @Nullable Version version,
                                               @Nullable JavaRuntime java, @Nullable LibraryAnalyzer analyzer) {
            return OperatingSystem.CURRENT_OS == OperatingSystem.LINUX
                    && Architecture.SYSTEM_ARCH == Architecture.X86_64
                    && (java == null || java.getArchitecture() == Architecture.X86_64);
        }

        @Override
        public boolean checkJava(GameVersionNumber gameVersionNumber, Version version, JavaRuntime java) {
            return java.getArchitecture() != Architecture.X86_64 || super.checkJava(gameVersionNumber, version, java);
        }
    },
    VANILLA_X86(false, VersionRange.all(), VersionRange.all()) {
        @Override
        protected boolean appliesToVersionImpl(GameVersionNumber gameVersionNumber, @Nullable Version version,
                                               @Nullable JavaRuntime java, @Nullable LibraryAnalyzer analyzer) {
            if (java == null || java.getArchitecture() != Architecture.ARM64)
                return false;

            if (OperatingSystem.CURRENT_OS == OperatingSystem.WINDOWS || OperatingSystem.CURRENT_OS == OperatingSystem.MACOS)
                return gameVersionNumber.compareTo("1.6") < 0;

            return false;
        }

        @Override
        public boolean checkJava(GameVersionNumber gameVersionNumber, Version version, JavaRuntime java) {
            return java.getArchitecture().isX86();
        }
    };

    private final boolean isMandatory;
    private final VersionRange<GameVersionNumber> gameVersionRange;
    private final VersionRange<VersionNumber> javaVersionRange;

    JavaVersionConstraint(boolean isMandatory, VersionRange<GameVersionNumber> gameVersionRange, VersionRange<VersionNumber> javaVersionRange) {
        this.isMandatory = isMandatory;
        this.gameVersionRange = gameVersionRange;
        this.javaVersionRange = javaVersionRange;
    }

    public boolean isMandatory() {
        return isMandatory;
    }

    public VersionRange<GameVersionNumber> getGameVersionRange() {
        return gameVersionRange;
    }

    public VersionRange<VersionNumber> getJavaVersionRange(Version version) {
        return javaVersionRange;
    }

    public final boolean appliesToVersion(@Nullable GameVersionNumber gameVersionNumber, @Nullable Version version,
                                          @Nullable JavaRuntime java, LibraryAnalyzer analyzer) {
        return gameVersionRange.contains(gameVersionNumber)
                && appliesToVersionImpl(gameVersionNumber, version, java, analyzer);
    }

    protected boolean appliesToVersionImpl(GameVersionNumber gameVersionNumber, @Nullable Version version,
                                           @Nullable JavaRuntime java, @Nullable LibraryAnalyzer analyzer) {
        GameJavaVersion gameJavaVersion;
        if (version == null || (gameJavaVersion = version.getJavaVersion()) == null) {
            return true;
        }

        String versionNumber = gameJavaVersion.majorVersion() >= 9
                ? String.valueOf(gameJavaVersion.majorVersion())
                : "1." + gameJavaVersion.majorVersion();

        VersionRange<VersionNumber> range = getJavaVersionRange(version);
        VersionNumber maximum = range.getMaximum();

        return maximum == null || maximum.compareTo(versionNumber) >= 0;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean checkJava(GameVersionNumber gameVersionNumber, Version version, JavaRuntime java) {
        return getJavaVersionRange(version).contains(java.getVersionNumber());
    }

    public static final List<JavaVersionConstraint> ALL = Lang.immutableListOf(values());
}
