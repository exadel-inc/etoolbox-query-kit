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
(function (document, ns, $) {
    'use strict';

    const PROFILE_KEY = 'eqk-profile';
    const DEFAULT_PROFILE = 'default';

    ns.getProfileName = function() {
        return localStorage.getItem(PROFILE_KEY) || DEFAULT_PROFILE;
    };

    $(document).on('foundation-contentloaded', function(e) {
        const $profile = $('[name="profile"]', e.target);
        Coral.commons.ready($profile[0], function(target) {
            const $target = $(target);
            $target.val(ns.getProfileName());
            $target.on('change', function() {
                localStorage.setItem(PROFILE_KEY, $target.val() || DEFAULT_PROFILE);
            });
        });
    });



})(document, Granite.Eqk = (Granite.Eqk || {}), Granite.$);
