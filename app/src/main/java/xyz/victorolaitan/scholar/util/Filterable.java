package xyz.victorolaitan.scholar.util;

import java.util.Date;
import java.util.List;

public interface Filterable extends ScholarModel {

    List<Nameable> filter(Date date);

    List<Nameable> filterRecursively(Date date);
}
