package io.github.komet0141.subclassCollector.utils;

import java.util.HashMap;
import java.util.Map;

public class InstanceHolder <SUPER_CLASS> extends HashMap<String, SUPER_CLASS> {
    private boolean isLocked = false;
    public boolean put(Class<? extends SUPER_CLASS> clazz) {
        String name = clazz.getName();
        if (containsKey(name) || isLocked) return false;
        try {
            clazz.getConstructor();
            put(name, clazz.newInstance());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public SUPER_CLASS putIfAbsent(String key, SUPER_CLASS value) {
        if(isLocked) return null;
        return super.putIfAbsent(key, value);
    }
    
    @Override
    public SUPER_CLASS put(String key, SUPER_CLASS value) {
        if(isLocked) return null;
        return super.put(key, value);
    }
    
    @Override
    public void putAll(Map<? extends String, ? extends SUPER_CLASS> m) {
        if(isLocked) return;
        super.putAll(m);
    }
    
    public SUPER_CLASS get(Class<? extends SUPER_CLASS> clazz) {
        return get(clazz.getName());
    }
    
    public void lock(){isLocked = true;}
}