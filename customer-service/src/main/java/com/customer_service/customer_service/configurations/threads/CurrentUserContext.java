package com.customer_service.customer_service.configurations.threads;


import com.customer_service.customer_service.authentication.user.CurrentContext;

public class CurrentUserContext {
    private static final ThreadLocal<CurrentContext> currentContext = new ThreadLocal<>();

    public static CurrentContext getCurrentContext() {
        return currentContext.get();
    }

    public static void setCurrentContext(CurrentContext context) {
        currentContext.set(context);
    }

    public static void clear() {
        currentContext.remove();
    }


}
