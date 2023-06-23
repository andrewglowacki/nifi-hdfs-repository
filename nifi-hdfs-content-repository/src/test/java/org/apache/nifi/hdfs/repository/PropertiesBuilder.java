package org.apache.nifi.hdfs.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.nifi.util.NiFiProperties;
import org.junit.Ignore;

@Ignore
class PropertiesBuilder {

    protected static int SECTIONS_PER_CONTAINER = 10;

    protected static class Property {
        public String key;
        public String value;
    }
    protected static Property prop(String key, String value) {
        Property prop = new Property();
        prop.key = key;
        prop.value = value;
        return prop;
    }
    protected static NiFiProperties props(Property ... properties) {
        Map<String, String> map = new HashMap<>();
        map.put(HdfsContentRepository.SECTIONS_PER_CONTAINER_PROPERTY, "" + SECTIONS_PER_CONTAINER);
        for (Property prop : properties) {
            map.put(prop.key, prop.value);
        }
        return NiFiProperties.createBasicNiFiProperties(null, map);
    }

    protected static RepositoryConfig config(NiFiProperties properties) {
        return new RepositoryConfig(properties);
    }

    protected static Set<String> set(String ... values) {
        Set<String> set = new TreeSet<>();
        for (String value : values) {
            set.add(value);
        }
        return set;
    }
}
