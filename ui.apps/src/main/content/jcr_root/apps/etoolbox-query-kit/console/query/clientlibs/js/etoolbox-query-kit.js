'use strict';

$(function () {
    const $executeButton = $('#btnExecute');
    const $resultContainer = $('#resultContainer');
    const $queryForm = $('#queryForm');
    const $domain = 'crx/de/index.jsp#';
    let editor = null;
    let editorLines = null;

    $queryForm.submit(function (e) {
        e.preventDefault();
        updateLimit();
        const form = $(this);
        const url = form.attr('action');
        const editor = document.querySelector('.CodeMirror').CodeMirror;
        const query = editor.getValue();
        const data = form.serialize().replace(/query=.+?&/, `query=${query}&`);
        $.ajax({
            url: url,
            type: 'POST',
            data: data,
            success: executeSuccess,
            error: function (error) {
                if (error.status === 400) {
                    editorLines.style.textDecoration = 'underline red';
                    editorLines.style.textDecorationStyle = 'dashed';
                }
                console.error(error.statusText);
                const dialog = getPromptDialog('error', 'Query is incorrect');
                dialog.show();
            }
        });
    });

    $queryForm.on('keyup', function () {
        editorLines.style.textDecoration = 'none';
        updateUrlParams();
    });

    function executeSuccess(data) {
        $('.resultTable').remove();
        $('.query-kit-pagination').remove();
        $('#resultInfo').remove();
        $(document).trigger('query-kit:success-response', [data]);
        updateUrlParams();
        if (data && data.results.length === 0) {
            const dialog = getPromptDialog('warning', 'No results found');
            dialog.show();
        } else {
            buildResultTable(data.results);
            addPagination(data);
        }
    }

    function updateUrlParams() {
        const query = editor.getValue();
        if (query && query.trim().length > 0 && history.pushState) {
            const newUrl = window.location.origin + window.location.pathname +
               '?query=' + encodeURIComponent(query);
            window.history.pushState({ path: newUrl }, '', newUrl);
        }
    }

    function buildResultTable(data) {
        const table = new Coral.Table();
        table.classList.add('resultTable');
        table.setAttribute('id', 'resultTable');
        const columns = Object.keys(data);
        columns.forEach(() => {
            table.appendChild(new Coral.Table.Column());
        });
        const head = new Coral.Table.Head();
        columns.forEach(item => {
            const headCell = new Coral.Table.HeaderCell();
            headCell.innerHTML = item;
            head.appendChild(headCell);
        });
        table.appendChild(head);
        for (let i = 0; i < data.path.length; i++) {
            var row = table.items.add({});
            Object.keys(data).forEach(key => {
                const cell = new Coral.Table.Cell();
                cell.innerHTML = key === 'path' ? '<a href="' + $domain + data[key][i] + '">' + data[key][i] + '</a>' : data[key][i];
                row.appendChild(cell);
            });
        }
        $resultContainer.append(table);
    }

    function addPagination(data) {
        const pagesCount = Math.ceil(data.resultCount / data.limit);
        const currentPage = Math.floor(data.offset / data.limit) + 1;
        const language = data.language;
        const query = data.query;
        const offset_next = data.offset + data.results.path.length;
        const offset_previous = data.offset - data.results.path.length;
        const limit = data.limit;
        const limitInput = $('#limitInput');

        const buttonNextPage = new Coral.Button().set({
            label: {
                innerHTML: 'Next'
            },
            variant: 'cta',
            iconSize: 'M',
            disabled: currentPage === pagesCount
        });
        buttonNextPage.classList.add('query-kit-pagination');
        buttonNextPage.setAttribute('id', 'nextPageButton');
        buttonNextPage.on('click', function () {
            doPostForPagination(language, query, offset_next, limitInput[0].getAttribute('value'));
        });

        const buttonPreviousPage = new Coral.Button().set({
            label: {
                innerHTML: 'Previous'
            },
            variant: 'cta',
            iconSize: 'M',
            disabled: currentPage === 1
        });
        buttonPreviousPage.classList.add('query-kit-pagination');
        buttonPreviousPage.setAttribute('id', 'buttonPreviousPage');
        buttonPreviousPage.on('click', function () {
            doPostForPagination(language, query, offset_previous, limitInput[0].getAttribute('value'));
        });

        const pageSelect = new Coral.Select().set({
            name: 'Select',
            placeholder: 'Choose a page',
            disabled: data.resultCount === -1
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
        pageSelect.on('change', function () {
            const newOffset = (pageSelect.selectedItem.get('value') - 1) * limit;
            doPostForPagination(language, query, newOffset, limit);
        });

        const resultInfo = document.createElement('div');
        resultInfo.setAttribute('id', 'resultInfo');
        resultInfo.innerText = `${data.offset + 1} - ${data.offset + data.results.path.length} rows of ${data.resultCount !== -1 ? data.resultCount : 'unknown'}`;
        const resultTable = $('#resultTable');
        resultTable.before(resultInfo);
        resultTable.after(pageSelect).after(buttonNextPage).after(buttonPreviousPage);
    }

    function doPostForPagination(language, query, offset, limit) {
        $.ajax({
            url: $queryForm.attr('action'),
            type: 'POST',
            data: { language: language, query: query, offset: offset, limit: limit },
            success: executeSuccess
        });
    }

    function getPromptDialog(variant, text) {
        return new Coral.Dialog().set({
            variant: variant,
            header: {
                innerHTML: variant.toUpperCase()
            },
            content: {
                innerHTML: text
            },
            footer: {
                innerHTML: '<button is="coral-button" variant="primary" coral-close="">Ok</button>'
            }
        });
    }

    setTimeout(function init() {
        editor = document.querySelector('.CodeMirror').CodeMirror;
        editorLines = document.querySelector('.CodeMirror-lines');
    }, 0);

    function updateLimit() {
        const limitFromSettings = JSON.parse(localStorage.getItem('resultNumberField'));
        $('#limitInput')[0].setAttribute('value', limitFromSettings);
    }
});
