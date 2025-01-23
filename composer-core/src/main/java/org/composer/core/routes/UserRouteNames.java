package org.composer.core.routes;

public enum UserRouteNames {
    NEW_COMPARE_USERS("new_CompareUsers_task"),
    AMQP("X_AMQP_step"),
    REST("X_Rest_step"),
    GRPC("X_GRPC_step"),
    RESULT("X_result"),
    END("close");
    public final String name;
    private UserRouteNames(String name){
        this.name = name;
    }

}
