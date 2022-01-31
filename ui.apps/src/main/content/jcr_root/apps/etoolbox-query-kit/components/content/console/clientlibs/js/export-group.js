(function(document, $) {
    "use strict";

    $(document).on('click', '#buttonExportPdf', function() {
        doPostForExport('pdf', 'PDF');
    });

    $(document).on('click', '#buttonExportXslx', function() {
        doPostForExport('vnd.openxmlformats-officedocument.spreadsheetml.sheet', 'XSLX');
    });

    $(document).on('click', '#buttonExportJson', function() {
        doPostForExport('json', 'JSON');
    });

    $(document).on("query-kit:success-response", function(event, result) {
        $("[name='export-buttons-group']").prop( "disabled", !(result.data && result.data.length > 0));
    });

    function doPostForExport(type, format) {
       var $languageSelect = $('#languageSelect')[0],
           editor = document.querySelector('.CodeMirror').CodeMirror,
           value = editor.getValue(),
           language = $languageSelect.selectedItem.value;

       var xhrRequest = new XMLHttpRequest();
       xhrRequest.open('POST', '/services/etoolbox-query-kit/export', true);
       xhrRequest.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
       xhrRequest.responseType = 'arraybuffer';
       xhrRequest.onload = function (e) {
           var blob = new Blob([this.response], { type: `application/${type}`});
           var downloadUrl = URL.createObjectURL(blob);
           var a = document.createElement("a");
           a.href = downloadUrl;
           a.download = `table.${type.toLowerCase()}`;
           document.body.appendChild(a);
           a.click();
           document.body.removeChild(a);
       };
       xhrRequest.send(`language=${language}&query=${value}&format=${format}`);
    }

})(document, Granite.$);