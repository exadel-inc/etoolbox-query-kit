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

    const foundationUi = $(window).adaptTo('foundation-ui');

    function fillSettingsDialog(e) {
        const $dialog = $(e.target);
        $dialog.find('.coral-Form-field[name]').each(function (index, input) {
            const $input = $(input);
            const storedValue = ns.DataStore.getValue($input.attr('name'), $input.data('default-value'));
            $input.adaptTo('foundation-field').setValue(storedValue);
        });
        const $form = $dialog.find('form');
        $form.submit(function (e) {
            e.preventDefault();
            completeSettingsDialog($form);
        });
    }

    function completeSettingsDialog($dialog) {
        $dialog.find('.coral-Form-field[name]').each(function (index, input) {
            const $input = $(input);
            ns.DataStore.setValue($input.attr('name'), $input.adaptTo('foundation-field').getValue());
        });
        $dialog.closest('coral-dialog').remove();
        foundationUi.notify('User settings saved');
        ns.runAction('eqk.query.execute', this);
    }

    $(document).on('coral-overlay:open', '#userSettingsDialog', fillSettingsDialog);
})(window, Granite.$, Granite.Eqk = (Granite.Eqk || {}));
