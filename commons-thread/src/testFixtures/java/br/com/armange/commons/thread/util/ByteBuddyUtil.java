package br.com.armange.commons.thread.util;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.MethodDelegation;

import static net.bytebuddy.matcher.ElementMatchers.named;

public final class ByteBuddyUtil {

    private static boolean installedByteBuddyAgent;

    public static <T> ByteBuddyChangesBuilder<T> changeClass(Class<T> sourceClass) {
        return new ByteBuddyChangesBuilder<>(sourceClass);
    }

    public static void resetClassDefinition(final Class<?> sourceClass) {
        installByteBuddyAgent();

        new ByteBuddy()
                .redefine(sourceClass)
                .make()
                .load(
                        sourceClass.getClassLoader(),
                        ClassReloadingStrategy.fromInstalledAgent());
    }

    public static class ByteBuddyChangesBuilder<T> {
        private final Class<T> sourceClass;

        private ByteBuddyChangesBuilder(final Class<T> sourceClass) {
            this.sourceClass = sourceClass;
        }

        public ByteBuddyChangesResultBuilder<T> whenCallMethod(final String methodName) {
            return new ByteBuddyChangesResultBuilder(sourceClass, methodName);
        }
    }

    public static class ByteBuddyChangesResultBuilder<T> {
        private final Class<T> sourceClass;
        private final String methodName;
        private final ByteBuddy byteBuddy = new ByteBuddy();

        private ByteBuddyChangesResultBuilder(Class<T> sourceClass, String methodName) {
            this.sourceClass = sourceClass;
            this.methodName = methodName;
        }

        public void doReturn(final Object value) {
            installByteBuddyAgent();

            byteBuddy
                    .redefine(sourceClass)
                    .method(named(methodName))
                    .intercept(FixedValue.value(value))
                    .make()
                    .load(
                            sourceClass.getClassLoader(),
                            ClassReloadingStrategy.fromInstalledAgent());
        }

        public void thenCall(final Class<?> interceptor) {
            installByteBuddyAgent();

            byteBuddy
                    .redefine(sourceClass)
                    .method(named(methodName))
                    .intercept(MethodDelegation.to(interceptor))
                    .make()
                    .load(
                            sourceClass.getClassLoader(),
                            ClassReloadingStrategy.fromInstalledAgent());
        }
    }

    private static void installByteBuddyAgent() {
        if (!installedByteBuddyAgent) {
            ByteBuddyAgent.install();

            installedByteBuddyAgent = true;
        }
    }
}
