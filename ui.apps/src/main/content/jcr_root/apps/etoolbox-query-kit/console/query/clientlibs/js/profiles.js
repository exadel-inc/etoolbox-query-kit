(function (document, $, ns) {
    'use strict';

    $(document).on('foundation-contentloaded', function (e) {
        const $profile = $('[name="profile"]', e.target);
        Coral.commons.ready($profile[0], function (target) {
            const $target = $(target);
            $target.val(ns.DataStore.getProfileName);
            $target.on('change', function () {
                ns.DataStore.setProfileName($target.val());
            });
        });
    });
})(document, Granite.$, Granite.Eqk = (Granite.Eqk || {}));
