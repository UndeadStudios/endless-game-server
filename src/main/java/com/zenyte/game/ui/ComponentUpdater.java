package com.zenyte.game.ui;

import com.zenyte.GameEngine;
import com.zenyte.game.util.Utils;
import com.zenyte.game.world.World;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import mgi.Indice;
import mgi.types.component.ComponentDefinitions;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.util.Strings;

import javax.security.auth.login.LoginException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author Kris | 19/10/2018 13:05
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public class ComponentUpdater {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ComponentUpdater.class);
    private static final Int2ObjectOpenHashMap<InterfaceInformation> MAP = new Int2ObjectOpenHashMap<>();

    public static final void main(final String[] args) throws LoginException, InterruptedException {
        //Game.load();
        //Definitions.loadDefinitions(Definitions.LOW_PRIORITY_DEFINITIONS);
        //new Scanner().scan();
        /*val scanner = new FastClasspathScanner(Scanner.class.getPackage().getName());
        scanner.matchSubclassesOf(Interface.class, (SubclassMatchProcessor<Interface>) NewInterfaceHandler::add);
        scanner.scan();*/
        GameEngine.main(new String[0]);
        load();
        NewInterfaceHandler.INTERFACES.forEach((k, v) -> parseClass(v.getClass()));
        //
        //save();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            log.error(Strings.EMPTY, e);
        }
        System.exit(-1);
    }

    public static final void parseClass(final Class<? extends Interface> clazz) {
        try {
            final java.io.BufferedReader reader = new BufferedReader(new FileReader(new File("src/main/java/" + clazz.getName().replaceAll("[.]", "/") + ".java")));
            String line;
            final java.util.ArrayList<java.lang.String> file = new ArrayList<String>();
            final com.zenyte.game.ui.Interface instance = clazz.newInstance();
            instance.attach();
            final int k = instance.getInterface().getId();
            final com.zenyte.game.ui.ComponentUpdater.InterfaceInformation info = MAP.get(k);
            if (info == null) {
                log.info("Interface " + k + " does not exist in previous components dump.");
                return;
            }
            while ((line = reader.readLine()) != null) {
                final java.lang.String originalLine = line;
                line = line.trim();
                final java.lang.StringBuilder spaceBuilder = new StringBuilder();
                for (final int c : originalLine.chars().toArray()) {
                    if (c != ' ' && c != 9) break;
                    spaceBuilder.append((char) c);
                }
                if (line.startsWith("put(")) {
                    line = line.substring("put(".length(), line.length() - ");".length());
                    final java.lang.String[] split = line.split(",");
                    final java.lang.String name = split[split.length - 1].replaceAll("\"", "").trim();
                    final int bitpacked = findInfo(instance.getComponentInfoCopy(), name);
                    final int componentId = (bitpacked >> 16) & 65535;
                    final int slotId = bitpacked & 65535;
                    final com.zenyte.game.ui.ComponentUpdater.ComponentInformation component = find(info.information, comp -> comp.componentId == componentId && comp.slotId == slotId, " Info: " + k + ", " + componentId + ", " + slotId);
                    if (!component.name.equals(name)) {
                    }
                    //log.warning("Component name mismatch: " + k + ", " + componentId + ", " + slotId + ", " + name + " | " + component.name);
                    try {
                        final java.util.List<java.lang.String> count = getDifferentFieldsCount(component.definitions, ComponentDefinitions.get(k, componentId), "componentId");
                        if (count.size() > 0) {
                            throw new RuntimeException("Component: " + componentId + " is no longer valid.");
                        }
                        file.add(originalLine);
                    } catch (Exception e) {
                        log.error(Strings.EMPTY, e);
                        try {
                            final int[] array = new int[Utils.getIndiceSize(Indice.INTERFACE_DEFINITIONS, k)];
                            for (int i = 0; i < array.length; i++) {
                                final mgi.types.component.ComponentDefinitions defs = ComponentDefinitions.get(k, i);
                                final java.util.List<java.lang.String> differenceCount = getDifferentFieldsCount(defs, component.definitions, "componentId");
                                array[i] = differenceCount.size();
                            }
                            int smallestDifferenceCount = Integer.MAX_VALUE;
                            for (int i = 0; i < array.length; i++) {
                                final int value = array[i];
                                if (value < smallestDifferenceCount) {
                                    smallestDifferenceCount = value;
                                }
                            }
                            //up to three fields allowed to be different before the change is considered too much.
                            if (smallestDifferenceCount > 3) {
                                file.add(originalLine + "//TODO Find correct component id; ambiguous options.");
                                continue;
                            }
                            if (smallestDifferenceCount > 0) {
                                int ambiguousComponentsCount = 0;
                                for (final int key : array) {
                                    if (key == smallestDifferenceCount) ambiguousComponentsCount++;
                                }
                                if (ambiguousComponentsCount > 1) {
                                    file.add(originalLine + "//TODO Find correct component id; ambiguous options.");
                                    continue;
                                }
                            }
                            final int newComponentId = ArrayUtils.indexOf(array, smallestDifferenceCount);
                            file.add(spaceBuilder.toString() + "put(" + newComponentId + ", " + (split.length >= 3 ? (slotId + ", ") : "") + "\"" + name + "\");//Component updated.");
                        } catch (Exception a) {
                            //a.printStackTrace();
                            file.add(originalLine + "//TODO: Find correct component id");
                        }
                    }
                } else file.add(originalLine);
            }
            for (final java.lang.String l : file) {
                System.err.println(l);
            }
            System.err.println();
            System.err.println();
            System.err.println();
        } catch (Exception e) {
            log.error(Strings.EMPTY, e);
        }
    }

    private static final int findInfo(final Int2ObjectOpenHashMap<String> map, final String name) {
        for (final it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<java.lang.String> entry : map.int2ObjectEntrySet()) {
            if (entry.getValue().equals(name)) return entry.getIntKey();
        }
        return -1;
    }

    public static final List<String> getDifferentFieldsCount(final ComponentDefinitions a, final ComponentDefinitions b, final String... ignoreFields) {
        final java.util.ArrayList<java.lang.String> list = new ArrayList<String>();
        loop:
        for (final Field field : a.getClass().getDeclaredFields()) {
            if ((field.getModifiers() & 8) != 0) {
                continue;
            }
            field.setAccessible(true);
            final java.lang.String fieldName = field.getName();
            for (final java.lang.String ignored : ignoreFields) {
                if (fieldName.equals(ignored)) continue loop;
            }
            try {
                final java.lang.Object valueA = field.get(a);
                final java.lang.Object valueB = b == null ? null : field.get(b);
                if (valueA == null || valueB == null) {
                    if (valueA != valueB) {
                        list.add(fieldName);
                    }
                    continue;
                }
                final Class<?> type = field.getType();
                if (type == int[][].class) {
                    final int[][] arrayA = (int[][]) valueA;
                    final int[][] arrayB = (int[][]) valueB;
                    if (arrayA.length != arrayB.length) {
                        list.add(fieldName);
                        continue;
                    }
                    for (int i = 0; i < arrayA.length; i++) {
                        if (!Arrays.equals(arrayA[i], arrayB[i])) {
                            list.add(fieldName);
                            continue loop;
                        }
                    }
                } else if (type == int[].class) {
                    if (!Arrays.equals((int[]) valueA, (int[]) valueB)) {
                        list.add(fieldName);
                    }
                    continue;
                } else if (type == byte[].class) {
                    if (!Arrays.equals((byte[]) valueA, (byte[]) valueB)) {
                        list.add(fieldName);
                    }
                    continue;
                } else if (type == short[].class) {
                    if (!Arrays.equals((short[]) valueA, (short[]) valueB)) {
                        list.add(fieldName);
                    }
                    continue;
                } else if (type == double[].class) {
                    if (!Arrays.equals((double[]) valueA, (double[]) valueB)) {
                        list.add(fieldName);
                    }
                    continue;
                } else if (type == float[].class) {
                    if (!Arrays.equals((float[]) valueA, (float[]) valueB)) {
                        list.add(fieldName);
                    }
                    continue;
                } else if (type == String[].class) {
                    final java.lang.String[] arrayA = (String[]) valueA;
                    final java.lang.String[] arrayB = (String[]) valueB;
                    if (arrayA.length != arrayB.length) {
                        list.add(fieldName);
                        continue;
                    }
                    for (int i = 0; i < arrayA.length; i++) {
                        if (!arrayA[i].equals(arrayB[i])) {
                            list.add(fieldName);
                            continue loop;
                        }
                    }
                    continue;
                } else if (type == Object[].class) {
                    final java.lang.Object[] arrayA = (Object[]) valueA;
                    final java.lang.Object[] arrayB = (Object[]) valueB;
                    if (arrayA.length != arrayB.length) {
                        list.add(fieldName);
                        continue;
                    }
                    for (int i = 0; i < arrayA.length; i++) {
                        Object va = arrayA[i];
                        Object vb = arrayB[i];
                        if (va instanceof Double) {
                            va = ((Double) va).intValue();
                        }
                        if (vb instanceof Double) {
                            vb = ((Double) vb).intValue();
                        }
                        if (va == null || vb == null) {
                            if (va != vb) {
                                list.add(fieldName);
                                continue loop;
                            }
                        }
                        if (!va.equals(vb)) {
                            list.add(fieldName);
                            continue loop;
                        }
                    }
                    continue;
                }
                if (!field.get(a).equals(field.get(b))) {
                    list.add(fieldName);
                }
            } catch (Exception e) {
                log.error(Strings.EMPTY, e);
            }
        }
        return list;
    }

    private static final <T> T find(final List<T> list, final Predicate<T> predicate, final String additionalInfo) {
        for (final T value : list) {
            if (predicate.test(value)) return value;
        }
        throw new RuntimeException("Unable to locate an entry that suits the predicate." + additionalInfo);
    }

    private static final void load() {
        try {
            final java.io.BufferedReader reader = new BufferedReader(new FileReader("data/components.json"));
            final com.zenyte.game.ui.ComponentUpdater.InterfaceInformation[] definitions = World.getGson().fromJson(reader, InterfaceInformation[].class);
            for (final com.zenyte.game.ui.ComponentUpdater.InterfaceInformation info : definitions) {
                MAP.put(info.id, info);
            }
        } catch (Exception e) {
            log.error(Strings.EMPTY, e);
        }
    }

    private static final void save() {
        final java.util.ArrayList<com.zenyte.game.ui.ComponentUpdater.InterfaceInformation> list = new ArrayList<InterfaceInformation>(NewInterfaceHandler.INTERFACES.size());
        NewInterfaceHandler.INTERFACES.forEach((k, v) -> {
            List<ComponentInformation> componentList;
            final com.zenyte.game.ui.ComponentUpdater.InterfaceInformation info = new InterfaceInformation(k, componentList = new ArrayList<>());
            list.add(info);
            v.getComponentInfoCopy().forEach((bitpacked, name) -> {
                final int componentId = (bitpacked >> 16) & 65535;
                final int slotId = bitpacked & 65535;
                final com.zenyte.game.ui.ComponentUpdater.ComponentInformation componentInfo = new ComponentInformation(componentId, slotId, name, ComponentDefinitions.get(k, componentId));
                info.information.add(componentInfo);
            });
        });
        final java.lang.String json = World.getGson().toJson(list);
        try {
            //val dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            //val now = LocalDateTime.now();//TODO archive old file.
            final java.io.PrintWriter pw = new PrintWriter("data/components.json", "UTF-8");
            pw.println(json);
            pw.close();
        } catch (final Exception e) {
            log.error(Strings.EMPTY, e);
        }
    }


    private static final class InterfaceInformation {
        private final int id;
        private final List<ComponentInformation> information;

        public InterfaceInformation(final int id, final List<ComponentInformation> information) {
            this.id = id;
            this.information = information;
        }
    }


    private static final class ComponentInformation {
        private final int componentId;
        private final int slotId;
        private final String name;
        private final ComponentDefinitions definitions;

        public ComponentInformation(final int componentId, final int slotId, final String name, final ComponentDefinitions definitions) {
            this.componentId = componentId;
            this.slotId = slotId;
            this.name = name;
            this.definitions = definitions;
        }
    }
}
