"use strict"

$(function () {

    var $executeButton = $('#executeButton'),
        pagesCount = 5,
        currentPage = 1,
        template = "",
        offset,
        limit = 10,
        results = null,
        total = null,
        editor = null,
        editorLines = null,
        query;

    var wait = new Coral.Wait().set({
        size: "L",
        centered: true,
        id: "coralWait",
        hidden: true
    });

    $executeButton.on("click", (function () {
        updateUrlParams();
        query = editor.getValue();
        currentPage = 1;
        results = null;
        offset = 0;
        updateTable();
    }));

    $(document).on('click', '.range-page', function (e) {
        var page = e.target.value;
        query = editor.getValue();
        offset = (page - 1) * limit;
        currentPage = page;
        updateTable()
    });

    $(document).on('click', '.navigation-button', function (e) {
        var page = e.target.value;
        if (e.target.classList.contains('next')) {
            currentPage++;
            offset = page * limit;
        } else {
            currentPage--;
            offset = (page - 1) * limit;
        }
        query = editor.getValue();
        updateTable()
    });

    function updateTable() {
        document.body.appendChild(wait);
        $.ajax({
            url: "/apps/etoolbox-query-kit/components/content/console/jcr%3Acontent/content/items/result/items/container/items/result-table.html",
            type: "GET",
            data: {"query": query, "offset": offset, "limit": limit, "_total": total === null},
            beforeSend: function(){
                $('#coralWait').modal();
            },
            success: function (data) {
                $('.navigation-button').attr('disabled', false);
                if (currentPage === 1) {
                    $('.previous').attr('disabled', true);
                } else if (currentPage === total) {
                    $('.next').attr('disabled', true);
                }

                $('.pagination').attr('hidden', false);
                var table = $(data);
                offset = 0;
                results = 500;
                total = results / limit;
                replaceTable(table);
                addPagination();
            },
            error: function (error) {
                console.log('error');
            },
            complete: function () {
                $('#coralWait').modal('hide');
            }
        })
    }

    function replaceTable(table) {
        $('#resultTable').remove();
        $('#resultColumn').prepend(table);
        console.log('table');
    }

    function updateUrlParams() {
        query = editor.getValue();
        if (query && query.trim().length > 0 && history.pushState) {
            var newUrl = window.location.origin + window.location.pathname +
                '?query=' + encodeURIComponent(query);
            window.history.pushState({path:newUrl},'',newUrl);
        }
    }

    function addPagination() {
        template = "";
        if (total <= pagesCount) {
            addRangePages(1, total);
        } else if (currentPage < pagesCount) {
            addRangePages(1, pagesCount);
            addLastPage();
        } else if (currentPage > total - pagesCount + 1) {
            addFirstPage();
            addRangePages(total - pagesCount + 1, total);
        } else {
            addFirstPage();
            addRangePages(currentPage - 1, currentPage + 1);
            addLastPage();
        }
        $('#pagesContainer')[0].innerHTML = template;
    }

    function addFirstPage() {
        template += `<button ${currentPage === 1 ? 'disabled' : ''} class='first-button'>1</button><i class='first'>...</i>`;
    }

    function addRangePages(start, end) {
        for (let i = start; i <= end; i++) {
            template += `<button class='range-page' value='${i}' ${i === currentPage ? 'disabled' : ''}>${i}</button>`;
        }
    }

    function addLastPage() {
        template += `<i class='last'>...</i><button ${total === currentPage ? 'disabled' : ''} class='last-button'>${total}</button>`;
    }

    setTimeout(function init() {
        editor = document.querySelector('.CodeMirror').CodeMirror;
        editorLines = document.querySelector('.CodeMirror-lines');
    }, 0)
});
