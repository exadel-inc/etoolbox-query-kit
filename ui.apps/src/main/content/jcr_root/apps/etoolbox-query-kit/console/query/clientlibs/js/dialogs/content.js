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
(function (window, $, ns) {
    'use strict';

    const registry = $(window).adaptTo('foundation-registry');
    const foundationUi = $(window).adaptTo('foundation-ui');

    function manageContentDialog(e) {
        const $dialog = $(e.target);
        const $form = $dialog.find('form');
        $form.submit(function(e) {
            e.preventDefault();
            submitAsync($form);
            $dialog.remove();
        });
    }

    function submitAsync($form) {
        $.ajax({
            url: $form.attr('action'),
            method: 'post',
            dataType: 'json',
            data: $form.serialize(),
            beforeSend: function () {
                foundationUi.wait();
            },
            success: function () {
                foundationUi.notify('Changes saved');
                ns.runAction('eqk.query.replay', this);
            },
            error: function (e) {
                foundationUi.alert('EToolbox Query Console', 'Could not save changes' + (e.responseText ? ': ' + e.responseText : ''), 'error');
            },
            complete: function () {
                foundationUi.clearWait();
            }
        });

    }

    $(document).on('coral-overlay:open', '#editContentDialog', manageContentDialog);

})(window, Granite.$, Granite.Eqk = (Granite.Eqk || {}));
