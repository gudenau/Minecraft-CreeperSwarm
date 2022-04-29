package net.gudenau.minecraft.creeperswarm;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public final class EnumAdder {
    private static final Map<Class<Enum<?>>, BiFunction<String, Object[], Enum<?>>> FACTORIES = new HashMap<>();
    
    @SuppressWarnings("unchecked")
    public static <E extends Enum<E>> E createEnum(Class<E> type, String name, Object... arguments) {
        if(!FACTORIES.containsKey(type)){
            type.getEnumConstants();
        }
        
        return (E) FACTORIES.get(type).apply(name, arguments);
    }
    
    @SuppressWarnings("unchecked")
    public static <E extends Enum<E>> void register(Class<E> type, BiFunction<String, Object[], E> factory) {
        FACTORIES.putIfAbsent((Class<Enum<?>>) type, (BiFunction<String, Object[], Enum<?>>) factory);
    }
}
