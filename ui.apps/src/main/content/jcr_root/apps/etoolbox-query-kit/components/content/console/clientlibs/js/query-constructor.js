"use strict"

$(function () {
    $('#constructorSelect button[coral-multifield-add]')[0].innerText = 'Add column';

    var andButton = $('#constructorWhere button[coral-multifield-add]')[0];
    andButton.innerText = 'AND';

    var orButton = andButton.cloneNode(true);
    orButton.innerText = 'OR'

    $('#constructorWhere')[0].appendChild(orButton);

    var queryConstructorForm = $('#queryConstructor');

    queryConstructorForm.submit(function (e) {
        e.preventDefault();
        var form = $(this);
        var url = form.attr('action');
        $.ajax({
            type: "POST",
            url: url,
            data: form.serialize(),
            success: function(data) {

            }
        });
    })
});

(function ($, $document) {
    $(window).on("load", function() {
        $('#constructorSelect button[coral-multifield-add]')[0].click();
        $('#constructorWhere button[coral-multifield-add]')[0].click();
        setTimeout(function () {
            $('coral-buttongroup')[0].remove();
        }, 50);
    });
})($, $(document));

(function(document, $) {

    $(document).on("click", "#constructorWhere button[coral-multifield-add]", function () {
        var multifieldWhereLength = $('#constructorWhere')[0].items.length;
        if (multifieldWhereLength < 2) {
            return;
        }
        var pressedButton = this.innerText.toLowerCase();
        setTimeout(function () {
            console.log("log");
            var buttonGroup = $('#constructorWhere')[0].items.getAll()[multifieldWhereLength - 1].querySelector('coral-buttongroup');

            buttonGroup.items.getAll().forEach(function(item, idx){
                    if(item.value === pressedButton){
                        item.selected = true;
                    }
                });
        }, 100);
    });
})(document,Granite.$);