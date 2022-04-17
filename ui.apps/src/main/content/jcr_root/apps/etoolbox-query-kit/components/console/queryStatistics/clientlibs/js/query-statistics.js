(function (document, $, ns) {
    'use strict';

    $(document).on('click', '.active-index', function (index) {
        $('#tab-queries-by-index')[0].click();
        let indexId = this.getAttribute('value');
        setTimeout((function() {
            $(`#${indexId}`)[0].scrollIntoView();
        }), 0)
    });
})(document, Granite.$, Granite.Eqk = (Granite.Eqk || {}));
