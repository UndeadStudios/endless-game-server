package mgi.tools.parser.readers;

import com.google.common.collect.ImmutableMap;
import com.moandjiezana.toml.Toml;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mgi.tools.parser.TypeProperty;
import mgi.tools.parser.TypeReader;
import mgi.types.Definitions;
import mgi.types.component.ComponentDefinitions;
import mgi.types.component.ComponentType;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Tommeh | 01/02/2020 | 16:59
 * @see <a href="https://www.rune-server.ee/members/tommeh/">Rune-Server profile</a>
 */
public class ComponentReader implements TypeReader {
    private static final Int2ObjectOpenHashMap<Object2ObjectOpenHashMap<String, ComponentDefinitions>> namedComponents = new Int2ObjectOpenHashMap<>();
    private static final Int2ObjectOpenHashMap<List<ComponentDefinitions>> unnamedComponents = new Int2ObjectOpenHashMap<>();
    private static final Object2ObjectOpenHashMap<String, Object2ObjectOpenHashMap<String, ComponentDefinitions>> namedInterfaces = new Object2ObjectOpenHashMap<>();
    private static final Map<String, String> HOOKS = ImmutableMap.<String, String>builder().put("onLoadListener", "onload").put("onMouseOverListener", "onmouseover").put("onMouseLeaveListener", "onmouseleave").put("onTargetLeaveListener", "onttargetleave").put("onTargetEnterListener", "onttargetenter").put("onVarTransmitListener", "onvartransmit").put("onInvTransmitListener", "oninvtransmit").put("onStatTransmitListener", "onstattransmit").put("onTimerListener", "ontimer").put("onOpListener", "onop").put("onMouseRepeatListener", "onmouserepeat").put("onClickListener", "onclick").put("onClickRepeatListener", "onclickrepeat").put("onReleaseListener", "onrelease").put("onHoldListener", "onhold").put("onDragListener", "ondrag").put("onDragCompleteListener", "ondragcomplete").put("onScrollWheelListener", "onscrollwheel").build();

    @Override
    public ArrayList<Definitions> read(final Toml toml) throws NoSuchFieldException, IllegalAccessException, CloneNotSupportedException {
        final java.util.ArrayList<mgi.types.Definitions> defs = new ArrayList<Definitions>();
        final int id = toml.getLong("id", -1L).intValue();
        final java.lang.String name = toml.getString("name", "");
        if (toml.contains("id")) {
            final java.util.ArrayList<mgi.types.component.ComponentDefinitions> interfaceComponents = new ArrayList<ComponentDefinitions>();
            final java.util.ArrayList<com.moandjiezana.toml.Toml> subTomls = getComponents(toml);
            final mgi.types.component.ComponentDefinitions groupComponent = getComponent(subTomls.get(0), id);
            groupComponent.setInterfaceId(id);
            interfaceComponents.add(groupComponent);
            int componentId = 1;
            for (final com.moandjiezana.toml.Toml t : subTomls.subList(1, subTomls.size())) {
                final mgi.types.component.ComponentDefinitions component = getComponent(t, id);
                component.setComponentId(componentId++);
                interfaceComponents.add(component);
            }
            for (final mgi.types.component.ComponentDefinitions component : interfaceComponents) {
                applyHooks(component);
                if (!name.isEmpty()) {
                    namedInterfaces.computeIfAbsent(name, map -> new Object2ObjectOpenHashMap<>()).put(component.getName(), component);
                }
            }
            groupComponent.getChildren().addAll(interfaceComponents.subList(1, interfaceComponents.size()));
            defs.add(groupComponent);
        } else {
            final java.util.ArrayList<com.moandjiezana.toml.Toml> subTomls = getComponents(toml);
            for (final com.moandjiezana.toml.Toml subToml : subTomls) {
                final mgi.types.component.ComponentDefinitions component = getComponent(subToml, id);
                if (component == null) {
                    continue;
                }
                applyHooks(component);
                defs.add(component);
                ComponentDefinitions.add(component);
            }
        }
        return defs;
    }

    private int getHighestComponentId(final int interfaceId) {
        final java.util.ArrayList<mgi.types.component.ComponentDefinitions> components = new ArrayList<ComponentDefinitions>();
        final java.util.List<mgi.types.component.ComponentDefinitions> unnamedComponents = ComponentReader.unnamedComponents.get(interfaceId);
        if (unnamedComponents != null) {
            components.addAll(unnamedComponents);
        }
        final it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap<java.lang.String, mgi.types.component.ComponentDefinitions> namedComponents = ComponentReader.namedComponents.get(interfaceId);
        if (namedComponents != null) {
            components.addAll(namedComponents.values());
        }
        components.addAll(ComponentDefinitions.getComponents(interfaceId));
        if (components == null) {
            return -1;
        }
        int componentId = -1;
        for (final mgi.types.component.ComponentDefinitions component : components) {
            if (component.getInterfaceId() != interfaceId) {
                continue;
            }
            if (componentId < component.getComponentId()) {
                componentId = component.getComponentId();
            }
        }
        return componentId;
    }

    private ComponentDefinitions getComponent(final Toml toml, int id) throws CloneNotSupportedException, NoSuchFieldException, IllegalAccessException {
        ComponentDefinitions component;
        final java.lang.String name = toml.getString("name", "");
        if (toml.contains("inherit")) {
            final java.lang.String inherit = toml.getString("inherit", "");
            final java.lang.String[] split = inherit.split(":");
            final java.lang.String inheritedInterface = split[0];
            final java.lang.String inheritedComponent = split[1];
            final int interfaceId = (int) Double.parseDouble(inheritedInterface);
            final int componentId = Integer.parseInt(inheritedComponent);
            component = ComponentDefinitions.get(interfaceId, componentId).clone();
        } else {
            component = new ComponentDefinitions();
            component.setIf3(true);
        }
        TypeReader.setFields(component, toml.toMap());
        if (toml.contains("type")) {
            final java.lang.String typeIdentifier = toml.getString("type", "");
            final mgi.types.component.ComponentType type = ComponentType.get(typeIdentifier);
            if (type == null) {
                throw new IllegalStateException("Unknown component type: " + typeIdentifier);
            }
            if (type.equals(ComponentType.TEXT)) {
                //default properties for text components
                component.setColor("ff981f");
                component.setTextShadowed(true);
            }
            component.setType(type.getId());
        }
        if (toml.contains("id")) {
            id = toml.getLong("id").intValue();
        }
        if (toml.contains("parentid")) {
            final int parentId = toml.getLong("parentid", 0L).intValue();
            component.setParentId(component.getInterfaceId() << 16 | parentId);
        }
        if (toml.contains("layer") && id > -1) {
            final java.lang.String layer = toml.getString("layer", "");
            final it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap<java.lang.String, mgi.types.component.ComponentDefinitions> components = namedComponents.get(id);
            final mgi.types.component.ComponentDefinitions parentComponent = components.get(layer);
            if (parentComponent == null) {
                throw new RuntimeException("Parent component: " + layer + " doesn\'t exist for interface " + id);
            }
            component.setInterfaceId(parentComponent.getInterfaceId());
            component.setComponentId(getHighestComponentId(parentComponent.getInterfaceId()) + 1);
            component.setParentId(parentComponent.getInterfaceId() << 16 | parentComponent.getComponentId());
        } else if (toml.contains("generatecomponentid") && toml.getBoolean("generatecomponentid")) {
            component.setInterfaceId(id);
            component.setComponentId(getHighestComponentId(id) + 1);
            component.setParentId(component.getParentId());
        }
        if (toml.contains("color")) {
            final java.lang.String color = toml.getString("color", "");
            component.setColor(color);
        }
        if (toml.contains("shadowcolor")) {
            final java.lang.String color = toml.getString("shadowcolor", "");
            component.setShadowColor(color);
        }
        for (final mgi.tools.parser.TypeProperty property : TypeProperty.values) {
            final java.lang.String identifier = property.getIdentifier();
            if (!toml.contains(identifier)) {
                continue;
            }
            if (property.toString().startsWith("OP_")) {
                final int index = Integer.parseInt(identifier.substring(2)) - 1;
                component.setOption(index, toml.getString(identifier, ""));
            }
        }
        if (component.getHooks() == null) {
            component.setHooks(new HashMap<>());
        }
        for (final java.lang.String hook : HOOKS.values()) {
            if (toml.contains(hook)) {
                final java.util.ArrayList<java.lang.Object> list = (ArrayList<Object>) toml.getList(hook);
                final java.lang.Object[] arguments = list.toArray();
                component.getHooks().put(hook, arguments);
            }
        }
        if (id > -1) {
            if (name.isEmpty()) {
                unnamedComponents.computeIfAbsent(id, list -> new ArrayList<>()).add(component);
            } else {
                namedComponents.computeIfAbsent(id, map -> new Object2ObjectOpenHashMap<>()).put(name, component);
            }
        }
        return component;
    }

    private ArrayList<Toml> getComponents(final Toml toml) {
        final java.util.ArrayList<com.moandjiezana.toml.Toml> components = new ArrayList<Toml>();
        for (final java.util.Map.Entry<java.lang.String, java.lang.Object> entry : toml.entrySet()) {
            if (entry.getKey().equals("id") || entry.getKey().equals("name")) {
                continue;
            }
            final java.lang.Object value = entry.getValue();
            if (value instanceof Toml) {
                components.add((Toml) value);
            } else {
                components.addAll((ArrayList<Toml>) value);
            }
        }
        return components;
    }

    private void applyHooks(final ComponentDefinitions component) throws IllegalAccessException {
        //TODO support all hooks
        final java.util.Map<java.lang.String, java.lang.Object[]> hooks = component.getHooks();
        if (hooks == null || hooks.isEmpty()) {
            return;
        }
        final it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap<java.lang.String, mgi.types.component.ComponentDefinitions> components = namedComponents.get(component.getInterfaceId());
        final java.lang.Class<? extends mgi.types.component.ComponentDefinitions> clazz = component.getClass();
        for (final java.lang.reflect.Field field : clazz.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            field.setAccessible(true);
            if (HOOKS.containsKey(field.getName())) {
                final java.lang.String identifier = HOOKS.get(field.getName());
                if (!hooks.containsKey(identifier)) {
                    continue;
                }
                final java.util.ArrayList<java.lang.Object> transformed = new ArrayList<Object>();
                final java.lang.Object[] arguments = hooks.get(identifier);
                for (java.lang.Object arg : arguments) {
                    if (arg instanceof String) {
                        final java.lang.String value = (String) arg;
                        if (value.startsWith("component:")) {
                            final java.lang.String componentName = value.split(":")[1];
                            if (componentName.equals("self")) {
                                arg = -2147483645;
                            } else {
                                final mgi.types.component.ComponentDefinitions referredComponent = components.get(componentName);
                                if (referredComponent != null) {
                                    arg = referredComponent.getInterfaceId() << 16 | referredComponent.getComponentId();
                                }
                            }
                        } else if (value.startsWith("color:")) {
                            final java.lang.String color = value.split(":")[1];
                            arg = Integer.parseInt(color, 16);
                        }
                    } else if (arg instanceof Long) {
                        arg = ((Long) arg).intValue();
                    }
                    transformed.add(arg);
                }
                field.set(component, transformed.toArray());
            }
        }
    }

    @Override
    public String getType() {
        return "component";
    }
}
