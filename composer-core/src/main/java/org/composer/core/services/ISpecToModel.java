package org.composer.core.services;

import org.composer.core.model.Specs;
import org.composer.core.model.CompareUsersModel;

import java.util.List;

public interface ISpecToModel {
    CompareUsersModel getModelFromSpecs(Specs specs) throws Exception;

}
