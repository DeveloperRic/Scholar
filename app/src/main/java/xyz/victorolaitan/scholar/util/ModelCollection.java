package xyz.victorolaitan.scholar.util;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import xyz.victorolaitan.scholar.session.DatabaseLink;

public abstract class ModelCollection<E extends Indexable> implements Iterable<E> {
    private HashMap<UUID, ModelLink<E>> map = new HashMap<>();

    protected abstract E getMethod(DatabaseLink link, UUID id);

    protected abstract boolean postMethod(DatabaseLink link, E model);

    public E get(int index) {
        int i = 0;
        for (ModelLink<E> link : map.values()) {
            if (i == index) return link.get();
            i++;
        }
        return null;
    }

    public E get(UUID id) {
        return get(id, false);
    }

    @SuppressWarnings("ConstantConditions")
    public E get(UUID id, boolean forceRefresh) {
        return map.containsKey(id) ? (forceRefresh ? map.get(id).update() : map.get(id).get()) : null;
    }

    public void add(UUID id) {
        add(id, null);
    }

    public void add(E model) {
        add(model.getId(), model);
    }

    private void add(UUID id, E model) {
        map.put(id, new ModelLink<E>(id, model) {
            @Override
            protected E getMethod(DatabaseLink link, UUID id) {
                return ModelCollection.this.getMethod(link, id);
            }

            @Override
            protected boolean postMethod(DatabaseLink link, E model) {
                return ModelCollection.this.postMethod(link, model);
            }
        });
    }

    public void addAll(Collection<? extends E> c) {
        for (E e : c) {
            add(e);
        }
    }

    public void clear() {
        map.clear();
    }

    public void remove(E model) {
        map.remove(model.getId());
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public int size() {
        return map.size();
    }

    public List<E> toList() {
        List<E> l = new ArrayList<>();
        for (ModelLink<E> link : map.values()) {
            l.add(link.get());
        }
        return l;
    }

    @NonNull
    @Override
    public Iterator<E> iterator() {
        return new ModelIterator();
    }

    private class ModelIterator implements Iterator<E> {
        private int index;

        @Override
        public boolean hasNext() {
            return index < map.size();
        }

        @Override
        public E next() {
            return get(index++);
        }
    }
}
