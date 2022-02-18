(function () {
    const keywords = 'all and as asc by cast contains default desc distinct explain fail from inner in index is ischildnode isdescendantnode issamenode join left like measure native not null option ok or order outer right select similar spellcheck suggest tag traversal union warn where';
    const builtin = 'binary boolean coalesce decimal double length localname long lower name path property reference score string upper uri weakreference';
    const dateSQL = 'date';

    function processStr(str) {
        return str.split(/\s+/).reduce((obj, el) => {
            obj[el] = true;
            return obj;
        }, {});
    }

    CodeMirror.defineMIME('text/sql2', {
        name: 'sql',
        keywords: processStr(keywords),
        builtin: processStr(builtin),
        dateSQL: processStr(dateSQL)
    });
})();
