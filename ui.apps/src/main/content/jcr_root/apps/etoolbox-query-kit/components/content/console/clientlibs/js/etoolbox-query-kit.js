"use strict"

$(function () {
    var $executeButton = $('#executeButton'),
        $queryForm = $('#queryForm'),
        $languageSelect = $('#languageSelect')[0],
        $saveButton = $('#saveButton'),
        $domain = 'crx/de/index.jsp#',
        timerId = null,
        editor = null,
        editorLines = null;

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
                success: executeSuccess,
                error(error) {
                    if (error.status === 400){
                        editorLines.style.textDecoration="underline red";
                        editorLines.style.textDecorationStyle="dashed";
                    }
                    console.error(error.statusText);
                  }
            })
        });
    }));

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
       iconSize: "M",
    }).on("click", (function () {
        doPostForExport('pdf', 'PDF');
    }));

    var buttonExportExcel = new Coral.Button().set({
       label: {
         innerHTML: "XSLX"
       },
       variant: "quiet",
       icon: "fileXML",
       iconSize: "M"
    }).on("click", (function (){
        doPostForExport('vnd.openxmlformats-officedocument.spreadsheetml.sheet', 'XSLX');
    }));

    var buttonExportJSON = new Coral.Button().set({
       label: {
           innerHTML: 'JSON'
       },
       variant: "quiet",
       icon: "fileJson",
       iconSize: "M"
    }).on("click", (function () {
        doPostForExport('json', 'JSON');
    }));

    exportButtonsGroup.items.add(buttonExportPdf);
    exportButtonsGroup.items.add(buttonExportExcel);
    exportButtonsGroup.items.add(buttonExportJSON);

    $executeButton.after(exportButtonsGroup);

    $queryForm.on('keyup', (function (e) {
        editorLines.style.textDecoration="none";
        clearTimeout(timerId);
        timerId = setTimeout(function(){
            localStorage.setItem($languageSelect.selectedItem.value, editor.getValue());
        }, 1000);
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
            doPostForPagination(language, query, offset_next, limitInput[0].get('value'));
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
            doPostForPagination(language, query, offset_previous, limitInput[0].get('value'));
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
            var newOffset = (pageSelect.selectedItem.get('value') - 1) * limit;
            doPostForPagination(language, query, newOffset, limit);
        })

        exportButtonsGroup.set({ disabled: data.length > 0 })

        $('#resultInfo').remove();
        var resultInfo = document.createElement("div");
        resultInfo.setAttribute('id', 'resultInfo');
        resultInfo.innerText = `${data['offset'] + 1} - ${data['offset'] + data["data"].length} rows of ${data['resultCount'] !== -1 ? data['resultCount'] : 'unknown'}`;
        var resultTable = $("#resultTable");
        resultTable.before(resultInfo);
        resultTable.after(pageSelect).after(buttonNextPage).after(buttonPreviousPage);
    }

    function doPostForExport(type, format){
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
       xhrRequest.send(`language=${$languageSelect.selectedItem.value}&query=${editor.getValue()}&format=${format}`);
    }

    function doPostForPagination(language, query, offset, limit){
       $.ajax({
           url: $queryForm.attr('action'),
           type: "POST",
           data: {"language": language, "query": query, "offset": offset, "limit": limit},
           success: executeSuccess
       })
    }

    setTimeout(function init(){
        editor = document.querySelector('.CodeMirror').CodeMirror;
        editorLines = document.querySelector('.CodeMirror-lines');
        editor.setValue(localStorage.getItem($languageSelect.selectedItem.value));
    }, 0)
});