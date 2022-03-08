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
(function (window, document, $, URITemplate) {
    'use strict';

    const ns = '.' + Date.now();

    function resolveToggleable(control, src, target) {
        if (src) {
            let promise;
            promise = $.ajax({
                url: src,
                cache: false
            });
            return promise.then(function (html) {
                return $(html)
                    .on('foundation-toggleable-hide', function (e) {
                        const target = $(e.target);

                        requestAnimationFrame(function () {
                            target.detach();
                        });
                    })
                    .appendTo(document.body)
                    .trigger('foundation-contentloaded');
            });
        }
        let el;
        if (target) {
            el = $(target);
        } else {
            el = control.closest('.foundation-toggleable');
        }
        return $.Deferred().resolve(el).promise();
    }

    $(window).adaptTo('foundation-registry').register('foundation.collection.action.action', {
        name: 'foundation.dialog.query',
        handler: function (name, el, config) {
            const control = $(el);
            const target = config.data.target;
            const nesting = config.data.nesting;
            let src;
            if (config.data.src) {
                src = config.data.src;
            }
            resolveToggleable(control, src, target).then(function (toggleable) {
                const api = toggleable.adaptTo('foundation-toggleable');
                toggleable.off(ns).one('foundation-form-submitted' + ns, function (e, success, xhr) {
                    if (!success) {
                        return;
                    }
                    api.hide();
                });
                requestAnimationFrame(function () {
                    if (nesting === 'hide') {
                        const parentAPI = control.closest('.foundation-toggleable').adaptTo('foundation-toggleable');
                        if (parentAPI) {
                            parentAPI.hide();
                        }
                    }
                    api.show(el);
                });
            });
        }
    });
})(window, document, Granite.$, Granite.URITemplate);
