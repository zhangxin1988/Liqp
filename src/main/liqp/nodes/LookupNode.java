package liqp.nodes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

class LookupNode implements LNode {

    private final String id;
    private final List<Indexable> indexes;

    public LookupNode(String id) {
        this.id = id;
        indexes = new ArrayList<Indexable>();
    }

    public void add(Indexable indexable) {
        indexes.add(indexable);
    }

    @Override
    public Object render(Map<String, Object> context) {

        Object value = context.get(id);

        for(Indexable index : indexes) {

            value = index.get(value, context);
        }

        return value;
    }

    interface Indexable {
        Object get(Object value, Map<String, Object> context);
    }

    public static class Hash implements Indexable {

        private final String hash;

        public Hash(String hash) {
            this.hash = hash;
        }

        @Override
        public Object get(Object value, Map<String, Object> context) {

            if(value == null) {
                return null;
            }

            if(hash.equals("size")) {
                if(value instanceof Collection) {
                    return ((Collection)value).size();
                }
                else if(value instanceof java.util.Map) {
                    java.util.Map map = (java.util.Map)value;
                    return map.containsKey(hash) ? map.get(hash) : map.size();
                }
                else if(value.getClass().isArray()) {
                    return ((Object[])value).length;
                }
            }
            else if(hash.equals("first")) {
                if(value instanceof java.util.List) {
                    java.util.List list = (java.util.List)value;
                    return list.isEmpty() ? null : list.get(0);
                }
                else if(value.getClass().isArray()) {
                    Object[] array = (Object[])value;
                    return array.length == 0 ? null : array[0];
                }
            }
            else if(hash.equals("last")) {
                if(value instanceof java.util.List) {
                    java.util.List list = (java.util.List)value;
                    return list.isEmpty() ? null : list.get(list.size() - 1);
                }
                else if(value.getClass().isArray()) {
                    Object[] array = (Object[])value;
                    return array.length == 0 ? null : array[array.length - 1];
                }
            }

            if(value instanceof java.util.Map) {
                return ((java.util.Map)value).get(hash);
            }
            else {
                return null;
            }
        }
    }

    public static class Index implements Indexable {

        private final LNode expression;

        public Index(LNode expression) {
            this.expression = expression;
        }

        @Override
        public Object get(Object value, Map<String, Object> context) {

            if(value == null) {
                return null;
            }

            Object key = expression.render(context);

            if(key instanceof Number) {
                int index = ((Number)key).intValue();

                if(value.getClass().isArray()) {
                    return ((Object[])value)[index];
                }
                else if(value instanceof List) {
                    return ((List<?>)value).get(index);
                }
                else {
                    return null;
                }
            }
            else {
                String hash = String.valueOf(key);
                return new Hash(hash).get(value, context);
            }
        }
    }
}
