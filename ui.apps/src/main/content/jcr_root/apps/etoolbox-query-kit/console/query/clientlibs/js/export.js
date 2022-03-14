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
(function (document, $, ns) {
    'use strict';

    // TODO try convert to granite-foundation actions

    $(document).on('click', '#buttonExportPdf', function () {
        doPostForExport('pdf', 'PDF');
    });

    $(document).on('click', '#buttonExportXslx', function () {
        doPostForExport('vnd.openxmlformats-officedocument.spreadsheetml.sheet', 'XSLX');
    });

    $(document).on('click', '#buttonExportJson', function () {
        doPostForExport('json', 'JSON');
    });

    function doPostForExport(type, format) {
        const query = ns.DataStore.getCurrentQuery();

        // TODO convert to AJAX request
        // foundation-io.wait()
        // then cancelWait()
        const xhrRequest = new XMLHttpRequest();
        xhrRequest.open('POST', '/services/etoolbox-query-kit/export', true);
        xhrRequest.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
        xhrRequest.responseType = 'arraybuffer';
        xhrRequest.onload = function (e) {
            const blob = new Blob([this.response], { type: `application/${type}` });
            const downloadUrl = URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = downloadUrl;
            a.download = `table.${format.toLowerCase()}`;
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
        };
        xhrRequest.send(`-query=${query}&-format=${format}`);
    }
})(document, Granite.$, Granite.Eqk = (Granite.Eqk || {}));
