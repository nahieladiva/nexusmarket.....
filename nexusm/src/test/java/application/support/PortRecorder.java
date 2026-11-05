package application.support;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Remembers which operations of an input port were called, to prove that a test exercises all of
 * them.
 */
public final class PortRecorder {

    private final Set<String> called = ConcurrentHashMap.newKeySet();

    @SuppressWarnings("unchecked")
    public <T> T wrap(Class<T> port, T target) {
        return (T) Proxy.newProxyInstance(port.getClassLoader(), new Class<?>[] {port},
            (proxy, method, args) -> {
                called.add(operation(port, method));
                try {
                    return method.invoke(target, args);
                } catch (InvocationTargetException e) {
                    throw e.getCause();
                }
            });
    }

    public List<String> notCalled(Class<?>... ports) {
        List<String> missing = new ArrayList<>();
        for (Class<?> port : ports) {
            for (Method method : port.getDeclaredMethods()) {
                if (!called.contains(operation(port, method))) {
                    missing.add(operation(port, method));
                }
            }
        }
        return missing;
    }

    private String operation(Class<?> port, Method method) {
        return port.getSimpleName() + "." + method.getName();
    }
}
