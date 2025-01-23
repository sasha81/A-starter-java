package org.composer.core.stubs;

import org.composer.core.model.CompareUsersModel;
import org.composer.core.model.Specs;
import org.composer.core.services.ISpecToModel;

public class ExceptionSpecToModelStub implements ISpecToModel {
    private final String errMsg;

    public ExceptionSpecToModelStub(String errMsg) {
        this.errMsg = errMsg;
    }

    @Override
    public CompareUsersModel getModelFromSpecs(Specs specs) throws Exception {
        throw new Exception(errMsg);
    }
}
