(function(document, $) {
    "use strict";

    $(document).on("foundation-contentloaded", function() {
        var chooseQueryButton = $('.dialog-choose-query');
        if (chooseQueryButton) {
            for (let i = 0; i < chooseQueryButton.length; i++) {
                chooseQueryButton[i].on('click', (function () {
                    var query = $(this).context.parentElement.querySelector('textarea').value;
                    var language = $(this).context.parentElement.parentElement.getAttribute('language');
                    var editor = document.querySelector('.CodeMirror').CodeMirror;
                    editor.setValue(query);
                    var $sel = $('#languageSelect');
                    $sel.each(function(idx, select){
                        select.items.getAll().forEach(function(item, idx){
                            if(item.value === language){
                                item.selected = true;
                            }
                        });

                    });
                    $('#saveButton')[0].setAttribute('icon', 'starFill');
                    var coralIcon =  $('#saveButton coral-icon')[0];
                    coralIcon.setAttribute('icon', 'starFill');
                    coralIcon.classList.add('coral3-Icon--starFill');
                    coralIcon.classList.remove('coral3-Icon--starStroke');
                }));
            }
        }
        var select = $(".language-select coral-select-item[selected]")[0];
        if (select) {
            select = select.innerText;
            var multifields = $('.queries-multifield');
            multifields.hide();
            var multifield = $(`#${select}-multifield`);
            multifield.show();
        }
    });

    $(document).on("change", ".language-select", function() {
        var select = $(this).context.selectedItem.innerText;
        var multifields = $('.queries-multifield');
        multifields.hide();
        var multifield = $(`#${select}-multifield`);
        multifield.show();
    });

    $(document).on("change", ".language-multifield", function () {
        var multifieldItemsCount = $(this).context.items.length;
        if (multifieldItemsCount === 0) {
            $(this).context.querySelector('button[coral-multifield-add]').click();
        }
    });
})(document,Granite.$);