"use strict"

$(function () {
    var $executeButton = $('#executeButton'),
        $queryForm = $('#queryForm'),
        $languageSelect = $('#languageSelect')[0],
        $saveButton = $('#saveButton'),
        $domain = 'crx/de/index.jsp#',
        timerId = null;

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
        $('.resultTable').remove();
        $('.query-kit-pagination').remove();
        buildResultTable(data["data"]);
        addPagination(data);
    }

    function buildResultTable(data) {
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

    var exportButtonsGroup = new Coral.ButtonGroup().set({
          name: "export-buttons-group",
          disabled: true
    });

    var buttonExportPdf = new Coral.Button().set({
       label: {
           innerHTML: 'PDF'
       },
       variant: "quiet",
       icon: "filePDF",
       iconSize: "M"
    });

    var buttonExportExcel = new Coral.Button().set({
       label: {
         innerHTML: "XML"
       },
       variant: "quiet",
       icon: "fileXML",
       iconSize: "M"
    });

    var buttonExportJSON = new Coral.Button().set({
       label: {
           innerHTML: 'JSON'
       },
       variant: "quiet",
       icon: "fileJson",
       iconSize: "M"
    });

    exportButtonsGroup.items.add(buttonExportPdf);
    exportButtonsGroup.items.add(buttonExportExcel);
    exportButtonsGroup.items.add(buttonExportJSON);

    $executeButton.after(exportButtonsGroup);

    $queryForm.on('keyup', (function (e) {
        var editor = document.querySelector('.CodeMirror').CodeMirror;
        var editorLines = document.querySelector('.CodeMirror-lines');
        var language = $languageSelect.selectedItem.value;
        clearTimeout(timerId);
        timerId = setTimeout(function(){
            localStorage.setItem(language, editor.getValue());
        }, 1000);
    }));

    function addPagination(data) {
        var pagesCount = Math.ceil(data["resultCount"] / data["limit"]);
        var currentPage = Math.floor(data["offset"] / data["limit"]) + 1;
        var language = data["language"];
        var query = data["query"];
        var offset_next = data["offset"] + data["data"].length;
        var offset_previous = data["offset"] - data["data"].length;
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
                data: {"language": language, "query": query, "offset": offset_next, "limit": limitInput[0].get('value')},
                success: executeSuccess
            })
        }));

        var buttonPreviousPage = new Coral.Button().set({
            label: {
                innerHTML: 'Previous'
            },
            variant: "cta",
            iconSize: "M",
            disabled: currentPage === 1
        });
        buttonPreviousPage.classList.add('query-kit-pagination');
        buttonPreviousPage.setAttribute("id", "buttonPreviousPage");
        buttonPreviousPage.on("click" ,(function () {
            var url = $queryForm.attr('action');
            $.ajax({
                url: url,
                type: "POST",
                data: {"language": language, "query": query, "offset": offset_previous, "limit": limitInput[0].get('value')},
                success: executeSuccess
            })
        }));

        var pageSelect = new Coral.Select().set({
            name: "Select",
            placeholder: "Choose a page",
            disabled: data['resultCount'] === -1
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

        exportButtonsGroup.set({ disabled: data.length > 0 })

        buttonExportExcel.on("click", (function (){
            doPostForExport('vnd.openxmlformats-officedocument.spreadsheetml.sheet', 'XSLX', language, query);
        }));

        buttonExportPdf.on("click", (function () {
            doPostForExport('pdf', 'PDF', language, query);
        }));

        buttonExportJSON.on("click", (function () {
            doPostForExport('json', 'JSON', language, query);
        }));

        $('#resultInfo').remove();
        var resultInfo = document.createElement("div");
        resultInfo.setAttribute('id', 'resultInfo');
        resultInfo.innerText = `${data['offset'] + 1} - ${data['offset'] + data["data"].length} rows of ${data['resultCount'] !== -1 ? data['resultCount'] : 'unknown'}`;
        var resultTable = $("#resultTable");
        resultTable.before(resultInfo);
        resultTable.after(pageSelect).after(buttonNextPage).after(buttonPreviousPage);
    }

    function doPostForExport(type, format, language, query){
       var xhrRequest = new XMLHttpRequest();
       xhrRequest.open('POST', '/services/etoolbox-query-kit/export', true);
       xhrRequest.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
       xhrRequest.responseType = 'arraybuffer';
       xhrRequest.onload = function (e) {
           var blob = new Blob([this.response], { type: `application/${type}`});
           var downloadUrl = URL.createObjectURL(blob);
           var a = document.createElement("a");
           a.href = downloadUrl;
           a.download = `table.${type}`;
           document.body.appendChild(a);
           a.click();
           document.body.removeChild(a);
       };
       xhrRequest.send(`language=${language}&query=${query}&format=${format}`);
    }

    setTimeout(function init(){
        var editor = document.querySelector('.CodeMirror').CodeMirror;
        var language = $languageSelect.selectedItem.value;
        editor.setValue(localStorage.getItem(language));
    }, 0)
});