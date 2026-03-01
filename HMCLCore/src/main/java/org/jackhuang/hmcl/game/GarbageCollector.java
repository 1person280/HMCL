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

import java.util.List;

public enum GarbageCollector {
    G1GC("G1GC", "G1 Garbage Collector", 
            "-XX:+UseG1GC",
            List.of(
                "-XX:MaxGCPauseMillis=200",
                "-XX:G1HeapRegionSize=32m",
                "-XX:G1NewSizePercent=20",
                "-XX:G1ReservePercent=20",
                "-XX:G1MixedGCCountTarget=5"
            ),
            "settings.gc.g1gc.desc"),
    
    ZGC("ZGC", "Z Garbage Collector",
            "-XX:+UseZGC",
            List.of(
                "-XX:ZCollectionInterval=5",
                "-XX:ZAllocationSpikeTolerance=2"
            ),
            "settings.gc.zgc.desc"),
    
    SHENANDOAH("Shenandoah", "Shenandoah Garbage Collector",
            "-XX:+UseShenandoahGC",
            List.of(
                "-XX:ShenandoahGCHeuristics=compact"
            ),
            "settings.gc.shenandoah.desc"),
    
    PARALLELGC("ParallelGC", "Parallel Garbage Collector",
            "-XX:+UseParallelGC",
            List.of(
                "-XX:ParallelGCThreads=4"
            ),
            "settings.gc.parallelgc.desc"),
    
    SERIALGC("SerialGC", "Serial Garbage Collector",
            "-XX:+UseSerialGC",
            List.of(),
            "settings.gc.serialgc.desc"),
    
    DEFAULT("Default", "Default (Auto-selected by JVM)",
            null,
            List.of(),
            "settings.gc.default.desc");

    private final String name;
    private final String displayName;
    private final String mainFlag;
    private final List<String> additionalFlags;
    private final String descriptionKey;

    GarbageCollector(String name, String displayName, String mainFlag, List<String> additionalFlags, String descriptionKey) {
        this.name = name;
        this.displayName = displayName;
        this.mainFlag = mainFlag;
        this.additionalFlags = additionalFlags;
        this.descriptionKey = descriptionKey;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getMainFlag() {
        return mainFlag;
    }

    public List<String> getAdditionalFlags() {
        return additionalFlags;
    }

    public String getDescriptionKey() {
        return descriptionKey;
    }

    public boolean isAvailable(int javaVersion) {
        switch (this) {
            case G1GC:
                return javaVersion >= 7;
            case ZGC:
                return javaVersion >= 15;
            case SHENANDOAH:
                return javaVersion >= 12;
            case PARALLELGC:
                return javaVersion >= 5;
            case SERIALGC:
                return javaVersion >= 5;
            case DEFAULT:
                return true;
            default:
                return false;
        }
    }

    public static GarbageCollector fromName(String name) {
        if (name == null || name.isEmpty()) {
            return DEFAULT;
        }
        for (GarbageCollector gc : values()) {
            if (gc.name.equalsIgnoreCase(name)) {
                return gc;
            }
        }
        return DEFAULT;
    }
}
