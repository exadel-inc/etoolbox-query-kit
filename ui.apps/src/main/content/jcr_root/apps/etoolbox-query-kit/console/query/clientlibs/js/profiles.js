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
