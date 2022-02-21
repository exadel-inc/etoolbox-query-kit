"use strict"

$(function () {

    var $executeButton = $('#executeButton'),
        $resultContainer = $('#resultContainer'),
        $queryForm = $('#queryForm'),
        $domain = 'crx/de/index.jsp#',
        editor = null,
        editorLines = null,
        $editRowForm = $('#editRowDialogForm');

    // $executeButton.on("click", (function () {
    //     updateUrlParams();
    //     location.reload();
    // }));

    $editRowForm.submit(function (e) {
        e.preventDefault();
        var form = $(this);
        var url = form.attr('action');
        var data = form.serialize();
        $.ajax({
            url: url,
            type: "POST",
            data: data,
            success: function (data) {
                console.log('success');
            },
            error: function (error) {
                console.log('LOL');
            }
        })
    });

    // $queryForm.submit(function (e) {
    //     e.preventDefault();
    //     var form = $(this);
    //     var url = form.attr('action');
    //     var editor = document.querySelector('.CodeMirror').CodeMirror;
    //     var query = editor.getValue();
    //     var data = form.serialize().replace(/query=.+?&/, `query=${query}&`);
    //     $.ajax({
    //         url: url,
    //         type: "GET",
    //         data: data,
    //         success: executeSuccess,
    //         error: function (error) {
    //             if (error.status === 400){
    //                 editorLines.style.textDecoration="underline red";
    //                 editorLines.style.textDecorationStyle="dashed";
    //             }
    //             console.error(error.statusText);
    //             var dialog = getPromptDialog('error', 'Query is incorrect');
    //             dialog.show();
    //         }
    //     })
    // });

    $queryForm.on('keyup', (function () {
        editorLines.style.textDecoration="none";
        updateUrlParams();
    }));

    function executeSuccess(data) {
        console.log('AAAAA');
        updateUrlParams();
    }

    function updateUrlParams() {
         var query = editor.getValue();
         if (query && query.trim().length > 0 && history.pushState) {
            var newUrl = window.location.origin + window.location.pathname +
               '?query=' + encodeURIComponent(query);
            window.history.pushState({path:newUrl},'',newUrl);
         }
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
                innerHTML: "<button is=\"coral-button\" variant=\"primary\" coral-close=\"\">Ok</button>"
            }
        });
    }

   setTimeout(function init() {
       editor = document.querySelector('.CodeMirror').CodeMirror;
       editorLines = document.querySelector('.CodeMirror-lines');
   }, 0)

    function updateLimit() {
        var limitFromSettings = JSON.parse(localStorage.getItem('resultNumberField'));
        $('#limitInput')[0].setAttribute('value', limitFromSettings);
    }
});