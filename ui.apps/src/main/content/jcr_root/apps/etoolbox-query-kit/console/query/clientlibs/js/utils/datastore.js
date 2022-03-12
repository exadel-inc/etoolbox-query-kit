(function (ns) {
    'use strict';

    const KEY_PAGE_LIMIT = 'eqk-pageLimit';
    const KEY_PROFILE = 'eqk-profile';
    const KEY_QUERY = 'eqk-query';

    const DEFAULT_PAGE_LIMIT = 20;
    const DEFAULT_PROFILE = 'default';

    class DataStore {

        static getQuery() {
            return sessionStorage.getItem(KEY_QUERY) || '';
        };

        static setQuery(value) {
            sessionStorage.setItem(KEY_QUERY, value);
        };

        static getProfileName() {
            return localStorage.getItem(KEY_PROFILE) || DEFAULT_PROFILE;
        };

        static setProfileName(value) {
            localStorage.setItem(KEY_PROFILE, value);
        };

        static getPageLimit() {
            return localStorage.getItem(KEY_PAGE_LIMIT) || DEFAULT_PAGE_LIMIT;
        }

        static setPageLimit(value) {
            localStorage.setItem(KEY_PAGE_LIMIT, value);
        };

        static getQueries(key) {
            const storageItem = localStorage.getItem(key);
            return storageItem ? JSON.parse(storageItem) : [];
        };

        static setQueries(key, queries) {
            localStorage.setItem(key, JSON.stringify(queries));
        }
    }

    ns.DataStore = DataStore;

})(Granite.Eqk = (Granite.Eqk || {}));
