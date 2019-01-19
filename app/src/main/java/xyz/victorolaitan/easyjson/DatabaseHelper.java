package xyz.victorolaitan.easyjson;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class DatabaseHelper {

    public static <T> T deserializeClass(Class<T> aClass, EasyJSON jsonStructure) throws EasyJSONException {
        return deserializeClass(aClass, jsonStructure.getRootNode());
    }

    private static <T> T deserializeClass(Class<T> aClass, JSONElement jsonElement) throws EasyJSONException {
        T instance = null;
        try {
            instance = aClass.newInstance();
        } catch (InstantiationException e) {
            throw new EasyJSONException(EasyJSONException.INSTANTIATION_ERROR, e);
        } catch (IllegalAccessException e) {
            throw new EasyJSONException(EasyJSONException.ILLEGAL_ACCESS, e);
        }
        deserializeInstance(instance, jsonElement);
        return instance;
    }

    public static <T> void deserializeInstance(T instance, EasyJSON jsonStructure) throws EasyJSONException {
        deserializeInstance(instance, jsonStructure.getRootNode());
    }

    private static <T> void deserializeInstance(T instance, JSONElement jsonElement) throws EasyJSONException {
        for (int i = 0; i < jsonElement.getChildren().size(); i++) {
            JSONElement child = (JSONElement) jsonElement.getChildren().get(i);

            try {
                if (child.getKey() != null) {
                    Field field = instance.getClass().getDeclaredField(child.getKey());
                    field.setAccessible(true);
                    switch (child.getType()) {
                        case PRIMITIVE:
                            field.set(instance, child.getValue());
                            break;
                        case ARRAY:
                            field.set(instance, extractArrayElements(child));
                            break;
                        case STRUCTURE:
                            field.set(instance, deserializeClass(field.getType(), child));
                            break;
                        case ROOT:
                            break;
                    }
                }
            } catch (IllegalAccessException e) {
                throw new EasyJSONException(EasyJSONException.ILLEGAL_ACCESS, e);
            } catch (NoSuchFieldException e) {
                throw new EasyJSONException(EasyJSONException.FIELD_NOT_FOUND, e);
            }
        }
    }

    private static ArrayList extractArrayElements(JSONElement jsonElement) {
        ArrayList values = new ArrayList();
        for (int i = 0; i < jsonElement.getChildren().size(); i++) {
            JSONElement e = (JSONElement) jsonElement.getChildren().get(i);
            values.add(e.getValue());
        }
        return values;
    }

    public static <T> EasyJSON serializeInstance(T instance, Object... excludeFields) throws EasyJSONException {
        return serializeInstance(instance, EasyJSON.create().getRootNode(), excludeFields).getEasyJSONStructure();
    }

    private static <T> JSONElement serializeInstance(T instance, JSONElement sourceElement, Object... excludeFields) throws EasyJSONException {
        return serialize(instance, instance.getClass(), sourceElement, excludeFields);
    }

    public static <T> EasyJSON serializeClass(Class<T> aClass, Object... excludeFields) throws EasyJSONException {
        return serializeClass(aClass, EasyJSON.create().getRootNode(), excludeFields).getEasyJSONStructure();
    }

    public static <T> JSONElement serializeClass(Class<T> aClass, JSONElement sourceElement, Object... excludeFields) throws EasyJSONException {
        return serialize(null, aClass, sourceElement, excludeFields);
    }

    private static <T> JSONElement serialize(T instance, Class tClass, JSONElement sourceElement, Object... excludeFields) throws EasyJSONException {
        for (Field field : tClass.getDeclaredFields()) {
            if (!excludeContains(field, instance, excludeFields)) {
                try {
                    Object fieldValue = field.get(instance);
                    Class enclosingClass = fieldValue.getClass().getEnclosingClass();
                    if (enclosingClass != null && enclosingClass.equals(tClass)) {
                        sourceElement.putStructure(field.getName(),
                                serialize(fieldValue, fieldValue.getClass(), EasyJSON.create().getRootNode(), excludeFields));
                    } else if (fieldValue instanceof ArrayList) {
                        sourceElement.putArray(field.getName(), ((ArrayList) fieldValue).toArray());
                    } else {
                        sourceElement.putPrimitive(field.getName(), fieldValue);
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return sourceElement;
    }

    private static boolean excludeContains(Field field, Object instance, Object... excludeFields) {
        for (Object e : excludeFields) {
            try {
                // fml
                Object value = field.get(instance);
                if (value == e) {
                    return true;
                }
            } catch (Exception ignored) {
            }
        }
        return false;
    }
}
