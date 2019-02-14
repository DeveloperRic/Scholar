package xyz.victorolaitan.scholar.util;

import java.util.UUID;

import xyz.victorolaitan.scholar.session.DatabaseLink;
import xyz.victorolaitan.scholar.session.Session;

public abstract class ModelLink<E extends Indexable> {
    private UUID id;
    private E model;

    protected ModelLink(E model) {
        this.id = model.getId();
        this.model = model;
    }

    ModelLink(UUID id, E model) {
        this.id = id;
        this.model = model;
    }

    protected abstract E getMethod(DatabaseLink link, UUID id);

    protected abstract boolean postMethod(DatabaseLink link, E model);

    public UUID id() {
        return id;
    }

    public E get() {
        return model == null ? update() : model;
    }

    public E update() {
        return model = getMethod(Session.getSession().getDatabase(), id);
    }

    public boolean save() {
        return model != null && postMethod(Session.getSession().getDatabase(), model);
    }
}
