/*
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
(function (window, $) {
    'use strict';

    $(window).adaptTo("foundation-registry").register("foundation.validation.validator", {
        selector: "[data-autocomplete-restricted]",
        validate: function(el) {

            const value = el.value;
            if (!value) {
                return 'You need to provide a value';
            }
            const isMatched = el.items.getAll().some(entry => entry.value === value);
            if (!isMatched) {
                return 'The value must match one of the available options';
            }
        }
    });
})(window, Granite.$);
