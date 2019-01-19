package xyz.victorolaitan.scholar.util;

public interface Nameable extends ScholarModel {

    String getName();

    void setName(String name);

    String getFancyName();

    String getShortName();
}
