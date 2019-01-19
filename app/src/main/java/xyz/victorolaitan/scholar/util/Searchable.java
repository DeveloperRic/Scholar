package xyz.victorolaitan.scholar.util;

import java.util.UUID;

public interface Searchable<T> extends ScholarModel {

    T search(UUID query);
}
