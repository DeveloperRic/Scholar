package xyz.victorolaitan.scholar.controller;

import xyz.victorolaitan.scholar.session.DatabaseLink;

public interface ModelCtrl extends FragmentCtrl {

    void duplicateModel();

    void deleteModel();

    boolean postModel(DatabaseLink database);
}
