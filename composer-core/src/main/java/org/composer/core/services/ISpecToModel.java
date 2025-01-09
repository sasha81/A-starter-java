package org.composer.core.services;

import org.composer.core.model.Specs;
import org.composer.core.model.XTaskModel;

import java.util.List;

public interface ISpecToModel {

    XTaskModel getModelFromSpecs(Specs specs);
    List<String> getExecutionSequence(Specs specs);

}
