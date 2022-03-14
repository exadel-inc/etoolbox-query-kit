(function (ns) {
    'use strict';

    const KEY_PAGE_SIZE = 'eqk-pageSize';
    const KEY_PROFILE = 'eqk-profile';
    const KEY_QUERY = 'eqk-query';

    const DEFAULT_PAGE_SIZE = 15;
    const DEFAULT_PROFILE = 'default';

    class DataStore {
        static get FAVORITE_QUERIES() {
            return 'eqk-favorite-queries';
        }

        static get LATEST_QUERIES() {
            return 'eqk-latest-queries';
        }

        // Session data

        static getCurrentQuery() {
            return sessionStorage.getItem(KEY_QUERY) || '';
        };

        static setCurrentQuery(value) {
            sessionStorage.setItem(KEY_QUERY, value);
        };

        // Locally stored data - Generic

        static getValue(key, defaultValue, postproc) {
            let result = localStorage.getItem(key) || defaultValue;
            if (postproc instanceof Function && result) {
                result = postproc(result);
            }
            return result;
        }

        static setValue(key, value, preproc) {
            let storable = value;
            if (preproc instanceof Function) {
                storable = preproc(storable);
            }
            localStorage.setItem(key, storable);
        }

        // Locally stored data - Particular

        static getProfileName() {
            return DataStore.getValue(KEY_PROFILE, DEFAULT_PROFILE);
        };

        static setProfileName(value) {
            DataStore.setValue(KEY_PROFILE, value);
        };

        static getPageSize() {
            return DataStore.getValue(KEY_PAGE_SIZE, DEFAULT_PAGE_SIZE);
        }

        static getFavoriteQueries() {
            return DataStore.getValue(this.FAVORITE_QUERIES, [], result => JSON.parse(result));
        }

        static setFavoriteQueries(value) {
            DataStore.setValue(this.FAVORITE_QUERIES, value, val => JSON.stringify(val));
        }

        static getLatestQueries() {
            return DataStore.getValue(this.LATEST_QUERIES, [], result => JSON.parse(result));
        }

        static setLatestQueries(value) {
            DataStore.setValue(this.LATEST_QUERIES, value, val => JSON.stringify(val));
        }
    }

    ns.DataStore = DataStore;
})(Granite.Eqk = (Granite.Eqk || {}));
