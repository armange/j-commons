/*
 * Copyright [2019] [Diego Armange Costa]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * */
package br.com.armange.commons.thread.builder;

/**
 * Class of support for lambda implementations that require final variables
 * when shared between different contexts. (Or for other appropriate uses)
 *
 * @param <T> The referenced value type
 * @author Diego Armange Costa
 * @since 2020-06-22 V1.1.0 (JDK 1.8)
 */
class Holder<T> {
    private T value;

    private Holder() {
    }

    /**
     * Constructs an empty holder for later injection of value.
     *
     * @param <U> The referenced value type
     * @return an empty holder for later injection of value.
     */
    public static <U> Holder<U> empty() {
        return new Holder<>();
    }

    /**
     * @return the referenced value
     */
    public T get() {
        return value;
    }

    /**
     * @param value the referenced value
     * @see Holder
     */
    public void set(final T value) {
        this.value = value;
    }
}
