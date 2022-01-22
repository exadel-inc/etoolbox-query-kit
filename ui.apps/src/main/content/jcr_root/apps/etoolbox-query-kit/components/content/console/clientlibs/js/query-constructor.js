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
                console.log(data);
                var editor = document.querySelector('.CodeMirror').CodeMirror;
                editor.setValue(data);
            }
        });
    })
});

(function ($, $document) {
    $(window).on("load", function() {
        $('#constructorWhere button[coral-multifield-add]')[0].click();
        setTimeout(function () {
            $('coral-buttongroup')[0].hide();
            // $('#constraintWhere')[0].items.getAll().forEach(function (item, idx) {
            //     item.setAttribute('variant', 'minimal')
            // });
        }, 50);
    });
})($, $(document));

(function(document, $) {

    var propertyNameNeed = ['simpleSearch', 'fullTextSearch', 'sameNode', 'childNode', 'descendantNode', 'length']
    var expressionNeed = ['simpleSearch', 'length', 'localName', 'name', 'fullTextSearch']
    $(document).on("click", "#constructorWhere button[coral-multifield-add]", function () {
        var multifieldWhereLength = $('#constructorWhere')[0].items.length;
        if (multifieldWhereLength < 2) {
            return;
        }
        var pressedButton = this.innerText.toLowerCase();
        setTimeout(function () {
            var buttonGroup = $('#constructorWhere')[0].items.getAll()[multifieldWhereLength - 1].querySelector('coral-buttongroup');

            buttonGroup.items.getAll().forEach(function(item, idx){
                    if(item.value === pressedButton){
                        item.selected = true;
                    }
                });
        }, 100);
    });

    $(document).on("change", "#constraintWhere", function() {
        var multifieldItem = this.closest('coral-multifield-item');
        var propertyNameWhere = multifieldItem.querySelector('#propertyNameWhere');
        propertyNameWhere.show();
        var operatorWhere = multifieldItem.querySelector('#operatorWhere');
        operatorWhere.show();
        var expressionWhere = multifieldItem.querySelector('#expressionWhere');
        expressionWhere.show();

        var selectedValue = $(this).context.selectedItem.value;
        if (selectedValue !== 'simpleSearch') {
            operatorWhere.hide();
        }
        if (propertyNameNeed.indexOf(selectedValue, 0) === -1) {
            propertyNameWhere.hide();
        }
        if (expressionNeed.indexOf(selectedValue, 0) === -1) {
            expressionWhere.hide();
        }
    });
})(document,Granite.$);