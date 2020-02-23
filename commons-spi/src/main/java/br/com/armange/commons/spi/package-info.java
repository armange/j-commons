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
/**
 * Utility package for service loading.<br>
 * <p><b>{@link br.com.armange.commons.spi.Loader}</b></p>
 * <p>Useful structure for service loading.</p>
 * <pre>
 * //Loading a single service.
 * final Service loadService = Loader.loadService(Service.class);
 * 
 * //loading a list of service.
 * final List<Service> services = Loader.loadServices(Service.class);
 * </pre>
 * */
package br.com.armange.commons.spi;