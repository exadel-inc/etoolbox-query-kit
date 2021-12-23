"use strict"

$(function () {
    var $executeButton = $('#executeButton'),
        $queryForm = $('#queryForm'),
        $languageSelect = $('#languageSelect')[0],
        $saveButton = $('#saveButton'),
        $domain = 'crx/de/index.jsp#';

    $saveButton.on('click', (function () {
        var language = $languageSelect.selectedItem.value;
        var editor = document.querySelector('.CodeMirror').CodeMirror;
        var query = editor.getValue();
        if (query) {
            var url = '/services/etoolbox-query-kit/save';
            $.ajax({
                url: url,
                type: "POST",
                data: {"language": language, "query": query},
                success() {
                    $('#saveButton')[0].setAttribute('icon', 'starFill');
                    var coralIcon =  $('#saveButton coral-icon')[0];
                    coralIcon.setAttribute('icon', 'starFill');
                    coralIcon.classList.add('coral3-Icon--starFill');
                    coralIcon.classList.remove('coral3-Icon--starStroke');
                }
            });
        }
    }));

    $executeButton.click((function () {
        $queryForm.submit(function (e) {
            e.preventDefault();
            var form = $(this);
            var url = form.attr('action');
            $.ajax({
                url: url,
                type: "POST",
                data: form.serialize(),
                success: executeSuccess
            })
        });
    }));

    function executeSuccess(data) {
        $("coral-alert").remove(".coral3-Alert, .coral3-Alert--info, .coral3-Alert--small");
        $('.resultTable').remove();
        $('.query-kit-pagination').remove();
        buildResultTable(data["data"]);
        addPagination(data);
    }

    function buildResultTable(data) {
        if(data.length == 1){
            var alert = new Coral.Alert().set({
                header: {
                    innerHTML: "INFO:"
                },
                content: {
                    innerHTML: "0 results were found"
                }
            });
            $queryForm.after(alert);
            return;
        }
        var columns = Object.keys(data[0]);
        var table = new Coral.Table();
        table.classList.add('resultTable');
        table.setAttribute("id", "resultTable");
        columns.forEach(() => {
            table.appendChild(new Coral.Table.Column());
        });
        var head = new Coral.Table.Head();
        columns.forEach(item => {
            var headCell = new Coral.Table.HeaderCell();
            headCell.innerHTML = item;
            head.appendChild(headCell)
        });
        table.appendChild(head);
        data.forEach(item => {
            var row = table.items.add({});
            Object.entries(item).forEach(item => {
                var cell = new Coral.Table.Cell();
                cell.innerHTML = item[0] === 'path' ? '<a href="' + $domain + item[1] + '">' + item[1] + '</a>' : item[1];
                row.appendChild(cell);
            })
        });
        $queryForm.after(table);
    }

    function addPagination(data) {
        var pagesCount = Math.ceil(data["resultCount"] / data["limit"]);
        var currentPage = Math.floor(data["offset"] / data["limit"]) + 1;
        var language = data["language"];
        var query = data["query"];
        var offset = data["offset"] + data["data"].length;
        var limit = data["limit"];
        var limitInput = $('#limitInput');

        var buttonNextPage = new Coral.Button().set({
            label: {
                innerHTML: 'Next'
            },
            variant: "cta",
            iconSize: "M",
            disabled: currentPage === pagesCount
        });
        buttonNextPage.classList.add('query-kit-pagination');
        buttonNextPage.setAttribute("id", "nextPageButton");
        buttonNextPage.on("click" ,(function () {
            var url = $queryForm.attr('action');
            $.ajax({
                url: url,
                type: "POST",
                data: {"language": language, "query": query, "offset": offset, "limit": limitInput[0].get('value')},
                success: executeSuccess
            })
        }));

        var pageSelect = new Coral.Select().set({
            name: "Select",
            placeholder: "Choose a page"
        });
        pageSelect.classList.add('query-kit-pagination');
        for (let i = 1; i <= pagesCount; i++) {
            pageSelect.items.add({
                content: {
                    innerHTML: `${i} page`
                },
                value: i,
                disabled: false,
                selected: i === currentPage
            });
        }
        pageSelect.on("change",  function () {
            var url = $queryForm.attr('action');
            var newOffset = (pageSelect.selectedItem.get('value') - 1) * limit;
            $.ajax({
                url: url,
                type: "POST",
                data: {"language": language, "query": query, "offset": newOffset, "limit": limit},
                success: executeSuccess
            })
        })

        var buttonExportExcel = new Coral.Button().set({
            label: {
                innerHTML: 'Export to XSLX'
            },
            variant: "cta",
            iconSize: "M"
        });
        buttonExportExcel.classList.add('query-kit-pagination');
        buttonExportExcel.setAttribute("id", "buttonExportExcel");
        buttonExportExcel.on("click" ,(function () {
            var xhr = new XMLHttpRequest();
            xhr.open('POST', '/services/etoolbox-query-kit/export', true);
            xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
            xhr.responseType = 'arraybuffer';
            xhr.onload = function (e) {
                var blob = new Blob([this.response], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
                var downloadUrl = URL.createObjectURL(blob);
                var a = document.createElement("a");
                a.href = downloadUrl;
                a.download = "table.xlsx";
                document.body.appendChild(a);
                a.click();
                document.body.removeChild(a);
            };
            xhr.send(`language=${language}&query=${query}&format=XSLX`);
        }));

        var buttonExportPdf = new Coral.Button().set({
            label: {
                innerHTML: 'Export to PDF'
            },
            variant: "cta",
            iconSize: "M"
        });
        buttonExportPdf.classList.add('query-kit-pagination');
        buttonExportPdf.setAttribute("id", "buttonExportPdf");
        buttonExportPdf.on("click" ,(function () {
            var xhrPdf = new XMLHttpRequest();
            xhrPdf.open('POST', '/services/etoolbox-query-kit/export', true);
            xhrPdf.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
            xhrPdf.responseType = 'arraybuffer';
            xhrPdf.onload = function (e) {
                var blob = new Blob([this.response], { type: 'application/pdf' });
                var downloadUrl = URL.createObjectURL(blob);
                var a = document.createElement("a");
                a.href = downloadUrl;
                a.download = "table.pdf";
                document.body.appendChild(a);
                a.click();
                document.body.removeChild(a);
            };
            xhrPdf.send(`language=${language}&query=${query}&format=PDF`);
        }));

        $('#resultInfo').remove();
        var resultInfo = document.createElement("div");
        resultInfo.setAttribute('id', 'resultInfo');
        resultInfo.innerText = `${data['offset'] + 1} - ${data['offset'] + data["data"].length} rows of ${data['resultCount'] !== -1 ? data['resultCount'] : 'unknown'}`;
        var resultTable = $("#resultTable");
        resultTable.before(resultInfo);
        resultTable.after(buttonExportPdf).after(buttonExportExcel).after(pageSelect).after(buttonNextPage);
    }
});